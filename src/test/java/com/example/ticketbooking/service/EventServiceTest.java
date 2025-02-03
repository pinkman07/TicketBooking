package com.example.ticketbooking.service;


import com.example.ticketbooking.dto.EventRequest;
import com.example.ticketbooking.entity.Event;
import com.example.ticketbooking.repository.BookingRepository;
import com.example.ticketbooking.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;
    private EventRequest eventRequest;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setName("Test Event");
        testEvent.setDate(LocalDateTime.now().plusDays(10));
        testEvent.setLocation("Test Location");
        testEvent.setTotalSeats(100);

        eventRequest = new EventRequest();
        eventRequest.setName("Test Event");
        eventRequest.setDate(LocalDateTime.now().plusDays(10));
        eventRequest.setLocation("Test Location");
        eventRequest.setTotalSeats(100);
    }

    @Test
    void createEvent_Success() {
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event result = eventService.createEvent(eventRequest);

        assertNotNull(result);
        assertEquals("Test Event", result.getName());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEvent_PastDate() {
        eventRequest.setDate(LocalDateTime.now().minusDays(1));

        assertThrows(IllegalArgumentException.class, () ->
                eventService.createEvent(eventRequest));
    }

    @Test
    void getAllEvents_Success() {
        when(eventRepository.findAllSorted(anyString())).thenReturn(Arrays.asList(testEvent));

        List<Event> results = eventService.getAllEvents("date");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("Test Event", results.get(0).getName());
    }

    @Test
    void deleteEvent_Success() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));
        when(bookingRepository.sumSeatsBookedByEvent(any(Event.class))).thenReturn(0);

        eventService.deleteEvent(1L);

        verify(eventRepository).delete(testEvent);
    }

    @Test
    void deleteEvent_WithActiveBookings() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));
        when(bookingRepository.sumSeatsBookedByEvent(any(Event.class))).thenReturn(5);

        assertThrows(IllegalStateException.class, () ->
                eventService.deleteEvent(1L));
    }

    @Test
    void updateEvent_Success() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event result = eventService.updateEvent(1L, eventRequest);

        assertNotNull(result);
        assertEquals("Test Event", result.getName());
        verify(eventRepository).save(any(Event.class));
    }
}
