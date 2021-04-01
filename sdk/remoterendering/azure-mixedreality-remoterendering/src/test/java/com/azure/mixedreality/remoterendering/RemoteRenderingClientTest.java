// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.SyncPoller;
import com.azure.mixedreality.remoterendering.implementation.models.ErrorResponseException;
import com.azure.mixedreality.remoterendering.models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoteRenderingClientTest extends RemoteRenderingTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private RemoteRenderingClient getClient(HttpClient httpClient) {
        return new RemoteRenderingClientBuilder()
            .accountId(super.getAccountId())
            .accountDomain(super.getAccountDomain())
            .credential(super.getAccountKey())
            .endpoint(super.getServiceEndpoint())
            .pipeline(super.getHttpPipeline(httpClient))
            .buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void conversionTest(HttpClient httpClient) {
        RemoteRenderingClient client = getClient(httpClient);

        AssetConversionOptions conversionOptions = new AssetConversionOptions()
            .setInputStorageContainerUrl(getStorageUrl())
            .setInputRelativeAssetPath("testBox.fbx")
            .setInputBlobPrefix("Input")
            .setInputStorageContainerReadListSas(getBlobContainerSasToken())
            .setOutputStorageContainerUrl(getStorageUrl())
            .setOutputBlobPrefix("Output")
            .setOutputStorageContainerWriteSas(getBlobContainerSasToken());

        String conversionId = getRandomId("conversionTest");

        SyncPoller<AssetConversion, AssetConversion> conversionPoller = setSyncPollerPollInterval(client
            .beginConversion(conversionId, conversionOptions));

        AssetConversion conversion0 = conversionPoller.poll().getValue();

        assertEquals(conversionId, conversion0.getId());
        assertEquals(conversionOptions.getInputRelativeAssetPath(), conversion0.getOptions().getInputRelativeAssetPath());
        assertNotEquals(AssetConversionStatus.FAILED, conversion0.getStatus());

        AssetConversion conversion = client.getConversion(conversionId);
        assertEquals(conversionId, conversion.getId());
        assertNotEquals(AssetConversionStatus.FAILED, conversion.getStatus());

        AssetConversion conversion2 = conversionPoller.waitForCompletion().getValue();

        assertEquals(conversionId, conversion2.getId());
        assertEquals(AssetConversionStatus.SUCCEEDED, conversion2.getStatus());
        assertTrue(conversion2.getOutputAssetUrl().endsWith("Output/testBox.arrAsset"));

        AtomicReference<Boolean> foundConversion = new AtomicReference<Boolean>(false);

        // iterate over each page
        client.listConversions().forEach(c -> {
            if (c.getId().equals(conversionId)) {
                foundConversion.set(true);
            }
        });

        assertTrue(foundConversion.get());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void failedConversionNoAccessTest(HttpClient httpClient) {
        RemoteRenderingClient client = getClient(httpClient);

        // Don't provide SAS tokens.
        AssetConversionOptions conversionOptions = new AssetConversionOptions()
            .setInputStorageContainerUrl(getStorageUrl())
            .setInputRelativeAssetPath("testBox.fbx")
            .setInputBlobPrefix("Input")
            .setOutputStorageContainerUrl(getStorageUrl())
            .setOutputBlobPrefix("Output");

        String conversionId = getRandomId("failedConversionNoAccess");

        ErrorResponseException ex = assertThrows(ErrorResponseException.class, () -> client.beginConversion(conversionId, conversionOptions));

        assertTrue(ex.getMessage().contains(RESPONSE_CODE_403));

        // Error accessing connected storage account due to insufficient permissions. Check if the Mixed Reality resource has correct permissions assigned
        assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("storage"));
        assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("permissions"));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void failedConversionMissingAssetTest(HttpClient httpClient) {
        RemoteRenderingClient client = getClient(httpClient);

        AssetConversionOptions conversionOptions = new AssetConversionOptions()
            .setInputStorageContainerUrl(getStorageUrl())
            .setInputRelativeAssetPath("boxWhichDoesNotExist.fbx")
            .setInputBlobPrefix("Input")
            .setInputStorageContainerReadListSas(getBlobContainerSasToken())
            .setOutputStorageContainerUrl(getStorageUrl())
            .setOutputBlobPrefix("Output")
            .setOutputStorageContainerWriteSas(getBlobContainerSasToken());

        String conversionId = getRandomId("failedConversionMissingAsset");

        SyncPoller<AssetConversion, AssetConversion> conversionPoller = setSyncPollerPollInterval(client
            .beginConversion(conversionId, conversionOptions));

        AssetConversion conversion = conversionPoller.waitForCompletion().getValue();

        assertEquals(conversionId, conversion.getId());

        assertEquals(AssetConversionStatus.FAILED, conversion.getStatus());
        assertNotNull(conversion.getError());
        // Invalid input provided. Check logs in output container for details.
        assertTrue(conversion.getError().getMessage().toLowerCase(Locale.ROOT).contains("invalid input"));
        assertTrue(conversion.getError().getMessage().toLowerCase(Locale.ROOT).contains("logs"));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void sessionTest(HttpClient httpClient) {
        RemoteRenderingClient client = getClient(httpClient);
        BeginSessionOptions options = new BeginSessionOptions().setMaxLeaseTime(Duration.ofMinutes(4)).setSize(RenderingSessionSize.STANDARD);

        String sessionId = getRandomId("sessionTest");

        SyncPoller<RenderingSession, RenderingSession> sessionPoller = setSyncPollerPollInterval(client
            .beginSession(sessionId, options));

        RenderingSession session0 = sessionPoller.poll().getValue();

        assertEquals(options.getSize(), session0.getSize());
        assertEquals(sessionId, session0.getId());

        RenderingSession sessionProperties = client.getSession(sessionId);
        assertEquals(session0.getCreationTime(), sessionProperties.getCreationTime());

        UpdateSessionOptions updateOptions = new UpdateSessionOptions().maxLeaseTime(Duration.ofMinutes(5));
        RenderingSession updatedSession = client.updateSession(sessionId, updateOptions);
        assertEquals(updatedSession.getMaxLeaseTime().toMinutes(), 5);

        RenderingSession readyRenderingSession = sessionPoller.getFinalResult();
        assertTrue((readyRenderingSession.getMaxLeaseTime().toMinutes() == 4) || (readyRenderingSession.getMaxLeaseTime().toMinutes() == 5));
        assertNotNull(readyRenderingSession.getHostname());
        assertNotEquals(readyRenderingSession.getArrInspectorPort(), 0);
        assertEquals(readyRenderingSession.getSize(), options.getSize());

        UpdateSessionOptions updateOptions2 = new UpdateSessionOptions().maxLeaseTime(Duration.ofMinutes(6));
        assertEquals(6, updateOptions2.getMaxLeaseTime().toMinutes());

        assertTrue(client.listSessions().stream().anyMatch(s -> s.getId().equals(sessionId)));

        client.endSession(sessionId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void failedSessionTest(HttpClient httpClient) {
        RemoteRenderingClient client = getClient(httpClient);
        BeginSessionOptions options = new BeginSessionOptions().setMaxLeaseTime(Duration.ofMinutes(-4)).setSize(RenderingSessionSize.STANDARD);

        String sessionId = getRandomId("failedSessionTest");

        ErrorResponseException ex = assertThrows(ErrorResponseException.class, () -> client.beginSession(sessionId, options));

        assertTrue(ex.getMessage().contains(RESPONSE_CODE_400));

        // The maxLeaseTimeMinutes value cannot be negative
        assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("lease"));
        assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("negative"));
    }
}
