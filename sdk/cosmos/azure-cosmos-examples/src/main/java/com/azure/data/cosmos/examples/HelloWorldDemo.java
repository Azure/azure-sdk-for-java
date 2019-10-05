// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.examples;

import com.azure.data.cosmos.*;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class HelloWorldDemo {
    public static void main(String[] args) {
        new HelloWorldDemo().runDemo();
    }

    void runDemo() {
        // Create a new CosmosAsyncClient via the builder
        // It only requires endpoint and key, but other useful settings are available
        CosmosAsyncClient client = CosmosAsyncClient.builder()
            .setEndpoint("<YOUR ENDPOINT HERE>")
            .setKey("<YOUR KEY HERE>")
            .buildAsyncClient();

        // Get a reference to the container
        // This will create (or read) a database and its container.
        CosmosAsyncContainer container = client.createDatabaseIfNotExists("contoso-travel")
            // TIP: Our APIs are Reactor Core based, so try to chain your calls
            .flatMap(response -> response.getDatabase()
                    .createContainerIfNotExists("passengers", "/id"))
            .flatMap(response -> Mono.just(response.getContainer()))
            .block(); // Blocking for demo purposes (avoid doing this in production unless you must)

        // Create an item
        container.createItem(new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND"))
            .flatMap(response -> {
                System.out.println("Created item: " + response.getProperties().toJson());
                // Read that item 👓
                return response.getItem().read();
            })
            .flatMap(response -> {
                System.out.println("Read item: " + response.getProperties().toJson());
                // Replace that item 🔁
                try {
                    Passenger p = response.getProperties().getObject(Passenger.class);
                    p.setDestination("SFO");
                    return response.getItem().replace(p);
                } catch (IOException e) {
                    System.err.println(e);
                    return Mono.error(e);
                }
            })
            // delete that item 💣
            .flatMap(response -> response.getItem().delete())
            .block(); // Blocking for demo purposes (avoid doing this in production unless you must)
    }

    // Just a random object for demo's sake
    public class Passenger {
        String id;
        String name;
        String destination;
        String source;

        public Passenger(String id, String name, String destination, String source) {
            this.id = id;
            this.name = name;
            this.destination = destination;
            this.source = source;
        }
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }
}
