package com.microsoft.azure.eventgrid;

import com.microsoft.azure.eventgrid.implementation.EventGridClientImpl;
import com.microsoft.azure.eventgrid.models.EventGridEvent;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EventGridTests {
    @Test
    public void canPublishEvent() throws Exception {
        String endpoint = System.getenv("EG_ENDPOINT");
        String key = System.getenv("EG_KEY");

        TopicCredentials topicCredentials = new TopicCredentials(key);
        EventGridClient client = new EventGridClientImpl(topicCredentials);
        client.publishEvents(endpoint, getEventsList());
    }

    private List<EventGridEvent> getEventsList() {
        List<EventGridEvent> eventsList = new ArrayList<>();

        for (int i = 0; i < 10; i++)
        {
            eventsList.add(new EventGridEvent()
                    .withId(UUID.randomUUID().toString())
                    .withData(new HashMap<String, String>() {{
                        put("Field1", "Value1");
                        put("Field2", "Value2");
                        put("Field3", "Value3");
                    }})
                    .withEventTime(DateTime.now())
                    .withEventType("Microsoft.MockPublisher.TestEvent")
                    .withSubject("TestSubject")
                    .withDataVersion("1.0"));
        }
        return eventsList;
    }
}
