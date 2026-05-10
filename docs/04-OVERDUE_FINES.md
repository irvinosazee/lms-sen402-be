# Overdue Fines

The fine system is the most recently added feature. This doc explains how it works so you can extend it or debug it without re-reading the source.

If you want the design rationale (why we did it this way), see [`../../../docs/superpowers/specs/2026-05-10-overdue-fines-design.md`](../../../docs/superpowers/specs/2026-05-10-overdue-fines-design.md).

---

## What it does

- A student who returns a book **after the 14-day due date** owes a fine: `daysOverdue × LOAN_FINE_PER_DAY` (₦100/day by default).
- The fine is **computed on read** — there's no scheduler, no daily cron job, no stored "current fine" column. Every time the loan is serialized, we recompute.
- A librarian or admin **settles** the fine after the book is returned. The student doesn't self-settle (you pay at the desk, like in real life).
- While a fine is outstanding, the student **cannot borrow another book** until it's settled.

---

## The data model

Only two new columns on `loans`:

```java
// apps/backend/src/main/java/com/lms/entities/Loan.java
@Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
private boolean finePaid = false;

private LocalDateTime finePaidAt;     // nullable
```

The `columnDefinition` default lets Hibernate's `ddl-auto: update` add the column without breaking existing rows.

One new config value:

```yaml
# apps/backend/src/main/resources/application.yml
app:
  loan:
    fine-per-day-naira: ${LOAN_FINE_PER_DAY:100}
```

---

## The math (`LoanFines.java`)

```java
public final class LoanFines {

    private LoanFines() {}

    public static long daysOverdue(Loan loan) {
        LocalDateTime end = loan.getReturnDate() != null ? loan.getReturnDate() : LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(loan.getDueDate(), end);
        return Math.max(0L, days);
    }

    public static long fineAccrued(Loan loan, long ratePerDay) {
        return daysOverdue(loan) * ratePerDay;
    }
}
```

- For a **borrowed** loan, `end = now()` → fine grows every day the book is overdue.
- For a **returned** loan, `end = returnDate` → fine is frozen at the value it had when the book came back.
- `ChronoUnit.DAYS.between` truncates partial days. Returning a 2-hour-late book registers as 0 days overdue. Fine.

This utility is **pure** — no side effects, no Spring, no database. Easy to test:

```java
// apps/backend/src/test/java/com/lms/services/LoanFinesTest.java
@Test
void returnedLate_freezesAtReturnDate() {
    LocalDateTime due = LocalDateTime.now().minusDays(10);
    LocalDateTime returned = due.plusDays(4);
    Loan l = loan(due, returned);
    assertEquals(4, LoanFines.daysOverdue(l));
    assertEquals(400L, LoanFines.fineAccrued(l, 100));
}
```

---

## How it's wired into responses

`LoanService.mapToResponse` (the entity → DTO converter) populates four computed fields on every `LoanResponseDTO`:

```java
long days = LoanFines.daysOverdue(loan);
long accrued = LoanFines.fineAccrued(loan, finePerDay);
response.setDaysOverdue(days);
response.setFineAccrued(accrued);
response.setFineOutstanding(loan.isFinePaid() ? 0L : accrued);
response.setFinePaid(loan.isFinePaid());
```

- `daysOverdue` and `fineAccrued` are independent of payment status — they reflect what the borrower *would* owe.
- `fineOutstanding` is **what's still due**. Becomes `0` once the librarian settles.
- `finePaid` tells the frontend whether to show "Settled" vs the outstanding amount.

---

## Settling a fine

```java
@Transactional
public LoanResponseDTO settleFine(Long loanId) {
    Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

    if (loan.getStatus() == LoanStatus.BORROWED) {
        throw new BadRequestException("Fine can only be settled after the book is returned");
    }
    if (loan.isFinePaid()) {
        throw new BadRequestException("Fine already settled");
    }
    if (LoanFines.fineAccrued(loan, finePerDay) == 0) {
        throw new BadRequestException("No outstanding fine on this loan");
    }

    loan.setFinePaid(true);
    loan.setFinePaidAt(LocalDateTime.now());
    return mapToResponse(loanRepository.save(loan));
}
```

Three rejection cases, in order:

