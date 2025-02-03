package com.example.ticketbooking.repository;

import com.example.ticketbooking.entity.Booking;
import com.example.ticketbooking.entity.BookingStatus;
import com.example.ticketbooking.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {


    @Query("SELECT COALESCE(SUM(b.seatsBooked), 0) FROM Booking b WHERE b.event = :event AND b.status = 'ACTIVE'")
    int sumSeatsBookedByEvent(@Param("event") Event event);

    boolean existsByEventAndUserIdAndStatus(Event event, String userId, BookingStatus status);

    List<Booking> findByUserId(String userId);

    @Query("SELECT b FROM Booking b WHERE b.event = :event AND b.status = :status")
    List<Booking> findByEventAndStatus(@Param("event") Event event, @Param("status") BookingStatus status);
}
