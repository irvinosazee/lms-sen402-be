package com.lms.services;

import com.lms.entities.Loan;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Pure fine math. No Spring, no DB — keeps it trivially testable. */
public final class LoanFines {

    private LoanFines() {}   // static-only

    /** Days past dueDate. Uses returnDate when present (frozen), else now (still accruing). */
    public static long daysOverdue(Loan loan) {
        LocalDateTime end = loan.getReturnDate() != null ? loan.getReturnDate() : LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(loan.getDueDate(), end);
        return Math.max(0L, days);
    }

    /** daysOverdue × rate. Returns 0 for on-time loans. */
    public static long fineAccrued(Loan loan, long ratePerDay) {
        return daysOverdue(loan) * ratePerDay;
    }
}
