package com.azure.digitaltwins.core;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.digitaltwins.core.models.*;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Code snippets for {@link DigitalTwinsClient}
 */
public class DigitalTwinsClientJavaDocCodeSnippets extends CodeSnippetBase {

    private DigitalTwinsClient digitalTwinsSyncClient;

    DigitalTwinsClientJavaDocCodeSnippets(){
        digitalTwinsSyncClient = createDigitalTwinsClient();
    }

    public DigitalTwinsClient createDigitalTwinsClient() {

        String tenantId = getTenenatId();
        String clientId = getClientId();
        String clientSecret = getClientSecret();
        String digitalTwinsEndpointUrl = getEndpointUrl();

        // BEGIN com.azure.digitaltwins.core.syncclient.instantiation
        DigitalTwinsClient digitalTwinsSyncClient = new DigitalTwinsClientBuilder()
            .credential(
                new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build())
            .endpoint(digitalTwinsEndpointUrl)
            .buildClient();
        // END com.azure.digitaltwins.core.digitaltwinsclient.instantiation

        return digitalTwinsSyncClient;
    }

    //region DigitalTwinSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsClient#createDigitalTwin(String, Object, Class)}
     */
    @Override
    public void createDigitalTwin() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncclient.createDigitalTwins#String-Object-Class#BasicDigitalTwin
        String modelId = "dtmi:samples:Building;1";

        BasicDigitalTwin basicTwin = new BasicDigitalTwin()
            .setId("myDigitalTwinId")
            .setMetadata(
                new DigitalTwinMetadata()
                    .setModelId(modelId)
            );

        BasicDigitalTwin result = digitalTwinsClient.createDigitalTwin(basicTwin.getId(), basicTwin, BasicDigitalTwin.class);
        // END: com.azure.digitaltwins.core.syncclient.createDigitalTwins#String-Object-Class#BasicDigitalTwin

        String digitalTwinStringPayload = getDigitalTwinPayload();

        // BEGIN: com.azure.digitaltwins.core.syncclient.createDigitalTwins#String-Object-Class#String
        String stringResult = digitalTwinsClient.createDigitalTwin("myDigitalTwinId", digitalTwinStringPayload, String.class);

        // END: com.azure.digitaltwins.core.syncclient.createDigitalTwins#String-Object-Class#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#createDigitalTwinWithResponse(String, Object, Class, Context)}
     */
    @Override
    public void createDigitalTwinWithResponse() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncclient.createDigitalTwinsWithResponse#String-Object-Class-Context#BasicDigitalTwin
        String modelId = "dtmi:samples:Building;1";

        BasicDigitalTwin basicDigitalTwin = new BasicDigitalTwin()
            .setId("myDigitalTwinId")
            .setMetadata(
                new DigitalTwinMetadata()
                    .setModelId(modelId)
            );

        Response<BasicDigitalTwin> resultWithResponse = digitalTwinsClient.createDigitalTwinWithResponse(
            basicDigitalTwin.getId(),
            basicDigitalTwin,
            BasicDigitalTwin.class,
            new Context("Key", "Value"));

        System.out.println("Response http status: " + resultWithResponse.getStatusCode() + " created digital twin Id: " + resultWithResponse.getValue().getId());
        // END: com.azure.digitaltwins.core.syncclient.createDigitalTwinsWithResponse#String-Object-Class-Context#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.syncclient.createDigitalTwinsWithResponse#String-Object-Class-Context#String
        String stringPayload = getDigitalTwinPayload();

        Response<String> stringResultWithResponse = digitalTwinsClient.createDigitalTwinWithResponse(
            basicDigitalTwin.getId(),
            stringPayload,
            String.class,
            new Context("Key", "Value"));

        System.out.println("Response http status: " + stringResultWithResponse.getStatusCode() + " created digital twin: " + stringResultWithResponse.getValue());
        // END: com.azure.digitaltwins.core.syncclient.createDigitalTwinsWithResponse#String-Object-Class-Context#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#getDigitalTwin(String, Class)}
     */
    @Override
    public void getDigitalTwin() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncclient.getDigitalTwin#String-Class#BasicDigitalTwin
        BasicDigitalTwin basicTwinResult = digitalTwinsClient.getDigitalTwin("myDigitalTwinId", BasicDigitalTwin.class);

