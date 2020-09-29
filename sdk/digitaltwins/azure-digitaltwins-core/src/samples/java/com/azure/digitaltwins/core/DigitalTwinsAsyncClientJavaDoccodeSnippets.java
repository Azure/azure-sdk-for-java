package com.azure.digitaltwins.core;

import com.azure.core.util.Context;
import com.azure.digitaltwins.core.models.*;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.util.List;

/**
 * Code snippets for {@link DigitalTwinsAsyncClient}
 */
public class DigitalTwinsAsyncClientJavaDoccodeSnippets extends CodeSnippetBase {

    private DigitalTwinsAsyncClient digitalTwinsAsyncClient;

    DigitalTwinsAsyncClientJavaDoccodeSnippets(){
        digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();
    }

    public DigitalTwinsAsyncClient createDigitalTwinsAsyncClient() {

        String tenantId = getTenenatId();
        String clientId = getClientId();
        String clientSecret = getClientSecret();
        String digitalTwinsEndpointUrl = getEndpointUrl();

        // BEGIN com.azure.digitaltwins.core.asyncclient.instantiation
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = new DigitalTwinsClientBuilder()
            .credential(
                new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build())
            .endpoint(digitalTwinsEndpointUrl)
            .buildAsyncClient();
        // BEGIN com.azure.digitaltwins.core.digitaltwinsclient.instantiation

        return digitalTwinsAsyncClient;
    }

    //region DigitalTwinSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#createDigitalTwin(String, Object, Class)}
     */
    @Override
    public void createDigitalTwin() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();
        // BEGIN: com.azure.digitaltwins.core.asyncclient.createDigitalTwins#String-Object-Class#BasicDigitalTwin
        String modelId = "dtmi:samples:Building;1";

        BasicDigitalTwin basicTwin = new BasicDigitalTwin()
            .setId("myDigitalTwinId")
            .setMetadata(
                new DigitalTwinMetadata()
                    .setModelId(modelId)
            );

        digitalTwinsAsyncClient.createDigitalTwin(basicTwin.getId(), basicTwin, BasicDigitalTwin.class)
            .subscribe(response -> System.out.println("Created digital twin Id: " + response.getId()));
        // END: com.azure.digitaltwins.core.asyncclient.createDigitalTwins#String-Object-Class#BasicDigitalTwin

        String digitalTwinStringPayload = getDigitalTwinPayload();

        // BEGIN: com.azure.digitaltwins.core.asyncclient.createDigitalTwins#String-Object-Class#String
        digitalTwinsAsyncClient.createDigitalTwin("myDigitalTwinId", digitalTwinStringPayload, String.class)
            .subscribe(stringResponse -> System.out.println("Created digital twin: " + stringResponse));
        // END: com.azure.digitaltwins.core.asyncclient.createDigitalTwins#String-Object-Class#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#createDigitalTwinWithResponse(String, Object, Class, Context)}
     */
    @Override
    public void createDigitalTwinWithResponse(){
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncclient.createDigitalTwinsWithResponse#String-Object-Class#BasicDigitalTwin
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
            new Context("Key", "Value"))
            .subscribe(resultWithResponse -> System.out.println("Response http status: " + resultWithResponse.getStatusCode() + " created digital twin Id: " + resultWithResponse.getValue().getId()));
        // END: com.azure.digitaltwins.core.asyncclient.createDigitalTwinsWithResponse#String-Object-Class#BasicDigitalTwin

        String stringPayload = getDigitalTwinPayload();

