package com.azure.health.deidentification.realtime;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;
import com.azure.health.deidentification.DeidentificationAsyncClient;
import com.azure.health.deidentification.DeidentificationClient;
import com.azure.health.deidentification.DeidentificationClientBuilder;
import com.azure.health.deidentification.models.*;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AsyncRealtimeOperationsTest extends TestProxyTestBase {
    protected DeidentificationAsyncClient deidentificationClient;

    @Override
    protected void beforeTest() {
        DeidentificationClientBuilder deidentificationClientbuilder = new DeidentificationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (getTestMode() == TestMode.PLAYBACK) {
            deidentificationClientbuilder.httpClient(interceptorManager.getPlaybackClient())
                .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else if (getTestMode() == TestMode.RECORD) {
            deidentificationClientbuilder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            deidentificationClientbuilder.credential(new DefaultAzureCredentialBuilder().build());
        }
        deidentificationClient = deidentificationClientbuilder.buildAsyncClient();

    }

    @Test
    void testSurrogateReturnsExpected() {
        String inputText = "Hello, my name is John Smith.";
        DeidentificationContent content = new DeidentificationContent(inputText, OperationType.SURROGATE, DocumentDataType.PLAINTEXT);
        Mono<DeidentificationResult> result = deidentificationClient.deidentify(content);
        DeidentificationResult asyncResult = result.block();

        assertNotNull(asyncResult);
        assertNull(asyncResult.getTaggerResult());
        assertNotNull(asyncResult.getOutputText());
        assertTrue(asyncResult.getOutputText().length() > 21);
        assertNotEquals(inputText, asyncResult.getOutputText());
    }

    @Test
    void testTagReturnsExpected() {
        String inputText = "Hello, my name is John Smith.";
        DeidentificationContent content = new DeidentificationContent(inputText, OperationType.TAG, DocumentDataType.PLAINTEXT);
        Mono<DeidentificationResult> result = deidentificationClient.deidentify(content);
        DeidentificationResult asyncResult = result.block();

        assertNotNull(asyncResult);
        assertNotNull(asyncResult.getTaggerResult());
        assertNull(asyncResult.getOutputText());
        assertNull(asyncResult.getTaggerResult().getEtag());
        assertNull(asyncResult.getTaggerResult().getPath());
        assertEquals(StringIndexType.TEXT_ELEMENT_V8, asyncResult.getTaggerResult().getStringIndexType());
        assertFalse(asyncResult.getTaggerResult().getEntities().isEmpty());
        assertTrue(asyncResult.getTaggerResult().getEntities().get(0).getCategory().equals(PhiCategory.DOCTOR) || asyncResult.getTaggerResult().getEntities().get(0).getCategory().equals(PhiCategory.PATIENT));
        assertEquals("John Smith", asyncResult.getTaggerResult().getEntities().get(0).getText());
        assertEquals(18, asyncResult.getTaggerResult().getEntities().get(0).getOffset());
        assertEquals(10, asyncResult.getTaggerResult().getEntities().get(0).getLength());
    }

}
