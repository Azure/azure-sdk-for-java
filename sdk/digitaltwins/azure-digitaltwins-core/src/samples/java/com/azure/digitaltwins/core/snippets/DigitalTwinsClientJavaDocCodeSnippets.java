package com.azure.digitaltwins.core.snippets;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.digitaltwins.core.DigitalTwinsAsyncClient;
import com.azure.digitaltwins.core.DigitalTwinsClient;
import com.azure.digitaltwins.core.DigitalTwinsClientBuilder;
import com.azure.digitaltwins.core.models.*;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Code snippets for {@link DigitalTwinsClient}
 */
public class DigitalTwinsClientJavaDocCodeSnippets extends CodeSnippetBase {

    private final DigitalTwinsClient digitalTwinsSyncClient;

    DigitalTwinsClientJavaDocCodeSnippets(){
        digitalTwinsSyncClient = createDigitalTwinsClient();
    }

    public DigitalTwinsClient createDigitalTwinsClient() {

        String tenantId = getTenenatId();
        String clientId = getClientId();
        String clientSecret = getClientSecret();
        String digitalTwinsEndpointUrl = getEndpointUrl();

        // BEGIN: com.azure.digitaltwins.core.syncClient.instantiation
        DigitalTwinsClient digitalTwinsSyncClient = new DigitalTwinsClientBuilder()
            .credential(
                new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build())
            .endpoint(digitalTwinsEndpointUrl)
            .buildClient();
        // END: com.azure.digitaltwins.core.syncClient.instantiation

        return digitalTwinsSyncClient;
    }

    //region DigitalTwinSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsClient#createDigitalTwin(String, Object, Class)}
     */
    @Override
    public void createDigitalTwin() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncClient.createDigitalTwins#String-Object-Class#BasicDigitalTwin
        String modelId = "dtmi:com:samples:Building;1";

        BasicDigitalTwin basicTwin = new BasicDigitalTwin("myDigitalTwinId")
            .setMetadata(
                new DigitalTwinMetadata()
                    .setModelId(modelId)
            );

        BasicDigitalTwin createdTwin = digitalTwinsClient.createDigitalTwin(
            basicTwin.getId(),
            basicTwin,
            BasicDigitalTwin.class);

        System.out.println("Created digital twin with Id: " + createdTwin.getId());
        // END: com.azure.digitaltwins.core.syncClient.createDigitalTwins#String-Object-Class#BasicDigitalTwin

        String digitalTwinStringPayload = getDigitalTwinPayload();

        // BEGIN: com.azure.digitaltwins.core.syncClient.createDigitalTwins#String-Object-Class#String
        String stringResult = digitalTwinsClient.createDigitalTwin(
            "myDigitalTwinId",
            digitalTwinStringPayload,
            String.class);
        System.out.println("Created digital twin: " + stringResult);
        // END: com.azure.digitaltwins.core.syncClient.createDigitalTwins#String-Object-Class#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#createDigitalTwinWithResponse(String, Object, Class, CreateDigitalTwinOptions, Context)}
     */
    @Override
    public void createDigitalTwinWithResponse() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncClient.createDigitalTwinsWithResponse#String-Object-Class-Options-Context#BasicDigitalTwin
        String modelId = "dtmi:com:samples:Building;1";

        BasicDigitalTwin basicDigitalTwin = new BasicDigitalTwin("myDigitalTwinId")
            .setMetadata(
                new DigitalTwinMetadata()
                    .setModelId(modelId)
            );

        Response<BasicDigitalTwin> resultWithResponse = digitalTwinsClient.createDigitalTwinWithResponse(
            basicDigitalTwin.getId(),
            basicDigitalTwin,
            BasicDigitalTwin.class,
            new CreateDigitalTwinOptions(),
            new Context("Key", "Value"));

        System.out.println("Response http status: "
            + resultWithResponse.getStatusCode() +
            " created digital twin Id: " +
            resultWithResponse.getValue().getId());
        // END: com.azure.digitaltwins.core.syncClient.createDigitalTwinsWithResponse#String-Object-Class-Options-Context#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.syncClient.createDigitalTwinsWithResponse#String-Object-Class-Options-Context#String
        String stringPayload = getDigitalTwinPayload();

        Response<String> stringResultWithResponse = digitalTwinsClient.createDigitalTwinWithResponse(
            basicDigitalTwin.getId(),
            stringPayload,
            String.class,
            new CreateDigitalTwinOptions(),
            new Context("Key", "Value"));

        System.out.println(
            "Response http status: " +
            stringResultWithResponse.getStatusCode() +
            " created digital twin: " +
            stringResultWithResponse.getValue());
        // END: com.azure.digitaltwins.core.syncClient.createDigitalTwinsWithResponse#String-Object-Class-Options-Context#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#getDigitalTwin(String, Class)}
     */
    @Override
    public void getDigitalTwin() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncClient.getDigitalTwin#String-Class#BasicDigitalTwin
        BasicDigitalTwin basicTwinResult = digitalTwinsClient.getDigitalTwin(
            "myDigitalTwinId",
            BasicDigitalTwin.class);

