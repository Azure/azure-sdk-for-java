// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.models.JsonPatchDocument;
import com.azure.core.util.Context;
import com.azure.digitaltwins.core.helpers.ConsoleLogger;
import com.azure.digitaltwins.core.helpers.SamplesArguments;
import com.azure.digitaltwins.core.helpers.SamplesConstants;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.digitaltwins.core.models.DigitalTwinsModelData;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class ComponentSyncSamples {
    private static DigitalTwinsClient client;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static Function<Integer, String> randomIntegerStringGenerator;

    static {
        randomIntegerStringGenerator = (maxLength) -> {
            int randInt = new Random().nextInt((int) Math.pow(10, 8) - 1) + 1;
            return String.valueOf(randInt);
        };
    }

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

        // This mapper gets used to deserialize a digital twin that has a date time within a property metadata, so it
        // needs to have this module in order to correctly deserialize that date time
        MAPPER.registerModule(new JavaTimeModule());

        runComponentSample();
    }

    @SuppressWarnings("rawtypes")
    public static void runComponentSample() throws JsonProcessingException {

        ConsoleLogger.printHeader("COMPONENT SAMPLES");

        // For the purpose of this example we will create temporary models using a random model Ids.
        // We have to make sure these model Ids are unique within the DT instance.

        String componentModelId = UniqueIdHelper.getUniqueModelId(SamplesConstants.TEMPORARY_COMPONENT_MODEL_PREFIX, client, randomIntegerStringGenerator);
        String modelId = UniqueIdHelper.getUniqueModelId(SamplesConstants.TEMPORARY_MODEL_PREFIX, client, randomIntegerStringGenerator);
        String basicDigitalTwinId = UniqueIdHelper.getUniqueDigitalTwinId(SamplesConstants.TEMPORARY_TWIN_PREFIX, client, randomIntegerStringGenerator);

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

        ConsoleLogger.printHeader("Create digital twin with components");
        // Create digital twin with component payload using the BasicDigitalTwin serialization helper.
        BasicDigitalTwin basicTwin = new BasicDigitalTwin(basicDigitalTwinId)
            .setMetadata(
                new BasicDigitalTwinMetadata()
                    .setModelId(modelId)
            )
            .addToContents("Prop1", "Value1")
            .addToContents("Prop2", 987)
            .addToContents(
                "Component1",
                new BasicDigitalTwinComponent()
                    .addToContents("ComponentProp1", "Component value 1")
                    .addToContents("ComponentProp2", 123)
            );

        BasicDigitalTwin basicTwinResponse = client.createOrReplaceDigitalTwin(basicDigitalTwinId, basicTwin, BasicDigitalTwin.class);

        ConsoleLogger.print("Created digital twin " + basicTwinResponse.getId());

        // You can get a digital twin in json string format and deserialize it on your own
        Response<String> getStringDigitalTwinResponse = client.getDigitalTwinWithResponse(basicDigitalTwinId, String.class, Context.NONE);
        ConsoleLogger.print("Successfully retrieved digital twin as a json string \n" + getStringDigitalTwinResponse.getValue());

        BasicDigitalTwin deserializedDigitalTwin = MAPPER.readValue(getStringDigitalTwinResponse.getValue(), BasicDigitalTwin.class);
        ConsoleLogger.print("Deserialized the string response into a BasicDigitalTwin with Id: " + deserializedDigitalTwin.getId());

        // You can also get a digital twin using the built in deserializer into a BasicDigitalTwin.
        // It works well for basic stuff, but as you can see it gets more difficult when delving into
        // more complex properties, like components.
        Response<BasicDigitalTwin> basicDigitalTwinResponse = client.getDigitalTwinWithResponse(basicDigitalTwinId, BasicDigitalTwin.class, Context.NONE);

        if (basicDigitalTwinResponse.getStatusCode() == HttpsURLConnection.HTTP_OK) {

            BasicDigitalTwin basicDigitalTwin = basicDigitalTwinResponse.getValue();

            String component1RawText = MAPPER.writeValueAsString(basicDigitalTwin.getContents().get("Component1"));

            HashMap component1 = MAPPER.readValue(component1RawText, HashMap.class);

            ConsoleLogger.print("Retrieved digital twin using generic API to use built in deserialization into a BasicDigitalTwin with Id: " + basicDigitalTwin.getId() + ":\n\t"
                + "ETag: " + basicDigitalTwin.getETag() + "\n\t"
                + "Prop1: " + basicDigitalTwin.getContents().get("Prop1") + "\n\t"
                + "Prop2: " + basicDigitalTwin.getContents().get("Prop2") + "\n\t"
                + "ComponentProp1: " + component1.get("ComponentProp1") + "\n\t"
                + "ComponentProp2: " + component1.get("ComponentProp2") + "\n\t"
            );
        }

        ConsoleLogger.printHeader("Update Component");

        // Update Component1 by replacing the property ComponentProp1 value,
        // using the UpdateOperationUtility to build the payload.
        JsonPatchDocument updateOp = new JsonPatchDocument().appendReplace("/ComponentProp1", "Some new Value");

        client.updateComponent(basicDigitalTwinId, "Component1", updateOp);

        ConsoleLogger.print("Updated component for digital twin: " + basicDigitalTwinId);

        ConsoleLogger.printHeader("Get Component");
        BasicDigitalTwinComponent getComponentResponse = client.getComponent(basicDigitalTwinId, "Component1", BasicDigitalTwinComponent.class);

        ConsoleLogger.print("Retrieved component for digital twin " + basicDigitalTwinId + " :");
        for (String key : getComponentResponse.getContents().keySet()) {
            ConsoleLogger.print("\t" + key + " : " + getComponentResponse.getContents().get(key));
            ConsoleLogger.print("\t\tLast updated on: " + getComponentResponse.getMetadata().get(key).getLastUpdatedOn());
        }

        // Clean up
        try {
            client.deleteDigitalTwin(basicDigitalTwinId);
        } catch (ErrorResponseException ex) {
            ConsoleLogger.printFatal("Failed to delete digital twin due to" + ex);
        }

        try {
            client.deleteModel(modelId);
            client.deleteModel(componentModelId);
        } catch (ErrorResponseException ex) {
            ConsoleLogger.printFatal("Failed to delete models due to" + ex);
        }
    }
}
