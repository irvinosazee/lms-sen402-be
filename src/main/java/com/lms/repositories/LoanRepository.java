package com.lms.repositories;

import com.lms.entities.Loan;
import com.lms.entities.LoanStatus;
import com.lms.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUser(User user);
    List<Loan> findByStatus(LoanStatus status);
    
    long countByStatus(LoanStatus status);
    long countByUserAndStatus(User user, LoanStatus status);
    
    @Query("SELECT count(l) FROM Loan l WHERE l.status = 'BORROWED' AND l.dueDate < :now")
    long countOverdue(LocalDateTime now);
    
    @Query("SELECT count(l) FROM Loan l WHERE l.user = :user AND l.status = 'BORROWED' AND l.dueDate < :now")
    long countOverdueByUser(User user, LocalDateTime now);

    @Query("SELECT l FROM Loan l ORDER BY l.borrowDate DESC")
    List<Loan> findRecentActivity(Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.user = :user ORDER BY l.borrowDate DESC")
    List<Loan> findRecentActivityByUser(User user, Pageable pageable);
}
