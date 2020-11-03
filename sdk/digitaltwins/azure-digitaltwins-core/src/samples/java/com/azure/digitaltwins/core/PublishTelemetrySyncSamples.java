package com.azure.digitaltwins.core;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.digitaltwins.core.helpers.ConsoleLogger;
import com.azure.digitaltwins.core.helpers.SamplesArguments;
import com.azure.digitaltwins.core.helpers.SamplesConstants;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.digitaltwins.core.models.*;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class PublishTelemetrySyncSamples {
    private static DigitalTwinsClient client;
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Function<Integer, String> randomIntegerStringGenerator = (maxLength) -> {
        int randInt = new Random().nextInt((int)Math.pow(10, 8) - 1) + 1;
        return String.valueOf(randInt);
    };

    public static void main(String[] args) throws IOException {

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

        runPublishTelemetrySample();
    }

    public static void runPublishTelemetrySample() throws JsonProcessingException {

        ConsoleLogger.printHeader("Telemetry Samples");

        // For the purpose of this example we will create temporary models using a random model Ids.
        // We have to make sure these model Ids are unique within the DT instance.

        String componentModelId = UniqueIdHelper.getUniqueModelId(SamplesConstants.TEMPORARY_COMPONENT_MODEL_PREFIX, client, randomIntegerStringGenerator);
        String modelId = UniqueIdHelper.getUniqueModelId(SamplesConstants.TEMPORARY_MODEL_PREFIX, client, randomIntegerStringGenerator);
        String digitalTwinId = UniqueIdHelper.getUniqueDigitalTwinId(SamplesConstants.TEMPORARY_TWIN_PREFIX, client, randomIntegerStringGenerator);

        String newComponentModelPayload = SamplesConstants.TEMPORARY_COMPONENT_MODEL_PAYLOAD
            .replace(SamplesConstants.COMPONENT_ID, componentModelId);

        String newModelPayload = SamplesConstants.TEMPORARY_MODEL_WITH_COMPONENT_PAYLOAD
            .replace(SamplesConstants.MODEL_ID, modelId)
            .replace(SamplesConstants.COMPONENT_ID, componentModelId);

        List<String> modelsList = new ArrayList<>(Arrays.asList(newComponentModelPayload, newModelPayload));

        ConsoleLogger.printHeader("Create Models");
        // We now create all the models (including components)
        Iterable<DigitalTwinsModelData> modelList =  client.createModels(modelsList);

        for (DigitalTwinsModelData model : modelList) {
            ConsoleLogger.print("Created model: " + model.getModelId());
        }

        ConsoleLogger.printHeader("Create DigitalTwin");

        String twinPayload = SamplesConstants.TEMPORARY_TWIN_PAYLOAD
            .replace(SamplesConstants.MODEL_ID, modelId);

        String digitalTwinResponse = client.createOrReplaceDigitalTwin(digitalTwinId, twinPayload, String.class);

        ConsoleLogger.printSuccess("Created digital twin with Id: " + digitalTwinId + "\n" + digitalTwinResponse);

        try
        {
            ConsoleLogger.printHeader("Publish Telemetry");
            // construct your json telemetry payload by hand.
            client.publishTelemetry(digitalTwinId, null,"{\"Telemetry1\": 5}");
            ConsoleLogger.print("Published telemetry message to twin " + digitalTwinId);

            ConsoleLogger.printHeader("Publish Component Telemetry");

            // construct your json telemetry payload using a hashtable.
            Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
            telemetryPayload.put("ComponentTelemetry1", 9);

            Response<Void> publishComponentTelemetryResponse = client.publishComponentTelemetryWithResponse(
                digitalTwinId,
                "Component1",
                null,
                telemetryPayload,
                null,
                Context.NONE);

            ConsoleLogger.printSuccess("Published component telemetry message to twin " + digitalTwinId);
        }
        catch (Exception ex)
        {
            ConsoleLogger.printFatal("Failed to publish a telemetry message due to:\n" + ex.getMessage());
        }

        try
        {
            // Delete the twin.
            client.deleteDigitalTwin(digitalTwinId);

            // Delete the models.
            client.deleteModel(modelId);
            client.deleteModel(componentModelId);
        }
        catch (ErrorResponseException ex)
        {
            if (ex.getResponse().getStatusCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                ConsoleLogger.printWarning("Digital twin or models do not exist.");
                System.exit(1);
            }

            ConsoleLogger.printFatal("Failed to delete due to:\n" + ex.getMessage());
        }
    }
}