        System.out.println("Retrieved digital twin with Id: " + basicTwinResult.getId());
        // END: com.azure.digitaltwins.core.syncclient.getDigitalTwin#String-Class#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.syncclient.getDigitalTwin#String-Class#String
        String stringResult = digitalTwinsClient.getDigitalTwin("myDigitalTwinId", String.class);

        System.out.println("Retrieved digital twin: " + stringResult);
        // END: com.azure.digitaltwins.core.syncclient.getDigitalTwin#String-Class#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#getDigitalTwinWithResponse(String, Class, Context)}
     */
    @Override
    public void getDigitalTwinWithResponse() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncclient.getDigitalTwinWithResponse#String-Class-Context#BasicDigitalTwin
        Response<BasicDigitalTwin> basicTwinResultWithResponse = digitalTwinsClient.getDigitalTwinWithResponse("myDigitalTwinId", BasicDigitalTwin.class, new Context("key", "value"));

        System.out.println("Http status code: " + basicTwinResultWithResponse.getStatusCode());
        System.out.println("Retrieved digital twin with Id: " + basicTwinResultWithResponse.getValue().getId());
        // END: com.azure.digitaltwins.core.syncclient.getDigitalTwinWithResponse#String-Class-Context#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.syncclient.getDigitalTwinWithResponse#String-Class-Context#String
        Response<String> stringResultWithResponse = digitalTwinsClient.getDigitalTwinWithResponse("myDigitalTwinId", String.class, new Context("key", "value"));

        System.out.println("Http response status: " + stringResultWithResponse.getStatusCode());
        System.out.println("Retrieved digital twin: " + stringResultWithResponse.getValue());
        // END: com.azure.digitaltwins.core.syncclient.getDigitalTwinWithResponse#String-Class-Context#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#updateDigitalTwin(String, List)}
     */
    @Override
    public void updateDigitalTwin() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncclient.updateDigitalTwin#String-List
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("Prop1", "newValue");

        digitalTwinsClient.updateDigitalTwin("myDigitalTwinId", updateOperationUtility.getUpdateOperations());
        // END: com.azure.digitaltwins.core.syncclient.updateDigitalTwin#String-List
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#updateDigitalTwinWithResponse(String, List, UpdateDigitalTwinRequestOptions, Context)}
     */
    @Override
    public void updateDigitalTwinWithResponse() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncclient.updateDigitalTwinWithResponse#String-List-Options-Context
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("Prop1", "newValue");

        Response response = digitalTwinsClient.updateDigitalTwinWithResponse(
            "myDigitalTwinId",
            updateOperationUtility.getUpdateOperations(),
            new UpdateDigitalTwinRequestOptions(),
            new Context("key", "value"));

        System.out.println("Update completed with HTTP status code: " + response.getStatusCode());
        // END: com.azure.digitaltwins.core.syncclient.updateDigitalTwinWithResponse#String-List-Options-Context
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#deleteDigitalTwin(String)}
     */
    @Override
    public void deleteDigitalTwin() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncclient.deleteDigitalTwin#String
        digitalTwinsClient.deleteDigitalTwin("myDigitalTwinId");
        // END: com.azure.digitaltwins.core.syncclient.deleteDigitalTwin#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#deleteDigitalTwinWithResponse(String, DeleteDigitalTwinRequestOptions, Context)}
     */
    @Override
    public void deleteDigitalTwinWithResponse() {
        DigitalTwinsClient digitalTwinsClient = createDigitalTwinsClient();

        // BEGIN: com.azure.digitaltwins.core.syncclient.deleteDigitalTwinWithResponse#String-Options-Context
        Response response = digitalTwinsClient.deleteDigitalTwinWithResponse(
            "myDigitalTwinId",
            new DeleteDigitalTwinRequestOptions(),
            new Context("key", "value"));

        System.out.println("Deleted digital twin HTTP response status code: " + response.getStatusCode());
        // END: com.azure.digitaltwins.core.syncclient.deleteDigitalTwinWithResponse#String-Options-Context
    }
    //endregion DigitalTwinSnippets

