// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

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
import static com.azure.iot.modelsrepository.TestHelper.assertRestException;

public class ModelRepositoryIntegrationTests extends ModelsRepositoryTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsSingleDtmiNoDependencies(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion) throws URISyntaxException {
        final String dtmi = "dtmi:com:example:Thermostat;1";

        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion);

        StepVerifier
            .create(client.getModels(dtmi))
            .assertNext(model -> Assertions.assertTrue(model.keySet().size() == 1 && model.containsKey(dtmi)))
            .verifyComplete();
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsSingleDtmiDoesNotExist(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion) throws URISyntaxException {
        final String dtmi = "dtmi:com:example:Thermostatddd;1";

        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion);

        StepVerifier
            .create(client.getModels(dtmi))
            .verifyErrorSatisfies(error -> assertRestException(error, 400));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsSingleDtmiWithDependencies(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion) throws URISyntaxException {
        final String dtmi = "dtmi:com:example:TemperatureController;1";
        List<String> expectedDependencies = Arrays.asList("dtmi:com:example:Thermostat;1", "dtmi:azure:DeviceManagement:DeviceInformation;1");
        List<String> expectedDtmis = new ArrayList<>(expectedDependencies);
        expectedDtmis.add(dtmi);

        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion);

        StepVerifier
            .create(client.getModels(dtmi))
            .assertNext(model -> Assertions.assertTrue(model.keySet().size() == expectedDtmis.size() && model.keySet().containsAll(expectedDependencies)))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsEnsureNoDuplicates(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion) throws URISyntaxException {
        List<String> inputDtmis = Arrays.asList(
            "dtmi:azure:DeviceManagement:DeviceInformation;1",
            "dtmi:azure:DeviceManagement:DeviceInformation;1"
        );

        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion);

        StepVerifier
            .create(client.getModels(inputDtmis))
            .assertNext(model -> Assertions.assertEquals(model.keySet().size(), 1))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsSingleDtmiWithDepsDisableDependencyResolution(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion) throws URISyntaxException {
        final String dtmi = "dtmi:com:example:Thermostat;1";
        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion);

        StepVerifier
            .create(client.getModels(dtmi, ModelsDependencyResolution.DISABLED))
            .assertNext(model -> Assertions.assertTrue(model.keySet().size() == 1 && model.containsKey(dtmi)))
            .verifyComplete();
    }
}
