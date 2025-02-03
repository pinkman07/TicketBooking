package com.example.ticketbooking.service;


import com.example.ticketbooking.entity.Booking;
import com.example.ticketbooking.entity.BookingStatus;
import com.example.ticketbooking.entity.Event;
import com.example.ticketbooking.repository.BookingRepository;
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
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventService eventService;

    @InjectMocks
    private BookingService bookingService;

    private Event testEvent;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setName("Test Event");
        testEvent.setDate(LocalDateTime.now().plusDays(10));
        testEvent.setLocation("Test Location");
        testEvent.setTotalSeats(100);

        testBooking = new Booking();
        testBooking.setBookingId(1L);
        testBooking.setEvent(testEvent);
        testBooking.setUserId("user123");
        testBooking.setSeatsBooked(2);
        testBooking.setStatus(BookingStatus.ACTIVE);
    }

    @Test
    void createBooking_Success() {
        when(eventService.getAvailableSeats(any(Event.class))).thenReturn(98);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingRepository.existsByEventAndUserIdAndStatus(any(), anyString(), any())).thenReturn(false);

        Booking result = bookingService.createBooking(testEvent, "user123", 2);

        assertNotNull(result);
        assertEquals("user123", result.getUserId());
        assertEquals(2, result.getSeatsBooked());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_PastEvent() {
        testEvent.setDate(LocalDateTime.now().minusDays(1));

        assertThrows(IllegalStateException.class, () ->
                bookingService.createBooking(testEvent, "user123", 2));
    }

    @Test
    void createBooking_NotEnoughSeats() {
        when(eventService.getAvailableSeats(any(Event.class))).thenReturn(1);

        assertThrows(IllegalStateException.class, () ->
                bookingService.createBooking(testEvent, "user123", 2));
    }

    @Test
    void cancelBooking_Success() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

        bookingService.cancelBooking(1L);

        assertEquals(BookingStatus.CANCELED, testBooking.getStatus());
        verify(bookingRepository).save(testBooking);
    }

    @Test
    void getUserBookings_Success() {
        when(bookingRepository.findByUserId(anyString())).thenReturn(Arrays.asList(testBooking));

        List<Booking> results = bookingService.getUserBookings("user123");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("user123", results.get(0).getUserId());
    }

    @Test
    void getTotalBookedSeats_Success() {
        when(bookingRepository.sumSeatsBookedByEvent(any(Event.class))).thenReturn(2);

        int result = bookingService.getTotalBookedSeats(testEvent);

        assertEquals(2, result);
    }
}