    //region RelationshipSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsClient#createRelationship(String, String, Object, Class)}
     */
    @Override
    public void createRelationship() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.createRelationship#String-String-Object-Class#BasicRelationship
        BasicRelationship buildingToFloorBasicRelationship = new BasicRelationship()
            .setId("myRelationshipId")
            .setSourceId("mySourceDigitalTwinId")
            .setTargetId("myTargetDigitalTwinId")
            .setName("contains")
            .addCustomProperty("Prop1", "Prop1 value")
            .addCustomProperty("Prop2", 6);

        BasicRelationship createdRelationship = digitalTwinsSyncClient.createRelationship(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            buildingToFloorBasicRelationship,
            BasicRelationship.class);

        System.out.println(
            "Created relationship with Id: " +
            createdRelationship.getId() +
            " from: " + createdRelationship.getSourceId() +
            " to: " + createdRelationship.getTargetId());
        // END: com.azure.digitaltwins.core.syncclient.createRelationship#String-String-Object-Class#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.syncclient.createRelationship#String-String-Object-Class#String
        String relationshipPayload = getRelationshipPayload();

        String createdRelationshipString = digitalTwinsSyncClient.createRelationship(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            relationshipPayload,
            String.class);

        System.out.println("Created relationship: " + createdRelationshipString);
        // END: com.azure.digitaltwins.core.syncclient.createRelationship#String-String-Object-Class#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#createRelationshipWithResponse(String, String, Object, Class, Context)}
     */
    @Override
    public void createRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.createRelationshipWithResponse#String-String-Object-Class-Context#BasicRelationship
        BasicRelationship buildingToFloorBasicRelationship = new BasicRelationship()
            .setId("myRelationshipId")
            .setSourceId("mySourceDigitalTwinId")
            .setTargetId("myTargetDigitalTwinId")
            .setName("contains")
            .addCustomProperty("Prop1", "Prop1 value")
            .addCustomProperty("Prop2", 6);

        Response<BasicRelationship> createdRelationshipWithResponse = digitalTwinsSyncClient.createRelationshipWithResponse(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            buildingToFloorBasicRelationship,
            BasicRelationship.class,
            new Context("key", "value"));

