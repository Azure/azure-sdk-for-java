package com.azure.maps.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.maps.render.models.Copyright;
import com.azure.maps.render.models.CopyrightCaption;
import com.azure.maps.render.models.MapAttribution;
import com.azure.maps.render.models.MapTileset;

public class RenderClientTestBase extends TestBase {
    static final String FAKE_API_KEY = "1234567890";

    private final String endpoint = Configuration.getGlobalConfiguration().get("API-LEARN_ENDPOINT");
    Duration durationTestMode;
    static InterceptorManager interceptorManagerTestBase;

    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            durationTestMode = Duration.ofMillis(1);
        } else {
            durationTestMode = TestUtils.DEFAULT_POLL_INTERVAL;
        }
        interceptorManagerTestBase = interceptorManager;
    }

    RenderClientBuilder getRenderAsyncClientBuilder(HttpClient httpClient, RenderServiceVersion serviceVersion) {
            RenderClientBuilder builder = new RenderClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion);
            String endpoint = getEndpoint();
            if (getEndpoint() != null) {
                builder.endpoint(endpoint);
            }
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(FAKE_API_KEY)).httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.credential((new AzureKeyCredential(
                Configuration.getGlobalConfiguration().get("SUBSCRIPTION_KEY"))));
        }
        return builder;
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new EnvironmentCredentialBuilder().httpClient(httpClient).build();
        }

        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, endpoint.replaceFirst("/$", "") + "/.default"));
        }

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return pipeline;
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : endpoint;
    }

    static void validateGetMapTile(byte[] actual) throws IOException {
        assertNotNull(actual);
        assertTrue(actual.length > 0);
    }

    static void validateGetMapTileWithResponse(int expectedStatusCode, Response<Void> response, ByteArrayOutputStream stream) throws IOException {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetMapTile(stream.toByteArray());
    }

    static void validateGetMapTileset(MapTileset expected, MapTileset actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getMinZoom(), actual.getMinZoom());
    }

    static void validateGetMapTilesetWithResponse(MapTileset expected, int expectedStatusCode, Response<MapTileset> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetMapTileset(expected, response.getValue());
    }

    static void validateGetMapAttribution(MapAttribution expected, MapAttribution actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getCopyrights().size(), actual.getCopyrights().size());
    }

    static void validateGetMapAttributionWithResponse(MapAttribution expected, int expectedStatusCode, Response<MapAttribution> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetMapAttribution(expected, response.getValue());
    }

    static void validateGetCopyrightCaption(CopyrightCaption expected, CopyrightCaption actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getCopyrightsCaption(), actual.getCopyrightsCaption());
        assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
    }

    static void validateGetCopyrightCaptionWithResponse(CopyrightCaption expected, int expectedStatusCode, Response<CopyrightCaption> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetCopyrightCaption(expected, response.getValue());
    }

    static void validateGetCopyrightCaptionFromBoundingBox(Copyright expected, Copyright actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
        assertEquals(expected.getGeneralCopyrights().size(), actual.getGeneralCopyrights().size());
        assertEquals(expected.getRegions().size(), actual.getRegions().size());
    }

    static void validateGetCopyrightCaptionFromBoundingBoxWithResponse(Copyright expected, int expectedStatusCode, Response<Copyright> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetCopyrightCaptionFromBoundingBox(expected, response.getValue());
    }

    static void validateGetMapStaticImage(byte[] actual) throws IOException {
        assertNotNull(actual);
        assertTrue(actual.length > 0);
    }

    static void validateGetMapStaticImageWithResponse(int expectedStatusCode, Response<Void> response, ByteArrayOutputStream stream) throws IOException {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetMapStaticImage(stream.toByteArray());
    }

    static void validateGetCopyrightForTile(Copyright expected, Copyright actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
        assertEquals(expected.getGeneralCopyrights().size(), actual.getGeneralCopyrights().size());
        assertEquals(expected.getRegions().size(), actual.getRegions().size());
    }

    static void validateGetCopyrightForTileWithResponse(Copyright expected, int expectedStatusCode, Response<Copyright> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetCopyrightForTile(expected, response.getValue());
    }

    static void validateGetCopyrightForWorld(Copyright expected, Copyright actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
        assertEquals(expected.getGeneralCopyrights().size(), actual.getGeneralCopyrights().size());
        assertEquals(expected.getRegions().size(), actual.getRegions().size());
    }

    static void validateGetCopyrightForWorldWithResponse(Copyright expected, int expectedStatusCode, Response<Copyright> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetCopyrightForTile(expected, response.getValue());
    }
}