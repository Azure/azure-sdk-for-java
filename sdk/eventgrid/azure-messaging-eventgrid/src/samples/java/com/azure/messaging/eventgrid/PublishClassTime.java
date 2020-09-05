// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.serializer.JacksonAdapter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This Sample is an example of using a custom serializer to send custom event data to an EventGrid topic.
 *
 * To run this sample, first create an EventGrid topic and store the key and endpoint as system environment variables.
 * See the README in the library folder for more help on getting started with EventGrid.
 *
 * Run the main method with no arguments to start publishing! By the end, you should have published 50 batches of
 * events to your topic, with each batch containing a number of individual events.
 */
public class PublishClassTime {

    private static final int REPEATS = 50;

    public static void main(String[] args) throws InterruptedException {
        publishEvents();
    }

    public static void publishEvents() throws InterruptedException {
        String key = System.getenv("TOPIC_KEY");
        String endpoint = System.getenv("TOPIC_ENDPOINT");

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
            .credential(new AzureKeyCredential(key))
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
