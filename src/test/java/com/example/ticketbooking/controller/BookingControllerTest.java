package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.BookingRequest;
import com.example.ticketbooking.entity.Booking;
import com.example.ticketbooking.entity.BookingStatus;
import com.example.ticketbooking.entity.Event;
import com.example.ticketbooking.service.BookingService;
import com.example.ticketbooking.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    private Event testEvent;
    private Booking testBooking;
    private BookingRequest bookingRequest;

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

        bookingRequest = new BookingRequest();
        bookingRequest.setEventId(1L);
        bookingRequest.setUserId("user123");
        bookingRequest.setSeats(2);
    }

    @Test
    void createBooking_Success() throws Exception {
        given(eventService.getEventWithAvailability(anyLong())).willReturn(testEvent);
        given(bookingService.createBooking(any(Event.class), anyString(), anyInt())).willReturn(testBooking);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(testBooking.getBookingId()))
                .andExpect(jsonPath("$.userId").value(testBooking.getUserId()));
    }

    @Test
    void getBooking_Success() throws Exception {
        given(bookingService.getBooking(anyLong())).willReturn(testBooking);

        mockMvc.perform(get("/api/bookings/{bookingId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(testBooking.getBookingId()));
    }

    @Test
    void getUserBookings_Success() throws Exception {
        List<Booking> bookings = Arrays.asList(testBooking);
        given(bookingService.getUserBookings(anyString())).willReturn(bookings);

        mockMvc.perform(get("/api/bookings/user/{userId}", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value(testBooking.getBookingId()));
    }

    @Test
    void getEventBookings_Success() throws Exception {
        List<Booking> bookings = Arrays.asList(testBooking);
        given(bookingService.getEventBookings(anyLong())).willReturn(bookings);

        mockMvc.perform(get("/api/bookings/event/{eventId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value(testBooking.getBookingId()));
    }

    @Test
    void checkActiveBooking_Success() throws Exception {
        given(eventService.getEventWithAvailability(anyLong())).willReturn(testEvent);
        given(bookingService.hasActiveBooking(any(Event.class), anyString())).willReturn(true);

        mockMvc.perform(get("/api/bookings/check")
                        .param("eventId", "1")
                        .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasActiveBooking").value(true));
    }

    @Test
    void cancelBooking_Success() throws Exception {
        doNothing().when(bookingService).cancelBooking(anyLong());

        mockMvc.perform(delete("/api/bookings/{bookingId}", 1L))
                .andExpect(status().isNoContent());
    }
}
