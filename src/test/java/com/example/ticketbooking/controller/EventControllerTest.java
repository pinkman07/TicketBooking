package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.EventRequest;
import com.example.ticketbooking.entity.Event;
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

@WebMvcTest(EventController.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void createEvent_Success() throws Exception {
        given(eventService.createEvent(any(EventRequest.class))).willReturn(testEvent);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEvent.getId()))
                .andExpect(jsonPath("$.name").value(testEvent.getName()));
    }

    @Test
    void getAllEvents_Success() throws Exception {
        List<Event> events = Arrays.asList(testEvent);
        given(eventService.getAllEvents(anyString())).willReturn(events);

        mockMvc.perform(get("/api/events")
                        .param("sortBy", "date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testEvent.getId()));
    }

    @Test
    void getEventAvailability_Success() throws Exception {
        given(eventService.getEventWithAvailability(anyLong())).willReturn(testEvent);
        given(eventService.getAvailableSeats(any(Event.class))).willReturn(50);

        mockMvc.perform(get("/api/events/{eventId}/availability", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.id").value(testEvent.getId()))
                .andExpect(jsonPath("$.availableSeats").value(50));
    }

    @Test
    void deleteEvent_Success() throws Exception {
        doNothing().when(eventService).deleteEvent(anyLong());

        mockMvc.perform(delete("/api/events/{eventId}", 1L))
                .andExpect(status().isNoContent());
    }

    // Error scenario tests
    @Test
    void createEvent_InvalidData() throws Exception {
        eventRequest.setName(""); // Invalid name

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isBadRequest());
    }

}