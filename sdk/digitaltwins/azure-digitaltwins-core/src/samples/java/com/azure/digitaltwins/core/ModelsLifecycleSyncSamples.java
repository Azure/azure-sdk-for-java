package com.azure.digitaltwins.core;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.digitaltwins.core.helpers.ConsoleLogger;
import com.azure.digitaltwins.core.helpers.SamplesArguments;
import com.azure.digitaltwins.core.helpers.SamplesConstants;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.digitaltwins.core.models.DigitalTwinsModelData;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

/**
 * Creates a component with a random Id.
 * Creates a new model with a random Id that uses the previously created component.
 * Decommission the newly created models (model and component) and check for success.
 * Delete all created models and components.
 */
public class ModelsLifecycleSyncSamples {

    private static DigitalTwinsClient client;

    public static void main(String[] args) {

        SamplesArguments parsedArguments = new SamplesArguments(args);

        client = new DigitalTwinsClientBuilder()
            .credential(
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

    public static Function<Integer, String> randomIntegerStringGenerator = (maxLength) -> {
        int randInt = new Random().nextInt(maxLength);
        return String.valueOf(randInt);
    };

    public static void runModelLifecycleSample() {
        // For the purpose of this sample we will create temporary models using random model Ids and then decommission a model.
        // We have to make sure these model Ids are unique within the DigitalTwin instance.
        String componentModelId = UniqueIdHelper.getUniqueModelId(SamplesConstants.TEMPORARY_COMPONENT_MODEL_PREFIX, client, randomIntegerStringGenerator);
        String sampleModelId = UniqueIdHelper.getUniqueModelId(SamplesConstants.TEMPORARY_MODEL_PREFIX, client, randomIntegerStringGenerator);

        String newComponentModelPayload = SamplesConstants.TEMPORARY_COMPONENT_MODEL_PAYLOAD
            .replace(SamplesConstants.COMPONENT_ID, componentModelId);

        String newModelPayload = SamplesConstants.TEMPORARY_MODEL_WITH_COMPONENT_PAYLOAD
            .replace(SamplesConstants.MODEL_ID, sampleModelId)
            .replace(SamplesConstants.COMPONENT_ID, componentModelId);

        ConsoleLogger.printHeader("Create models");

        try {
            client.createModels(new ArrayList<>(Arrays.asList(newComponentModelPayload, newModelPayload)));

            ConsoleLogger.print("Created models " + componentModelId + " and " + sampleModelId);
        }
        catch (ErrorResponseException ex) {
            if (ex.getResponse().getStatusCode() == HttpURLConnection.HTTP_CONFLICT) {
                ConsoleLogger.printWarning("One or more models already existed");
            }
        }
        catch (Exception ex) {
            ConsoleLogger.printFatal("Failed to create models due to: \n" + ex);
            System.exit(0);
        }

        ConsoleLogger.printHeader("Get models");

        try {
            DigitalTwinsModelData sampleModelResponse = client.getModel(sampleModelId);
            ConsoleLogger.print("Retrieved model " + sampleModelResponse.getModelId());
        }
        catch (Exception ex) {
            ConsoleLogger.printFatal("Failed to get the model due to:\n" + ex);
            System.exit(0);
        }

        ConsoleLogger.printHeader("Decommission models");

        try {
            client.decommissionModel(sampleModelId);
            client.decommissionModel(componentModelId);

            ConsoleLogger.print("Decommissioned "+ sampleModelId + " and " + componentModelId);
        }
        catch (Exception ex) {
            ConsoleLogger.printFatal("Failed to decommission models due to:\n" + ex);
            System.exit(0);
        }

        ConsoleLogger.printHeader("Delete models");

        try {
            client.deleteModel(sampleModelId);
            client.deleteModel(componentModelId);

            ConsoleLogger.print("Deleted "+ sampleModelId + " and " + componentModelId);
        }
        catch (Exception ex) {
            ConsoleLogger.printFatal("Failed to deleteModel models due to:\n" + ex);
            System.exit(0);
        }
    }
}
