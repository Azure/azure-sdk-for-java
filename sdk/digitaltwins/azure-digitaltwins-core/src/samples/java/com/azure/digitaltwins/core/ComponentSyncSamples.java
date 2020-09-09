package com.azure.digitaltwins.core;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.digitaltwins.core.helpers.ConsoleLogger;
import com.azure.digitaltwins.core.helpers.SamplesArguments;
import com.azure.digitaltwins.core.helpers.SamplesConstants;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.digitaltwins.core.serialization.BasicDigitalTwin;
import com.azure.digitaltwins.core.serialization.DigitalTwinMetadata;
import com.azure.digitaltwins.core.serialization.ModelProperties;
import com.azure.digitaltwins.core.util.UpdateOperationUtility;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.util.*;

public class ComponentSyncSamples {
    private static DigitalTwinsClient client;
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {

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

        runComponentSample();
    }

    @SuppressWarnings("rawtypes")
    public static void runComponentSample() throws JsonProcessingException {

        ConsoleLogger.printHeader("COMPONENT SAMPLES");

        // For the purpose of this example we will create temporary models using a random model Ids.
        // We have to make sure these model Ids are unique within the DT instance.

        String componentModelId = UniqueIdHelper.getUniqueModelId(SamplesConstants.TemporaryComponentModelPrefix, client);
        String modelId = UniqueIdHelper.getUniqueModelId(SamplesConstants.TemporaryModelPrefix, client);
        String basicDigitalTwinId = UniqueIdHelper.getUniqueDigitalTwinId(SamplesConstants.TemporaryTwinPrefix, client);

        String newComponentModelPayload = SamplesConstants.TemporaryComponentModelPayload
            .replace(SamplesConstants.ComponentId, componentModelId);

        String newModelPayload = SamplesConstants.TemporaryModelWithComponentPayload
            .replace(SamplesConstants.ModelId, modelId)
            .replace(SamplesConstants.ComponentId, componentModelId);

        List<String> modelsList = new ArrayList<>(Arrays.asList(newComponentModelPayload, newModelPayload));

        ConsoleLogger.printHeader("Create Models");
        // We now create all the models (including components)
        client.createModels(modelsList);

        ConsoleLogger.print("Created models: " + componentModelId + " and " + modelId);

        ConsoleLogger.printHeader("Create digital twin with components");
        // Create digital twin with component payload using the BasicDigitalTwin serialization helper.
        BasicDigitalTwin basicTwin = new BasicDigitalTwin()
            .setId(basicDigitalTwinId)
            .setMetadata(
                new DigitalTwinMetadata()
                    .setModelId(modelId)
            )
            .setCustomProperties("Prop1", "Value1")
            .setCustomProperties("Prop2", 987)
            .setCustomProperties(
                "Component1",
                new ModelProperties()
                    .setCustomProperties("ComponentProp1", "Component value 1")
                    .setCustomProperties("ComponentProp2", 123)
            );

        String basicDigitalTwinPayload = mapper.writeValueAsString(basicTwin);

        client.createDigitalTwin(basicDigitalTwinId, basicDigitalTwinPayload);

        ConsoleLogger.print("Created digital twin " + basicDigitalTwinId);

        // You can get a digital twin in json string format and deserialize it on your own
        Response<String> getStringDigitalTwinResponse = client.getDigitalTwinWithResponse(basicDigitalTwinId, Context.NONE);
        ConsoleLogger.print("Successfully retrieved digital twin as a json string \n" + getStringDigitalTwinResponse.getValue());

        BasicDigitalTwin deserializedDigitalTwin = mapper.readValue(getStringDigitalTwinResponse.getValue(), BasicDigitalTwin.class);
        ConsoleLogger.print("Deserialized the string response into a BasicDigitalTwin with Id: " + deserializedDigitalTwin.getId());

        // You can also get a digital twin using the built in deserializer into a BasicDigitalTwin.
        // It works well for basic stuff, but as you can see it gets more difficult when delving into
        // more complex properties, like components.
        Response<BasicDigitalTwin> basicDigitalTwinResponse = client.getDigitalTwinWithResponse(basicDigitalTwinId, BasicDigitalTwin.class, Context.NONE);

        if (basicDigitalTwinResponse.getStatusCode() == HttpsURLConnection.HTTP_OK) {

            BasicDigitalTwin basicDigitalTwin = basicDigitalTwinResponse.getValue();

            String component1RawText = mapper.writeValueAsString(basicDigitalTwin.getCustomProperties().get("Component1"));

            HashMap component1 = mapper.readValue(component1RawText, HashMap.class);

            ConsoleLogger.print("Retrieved digital twin using generic API to use built in deserialization into a BasicDigitalTwin with Id: " + basicDigitalTwin.getId() + ":\n\t"
                + "Etag: " + basicDigitalTwin.getTwinETag() + "\n\t"
                + "Prop1: " + basicDigitalTwin.getCustomProperties().get("Prop1") + "\n\t"
                + "Prop2: " + basicDigitalTwin.getCustomProperties().get("Prop2") + "\n\t"
                + "ComponentProp1: " + component1.get("ComponentProp1") + "\n\t"
                + "ComponentProp2: " + component1.get("ComponentProp2") + "\n\t"
            );
        }

        ConsoleLogger.printHeader("Update Component");

        // Update Component1 by replacing the property ComponentProp1 value,
        // using the UpdateOperationUtility to build the payload.
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();

        updateOperationUtility.appendReplaceOperation("/ComponentProp1", "Some new Value");

        client.updateComponent(basicDigitalTwinId, "Component1", updateOperationUtility.getUpdateOperations());

        ConsoleLogger.print("Updated component for digital twin: " + basicDigitalTwinId);

        ConsoleLogger.printHeader("Get Component");
        String getComponentResponse = client.getComponent(basicDigitalTwinId, "Component1");
        ConsoleLogger.print("Retrieved component for digital twin " + basicDigitalTwinId + " :\n" + getComponentResponse);

        // Clean up
        try {
            client.deleteDigitalTwin(basicDigitalTwinId);
        }
        catch (ErrorResponseException ex) {
            ConsoleLogger.printFatal("Failed to delete digital twin due to" + ex);
        }

        try {
            client.deleteModel(modelId);
            client.deleteModel(componentModelId);
        }
        catch (ErrorResponseException ex) {
            ConsoleLogger.printFatal("Failed to delete models due to" + ex);
        }
    }
}
