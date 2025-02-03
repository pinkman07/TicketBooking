package com.example.ticketbooking.repository;

import com.example.ticketbooking.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e LEFT JOIN e.bookings b " +
            "GROUP BY e " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'availability' THEN (e.totalSeats - COALESCE(SUM(CASE WHEN b.status = 'ACTIVE' THEN b.seatsBooked ELSE 0 END), 0)) END DESC, " +
            "CASE WHEN :sortBy = 'date' THEN e.date END ASC, " +
            "CASE WHEN :sortBy = 'location' THEN e.location END ASC, " +
            "e.id ASC")
    List<Event> findAllSorted(@Param("sortBy") String sortBy);



}
