// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.snippets;

import com.azure.core.models.JsonPatchDocument;
import com.azure.digitaltwins.core.BasicDigitalTwin;
import com.azure.digitaltwins.core.BasicDigitalTwinMetadata;
import com.azure.digitaltwins.core.BasicRelationship;
import com.azure.digitaltwins.core.DigitalTwinsAsyncClient;
import com.azure.digitaltwins.core.DigitalTwinsClientBuilder;
import com.azure.digitaltwins.core.models.CreateOrReplaceDigitalTwinOptions;
import com.azure.digitaltwins.core.models.CreateOrReplaceRelationshipOptions;
import com.azure.digitaltwins.core.models.DeleteDigitalTwinOptions;
import com.azure.digitaltwins.core.models.DeleteRelationshipOptions;
import com.azure.digitaltwins.core.models.DigitalTwinsEventRoute;
import com.azure.digitaltwins.core.models.ListDigitalTwinsEventRoutesOptions;
import com.azure.digitaltwins.core.models.ListModelsOptions;
import com.azure.digitaltwins.core.models.PublishComponentTelemetryOptions;
import com.azure.digitaltwins.core.models.PublishTelemetryOptions;
import com.azure.digitaltwins.core.models.QueryOptions;
import com.azure.digitaltwins.core.models.UpdateComponentOptions;
import com.azure.digitaltwins.core.models.UpdateDigitalTwinOptions;
import com.azure.digitaltwins.core.models.UpdateRelationshipOptions;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

/**
 * Code snippets for {@link DigitalTwinsAsyncClient}
 */
public class DigitalTwinsAsyncClientJavaDocCodeSnippets extends CodeSnippetBase {

    private final DigitalTwinsAsyncClient digitalTwinsAsyncClient;

    DigitalTwinsAsyncClientJavaDocCodeSnippets() {
        digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();
    }

    public DigitalTwinsAsyncClient createDigitalTwinsAsyncClient() {

        String tenantId = getTenenatId();
        String clientId = getClientId();
        String clientSecret = getClientSecret();
        String digitalTwinsEndpointUrl = getEndpointUrl();

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.instantiation
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = new DigitalTwinsClientBuilder()
            .credential(
                new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build())
            .endpoint(digitalTwinsEndpointUrl)
            .buildAsyncClient();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.instantiation

        return digitalTwinsAsyncClient;
    }

    //region DigitalTwinSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#createOrReplaceDigitalTwin(String, Object, Class)}
     */
    @Override
    public void createDigitalTwin() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwins#String-Object-Class#BasicDigitalTwin
        String modelId = "dtmi:com:samples:Building;1";

        BasicDigitalTwin basicTwin = new BasicDigitalTwin("myDigitalTwinId")
            .setMetadata(
                new BasicDigitalTwinMetadata()
                    .setModelId(modelId)
            );

