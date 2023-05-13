package com.azure.messaging.eventgrid;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
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

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventGridMessagingClientTest {

    public static final String TOPICNAME = "billtopic";
    public static final String EVENT_SUBSCRIPTION_NAME = "billesname";

    EventGridClient buildClient() {
        EventGridClientBuilder builder = new EventGridClientBuilder()
            .httpClient(HttpClient.createDefault())
            .endpoint("https://billtestns.centraluseuap-1.eventgrid.azure.net")
            .serviceVersion(EventGridMessagingServiceVersion.V2023_06_01_PREVIEW)
            .credential(CREDENTIAL);
        return builder.buildClient();
    }

    AzureKeyCredential CREDENTIAL = new AzureKeyCredential(Configuration.getGlobalConfiguration().get("EG_KEY"));
    @Test
    void publishCloudEventWithResponse() {
        EventGridClient client = buildClient();
        CloudEvent event = getCloudEvent();
        client.publishCloudEventWithResponse(TOPICNAME, event, new RequestOptions());
    }



    @Test
    void publishBatchOfCloudEventsWithResponse() {
        EventGridClient client = buildClient();
        List<CloudEvent> events = List.of(getCloudEvent());
        client.publishCloudEventsWithResponse(TOPICNAME, events, new RequestOptions());
    }

    @Test
    void receiveBatchOfCloudEventsWithResponse() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEventsWithResponse(TOPICNAME, EVENT_SUBSCRIPTION_NAME, new RequestOptions()).getValue();
        assertNotNull(receiveResult);
        assertTrue(receiveResult.getValue().size() > 0);
    }

    @Test
    void acknowledgeBatchOfCloudEventsWithResponse() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEventsWithResponse(TOPICNAME, EVENT_SUBSCRIPTION_NAME, new RequestOptions()).getValue();
        AcknowledgeOptions acknowledgeOptions = new AcknowledgeOptions(List.of(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        AcknowledgeResult acknowledgeResult = client.acknowledgeCloudEventsWithResponse(TOPICNAME, EVENT_SUBSCRIPTION_NAME, acknowledgeOptions, new RequestOptions()).getValue();
        assertNotNull(acknowledgeResult);
        assertTrue(acknowledgeResult.getSucceededLockTokens().size() > 0);
    }

    @Test
    void releaseBatchOfCloudEventsWithResponse() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEventsWithResponse(TOPICNAME, EVENT_SUBSCRIPTION_NAME, new RequestOptions()).getValue();
        ReleaseOptions releaseOptions = new ReleaseOptions(List.of(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        ReleaseResult releaseResult = client.releaseCloudEventsWithResponse(TOPICNAME, EVENT_SUBSCRIPTION_NAME, releaseOptions, new RequestOptions()).getValue();
        assertNotNull(releaseResult);
        assertTrue(releaseResult.getSucceededLockTokens().size() > 0);
    }

    @Test
    void rejectBatchOfCloudEventsWithResponse() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEventsWithResponse(TOPICNAME, EVENT_SUBSCRIPTION_NAME, new RequestOptions()).getValue();
        RejectOptions rejectOptions = new RejectOptions(List.of(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        RejectResult rejectResult = client.rejectCloudEventsWithResponse(TOPICNAME, EVENT_SUBSCRIPTION_NAME, rejectOptions, new RequestOptions()).getValue();
        assertNotNull(rejectResult);
        assertTrue(rejectResult.getSucceededLockTokens().size() > 0);
    }

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
        ReceiveResult result = client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 10,10);
        assertNotNull(result);
        assertTrue(result.getValue().size() > 0);
    }

    @Test
    void acknowledgeBatchOfCloudEvents() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 1, 10);
        AcknowledgeOptions acknowledgeOptions = new AcknowledgeOptions(List.of(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        AcknowledgeResult acknowledgeResult = client.acknowledgeCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, acknowledgeOptions);
        assertNotNull(acknowledgeResult);
        assertTrue(acknowledgeResult.getSucceededLockTokens().size() > 0);
    }

    @Test
    void releaseBatchOfCloudEvents() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 1, 10);
        ReleaseOptions releaseOptions = new ReleaseOptions(List.of(receiveResult.getValue().get(0).getBrokerProperties().getLockToken()));
        ReleaseResult releaseResult = client.releaseCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, releaseOptions);
        assertNotNull(releaseResult);
        assertTrue(releaseResult.getSucceededLockTokens().size() > 0);
    }

    @Test
    void rejectBatchOfCloudEvents() {
        EventGridClient client = buildClient();
        client.publishCloudEvent(TOPICNAME, getCloudEvent());
        ReceiveResult receiveResult = client.receiveCloudEvents(TOPICNAME, EVENT_SUBSCRIPTION_NAME, 1, 10);
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
