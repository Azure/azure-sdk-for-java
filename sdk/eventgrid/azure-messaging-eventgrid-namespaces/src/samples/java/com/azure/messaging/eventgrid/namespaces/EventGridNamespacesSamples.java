// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.namespaces;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventgrid.namespaces.models.ReceiveResult;
import com.azure.messaging.eventgrid.namespaces.models.User;

import java.time.Duration;
import java.util.Arrays;

public class EventGridNamespacesSamples {
    final String endpoint = "endpoint";
    final String topic = "topic";
    final String namespace = "namespace";

    final String key = "key";

    public void showTokenAuthentication() {
        // BEGIN: com.azure.messaging.eventgrid.namespaces.TokenCredentialExample
        EventGridSenderClient client = new EventGridSenderClientBuilder().endpoint("your endpoint")
            .topicName("your topic")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.messaging.eventgrid.namespaces.TokenCredentialExample
    }

    public void showKeyAuthentication() {
        // BEGIN: com.azure.messaging.eventgrid.namespaces.AccessKeyExample
        EventGridSenderClient client = new EventGridSenderClientBuilder().endpoint("your endpoint")
            .topicName("your topic")
            .credential(new AzureKeyCredential("your access key"))
            .buildClient();
        // END: com.azure.messaging.eventgrid.namespaces.AccessKeyExample
    }

    public void sendEvents() {
        EventGridSenderClient client = new EventGridSenderClientBuilder().endpoint(endpoint)
            .topicName(topic)
            .credential(new AzureKeyCredential(key))
            .buildClient();

        // BEGIN: com.azure.messaging.eventgrid.namespaces.SendEventExample
        User user = new User("John", "Doe");
        CloudEvent cloudEvent
            = new CloudEvent("source", "type", BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");
        client.send(cloudEvent);
        // END: com.azure.messaging.eventgrid.namespaces.SendEventExample

        // BEGIN: com.azure.messaging.eventgrid.namespaces.SendMultipleEventsExample
        User john = new User("John", "Doe");
        User jane = new User("Jane", "Doe");
        CloudEvent johnEvent
            = new CloudEvent("source", "type", BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");
        CloudEvent janeEvent
            = new CloudEvent("source", "type", BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");
        client.send(Arrays.asList(johnEvent, janeEvent));
        // END: com.azure.messaging.eventgrid.namespaces.SendMultipleEventsExample
    }

    public void sendEventsAsync() {
        EventGridSenderAsyncClient client = new EventGridSenderClientBuilder().endpoint(endpoint)
            .topicName(topic)
            .credential(new AzureKeyCredential(key))
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.eventgrid.namespaces.SendEventAsyncExample
        User user = new User("John", "Doe");
        CloudEvent cloudEvent
            = new CloudEvent("source", "type", BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");
        client.send(cloudEvent).subscribe();
        // END: com.azure.messaging.eventgrid.namespaces.SendEventAsyncExample

        // BEGIN: com.azure.messaging.eventgrid.namespaces.SendMultipleEventsAsyncExample
        User john = new User("John", "Doe");
        User jane = new User("Jane", "Doe");
        CloudEvent johnEvent
            = new CloudEvent("source", "type", BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");
        CloudEvent janeEvent
            = new CloudEvent("source", "type", BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");
        client.send(Arrays.asList(johnEvent, janeEvent)).subscribe();
        // END: com.azure.messaging.eventgrid.namespaces.SendMultipleEventsAsyncExample
    }

    public void receiveEventsExample() {
        // BEGIN: com.azure.messaging.eventgrid.namespaces.ReceiveEventExample

        EventGridReceiverClient client = new EventGridReceiverClientBuilder().endpoint("your endpoint")
            .topicName("your topic")
            .subscriptionName("your subscription")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Receive optionally takes a maximum number of events and a duration to wait. The defaults are
        // 1 event and 60 seconds.
        ReceiveResult result = client.receive(2, Duration.ofSeconds(10));

        // The result contains the received events and the details of the operation. Use the details to obtain
        // lock tokens for settling the event. Lock tokens are opaque strings that are used to acknowledge,
        // release, or reject the event.

        result.getDetails().forEach(details -> {
            CloudEvent event = details.getEvent();
            // Based on some examination of the event, it might be acknowledged, released, or rejected.
            User user = event.getData().toObject(User.class);
            if (user.getFirstName().equals("John")) {
                // Acknowledge the event.
                client.acknowledge(Arrays.asList(details.getBrokerProperties().getLockToken()));
            } else if (user.getFirstName().equals("Jane")) {
                // Release the event.
                client.release(Arrays.asList(details.getBrokerProperties().getLockToken()));
            } else {
                // Reject the event.
                client.reject(Arrays.asList(details.getBrokerProperties().getLockToken()));
            }
        });

        // END: com.azure.messaging.eventgrid.namespaces.ReceiveEventExample
    }

}
