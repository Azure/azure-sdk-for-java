package com.azure.messaging;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.messaging.eventgrid.CloudEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PublishClassTime {

    public static final int REPEATS = 100;

    @Disabled("Sample code, requires system environment variables for endpoint and key")
    @Test
    public void publishEvents() throws InterruptedException {
        String key = System.getenv("DEMO_KEY");
        String endpoint = System.getenv("DEMO_ENDPOINT");

        JacksonAdapter customSerializer = new JacksonAdapter();

        customSerializer.serializer().registerModule(new SimpleModule().addSerializer(ClassTime.class,
            new JsonSerializer<ClassTime>() {

                @Override
                public void serialize(ClassTime classTime, JsonGenerator jsonGenerator,
                                      SerializerProvider serializerProvider) throws IOException {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField("department", classTime.getDepartment());
                    jsonGenerator.writeNumberField("courseNumber", classTime.getCourseNumber());
                    jsonGenerator.writeStringField("startTime", classTime.getStartTime().toString());
                    jsonGenerator.writeEndObject();
                }
            }));

        // EG client
        EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
            .keyCredential(new AzureKeyCredential(key))
            .endpoint(endpoint)
            .serializer(customSerializer)
            .buildClient();

        Random random = new Random();

        for (int i = 0; i < REPEATS; i++) {

            publish(egClient, random);
            Thread.sleep(1000);
        }


    }


    public static void publish(EventGridPublisherClient egClient, Random random) {
        List<CloudEvent> events = new ArrayList<>();
        int times = random.nextInt(4) + 1;
        for (int i = 0; i < times; i++) {
            ClassTime classTime = ClassTime.getRandom(random);
            System.out.println(classTime);

            events.add(new CloudEvent("/microsoft/demo", "Microsoft.Demo.ClassTime")
                .setData(classTime));
        }
        System.out.println();


        egClient.sendCloudEvents(events);
    }
}
