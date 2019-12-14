// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.models.ComplexConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * Sample demonstrates how to use Azure App Configuration to switch between "beta" and "production"
 * configuration sets.
 *
 * <p>
 * In the sample, the user stores a connection string for a resource and a complex configuration object.
 * {@link ComplexConfiguration}
 * The {@link ComplexConfiguration} is serialized into a JSON string and read out from the service as a
 * strongly-typed typed object.
 * </p>
 */
public class ConfigurationSets {
    private static final String CONNECTION_STRING_KEY = "connection-string";
    private static final String COMPLEX_SETTING_KEY = "complex-setting";
    private static final String BETA = "beta";
    private static final String PRODUCTION = "production";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Entry point to the configuration set sample. Creates two sets of configuration values and fetches values from the
     * "beta" configuration set.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException If the service is unable to deserialize the complex configuration object.
     */
    public static void main(String[] args) throws IOException {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};name={secret_value}";

        // Instantiate a configuration client that will be used to call the configuration service.
        ConfigurationAsyncClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        // Demonstrates two different complex objects being stored in Azure App Configuration; one used for beta and the
        // other used for production.
        ComplexConfiguration betaSetting = new ComplexConfiguration().endpointUri("https://beta.endpoint.com").name("beta-name").numberOfInstances(1);
        ComplexConfiguration productionSetting = new ComplexConfiguration().endpointUri("https://production.endpoint.com").name("production-name").numberOfInstances(2);

        // Adding one configuration set for beta testing and another for production to Azure App Configuration.
        // blockLast() is added here to prevent execution before both sets of configuration values have been added to
        // the service.
        Flux.merge(
            addConfigurations(client, BETA, "https://beta-storage.core.windows.net", betaSetting),
            addConfigurations(client, PRODUCTION, "https://production-storage.core.windows.net", productionSetting)
        ).blockLast();

        // For your services, you can select settings with "beta" or "production" label, depending on what you want your
        // services to communicate with. The sample below fetches all of the "beta" settings.
        SettingSelector selector = new SettingSelector().setLabels(BETA);

        client.listConfigurationSettings(selector).toStream().forEach(setting -> {
            System.out.println("Key: " + setting.getKey());
            if ("application/json".equals(setting.getContentType())) {
                try {
                    ComplexConfiguration kv = MAPPER.readValue(setting.getValue(), ComplexConfiguration.class);
                    System.out.println("Value: " + kv.toString());
                } catch (IOException e) {
                    System.err.println(String.format("Could not deserialize %s%n%s", setting.getValue(), e.toString()));
                }
            } else {
                System.out.println("Value: " + setting.getValue());
            }
        });

        // For the BETA and PRODUCTION sets, we fetch all of the settings we created in each set, and delete them.
        // Blocking so that the program does not exit before these tasks have completed.
        Flux.fromArray(new String[]{BETA, PRODUCTION})
            .flatMap(set -> client.listConfigurationSettings(new SettingSelector().setLabels(set)))
            .map(setting -> client.deleteConfigurationSettingWithResponse(setting, false))
            .blockLast();
    }

    /*
     * Adds the "connection-string" and "key-vault" configuration settings
     */
    private static Mono<Void> addConfigurations(ConfigurationAsyncClient client, String configurationSet,
                                                String storageEndpoint, ComplexConfiguration complexConfiguration) throws JsonProcessingException {
        ConfigurationSetting endpointSetting = new ConfigurationSetting()
            .setKey(CONNECTION_STRING_KEY)
            .setLabel(configurationSet)
            .setValue(storageEndpoint);
        ConfigurationSetting keyVaultSetting = new ConfigurationSetting()
            .setKey(COMPLEX_SETTING_KEY)
            .setLabel(configurationSet)
            .setValue(MAPPER.writeValueAsString(complexConfiguration))
            .setContentType("application/json");

        return Flux.merge(client.addConfigurationSetting(keyVaultSetting.getKey(), keyVaultSetting.getLabel(), keyVaultSetting.getValue()),
            client.addConfigurationSetting(endpointSetting.getKey(), endpointSetting.getLabel(), endpointSetting.getValue())).then();
    }
}