        System.out.println(
            "Created relationship with Id: " +
                createdRelationshipWithResponse.getValue().getId() +
                " from: " + createdRelationshipWithResponse.getValue().getSourceId() +
                " to: " + createdRelationshipWithResponse.getValue().getTargetId() +
                " Http status code: " +
                createdRelationshipWithResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncclient.createRelationshipWithResponse#String-String-Object-Class-Context#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.syncclient.createRelationshipWithResponse#String-String-Object-Class-Context#String
        String relationshipPayload = getRelationshipPayload();

        Response<String> createdRelationshipStringWithResponse = digitalTwinsSyncClient.createRelationshipWithResponse(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            relationshipPayload,
            String.class,
            new Context("key", "value"));

        System.out.println(
            "Created relationship: " +
            createdRelationshipStringWithResponse +
            " With HTTP status code: " +
            createdRelationshipStringWithResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncclient.createRelationshipWithResponse#String-String-Object-Class-Context#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#getRelationship(String, String, Class)}
     */
    @Override
    public void getRelationship() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.getRelationship#String#BasicRelationship
        BasicRelationship retrievedRelationship = digitalTwinsSyncClient.getRelationship("myDigitalTwinId", "myRelationshipName", BasicRelationship.class);

        System.out.println(
            "Retrieved relationship with Id: "
            + retrievedRelationship.getId() +
            " from: " +
            retrievedRelationship.getSourceId() +
            " to: " + retrievedRelationship.getTargetId());
        // END: com.azure.digitaltwins.core.syncclient.getRelationship#String#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.syncclient.getRelationship#String#String
        String retrievedRelationshipString = digitalTwinsSyncClient.getRelationship("myDigitalTwinId", "myRelationshipName", String.class);

        System.out.println("Retrieved relationship: " + retrievedRelationshipString);
        // END: com.azure.digitaltwins.core.syncclient.getRelationship#String#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#getRelationshipWithResponse(String, String, Class, Context)}
     */
    @Override
    public void getRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.getRelationshipWithResponse#String-String-Class-Context#BasicRelationship
        Response<BasicRelationship> retrievedRelationshipWithResponse = digitalTwinsSyncClient.getRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipName",
            BasicRelationship.class,
            new Context("key", "value"));

        System.out.println(
            "Retrieved relationship with Id: "
                + retrievedRelationshipWithResponse.getValue().getId() +
                " from: " +
                retrievedRelationshipWithResponse.getValue().getSourceId() +
                " to: " + retrievedRelationshipWithResponse.getValue().getTargetId() +
                "HTTP status code: " + retrievedRelationshipWithResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncclient.getRelationshipWithResponse#String-String-Class-Context#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.syncclient.getRelationshipWithResponse#String-String-Class-Context#String
        Response<String> retrievedRelationshipString = digitalTwinsSyncClient.getRelationshipWithResponse("myDigitalTwinId", "myRelationshipName", String.class, new Context("key", "value"));

        System.out.println("Retrieved relationship: " + retrievedRelationshipString + " HTTP status code: " + retrievedRelationshipString.getStatusCode());
        // END: com.azure.digitaltwins.core.syncclient.getRelationshipWithResponse#String-String-Class-Context#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#updateRelationship(String, String, List)}
     */
    @Override
    public void updateRelationship() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.updateRelationship#String-String-List
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/relationshipProperty1", "new property value");

        digitalTwinsSyncClient.updateRelationship("myDigitalTwinId", "myRelationshipId", updateOperationUtility.getUpdateOperations());
        // END: com.azure.digitaltwins.core.syncclient.updateRelationship#String-String-List
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#updateRelationshipWithResponse(String, String, List, UpdateRelationshipRequestOptions, Context)}
     */
    @Override
    public void updateRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.updateRelationshipWithResponse#String-String-List-Options-Context
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/relationshipProperty1", "new property value");

        Response updateResponse = digitalTwinsSyncClient.updateRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipId",
            updateOperationUtility.getUpdateOperations(),
            new UpdateRelationshipRequestOptions(),
            new Context("key", "value"));

        System.out.println("Relationship updated with status code: " + updateResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncclient.updateRelationshipWithResponse#String-String-List-Options-Context
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#deleteRelationship(String, String)}
     */
    @Override
    public void deleteRelationship() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.deleteRelationship#String-String
        digitalTwinsSyncClient.deleteRelationship("myDigitalTwinId", "myRelationshipId");
        // END: com.azure.digitaltwins.core.syncclient.deleteRelationship#String-String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#deleteRelationshipWithResponse(String, String, DeleteRelationshipRequestOptions, Context)}
     */
    @Override
    public void deleteRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.deleteRelationshipWithResponse#String-String-Options-Context
        Response deleteResponse = digitalTwinsSyncClient.deleteRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipId",
            new DeleteRelationshipRequestOptions(),
            new Context("key", "value"));

        System.out.println("Deleted relationship with HTTP status code: " + deleteResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncclient.deleteRelationshipWithResponse#String-String-Options-Context
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#listRelationships(String, Class)}
     * and {@link DigitalTwinsClient#listRelationships(String, String, Class, Context)}
     */
    @Override
    public void listRelationships() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.listRelationships#String-Class#BasicRelationship#IterateByItem
        PagedIterable<BasicRelationship> pagedRelationshipsByItem = digitalTwinsSyncClient.listRelationships("myDigitalTwinId", BasicRelationship.class);

        for (BasicRelationship rel : pagedRelationshipsByItem) {
            System.out.println("Retrieved relationship with Id: " + rel.getId());
        }
        // END: com.azure.digitaltwins.core.syncclient.listRelationships#String-Class#BasicRelationship#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.syncclient.listRelationships#String-Class#String#IterateByItem
        PagedIterable<String> pagedRelationshipsStringByItem = digitalTwinsSyncClient.listRelationships("myDigitalTwinId", String.class);

        for (String rel : pagedRelationshipsStringByItem) {
            System.out.println("Retrieved relationship: " + rel);
        }
        // END: com.azure.digitaltwins.core.syncclient.listRelationships#String-Class#String#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.syncclient.listRelationships#String-String-Class-Context#BasicRelationship#IterateByItem
        PagedIterable<BasicRelationship> pagedRelationshipByNameByItem = digitalTwinsSyncClient.listRelationships(
            "myDigitalTwinId",
            "myRelationshipName",
            BasicRelationship.class,
            new Context("Key", "value"));

        for (BasicRelationship rel : pagedRelationshipByNameByItem) {
            System.out.println("Retrieved relationship with Id: " + rel.getId());
        }
        // END: com.azure.digitaltwins.core.syncclient.listRelationships#String-String-Class-Context#BasicRelationship#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.syncclient.listRelationships#String-String-Class-Context#String#IterateByItem
        PagedIterable<String> pagedRelationshipsStringByNameByItem = digitalTwinsSyncClient.listRelationships(
            "myDigitalTwinId",
            "myRelationshipId",
            String.class,
            new Context("key", "value"));

        for (String rel : pagedRelationshipsStringByNameByItem) {
            System.out.println("Retrieved relationship: " + rel);
        }
        // END: com.azure.digitaltwins.core.syncclient.listRelationships#String-String-Class-Context#String#IterateByItem
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#listIncomingRelationships(String, Context)}
     */
    @Override
    public void listIncomingRelationships() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.listIncomingRelationships#String-Context
        PagedIterable<IncomingRelationship> pagedIncomingRelationships = digitalTwinsSyncClient.listIncomingRelationships("myDigitalTwinId", new Context("key", "value"));

        for (IncomingRelationship rel : pagedIncomingRelationships) {
            System.out.println("Retrieved relationship with Id: " + rel.getRelationshipId() + " from: " + rel.getSourceId() + " to: myDigitalTwinId");
        }
        // END: com.azure.digitaltwins.core.syncclient.listIncomingRelationships#String-Context
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

        // BEGIN: com.azure.digitaltwins.core.syncclient.createModels#Iterable
        Iterable<DigitalTwinsModelData> createdModels = digitalTwinsSyncClient.createModels(Arrays.asList(model1, model2, model3));

        createdModels.forEach(model -> System.out.println("Retrieved model with Id: " + model.getId()));
        // END: com.azure.digitaltwins.core.syncclient.createModels#Iterable
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#createModelsWithResponse(Iterable, Context)}
     */
    @Override
    public void createModelsWithResponse() {
        String model1 = loadModelFromFile("model1");
        String model2 = loadModelFromFile("model2");
        String model3 = loadModelFromFile("model3");

        // BEGIN: com.azure.digitaltwins.core.syncclient.createModelsWithResponse#Iterable
        Response<Iterable<DigitalTwinsModelData>> createdModels = digitalTwinsSyncClient.createModelsWithResponse(Arrays.asList(model1, model2, model3), new Context("key", "value"));

        System.out.println("Received HTTP response of " + createdModels.getStatusCode());

        createdModels.getValue().forEach(model -> System.out.println("Retrieved model with Id: " + model.getId()));
        // END: com.azure.digitaltwins.core.syncclient.createModelsWithResponse#Iterable
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#getModel(String)}
     */
    @Override
    public void getModel() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.getModel#String
        DigitalTwinsModelData model = digitalTwinsSyncClient.getModel("dtmi:samples:Building;1");

        System.out.println("Retrieved model with Id: " + model.getId());
        // END: com.azure.digitaltwins.core.syncclient.getModel#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#getModelWithResponse(String, Context)}
     */
    @Override
    public void getModelWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.getModelWithResponse#String
        Response<DigitalTwinsModelData> modelWithResponse = digitalTwinsSyncClient.getModelWithResponse("dtmi:samples:Building;1", new Context("key", "value"));

        System.out.println("Received HTTP response with status code: " + modelWithResponse.getStatusCode());
        System.out.println("Retrieved model with Id: " + modelWithResponse.getValue().getId());
        // END: com.azure.digitaltwins.core.syncclient.getModelWithResponse#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#listModels()} and {@link DigitalTwinsClient#listModels(ModelsListOptions, Context)}
     */
    @Override
    public void listModels() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.listModels
        PagedIterable<DigitalTwinsModelData> modelsListPagedIterable =  digitalTwinsSyncClient.listModels();

        modelsListPagedIterable.forEach(model -> System.out.println("Retrieved a model with Id: " + model.getId()));
        // END: com.azure.digitaltwins.core.syncclient.listModels

        // BEGIN: com.azure.digitaltwins.core.syncclient.listModels#Options
        PagedIterable<DigitalTwinsModelData> modelsListWithOptionsPagedIterable =  digitalTwinsSyncClient.listModels(
            new ModelsListOptions()
                .setIncludeModelDefinition(true)
                .setMaxItemCount(5),
            new Context("key", "value"));

        modelsListWithOptionsPagedIterable.forEach(model -> System.out.println("Retrieved a model with Id: " + model.getId()));
        // END: com.azure.digitaltwins.core.syncclient.listModels#Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#decommissionModel(String)}
     */
    @Override
    public void decommissionModel() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.decommissionModel#String
        digitalTwinsSyncClient.decommissionModel("dtmi:samples:Building;1");
        // END: com.azure.digitaltwins.core.syncclient.decommissionModel#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#decommissionModelWithResponse(String, Context)}
     */
    @Override
    public void decommissionModelWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.decommissionModelWithResponse#String
        Response response = digitalTwinsSyncClient.decommissionModelWithResponse("dtmi:samples:Building;1", new Context("key", "value"));

        System.out.println("Received decommission operation HTTP response with status: " + response.getStatusCode());
        // END: com.azure.digitaltwins.core.syncclient.decommissionModelWithResponse#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#deleteModel(String)}
     */
    @Override
    public void deleteModel() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.deleteModel#String
        digitalTwinsSyncClient.deleteModel("dtmi:samples:Building;1");
        // END: com.azure.digitaltwins.core.syncclient.deleteModel#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#deleteModelWithResponse(String, Context)}
     */
    @Override
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.deleteModelWithResponse#String
        Response response = digitalTwinsSyncClient.deleteModelWithResponse("dtmi:samples:Building;1", new Context("key", "value"));

        System.out.println("Received delete model operation HTTP response with status: " + response.getStatusCode());
        // END: com.azure.digitaltwins.core.syncclient.deleteModelWithResponse#String
    }

    //endregion ModelsSnippets

    //region ComponentSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsClient#getComponent(String, String, Class)}
     */
    @Override
    public void getComponent() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.getComponent#String-String-Class
        String componentString = digitalTwinsSyncClient.getComponent("myDigitalTwinId", "myComponentPath", String.class);
        // END: com.azure.digitaltwins.core.syncclient.getComponent#String-String-Class
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#getComponentWithResponse(String, String, Class, Context)}
     */
    @Override
    public void getComponentWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.getComponentWithResponse#String-String-Class-Context
        Response<String> componentStringWithResponse = digitalTwinsSyncClient.getComponentWithResponse(
            "myDigitalTwinId",
            "myComponentPath",
            String.class,
            new Context("key", "value"));

        System.out.println("Received component get operation response with HTTP status code: " + componentStringWithResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncclient.getComponentWithResponse#String-String-Class-Context
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#updateComponent(String, String, List)}
     */
    @Override
    public void updateComponent() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.updateComponent#String-String-List
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/ComponentProp1", "Some new value");

        digitalTwinsSyncClient.updateComponent(
            "myDigitalTwinId",
            "myComponentName",
            updateOperationUtility.getUpdateOperations());
        // END: com.azure.digitaltwins.core.syncclient.updateComponent#String-String-List
    }

    /**
     * Generates code samples for using {@link DigitalTwinsClient#updateComponentWithResponse(String, String, List, UpdateComponentRequestOptions, Context)}
     */
    @Override
    public void updateComponentWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.updateComponentWithResponse#String-String-List-Options-Context
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/ComponentProp1", "Some new value");

        Response updateResponse = digitalTwinsSyncClient.updateComponentWithResponse(
            "myDigitalTwinId",
            "myComponentName",
            updateOperationUtility.getUpdateOperations(),
            new UpdateComponentRequestOptions(),
            new Context("key", "value"));

        System.out.println("Received update operation HTTP response with status: " + updateResponse.getStatusCode());
        // END: com.azure.digitaltwins.core.syncclient.updateComponentWithResponse#String-String-List-Options-Context
    }

    //endregion ComponentSnippets

    //region QuerySnippets
    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#query(String, Class)} and {@link DigitalTwinsAsyncClient#query(String, Class, Context)}
     */
    @Override
    public void query() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.query#String#BasicDigitalTwin
        PagedIterable<BasicDigitalTwin> queryResultBasicDigitalTwin = digitalTwinsSyncClient.query("SELECT * FROM digitaltwins", BasicDigitalTwin.class);

        queryResultBasicDigitalTwin.forEach(basicTwin -> System.out.println("Retrieved digitalTwin query result with Id: " + basicTwin.getId()));
        // END: com.azure.digitaltwins.core.syncclient.query#String#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.syncclient.query#String#String
        PagedIterable<String> queryResultString = digitalTwinsSyncClient.query("SELECT * FROM digitaltwins", String.class);

        queryResultString.forEach(queryResult -> System.out.println("Retrieved digitalTwin query result: " + queryResult));
        // END: com.azure.digitaltwins.core.syncclient.query#String#String

        // BEGIN: com.azure.digitaltwins.core.syncclient.query#String-Context#BasicDigitalTwin
        PagedIterable<BasicDigitalTwin> queryResultBasicDigitalTwinWithContext = digitalTwinsSyncClient.query("SELECT * FROM digitaltwins", BasicDigitalTwin.class, new Context("key", "value"));

        queryResultBasicDigitalTwinWithContext.forEach(basicTwin -> System.out.println("Retrieved digitalTwin query result with Id: " + basicTwin.getId()));
        // END: com.azure.digitaltwins.core.syncclient.query#String-Context#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.syncclient.query#String-Context#String
        PagedIterable<String> queryResultStringWithContext = digitalTwinsSyncClient.query("SELECT * FROM digitaltwins", String.class, new Context("key", "value"));

        queryResultStringWithContext.forEach(queryResult -> System.out.println("Retrieved digitalTwin query result: " + queryResult));
        // END: com.azure.digitaltwins.core.syncclient.query#String-Context#String
    }
    //endregion QuerySnippets

    //region EventRouteSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#createEventRoute(String, EventRoute)}
     */
    @Override
    public void createEventRoute() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.createEventRoute#String-EventRoute
        String filter = "$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'";

        EventRoute eventRoute = new EventRoute("myEndpoitName").setFilter(filter);
        digitalTwinsSyncClient.createEventRoute("myEventRouteId", eventRoute);
        // END: com.azure.digitaltwins.core.syncclient.createEventRoute#String-EventRoute
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#createEventRouteWithResponse(String, EventRoute)}
     */
    @Override
    public void createEventRouteWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.createEventRouteWithResponse#String-EventRoute-Context
        String filter = "$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'";

        EventRoute eventRoute = new EventRoute("myEndpoitName").setFilter(filter);
        Response<Void> response = digitalTwinsSyncClient.createEventRouteWithResponse("myEventRouteId", eventRoute, new Context("key", "value"));

        System.out.println("Created an event rout with HTTP status code: " + response.getStatusCode());
        // END: com.azure.digitaltwins.core.syncclient.createEventRouteWithResponse#String-EventRoute-Context
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getEventRoute(String)}
     */
    @Override
    public void getEventRoute() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.getEventRoute#String
        EventRoute eventRoute = digitalTwinsSyncClient.getEventRoute("myEventRouteId");

        System.out.println("Retrieved event route with Id: " + eventRoute.getId());
        // END: BEGIN: com.azure.digitaltwins.core.syncclient.getEventRoute#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getEventRouteWithResponse(String)}
     */
    @Override
    public void getEventRouteWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.syncclient.getEventRouteWithResponse#String-Context
        Response<EventRoute> eventRouteWithResponse = digitalTwinsSyncClient.getEventRouteWithResponse("myEventRouteId", new Context("key", "value"));

        System.out.println("Received get event route operation response with HTTP status code: " + eventRouteWithResponse.getStatusCode());
        System.out.println("Retrieved event route with Id: " + eventRouteWithResponse.getValue().getId());
        // END: BEGIN: com.azure.digitaltwins.core.syncclient.getEventRouteWithResponse#String-Context
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteEventRoute(String)}
     */
    @Override
    public void deleteEventRoute() {

    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteEventRouteWithResponse(String)}
     */
    @Override
    public void deleteEventRouteWithResponse() {

    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#listEventRoutes()} and {@link DigitalTwinsAsyncClient#listEventRoutes(EventRoutesListOptions)}
     */
    @Override
    public void listEventRoutes() {

    }

    //endregion EventRouteSnippets
}
