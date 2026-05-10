package com.lms.services;

import com.lms.entities.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoanFinesTest {

    private Loan loan(LocalDateTime dueDate, LocalDateTime returnDate) {
        Author a = new Author("X", "");
        Category c = new Category("Y", "");
        Book b = new Book("T", "I", 1, 1, a, c);
        User u = new User("u@x.com", "p", "F", "L", Role.STUDENT);
        Loan l = new Loan(b, u, dueDate.minusDays(14), dueDate, LoanStatus.BORROWED);
        l.setReturnDate(returnDate);
        if (returnDate != null) l.setStatus(LoanStatus.RETURNED);
        return l;
    }

    @Test
    void notOverdue_returnsZero() {
        Loan l = loan(LocalDateTime.now().plusDays(2), null);
        assertEquals(0, LoanFines.daysOverdue(l));
        assertEquals(0L, LoanFines.fineAccrued(l, 100));
    }

    @Test
    void borrowedAndOverdue_accruesAgainstNow() {
        Loan l = loan(LocalDateTime.now().minusDays(3), null);
        assertEquals(3, LoanFines.daysOverdue(l));
        assertEquals(300L, LoanFines.fineAccrued(l, 100));
    }

    @Test
    void returnedLate_freezesAtReturnDate() {
        LocalDateTime due = LocalDateTime.now().minusDays(10);
        LocalDateTime returned = due.plusDays(4);
        Loan l = loan(due, returned);
        assertEquals(4, LoanFines.daysOverdue(l));
        assertEquals(400L, LoanFines.fineAccrued(l, 100));
    }

    @Test
    void returnedOnTime_returnsZero() {
        LocalDateTime due = LocalDateTime.now().minusDays(2);
        LocalDateTime returned = due.minusDays(1);
        Loan l = loan(due, returned);
        assertEquals(0, LoanFines.daysOverdue(l));
        assertEquals(0L, LoanFines.fineAccrued(l, 100));
    }
}
