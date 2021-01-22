package com.azure.digitaltwins.core;

    import com.azure.core.http.policy.HttpLogOptions;
    import com.azure.core.http.rest.PagedIterable;
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

    import java.io.IOException;
    import java.net.HttpURLConnection;
    import java.util.*;
    import java.util.function.Function;

public class RelationshipsSyncSamples {
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

        runRelationshipsSample();
    }

    public static void runRelationshipsSample() throws JsonProcessingException {

        ConsoleLogger.printHeader("RELATIONSHIP SAMPLE");

        // For the purpose of keeping code snippets readable to the user, hardcoded string literals are used in place of assigned variables, eg Ids.
        // Despite not being a good code practice, this prevents code snippets from being out of context for the user when making API calls that accept Ids as parameters.

        String sampleBuildingModelId = UniqueIdHelper.getUniqueModelId(SamplesConstants.BUILDING_MODEL_ID, client, randomIntegerStringGenerator);
        String sampleFloorModelId = UniqueIdHelper.getUniqueModelId(SamplesConstants.FLOOR_MODEL_ID, client, randomIntegerStringGenerator);

        String buildingTwinId = UniqueIdHelper.getUniqueDigitalTwinId("buildingTwinId", client, randomIntegerStringGenerator);
        String floorTwinId = UniqueIdHelper.getUniqueDigitalTwinId("floorTwinId", client, randomIntegerStringGenerator);

        final String buildingFloorRelationshipId = "buildingFloorRelationshipId";

        String buildingModelPayload = SamplesConstants.TEMPORARY_MODEL_WITH_RELATIONSHIP_PAYLOAD
            .replace(SamplesConstants.MODEL_ID, sampleBuildingModelId)
            .replace(SamplesConstants.MODEL_DISPLAY_NAME, "Building")
            .replace(SamplesConstants.RELATIONSHIP_NAME, "contains")
            .replace(SamplesConstants.RELATIONSHIP_TARGET_MODEL_ID, sampleFloorModelId);

        String floorModelPayload = SamplesConstants.TEMPORARY_MODEL_WITH_RELATIONSHIP_PAYLOAD
            .replace(SamplesConstants.MODEL_ID, sampleFloorModelId)
            .replace(SamplesConstants.MODEL_DISPLAY_NAME, "Floor")
            .replace(SamplesConstants.RELATIONSHIP_NAME, "containedIn")
            .replace(SamplesConstants.RELATIONSHIP_TARGET_MODEL_ID, sampleBuildingModelId);

        Iterable<DigitalTwinsModelData> createdModels = client.createModels(new ArrayList<>(Arrays.asList(buildingModelPayload, floorModelPayload)));

        for (DigitalTwinsModelData model : createdModels) {
            ConsoleLogger.print("Created model " + model.getModelId());
        }

        // Create a building digital twin
        BasicDigitalTwin buildingDigitalTwin = new BasicDigitalTwin(buildingTwinId)
            .setMetadata(new BasicDigitalTwinMetadata()
                .setModelId(sampleBuildingModelId));

        client.createOrReplaceDigitalTwin(buildingTwinId, buildingDigitalTwin, BasicDigitalTwin.class);

        ConsoleLogger.print("Created twin" + buildingDigitalTwin.getId());

        BasicDigitalTwin floorDigitalTwin = new BasicDigitalTwin(floorTwinId)
            .setMetadata(new BasicDigitalTwinMetadata()
                .setModelId(sampleFloorModelId));

        BasicDigitalTwin createdTwin = client.createOrReplaceDigitalTwin(floorTwinId, floorDigitalTwin, BasicDigitalTwin.class);

        ConsoleLogger.print("Created twin with Id:" + createdTwin.getId());

        ConsoleLogger.printHeader("Create relationships");

        BasicRelationship buildingFloorRelationshipPayload = new BasicRelationship(buildingFloorRelationshipId, buildingTwinId, floorTwinId, "contains")
            .addProperty("Prop1", "Prop1 value")
            .addProperty("Prop2", 6);

        client.createOrReplaceRelationship(buildingTwinId, buildingFloorRelationshipId, buildingFloorRelationshipPayload, BasicRelationship.class);

        ConsoleLogger.printSuccess("Created a digital twin relationship "+ buildingFloorRelationshipId + " from twin: " + buildingTwinId + " to twin: " + floorTwinId);

        ConsoleLogger.printHeader("Get Relationship");
        Response<BasicRelationship> getRelationshipResponse = client.getRelationshipWithResponse(
            buildingTwinId,
            buildingFloorRelationshipId,
            BasicRelationship.class,
            Context.NONE);

        if (getRelationshipResponse.getStatusCode() == HttpURLConnection.HTTP_OK) {
            BasicRelationship retrievedRelationship = getRelationshipResponse.getValue();
            ConsoleLogger.printSuccess("Retrieved relationship: " + retrievedRelationship.getId() + " from twin: " + retrievedRelationship.getSourceId() + "\n\t" +
                "Prop1: " + retrievedRelationship.getProperties().get("Prop1") + "\n\t" +
                "Prop2: " + retrievedRelationship.getProperties().get("Prop2") + "\n");

            ConsoleLogger.printSuccess("Retrieved relationship has ETag: " + retrievedRelationship.getETag() + "\n\t");
        }

        ConsoleLogger.printHeader("List relationships");

        PagedIterable<BasicRelationship> relationshipPages = client.listRelationships(buildingTwinId, BasicRelationship.class);

        for (BasicRelationship relationship : relationshipPages) {
            ConsoleLogger.printSuccess("Retrieved relationship: " + relationship.getId() + " with source: " + relationship.getSourceId() + " and target: " + relationship.getTargetId());
        }

        ConsoleLogger.printHeader("List incoming relationships");
        // Get all incoming relationships in the graph where floorTwinId is the target of the relationship.

        PagedIterable<IncomingRelationship> incomingRelationships = client.listIncomingRelationships(floorTwinId, Context.NONE);

        for (IncomingRelationship incomingRelationship : incomingRelationships) {
            ConsoleLogger.printSuccess("Found an incoming relationship: " + incomingRelationship.getRelationshipId() + " from: " + incomingRelationship.getSourceId());
        }

        // Delete the contains relationship, created earlier in the sample code, from building to floor.

        ConsoleLogger.printHeader("Delete relationship");

        client.deleteRelationship(buildingTwinId, buildingFloorRelationshipId);
        ConsoleLogger.printSuccess("Deleted relationship: " + buildingFloorRelationshipId);

        // Clean up
        try {
            // Delete all twins
            client.deleteDigitalTwin(buildingTwinId);
            client.deleteDigitalTwin(floorTwinId);
        }
        catch (ErrorResponseException ex) {
            ConsoleLogger.printFatal("Failed to delete digital twin due to" + ex);
        }

        try {
            // Delete all models
            client.deleteModel(sampleBuildingModelId);
            client.deleteModel(sampleFloorModelId);
        }
        catch (ErrorResponseException ex) {
            ConsoleLogger.printFatal("Failed to delete models due to" + ex);
        }
    }
}
