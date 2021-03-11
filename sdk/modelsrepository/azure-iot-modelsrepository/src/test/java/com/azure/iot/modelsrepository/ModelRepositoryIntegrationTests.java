package com.azure.iot.modelsrepository;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.azure.iot.modelsrepository.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;

public class ModelRepositoryIntegrationTests extends ModelsRepositoryTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsSingleDtmiNoDependencies(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion) throws URISyntaxException {
        final String dtmi = "dtmi:com:example:Thermostat;1";

        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion);

        Map<String, String> result = client.getModels(dtmi).block();
        Assertions.assertTrue(result.keySet().stream().count() == 1);
        Assertions.assertTrue(result.keySet().contains(dtmi));

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsSingleDtmiWithDependencies(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion) throws URISyntaxException {
        final String dtmi = "dtmi:com:example:TemperatureController;1";
        List<String> expectedDependencies = Arrays.asList("dtmi:com:example:Thermostat;1", "dtmi:azure:DeviceManagement:DeviceInformation;1");
        List<String> expectedDtmis = new ArrayList<>(expectedDependencies);
        expectedDtmis.add(dtmi);

        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion);

        Map<String, String> result = client.getModels(dtmi).block();

        Assertions.assertTrue(result.keySet().stream().count() == expectedDtmis.stream().count());
        Assertions.assertTrue(result.keySet().containsAll(expectedDependencies));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsEnsureNoDuplicates(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion) throws URISyntaxException {
        List<String> inputDtmis = Arrays.asList(
            "dtmi:azure:DeviceManagement:DeviceInformation;1",
            "dtmi:azure:DeviceManagement:DeviceInformation;1"
        );

        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion);

        Map<String, String> result = client.getModels(inputDtmis).block();
        Assertions.assertTrue(result.keySet().stream().count() == 1);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.iot.modelsrepository.TestHelper#getTestParameters")
    public void getModelsSingleDtmiWithDepsDisableDependencyResolution(HttpClient httpClient, ModelsRepositoryServiceVersion serviceVersion) throws URISyntaxException {
        final String dtmi = "dtmi:com:example:Thermostat;1";
        ModelsRepositoryAsyncClient client = getAsyncClient(httpClient, serviceVersion);

        Map<String, String> result = client.getModels(dtmi, DependencyResolutionOptions.DISABLED).block();

        Assertions.assertTrue(result.keySet().stream().count() == 1);
        Assertions.assertTrue(result.keySet().contains(dtmi));
    }
}
