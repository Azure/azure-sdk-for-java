// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.examples;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.PartitionKey;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class HelloWorldDemo {
    public static void main(String[] args) {
        new HelloWorldDemo().runDemo();
    }

    void runDemo() {
        // Create a new CosmosAsyncClient via the CosmosClientBuilder
        // It only requires endpoint and key, but other useful settings are available
        CosmosAsyncClient client = new CosmosClientBuilder()
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
                System.out.println("Created item: " + response.getResource());
                // Read that item 👓
                return container.readItem(response.getResource().getId(),
                                          new PartitionKey(response.getResource().getId()),
                                          Passenger.class);
            })
            .flatMap(response -> {
                System.out.println("Read item: " + response.getResource());
                // Replace that item 🔁
                Passenger p = response.getResource();
                p.setDestination("SFO");
                return container.replaceItem(p,
                                             response.getResource().getId(),
                                             new PartitionKey(response.getResource().getId()));
            })
            // delete that item 💣
            .flatMap(response -> container.deleteItem(response.getResource().getId(),
                                                      new PartitionKey(response.getResource().getId())))
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
