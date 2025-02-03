package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.BookingRequest;
import com.example.ticketbooking.entity.Booking;
import com.example.ticketbooking.entity.Event;
import com.example.ticketbooking.service.BookingService;
import com.example.ticketbooking.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Booking Management")
public class BookingController {
    private final BookingService bookingService;
    private final EventService eventService;

    public BookingController(BookingService bookingService, EventService eventService) {
        this.bookingService = bookingService;
        this.eventService = eventService;
    }

    @PostMapping
    @Operation(summary = "Create a new booking")
    @ApiResponse(responseCode = "200", description = "Booking created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid booking request")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @ApiResponse(responseCode = "409", description = "User already has an active booking")
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequest request) {
        Event event = eventService.getEventWithAvailability(request.getEventId());
        return ResponseEntity.ok(bookingService.createBooking(event, request.getUserId(), request.getSeats()));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking by ID")
    @ApiResponse(responseCode = "200", description = "Booking found")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    public ResponseEntity<Booking> getBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBooking(bookingId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's bookings")
    @ApiResponse(responseCode = "200", description = "User bookings retrieved successfully")
    public ResponseEntity<List<Booking>> getUserBookings(@PathVariable String userId) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId));
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get all active bookings for an event")
    @ApiResponse(responseCode = "200", description = "Event bookings retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Event not found")
    public ResponseEntity<List<Booking>> getEventBookings(@PathVariable Long eventId) {
        return ResponseEntity.ok(bookingService.getEventBookings(eventId));
    }

    @GetMapping("/event/{eventId}/seats")
    @Operation(summary = "Get total booked seats for an event")
    @ApiResponse(responseCode = "200", description = "Total booked seats retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Event not found")
    public ResponseEntity<Map<String, Integer>> getEventBookedSeats(@PathVariable Long eventId) {
        Event event = eventService.getEventWithAvailability(eventId);
        int totalBookedSeats = bookingService.getTotalBookedSeats(event);
        return ResponseEntity.ok(Map.of("totalBookedSeats", totalBookedSeats));
    }

    @GetMapping("/check")
    @Operation(summary = "Check if user has active booking for an event")
    @ApiResponse(responseCode = "200", description = "Booking status checked successfully")
    @ApiResponse(responseCode = "404", description = "Event not found")
    public ResponseEntity<Map<String, Boolean>> checkActiveBooking(
            @RequestParam Long eventId,
            @RequestParam String userId) {
        Event event = eventService.getEventWithAvailability(eventId);
        boolean hasBooking = bookingService.hasActiveBooking(event, userId);
        return ResponseEntity.ok(Map.of("hasActiveBooking", hasBooking));
    }

    @DeleteMapping("/{bookingId}")
    @Operation(summary = "Cancel a booking")
    @ApiResponse(responseCode = "204", description = "Booking cancelled successfully")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}