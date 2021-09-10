// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventgrid;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.BinaryData;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventgrid.models.EventHubEventSubscriptionDestination;
import com.azure.resourcemanager.eventgrid.models.EventSubscription;
import com.azure.resourcemanager.eventgrid.models.EventSubscriptionFilter;
import com.azure.resourcemanager.eventgrid.models.Topic;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceSkuType;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.OPERATION_TIMEOUT;
import static java.nio.charset.StandardCharsets.UTF_8;

public class EventGridPublishAndConsumeExample {
    private static AzureResourceManager resourceManager;
    private static EventGridManager eventGridManager;

    private static final Random RANDOM = new Random();
    private static final int NUMBER_OF_EVENTS = 10;
    private static final Region REGION = Region.US_WEST2;
    private static final String RESOURCE_GROUP_NAME = "rg" + randomPadding();
    private static final String EVENT_HUB_NAME = "eh" + randomPadding();
    private static final String EVENT_HUB_NAMESPACE = "ehNamespace" + randomPadding();
    private static final String TOPIC_NAME = "myTopicName" + randomPadding();
    private static final String EVENT_SUBSCRIPTION_NAME = "eventSubscription" + randomPadding();
    private static final String EVENT_HUB_RULE_NAME = "myManagementRule" + randomPadding();

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            // 1. Authenticate
            // Authentication environment variables need to be set: AZURE_CLIENT_ID,AZURE_TENANT_ID,AZURE_CLIENT_SECRET,AZURE_SUBSCRIPTION_ID
            TokenCredential credential = new EnvironmentCredentialBuilder()
                .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
                .build();

            AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

            // 2. Create ResourceManager, EventGridManager

            // Create one HttpClient to make it shared by two resource managers.
            HttpClient httpClient = HttpClient.createDefault();

            resourceManager = AzureResourceManager.configure()
                .withHttpClient(httpClient)
                .withLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            eventGridManager = EventGridManager.configure()
                .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .withHttpClient(httpClient)
                .authenticate(credential, profile);

