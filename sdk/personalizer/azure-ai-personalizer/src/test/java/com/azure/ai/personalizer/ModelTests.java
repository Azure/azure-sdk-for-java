package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.ModelProperties;
import com.azure.core.http.HttpClient;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

import static com.azure.ai.personalizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelTests extends PersonalizerTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void ModelTest(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerAdminClient client = getAdministrationClient(httpClient, serviceVersion, true);
        exportModel(false, client);
        exportModel(true, client);
        getModelProperties(client);
        resetModel(client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void ExportImportModelTest(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerAdminClient client = getAdministrationClient(httpClient, serviceVersion, true);
        Flux<ByteBuffer> response1 = exportModel(true, client);
        ImportSignedModel(response1, client);
    }

    private Flux<ByteBuffer> exportModel(boolean isSigned, PersonalizerAdminClient client)
    {
        return client.exportModel(isSigned);
    }

    private void ImportSignedModel(Flux<ByteBuffer> modelBody, PersonalizerAdminClient client)
    {
        client.importModel(modelBody);
    }


    private void resetModel(PersonalizerAdminClient client)
    {
        client.resetModel();
    }

    private void getModelProperties(PersonalizerAdminClient client)
    {
        ModelProperties modelProperties = client.getModelProperties();
        assertTrue(modelProperties.getCreationTime() != null);
        assertTrue(modelProperties.getLastModifiedTime() != null);
    }
}