        System.out.println("Retrieved digital twin with Id: " + basicTwinResult.getId());
        // END: com.azure.digitaltwins.core.syncClient.getDigitalTwin#String-Class#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.syncClient.getDigitalTwin#String-Class#String
        String stringResult = digitalTwinsClient.getDigitalTwin("myDigitalTwinId", String.class);

        System.out.println("Retrieved digital twin: " + stringResult);
        // END: com.azure.digitaltwins.core.syncClient.getDigitalTwin#String-Class#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#getDigitalTwinWithResponse(String, Class, GetDigitalTwinOptions, Context)}
     */
    @Override
    public void getDigitalTwinWithResponse() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncClient.getDigitalTwinWithResponse#String-Class-Options-Context#BasicDigitalTwin
        Response<BasicDigitalTwin> basicTwinResultWithResponse = digitalTwinsClient.getDigitalTwinWithResponse(
            "myDigitalTwinId",
            BasicDigitalTwin.class,
            new GetDigitalTwinOptions(),
            new Context("key", "value"));

        System.out.println("Http status code: " + basicTwinResultWithResponse.getStatusCode());
        System.out.println("Retrieved digital twin with Id: " + basicTwinResultWithResponse.getValue().getId());
        // END: com.azure.digitaltwins.core.syncClient.getDigitalTwinWithResponse#String-Class-Options-Context#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.syncClient.getDigitalTwinWithResponse#String-Class-Options-Context#String
        Response<String> stringResultWithResponse = digitalTwinsClient.getDigitalTwinWithResponse(
            "myDigitalTwinId",
            String.class,
            new GetDigitalTwinOptions(),
            new Context("key", "value"));

        System.out.println("Http response status: " + stringResultWithResponse.getStatusCode());
        System.out.println("Retrieved digital twin: " + stringResultWithResponse.getValue());
        // END: com.azure.digitaltwins.core.syncClient.getDigitalTwinWithResponse#String-Class-Options-Context#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#updateDigitalTwin(String, List)}
     */
    @Override
    public void updateDigitalTwin() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncClient.updateDigitalTwin#String-List
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("Prop1", "newValue");

        digitalTwinsClient.updateDigitalTwin(
            "myDigitalTwinId",
            updateOperationUtility.getUpdateOperations());
        // END: com.azure.digitaltwins.core.syncClient.updateDigitalTwin#String-List
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#updateDigitalTwinWithResponse(String, List, UpdateDigitalTwinOptions, Context)}
     */
    @Override
    public void updateDigitalTwinWithResponse() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncClient.updateDigitalTwinWithResponse#String-List-Options-Context
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("Prop1", "newValue");

        Response<Void> response = digitalTwinsClient.updateDigitalTwinWithResponse(
            "myDigitalTwinId",
            updateOperationUtility.getUpdateOperations(),
            new UpdateDigitalTwinOptions(),
            new Context("key", "value"));

        System.out.println("Update completed with HTTP status code: " + response.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.updateDigitalTwinWithResponse#String-List-Options-Context
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#deleteDigitalTwin(String)}
     */
    @Override
    public void deleteDigitalTwin() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncClient.deleteDigitalTwin#String
        digitalTwinsClient.deleteDigitalTwin("myDigitalTwinId");
        // END: com.azure.digitaltwins.core.syncClient.deleteDigitalTwin#String
    }

    /**
     * Generates code samples for using
     * @link DigitalTwinsClient#deleteDigitalTwinWithResponse(String, DeleteDigitalTwinOptions, Context)}
     */
    @Override
    public void deleteDigitalTwinWithResponse() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncClient.deleteDigitalTwinWithResponse#String-Options-Context
        Response<Void> response = digitalTwinsClient.deleteDigitalTwinWithResponse(
            "myDigitalTwinId",
            new DeleteDigitalTwinOptions(),
            new Context("key", "value"));

        System.out.println("Deleted digital twin HTTP response status code: " + response.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.deleteDigitalTwinWithResponse#String-Options-Context
    }
    //endregion DigitalTwinSnippets

    //region RelationshipSnippets

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#createRelationship(String, String, Object, Class)}
     */
    @Override
    public void createRelationship() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.createRelationship#String-String-Object-Class#BasicRelationship
        BasicRelationship buildingToFloorBasicRelationship = new BasicRelationship(
                "myRelationshipId",
                "mySourceDigitalTwinId",
                "myTargetDigitalTwinId",
                "contains")
            .addCustomProperty("Prop1", "Prop1 value")
            .addCustomProperty("Prop2", 6);

        BasicRelationship createdRelationship = digitalTwinsSyncClient.createRelationship(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            buildingToFloorBasicRelationship,
            BasicRelationship.class);

