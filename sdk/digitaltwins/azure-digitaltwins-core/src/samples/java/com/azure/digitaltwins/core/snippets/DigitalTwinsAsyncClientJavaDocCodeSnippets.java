package com.azure.digitaltwins.core.snippets;

import com.azure.digitaltwins.core.DigitalTwinsAsyncClient;
import com.azure.digitaltwins.core.DigitalTwinsClientBuilder;
import com.azure.digitaltwins.core.models.*;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Code snippets for {@link DigitalTwinsAsyncClient}
 */
public class DigitalTwinsAsyncClientJavaDocCodeSnippets extends CodeSnippetBase {

    private final DigitalTwinsAsyncClient digitalTwinsAsyncClient;

    DigitalTwinsAsyncClientJavaDocCodeSnippets(){
        digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();
    }

    public DigitalTwinsAsyncClient createDigitalTwinsAsyncClient() {

        String tenantId = getTenenatId();
        String clientId = getClientId();
        String clientSecret = getClientSecret();
        String digitalTwinsEndpointUrl = getEndpointUrl();

        // BEGIN: com.azure.digitaltwins.core.asyncClient.instantiation
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = new DigitalTwinsClientBuilder()
            .credential(
                new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build())
            .endpoint(digitalTwinsEndpointUrl)
            .buildAsyncClient();
        // END: com.azure.digitaltwins.core.asyncClient.instantiation

        return digitalTwinsAsyncClient;
    }

    //region DigitalTwinSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#createDigitalTwin(String, Object, Class)}
     */
    @Override
    public void createDigitalTwin() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();
        // BEGIN: com.azure.digitaltwins.core.asyncClient.createDigitalTwins#String-Object-Class#BasicDigitalTwin
        String modelId = "dtmi:samples:Building;1";

        BasicDigitalTwin basicTwin = new BasicDigitalTwin()
            .setId("myDigitalTwinId")
            .setMetadata(
                new DigitalTwinMetadata()
                    .setModelId(modelId)
            );

        digitalTwinsAsyncClient.createDigitalTwin(basicTwin.getId(), basicTwin, BasicDigitalTwin.class)
            .subscribe(response -> System.out.println("Created digital twin Id: " + response.getId()));
        // END: com.azure.digitaltwins.core.asyncClient.createDigitalTwins#String-Object-Class#BasicDigitalTwin

        String digitalTwinStringPayload = getDigitalTwinPayload();

