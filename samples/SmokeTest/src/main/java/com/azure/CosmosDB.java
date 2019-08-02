package com.azure;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.FeedOptions;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class CosmosDB {
    private static CosmosClient client;
    private static CosmosContainer container;
    private static final String dbName = "JavaSolarSystem";
    private static final String collectionName = "Planets";

    private static void createDatabase() {
        System.out.print("Creating database... ");
        client.createDatabaseIfNotExists(dbName).block();
        System.out.println("\tDONE.");
    }

    private static void createCollection() {
        System.out.print("Creating Collection... ");
        client.getDatabase(dbName).createContainer(collectionName, "/id").block();
        container = client.getDatabase(dbName).getContainer(collectionName);
        System.out.println("\tDONE.");
    }

    private static void createDocuments() {
        System.out.println("Inserting Items... ");
        Planet[] planets = new Planet[]{
            new Planet(
                "Earth",
                false,
                3959,
                new Moon[]{
                    new Moon("Moon")
                }),
            new Planet(
                "Mars",
                false,
                2106,
                new Moon[]{
                    new Moon("Phobos"),
                    new Moon("Deimos")
                })
        };

        for (Planet planet : planets) {
            System.out.println("\tInserting '" + planet.id + "'...");
            container.createItem(planet).block();
        }

        System.out.println("DONE.");
    }

    private static void simpleQuery() {
        System.out.println("Querying collection...");
        FeedOptions options = new FeedOptions().enableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosItemProperties>> queryResults = container.queryItems("SELECT c.id FROM c", options);

        queryResults.publishOn(Schedulers.elastic()).toIterable().forEach(cosmosItemPropertiesFeedResponse -> {
            System.out.println('\t' + cosmosItemPropertiesFeedResponse.results().toString());
        });
        System.out.println("\tDONE.");
    }

    private static void deleteDatabase() {
        System.out.print("Cleaning up the resource...");
        client.getDatabase(dbName).delete().block();
        System.out.println("\tDONE.");
    }

    public static void main(String[] args) {
        System.out.println("\n---------------------");
        System.out.println("COSMOS DB");
        System.out.println("---------------------\n");

        client = CosmosClient
            .builder()
            .endpoint(System.getenv("COSMOS_ENDPOINT"))
            .key(System.getenv("COSMOS_KEY"))
            .build();

        try {
            //if the database already exists, it is going to be deleted with all its content.
            deleteDatabase();
        } catch (Exception e) {
            //This means that the database does not exists already, it's fine
        }

        try {
            createDatabase();
            createCollection();
            createDocuments();
            simpleQuery();
        } finally {
            deleteDatabase();
            client.close();
        }
    }
}

// Classes for this sample
class Planet {
    public String id;
    public boolean hasRings;
    public int radious;
    public Moon[] moons;

    public Planet(String id, boolean hasRings, int radius, Moon[] moons) {
        this.id = id;
        this.hasRings = hasRings;
        this.radious = radius;
        this.moons = moons;
    }
}

class Moon {
    public String name;

    public Moon(String name) {
        this.name = name;
    }

}
