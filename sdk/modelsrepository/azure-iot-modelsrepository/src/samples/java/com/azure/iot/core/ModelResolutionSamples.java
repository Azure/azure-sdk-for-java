// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.core;

import com.azure.iot.modelsrepository.ModelDependencyResolution;
import com.azure.iot.modelsrepository.ModelsRepositoryAsyncClient;
import com.azure.iot.modelsrepository.ModelsRepositoryClient;
import com.azure.iot.modelsrepository.ModelsRepositoryClientBuilder;

import java.util.Arrays;

public class ModelResolutionSamples {
    private static final String CLIENT_SAMPLES_DIRECTORY_PATH = (System.getProperty("user.dir").concat("/src/samples/resources/TestModelRepo/")).replace("\\", "/");

    private static final int MAX_WAIT_TIME_ASYNC_OPERATIONS_IN_SECONDS = 10;

    /**
     * Demonstrates how to instantiate sync and async clients.
     */
    public static void clientInitializationSamples() {
        // When no URI is provided for instantiation, the Azure IoT Models Repository global endpoint
        // https://devicemodels.azure.com/ is used and the model dependency resolution
        // configuration is set to Enabled.
        ModelsRepositoryAsyncClient asyncClient = new ModelsRepositoryClientBuilder()
            .buildAsyncClient();

        ModelsRepositoryClient syncClient = new ModelsRepositoryClientBuilder()
            .buildClient();

        System.out.println("Initialized the async client pointing to the global endpoint" + asyncClient.getRepositoryEndpoint());
        System.out.println("Initialized the sync client pointing to the global endpoint" + syncClient.getRepositoryEndpoint());

        // This form shows specifying a custom URI for the models repository with default client options.
        // The default client options will enable model dependency resolution.
        asyncClient = new ModelsRepositoryClientBuilder()
            .repositoryEndpoint("https://contoso.com/models")
            .buildAsyncClient();

        syncClient = new ModelsRepositoryClientBuilder()
            .repositoryEndpoint("https://contoso.com/models")
            .buildClient();

        System.out.println("Initialized the async client pointing to the custom endpoint" + asyncClient.getRepositoryEndpoint().toString());
        System.out.println("Initialized the sync client pointing to the custom endpoint" + syncClient.getRepositoryEndpoint().toString());

        // The client will also work with a local file-system URI. This example shows initialization
        // with a local URI and disabling model dependency resolution.
        asyncClient = new ModelsRepositoryClientBuilder()
            .repositoryEndpoint(CLIENT_SAMPLES_DIRECTORY_PATH)
            .modelDependencyResolution(ModelDependencyResolution.DISABLED)
            .buildAsyncClient();

        syncClient = new ModelsRepositoryClientBuilder()
            .repositoryEndpoint(CLIENT_SAMPLES_DIRECTORY_PATH)
            .modelDependencyResolution(ModelDependencyResolution.DISABLED)
            .buildClient();

        System.out.println("Initialized the async client pointing to the local file-system: " + asyncClient.getRepositoryEndpoint());
        System.out.println("Initialized the sync client pointing to the local file-system: " + syncClient.getRepositoryEndpoint());
    }

    /**
     * Demonstrates how to get model and its dependencies from the default global endpoint.
     * @throws InterruptedException {@link InterruptedException}.
     */
    public static void getModelsFromGlobalRepository() throws InterruptedException {
        // Global endpoint client
        ModelsRepositoryAsyncClient asyncClient = new ModelsRepositoryClientBuilder()
            .buildAsyncClient();

        // The output of getModels will include at least the definition for the target dtmi.
        // If the model dependency resolution configuration is enabled, then models in which the
        // target dtmi depends on will also be included in the returned Map<String, String>.
        String targetDtmi = "dtmi:com:example:TemperatureController;1";

        // In this case the above dtmi has 2 model dependencies.
        // dtmi:com:example:Thermostat;1 and dtmi:azure:DeviceManagement:DeviceInformation;1
        asyncClient.getModels(targetDtmi)
            .doOnSuccess(aVoid -> System.out.println("Fetched the model and dependencies for: " + targetDtmi))
            .subscribe(res -> System.out.println(String.format("%s resolved in %s interfaces.", targetDtmi, res.size())));
    }

    /**
     * Demonstrates how to get multiple model definitions and their dependencies from the default global endpoint.
     * @throws InterruptedException {@link InterruptedException}.
     */
    public static void getMultipleModelsFromGlobalRepository() throws InterruptedException {
        // Global endpoint client
        ModelsRepositoryAsyncClient asyncClient = new ModelsRepositoryClientBuilder()
            .buildAsyncClient();

        // When given an Iterable of dtmis, the output of getModels() will include at
        // least the definitions of each dtmi enumerated in the Iterable.
        // If the model dependency resolution configuration is enabled, then models in which each
        // enumerated dtmi depends on will also be included in the returned Map<String, String>.
        Iterable<String> dtmis = Arrays.asList("dtmi:com:example:TemperatureController;1", "dtmi:com:example:azuresphere:sampledevice;1");

        // In this case the dtmi "dtmi:com:example:TemperatureController;1" has 2 model dependencies
        // and the dtmi "dtmi:com:example:azuresphere:sampledevice;1" has no additional dependencies.
        // The returned Map<String, String> will include 4 models.
        asyncClient.getModels(dtmis)
            .doOnSuccess(aVoid -> System.out.println("Fetched the models and dependencies for: " + String.join(", ", dtmis)))
            .subscribe(res -> System.out.println(String.format("Dtmis %s resolved in %s interfaces.", String.join(", ", dtmis), res.size())));
    }

    /**
     * Demonstrates how to get a model definition and its dependencies from a local file-system.
     * @throws InterruptedException {@link InterruptedException}.
     */
    public static void getModelsFromLocalRepository() throws InterruptedException {
        // Local sample repository client
        ModelsRepositoryAsyncClient asyncClient = new ModelsRepositoryClientBuilder()
            .repositoryEndpoint(CLIENT_SAMPLES_DIRECTORY_PATH)
            .buildAsyncClient();

        // The output of getModels will include at least the definition for the target dtmi.
        // If the model dependency resolution configuration is enabled, then models in which the
        // target dtmi depends on will also be included in the returned Map<String, String>.
        String targetDtmi = "dtmi:com:example:TemperatureController;1";

        // In this case the above dtmi has 2 model dependencies.
        // dtmi:com:example:Thermostat;1 and dtmi:azure:DeviceManagement:DeviceInformation;1
        asyncClient.getModels(targetDtmi)
            .doOnSuccess(aVoid -> System.out.println("Fetched the model and dependencies for: " + targetDtmi))
            .subscribe(res -> System.out.println(String.format("%s resolved in %s interfaces.", targetDtmi, res.size())));
    }
}
