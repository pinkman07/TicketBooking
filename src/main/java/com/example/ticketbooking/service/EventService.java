package com.example.ticketbooking.service;

import com.example.ticketbooking.dto.EventRequest;
import com.example.ticketbooking.entity.Event;
import com.example.ticketbooking.repository.BookingRepository;
import com.example.ticketbooking.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public EventService(EventRepository eventRepository, BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Event createEvent(EventRequest request) {
        // Validate date is not in the past
        if (request.getDate().isBefore(LocalDateTime.now())) {
            log.error("Attempted to create event with past date: {}", request.getDate());
            throw new IllegalArgumentException("Event date cannot be in the past");
        }

        Event event = new Event();
        event.setName(request.getName());
        event.setDate(request.getDate());
        event.setLocation(request.getLocation());
        event.setTotalSeats(request.getTotalSeats());

        Event savedEvent = eventRepository.save(event);
        log.info("Successfully created event with ID: {}", savedEvent.getId());
        return savedEvent;
    }

    public List<Event> getAllEvents(String sortBy) {
        if (sortBy == null) {
            //default sort
            sortBy = "date";
        }

        // Validate sort parameter
        if (!sortBy.equals("date") && !sortBy.equals("location") && !sortBy.equals("availability")) {
            log.error("Invalid sort parameter provided: {}", sortBy);
            throw new IllegalArgumentException("Invalid sort parameter. Must be one of: date, location, availability");
        }

        return eventRepository.findAllSorted(sortBy);
    }

    public Event getEventWithAvailability(Long eventId) {
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
    }

    public int getAvailableSeats(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        int bookedSeats = bookingRepository.sumSeatsBookedByEvent(event);
        int availableSeats = event.getTotalSeats() - bookedSeats;
        log.info("Event {} has {} seats available (total: {}, booked: {})",
                event.getId(), availableSeats, event.getTotalSeats(), bookedSeats);
        return availableSeats;
    }

    @Transactional
    public Event updateEvent(Long eventId, EventRequest request) {
        log.info("Updating event {} with new details - name: {}, date: {}, location: {}, seats: {}",
                eventId, request.getName(), request.getDate(), request.getLocation(), request.getTotalSeats());

        Event event = getEventWithAvailability(eventId);

        // Validate date is not in the past
        if (request.getDate().isBefore(LocalDateTime.now())) {
            log.error("Attempted to update event {} with past date: {}", eventId, request.getDate());
            throw new IllegalArgumentException("Event date cannot be in the past");
        }

        // Check if reducing seats would conflict with existing bookings
        if (request.getTotalSeats() < event.getTotalSeats()) {
            int currentlyBooked = bookingRepository.sumSeatsBookedByEvent(event);
            if (request.getTotalSeats() < currentlyBooked) {
                log.error("Cannot reduce seats for event {} below current bookings. Requested: {}, Currently booked: {}",
                        eventId, request.getTotalSeats(), currentlyBooked);
                throw new IllegalStateException(
                        String.format("Cannot reduce total seats below current bookings. Current bookings: %d", currentlyBooked)
                );
            }
        }

        event.setName(request.getName());
        event.setDate(request.getDate());
        event.setLocation(request.getLocation());
        event.setTotalSeats(request.getTotalSeats());

        Event updatedEvent = eventRepository.save(event);
        log.info("Successfully updated event: {}", eventId);
        return updatedEvent;
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = getEventWithAvailability(eventId);

        // Check if there are any active bookings
        int activeBookings = bookingRepository.sumSeatsBookedByEvent(event);
        if (activeBookings > 0) {
            log.error("Cannot delete event {} - has {} active bookings", eventId, activeBookings);

            throw new IllegalStateException(
                    String.format("Cannot delete event with active bookings. Current bookings: %d", activeBookings)
            );
        }

        eventRepository.delete(event);
        log.info("Successfully deleted event: {}", eventId);

    }
}