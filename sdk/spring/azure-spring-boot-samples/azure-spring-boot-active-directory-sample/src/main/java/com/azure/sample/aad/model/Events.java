package com.azure.sample.aad.model;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class Events extends ConcurrentHashMap<String, Event> {
}
