// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import com.azure.messaging.eventgrid.models.AcknowledgeOptions;
import com.azure.messaging.eventgrid.models.AcknowledgeResult;
import com.azure.messaging.eventgrid.models.ReceiveResult;
import com.azure.messaging.eventgrid.models.RejectOptions;
import com.azure.messaging.eventgrid.models.RejectResult;
import com.azure.messaging.eventgrid.models.ReleaseOptions;
import com.azure.messaging.eventgrid.models.ReleaseResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventGridMessagingClientTest {

    public static final String TOPICNAME = "billwertegv2-egv2-topic";
    public static final String EVENT_SUBSCRIPTION_NAME = "billwertegv2-egv2-es";

    EventGridClient buildClient() {
        EventGridClientBuilder builder = new EventGridClientBuilder()
            .httpClient(HttpClient.createDefault())
            .endpoint("https://billwertegv2-egv2-ns.centraluseuap-1.eventgrid.azure.net")
            .serviceVersion(EventGridMessagingServiceVersion.V2023_06_01_PREVIEW)
            .credential(CREDENTIAL)
            .httpLogOptions(new HttpLogOptions()
                .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .setAllowedQueryParamNames(Set.of("maxWaitTime")));
        return builder.buildClient();
    }

    AzureKeyCredential CREDENTIAL = new AzureKeyCredential(Configuration.getGlobalConfiguration().get("EG_KEY"));
    @Test
    void publishCloudEvent() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
    }

    @Test
    void publishBatchOfCloudEvents() {
        EventGridClient client = buildClient();
        client.publishCloudEvents(TOPICNAME, List.of(getCloudEvent(), getCloudEvent()));
    }

    @Test
    void receiveBatchOfCloudEvents() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult result = client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 10, Duration.ofSeconds(10));
        assertNotNull(result);
        assertTrue(result.getValue().size() > 0);
    }

    @Test
    void acknowledgeBatchOfCloudEvents() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
        AcknowledgeOptions acknowledgeOptions = new AcknowledgeOptions(List.of(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        AcknowledgeResult acknowledgeResult = client.acknowledgeCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, acknowledgeOptions);
        assertNotNull(acknowledgeResult);
        assertTrue(acknowledgeResult.getSucceededLockTokens().size() > 0);
    }

    @Test
    void releaseBatchOfCloudEvents() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
        ReleaseOptions releaseOptions = new ReleaseOptions(List.of(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        ReleaseResult releaseResult = client.releaseCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, releaseOptions);
        assertNotNull(releaseResult);
        assertTrue(releaseResult.getSucceededLockTokens().size() > 0);
    }

    @Test
    void rejectBatchOfCloudEvents() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 1, Duration.ofSeconds(10));
        RejectOptions rejectOptions = new RejectOptions(List.of(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        RejectResult rejectResult = client.rejectCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, rejectOptions);
        assertNotNull(rejectResult);
        assertTrue(rejectResult.getSucceededLockTokens().size() > 0);
    }

    private static CloudEvent getCloudEvent() {
        return new CloudEvent("/events/testEvent", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }), CloudEventDataFormat.JSON, "application/json")
            .setSubject("Test")
            .setTime(OffsetDateTime.now());
    }
}