        System.out.println(
            "Created relationship with Id: " +
            createdRelationship.getRelationshipId() +
            " from: " + createdRelationship.getSourceDigitalTwinId() +
            " to: " + createdRelationship.getTargetDigitalTwinId());
        // END: com.azure.digitaltwins.core.syncClient.createRelationship#String-String-Object-Class#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.syncClient.createRelationship#String-String-Object-Class#String
        String relationshipPayload = getRelationshipPayload();

        String createdRelationshipString = digitalTwinsSyncClient.createRelationship(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            relationshipPayload,
            String.class);

        System.out.println("Created relationship: " + createdRelationshipString);
        // END: com.azure.digitaltwins.core.syncClient.createRelationship#String-String-Object-Class#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#createRelationshipWithResponse(String, String, Object, Class, CreateRelationshipOptions, Context)}
     */
    @Override
    public void createRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.createRelationshipWithResponse#String-String-Object-Class-Options-Context#BasicRelationship
        BasicRelationship buildingToFloorBasicRelationship = new BasicRelationship(
                "myRelationshipId",
                "mySourceDigitalTwinId",
                "myTargetDigitalTwinId",
                "contains")
            .addCustomProperty("Prop1", "Prop1 value")
            .addCustomProperty("Prop2", 6);

        Response<BasicRelationship> createdRelationshipWithResponse =
            digitalTwinsSyncClient.createRelationshipWithResponse(
                "mySourceDigitalTwinId",
                "myRelationshipId",
                buildingToFloorBasicRelationship,
                BasicRelationship.class,
                new CreateRelationshipOptions(),
                new Context("key", "value"));

