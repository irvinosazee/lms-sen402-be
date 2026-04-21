package com.lms.repositories;

import com.lms.entities.Loan;
import com.lms.entities.LoanStatus;
import com.lms.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUser(User user);
    List<Loan> findByStatus(LoanStatus status);
}
