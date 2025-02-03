package com.example.ticketbooking.service;

import com.example.ticketbooking.entity.Booking;
import com.example.ticketbooking.entity.BookingStatus;
import com.example.ticketbooking.entity.Event;
import com.example.ticketbooking.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
@Transactional(readOnly = true)
public class BookingService {
    private final BookingRepository bookingRepository;
    private final EventService eventService;

    public BookingService(BookingRepository bookingRepository, EventService eventService) {
        this.bookingRepository = bookingRepository;
        this.eventService = eventService;
    }

    @Transactional
    public Booking createBooking(Event event, String userId, int seats) {
        log.info("Attempting to create booking - Event: {}, User: {}, Seats: {}",
                event.getId(), userId, seats);
        // Validate input
        if (userId == null || userId.trim().isEmpty()) {
            log.error("Booking creation failed: Empty user ID");
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (seats <= 0) {
            log.error("Booking creation failed: Invalid number of seats requested: {}", seats);
            throw new IllegalArgumentException("Number of seats must be greater than 0");
        }

        // Validate event date
        if (event.getDate().isBefore(LocalDateTime.now())) {
            log.error("Cannot book tickets for past event: {}, Date: {}", event.getId(), event.getDate());
            throw new IllegalStateException("Cannot book tickets for past events");
        }

        // Validate user doesn't have existing booking
        if (bookingRepository.existsByEventAndUserIdAndStatus(event, userId, BookingStatus.ACTIVE)) {
            log.error("User {} already has an active booking for event {}", userId, event.getId());
            throw new IllegalStateException("User already has an active booking for this event");
        }

        // Check seat availability
        int availableSeats = eventService.getAvailableSeats(event);
        log.debug("Available seats for event {}: {}", event.getId(), availableSeats);
        if (seats > availableSeats) {
            log.error("Insufficient seats available. Event: {}, Requested: {}, Available: {}",
                    event.getId(), seats, availableSeats);
            throw new IllegalStateException(String.format(
                    "Not enough seats available. Requested: %d, Available: %d",
                    seats, availableSeats));
        }

        // Create and save booking
        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setUserId(userId);
        booking.setSeatsBooked(seats);
        booking.setStatus(BookingStatus.ACTIVE);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Successfully created booking {} for event {} - User: {}, Seats: {}",
                savedBooking.getBookingId(), event.getId(), userId, seats);
        return savedBooking;
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found with ID: {}", bookingId);
                    return new EntityNotFoundException("Booking not found with id: " + bookingId);
                });
        // Validate if booking can be cancelled
        if (booking.getStatus() == BookingStatus.CANCELED) {
            log.error("Cannot cancel booking {}: already canceled", bookingId);
            throw new IllegalStateException("Booking is already canceled");
        }

        if (booking.getEvent().getDate().isBefore(LocalDateTime.now())) {
            log.error("Cannot cancel booking {} for past event. Event date: {}",
                    bookingId, booking.getEvent().getDate());
            throw new IllegalStateException("Cannot cancel bookings for past events");
        }

        booking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(String userId) {
        log.info("Fetching bookings for user: {}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            log.error("Attempted to fetch bookings with empty user ID");
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        return bookingRepository.findByUserId(userId);
    }

    public Booking getBooking(Long bookingId) {
        log.info("Fetching booking: {}", bookingId);
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
    }

    public List<Booking> getEventBookings(Long eventId) {
        Event event = eventService.getEventWithAvailability(eventId);
        List<Booking> bookings = bookingRepository.findByEventAndStatus(event, BookingStatus.ACTIVE);

        log.info("Found {} active bookings for event {}", bookings.size(), eventId);
        return bookings;    }

    public boolean hasActiveBooking(Event event, String userId) {
        log.info("Checking active booking - Event: {}, User: {}", event.getId(), userId);
        boolean hasBooking = bookingRepository.existsByEventAndUserIdAndStatus(event, userId, BookingStatus.ACTIVE);
        log.debug("User {} {} an active booking for event {}",
                userId, hasBooking ? "has" : "does not have", event.getId());

        return hasBooking;    }

    public int getTotalBookedSeats(Event event) {
        int bookedSeats = bookingRepository.sumSeatsBookedByEvent(event);
        log.info("Event {} has {} seats booked", event.getId(), bookedSeats);
        return bookedSeats;    }
}