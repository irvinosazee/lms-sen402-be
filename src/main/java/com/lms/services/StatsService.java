package com.lms.services;

import com.lms.dtos.DashboardActivityDTO;
import com.lms.dtos.StatsResponseDTO;
import com.lms.entities.LoanStatus;
import com.lms.entities.Role;
import com.lms.entities.User;
import com.lms.repositories.BookRepository;
import com.lms.repositories.LoanRepository;
import com.lms.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Dashboard payloads. Staff see system-wide totals; students see their own. */
@Service
public class StatsService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final long finePerDay;

    public StatsService(BookRepository bookRepository, LoanRepository loanRepository,
                        UserRepository userRepository,
                        @Value("${app.loan.fine-per-day-naira:100}") long finePerDay) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.finePerDay = finePerDay;
    }

    /** Branches on role: staff get totals + system feed, students get personal stats + own feed. */
    public StatsResponseDTO getDashboardStats() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Long> metrics = new HashMap<>();
        List<DashboardActivityDTO> activities;

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.LIBRARIAN) {
            metrics.put("totalBooks", bookRepository.count());
            metrics.put("totalStudents", userRepository.count());
            metrics.put("activeLoans", loanRepository.countByStatus(LoanStatus.BORROWED));
            metrics.put("overdueCount", loanRepository.countOverdue(LocalDateTime.now()));
            // Sum unpaid fines across all overdue loans.
            metrics.put("outstandingFinesTotal",
                    loanRepository.findUnpaidPotentiallyOverdue(LocalDateTime.now()).stream()
                            .mapToLong(l -> LoanFines.fineAccrued(l, finePerDay)).sum());

            // System-wide last 5 loans for the activity feed.
            activities = loanRepository.findRecentActivity(PageRequest.of(0, 5)).stream()
                    .map(l -> new DashboardActivityDTO(l.getStatus().toString(),
                            l.getBook().getTitle(), l.getUser().getEmail(), l.getBorrowDate()))
                    .collect(Collectors.toList());
        } else {
            metrics.put("myActiveLoans", loanRepository.countByUserAndStatus(user, LoanStatus.BORROWED));
            metrics.put("overdueCount", loanRepository.countOverdueByUser(user, LocalDateTime.now()));
            metrics.put("totalBorrowed", (long) loanRepository.findByUser(user).size());
            metrics.put("myOutstandingFines",
                    loanRepository.findByUserAndFinePaidFalse(user).stream()
                            .mapToLong(l -> LoanFines.fineAccrued(l, finePerDay)).sum());

            // Personal feed — "You" instead of echoing their own email back at them.
            activities = loanRepository.findRecentActivityByUser(user, PageRequest.of(0, 5)).stream()
                    .map(l -> new DashboardActivityDTO(l.getStatus().toString(),
                            l.getBook().getTitle(), "You", l.getBorrowDate()))
                    .collect(Collectors.toList());
        }

        return new StatsResponseDTO(metrics, activities);
    }
}
