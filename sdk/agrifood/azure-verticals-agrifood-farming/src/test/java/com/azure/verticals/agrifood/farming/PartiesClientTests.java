// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.verticals.agrifood.farming;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.identity.DefaultAzureCredentialBuilder;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public class PartiesClientTests extends TestBase {
    private final String defaultEndpoint = "https://REDACTED.farmbeats.azure.net";

    private PartiesAsyncClient createPartiesClient() {
        PartiesClientBuilder builder = new PartiesClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("FARMBEATS_ENDPOINT", defaultEndpoint))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient())
                .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder.buildAsyncClient();
    }

    private BoundariesAsyncClient createBoundariesClient() {
        BoundariesClientBuilder builder = new BoundariesClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("FARMBEATS_ENDPOINT", defaultEndpoint))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient())
                .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder.buildAsyncClient();
    }

    private ScenesAsyncClient createScenesClient() {
        ScenesClientBuilder builder = new ScenesClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("FARMBEATS_ENDPOINT", defaultEndpoint))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient())
                .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder.buildAsyncClient();
    }

    @Test
    public void testParties() {
        PartiesAsyncClient client = createPartiesClient();
        JSONObject object = new JSONObject().appendField("name", "party1");
        BinaryData party = BinaryData.fromObject(object);
        client.createOrUpdateWithResponse("contoso-party", party, null).block();
        Response<BinaryData> response = client.getWithResponse("contoso-party", new RequestOptions()).block();
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    public void testSatelliteJob() {
        BoundariesAsyncClient boundariesClient = createBoundariesClient();
        BinaryData boundary = BinaryData.fromString(
            "{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[73.70457172393799,20.545385304358106],[73.70457172393799,20.545385304358106],[73.70448589324951,20.542411534243367],[73.70877742767334,20.541688176010233],[73.71023654937744,20.545083911372505],[73.70663166046143,20.546992723579137],[73.70457172393799,20.545385304358106]]]},\"name\":\"string\",\"description\":\"string\"}");
        Response<BinaryData> response
            = boundariesClient.createOrUpdateWithResponse("contoso-party", "contoso-boundary", boundary, null).block();
        Assertions.assertNotNull(response.getValue());

        ScenesAsyncClient scenesClient = createScenesClient();
        BinaryData satelliteJob = BinaryData.fromString(
            "{\"boundaryId\":\"contoso-boundary\",\"endDateTime\":\"2022-02-01T00:00:00Z\",\"partyId\":\"contoso-party\",\"source\":\"Sentinel_2_L2A\",\"startDateTime\":\"2022-01-01T00:00:00Z\",\"provider\":\"Microsoft\",\"data\":{\"imageNames\":[\"NDVI\"],\"imageFormats\":[\"TIF\"],\"imageResolutions\":[10]},\"name\":\"string\",\"description\":\"string\"}");
        PollResponse<BinaryData> satelliteJobPollResponse = setPlaybackSyncPollerPollInterval(
            scenesClient.beginCreateSatelliteDataIngestionJob("contoso-job-35864", satelliteJob, null).getSyncPoller())
                .waitForCompletion();
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            satelliteJobPollResponse.getStatus());

        Assertions.assertNotNull(
            scenesClient.getSatelliteDataIngestionJobDetailsWithResponse("contoso-job-35864", null).block().getValue());

        Iterable<BinaryData> scenes
            = scenesClient.list("Microsoft", "contoso-party", "contoso-boundary", "Sentinel_2_L2A", null).toIterable();
        scenes.forEach(scene -> Assertions.assertNotNull(scene));
    }
}