        // BEGIN: com.azure.digitaltwins.core.asyncclient.createDigitalTwinsWithResponse#String-Object-Class#String
        digitalTwinsAsyncClient.createDigitalTwinWithResponse(
            basicDigitalTwin.getId(),
            stringPayload,
            String.class,
            new Context("Key", "Value"))
            .subscribe(stringWithResponse -> System.out.println("Response http status: " + stringWithResponse.getStatusCode() + " created digital twin: " + stringWithResponse.getValue()));
        // END: com.azure.digitaltwins.core.asyncclient.createDigitalTwinsWithResponse#String-Object-Class#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getDigitalTwin(String, Class)}
     */
    public void getDigitalTwin() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncclient.getDigitalTwin#String-Class#BasicDigitalTwin
        digitalTwinsAsyncClient.getDigitalTwin("myDigitalTwinId", BasicDigitalTwin.class)
            .subscribe(basicDigitalTwin -> System.out.println("Retrieved digital twin with Id: " + basicDigitalTwin.getId()));
        // END: com.azure.digitaltwins.core.asyncclient.getDigitalTwin#String-Class#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.asyncclient.getDigitalTwin#String-Class#String
        digitalTwinsAsyncClient.getDigitalTwin("myDigitalTwinId", String.class)
            .subscribe(stringResult -> System.out.println("Retrieved digital twin: " + stringResult));
        // END: com.azure.digitaltwins.core.asyncclient.getDigitalTwin#String-Class#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getDigitalTwinWithResponse(String, Class, Context)}
     */
    @Override
    public void getDigitalTwinWithResponse() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncclient.getDigitalTwinWithResponse#String-Class#BasicDigitalTwin
        digitalTwinsAsyncClient.getDigitalTwinWithResponse("myDigitalTwinId", BasicDigitalTwin.class)
            .subscribe(
                basicDigitalTwinWithResponse -> System.out.println(
                    "Retrieved digital twin with Id: " + basicDigitalTwinWithResponse.getValue().getId() +
                    " Http Status Code: " + basicDigitalTwinWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncclient.getDigitalTwinWithResponse#String-Class#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.asyncclient.getDigitalTwinWithResponse#String-Class#String
        digitalTwinsAsyncClient.getDigitalTwinWithResponse("myDigitalTwinId", String.class)
            .subscribe(
                basicDigitalTwinWithResponse -> System.out.println(
                    "Retrieved digital twin: " + basicDigitalTwinWithResponse.getValue() +
                    " Http Status Code: " + basicDigitalTwinWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncclient.getDigitalTwinWithResponse#String-Class#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#updateDigitalTwin(String, List)}
     */
    @Override
    public void updateDigitalTwin() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncclient.updateDigitalTwin#String-List
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("Prop1", "newValue");

        digitalTwinsAsyncClient.updateDigitalTwin("myDigitalTwinId", updateOperationUtility.getUpdateOperations())
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncclient.updateDigitalTwin#String-List
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#updateDigitalTwinWithResponse(String, List, UpdateDigitalTwinRequestOptions)}
     */
    @Override
    public void updateDigitalTwinWithResponse() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncclient.updateDigitalTwinWithResponse#String-List-Options
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("Prop1", "newValue");

        digitalTwinsAsyncClient.updateDigitalTwinWithResponse(
            "myDigitalTwinId",
            updateOperationUtility.getUpdateOperations(),
            new UpdateDigitalTwinRequestOptions(),
            new Context("key", "value"))
        .subscribe(updateResponse -> System.out.println("Update completed with HTTP status code: " + updateResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncclient.updateDigitalTwinWithResponse#String-List-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteDigitalTwin(String)}
     */
    @Override
    public void deleteDigitalTwin() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncclient.deleteDigitalTwin#String
        digitalTwinsAsyncClient.deleteDigitalTwin("myDigitalTwinId")
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncclient.deleteDigitalTwin#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteDigitalTwinWithResponse(String, DeleteDigitalTwinRequestOptions)}
     */
    @Override
    public void deleteDigitalTwinWithResponse() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.asyncclient.deleteDigitalTwinWithResponse#String-Options
        digitalTwinsAsyncClient.deleteDigitalTwinWithResponse(
            "myDigitalTwinId",
            new DeleteDigitalTwinRequestOptions(),
            new Context("key", "value"))
        .subscribe(deleteResponse -> System.out.println("Deleted digital twin. HTTP response status code: " + deleteResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncclient.deleteDigitalTwinWithResponse#String-Options
    }

    //endregion DigitalTwinSnippets

    //region RelationshipSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#createRelationship(String, String, Object, Class)}
     */
    @Override
    public void createRelationship() {
        // BEGIN: com.azure.digitaltwins.core.asyncclient.createRelationship#String-String-Object-Class#BasicRelationship
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
        // END: com.azure.digitaltwins.core.asyncclient.createRelationship#String-String-Object-Class#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.asyncclient.createRelationship#String-String-Object-Class#String
        String relationshipPayload = getRelationshipPayload();

        digitalTwinsAsyncClient.createRelationship(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            relationshipPayload,
            String.class)
        .subscribe(createRelationshipString -> System.out.println("Created relationship: " + createRelationshipString));
        // END: com.azure.digitaltwins.core.asyncclient.createRelationship#String-String-Object-Class#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#createRelationshipWithResponse(String, String, Object, Class)}
     */
    @Override
    public void createRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncclient.createRelationshipWithResponse#String-String-Object-Class#BasicRelationship
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
            BasicRelationship.class)
        .subscribe(createdRelationshipWithResponse -> System.out.println(
            "Created relationship with Id: " +
                createdRelationshipWithResponse.getValue().getId() +
                " from: " + createdRelationshipWithResponse.getValue().getSourceId() +
                " to: " + createdRelationshipWithResponse.getValue().getTargetId() +
                " Http status code: " +
                createdRelationshipWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncclient.createRelationshipWithResponse#String-String-Object-Class#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.asyncclient.createRelationshipWithResponse#String-String-Object-Class#String
        String relationshipPayload = getRelationshipPayload();

        digitalTwinsAsyncClient.createRelationshipWithResponse(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            relationshipPayload,
            String.class)
        .subscribe(createdRelationshipStringWithResponse -> System.out.println(
            "Created relationship: " +
                createdRelationshipStringWithResponse +
                " With HTTP status code: " +
                createdRelationshipStringWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncclient.createRelationshipWithResponse#String-String-Object-Class#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getRelationship(String, String, Class)}
     */
    @Override
    public void getRelationship() {
        // BEGIN: com.azure.digitaltwins.core.asyncclient.getRelationship#String#BasicRelationship
        digitalTwinsAsyncClient.getRelationship("myDigitalTwinId", "myRelationshipName", BasicRelationship.class)
            .subscribe(retrievedRelationship -> System.out.println(
                "Retrieved relationship with Id: "
                    + retrievedRelationship.getId() +
                    " from: " +
                    retrievedRelationship.getSourceId() +
                    " to: " + retrievedRelationship.getTargetId()));
        // END: com.azure.digitaltwins.core.asyncclient.getRelationship#String#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.asyncclient.getRelationship#String#String
        digitalTwinsAsyncClient.getRelationship("myDigitalTwinId", "myRelationshipName", String.class)
            .subscribe(retrievedRelationshipString -> System.out.println("Retrieved relationship: " + retrievedRelationshipString));
        // END: com.azure.digitaltwins.core.asyncclient.getRelationship#String#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getRelationshipWithResponse(String, String, Class)}
     */
    @Override
    public void getRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncclient.getRelationshipWithResponse#String-String-Class#BasicRelationship
        digitalTwinsAsyncClient.getRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipName",
            BasicRelationship.class,
            new Context("key", "value"))
        .subscribe(retrievedRelationshipWithResponse -> System.out.println(
            "Retrieved relationship with Id: "
                + retrievedRelationshipWithResponse.getValue().getId() +
                " from: " +
                retrievedRelationshipWithResponse.getValue().getSourceId() +
                " to: " + retrievedRelationshipWithResponse.getValue().getTargetId() +
                "HTTP status code: " + retrievedRelationshipWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncclient.getRelationshipWithResponse#String-String-Class#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.asyncclient.getRelationshipWithResponse#String-String-Class#String
        digitalTwinsAsyncClient.getRelationshipWithResponse("myDigitalTwinId", "myRelationshipName", String.class, new Context("key", "value"))
            .subscribe(retrievedRelationshipStringWithResponse -> System.out.println(
                "Retrieved relationship: " +
                retrievedRelationshipStringWithResponse +
                " HTTP status code: " +
                retrievedRelationshipStringWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncclient.getRelationshipWithResponse#String-String-Class#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#updateRelationship(String, String, List)}
     */
    @Override
    public void updateRelationship() {
        // BEGIN: com.azure.digitaltwins.core.asyncclient.updateRelationship#String-String-List
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/relationshipProperty1", "new property value");

        digitalTwinsAsyncClient.updateRelationship("myDigitalTwinId", "myRelationshipId", updateOperationUtility.getUpdateOperations())
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncclient.updateRelationship#String-String-List
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#updateRelationshipWithResponse(String, String, List, UpdateRelationshipRequestOptions)}
     */
    @Override
    public void updateRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncclient.updateRelationshipWithResponse#String-String-List-Options
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility();
        updateOperationUtility.appendReplaceOperation("/relationshipProperty1", "new property value");

        digitalTwinsAsyncClient.updateRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipId",
            updateOperationUtility.getUpdateOperations(),
            new UpdateRelationshipRequestOptions())
        .subscribe(updateResponse -> System.out.println("Relationship updated with status code: " + updateResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncclient.updateRelationshipWithResponse#String-String-List-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteRelationship(String, String)}
     */
    @Override
    public void deleteRelationship() {
        // BEGIN: com.azure.digitaltwins.core.asyncclient.deleteRelationship#String-String
        digitalTwinsAsyncClient.deleteRelationship("myDigitalTwinId", "myRelationshipId")
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncclient.deleteRelationship#String-String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteRelationshipWithResponse(String, String, DeleteRelationshipRequestOptions)}
     */
    @Override
    public void deleteRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.asyncclient.deleteRelationshipWithResponse#String-String-Options
        digitalTwinsAsyncClient.deleteRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipId",
            new DeleteRelationshipRequestOptions(),
            new Context("key", "value"))
        .subscribe(deleteResponse -> System.out.println("Deleted relationship with HTTP status code: " + deleteResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.asyncclient.deleteRelationshipWithResponse#String-String-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#listRelationships(String, Class)}
     * and {@link DigitalTwinsAsyncClient#listRelationships(String, String, Class)}
     */
    @Override
    public void listRelationships() {
        // BEGIN: com.azure.digitaltwins.core.asyncclient.listRelationships#String-Class#BasicRelationship#IterateByItem
        digitalTwinsAsyncClient.listRelationships("myDigitalTwinId", BasicRelationship.class)
            .doOnNext(basicRel -> System.out.println("Retrieved relationship with Id: " + basicRel.getId()));
        // END: com.azure.digitaltwins.core.syncclient.listRelationships#String-Class#BasicRelationship#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.asyncclient.listRelationships#String-Class#String#IterateByItem
        digitalTwinsAsyncClient.listRelationships("myDigitalTwinId", String.class)
            .doOnNext(rel -> System.out.println("Retrieved relationship: " + rel));
        // END: com.azure.digitaltwins.core.asyncclient.listRelationships#String-Class#String#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.asyncclient.listRelationships#String-String-Class#BasicRelationship#IterateByItem
        digitalTwinsAsyncClient.listRelationships(
            "myDigitalTwinId",
            "myRelationshipName",
            BasicRelationship.class)
            .doOnNext(rel -> System.out.println("Retrieved relationship with Id: " + rel.getId()));
        // END: com.azure.digitaltwins.core.asyncclient.listRelationships#String-String-Class#BasicRelationship#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.asyncclient.listRelationships#String-String-Class#String#IterateByItem
        digitalTwinsAsyncClient.listRelationships(
            "myDigitalTwinId",
            "myRelationshipId",
            String.class)
            .doOnNext(rel -> System.out.println("Retrieved relationship: " + rel));
        // END: com.azure.digitaltwins.core.asyncclient.listRelationships#String-String-Class#String#IterateByItem
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#listIncomingRelationships(String)}
     */
    @Override
    public void listIncomingRelationships() {
        // BEGIN: com.azure.digitaltwins.core.asyncclient.listIncomingRelationships#String
        digitalTwinsAsyncClient.listIncomingRelationships("myDigitalTwinId")
            .doOnNext(incomingRel -> System.out.println("Retrieved relationship with Id: " + incomingRel.getRelationshipId() + " from: " + incomingRel.getSourceId() + " to: myDigitalTwinId"))
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncclient.listIncomingRelationships#String

        // BEGIN: com.azure.digitaltwins.core.asyncclient.listIncomingRelationships#String-Context
        digitalTwinsAsyncClient.listIncomingRelationships("myDigitalTwinId", new Context("key", "value"))
            .doOnNext(incomingRel -> System.out.println("Retrieved relationship with Id: " + incomingRel.getRelationshipId() + " from: " + incomingRel.getSourceId() + " to: myDigitalTwinId"))
            .subscribe();
        // END: com.azure.digitaltwins.core.asyncclient.listIncomingRelationships#String-Context
    }

    //endregion RelationshipSnippets
}
