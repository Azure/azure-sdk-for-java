// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.administration.PersonalizerAdministrationClient;
import com.azure.ai.personalizer.administration.models.PersonalizerModelProperties;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.personalizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelTests extends PersonalizerTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void modelTest(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerAdministrationClient client = getAdministrationClient(httpClient, serviceVersion, true);
        exportModel(false, client);
        exportModel(true, client);
        getModelProperties(client);
        resetModel(client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void exportImportModelTest(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerAdministrationClient client = getAdministrationClient(httpClient, serviceVersion, true);
        BinaryData model = exportModel(true, client);
        importSignedModel(model, client);
    }

    private BinaryData exportModel(boolean isSigned, PersonalizerAdministrationClient client) {
        return client.exportModel(isSigned);
    }

    private void importSignedModel(BinaryData modelBody, PersonalizerAdministrationClient client) {
        client.importModel(modelBody);
    }


    private void resetModel(PersonalizerAdministrationClient client) {
        client.resetModel();
    }

    private void getModelProperties(PersonalizerAdministrationClient client) {
        PersonalizerModelProperties modelProperties = client.getModelProperties();
        assertTrue(modelProperties.getCreationTime() != null);
        assertTrue(modelProperties.getLastModifiedTime() != null);
    }
}
