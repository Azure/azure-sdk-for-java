// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

import com.azure.core.exception.AzureException;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.iot.modelsrepository.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;

class ModelRepositoryIntegrationTests extends ModelsRepositoryTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsSingleDtmiNoDependencies(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion, String repositoryUri) throws URISyntaxException {
        String dtmi= "";
        if (repositoryUri == TestHelper.MODELS_REPOSITORY_NO_METADATA_ENDPOINT) {
            dtmi = "Azure:iot;plugandplay;models:main:dtmi:com:example:abcThermostat;1";
        } else {
            dtmi = "dtmi:com:example:Thermostat;1";
        }

        final String DTMI = dtmi;
        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion, repositoryUri);

        StepVerifier
            .create(client.getModels(DTMI))
            .assertNext(model -> Assertions.assertTrue(model.keySet().size() == 1 && model.containsKey(DTMI)))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsSingleDtmiDoesNotExist(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion, String repositoryUri) throws URISyntaxException {
        final String dtmi = "dtmi:com:example:Thermostatddd;1";

        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion, repositoryUri);

        StepVerifier
            .create(client.getModels(dtmi))
            .verifyErrorSatisfies(error -> Assertions.assertTrue(error.getClass() == AzureException.class));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsSingleDtmiWithDependencies(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion, String repositoryUri) throws URISyntaxException {
        final String dtmi = "dtmi:com:example:TemperatureController;1";
        List<String> expectedDependencies = Arrays.asList("dtmi:com:example:Thermostat;1", "dtmi:azure:DeviceManagement:DeviceInformation;1");
        List<String> expectedDtmis = new ArrayList<>(expectedDependencies);
        expectedDtmis.add(dtmi);

        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion, repositoryUri);

        StepVerifier
            .create(client.getModels(dtmi))
            .assertNext(model -> Assertions.assertTrue(model.keySet().size() == expectedDtmis.size() && model.keySet().containsAll(expectedDependencies)))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsEnsureNoDuplicates(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion, String repositoryUri) throws URISyntaxException {
        List<String> inputDtmis = Arrays.asList(
            "dtmi:azure:DeviceManagement:DeviceInformation;1",
            "dtmi:azure:DeviceManagement:DeviceInformation;1"
        );

        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion, repositoryUri);

        StepVerifier
            .create(client.getModels(inputDtmis))
            .assertNext(model -> Assertions.assertEquals(model.keySet().size(), 1))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsSingleDtmiWithDepsDisableDependencyResolution(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion, String repositoryUri) throws URISyntaxException {
        final String dtmi = "dtmi:com:example:Thermostat;1";
        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion, repositoryUri);

        StepVerifier
            .create(client.getModels(dtmi, ModelDependencyResolution.DISABLED))
            .assertNext(model -> Assertions.assertTrue(model.keySet().size() == 1 && model.containsKey(dtmi)))
            .verifyComplete();
    }
}
