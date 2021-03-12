// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.mixedreality.remoterendering.implementation.models.ErrorResponseException;
import com.azure.mixedreality.remoterendering.models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.azure.core.http.HttpClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class RemoteRenderingAsyncClientTest extends RemoteRenderingTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private RemoteRenderingAsyncClient getClient(HttpClient httpClient) {
        return new RemoteRenderingClientBuilder()
            .accountId(super.getAccountId())
            .accountDomain(super.getAccountDomain())
            .credential(super.getAccountKey())
            .endpoint(super.getServiceEndpoint())
            .pipeline(super.getHttpPipeline(httpClient))
            .buildAsyncClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void conversionTest(HttpClient httpClient) {
        RemoteRenderingAsyncClient client = getClient(httpClient);

        AssetConversionOptions conversionOptions = new AssetConversionOptions()
            .setInputStorageContainerUrl(getStorageUrl())
            .setInputRelativeAssetPath("testBox.fbx")
            .setInputBlobPrefix("Input")
            .setInputStorageContainerReadListSas(getBlobContainerSasToken())
            .setOutputStorageContainerUrl(getStorageUrl())
            .setOutputBlobPrefix("Output")
            .setOutputStorageContainerWriteSas(getBlobContainerSasToken());

        String conversionId = getRandomId("asyncConversionTest");

        PollerFlux<AssetConversion, AssetConversion> poller = setPollerFluxPollInterval(client
            .beginConversion(conversionId, conversionOptions));

        Flux<AsyncPollResponse<AssetConversion, AssetConversion>> terminalPoller = poller.map(response -> {
            AssetConversion conversion = response.getValue();
            assertEquals(conversionId, conversion.getId());
            assertEquals(conversionOptions.getInputRelativeAssetPath(), conversion.getOptions().getInputRelativeAssetPath());
            assertNotEquals(AssetConversionStatus.FAILED, conversion.getStatus());
            return response;
        }).filter(response -> ((response.getStatus() != LongRunningOperationStatus.NOT_STARTED)
                              && (response.getStatus() != LongRunningOperationStatus.IN_PROGRESS)));

        StepVerifier.create(terminalPoller)
            .assertNext(response -> {
                assertEquals(response.getStatus(), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);

                AssetConversion conversion = response.getValue();
                assertEquals(conversion.getStatus(), AssetConversionStatus.SUCCEEDED);
                assertTrue(conversion.getOutputAssetUrl().endsWith("Output/testBox.arrAsset"));
            })
            .verifyComplete();

        StepVerifier.create(client.getConversion(conversionId))
            .assertNext(conversion -> {
                assertEquals(conversion.getStatus(), AssetConversionStatus.SUCCEEDED);
                assertTrue(conversion.getOutputAssetUrl().endsWith("Output/testBox.arrAsset"));
            })
            .verifyComplete();

        Boolean foundConversion = client.listConversions().any(conversion -> conversion.getId().equals(conversionId)).block();
        assertNotNull(foundConversion);
        assertTrue(foundConversion);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void failedConversionNoAccessTest(HttpClient httpClient) {
        RemoteRenderingAsyncClient client = getClient(httpClient);

        // Don't provide SAS tokens.
        AssetConversionOptions conversionOptions = new AssetConversionOptions()
            .setInputStorageContainerUrl(getStorageUrl())
            .setInputRelativeAssetPath("testBox.fbx")
            .setInputBlobPrefix("Input")
            .setOutputStorageContainerUrl(getStorageUrl())
            .setOutputBlobPrefix("Output");

        String conversionId = getRandomId("failedConversionNoAccessAsync");

        PollerFlux<AssetConversion, AssetConversion> poller = setPollerFluxPollInterval(client
            .beginConversion(conversionId, conversionOptions));

        StepVerifier.create(poller).expectErrorMatches(error -> {
            // Error accessing connected storage account due to insufficient permissions. Check if the Mixed Reality resource has correct permissions assigned
            return (error instanceof ErrorResponseException)
                && error.getMessage().contains(RESPONSE_CODE_403)
                && error.getMessage().toLowerCase(Locale.ROOT).contains("storage")
                && error.getMessage().toLowerCase(Locale.ROOT).contains("permissions");
        }).verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void failedConversionMissingAssetTest(HttpClient httpClient) {
        RemoteRenderingAsyncClient client = getClient(httpClient);

        AssetConversionOptions conversionOptions = new AssetConversionOptions()
            .setInputStorageContainerUrl(getStorageUrl())
            .setInputRelativeAssetPath("boxWhichDoesNotExist.fbx")
            .setInputBlobPrefix("Input")
            .setInputStorageContainerReadListSas(getBlobContainerSasToken())
            .setOutputStorageContainerUrl(getStorageUrl())
            .setOutputBlobPrefix("Output")
            .setOutputStorageContainerWriteSas(getBlobContainerSasToken());

        String conversionId = getRandomId("failedConversionMissingAssetAsync");

        PollerFlux<AssetConversion, AssetConversion> poller = setPollerFluxPollInterval(client
            .beginConversion(conversionId, conversionOptions));

        Flux<AsyncPollResponse<AssetConversion, AssetConversion>> terminalPoller = poller.map(response -> {
            AssetConversion conversion = response.getValue();
            assertEquals(conversionId, conversion.getId());
            assertEquals(conversionOptions.getInputRelativeAssetPath(), conversion.getOptions().getInputRelativeAssetPath());
            assertNotEquals(AssetConversionStatus.SUCCEEDED, conversion.getStatus());
            return response;
        }).filter(response -> ((response.getStatus() != LongRunningOperationStatus.NOT_STARTED)
            && (response.getStatus() != LongRunningOperationStatus.IN_PROGRESS)));

        StepVerifier.create(terminalPoller)
            .assertNext(response -> {
                assertEquals(response.getStatus(), LongRunningOperationStatus.FAILED);

                AssetConversion conversion = response.getValue();

                assertEquals(AssetConversionStatus.FAILED, conversion.getStatus());
                assertNotNull(conversion.getError());
                // Invalid input provided. Check logs in output container for details.
                assertTrue(conversion.getError().getMessage().toLowerCase(Locale.ROOT).contains("invalid input"));
                assertTrue(conversion.getError().getMessage().toLowerCase(Locale.ROOT).contains("logs"));
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void sessionTest(HttpClient httpClient) {

        final long firstExpectedLeaseTimeMinutes = 4;
        final long secondExpectedLeaseTimeMinutes = 5;

        RemoteRenderingAsyncClient client = getClient(httpClient);

        BeginSessionOptions options = new BeginSessionOptions().setMaxLeaseTime(Duration.ofMinutes(firstExpectedLeaseTimeMinutes)).setSize(RenderingSessionSize.STANDARD);

        String sessionId = getRandomId("asyncSessionTest");

        PollerFlux<RenderingSession, RenderingSession> sessionPoller = setPollerFluxPollInterval(client
            .beginSession(sessionId, options));

        Flux<AsyncPollResponse<RenderingSession, RenderingSession>> terminalPoller = sessionPoller.map(response -> {
            RenderingSession session = response.getValue();
            assertEquals(sessionId, session.getId());
            assertNotEquals(RenderingSessionStatus.ERROR, session.getStatus());
            return response;
        }).filter(response -> ((response.getStatus() != LongRunningOperationStatus.NOT_STARTED)
            && (response.getStatus() != LongRunningOperationStatus.IN_PROGRESS)));

        StepVerifier.create(terminalPoller)
            .assertNext(response -> {
                assertEquals(response.getStatus(), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);

                RenderingSession readyRenderingSession = response.getValue();
                assertEquals(readyRenderingSession.getStatus(), RenderingSessionStatus.READY);

                assertEquals(firstExpectedLeaseTimeMinutes, readyRenderingSession.getMaxLeaseTime().toMinutes());
                assertNotNull(readyRenderingSession.getHostname());
                assertNotEquals(readyRenderingSession.getArrInspectorPort(), 0);
                assertEquals(readyRenderingSession.getSize(), options.getSize());
            })
            .verifyComplete();

        StepVerifier.create(client.getSession(sessionId))
            .assertNext(session -> {
                assertEquals(session.getStatus(), RenderingSessionStatus.READY);
                assertNotNull(session.getHostname());
                assertNotEquals(session.getArrInspectorPort(), 0);
                assertEquals(session.getSize(), options.getSize());
            })
            .verifyComplete();

        UpdateSessionOptions updateOptions = new UpdateSessionOptions().maxLeaseTime(Duration.ofMinutes(secondExpectedLeaseTimeMinutes));

        StepVerifier.create(client.updateSession(sessionId, updateOptions))
            .assertNext(session -> assertEquals(secondExpectedLeaseTimeMinutes, session.getMaxLeaseTime().toMinutes())).verifyComplete();

        Boolean foundSession = client.listSessions().any(session -> session.getId().equals(sessionId)).block();
        assertNotNull(foundSession);
        assertTrue(foundSession);

        client.endSession(sessionId).block();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void failedSessionTest(HttpClient httpClient) {
        RemoteRenderingAsyncClient client = getClient(httpClient);
        BeginSessionOptions options = new BeginSessionOptions().setMaxLeaseTime(Duration.ofMinutes(-4)).setSize(RenderingSessionSize.STANDARD);

        String sessionId = getRandomId("failedSessionTestAsync");

        PollerFlux<RenderingSession, RenderingSession> poller = setPollerFluxPollInterval(client
            .beginSession(sessionId, options));

        StepVerifier.create(poller).expectErrorMatches(error -> {
            // The maxLeaseTimeMinutes value cannot be negative
            return (error instanceof ErrorResponseException)
                && error.getMessage().contains(RESPONSE_CODE_400)
                && error.getMessage().toLowerCase(Locale.ROOT).contains("lease")
                && error.getMessage().toLowerCase(Locale.ROOT).contains("negative");
        }).verify();
    }
}