        digitalTwinsAsyncClient.createOrReplaceDigitalTwin(basicTwin.getId(), basicTwin, BasicDigitalTwin.class)
            .subscribe(response -> System.out.println("Created digital twin Id: " + response.getId()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwins#String-Object-Class#BasicDigitalTwin

        String digitalTwinStringPayload = getDigitalTwinPayload();

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwins#String-Object-Class#String
        digitalTwinsAsyncClient.createOrReplaceDigitalTwin("myDigitalTwinId", digitalTwinStringPayload, String.class)
            .subscribe(stringResponse -> System.out.println("Created digital twin: " + stringResponse));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwins#String-Object-Class#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#createOrReplaceDigitalTwinWithResponse(String, Object, Class, CreateOrReplaceDigitalTwinOptions)}
     */
    @Override
    public void createDigitalTwinWithResponse() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#BasicDigitalTwin
        String modelId = "dtmi:com:samples:Building;1";

        BasicDigitalTwin basicDigitalTwin = new BasicDigitalTwin("myDigitalTwinId")
            .setMetadata(
                new BasicDigitalTwinMetadata()
                    .setModelId(modelId)
            );

        digitalTwinsAsyncClient.createOrReplaceDigitalTwinWithResponse(
            basicDigitalTwin.getId(),
            basicDigitalTwin,
            BasicDigitalTwin.class,
            new CreateOrReplaceDigitalTwinOptions())
            .subscribe(resultWithResponse ->
                System.out.println(
                    "Response http status: "
                    + resultWithResponse.getStatusCode()
                    + " created digital twin Id: "
                    + resultWithResponse.getValue().getId()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#BasicDigitalTwin

        String stringPayload = getDigitalTwinPayload();

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#String
        digitalTwinsAsyncClient.createOrReplaceDigitalTwinWithResponse(
            basicDigitalTwin.getId(),
            stringPayload,
            String.class,
            new CreateOrReplaceDigitalTwinOptions())
            .subscribe(stringWithResponse ->
                System.out.println(
                    "Response http status: "
                    + stringWithResponse.getStatusCode()
                    + " created digital twin: "
                    + stringWithResponse.getValue()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getDigitalTwin(String, Class)}
     */
    public void getDigitalTwin() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwin#String-Class#BasicDigitalTwin
        digitalTwinsAsyncClient.getDigitalTwin("myDigitalTwinId", BasicDigitalTwin.class)
            .subscribe(
                basicDigitalTwin -> System.out.println("Retrieved digital twin with Id: " + basicDigitalTwin.getId()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwin#String-Class#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwin#String-Class#String
        digitalTwinsAsyncClient.getDigitalTwin("myDigitalTwinId", String.class)
            .subscribe(stringResult -> System.out.println("Retrieved digital twin: " + stringResult));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwin#String-Class#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#getDigitalTwinWithResponse(String, Class)}
     */
    @Override
    public void getDigitalTwinWithResponse() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwinWithResponse#String-Class-Options#BasicDigitalTwin
        digitalTwinsAsyncClient.getDigitalTwinWithResponse(
            "myDigitalTwinId",
            BasicDigitalTwin.class)
            .subscribe(
                basicDigitalTwinWithResponse -> System.out.println(
                    "Retrieved digital twin with Id: " + basicDigitalTwinWithResponse.getValue().getId()
                    + " Http Status Code: " + basicDigitalTwinWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwinWithResponse#String-Class-Options#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwinWithResponse#String-Class-Options#String
        digitalTwinsAsyncClient.getDigitalTwinWithResponse(
            "myDigitalTwinId",
            String.class)
            .subscribe(
                basicDigitalTwinWithResponse -> System.out.println(
                    "Retrieved digital twin: " + basicDigitalTwinWithResponse.getValue()
                    + " Http Status Code: " + basicDigitalTwinWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwinWithResponse#String-Class-Options#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#updateDigitalTwin(String, JsonPatchDocument)}
     */
    @Override
    public void updateDigitalTwin() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateDigitalTwin#String-JsonPatchDocument
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        jsonPatchDocument.appendReplace("Prop1", "newValue");

        digitalTwinsAsyncClient.updateDigitalTwin(
            "myDigitalTwinId",
            jsonPatchDocument)
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateDigitalTwin#String-JsonPatchDocument
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#updateDigitalTwinWithResponse(String, JsonPatchDocument, UpdateDigitalTwinOptions)}
     */
    @Override
    public void updateDigitalTwinWithResponse() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateDigitalTwinWithResponse#String-JsonPatchDocument-UpdateDigitalTwinOptions
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        jsonPatchDocument.appendReplace("Prop1", "newValue");

        digitalTwinsAsyncClient.updateDigitalTwinWithResponse(
            "myDigitalTwinId",
            jsonPatchDocument,
            new UpdateDigitalTwinOptions())
            .subscribe(updateResponse ->
                System.out.println("Update completed with HTTP status code: " + updateResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateDigitalTwinWithResponse#String-JsonPatchDocument-UpdateDigitalTwinOptions
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteDigitalTwin(String)}
     */
    @Override
    public void deleteDigitalTwin() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteDigitalTwin#String
        digitalTwinsAsyncClient.deleteDigitalTwin("myDigitalTwinId")
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteDigitalTwin#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#deleteDigitalTwinWithResponse(String, DeleteDigitalTwinOptions)}
     */
    @Override
    public void deleteDigitalTwinWithResponse() {
        DigitalTwinsAsyncClient digitalTwinsAsyncClient = createDigitalTwinsAsyncClient();

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteDigitalTwinWithResponse#String-DeleteDigitalTwinOptions
        digitalTwinsAsyncClient.deleteDigitalTwinWithResponse(
            "myDigitalTwinId",
            new DeleteDigitalTwinOptions())
            .subscribe(deleteResponse ->
                System.out.println("Deleted digital twin. HTTP response status code: " + deleteResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteDigitalTwinWithResponse#String-DeleteDigitalTwinOptions
    }

    //endregion DigitalTwinSnippets

    //region RelationshipSnippets

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#createOrReplaceRelationship(String, String, Object, Class)}
     */
    @Override
    public void createRelationship() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationship#String-String-Object-Class#BasicRelationship
        BasicRelationship buildingToFloorBasicRelationship = new BasicRelationship(
                "myRelationshipId",
                "mySourceDigitalTwinId",
                "myTargetDigitalTwinId",
                "contains")
            .addProperty("Prop1", "Prop1 value")
            .addProperty("Prop2", 6);

        digitalTwinsAsyncClient.createOrReplaceRelationship(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            buildingToFloorBasicRelationship,
            BasicRelationship.class)
            .subscribe(createdRelationship -> System.out.println(
                "Created relationship with Id: "
                + createdRelationship.getId()
                + " from: " + createdRelationship.getSourceId()
                + " to: " + createdRelationship.getTargetId()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationship#String-String-Object-Class#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationship#String-String-Object-Class#String
        String relationshipPayload = getRelationshipPayload();

        digitalTwinsAsyncClient.createOrReplaceRelationship(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            relationshipPayload,
            String.class)
            .subscribe(createRelationshipString ->
                System.out.println("Created relationship: " + createRelationshipString));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationship#String-String-Object-Class#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#createOrReplaceRelationshipWithResponse(String, String, Object, Class, CreateOrReplaceRelationshipOptions)}
     */
    @Override
    public void createRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse#String-String-Object-Class-Options#BasicRelationship
        BasicRelationship buildingToFloorBasicRelationship = new BasicRelationship(
                "myRelationshipId",
                "mySourceDigitalTwinId",
                "myTargetDigitalTwinId",
                "contains")
            .addProperty("Prop1", "Prop1 value")
            .addProperty("Prop2", 6);

        digitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            buildingToFloorBasicRelationship,
            BasicRelationship.class,
            new CreateOrReplaceRelationshipOptions())
            .subscribe(createdRelationshipWithResponse -> System.out.println(
                "Created relationship with Id: "
                + createdRelationshipWithResponse.getValue().getId()
                + " from: " + createdRelationshipWithResponse.getValue().getSourceId()
                + " to: " + createdRelationshipWithResponse.getValue().getTargetId()
                + " Http status code: "
                + createdRelationshipWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse#String-String-Object-Class-Options#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse#String-String-Object-Class-Options#String
        String relationshipPayload = getRelationshipPayload();

        digitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse(
            "mySourceDigitalTwinId",
            "myRelationshipId",
            relationshipPayload,
            String.class,
            new CreateOrReplaceRelationshipOptions())
            .subscribe(createdRelationshipStringWithResponse -> System.out.println(
                "Created relationship: "
                + createdRelationshipStringWithResponse
                + " With HTTP status code: "
                + createdRelationshipStringWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse#String-String-Object-Class-Options#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getRelationship(String, String, Class)}
     */
    @Override
    public void getRelationship() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationship#String#BasicRelationship
        digitalTwinsAsyncClient.getRelationship(
            "myDigitalTwinId",
            "myRelationshipName",
            BasicRelationship.class)
            .subscribe(retrievedRelationship -> System.out.println(
                "Retrieved relationship with Id: "
                + retrievedRelationship.getId()
                + " from: "
                + retrievedRelationship.getSourceId()
                + " to: " + retrievedRelationship.getTargetId()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationship#String#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationship#String#String
        digitalTwinsAsyncClient.getRelationship(
            "myDigitalTwinId",
            "myRelationshipName",
            String.class)
            .subscribe(retrievedRelationshipString ->
                System.out.println("Retrieved relationship: " + retrievedRelationshipString));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationship#String#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#getRelationshipWithResponse(String, String, Class)}
     */
    @Override
    public void getRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationshipWithResponse#String-String-Class-Options#BasicRelationship
        digitalTwinsAsyncClient.getRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipName",
            BasicRelationship.class)
            .subscribe(retrievedRelationshipWithResponse -> System.out.println(
                "Retrieved relationship with Id: "
                    + retrievedRelationshipWithResponse.getValue().getId()
                    + " from: "
                    + retrievedRelationshipWithResponse.getValue().getSourceId()
                    + " to: " + retrievedRelationshipWithResponse.getValue().getTargetId()
                    + "HTTP status code: " + retrievedRelationshipWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationshipWithResponse#String-String-Class-Options#BasicRelationship

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationshipWithResponse#String-String-Class-Options#String
        digitalTwinsAsyncClient.getRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipName",
            String.class)
            .subscribe(retrievedRelationshipStringWithResponse -> System.out.println(
                "Retrieved relationship: "
                + retrievedRelationshipStringWithResponse
                + " HTTP status code: "
                + retrievedRelationshipStringWithResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationshipWithResponse#String-String-Class-Options#String
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#updateRelationship(String, String, JsonPatchDocument)}
     */
    @Override
    public void updateRelationship() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateRelationship#String-String-JsonPatchDocument
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        jsonPatchDocument.appendReplace("/relationshipProperty1", "new property value");

        digitalTwinsAsyncClient.updateRelationship(
            "myDigitalTwinId",
            "myRelationshipId",
            jsonPatchDocument)
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateRelationship#String-String-JsonPatchDocument
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#updateRelationshipWithResponse(String, String, JsonPatchDocument, UpdateRelationshipOptions)}
     */
    @Override
    public void updateRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateRelationshipWithResponse#String-String-JsonPatchDocument-UpdateRelationshipOptions
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        jsonPatchDocument.appendReplace("/relationshipProperty1", "new property value");

        digitalTwinsAsyncClient.updateRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipId",
            jsonPatchDocument,
            new UpdateRelationshipOptions())
            .subscribe(updateResponse ->
                System.out.println(
                    "Relationship updated with status code: "
                    + updateResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateRelationshipWithResponse#String-String-JsonPatchDocument-UpdateRelationshipOptions
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteRelationship(String, String)}
     */
    @Override
    public void deleteRelationship() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteRelationship#String-String
        digitalTwinsAsyncClient.deleteRelationship("myDigitalTwinId", "myRelationshipId")
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteRelationship#String-String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#deleteRelationshipWithResponse(String, String, DeleteRelationshipOptions)}
     */
    @Override
    public void deleteRelationshipWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteRelationshipWithResponse#String-String-DeleteRelationshipOptions
        digitalTwinsAsyncClient.deleteRelationshipWithResponse(
            "myDigitalTwinId",
            "myRelationshipId",
            new DeleteRelationshipOptions())
            .subscribe(deleteResponse ->
                System.out.println(
                    "Deleted relationship with HTTP status code: "
                    + deleteResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteRelationshipWithResponse#String-String-DeleteRelationshipOptions
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#listRelationships(String, Class)}
     * and {@link DigitalTwinsAsyncClient#listRelationships(String, String, Class)}
     */
    @Override
    public void listRelationships() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-Class-Options#BasicRelationship#IterateByItem
        digitalTwinsAsyncClient.listRelationships("myDigitalTwinId", BasicRelationship.class)
            .doOnNext(basicRel -> System.out.println("Retrieved relationship with Id: " + basicRel.getId()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-Class-Options#BasicRelationship#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-Class-Options#String#IterateByItem
        digitalTwinsAsyncClient.listRelationships("myDigitalTwinId", String.class)
            .doOnNext(rel -> System.out.println("Retrieved relationship: " + rel));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-Class-Options#String#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-String-Class-Options#BasicRelationship#IterateByItem
        digitalTwinsAsyncClient.listRelationships(
            "myDigitalTwinId",
            "myRelationshipName",
            BasicRelationship.class)
            .doOnNext(rel -> System.out.println("Retrieved relationship with Id: " + rel.getId()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-String-Class-Options#BasicRelationship#IterateByItem

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-String-Class-Options#String#IterateByItem
        digitalTwinsAsyncClient.listRelationships(
            "myDigitalTwinId",
            "myRelationshipId",
            String.class)
            .doOnNext(rel -> System.out.println("Retrieved relationship: " + rel));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-String-Class-Options#String#IterateByItem
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#listIncomingRelationships(String)}
     */
    @Override
    public void listIncomingRelationships() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listIncomingRelationships#String
        digitalTwinsAsyncClient.listIncomingRelationships("myDigitalTwinId")
            .doOnNext(incomingRel -> System.out.println(
                "Retrieved relationship with Id: "
                    + incomingRel.getRelationshipId()
                    + " from: " + incomingRel.getSourceId()
                    + " to: myDigitalTwinId"))
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listIncomingRelationships#String

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listIncomingRelationships#String-Options
        digitalTwinsAsyncClient.listIncomingRelationships(
            "myDigitalTwinId")
            .doOnNext(incomingRel -> System.out.println(
                "Retrieved relationship with Id: "
                + incomingRel.getRelationshipId()
                + " from: " + incomingRel.getSourceId()
                + " to: myDigitalTwinId"))
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listIncomingRelationships#String-Options
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

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createModels#Iterable
        digitalTwinsAsyncClient.createModels(Arrays.asList(model1, model2, model3))
            .subscribe(createdModels -> createdModels.forEach(model ->
                System.out.println("Retrieved model with Id: " + model.getModelId())));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createModels#Iterable
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#createModelsWithResponse(Iterable)}
     */
    @Override
    public void createModelsWithResponse() {
        String model1 = loadModelFromFile("model1");
        String model2 = loadModelFromFile("model2");
        String model3 = loadModelFromFile("model3");

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createModelsWithResponse#Iterable-Options
        digitalTwinsAsyncClient.createModelsWithResponse(
            Arrays.asList(model1, model2, model3))
            .subscribe(createdModels -> {
                System.out.println("Received a response with HTTP status code: " + createdModels.getStatusCode());
                createdModels.getValue().forEach(
                    model -> System.out.println("Retrieved model with Id: " + model.getModelId()));
            });
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createModelsWithResponse#Iterable-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getModel(String)}
     */
    @Override
    public void getModel() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getModel#String
        digitalTwinsAsyncClient.getModel("dtmi:com:samples:Building;1")
            .subscribe(model -> System.out.println("Retrieved model with Id: " + model.getModelId()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getModel#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#getModelWithResponse(String)}
     */
    @Override
    public void getModelWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getModelWithResponse#String-Options
        digitalTwinsAsyncClient.getModelWithResponse(
            "dtmi:com:samples:Building;1")
            .subscribe(modelWithResponse -> {
                System.out.println("Received HTTP response with status code: " + modelWithResponse.getStatusCode());
                System.out.println("Retrieved model with Id: " + modelWithResponse.getValue().getModelId());
            });
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getModelWithResponse#String-Options
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#listModels()} and
     * {@link DigitalTwinsAsyncClient#listModels(ListModelsOptions)} )}
     */
    @Override
    public void listModels() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listModels
        digitalTwinsAsyncClient.listModels()
            .doOnNext(model -> System.out.println("Retrieved model with Id: " + model.getModelId()))
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listModels

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listModels#ListModelsOptions
        digitalTwinsAsyncClient.listModels(
            new ListModelsOptions()
                .setMaxItemsPerPage(5)
                .setIncludeModelDefinition(true))
            .doOnNext(model -> System.out.println("Retrieved model with Id: " + model.getModelId()))
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listModels#ListModelsOptions
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#decommissionModel(String)}
     */
    @Override
    public void decommissionModel() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.decommissionModel#String
        digitalTwinsAsyncClient.decommissionModel("dtmi:com:samples:Building;1")
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.decommissionModel#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#decommissionModelWithResponse(String)}
     */
    @Override
    public void decommissionModelWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.decommissionModelWithResponse#String-Options
        digitalTwinsAsyncClient.decommissionModelWithResponse(
            "dtmi:com:samples:Building;1")
            .subscribe(response ->
                System.out.println(
                    "Received decommission model HTTP response with status:"
                    + response.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.decommissionModelWithResponse#String-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteModel(String)}
     */
    @Override
    public void deleteModel() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteModel#String
        digitalTwinsAsyncClient.deleteModel("dtmi:com:samples:Building;1")
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteModel#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#deleteModelWithResponse(String)}
     */
    @Override
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteModelWithResponse#String-Options
        digitalTwinsAsyncClient.deleteModelWithResponse(
            "dtmi:com:samples:Building;1")
            .subscribe(response ->
                System.out.println(
                    "Received delete model operation response with HTTP status code:"
                    + response.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteModelWithResponse#String-Options
    }

    //endregion ModelsSnippets

    //region ComponentSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getComponent(String, String, Class)}
     */
    @Override
    public void getComponent() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getComponent#String-String-Class
        digitalTwinsAsyncClient.getComponent(
            "myDigitalTwinId",
            "myComponentName",
            String.class)
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getComponent#String-String-Class
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#getComponentWithResponse(String, String, Class)}
     */
    @Override
    public void getComponentWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getComponentWithResponse#String-String-Class-Options
        digitalTwinsAsyncClient.getComponentWithResponse(
            "myDigitalTwinId",
            "myComponentName",
            String.class)
            .subscribe(response ->
                System.out.println(
                    "Received component get operation response with HTTP status code: "
                    + response.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getComponentWithResponse#String-String-Class-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#updateComponent(String, String, JsonPatchDocument)}
     */
    @Override
    public void updateComponent() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateComponent#String-String-JsonPatchDocument
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        jsonPatchDocument.appendReplace("/ComponentProp1", "Some new value");

        digitalTwinsAsyncClient.updateComponent(
            "myDigitalTwinId",
            "myComponentName",
            jsonPatchDocument)
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateComponent#String-String-JsonPatchDocument
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#updateComponentWithResponse(String, String, JsonPatchDocument, UpdateComponentOptions)}
     */
    @Override
    public void updateComponentWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateComponentWithResponse#String-String-JsonPatchDocument-UpdateComponentOptions
        JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
        jsonPatchDocument.appendReplace("/ComponentProp1", "Some new value");

        digitalTwinsAsyncClient.updateComponentWithResponse(
            "myDigitalTwinId",
            "myComponentName",
            jsonPatchDocument,
            new UpdateComponentOptions().setIfMatch("*"))
            .subscribe(updateResponse ->
                System.out.println(
                    "Received update operation response with HTTP status code: "
                    + updateResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateComponentWithResponse#String-String-JsonPatchDocument-UpdateComponentOptions
    }

    //endregion ComponentSnippets

    //region QuerySnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#query(String, Class)} and
     * {@link DigitalTwinsAsyncClient#query(String, Class, QueryOptions)}
     */
    @Override
    public void query() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String#BasicDigitalTwin
        digitalTwinsAsyncClient.query(
            "SELECT * FROM digitaltwins",
            BasicDigitalTwin.class)
            .doOnNext(
                basicTwin -> System.out.println("Retrieved digitalTwin query result with Id: " + basicTwin.getId()))
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String#String
        digitalTwinsAsyncClient.query(
            "SELECT * FROM digitaltwins",
            String.class)
            .doOnNext(twinString -> System.out.println("Retrieved digitalTwin query result with Id: " + twinString))
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String#String

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String-Options#BasicDigitalTwin
        digitalTwinsAsyncClient.query(
            "SELECT * FROM digitaltwins",
            BasicDigitalTwin.class,
            new QueryOptions().setMaxItemsPerPage(5))
            .doOnNext(
                basicTwin -> System.out.println("Retrieved digitalTwin query result with Id: " + basicTwin.getId()))
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String-Options#BasicDigitalTwin

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String-Options#String
        digitalTwinsAsyncClient.query(
            "SELECT * FROM digitaltwins",
            String.class,
            new QueryOptions().setMaxItemsPerPage(5))
            .doOnNext(twinString -> System.out.println("Retrieved digitalTwin query result with Id: " + twinString))
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String-Options#String
    }

    //endregion QuerySnippets

    //region EventRouteSnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#createOrReplaceEventRoute(String, DigitalTwinsEventRoute)}
     */
    @Override
    public void createEventRoute() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceEventRoute#String-DigitalTwinsEventRoute
        String filter =
            "$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'";

        DigitalTwinsEventRoute eventRoute = new DigitalTwinsEventRoute("myEndpointName").setFilter(filter);
        digitalTwinsAsyncClient.createOrReplaceEventRoute("myEventRouteId", eventRoute).subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceEventRoute#String-DigitalTwinsEventRoute
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#createOrReplaceEventRouteWithResponse(String, DigitalTwinsEventRoute)}
     */
    @Override
    public void createEventRouteWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceEventRouteWithResponse#String-DigitalTwinsEventRoute
        String filter =
            "$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'";

        DigitalTwinsEventRoute eventRoute = new DigitalTwinsEventRoute("myEndpointName").setFilter(filter);
        digitalTwinsAsyncClient.createOrReplaceEventRouteWithResponse(
            "myEventRouteId",
            eventRoute)
            .subscribe(response ->
                System.out.println("Created an event rout with HTTP status code: " + response.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceEventRouteWithResponse#String-DigitalTwinsEventRoute
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#getEventRoute(String)}
     */
    @Override
    public void getEventRoute() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getEventRoute#String
        digitalTwinsAsyncClient.getEventRoute("myEventRouteId")
            .subscribe(eventRoute -> System.out.println("Retrieved event route with Id: " + eventRoute.getEventRouteId()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getEventRoute#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#getEventRouteWithResponse(String)}
     */
    @Override
    public void getEventRouteWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getEventRouteWithResponse#String-Options
        digitalTwinsAsyncClient.getEventRouteWithResponse(
            "myEventRouteId")
            .subscribe(eventRouteWithResponse -> {
                System.out.println(
                    "Received get event route operation response with HTTP status code: "
                    + eventRouteWithResponse.getStatusCode());
                System.out.println(
                    "Retrieved event route with Id: "
                    + eventRouteWithResponse.getValue().getEventRouteId());
            });
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getEventRouteWithResponse#String-Options
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#deleteEventRoute(String)}
     */
    @Override
    public void deleteEventRoute() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteEventRoute#String
        digitalTwinsAsyncClient.deleteEventRoute("myEventRouteId")
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteEventRoute#String
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#deleteEventRouteWithResponse(String)}
     */
    @Override
    public void deleteEventRouteWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteEventRouteWithResponse#String-Options
        digitalTwinsAsyncClient.deleteEventRouteWithResponse(
            "myEventRouteId")
            .subscribe(deleteResponse ->
                System.out.println(
                    "Received delete event route operation response with HTTP status code: "
                    + deleteResponse.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteEventRouteWithResponse#String-Options
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#listEventRoutes()} and
     * {@link DigitalTwinsAsyncClient#listEventRoutes(ListDigitalTwinsEventRoutesOptions)}
     */
    @Override
    public void listEventRoutes() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listEventRoutes
        digitalTwinsAsyncClient.listEventRoutes()
            .doOnNext(eventRoute -> System.out.println("Retrieved event route with Id: " + eventRoute.getEventRouteId()))
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listEventRoutes

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listEventRoutes#ListDigitalTwinsEventRoutesOptions
        digitalTwinsAsyncClient.listEventRoutes(new ListDigitalTwinsEventRoutesOptions().setMaxItemsPerPage(5))
            .doOnNext(eventRoute -> System.out.println("Retrieved event route with Id: " + eventRoute.getEventRouteId()))
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listEventRoutes#ListDigitalTwinsEventRoutesOptions
    }

    //endregion EventRouteSnippets

    //region TelemetrySnippets

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#publishTelemetry(String, String, Object)}
     */
    @Override
    public void publishTelemetry() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetry#String-String-Object#String
        digitalTwinsAsyncClient.publishTelemetry(
            "myDigitalTwinId",
            UUID.randomUUID().toString(),
            "{\"Telemetry1\": 5}")
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetry#String-String-Object#String

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetry#String-String-Object#Object
        Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
        telemetryPayload.put("Telemetry1", 5);

        digitalTwinsAsyncClient.publishTelemetry(
            "myDigitalTwinId",
            UUID.randomUUID().toString(),
            telemetryPayload)
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetry#String-String-Object#Object
    }

    /**
     * Generates code samples for using {@link DigitalTwinsAsyncClient#publishTelemetryWithResponse(String, String, Object, PublishTelemetryOptions)}
     */
    @Override
    public void publishTelemetryWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetryWithResponse#String-String-Object-Options#String
        digitalTwinsAsyncClient.publishTelemetryWithResponse(
            "myDigitalTwinId",
            UUID.randomUUID().toString(),
            "{\"Telemetry1\": 5}",
            new PublishTelemetryOptions().setTimestamp(OffsetDateTime.now(ZoneId.systemDefault())))
            .subscribe(responseString ->
                System.out.println(
                    "Received publish telemetry operation response with HTTP status code: "
                    + responseString.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetryWithResponse#String-String-Object-Options#String

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetryWithResponse#String-String-Object-Options#Object
        Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
        telemetryPayload.put("Telemetry1", 5);

        digitalTwinsAsyncClient.publishTelemetryWithResponse(
            "myDigitalTwinId",
            UUID.randomUUID().toString(),
            telemetryPayload,
            new PublishTelemetryOptions().setTimestamp(OffsetDateTime.now(ZoneId.systemDefault())))
            .subscribe(responseObject ->
                System.out.println(
                    "Received publish telemetry operation response with HTTP status code: "
                    + responseObject.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetryWithResponse#String-String-Object-Options#Object
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#publishComponentTelemetry(String, String, String, Object)}
     */
    @Override
    public void publishComponentTelemetry() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetry#String-String-String-Object#String
        digitalTwinsAsyncClient.publishComponentTelemetry(
            "myDigitalTwinId",
            "myComponentName",
            UUID.randomUUID().toString(),
            "{\"Telemetry1\": 5}")
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetry#String-String-String-Object#String

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetry#String-String-String-Object#Object
        Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
        telemetryPayload.put("Telemetry1", 5);

        digitalTwinsAsyncClient.publishComponentTelemetry(
            "myDigitalTwinId",
            "myComponentName",
            UUID.randomUUID().toString(),
            telemetryPayload)
            .subscribe();
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetry#String-String-String-Object#Object
    }

    /**
     * Generates code samples for using
     * {@link DigitalTwinsAsyncClient#publishComponentTelemetryWithResponse(String, String, String, Object, PublishComponentTelemetryOptions)}
     */
    @Override
    public void publishComponentTelemetryWithResponse() {
        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#String
        digitalTwinsAsyncClient.publishComponentTelemetryWithResponse(
            "myDigitalTwinId",
            "myComponentName",
            UUID.randomUUID().toString(),
            "{\"Telemetry1\": 5}",
            new PublishComponentTelemetryOptions().setTimestamp(OffsetDateTime.now(ZoneId.systemDefault())))
            .subscribe(responseString ->
                System.out.println(
                    "Received publish component telemetry operation response with HTTP status code: "
                    + responseString.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#String

        // BEGIN: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#Object
        Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
        telemetryPayload.put("Telemetry1", 5);

        digitalTwinsAsyncClient.publishComponentTelemetryWithResponse(
            "myDigitalTwinId",
            "myComponentName",
            UUID.randomUUID().toString(),
            telemetryPayload,
            new PublishComponentTelemetryOptions().setTimestamp(OffsetDateTime.now(ZoneId.systemDefault())))
            .subscribe(responseObject ->
                System.out.println(
                    "Received publish component telemetry operation response with HTTP status code: "
                    + responseObject.getStatusCode()));
        // END: com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#Object
    }

    //endregion TelemetrySnippets
}
