package com.azure.messaging.eventgrid;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import jdk.jfr.Event;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventGridMessagingClientTest {

    EventGridClient buildClient() {
        EventGridClientBuilder builder = new EventGridClientBuilder()
            .httpClient(HttpClient.createDefault())
            .endpoint("https://llaweventgridv2.centraluseuap-1.eventgrid.azure.net")
            .serviceVersion(EventGridPublisherServiceVersion.V2022_05_01)
            .credential(CREDENTIAL);
        return builder.buildClient();
    }

    AzureKeyCredential CREDENTIAL = new AzureKeyCredential(Configuration.getGlobalConfiguration().get("EG_KEY"));
    RequestOptions OPTIONS = new RequestOptions().addHeader("Authorization", "SharedAccessKey " + CREDENTIAL.getKey());
    @Test
    void publishCloudEventWithResponse() {
        EventGridClient client = buildClient();
        CloudEvent event = new CloudEvent("/billwert/testEvent", "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(new HashMap<String, String>() {
                {
                    put("Field1", "Value1");
                    put("Field2", "Value2");
                    put("Field3", "Value3");
                }
            }), CloudEventDataFormat.JSON, "application/json")
            .setSubject("Test")
            .setTime(OffsetDateTime.now());
        client.publishCloudEventWithResponse("testtopic", event, OPTIONS);
    }

    @Test
    void publishBatchOfCloudEventsWithResponse() {
    }

    @Test
    void receiveBatchOfCloudEventsWithResponse() {
//        EventGridClient client = buildClient();
//        ReceiveResponse response = client.receiveBatchOfCloudEventsWithResponse("testtopic", "testsub", OPTIONS).getValue();
//        CloudEvent event = response.getValue().get(0).getEvent();
    }

    @Test
    void acknowledgeBatchOfCloudEventsWithResponse() {
//        EventGridMessagingClient client = buildClient();
//        LockTokenInput input = new LockTokenInput(List.of("CiYKJEI1OEY1QTYzLUEzOEItNDEzRS1CQ0I4LUE4ODZCNzIzRTVGQhISChDc3HHUgJZM6Y+26aDAlsNX"));
//        LockTokensResponse response =  client.acknowledgeBatchOfCloudEventsWithResponse("testtopic", "testsub", input, OPTIONS).getValue();
//        List<String> success = response.getSucceededLockTokens();
    }

    @Test
    void releaseBatchOfCloudEventsWithResponse() {
    }

    @Test
    void publishCloudEvent() {
    }

    @Test
    void publishBatchOfCloudEvents() {
    }

    @Test
    void receiveBatchOfCloudEvents() {
    }

    @Test
    void testReceiveBatchOfCloudEvents() {
    }

    @Test
    void acknowledgeBatchOfCloudEvents() {
    }

    @Test
    void releaseBatchOfCloudEvents() {
    }
}
