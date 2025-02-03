package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.EventRequest;
import com.example.ticketbooking.entity.Event;
import com.example.ticketbooking.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Management")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @Operation(summary = "Create a new event")
    @ApiResponse(responseCode = "200", description = "Event created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid event data")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @GetMapping
    @Operation(summary = "Get all events")
    public ResponseEntity<List<Event>> getAllEvents(
            @RequestParam(required = false, defaultValue = "date") String sortBy) {
        return ResponseEntity.ok(eventService.getAllEvents(sortBy));
    }

    @GetMapping("/{eventId}/availability")
    @Operation(summary = "Get event availability")
    public ResponseEntity<Map<String, Object>> getEventAvailability(@PathVariable Long eventId) {
        Event event = eventService.getEventWithAvailability(eventId);
        int availableSeats = eventService.getAvailableSeats(event);
        return ResponseEntity.ok(Map.of(
                "event", event,
                "availableSeats", availableSeats
        ));
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "400", description = "Cannot delete event with active bookings")
    })
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}