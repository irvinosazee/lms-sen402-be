package com.lms.services;

import com.lms.entities.Loan;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
