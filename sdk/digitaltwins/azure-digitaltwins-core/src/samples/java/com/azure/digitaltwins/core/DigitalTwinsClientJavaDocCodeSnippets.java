package com.azure.digitaltwins.core;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.digitaltwins.core.models.*;
import com.azure.identity.ClientSecretCredentialBuilder;

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
        // BEGIN com.azure.digitaltwins.core.digitaltwinsclient.instantiation

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
     * Generates code samples for using {@link DigitalTwinsClient#listIncomingRelationships(String)}
     * and {@link DigitalTwinsClient#listIncomingRelationships(String, Context)}
     */
    @Override
    public void listIncomingRelationships() {

    }

    //endregion RelationshipSnippets
}
