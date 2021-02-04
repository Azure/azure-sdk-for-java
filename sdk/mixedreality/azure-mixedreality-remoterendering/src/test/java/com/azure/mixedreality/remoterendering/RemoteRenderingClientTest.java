package com.azure.mixedreality.remoterendering;

import com.azure.core.http.HttpClient;
import com.azure.mixedreality.remoterendering.models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoteRenderingClientTest extends RemoteRenderingTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private RemoteRenderingClient client;

    private RemoteRenderingClient getClient(HttpClient httpClient) {
        return new RemoteRenderingClientBuilder()
            .accountId(super.getAccountId())
            .accountDomain(super.getAccountDomain())
            .endpoint(super.getServiceEndpoint())
            .pipeline(super.getHttpPipeline(httpClient))
            .buildClient();
    }

    //@ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    //@MethodSource("getHttpClients")
    public void conversionTest(HttpClient httpClient) {
        var client = getClient(httpClient);

        ConversionOptions conversionOptions = new ConversionOptionsBuilder()
            .inputStorageContainerUri(getStorageUrl())
            .inputRelativeAssetPath("testBox.fbx")
            .inputBlobPrefix("Input")
            .inputStorageContainerReadListSas(getBlobContainerSasToken())
            .outputStorageContainerUri(getStorageUrl())
            .outputBlobPrefix("Output")
            .buildConversionOptions();

        String conversionId = getRandomId("conversionTest");

        var conversionPoller = client.beginConversion(conversionId, conversionOptions);

        var conversion0 = conversionPoller.poll().getValue();

        assertEquals(conversionId, conversion0.getId());
        assertEquals(conversionOptions.getConversionInputOptions().getRelativeAssetPath(), conversion0.getOptions().getConversionInputOptions().getRelativeAssetPath());
        assertNotEquals(ConversionStatus.FAILED, conversion0.getStatus());

        Conversion conversion = client.getConversion(conversionId);
        assertEquals(conversionId, conversion.getId());
        assertNotEquals(ConversionStatus.FAILED, conversion.getStatus());

        var conversion2 = conversionPoller.waitForCompletion().getValue();

        assertEquals(conversionId, conversion2.getId());
        assertEquals(ConversionStatus.SUCCEEDED, conversion2.getStatus());
        assertTrue(conversion2.getOutputAssetUri().endsWith("Output/testBox.arrAsset"));

        AtomicReference<Boolean> foundConversion = new AtomicReference<Boolean>(false);

        // iterate over each page
        client.listConversions().forEach(c -> {
            if (c.getId() == conversionId) {
                foundConversion.set(true);
            }
        });

        assertTrue(foundConversion.get());
    }

    //@ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    //@MethodSource("getHttpClients")
    public void sessionTest(HttpClient httpClient) {
        var client = getClient(httpClient);
        SessionCreationOptions options = new SessionCreationOptions(4, SessionSize.STANDARD);

        String sessionId = getRandomId("sessionTest");

        var sessionPoller = client.beginSession(sessionId, options);

        var session0 = sessionPoller.poll().getValue();

        assertEquals(options.getSize(), session0.getSize());
        assertEquals(sessionId, session0.getId());

        Session sessionProperties = client.getSession(sessionId);
        assertEquals(session0.getCreationTime(), sessionProperties.getCreationTime());

        SessionUpdateOptions updateOptions = new SessionUpdateOptions(5);
        Session updatedSession = client.updateSession(sessionId, updateOptions);
        assertEquals(updatedSession.getMaxLeaseTimeMinutes(), 5);

        Session readyRenderingSession = sessionPoller.getFinalResult();
        assertTrue(readyRenderingSession.getMaxLeaseTimeMinutes() == 5);
        assertNotNull(readyRenderingSession.getHostname());
        assertNotNull(readyRenderingSession.getArrInspectorPort());
        assertNotNull(readyRenderingSession.getHostname());
        assertEquals(readyRenderingSession.getSize(), options.getSize());

        SessionUpdateOptions updateOptions2 = new SessionUpdateOptions(6);
        assertEquals(6, updateOptions2.getMaxLeaseTimeMinutes());

        AtomicReference<Boolean> foundSession = new AtomicReference<Boolean>(false);

        client.listSessions().forEach(s -> {
            if (s.getId() == sessionId) {
                foundSession.set(true);
            }
        });

        assertTrue(foundSession.get());
    }
}
