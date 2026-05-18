package com.lms.entities;

/** Loan lifecycle states. OVERDUE is computed, not stored — see LoanFines. */
public enum LoanStatus {
    BORROWED,
    RETURNED,
    OVERDUE
}