        // BEGIN: com.azure.digitaltwins.core.asyncClient.createDigitalTwins#String-Object-Class#String
        digitalTwinsAsyncClient.createDigitalTwin("myDigitalTwinId", digitalTwinStringPayload, String.class)
            .subscribe(stringResponse -> System.out.println("Created digital twin: " + stringResponse));
        // END: com.azure.digitaltwins.core.asyncClient.createDigitalTwins#String-Object-Class#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#createDigitalTwinWithResponse(String, Object, Class, DigitalTwinsAddOptions)}
     */
    @Override
    public void createDigitalTwinWithResponse(){
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#BasicDigitalTwin
        String modelId = "dtmi:samples:Building;1";

        BasicDigitalTwin basicDigitalTwin = new BasicDigitalTwin()
            .setId("myDigitalTwinId")
            .setMetadata(
                new DigitalTwinMetadata()
                    .setModelId(modelId)
            );

        digitalTwinsAsyncClient.createDigitalTwinWithResponse(
            basicDigitalTwin.getId(),
            basicDigitalTwin,
            BasicDigitalTwin.class,
            new DigitalTwinsAddOptions())
            .subscribe(resultWithResponse ->
                System.out.println(
                    "Response http status: " +
                    resultWithResponse.getStatusCode() +
                    " created digital twin Id: " +
                    resultWithResponse.getValue().getId()));
        // END: com.azure.digitaltwins.core.asyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#BasicDigitalTwin

        String stringPayload = getDigitalTwinPayload();

        // BEGIN: com.azure.digitaltwins.core.asyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#String
        digitalTwinsAsyncClient.createDigitalTwinWithResponse(
            basicDigitalTwin.getId(),
            stringPayload,
            String.class,
            new DigitalTwinsAddOptions())
            .subscribe(stringWithResponse ->
                System.out.println(
                    "Response http status: " +
                    stringWithResponse.getStatusCode() +
                    " created digital twin: " +
                    stringWithResponse.getValue()));
        // END: com.azure.digitaltwins.core.asyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getDigitalTwin(String, Class)}
     */
    public void getDigitalTwin() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncClient.getDigitalTwin#String-Class#BasicDigitalTwin
        digitalTwinsAsyncClient.getDigitalTwin("myDigitalTwinId", BasicDigitalTwin.class)
            .subscribe(
                basicDigitalTwin -> System.out.println("Retrieved digital twin with Id: " + basicDigitalTwin.getId()));
        // END: com.azure.digitaltwins.core.asyncClient.getDigitalTwin#String-Class#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.asyncClient.getDigitalTwin#String-Class#String
        digitalTwinsAsyncClient.getDigitalTwin("myDigitalTwinId", String.class)
            .subscribe(stringResult -> System.out.println("Retrieved digital twin: " + stringResult));
        // END: com.azure.digitaltwins.core.asyncClient.getDigitalTwin#String-Class#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#getDigitalTwinWithResponse(String, Class, DigitalTwinsGetByIdOptions)}
     */
    @Override
    public void getDigitalTwinWithResponse() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncClient.getDigitalTwinWithResponse#String-Class-Options#BasicDigitalTwin
        digitalTwinsAsyncClient.getDigitalTwinWithResponse(
            "myDigitalTwinId",
            BasicDigitalTwin.class,
            new DigitalTwinsGetByIdOptions())
            .subscribe(
                basicDigitalTwinWithResponse -> System.out.println(
                    "Retrieved digital twin with Id: " + basicDigitalTwinWithResponse.getValue().getId() +
                    " Http Status Code: " + basicDigitalTwinWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.getDigitalTwinWithResponse#String-Class-Options#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.asyncClient.getDigitalTwinWithResponse#String-Class-Options#String
        digitalTwinsAsyncClient.getDigitalTwinWithResponse(
            "myDigitalTwinId",
            String.class,
            new DigitalTwinsGetByIdOptions())
            .subscribe(
                basicDigitalTwinWithResponse -> System.out.println(
                    "Retrieved digital twin: " + basicDigitalTwinWithResponse.getValue() +
                    " Http Status Code: " + basicDigitalTwinWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.getDigitalTwinWithResponse#String-Class-Options#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#updateDigitalTwin(String, List)}
     */
    @Override
    public void updateDigitalTwin() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncClient.updateDigitalTwin#String-List
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("Prop1", "newValue");

        digitalTwinsAsyncClient.updateDigitalTwin(
            "myDigitalTwinId",
            updateOperationUtility.getUpdateOperations())
        .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.updateDigitalTwin#String-List
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#updateDigitalTwinWithResponse(String, List, DigitalTwinsUpdateOptions)}
     */
    @Override
    public void updateDigitalTwinWithResponse() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncClient.updateDigitalTwinWithResponse#String-List-Options
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("Prop1", "newValue");

        digitalTwinsAsyncClient.updateDigitalTwinWithResponse(
            "myDigitalTwinId",
            updateOperationUtility.getUpdateOperations(),
            new DigitalTwinsUpdateOptions())
        .subscribe(updateResponse ->
            System.out.println("Update completed with HTTP status code: " + updateResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.updateDigitalTwinWithResponse#String-List-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteDigitalTwin(String)}
     */
    @Override
    public void deleteDigitalTwin() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncClient.deleteDigitalTwin#String
        digitalTwinsAsyncClient.deleteDigitalTwin("myDigitalTwinId")
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.deleteDigitalTwin#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#deleteDigitalTwinWithResponse(String, DigitalTwinsDeleteOptions)}
     */
    @Override
    public void deleteDigitalTwinWithResponse() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncClient.deleteDigitalTwinWithResponse#String-Options
        digitalTwinsAsyncClient.deleteDigitalTwinWithResponse(
            "myDigitalTwinId",
            new DigitalTwinsDeleteOptions())
        .subscribe(deleteResponse ->
            System.out.println("Deleted digital twin. HTTP response status code: " + deleteResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.deleteDigitalTwinWithResponse#String-Options
    }

    //endregion DigitalTwinSnippets

    //region RelationshipSnippets

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#createRelationship(String, String, Object, Class)}
     */
    @Override
    public void createRelationship() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.createRelationship#String-String-Object-Class#BasicRelationship
        BasicRelationship buildingToFloorBasicRelationship = new BasicRelationship()
            .setId("myRelationshipId")
            .setSourceId("mySourceDigitalTwinId")
            .setTargetId("myTargetDigitalTwinId")
            .setName("contains")
            .addCustomProperty("Prop1", "Prop1 value")
            .addCustomProperty("Prop2", 6);

        digitalTwinsAsyncClient.createRelationship(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            buildingToFloorBasicRelationship,
            BasicRelationship.class)
        .subscribe(createdRelationship -> System.out.println(
            "Created relationship with Id: " +
                createdRelationship.getId() +
                " from: " + createdRelationship.getSourceId() +
                " to: " + createdRelationship.getTargetId()));
        // END: com.azure.digitaltwins.core.asyncClient.createRelationship#String-String-Object-Class#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.asyncClient.createRelationship#String-String-Object-Class#String
        String relationshipPayload = getRelationshipPayload();

        digitalTwinsAsyncClient.createRelationship(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            relationshipPayload,
            String.class)
        .subscribe(createRelationshipString ->
            System.out.println("Created relationship: " + createRelationshipString));
        // END: com.azure.digitaltwins.core.asyncClient.createRelationship#String-String-Object-Class#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#createRelationshipWithResponse(String, String, Object, Class, DigitalTwinsAddRelationshipOptions)}
     */
    @Override
    public void createRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.createRelationshipWithResponse#String-String-Object-Class-Options#BasicRelationship
        BasicRelationship buildingToFloorBasicRelationship = new BasicRelationship()
            .setId("myRelationshipId")
            .setSourceId("mySourceDigitalTwinId")
            .setTargetId("myTargetDigitalTwinId")
            .setName("contains")
            .addCustomProperty("Prop1", "Prop1 value")
            .addCustomProperty("Prop2", 6);

        digitalTwinsAsyncClient.createRelationshipWithResponse(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            buildingToFloorBasicRelationship,
            BasicRelationship.class,
            new DigitalTwinsAddRelationshipOptions())
        .subscribe(createdRelationshipWithResponse -> System.out.println(
            "Created relationship with Id: " +
                createdRelationshipWithResponse.getValue().getId() +
                " from: " + createdRelationshipWithResponse.getValue().getSourceId() +
                " to: " + createdRelationshipWithResponse.getValue().getTargetId() +
                " Http status code: " +
                createdRelationshipWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.createRelationshipWithResponse#String-String-Object-Class-Options#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.asyncClient.createRelationshipWithResponse#String-String-Object-Class-Options#String
        String relationshipPayload = getRelationshipPayload();

        digitalTwinsAsyncClient.createRelationshipWithResponse(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            relationshipPayload,
            String.class,
            new DigitalTwinsAddRelationshipOptions())
        .subscribe(createdRelationshipStringWithResponse -> System.out.println(
            "Created relationship: " +
                createdRelationshipStringWithResponse +
                " With HTTP status code: " +
                createdRelationshipStringWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.createRelationshipWithResponse#String-String-Object-Class-Options#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getRelationship(String, String, Class)}
     */
    @Override
    public void getRelationship() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.getRelationship#String#BasicRelationship
        digitalTwinsAsyncClient.getRelationship(
            "myDigitalTwinId",
            "myRelationshipName",
            BasicRelationship.class)
            .subscribe(retrievedRelationship -> System.out.println(
                "Retrieved relationship with Id: "
                    + retrievedRelationship.getId() +
                    " from: " +
                    retrievedRelationship.getSourceId() +
                    " to: " + retrievedRelationship.getTargetId()));
        // END: com.azure.digitaltwins.core.asyncClient.getRelationship#String#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.asyncClient.getRelationship#String#String
        digitalTwinsAsyncClient.getRelationship(
            "myDigitalTwinId",
            "myRelationshipName",
            String.class)
            .subscribe(retrievedRelationshipString ->
                System.out.println("Retrieved relationship: " + retrievedRelationshipString));
        // END: com.azure.digitaltwins.core.asyncClient.getRelationship#String#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#getRelationshipWithResponse(String, String, Class, DigitalTwinsGetRelationshipByIdOptions)}
     */
    @Override
    public void getRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.getRelationshipWithResponse#String-String-Class-Options#BasicRelationship
        digitalTwinsAsyncClient.getRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipName",
            BasicRelationship.class,
            new DigitalTwinsGetRelationshipByIdOptions())
        .subscribe(retrievedRelationshipWithResponse -> System.out.println(
            "Retrieved relationship with Id: "
                + retrievedRelationshipWithResponse.getValue().getId() +
                " from: " +
                retrievedRelationshipWithResponse.getValue().getSourceId() +
                " to: " + retrievedRelationshipWithResponse.getValue().getTargetId() +
                "HTTP status code: " + retrievedRelationshipWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.getRelationshipWithResponse#String-String-Class-Options#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.asyncClient.getRelationshipWithResponse#String-String-Class-Options#String
        digitalTwinsAsyncClient.getRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipName",
            String.class,
            new DigitalTwinsGetRelationshipByIdOptions())
        .subscribe(retrievedRelationshipStringWithResponse -> System.out.println(
            "Retrieved relationship: " +
            retrievedRelationshipStringWithResponse +
            " HTTP status code: " +
            retrievedRelationshipStringWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.getRelationshipWithResponse#String-String-Class-Options#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#updateRelationship(String, String, List)}
     */
    @Override
    public void updateRelationship() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.updateRelationship#String-String-List
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/relationshipProperty1", "new property value");

        digitalTwinsAsyncClient.updateRelationship(
            "myDigitalTwinId",
            "myRelationshipId",
            updateOperationUtility.getUpdateOperations())
        .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.updateRelationship#String-String-List
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#updateRelationshipWithResponse(String, String, List, DigitalTwinsUpdateRelationshipOptions)}
     */
    @Override
    public void updateRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.updateRelationshipWithResponse#String-String-List-Options
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/relationshipProperty1", "new property value");

        digitalTwinsAsyncClient.updateRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipId",
            updateOperationUtility.getUpdateOperations(),
            new DigitalTwinsUpdateRelationshipOptions())
        .subscribe(updateResponse ->
            System.out.println(
                "Relationship updated with status code: " +
                updateResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.updateRelationshipWithResponse#String-String-List-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteRelationship(String, String)}
     */
    @Override
    public void deleteRelationship() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.deleteRelationship#String-String
        digitalTwinsAsyncClient.deleteRelationship("myDigitalTwinId", "myRelationshipId")
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.deleteRelationship#String-String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#deleteRelationshipWithResponse(String, String, DigitalTwinsDeleteRelationshipOptions)}
     */
    @Override
    public void deleteRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.deleteRelationshipWithResponse#String-String-Options
        digitalTwinsAsyncClient.deleteRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipId",
            new DigitalTwinsDeleteRelationshipOptions())
        .subscribe(deleteResponse ->
            System.out.println(
                "Deleted relationship with HTTP status code: " +
                deleteResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.deleteRelationshipWithResponse#String-String-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#listRelationships(String, Class)}
     * and {@link DigitalTwinsAsyncClient#listRelationships(String, String, Class, DigitalTwinsListRelationshipsOptions)}
     */
    @Override
    public void listRelationships() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.listRelationships#String-Class-Options#BasicRelationship#IterateByItem
        digitalTwinsAsyncClient.listRelationships("myDigitalTwinId", BasicRelationship.class)
            .doOnNext(basicRel -> System.out.println("Retrieved relationship with Id: " + basicRel.getId()));
        // END: com.azure.digitaltwins.core.asyncClient.listRelationships#String-Class-Options#BasicRelationship#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.asyncClient.listRelationships#String-Class-Options#String#IterateByItem
        digitalTwinsAsyncClient.listRelationships("myDigitalTwinId", String.class)
            .doOnNext(rel -> System.out.println("Retrieved relationship: " + rel));
        // END: com.azure.digitaltwins.core.asyncClient.listRelationships#String-Class-Options#String#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.asyncClient.listRelationships#String-String-Class-Options#BasicRelationship#IterateByItem
        digitalTwinsAsyncClient.listRelationships(
            "myDigitalTwinId",
            "myRelationshipName",
            BasicRelationship.class,
            new DigitalTwinsListRelationshipsOptions())
            .doOnNext(rel -> System.out.println("Retrieved relationship with Id: " + rel.getId()));
        // END: com.azure.digitaltwins.core.asyncClient.listRelationships#String-String-Class-Options#BasicRelationship#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.asyncClient.listRelationships#String-String-Class-Options#String#IterateByItem
        digitalTwinsAsyncClient.listRelationships(
            "myDigitalTwinId",
            "myRelationshipId",
            String.class,
            new DigitalTwinsListRelationshipsOptions())
            .doOnNext(rel -> System.out.println("Retrieved relationship: " + rel));
        // END: com.azure.digitaltwins.core.asyncClient.listRelationships#String-String-Class-Options#String#IterateByItem
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#listIncomingRelationships(String, DigitalTwinsListIncomingRelationshipsOptions)}
     */
    @Override
    public void listIncomingRelationships() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.listIncomingRelationships#String-Options
        digitalTwinsAsyncClient.listIncomingRelationships(
            "myDigitalTwinId",
            new DigitalTwinsListIncomingRelationshipsOptions())
            .doOnNext(incomingRel -> System.out.println(
                "Retrieved relationship with Id: " +
                incomingRel.getRelationshipId() +
                " from: " + incomingRel.getSourceId() +
                " to: myDigitalTwinId"))
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.listIncomingRelationships#String-Options
    }

    //endregion RelationshipSnippets

    //region ModelsSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#createModels(Iterable)}
     */
    @Override
    public void createModels() {
        String model1 = loadModelFromFile("model1");
        String model2 = loadModelFromFile("model2");
        String model3 = loadModelFromFile("model3");

        // BEGIN: com.azure.digitaltwins.core.asyncClient.createModels#Iterable
        digitalTwinsAsyncClient.createModels(Arrays.asList(model1, model2, model3))
            .subscribe(createdModels -> createdModels.forEach(model ->
                System.out.println("Retrieved model with Id: " + model.getId())));
        // END: com.azure.digitaltwins.core.asyncClient.createModels#Iterable
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#createModelsWithResponse(Iterable, DigitalTwinModelsAddOptions)}
     */
    @Override
    public void createModelsWithResponse() {
        String model1 = loadModelFromFile("model1");
        String model2 = loadModelFromFile("model2");
        String model3 = loadModelFromFile("model3");

        // BEGIN: com.azure.digitaltwins.core.asyncClient.createModelsWithResponse#Iterable-Options
        digitalTwinsAsyncClient.createModelsWithResponse(
            Arrays.asList(model1, model2, model3),
            new DigitalTwinModelsAddOptions())
            .subscribe(createdModels -> {
                System.out.println("Received a response with HTTP status code: " + createdModels.getStatusCode());
                createdModels.getValue().forEach(
                    model -> System.out.println("Retrieved model with Id: " + model.getId()));
            });
        // END: com.azure.digitaltwins.core.asyncClient.createModelsWithResponse#Iterable-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getModel(String)}
     */
    @Override
    public void getModel() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.getModel#String
        digitalTwinsAsyncClient.getModel("dtmi:samples:Building;1")
            .subscribe(model -> System.out.println("Retrieved model with Id: " + model.getId()));
        // END: com.azure.digitaltwins.core.asyncClient.getModel#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#getModelWithResponse(String, DigitalTwinModelsGetByIdOptions)}
     */
    @Override
    public void getModelWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.getModelWithResponse#String-Options
        digitalTwinsAsyncClient.getModelWithResponse(
            "dtmi:samples:Building;1",
            new DigitalTwinModelsGetByIdOptions())
            .subscribe(modelWithResponse -> {
                System.out.println("Received HTTP response with status code: " + modelWithResponse.getStatusCode());
                System.out.println("Retrieved model with Id: " + modelWithResponse.getValue().getId());
            });
        // END: com.azure.digitaltwins.core.asyncClient.getModelWithResponse#String-Options
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#listModels()} and
     * {@link DigitalTwinsAsyncClient#listModels(DigitalTwinModelsListOptions)} )}
     */
    @Override
    public void listModels() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.listModels
        digitalTwinsAsyncClient.listModels()
            .doOnNext(model -> System.out.println("Retrieved model with Id: " + model.getId()))
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.listModels

        // BEGIN: com.azure.digitaltwins.core.asyncClient.listModels#Options
        digitalTwinsAsyncClient.listModels(
            new DigitalTwinModelsListOptions()
                .setMaxItemsPerPage(5)
                .setIncludeModelDefinition(true))
            .doOnNext(model -> System.out.println("Retrieved model with Id: " + model.getId()))
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.listModels#Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#decommissionModel(String)}
     */
    @Override
    public void decommissionModel() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.decommissionModel#String
        digitalTwinsAsyncClient.decommissionModel("dtmi:samples:Building;1")
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.decommissionModel#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#decommissionModelWithResponse(String, DigitalTwinModelsUpdateOptions)}
     */
    @Override
    public void decommissionModelWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.decommissionModelWithResponse#String-Options
        digitalTwinsAsyncClient.decommissionModelWithResponse(
            "dtmi:samples:Building;1", new DigitalTwinModelsUpdateOptions())
        .subscribe(response ->
            System.out.println(
                "Received decommission model HTTP response with status:" +
                response.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.decommissionModelWithResponse#String-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteModel(String)}
     */
    @Override
    public void deleteModel() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.deleteModel#String
        digitalTwinsAsyncClient.deleteModel("dtmi:samples:Building;1")
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.deleteModel#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#deleteModelWithResponse(String, DigitalTwinModelsDeleteOptions)}
     */
    @Override
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.deleteModelWithResponse#String-Options
        digitalTwinsAsyncClient.deleteModelWithResponse(
            "dtmi:samples:Building;1",
            new DigitalTwinModelsDeleteOptions())
        .subscribe(response ->
            System.out.println(
                "Received delete model operation response with HTTP status code:" +
                response.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.deleteModelWithResponse#String-Options
    }

    //endregion ModelsSnippets

    //region ComponentSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getComponent(String, String, Class)}
     */
    @Override
    public void getComponent() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.getComponent#String-String-Class
        digitalTwinsAsyncClient.getComponent(
            "myDigitalTwinId",
            "myComponentPath",
            String.class)
        .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.getComponent#String-String-Class
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#getComponentWithResponse(String, String, Class, DigitalTwinsGetComponentOptions)}
     */
    @Override
    public void getComponentWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.getComponentWithResponse#String-String-Class-Options
        digitalTwinsAsyncClient.getComponentWithResponse(
            "myDigitalTwinId",
            "myComponentPath",
            String.class,
            new DigitalTwinsGetComponentOptions())
        .subscribe(response ->
            System.out.println(
                "Received component get operation response with HTTP status code: " +
                response.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.getComponentWithResponse#String-String-Class-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#updateComponent(String, String, List)}
     */
    @Override
    public void updateComponent() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.updateComponent#String-String-List
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/ComponentProp1", "Some new value");

        digitalTwinsAsyncClient.updateComponent(
            "myDigitalTwinId",
            "myComponentName",
            updateOperationUtility.getUpdateOperations())
        .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.updateComponent#String-String-List
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#updateComponentWithResponse(String, String, List, DigitalTwinsUpdateComponentOptions)}
     */
    @Override
    public void updateComponentWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.updateComponentWithResponse#String-String-List-Options
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/ComponentProp1", "Some new value");

        digitalTwinsAsyncClient.updateComponentWithResponse(
            "myDigitalTwinId",
            "myComponentName",
            updateOperationUtility.getUpdateOperations(),
            new DigitalTwinsUpdateComponentOptions().setIfMatch("*"))
        .subscribe(updateResponse ->
            System.out.println(
                "Received update operation response with HTTP status code: " +
                updateResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.updateComponentWithResponse#String-String-List-Options
    }

    //endregion ComponentSnippets

    //region QuerySnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#query(String, Class, QueryTwinsOptions)}
     */
    @Override
    public void query() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.query#String-Options#BasicDigitalTwin
        digitalTwinsAsyncClient.query(
            "SELECT * FROM digitaltwins",
            BasicDigitalTwin.class,
            new QueryTwinsOptions().setMaxItemsPerPage(5))
            .doOnNext(
                basicTwin -> System.out.println("Retrieved digitalTwin query result with Id: " + basicTwin.getId()))
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.query#String-Options#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.asyncClient.query#String-Options#String
        digitalTwinsAsyncClient.query(
            "SELECT * FROM digitaltwins",
            String.class,
            new QueryTwinsOptions().setMaxItemsPerPage(5))
            .doOnNext(twinString -> System.out.println("Retrieved digitalTwin query result with Id: " + twinString))
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.query#String-Options#String
    }

    //endregion QuerySnippets

    //region EventRouteSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#createEventRoute(String, EventRoute)}
     */
    @Override
    public void createEventRoute() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.createEventRoute#String-EventRoute
        String filter =
            "$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'";

        EventRoute eventRoute = new EventRoute("myEndpointName").setFilter(filter);
        digitalTwinsAsyncClient.createEventRoute("myEventRouteId", eventRoute).subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.createEventRoute#String-EventRoute
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#createEventRouteWithResponse(String, EventRoute, EventRoutesAddOptions)}
     */
    @Override
    public void createEventRouteWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.createEventRouteWithResponse#String-EventRoute-Options
        String filter =
            "$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'";

        EventRoute eventRoute = new EventRoute("myEndpointName").setFilter(filter);
        digitalTwinsAsyncClient.createEventRouteWithResponse(
            "myEventRouteId",
            eventRoute,
            new EventRoutesAddOptions())
            .subscribe(response ->
                System.out.println("Created an event rout with HTTP status code: " + response.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.createEventRouteWithResponse#String-EventRoute-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getEventRoute(String)}
     */
    @Override
    public void getEventRoute() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.getEventRoute#String
        digitalTwinsAsyncClient.getEventRoute("myEventRouteId")
            .subscribe(eventRoute -> System.out.println("Retrieved event route with Id: " + eventRoute.getId()));
        // END: com.azure.digitaltwins.core.asyncClient.getEventRoute#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#getEventRouteWithResponse(String, EventRoutesGetByIdOptions)}
     */
    @Override
    public void getEventRouteWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.getEventRouteWithResponse#String-Options
        digitalTwinsAsyncClient.getEventRouteWithResponse(
            "myEventRouteId",
            new EventRoutesGetByIdOptions())
        .subscribe(eventRouteWithResponse -> {
            System.out.println(
                "Received get event route operation response with HTTP status code: " +
                eventRouteWithResponse.getStatusCode() );
            System.out.println(
                "Retrieved event route with Id: " +
                eventRouteWithResponse.getValue().getId());
        } );
        // END: com.azure.digitaltwins.core.asyncClient.getEventRouteWithResponse#String-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteEventRoute(String)}
     */
    @Override
    public void deleteEventRoute() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.deleteEventRoute#String
        digitalTwinsAsyncClient.deleteEventRoute("myEventRouteId")
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.deleteEventRoute#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#deleteEventRouteWithResponse(String, EventRoutesDeleteOptions)}
     */
    @Override
    public void deleteEventRouteWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.deleteEventRouteWithResponse#String-Options
        digitalTwinsAsyncClient.deleteEventRouteWithResponse(
            "myEventRouteId",
            new EventRoutesDeleteOptions())
            .subscribe(deleteResponse ->
                System.out.println(
                    "Received delete event route operation response with HTTP status code: " +
                    deleteResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.deleteEventRouteWithResponse#String-Options
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#listEventRoutes()} and
     * {@link DigitalTwinsAsyncClient#listEventRoutes(EventRoutesListOptions)}
     */
    @Override
    public void listEventRoutes() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.listEventRoutes
        digitalTwinsAsyncClient.listEventRoutes()
            .doOnNext(eventRoute -> System.out.println("Retrieved event route with Id: " + eventRoute.getId()))
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.listEventRoutes

        // BEGIN: com.azure.digitaltwins.core.asyncClient.listEventRoutes#Options
        digitalTwinsAsyncClient.listEventRoutes(new EventRoutesListOptions().setMaxItemsPerPage(5))
            .doOnNext(eventRoute -> System.out.println("Retrieved event route with Id: " + eventRoute.getId()))
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.listEventRoutes#Options
    }

    //endregion EventRouteSnippets

    //region TelemetrySnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#publishTelemetry(String, String, Object)}
     */
    @Override
    public void publishTelemetry() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.publishTelemetry#String-String-Object#String
        digitalTwinsAsyncClient.publishTelemetry(
            "myDigitalTwinId",
            UUID.randomUUID().toString(),
            "{\"Telemetry1\": 5}")
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.publishTelemetry#String-String-Object#String

        // BEGIN: com.azure.digitaltwins.core.asyncClient.publishTelemetry#String-String-Object#Object
        Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
        telemetryPayload.put("Telemetry1", 5);

        digitalTwinsAsyncClient.publishTelemetry(
            "myDigitalTwinId",
            UUID.randomUUID().toString(),
            telemetryPayload)
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.publishTelemetry#String-String-Object#Object
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#publishTelemetryWithResponse(String, String, Object, DigitalTwinsSendTelemetryOptions)}
     */
    @Override
    public void publishTelemetryWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.publishTelemetryWithResponse#String-String-Object-Options#String
        digitalTwinsAsyncClient.publishTelemetryWithResponse(
            "myDigitalTwinId",
            UUID.randomUUID().toString(),
            "{\"Telemetry1\": 5}",
            new DigitalTwinsSendTelemetryOptions().setTimestamp(OffsetDateTime.now(ZoneId.systemDefault())))
        .subscribe(responseString ->
            System.out.println(
                "Received publish telemetry operation response with HTTP status code: " +
                responseString.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.publishTelemetryWithResponse#String-String-Object-Options#String

        // BEGIN: com.azure.digitaltwins.core.asyncClient.publishTelemetryWithResponse#String-String-Object-Options#Object
        Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
        telemetryPayload.put("Telemetry1", 5);

        digitalTwinsAsyncClient.publishTelemetryWithResponse(
            "myDigitalTwinId",
            UUID.randomUUID().toString(),
            telemetryPayload,
            new DigitalTwinsSendTelemetryOptions().setTimestamp(OffsetDateTime.now(ZoneId.systemDefault())))
        .subscribe(responseObject ->
            System.out.println(
                "Received publish telemetry operation response with HTTP status code: " +
                responseObject.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.publishTelemetryWithResponse#String-String-Object-Options#Object
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#publishComponentTelemetry(String, String, String, Object)}
     */
    @Override
    public void publishComponentTelemetry() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.publishComponentTelemetry#String-String-String-Object#String
        digitalTwinsAsyncClient.publishComponentTelemetry(
            "myDigitalTwinId",
            "myComponentName",
            UUID.randomUUID().toString(),
            "{\"Telemetry1\": 5}")
        .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.publishComponentTelemetry#String-String-String-Object#String

        // BEGIN: com.azure.digitaltwins.core.asyncClient.publishComponentTelemetry#String-String-String-Object#Object
        Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
        telemetryPayload.put("Telemetry1", 5);

        digitalTwinsAsyncClient.publishComponentTelemetry(
            "myDigitalTwinId",
            "myComponentName",
            UUID.randomUUID().toString(),
            telemetryPayload)
        .subscribe();
        // END: com.azure.digitaltwins.core.asyncClient.publishComponentTelemetry#String-String-String-Object#Object
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#publishComponentTelemetryWithResponse(String, String, String, Object, DigitalTwinsSendComponentTelemetryOptions)}
     */
    @Override
    public void publishComponentTelemetryWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#String
        digitalTwinsAsyncClient.publishComponentTelemetryWithResponse(
            "myDigitalTwinId",
            "myComponentName",
            UUID.randomUUID().toString(),
            "{\"Telemetry1\": 5}",
            new DigitalTwinsSendComponentTelemetryOptions().setTimestamp(OffsetDateTime.now(ZoneId.systemDefault())))
            .subscribe(responseString ->
                System.out.println(
                    "Received publish component telemetry operation response with HTTP status code: " +
                    responseString.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#String

        // BEGIN: com.azure.digitaltwins.core.asyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#Object
        Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
        telemetryPayload.put("Telemetry1", 5);

        digitalTwinsAsyncClient.publishComponentTelemetryWithResponse(
            "myDigitalTwinId",
            "myComponentName",
            UUID.randomUUID().toString(),
            telemetryPayload,
            new DigitalTwinsSendComponentTelemetryOptions().setTimestamp(OffsetDateTime.now(ZoneId.systemDefault())))
            .subscribe(responseObject ->
                System.out.println(
                    "Received publish component telemetry operation response with HTTP status code: " +
                    responseObject.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#Object
    }

    //endregion TelemetrySnippets
}
