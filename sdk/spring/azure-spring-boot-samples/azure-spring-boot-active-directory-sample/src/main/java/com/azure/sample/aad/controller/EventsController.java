package com.azure.sample.aad.controller;

import com.azure.sample.aad.model.Event;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping(path = { "/events" }, produces = { MediaType.APPLICATION_JSON_VALUE })
public class EventsController {

    private final HashMap<String, Event> events = new HashMap<>();

    public EventsController() {
        this.addEvents();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @GetMapping
    public ResponseEntity<?> getEvents() {
        return ResponseEntity.ok(this.events);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public void addEvents() {
        String location = "Asia/Shanghai";
        LocalDateTime time = LocalDateTime.now(ZoneId.of(location)).plusDays(1);
        this.events.put(UUID.randomUUID().toString(), new Event("meeting", time, location));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(path = { "/join/{eventId}" })
    public void join(@PathVariable String eventId, HttpServletRequest request) {
        Event event = this.events.get(eventId);
        Principal principal = request.getUserPrincipal();
        // add the event into outlook (office scope).
    }

    @GetMapping(path = { "/test" })
    public void test(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        System.out.println(principal);
    }
}