        System.out.println(
            "Created relationship with Id: " +
                createdRelationshipWithResponse.getValue().getRelationshipId() +
                " from: " + createdRelationshipWithResponse.getValue().getSourceDigitalTwinId() +
                " to: " + createdRelationshipWithResponse.getValue().getTargetDigitalTwinId() +
                " Http status code: " +
                createdRelationshipWithResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.createRelationshipWithResponse#String-String-Object-Class-Options-Context#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.syncClient.createRelationshipWithResponse#String-String-Object-Class-Options-Context#String
        String relationshipPayload = getRelationshipPayload();

        Response<String> createdRelationshipStringWithResponse = digitalTwinsSyncClient.createRelationshipWithResponse(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            relationshipPayload,
            String.class,
            new CreateRelationshipOptions(),
            new Context("key", "value"));

        System.out.println(
            "Created relationship: " +
            createdRelationshipStringWithResponse +
            " With HTTP status code: " +
            createdRelationshipStringWithResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.createRelationshipWithResponse#String-String-Object-Class-Options-Context#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#getRelationship(String, String, Class)}
     */
    @Override
    public void getRelationship() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.getRelationship#String#BasicRelationship
        BasicRelationship retrievedRelationship = digitalTwinsSyncClient.getRelationship(
            "myDigitalTwinId",
            "myRelationshipName",
            BasicRelationship.class);

        System.out.println(
            "Retrieved relationship with Id: "
            + retrievedRelationship.getRelationshipId() +
            " from: " +
            retrievedRelationship.getSourceDigitalTwinId() +
            " to: " + retrievedRelationship.getTargetDigitalTwinId());
        // END: com.azure.digitaltwins.core.syncClient.getRelationship#String#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.syncClient.getRelationship#String#String
        String retrievedRelationshipString = digitalTwinsSyncClient.getRelationship(
            "myDigitalTwinId",
            "myRelationshipName",
            String.class);

        System.out.println("Retrieved relationship: " + retrievedRelationshipString);
        // END: com.azure.digitaltwins.core.syncClient.getRelationship#String#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#getRelationshipWithResponse(String, String, Class, GetRelationshipOptions, Context)}
     */
    @Override
    public void getRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.getRelationshipWithResponse#String-String-Class-Options-Context#BasicRelationship
        Response<BasicRelationship> retrievedRelationshipWithResponse =
            digitalTwinsSyncClient.getRelationshipWithResponse(
                "myDigitalTwinId",
                "myRelationshipName",
                BasicRelationship.class,
                new GetRelationshipOptions(),
                new Context("key", "value"));

        System.out.println(
            "Retrieved relationship with Id: "
                + retrievedRelationshipWithResponse.getValue().getRelationshipId() +
                " from: " +
                retrievedRelationshipWithResponse.getValue().getSourceDigitalTwinId() +
                " to: " + retrievedRelationshipWithResponse.getValue().getTargetDigitalTwinId() +
                "HTTP status code: " + retrievedRelationshipWithResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.getRelationshipWithResponse#String-String-Class-Options-Context#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.syncClient.getRelationshipWithResponse#String-String-Class-Options-Context#String
        Response<String> retrievedRelationshipString = digitalTwinsSyncClient.getRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipName",
            String.class,
            new GetRelationshipOptions(),
            new Context("key", "value"));

        System.out.println(
            "Retrieved relationship: " +
            retrievedRelationshipString +
            " HTTP status code: " +
            retrievedRelationshipString.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.getRelationshipWithResponse#String-String-Class-Options-Context#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#updateRelationship(String, String, List)}
     */
    @Override
    public void updateRelationship() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.updateRelationship#String-String-List
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/relationshipProperty1", "new property value");

        digitalTwinsSyncClient.updateRelationship(
            "myDigitalTwinId",
            "myRelationshipId",
            updateOperationUtility.getUpdateOperations());
        // END: com.azure.digitaltwins.core.syncClient.updateRelationship#String-String-List
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#updateRelationshipWithResponse(String, String, List, UpdateRelationshipOptions, Context)}
     */
    @Override
    public void updateRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.updateRelationshipWithResponse#String-String-List-Options-Context
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/relationshipProperty1", "new property value");

        Response<Void> updateResponse = digitalTwinsSyncClient.updateRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipId",
            updateOperationUtility.getUpdateOperations(),
            new UpdateRelationshipOptions(),
            new Context("key", "value"));

        System.out.println("Relationship updated with status code: " + updateResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.updateRelationshipWithResponse#String-String-List-Options-Context
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#deleteRelationship(String, String)}
     */
    @Override
    public void deleteRelationship() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.deleteRelationship#String-String
        digitalTwinsSyncClient.deleteRelationship("myDigitalTwinId", "myRelationshipId");
        // END: com.azure.digitaltwins.core.syncClient.deleteRelationship#String-String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#deleteRelationshipWithResponse(String, String, DeleteRelationshipOptions, Context)}
     */
    @Override
    public void deleteRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.deleteRelationshipWithResponse#String-String-Options-Context
        Response<Void> deleteResponse = digitalTwinsSyncClient.deleteRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipId",
            new DeleteRelationshipOptions(),
            new Context("key", "value"));

        System.out.println("Deleted relationship with HTTP status code: " + deleteResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.deleteRelationshipWithResponse#String-String-Options-Context
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#listRelationships(String, Class)}
     * and {@link DigitalTwinsClient#listRelationships(String, String, Class, ListRelationshipsOptions, Context)}
     */
    @Override
    public void listRelationships() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.listRelationships#String-Class#BasicRelationship#IterateByItem
        PagedIterable<BasicRelationship> pagedRelationshipsByItem = digitalTwinsSyncClient.listRelationships(
            "myDigitalTwinId",
            BasicRelationship.class);

        for (BasicRelationship rel : pagedRelationshipsByItem) {
            System.out.println("Retrieved relationship with Id: " + rel.getRelationshipId());
        }
        // END: com.azure.digitaltwins.core.syncClient.listRelationships#String-Class#BasicRelationship#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.syncClient.listRelationships#String-Class#String#IterateByItem
        PagedIterable<String> pagedRelationshipsStringByItem = digitalTwinsSyncClient.listRelationships(
            "myDigitalTwinId",
            String.class);

        for (String rel : pagedRelationshipsStringByItem) {
            System.out.println("Retrieved relationship: " + rel);
        }
        // END: com.azure.digitaltwins.core.syncClient.listRelationships#String-Class#String#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.syncClient.listRelationships#String-String-Class-Options-Context#BasicRelationship#IterateByItem
        PagedIterable<BasicRelationship> pagedRelationshipByNameByItem = digitalTwinsSyncClient.listRelationships(
            "myDigitalTwinId",
            "myRelationshipName",
            BasicRelationship.class,
            new ListRelationshipsOptions(),
            new Context("Key", "value"));

        for (BasicRelationship rel : pagedRelationshipByNameByItem) {
            System.out.println("Retrieved relationship with Id: " + rel.getRelationshipId());
        }
        // END: com.azure.digitaltwins.core.syncClient.listRelationships#String-String-Class-Options-Context#BasicRelationship#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.syncClient.listRelationships#String-String-Class-Options-Context#String#IterateByItem
        PagedIterable<String> pagedRelationshipsStringByNameByItem = digitalTwinsSyncClient.listRelationships(
            "myDigitalTwinId",
            "myRelationshipId",
            String.class,
            new ListRelationshipsOptions(),
            new Context("key", "value"));

        for (String rel : pagedRelationshipsStringByNameByItem) {
            System.out.println("Retrieved relationship: " + rel);
        }
        // END: com.azure.digitaltwins.core.syncClient.listRelationships#String-String-Class-Options-Context#String#IterateByItem
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#listIncomingRelationships(String, ListIncomingRelationshipsOptions, Context)}
     * and {@link DigitalTwinsClient#listIncomingRelationships(String)}
     */
    @Override
    public void listIncomingRelationships() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.listIncomingRelationships#String
        PagedIterable<IncomingRelationship> pagedIncomingRelationships =
            digitalTwinsSyncClient.listIncomingRelationships(
                "myDigitalTwinId",
                new ListIncomingRelationshipsOptions(),
                new Context("key", "value"));

        for (IncomingRelationship rel : pagedIncomingRelationships) {
            System.out.println(
                "Retrieved relationship with Id: " +
                    rel.getRelationshipId() +
                    " from: " +
                    rel.getSourceDigitalTwinId() +
                    " to: myDigitalTwinId");
        }
        // END: com.azure.digitaltwins.core.syncClient.listIncomingRelationships#String

        // BEGIN: com.azure.digitaltwins.core.syncClient.listIncomingRelationships#String-Options-Context
        PagedIterable<IncomingRelationship> pagedIncomingRelationshipsWithContext =
            digitalTwinsSyncClient.listIncomingRelationships(
                "myDigitalTwinId",
                new ListIncomingRelationshipsOptions(),
                new Context("key", "value"));

        for (IncomingRelationship rel : pagedIncomingRelationshipsWithContext) {
            System.out.println(
                "Retrieved relationship with Id: " +
                rel.getRelationshipId() +
                " from: " +
                rel.getSourceDigitalTwinId() +
                " to: myDigitalTwinId");
        }
        // END: com.azure.digitaltwins.core.syncClient.listIncomingRelationships#String-Options-Context
    }

    //endregion RelationshipSnippets

    //region ModelsSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsClient#createModels(Iterable)}
     */
    @Override
    public void createModels() {
        String model1 = loadModelFromFile("model1");
        String model2 = loadModelFromFile("model2");
        String model3 = loadModelFromFile("model3");

        // BEGIN: com.azure.digitaltwins.core.syncClient.createModels#Iterable
        Iterable<DigitalTwinsModelData> createdModels = digitalTwinsSyncClient.createModels(
            Arrays.asList(model1, model2, model3));

        createdModels.forEach(model ->
            System.out.println("Retrieved model with Id: " + model.getModelId()));
        // END: com.azure.digitaltwins.core.syncClient.createModels#Iterable
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#createModelsWithResponse(Iterable, CreateModelsOptions, Context)}
     */
    @Override
    public void createModelsWithResponse() {
        String model1 = loadModelFromFile("model1");
        String model2 = loadModelFromFile("model2");
        String model3 = loadModelFromFile("model3");

        // BEGIN: com.azure.digitaltwins.core.syncClient.createModelsWithResponse#Iterable
        Response<Iterable<DigitalTwinsModelData>> createdModels = digitalTwinsSyncClient.createModelsWithResponse(
            Arrays.asList(model1, model2, model3),
            new CreateModelsOptions(),
            new Context("key", "value"));

        System.out.println("Received HTTP response of " + createdModels.getStatusCode());

        createdModels.getValue()
            .forEach(model -> System.out.println("Retrieved model with Id: " + model.getModelId()));
        // END: com.azure.digitaltwins.core.syncClient.createModelsWithResponse#Iterable
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#getModel(String)}
     */
    @Override
    public void getModel() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.getModel#String
        DigitalTwinsModelData model = digitalTwinsSyncClient.getModel("dtmi:com:samples:Building;1");

        System.out.println("Retrieved model with Id: " + model.getModelId());
        // END: com.azure.digitaltwins.core.syncClient.getModel#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#getModelWithResponse(String, GetModelOptions, Context)}
     */
    @Override
    public void getModelWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.getModelWithResponse#String
        Response<DigitalTwinsModelData> modelWithResponse = digitalTwinsSyncClient.getModelWithResponse(
            "dtmi:com:samples:Building;1",
            new GetModelOptions(),
            new Context("key", "value"));

        System.out.println("Received HTTP response with status code: " + modelWithResponse.getStatusCode());
        System.out.println("Retrieved model with Id: " + modelWithResponse.getValue().getModelId());
        // END: com.azure.digitaltwins.core.syncClient.getModelWithResponse#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#listModels()} and
     * {@link DigitalTwinsClient#listModels(ListModelsOptions, Context)}
     */
    @Override
    public void listModels() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.listModels
        PagedIterable<DigitalTwinsModelData> modelsListPagedIterable =  digitalTwinsSyncClient.listModels();

        modelsListPagedIterable.forEach(model -> System.out.println("Retrieved a model with Id: " + model.getModelId()));
        // END: com.azure.digitaltwins.core.syncClient.listModels

        // BEGIN: com.azure.digitaltwins.core.syncClient.listModels#Options
        PagedIterable<DigitalTwinsModelData> modelsListWithOptionsPagedIterable =  digitalTwinsSyncClient.listModels(
            new ListModelsOptions()
                .setIncludeModelDefinition(true)
                .setMaxItemsPerPage(5),
            new Context("key", "value"));

        modelsListWithOptionsPagedIterable.forEach(
            model -> System.out.println("Retrieved a model with Id: " + model.getModelId()));
        // END: com.azure.digitaltwins.core.syncClient.listModels#Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#decommissionModel(String)}
     */
    @Override
    public void decommissionModel() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.decommissionModel#String
        digitalTwinsSyncClient.decommissionModel("dtmi:com:samples:Building;1");
        // END: com.azure.digitaltwins.core.syncClient.decommissionModel#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#decommissionModelWithResponse(String, DecommissionModelOptions, Context)}
     */
    @Override
    public void decommissionModelWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.decommissionModelWithResponse#String
        Response<Void> response = digitalTwinsSyncClient.decommissionModelWithResponse(
            "dtmi:com:samples:Building;1",
            new DecommissionModelOptions(),
            new Context("key", "value"));

        System.out.println("Received decommission operation HTTP response with status: " + response.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.decommissionModelWithResponse#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#deleteModel(String)}
     */
    @Override
    public void deleteModel() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.deleteModel#String
        digitalTwinsSyncClient.deleteModel("dtmi:com:samples:Building;1");
        // END: com.azure.digitaltwins.core.syncClient.deleteModel#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#deleteModelWithResponse(String, DeleteModelOptions, Context)}
     */
    @Override
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.deleteModelWithResponse#String
        Response<Void> response = digitalTwinsSyncClient.deleteModelWithResponse(
            "dtmi:com:samples:Building;1",
            new DeleteModelOptions(),
            new Context("key", "value"));

        System.out.println("Received delete model operation HTTP response with status: " + response.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.deleteModelWithResponse#String
    }

    //endregion ModelsSnippets

    //region ComponentSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsClient#getComponent(String, String, Class)}
     */
    @Override
    public void getComponent() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.getComponent#String-String-Class
        String componentString = digitalTwinsSyncClient.getComponent(
            "myDigitalTwinId",
            "myComponentName",
            String.class);

        System.out.println("Retrieved component: " + componentString);
        // END: com.azure.digitaltwins.core.syncClient.getComponent#String-String-Class
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#getComponentWithResponse(String, String, Class, GetComponentOptions, Context)}
     */
    @Override
    public void getComponentWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.getComponentWithResponse#String-String-Class-Options-Context
        Response<String> componentStringWithResponse = digitalTwinsSyncClient.getComponentWithResponse(
            "myDigitalTwinId",
            "myComponentName",
            String.class,
            new GetComponentOptions(),
            new Context("key", "value"));

        System.out.println(
            "Received component get operation response with HTTP status code: " +
            componentStringWithResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.getComponentWithResponse#String-String-Class-Options-Context
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#updateComponent(String, String, List)}
     */
    @Override
    public void updateComponent() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.updateComponent#String-String-List
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/ComponentProp1", "Some new value");

        digitalTwinsSyncClient.updateComponent(
            "myDigitalTwinId",
            "myComponentName",
            updateOperationUtility.getUpdateOperations());
        // END: com.azure.digitaltwins.core.syncClient.updateComponent#String-String-List
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#updateComponentWithResponse(String, String, List, UpdateComponentOptions, Context)}
     */
    @Override
    public void updateComponentWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.updateComponentWithResponse#String-String-List-Options-Context
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/ComponentProp1", "Some new value");

        Response<Void> updateResponse = digitalTwinsSyncClient.updateComponentWithResponse(
            "myDigitalTwinId",
            "myComponentName",
            updateOperationUtility.getUpdateOperations(),
            new UpdateComponentOptions(),
            new Context("key", "value"));

        System.out.println(
            "Received update operation HTTP response with status: " +
            updateResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.updateComponentWithResponse#String-String-List-Options-Context
    }

    //endregion ComponentSnippets

    //region QuerySnippets
    /**
     * Generates code samples for using {@link DigitalTwinsClient#query(String, Class)} and
     * {@link DigitalTwinsAsyncClient#query(String, Class, QueryOptions)}
     */
    @Override
    public void query() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.query#String#BasicDigitalTwin
        PagedIterable<BasicDigitalTwin> queryResultBasicDigitalTwin = digitalTwinsSyncClient.query(
            "SELECT * FROM digitaltwins",
            BasicDigitalTwin.class);

        queryResultBasicDigitalTwin.forEach(basicTwin -> System.out.println(
            "Retrieved digitalTwin query result with Id: " +
            basicTwin.getId()));
        // END: com.azure.digitaltwins.core.syncClient.query#String#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.syncClient.query#String#String
        PagedIterable<String> queryResultString = digitalTwinsSyncClient.query(
            "SELECT * FROM digitaltwins",
            String.class);

        queryResultString.forEach(
            queryResult -> System.out.println("Retrieved digitalTwin query result: " + queryResult));
        // END: com.azure.digitaltwins.core.syncClient.query#String#String

        // BEGIN: com.azure.digitaltwins.core.syncClient.query#String-Options-Context#BasicDigitalTwin
        PagedIterable<BasicDigitalTwin> queryResultBasicDigitalTwinWithContext = digitalTwinsSyncClient.query(
            "SELECT * FROM digitaltwins",
            BasicDigitalTwin.class,
            new QueryOptions(),
            new Context("key", "value"));

        queryResultBasicDigitalTwinWithContext
            .forEach(basicTwin ->
                System.out.println("Retrieved digitalTwin query result with Id: " + basicTwin.getId()));
        // END: com.azure.digitaltwins.core.syncClient.query#String-Options-Context#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.syncClient.query#String-Options-Context#String
        PagedIterable<String> queryResultStringWithContext = digitalTwinsSyncClient.query(
            "SELECT * FROM digitaltwins",
            String.class,
            new QueryOptions(),
            new Context("key", "value"));

        queryResultStringWithContext
            .forEach(queryResult ->
                System.out.println("Retrieved digitalTwin query result: " + queryResult));
        // END: com.azure.digitaltwins.core.syncClient.query#String-Options-Context#String
    }
    //endregion QuerySnippets

    //region EventRouteSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsClient#createEventRoute(String, EventRoute)}
     */
    @Override
    public void createEventRoute() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.createEventRoute#String-EventRoute
        String filter =
            "$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'";

        EventRoute eventRoute = new EventRoute("myEndpointName").setFilter(filter);
        digitalTwinsSyncClient.createEventRoute("myEventRouteId", eventRoute);
        // END: com.azure.digitaltwins.core.syncClient.createEventRoute#String-EventRoute
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#createEventRouteWithResponse(String, EventRoute, CreateEventRouteOptions, Context)}
     */
    @Override
    public void createEventRouteWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.createEventRouteWithResponse#String-EventRoute-Options-Context
        String filter =
            "$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'";

        EventRoute eventRoute = new EventRoute("myEndpointName").setFilter(filter);
        Response<Void> response = digitalTwinsSyncClient.createEventRouteWithResponse(
            "myEventRouteId",
            eventRoute,
            new CreateEventRouteOptions(),
            new Context("key", "value"));

        System.out.println("Created an event rout with HTTP status code: " + response.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.createEventRouteWithResponse#String-EventRoute-Options-Context
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#getEventRoute(String)}
     */
    @Override
    public void getEventRoute() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.getEventRoute#String
        EventRoute eventRoute = digitalTwinsSyncClient.getEventRoute("myEventRouteId");

        System.out.println("Retrieved event route with Id: " + eventRoute.getEventRouteId());
        // END: com.azure.digitaltwins.core.syncClient.getEventRoute#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#getEventRouteWithResponse(String, GetEventRouteOptions, Context)}
     */
    @Override
    public void getEventRouteWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.getEventRouteWithResponse#String-Options-Context
        Response<EventRoute> eventRouteWithResponse = digitalTwinsSyncClient.getEventRouteWithResponse(
            "myEventRouteId",
            new GetEventRouteOptions(),
            new Context("key", "value"));

        System.out.println(
            "Received get event route operation response with HTTP status code: " +
            eventRouteWithResponse.getStatusCode());
        System.out.println("Retrieved event route with Id: " + eventRouteWithResponse.getValue().getEventRouteId());
        // END: com.azure.digitaltwins.core.syncClient.getEventRouteWithResponse#String-Options-Context
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#deleteEventRoute(String)}
     */
    @Override
    public void deleteEventRoute() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.deleteEventRoute#String
        digitalTwinsSyncClient.deleteEventRoute("myEventRouteId");
        // END: com.azure.digitaltwins.core.syncClient.deleteEventRoute#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#deleteEventRouteWithResponse(String, DeleteEventRouteOptions, Context)}
     */
    @Override
    public void deleteEventRouteWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.deleteEventRouteWithResponse#String-Options-Context
        Response<Void> deleteResponse = digitalTwinsSyncClient.deleteEventRouteWithResponse(
            "myEventRouteId",
            new DeleteEventRouteOptions(),
            new Context("key", "value"));

        System.out.println(
            "Received delete event route operation response with HTTP status code: " +
            deleteResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.deleteEventRouteWithResponse#String-Options-Context
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#listEventRoutes()} and
     * {@link DigitalTwinsClient#listEventRoutes(ListEventRoutesOptions, Context)}
     */
    @Override
    public void listEventRoutes() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.listEventRoutes
        PagedIterable<EventRoute> listResponse =  digitalTwinsSyncClient.listEventRoutes();

        listResponse.forEach(
            eventRoute -> System.out.println("Retrieved event route with Id: " + eventRoute.getEventRouteId()));
        // END: com.azure.digitaltwins.core.syncClient.listEventRoutes

        // BEGIN: com.azure.digitaltwins.core.syncClient.listEventRoutes#Options-Context
        PagedIterable<EventRoute> listResponseWithOptions =  digitalTwinsSyncClient.listEventRoutes(
            new ListEventRoutesOptions().setMaxItemsPerPage(5),
            new Context("key", "value"));

        listResponseWithOptions
            .forEach(
                eventRoute -> System.out.println("Retrieved event route with Id: " + eventRoute.getEventRouteId()));
        // END: com.azure.digitaltwins.core.syncClient.listEventRoutes#Options-Context
    }

    //endregion EventRouteSnippets

    //region TelemetrySnippets

    /**
     * Generates code samples for using {@link DigitalTwinsClient#publishTelemetry(String, String, Object)}
     */
    @Override
    public void publishTelemetry() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.publishTelemetry#String-String-Object#String
        digitalTwinsSyncClient.publishTelemetry(
            "myDigitalTwinId",
            UUID.randomUUID().toString(),
            "{\"Telemetry1\": 5}");
        // END: com.azure.digitaltwins.core.syncClient.publishTelemetry#String-String-Object#String

        // BEGIN: com.azure.digitaltwins.core.syncClient.publishTelemetry#String-String-Object#Object
        Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
        telemetryPayload.put("Telemetry1", 5);

        digitalTwinsSyncClient.publishTelemetry(
            "myDigitalTwinId",
            UUID.randomUUID().toString(),
            telemetryPayload);
        // END: com.azure.digitaltwins.core.syncClient.publishTelemetry#String-String-Object#Object
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#publishTelemetryWithResponse(String, String, Object, PublishTelemetryOptions, Context)}
     */
    @Override
    public void publishTelemetryWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.publishTelemetryWithResponse#String-String-Object-Options-Context#String
        Response<Void> responseString = digitalTwinsSyncClient.publishTelemetryWithResponse(
            "myDigitalTwinId",
            UUID.randomUUID().toString(),
            "{\"Telemetry1\": 5}",
            new PublishTelemetryOptions().setTimestamp(OffsetDateTime.now(ZoneId.systemDefault())),
            new Context("key", "value"));

        System.out.println(
            "Received publish telemetry operation response with HTTP status code: " +
            responseString.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.publishTelemetryWithResponse#String-String-Object-Options-Context#String

        // BEGIN: com.azure.digitaltwins.core.syncClient.publishTelemetryWithResponse#String-String-Object-Options-Context#Object
        Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
        telemetryPayload.put("Telemetry1", 5);

        Response<Void> responseObject = digitalTwinsSyncClient.publishTelemetryWithResponse(
            "myDigitalTwinId",
            UUID.randomUUID().toString(),
            telemetryPayload,
            new PublishTelemetryOptions().setTimestamp(OffsetDateTime.now(ZoneId.systemDefault())),
            new Context("key", "value"));

        System.out.println(
            "Received publish telemetry operation response with HTTP status code: " +
            responseObject.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.publishTelemetryWithResponse#String-String-Object-Options-Context#Object
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#publishComponentTelemetry(String, String, String, Object)}
     */
    @Override
    public void publishComponentTelemetry() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.publishComponentTelemetry#String-String-String-Object#String
        digitalTwinsSyncClient.publishComponentTelemetry(
            "myDigitalTwinId",
            "myComponentName",
            UUID.randomUUID().toString(),
            "{\"Telemetry1\": 5}");
        // END: com.azure.digitaltwins.core.syncClient.publishComponentTelemetry#String-String-String-Object#String

        // BEGIN: com.azure.digitaltwins.core.syncClient.publishComponentTelemetry#String-String-String-Object#Object
        Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
        telemetryPayload.put("Telemetry1", 5);

        digitalTwinsSyncClient.publishComponentTelemetry(
            "myDigitalTwinId",
            "myComponentName",
            UUID.randomUUID().toString(),
            telemetryPayload);
        // END: com.azure.digitaltwins.core.syncClient.publishComponentTelemetry#String-String-String-Object#Object
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsClient#publishComponentTelemetryWithResponse(String, String, String, Object, PublishComponentTelemetryOptions, Context)}
     */
    @Override
    public void publishComponentTelemetryWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options-Context#String
        Response<Void> responseString = digitalTwinsSyncClient.publishComponentTelemetryWithResponse(
            "myDigitalTwinId",
            "myComponentName",
            UUID.randomUUID().toString(),
            "{\"Telemetry1\": 5}",
            new PublishComponentTelemetryOptions().setTimestamp(OffsetDateTime.now(ZoneId.systemDefault())),
            new Context("key", "value"));

        System.out.println(
            "Received publish component telemetry operation response with HTTP status code: " +
            responseString.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options-Context#String

        // BEGIN: com.azure.digitaltwins.core.syncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options-Context#Object
        Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
        telemetryPayload.put("Telemetry1", 5);

        Response<Void> responseObject = digitalTwinsSyncClient.publishComponentTelemetryWithResponse(
            "myDigitalTwinId",
            "myComponentName",
            UUID.randomUUID().toString(),
            telemetryPayload,
            new PublishComponentTelemetryOptions().setTimestamp(OffsetDateTime.now(ZoneId.systemDefault())),
            new Context("key", "value"));

        System.out.println(
            "Received publish component telemetry operation response with HTTP status code: " +
            responseObject.getStatusCode());
        // END: com.azure.digitaltwins.core.syncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options-Context#Object
    }

    //endregion TelemetrySnippets
}
