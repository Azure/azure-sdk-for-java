// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.azure.cosmos.ReadmeSamples.*;

public class AsyncContainerCodeSnippets {
    private final CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
        .endpoint("<YOUR ENDPOINT HERE>")
        .key("<YOUR KEY HERE>")
        .buildAsyncClient();

    private final CosmosClient cosmosClient = new CosmosClientBuilder()
        .endpoint("<YOUR ENDPOINT HERE>")
        .key("<YOUR KEY HERE>")
        .buildClient();

    private final CosmosAsyncDatabase cosmosAsyncDatabase = cosmosAsyncClient
        .getDatabase("<YOUR DATABASE NAME>");
    private final CosmosAsyncContainer cosmosAsyncContainer = cosmosAsyncDatabase
        .getContainer("<YOUR CONTAINER NAME>");

    private final CosmosContainer cosmosContainer = cosmosClient
        .getDatabase("<YOUR DATABASE NAME>")
        .getContainer("<YOUR CONTAINER NAME>");

    public void getFeedRangesAsyncSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.getFeedRanges
        cosmosAsyncContainer.getFeedRanges()
            .subscribe(feedRanges -> {
                for (FeedRange feedRange : feedRanges) {
                    System.out.println("Feed range: " + feedRange);
                }
            });
        // END: com.azure.cosmos.CosmosAsyncContainer.getFeedRanges
    }

    public void getFeedRangesSample() {
        // BEGIN: com.azure.cosmos.CosmosContainer.getFeedRanges
        List<FeedRange> feedRanges = cosmosContainer.getFeedRanges();
        for (FeedRange feedRange : feedRanges) {
            System.out.println("Feed range: " + feedRange);
        }
        // END: com.azure.cosmos.CosmosContainer.getFeedRanges
    }

    public void readThroughputAsyncSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.readThroughput
        Mono<ThroughputResponse> throughputResponseMono = cosmosAsyncContainer.readThroughput();
        throughputResponseMono.subscribe(throughputResponse -> {
            System.out.println(throughputResponse);
        }, throwable -> {
            throwable.printStackTrace();
        });
        // END: com.azure.cosmos.CosmosAsyncContainer.readThroughput
    }

    public void readThroughputSample() {
        // BEGIN: com.azure.cosmos.CosmosContainer.readThroughput
        try {
            ThroughputResponse throughputResponse = cosmosContainer.readThroughput();
            System.out.println(throughputResponse);
        } catch (CosmosException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // END: com.azure.cosmos.CosmosContainer.readThroughput
    }

    public void replaceThroughputAsyncSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.replaceThroughput
        ThroughputProperties throughputProperties =
            ThroughputProperties.createAutoscaledThroughput(1000);

        cosmosAsyncContainer.replaceThroughput(throughputProperties)
            .subscribe(throughputResponse -> {
                    System.out.println(throughputResponse);
                },
                throwable -> {
                    throwable.printStackTrace();
                });
        // END: com.azure.cosmos.CosmosAsyncContainer.replaceThroughput
    }

    public void replaceThroughputSample() {
        // BEGIN: com.azure.cosmos.CosmosContainer.replaceThroughput
        ThroughputProperties throughputProperties =
            ThroughputProperties.createAutoscaledThroughput(1000);
        try {
            ThroughputResponse throughputResponse =
                cosmosContainer.replaceThroughput(throughputProperties);
            System.out.println(throughputResponse);
        } catch (CosmosException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // END: com.azure.cosmos.CosmosContainer.replaceThroughput
    }

    public void queryConflictsAsyncSample() {
        List<String> conflictIds = Collections.emptyList();
        String query = "SELECT * from c where c.id in (%s)";
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.queryConflicts
        try {
            cosmosAsyncContainer.queryConflicts(query).
                byPage(100)
                .subscribe(response -> {
                    for (CosmosConflictProperties conflictProperties : response.getResults()) {
                        System.out.println(conflictProperties);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
        } catch (CosmosException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // END: com.azure.cosmos.CosmosAsyncContainer.queryConflicts
    }

    public void readAllConflictsAsyncSample() {
        List<String> conflictIds = Collections.emptyList();
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.readAllConflicts
        try {
            cosmosAsyncContainer.readAllConflicts(options).
                byPage(100)
                .subscribe(response -> {
                    for (CosmosConflictProperties conflictProperties : response.getResults()) {
                        System.out.println(conflictProperties);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
        } catch (CosmosException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // END: com.azure.cosmos.CosmosAsyncContainer.readAllConflicts
    }

    public void patchItemAsyncSample() {
        Passenger passenger = new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND");
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.patchItem
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create();

        cosmosPatchOperations
            .add("/departure", "SEA")
            .increment("/trips", 1);

        cosmosAsyncContainer.patchItem(
                passenger.getId(),
                new PartitionKey(passenger.getId()),
                cosmosPatchOperations,
                Passenger.class)
            .subscribe(response -> {
                System.out.println(response);
            }, throwable -> {
                throwable.printStackTrace();
            });
        // END: com.azure.cosmos.CosmosAsyncContainer.patchItem
    }

    public void replaceItemAsyncSample() {
        Passenger oldPassenger = new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND");
        Passenger newPassenger = new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND");
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.replaceItem
        cosmosAsyncContainer.replaceItem(
                newPassenger,
                oldPassenger.getId(),
                new PartitionKey(oldPassenger.getId()),
                new CosmosItemRequestOptions())
            .subscribe(response -> {
                System.out.println(response);
            }, throwable -> {
                throwable.printStackTrace();
            });
        // END: com.azure.cosmos.CosmosAsyncContainer.replaceItem
    }

    public void readAllItemsAsyncSample() {
        String partitionKey = "partitionKey";
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.readAllItems
        cosmosAsyncContainer
            .readAllItems(new PartitionKey(partitionKey), Passenger.class)
            .byPage(100)
            .flatMap(passengerFeedResponse -> {
                for (Passenger passenger : passengerFeedResponse.getResults()) {
                    System.out.println(passenger);
                }
                return Flux.empty();
            })
            .subscribe();
        // END: com.azure.cosmos.CosmosAsyncContainer.readAllItems
    }

    public void readManyAsyncSample() {
        String passenger1Id = "item1";
        String passenger2Id = "item1";

        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.readMany
        List<CosmosItemIdentity> itemIdentityList = new ArrayList<>();
        itemIdentityList.add(new CosmosItemIdentity(new PartitionKey(passenger1Id), passenger1Id));
        itemIdentityList.add(new CosmosItemIdentity(new PartitionKey(passenger2Id), passenger2Id));

        cosmosAsyncContainer.readMany(itemIdentityList, Passenger.class)
            .flatMap(passengerFeedResponse -> {
                for (Passenger passenger : passengerFeedResponse.getResults()) {
                    System.out.println(passenger);
                }
                return Mono.empty();
            })
            .subscribe();
        // END: com.azure.cosmos.CosmosAsyncContainer.readMany
    }

    public void queryChangeFeedSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.queryChangeFeed
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRange.forFullRange())
            .allVersionsAndDeletes();

        cosmosAsyncContainer.queryChangeFeed(options, Passenger.class)
            .byPage()
            .flatMap(passengerFeedResponse -> {
                for (Passenger passenger : passengerFeedResponse.getResults()) {
                    System.out.println(passenger);
                }
                return Flux.empty();
            })
            .subscribe();
        // END: com.azure.cosmos.CosmosAsyncContainer.queryChangeFeed
    }

    public void queryItemsAsyncSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.queryItems
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        String query = "SELECT * FROM Passenger WHERE Passenger.departure IN ('SEA', 'IND')";
        cosmosAsyncContainer.queryItems(query, options, Passenger.class)
            .byPage()
            .flatMap(passengerFeedResponse -> {
                for (Passenger passenger : passengerFeedResponse.getResults()) {
                    System.out.println(passenger);
                }
                return Flux.empty();
            })
            .subscribe();
        // END: com.azure.cosmos.CosmosAsyncContainer.queryItems
    }

    public void queryItemsAsyncSqlQuerySpecSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.SqlQuerySpec.queryItems
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        String query = "SELECT * FROM Passenger p WHERE (p.departure = @departure)";
        List<SqlParameter> parameters = Collections.singletonList(new SqlParameter("@departure", "SEA"));
        SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(query, parameters);

        cosmosAsyncContainer.queryItems(sqlQuerySpec, options, Passenger.class)
            .byPage()
            .flatMap(passengerFeedResponse -> {
                for (Passenger passenger : passengerFeedResponse.getResults()) {
                    System.out.println(passenger);
                }
                return Flux.empty();
            })
            .subscribe();
        // END: com.azure.cosmos.CosmosAsyncContainer.SqlQuerySpec.queryItems
    }

    public void databaseReadAsyncSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.read
        CosmosAsyncDatabase database = cosmosAsyncClient
            .getDatabase("<YOUR DATABASE NAME>");
        database.read().subscribe(databaseResponse -> {
                System.out.println(databaseResponse);
            },
            throwable -> {
                throwable.printStackTrace();
            });
        // END: com.azure.cosmos.CosmosAsyncDatabase.read
    }

    public void databaseDeleteAsyncSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.delete
        CosmosAsyncDatabase database = cosmosAsyncClient
            .getDatabase("<YOUR DATABASE NAME>");
        database.delete().subscribe(databaseResponse -> {
                System.out.println(databaseResponse);
            },
            throwable -> {
                throwable.printStackTrace();
            });
        // END: com.azure.cosmos.CosmosAsyncDatabase.delete
    }

    public void databaseCreateContainerAsyncSample() {
        String containerId = "passengers";
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createContainer
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(containerId, partitionKeyDefinition);
        cosmosAsyncDatabase.createContainer(containerProperties)
            .subscribe(
                cosmosContainerResponse -> System.out.println(cosmosContainerResponse),
                throwable -> System.out.println("Failed to create container: " + throwable)
            );
        // END: com.azure.cosmos.CosmosAsyncDatabase.createContainer
    }

    public void databaseCreateContainerPropsSample() {
        String containerId = "passengers";
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        int autoScaleMaxThroughput = 1000;

        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createContainerProps
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(containerId, partitionKeyDefinition);
        ThroughputProperties throughputProperties =
            ThroughputProperties.createAutoscaledThroughput(autoScaleMaxThroughput);
        cosmosAsyncDatabase.createContainer(containerProperties, throughputProperties)
            .subscribe(
                cosmosContainerResponse -> System.out.println(cosmosContainerResponse),
                throwable -> System.out.println("Failed to create container: " + throwable)
            );
        // END: com.azure.cosmos.CosmosAsyncDatabase.createContainerProps
    }

    public void databaseCreateContainerThroughputSample() {
        String containerId = "passengers";
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        int throughput = 1000;
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createContainerThroughput
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(containerId, partitionKeyDefinition);

        cosmosAsyncDatabase.createContainer(
                containerProperties,
                throughput,
                options
            )
            .subscribe(
                cosmosContainerResponse -> System.out.println(cosmosContainerResponse),
                throwable -> System.out.println("Failed to create container: " + throwable)
            );
        // END: com.azure.cosmos.CosmosAsyncDatabase.createContainerThroughput
    }

    public void databaseCreateContainerPartitionKeyAsyncSample() {
        String containerId = "passengers";
        String partitionKeyPath = "/id";
        int autoscaledThroughput = 1000;

        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createContainerPartitionKey
        ThroughputProperties throughputProperties =
            ThroughputProperties.createAutoscaledThroughput(autoscaledThroughput);
        cosmosAsyncDatabase.createContainer(
                containerId,
                partitionKeyPath,
                throughputProperties
            )
            .subscribe(
                cosmosContainerResponse -> System.out.println(cosmosContainerResponse),
                throwable -> System.out.println("Failed to create container: " + throwable)
            );
        // END: com.azure.cosmos.CosmosAsyncDatabase.createContainerPartitionKey
    }

    public void databaseCreateContainerIfNotExistsAsyncSampleThroughput() {
        String containerId = "passengers";
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        int autoScaleMaxThroughput = 1000;
        ThroughputProperties throughputProperties =
            ThroughputProperties.createAutoscaledThroughput(autoScaleMaxThroughput);

        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExistsThroughput
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(containerId, partitionKeyDefinition);
        cosmosAsyncDatabase.createContainerIfNotExists(containerProperties, throughputProperties)
            .subscribe(
                cosmosContainerResponse -> System.out.println(cosmosContainerResponse),
                throwable -> System.out.println("Failed to create container: " + throwable)
            );
        // END: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExistsThroughput
    }

    public void databaseCreateContainerIfNotExistsAsyncSample() {
        String containerId = "passengers";
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(containerId, partitionKeyDefinition);
        cosmosAsyncDatabase.createContainerIfNotExists(containerProperties)
            .subscribe(
                cosmosContainerResponse -> System.out.println(cosmosContainerResponse),
                throwable -> System.out.println("Failed to create container: " + throwable)
            );
        // END: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists
    }

    public void readAllContainersSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.readAllContainers
        cosmosAsyncDatabase.readAllContainers()
            .byPage()
            .flatMap(containerPropertiesFeedResponse -> {
                for (CosmosContainerProperties properties : containerPropertiesFeedResponse.getResults()) {
                    System.out.println(properties);
                }
                return Flux.empty();
            })
            .subscribe();
        // END: com.azure.cosmos.CosmosAsyncDatabase.readAllContainers
    }

    public void queryContainersSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.queryContainers
        cosmosAsyncDatabase.queryContainers("SELECT * FROM DB_NAME")
            .byPage()
            .flatMap(containerPropertiesFeedResponse -> {
                for (CosmosContainerProperties properties : containerPropertiesFeedResponse.getResults()) {
                    System.out.println(properties);
                }
                return Flux.empty();
            })
            .subscribe();
        // END: com.azure.cosmos.CosmosAsyncDatabase.queryContainers
    }

    public void createUserAsyncSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createUser
        String userId = "userId";
        CosmosUserProperties userProperties = new CosmosUserProperties();
        userProperties.setId(userId);
        cosmosAsyncDatabase.createUser(userProperties)
            .subscribe(
                userResponse -> System.out.println(userResponse),
                throwable -> System.out.println("Failed to create user: " + throwable)
            );
        // END: com.azure.cosmos.CosmosAsyncDatabase.createUser
    }

    public void upsertUserAsyncSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.upsertUser
        String userId = "userId";
        CosmosUserProperties userProperties = new CosmosUserProperties();
        userProperties.setId(userId);
        cosmosAsyncDatabase.upsertUser(userProperties)
            .subscribe(
                userResponse -> System.out.println(userResponse),
                throwable -> System.out.println("Failed to upsert user: " + throwable)
            );
        // END: com.azure.cosmos.CosmosAsyncDatabase.upsertUser
    }

    public void readAllUsersSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.readAllUsers
        cosmosAsyncDatabase.readAllUsers()
            .byPage()
            .flatMap(userPropertiesFeedResponse -> {
                for (CosmosUserProperties properties : userPropertiesFeedResponse.getResults()) {
                    System.out.println(properties);
                }
                return Flux.empty();
            })
            .subscribe();
        // END: com.azure.cosmos.CosmosAsyncDatabase.readAllUsers
    }

    public void databaseReplaceThroughputAsyncSample() {
        int autoScaleMaxThroughput = 1000;
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.replaceThroughput
        ThroughputProperties autoscaledThroughput = ThroughputProperties
            .createAutoscaledThroughput(autoScaleMaxThroughput);
        cosmosAsyncDatabase.replaceThroughput(autoscaledThroughput)
            .subscribe(throughputResponse -> {
                    System.out.println(throughputResponse);
                },
                throwable -> {
                    throwable.printStackTrace();
                });
        // END: com.azure.cosmos.CosmosAsyncDatabase.replaceThroughput
    }
    public void databaseReadThroughputAsyncSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.readThroughput
        cosmosAsyncDatabase.readThroughput()
            .subscribe(throughputResponse -> {
                    System.out.println(throughputResponse);
                },
                throwable -> {
                    throwable.printStackTrace();
                });
        // END: com.azure.cosmos.CosmosAsyncDatabase.readThroughput
    }

}