            // 3. Run sample
            runSample();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runSample() {
        try {

            // 1. Create a resource group.
            ResourceGroup resourceGroup =
                resourceManager.resourceGroups().define(RESOURCE_GROUP_NAME).withRegion(REGION).create();

            System.out.println("Resource group created with name " + RESOURCE_GROUP_NAME);

            // 2. Create an event hub.
            // 2.1 Create a event hub namespace.
            EventHubNamespace namespace = resourceManager.eventHubNamespaces()
                .define(EVENT_HUB_NAMESPACE)
                .withRegion(REGION)
                .withExistingResourceGroup(RESOURCE_GROUP_NAME)
                .withAutoScaling()
                .withSku(EventHubNamespaceSkuType.STANDARD)
                .create();

            System.out.println("EventHub namespace created with name " + namespace.name());

            // 2.2 Create event hub.
            EventHub eventHub = resourceManager.eventHubs()
                .define(EVENT_HUB_NAME)
                .withExistingNamespace(RESOURCE_GROUP_NAME, EVENT_HUB_NAMESPACE)
                .withNewManageRule(EVENT_HUB_RULE_NAME)
                .withPartitionCount(1) // Here we create eventhub with 1 partition, so that when we subscribe, we can make sure all the events come from the same partition, and then subscribe to the first partition. It is for sample purpose. In real use case, one can configure multiple partitions
                .create();

            System.out.println("EventHub created with name " + eventHub.name());

            // 3. Create an event grid topic.
            Topic eventGridTopic = eventGridManager.topics()
                .define(TOPIC_NAME)
                .withRegion(REGION)
                .withExistingResourceGroup(RESOURCE_GROUP_NAME)
                .create();

            System.out.println("EventGrid topic created with name " + eventGridTopic.name());

            // 4. Create an event grid subscription.
            EventSubscription eventSubscription = eventGridManager.eventSubscriptions()
                .define(EVENT_SUBSCRIPTION_NAME)
                .withExistingScope(eventGridTopic.id())
                .withDestination(new EventHubEventSubscriptionDestination()
                    .withResourceId(eventHub.id()))
                .withFilter(new EventSubscriptionFilter()
                    .withIsSubjectCaseSensitive(false)
                    .withSubjectBeginsWith("")
                    .withSubjectEndsWith(""))
                .create();

            System.out.println("EventGrid event subscription created with name " + eventSubscription.name());

            // 5. Retrieve the event grid client connection key.
            String eventGridClientConnectionKey = eventGridManager.topics().listSharedAccessKeys(RESOURCE_GROUP_NAME, TOPIC_NAME).key1();

            System.out.format("Found EventGrid client connection key \"%s\" for endpoint \"%s\"\n", eventGridClientConnectionKey, eventGridTopic.endpoint());

            // 6. Create an event grid publisher client.
            EventGridPublisherClient<EventGridEvent> eventGridPublisherClient = new EventGridPublisherClientBuilder()
                .endpoint(eventGridTopic.endpoint())
                .credential(new AzureKeyCredential(eventGridClientConnectionKey))
                .buildEventGridEventPublisherClient();

            System.out.println("Done creating event grid publisher client.");

            // 7. Create an event hub consumer client
            String connectionString = eventHub.listAuthorizationRules().stream().findFirst().get().getKeys().primaryConnectionString();
            System.out.format("Event hub connection string: %s%n", connectionString);

            EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
                .connectionString(connectionString)
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .buildAsyncConsumerClient();

            System.out.println("Done creating event hub consumer client.");

            // 8. Subscribe to coming events from event grid using `EventHubConsumerAsyncClient`.
            // 8.1 Get the first partition and block on it
            String firstPartition = consumer.getPartitionIds().blockFirst(OPERATION_TIMEOUT);

            // This shouldn't happen, but if we are unable to get the partitions within the timeout period.
            if (firstPartition == null) {
                firstPartition = "0";
            }

            // 8.2 Subscribe to the coming events from event grid
            // We use CountDownLatch to wait for the coming events
            CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_EVENTS);

            Disposable subscription = consumer.receiveFromPartition(firstPartition, EventPosition.latest())
                .subscribe(partitionEvent -> {
                    EventData eventData = partitionEvent.getData();
                    String contents = new String(eventData.getBody(), UTF_8);
                    countDownLatch.countDown();

                    System.out.printf("Event received. Event sequence number number: %s. Contents: %s%n", eventData.getSequenceNumber(), contents);
                }, error -> {
                    System.err.println("Error occurred while consuming events: " + error);

                    // Count down until 0, so the main thread does not keep waiting for events.
                    while (countDownLatch.getCount() > 0) {
                        countDownLatch.countDown();
                    }
                }, () -> {
                    System.out.println("Finished reading events.");
                });

            // 9. Publish custom events to the EventGrid.
            // We create events to send to the service and block until the send has completed.
            Flux.range(0, NUMBER_OF_EVENTS)
                .doOnNext(number -> {
                    String body = String.format("Custom Event Number: %s", number);
                    EventGridEvent event = new EventGridEvent("com/example/MyApp", "User.Created.Text", BinaryData.fromObject(body), "0.1");
                    eventGridPublisherClient.sendEvent(event);
                    System.out.format("Done publishing event: %s.%n", body);
                })
                .doOnComplete(() -> System.out.println("Done publishing events using event grid."))
                .blockLast();

            // clean up subscription and consumer resources
            subscription.dispose();
            consumer.close();

            boolean isSuccessful = countDownLatch.await(OPERATION_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            if (!isSuccessful) {
                System.err.printf("Did not complete successfully. There are: %s events left.%n",
                    countDownLatch.getCount());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 10. clean up the resources created above
            resourceManager.resourceGroups().beginDeleteByName(RESOURCE_GROUP_NAME);
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }


}
