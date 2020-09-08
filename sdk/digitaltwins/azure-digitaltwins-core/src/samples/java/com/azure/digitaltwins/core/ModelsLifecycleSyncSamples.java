package com.azure.digitaltwins.core;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.digitaltwins.core.helpers.ConsoleLogger;
import com.azure.digitaltwins.core.helpers.SamplesArguments;
import com.azure.digitaltwins.core.helpers.SamplesConstants;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.digitaltwins.core.models.ModelData;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Creates a component with a random Id.
 * Creates a new model with a random Id that uses the previously created component.
 * Decommission the newly created models (model and component) and check for success.
 * Delete all created models and components.
 */
public class ModelsLifecycleSyncSamples {

    private static DigitalTwinsClient client;

    public static void main(String[] args) throws IOException, InterruptedException {

        SamplesArguments parsedArguments = new SamplesArguments(args);

        client = new DigitalTwinsClientBuilder()
            .tokenCredential(
                new ClientSecretCredentialBuilder()
                    .tenantId(parsedArguments.getTenantId())
                    .clientId(parsedArguments.getClientId())
                    .clientSecret(parsedArguments.getClientSecret())
                    .build()
            )
            .endpoint(parsedArguments.getDigitalTwinEndpoint())
            .httpLogOptions(
                new HttpLogOptions()
                    .setLogLevel(parsedArguments.getHttpLogDetailLevel()))
            .buildClient();

        runModelLifecycleSample();
    }

    public static void runModelLifecycleSample() {
        // For the purpose of this sample we will create temporary models using random model Ids and then decommission a model.
        // We have to make sure these model Ids are unique within the DigitalTwin instance.
        String componentModelId = UniqueIdHelper.getUniqueModelId(SamplesConstants.TemporaryComponentModelPrefix, client);
        String sampleModelId = UniqueIdHelper.getUniqueModelId(SamplesConstants.TemporaryModelPrefix, client);

        String newComponentModelPayload = SamplesConstants.TemporaryComponentModelPayload
            .replace(SamplesConstants.ComponentId, componentModelId);

        String newModelPayload = SamplesConstants.TemporaryModelWithComponentPayload
            .replace(SamplesConstants.ModelId, sampleModelId)
            .replace(SamplesConstants.ComponentId, componentModelId);

        ConsoleLogger.PrintHeader("Create models");

        try {
            client.createModels(new ArrayList<String>(Arrays.asList(newComponentModelPayload, newModelPayload)));

            ConsoleLogger.PrintSuccess("Created models " + componentModelId + " and " + sampleModelId);
        }
        catch (ErrorResponseException ex) {
            if (ex.getResponse().getStatusCode() == HttpURLConnection.HTTP_CONFLICT) {
                ConsoleLogger.PrintWarning("One or more models already existed");
            }
        }
        catch (Exception ex) {
            ConsoleLogger.PrintFatal("Failed to create models due to: \n" + ex);
            System.exit(0);
        }

        ConsoleLogger.PrintHeader("Get models");

        try {
            ModelData sampleModelResponse = client.getModel(sampleModelId);
            ConsoleLogger.PrintSuccess("Retrieved model " + sampleModelResponse.getId());
        }
        catch (Exception ex) {
            ConsoleLogger.PrintFatal("Failed to get the model due to:\n" + ex);
            System.exit(0);
        }

        ConsoleLogger.PrintHeader("Decommission models");

        try {
            client.decommissionModel(sampleModelId);
            client.decommissionModel(componentModelId);

            ConsoleLogger.PrintSuccess("Decommissioned "+ sampleModelId + " and " + componentModelId);
        }
        catch (Exception ex) {
            ConsoleLogger.PrintFatal("Failed to decommission models due to:\n" + ex);
            System.exit(0);
        }

        ConsoleLogger.PrintHeader("Delete models");

        try {
            client.deleteModel(sampleModelId);
            client.deleteModel(componentModelId);

            ConsoleLogger.PrintSuccess("Deleted "+ sampleModelId + " and " + componentModelId);
        }
        catch (Exception ex) {
            ConsoleLogger.PrintFatal("Failed to deleteModel models due to:\n" + ex);
            System.exit(0);
        }
    }
}
