package com.lms.repositories;

import com.lms.entities.Loan;
import com.lms.entities.LoanStatus;
import com.lms.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Loans. Mixes Spring-Data derived queries (findBy/countBy method names) with explicit JPQL
 * (@Query) where the derived-naming convention would be too clunky.
 */
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUser(User user);
    List<Loan> findByStatus(LoanStatus status);

    long countByStatus(LoanStatus status);
    long countByUserAndStatus(User user, LoanStatus status);

    /** Count of loans currently checked out past their due date. */
    @Query("SELECT count(l) FROM Loan l WHERE l.status = 'BORROWED' AND l.dueDate < :now")
    long countOverdue(LocalDateTime now);

    /** Same as countOverdue but scoped to a single user. */
    @Query("SELECT count(l) FROM Loan l WHERE l.user = :user AND l.status = 'BORROWED' AND l.dueDate < :now")
    long countOverdueByUser(User user, LocalDateTime now);

    /** Most recent loans system-wide — feeds the dashboard activity panel. */
    @Query("SELECT l FROM Loan l ORDER BY l.borrowDate DESC")
    List<Loan> findRecentActivity(Pageable pageable);

    /** Same as findRecentActivity, but for one user's own feed. */
    @Query("SELECT l FROM Loan l WHERE l.user = :user ORDER BY l.borrowDate DESC")
    List<Loan> findRecentActivityByUser(User user, Pageable pageable);

    /** Used by the borrow-gate in LoanService — covers both unsettled-and-still-borrowed and
     *  unsettled-but-returned cases. The service then filters for actual fineAccrued > 0. */
    List<Loan> findByUserAndFinePaidFalse(User user);

    /** Used by StatsService to sum outstanding fines across the system. */
    @Query("SELECT l FROM Loan l WHERE l.finePaid = false AND l.dueDate < :now")
    List<Loan> findUnpaidPotentiallyOverdue(LocalDateTime now);
}
