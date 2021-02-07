package com.azure.mixedreality.remoterendering;

import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.mixedreality.remoterendering.models.Conversion;
import com.azure.mixedreality.remoterendering.models.ConversionOptions;
import com.azure.mixedreality.remoterendering.models.ConversionStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.azure.core.http.HttpClient;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

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
        var client = getClient(httpClient);

        ConversionOptions conversionOptions = new ConversionOptions()
            .inputStorageContainerUrl(getStorageUrl())
            .inputRelativeAssetPath("testBox.fbx")
            .inputBlobPrefix("Input")
            .inputStorageContainerReadListSas(getBlobContainerSasToken())
            .outputStorageContainerUrl(getStorageUrl())
            .outputBlobPrefix("Output")
            .outputStorageContainerWriteSas(getBlobContainerSasToken());

        String conversionId = getRandomId("conversionTest");

        var poller = client.beginConversion(conversionId, conversionOptions);

        var terminalPoller = poller.map(response -> {
            var conversion = response.getValue();
            assertEquals(conversionId, conversion.getId());
            assertEquals(conversionOptions.getInputRelativeAssetPath(), conversion.getOptions().getInputRelativeAssetPath());
            assertNotEquals(ConversionStatus.FAILED, conversion.getStatus());
            return response;
        }).filter(response -> {
            return ((response.getStatus() != LongRunningOperationStatus.NOT_STARTED)
                                  && (response.getStatus() != LongRunningOperationStatus.IN_PROGRESS));
        });

        StepVerifier.create(terminalPoller)
            .assertNext(response -> {
                assertEquals(response.getStatus(), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);

                var conversion = response.getValue();
                assertEquals(conversion.getStatus(), ConversionStatus.SUCCEEDED);
                assertTrue(conversion.getOutputAssetUrl().endsWith("Output/testBox.arrAsset"));
            })
            .verifyComplete();

        StepVerifier.create(client.getConversion(conversionId))
            .assertNext(conversion -> {
                assertEquals(conversion.getStatus(), ConversionStatus.SUCCEEDED);
                assertTrue(conversion.getOutputAssetUrl().endsWith("Output/testBox.arrAsset"));
            })
            .verifyComplete();

        var foundConversion = client.listConversions().any(conversion -> {
            return conversion.getId().equals(conversionId);
        });

        assertTrue(foundConversion.block());
    };
}
