package com.example.carsharingservice.repository;

import com.example.carsharingservice.model.Rental;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    @Query(
            "SELECT r "
                    + "FROM Rental r "
                    + "JOIN FETCH r.car "
                    + "WHERE r.user.id = :userId AND r.isActive = :active"
    )
    List<Rental> findByUserIdAndActiveIs(
            @Param("userId")Long userId,
            @Param("active") boolean active);

    @Query(
            "SELECT r "
            + "FROM Rental r "
            + "WHERE r.returnDate <= :date AND r.isActive = true"
    )
    List<Rental> findOverdueRentals(@Param("date") LocalDate returnDateIsLessThan);
}