1. **Status check first.** Can't settle a loan that's still borrowed (the student hasn't brought the book back — they pay at the desk on return).
2. **Already paid?** Stops double-settlement.
3. **Anything to settle?** A loan returned on time has `fineAccrued == 0` — nothing to mark paid.

Authorization is enforced at the controller:

```java
@PostMapping("/{loanId}/settle-fine")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public ResponseEntity<LoanResponseDTO> settleFine(@PathVariable Long loanId) {
    return ResponseEntity.ok(loanService.settleFine(loanId));
}
```

Students don't self-settle.

---

## The borrow-gate

When a student tries to borrow:

```java
// LoanService.borrowBook (excerpt)
var unpaid = loanRepository.findByUserAndFinePaidFalse(user);
boolean hasOutstandingFine = unpaid.stream()
        .anyMatch(l -> LoanFines.fineAccrued(l, finePerDay) > 0);
if (hasOutstandingFine) {
    throw new BadRequestException("Settle outstanding fines before borrowing again");
}
```

- Query all the student's loans where `fine_paid = false`.
- For each, compute the fine. If any has `fineAccrued > 0`, block.
- Why include not-yet-overdue loans in the query? Because Spring Data derived-query naming gets awkward fast. The stream filter is the cheap, readable way to express "...and overdue." For demo-scale data (≤ ~50 loans per user), this is a non-issue.

This is the moment of truth — the system actually *enforces* the rule rather than just displaying it.

---

## Dashboard aggregates (`StatsService`)

For ADMIN/LIBRARIAN:
```java
long allOutstanding = loanRepository.findUnpaidPotentiallyOverdue(LocalDateTime.now()).stream()
        .mapToLong(l -> LoanFines.fineAccrued(l, finePerDay))
        .sum();
metrics.put("outstandingFinesTotal", allOutstanding);
```

For STUDENT (their own outstanding fines):
```java
long myOutstanding = loanRepository.findByUserAndFinePaidFalse(user).stream()
        .mapToLong(l -> LoanFines.fineAccrued(l, finePerDay))
        .sum();
metrics.put("myOutstandingFines", myOutstanding);
```

The frontend renders these as metric cards on `/dashboard`.

---

## Frontend touchpoints

The fine system shows up in three places in the UI:

1. **`/dashboard/loans`** — Fine column, "Show overdue only" toggle, "Settle Fine" button (staff only).
2. **`/dashboard/books`** — Borrow click that fails with a fine block shows a toast with a "View My Loans" deep-link action.
3. **`/dashboard`** — Outstanding Fines metric card (amber) for staff; conditional "Your Fines" card for students with active fines.

Implementation refs in the frontend:
- `src/api/loan.api.ts` — `settleFine` POST.
- `src/hooks/use-loans.ts` — `useLoanMutations` exports `settleFine` and `borrowAsync`.
- `src/pages/LoansPage.tsx` — column, toggle, button + confirm dialog.
- `src/pages/BooksPage.tsx` — borrow `try/catch` with conditional toast.
- `src/features/dashboard/DashboardOverview.tsx` — metric cards.

---

## Configuration knob

`LOAN_FINE_PER_DAY` env var (default 100 naira/day).

- `0` disables fines entirely (every accrual is `0 × ... = 0`). Useful for testing flows where fines would block.
- Negative values are rejected on startup by the `LoanService` constructor guard:
  ```java
  if (finePerDay < 0) {
      throw new IllegalStateException("app.loan.fine-per-day-naira must be >= 0");
  }
  ```

Changing the rate while the app is running affects all NEW reads of borrowed loans. Returned loans keep their frozen fine (because the math uses `returnDate`, not `now()`).

---

## Things we deliberately didn't build

- **No fine history table.** Settlement events aren't audited separately. If asked, point to the activity feed and to the per-loan `finePaidAt` timestamp.
- **No partial payments.** You settle the full amount or nothing.
- **No grace period.** The fine starts accruing on day 1 past due.
- **No notifications.** No email or SMS reminders for overdue loans.
- **No retroactive recompute on rate change.** Frozen fines stay at the old rate; in-flight accruals immediately use the new rate.

The full out-of-scope list and the rationale for each cut is in the design spec linked at the top of this doc.

---

## Testing the feature manually

```bash
# 1. As a student with outstanding fines, try to borrow → expect 400
curl -X POST http://localhost:8081/api/v1/loans/borrow \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"bookId": 2}'
# → {"message": "Settle outstanding fines before borrowing again", ...}

# 2. As a librarian, return the overdue book first
curl -X POST http://localhost:8081/api/v1/loans/5/return \
  -H "Authorization: Bearer $LIBRARIAN_TOKEN"

# 3. Then settle the fine
curl -X POST http://localhost:8081/api/v1/loans/5/settle-fine \
  -H "Authorization: Bearer $LIBRARIAN_TOKEN"
# → loan body with finePaid=true, fineOutstanding=0

# 4. Student can now borrow again
```

The seed data is set up so the student already has one overdue loan on first boot — that's the row to play with.
