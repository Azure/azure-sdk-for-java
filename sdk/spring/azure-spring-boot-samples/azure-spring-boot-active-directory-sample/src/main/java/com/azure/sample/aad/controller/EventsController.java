package com.azure.sample.aad.controller;

import com.azure.sample.aad.model.Event;
import com.azure.sample.aad.model.Events;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@RequestMapping(path = { "/events" })
public class EventsController {

    @Autowired
    private Events events;

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @GetMapping
    public ResponseEntity<Events> getEvents() {
        return ResponseEntity.ok(this.events);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public void addEvents() {
        String location = "Asia/Shanghai";
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of(location)).plusDays(1);
        this.events.put(UUID.randomUUID().toString(), new Event("meeting", time, location));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(path = { "/join/{eventId}" })
    public void join(@PathVariable String eventId, HttpServletRequest request) {
        Event event = this.events.get(eventId);
        Principal principal = request.getUserPrincipal();
        System.out.println(principal);
        // add the event into outlook (office scope).
    }
}
