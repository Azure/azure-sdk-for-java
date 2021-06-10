// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.codesnippets;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableAccessPolicies;
import com.azure.data.tables.models.TableAccessPolicy;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableEntityUpdateMode;
import com.azure.data.tables.models.TableItem;
import com.azure.data.tables.models.TableSignedIdentifier;
import com.azure.data.tables.models.TableTransactionAction;
import com.azure.data.tables.models.TableTransactionActionType;
import com.azure.data.tables.models.TableTransactionFailedException;
import com.azure.data.tables.models.TableTransactionResult;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets for {@link TableClient}.
 */
public class TableClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link TableClient}.
     *
     * @return An instance of {@link TableClient}.
     */
    public TableClient createClient() {
        // BEGIN: com.azure.data.tables.tableClient.instantiation
        TableClient tableClient = new TableClientBuilder()
            .endpoint("https://myaccount.core.windows.net/")
            .credential(new AzureNamedKeyCredential("name", "key"))
            .tableName("myTable")
            .buildClient();
        // END: com.azure.data.tables.tableClient.instantiation

        return tableClient;
    }

    /**
     * Generates code samples for using {@link TableClient#createTable()} and
     * {@link TableClient#createTableWithResponse(Duration, Context)}.
     */
    public void createTable() {
        TableClient tableClient = createClient();

        // BEGIN: com.azure.data.tables.tableClient.createTable
        TableItem tableItem = tableClient.createTable();

        System.out.printf("Table with name '%s' was created.", tableItem.getName());
        // END: com.azure.data.tables.tableClient.createTable

        // BEGIN: com.azure.data.tables.tableClient.createTableWithResponse#Duration-Context
        Response<TableItem> response = tableClient.createTableWithResponse(Duration.ofSeconds(5),
            new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Table with name '%s' was created.",
            response.getStatusCode(), response.getValue().getName());
        // END: com.azure.data.tables.tableClient.createTableWithResponse#Duration-Context
    }

    /**
     * Generates code samples for using {@link TableClient#deleteTable()} and
     * {@link TableClient#deleteTableWithResponse(Duration, Context)}.
     */
    public void deleteTable() {
        TableClient tableClient = createClient();

        // BEGIN: com.azure.data.tables.tableClient.deleteTable
        tableClient.deleteTable();

        System.out.print("Table was deleted.");
        // END: com.azure.data.tables.tableClient.deleteTable

        // BEGIN: com.azure.data.tables.tableClient.deleteTableWithResponse#Duration-Context
        Response<Void> response = tableClient.deleteTableWithResponse(Duration.ofSeconds(5),
            new Context("key1", "value1"));

        System.out.printf("Table was deleted successfully with status code: %d.", response.getStatusCode());
        // END: com.azure.data.tables.tableClient.deleteTableWithResponse#Duration-Context
    }

    /**
     * Generates code samples for using {@link TableClient#createEntity(TableEntity)} and
     * {@link TableClient#createEntityWithResponse(TableEntity, Duration, Context)}.
     */
    public void createEntity() {
        TableClient tableClient = createClient();

        // BEGIN: com.azure.data.tables.tableClient.createEntity#TableEntity
        String partitionKey = "partitionKey";
        String rowKey = "rowKey";

        TableEntity tableEntity = new TableEntity(partitionKey, rowKey)
            .addProperty("Property", "Value");

        tableClient.createEntity(tableEntity);

        System.out.printf("Table entity with partition key '%s' and row key: '%s' was created.", partitionKey, rowKey);
        // END: com.azure.data.tables.tableClient.createEntity#TableEntity

        // BEGIN: com.azure.data.tables.tableClient.createEntityWithResponse#TableEntity-Duration-Context
        String myPartitionKey = "partitionKey";
        String myRowKey = "rowKey";

        TableEntity myTableEntity = new TableEntity(myPartitionKey, myRowKey)
            .addProperty("Property", "Value");

        Response<Void> response = tableClient.createEntityWithResponse(myTableEntity, Duration.ofSeconds(5),
            new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Table entity with partition key '%s' and row key"
            + " '%s' was created.", response.getStatusCode(), myPartitionKey, myRowKey);
        // END: com.azure.data.tables.tableClient.createEntityWithResponse#TableEntity-Duration-Context
    }

    /**
     * Generates code samples for using {@link TableClient#upsertEntity(TableEntity)} and
     * {@link TableClient#upsertEntityWithResponse(TableEntity, TableEntityUpdateMode, Duration, Context)}.
     */
    public void upsertEntity() {
        TableClient tableClient = createClient();

        // BEGIN: com.azure.data.tables.tableClient.upsertEntity#TableEntity
        String partitionKey = "partitionKey";
        String rowKey = "rowKey";

        TableEntity tableEntity = new TableEntity(partitionKey, rowKey)
            .addProperty("Property", "Value");

        tableClient.upsertEntity(tableEntity);

        System.out.printf("Table entity with partition key '%s' and row key: '%s' was updated/created.", partitionKey,
            rowKey);
        // END: com.azure.data.tables.tableClient.upsertEntity#TableEntity

        // BEGIN: com.azure.data.tables.tableClient.upsertEntityWithResponse#TableEntity-TableEntityUpdateMode-Duration-Context
        String myPartitionKey = "partitionKey";
        String myRowKey = "rowKey";

        TableEntity myTableEntity = new TableEntity(myPartitionKey, myRowKey)
            .addProperty("Property", "Value");

        Response<Void> response = tableClient.upsertEntityWithResponse(myTableEntity, TableEntityUpdateMode.REPLACE,
            Duration.ofSeconds(5), new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Table entity with partition key '%s' and row key"
            + " '%s' was updated/created.", response.getStatusCode(), partitionKey, rowKey);
        // END: com.azure.data.tables.tableClient.upsertEntityWithResponse#TableEntity-TableEntityUpdateMode-Duration-Context
    }

    /**
     * Generates code samples for using {@link TableClient#updateEntity(TableEntity)},
     * {@link TableClient#updateEntity(TableEntity, TableEntityUpdateMode)} and
     * {@link TableClient#updateEntityWithResponse(TableEntity, TableEntityUpdateMode, boolean, Duration, Context)}.
     */
    public void updateEntity() {
        TableClient tableClient = createClient();

        // BEGIN: com.azure.data.tables.tableClient.updateEntity#TableEntity
        String partitionKey = "partitionKey";
        String rowKey = "rowKey";

        TableEntity tableEntity = new TableEntity(partitionKey, rowKey)
            .addProperty("Property", "Value");

        tableClient.updateEntity(tableEntity);

        System.out.printf("Table entity with partition key '%s' and row key: '%s' was updated/created.", partitionKey,
            rowKey);
        // END: com.azure.data.tables.tableClient.updateEntity#TableEntity

        // BEGIN: com.azure.data.tables.tableClient.updateEntity#TableEntity-TableEntityUpdateMode
        String myPartitionKey = "partitionKey";
        String myRowKey = "rowKey";

        TableEntity myTableEntity = new TableEntity(myPartitionKey, myRowKey)
            .addProperty("Property", "Value");

        tableClient.updateEntity(myTableEntity, TableEntityUpdateMode.REPLACE);

        System.out.printf("Table entity with partition key '%s' and row key: '%s' was updated/created.", partitionKey,
            rowKey);
        // END: com.azure.data.tables.tableClient.updateEntity#TableEntity-TableEntityUpdateMode

        // BEGIN: com.azure.data.tables.tableClient.updateEntityWithResponse#TableEntity-TableEntityUpdateMode-boolean-Duration-Context
        String somePartitionKey = "partitionKey";
        String someRowKey = "rowKey";

        TableEntity someTableEntity = new TableEntity(somePartitionKey, someRowKey)
            .addProperty("Property", "Value");

        Response<Void> response = tableClient.updateEntityWithResponse(someTableEntity, TableEntityUpdateMode.REPLACE,
            true, Duration.ofSeconds(5), new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Table entity with partition key '%s' and row key"
            + " '%s' was updated.", response.getStatusCode(), partitionKey, rowKey);
        // END: com.azure.data.tables.tableClient.updateEntityWithResponse#TableEntity-TableEntityUpdateMode-boolean-Duration-Context
    }

    /**
     * Generates code samples for using {@link TableClient#deleteEntity(String, String)},
     * {@link TableClient#deleteEntity(TableEntity)} and
     * {@link TableClient#deleteEntityWithResponse(TableEntity, boolean, Duration, Context)} .
     */
    public void deleteEntity() {
        TableClient tableClient = createClient();

        // BEGIN: com.azure.data.tables.tableClient.deleteEntity#String-String
        String partitionKey = "partitionKey";
        String rowKey = "rowKey";

        tableClient.deleteEntity(partitionKey, rowKey);

        System.out.printf("Table entity with partition key '%s' and row key: '%s' was deleted.", partitionKey, rowKey);
        // END: com.azure.data.tables.tableClient.deleteEntity#String-String

        // BEGIN: com.azure.data.tables.tableClient.deleteEntity#TableEntity
        String myPartitionKey = "partitionKey";
        String myRowKey = "rowKey";

        TableEntity myTableEntity = new TableEntity(myPartitionKey, myRowKey)
            .addProperty("Property", "Value");

        tableClient.deleteEntity(myTableEntity);

        System.out.printf("Table entity with partition key '%s' and row key: '%s' was created.", partitionKey, rowKey);
        // END: com.azure.data.tables.tableClient.deleteEntity#TableEntity

        // BEGIN: com.azure.data.tables.tableClient.deleteEntityWithResponse#TableEntity-Duration-Context
        String somePartitionKey = "partitionKey";
        String someRowKey = "rowKey";

        TableEntity someTableEntity = new TableEntity(somePartitionKey, someRowKey)
            .addProperty("Property", "Value");

        Response<Void> response = tableClient.deleteEntityWithResponse(someTableEntity, true, Duration.ofSeconds(5),
            new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Table entity with partition key '%s' and row key"
            + " '%s' was deleted.", response.getStatusCode(), somePartitionKey, someRowKey);
        // END: com.azure.data.tables.tableClient.deleteEntityWithResponse#TableEntity-Duration-Context
    }

    /**
     * Generates code samples for using {@link TableClient#listEntities()} and
     * {@link TableClient#listEntities(ListEntitiesOptions, Duration, Context)}.
     */
    public void listEntities() {
        TableClient tableClient = createClient();

        // BEGIN: com.azure.data.tables.tableClient.listEntities
        PagedIterable<TableEntity> tableEntities = tableClient.listEntities();

        tableEntities.forEach(tableEntity ->
            System.out.printf("Retrieved entity with partition key '%s' and row key '%s'.%n",
                tableEntity.getPartitionKey(), tableEntity.getRowKey()));
        // END: com.azure.data.tables.tableClient.listEntities

        // BEGIN: com.azure.data.tables.tableClient.listEntities#ListEntitiesOptions-Duration-Context
        List<String> propertiesToSelect = new ArrayList<>();
        propertiesToSelect.add("name");
        propertiesToSelect.add("lastname");
        propertiesToSelect.add("age");

        ListEntitiesOptions listEntitiesOptions = new ListEntitiesOptions()
            .setTop(15)
            .setFilter("PartitionKey eq 'MyPartitionKey' and RowKey eq 'MyRowKey'")
            .setSelect(propertiesToSelect);

        PagedIterable<TableEntity> myTableEntities = tableClient.listEntities(listEntitiesOptions,
            Duration.ofSeconds(5), new Context("key1", "value1"));

        myTableEntities.forEach(tableEntity -> {
            System.out.printf("Retrieved entity with partition key '%s', row key '%s' and properties:%n",
                tableEntity.getPartitionKey(), tableEntity.getRowKey());

            tableEntity.getProperties().forEach((key, value) ->
                System.out.printf("Name: '%s'. Value: '%s'.%n", key, value));
        });
        // END: com.azure.data.tables.tableClient.listEntities#ListEntitiesOptions-Duration-Context
    }

    /**
     * Generates code samples for using {@link TableClient#getEntity(String, String)}  and
     * {@link TableClient#getEntityWithResponse(String, String, List, Duration, Context)}.
     */
    public void getEntity() {
        TableClient tableClient = createClient();

        // BEGIN: com.azure.data.tables.tableClient.getEntity#String-String
        String partitionKey = "partitionKey";
        String rowKey = "rowKey";

        TableEntity tableEntity = tableClient.getEntity(partitionKey, rowKey);

        System.out.printf("Retrieved entity with partition key '%s' and row key '%s'.", tableEntity.getPartitionKey(),
            tableEntity.getRowKey());
        // END: com.azure.data.tables.tableClient.getEntity#String-String

        // BEGIN: com.azure.data.tables.tableClient.getEntityWithResponse#String-String-ListEntitiesOptions-Duration-Context
        String myPartitionKey = "partitionKey";
        String myRowKey = "rowKey";

        List<String> propertiesToSelect = new ArrayList<>();
        propertiesToSelect.add("name");
        propertiesToSelect.add("lastname");
        propertiesToSelect.add("age");

        Response<TableEntity> response = tableClient.getEntityWithResponse(myPartitionKey, myRowKey, propertiesToSelect,
            Duration.ofSeconds(5), new Context("key1", "value1"));

        TableEntity myTableEntity = response.getValue();

        System.out.printf("Response successful with status code: %d. Retrieved entity with partition key '%s', row key"
                + " '%s' and properties:", response.getStatusCode(), myTableEntity.getPartitionKey(),
            myTableEntity.getRowKey());

        myTableEntity.getProperties().forEach((key, value) ->
            System.out.printf("%nName: '%s'. Value: '%s'.", key, value));
        // END: com.azure.data.tables.tableClient.getEntityWithResponse#String-String-ListEntitiesOptions-Duration-Context
    }

    /**
     * Generates code samples for using {@link TableClient#getAccessPolicies()}  and
     * {@link TableClient#getAccessPoliciesWithResponse(Duration, Context)}.
     */
    public void getAccessPolicies() {
        TableClient tableClient = createClient();

        // BEGIN: com.azure.data.tables.tableClient.getAccessPolicies
        TableAccessPolicies accessPolicies = tableClient.getAccessPolicies();

        accessPolicies.getIdentifiers().forEach(signedIdentifier ->
            System.out.printf("Retrieved table access policy with id '%s'.", signedIdentifier.getId()));
        // END: com.azure.data.tables.tableClient.getAccessPolicies

        // BEGIN: com.azure.data.tables.tableClient.getAccessPoliciesWithResponse#Duration-Context
        List<String> propertiesToSelect = new ArrayList<>();
        propertiesToSelect.add("name");
        propertiesToSelect.add("lastname");
        propertiesToSelect.add("age");

        Response<TableAccessPolicies> response = tableClient.getAccessPoliciesWithResponse(Duration.ofSeconds(5),
            new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Retrieved table access policies with the following"
            + " IDs:", response.getStatusCode());

        response.getValue().getIdentifiers().forEach(signedIdentifier ->
            System.out.printf("%n%s", signedIdentifier.getId()));
        // END: com.azure.data.tables.tableClient.getAccessPoliciesWithResponse#Duration-Context
    }

    /**
     * Generates code samples for using {@link TableClient#setAccessPolicies(List)}   and
     * {@link TableClient#setAccessPoliciesWithResponse(List, Duration, Context)}.
     */
    public void setAccessPolicies() {
        TableClient tableClient = createClient();

        // BEGIN: com.azure.data.tables.tableClient.setAccessPolicies#List
        List<TableSignedIdentifier> signedIdentifiers = new ArrayList<>();

        signedIdentifiers.add(new TableSignedIdentifier("id1")
            .setAccessPolicy(new TableAccessPolicy()
                .setStartsOn(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setExpiresOn(OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setPermissions("r")));
        signedIdentifiers.add(new TableSignedIdentifier("id2")
            .setAccessPolicy(new TableAccessPolicy()
                .setStartsOn(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setExpiresOn(OffsetDateTime.of(2021, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC))
                .setPermissions("raud")));

        tableClient.setAccessPolicies(signedIdentifiers);

        System.out.print("Set table access policies.");
        // END: com.azure.data.tables.tableClient.setAccessPolicies#List

        // BEGIN: com.azure.data.tables.tableClient.setAccessPoliciesWithResponse#List-Duration-Context
        List<TableSignedIdentifier> mySignedIdentifiers = new ArrayList<>();

        mySignedIdentifiers.add(new TableSignedIdentifier("id1")
            .setAccessPolicy(new TableAccessPolicy()
                .setStartsOn(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setExpiresOn(OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setPermissions("r")));
        mySignedIdentifiers.add(new TableSignedIdentifier("id2")
            .setAccessPolicy(new TableAccessPolicy()
                .setStartsOn(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setExpiresOn(OffsetDateTime.of(2021, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC))
                .setPermissions("raud")));

        Response<Void> response = tableClient.setAccessPoliciesWithResponse(mySignedIdentifiers, Duration.ofSeconds(5),
            new Context("key1", "value1"));

        System.out.printf("Set table access policies successfully with status code: %d.", response.getStatusCode());
        // END: com.azure.data.tables.tableClient.setAccessPoliciesWithResponse#List-Duration-Context
    }

    /**
     * Generates code samples for using {@link TableClient#submitTransaction(List)} and
     * {@link TableClient#submitTransactionWithResponse(List, Duration, Context)}.
     */
    public void submitTransaction() {
        TableClient tableClient = createClient();

        // BEGIN: com.azure.data.tables.tableClient.submitTransaction#List
        List<TableTransactionAction> transactionActions = new ArrayList<>();

        String partitionKey = "markers";
        String firstEntityRowKey = "m001";
        String secondEntityRowKey = "m002";

        TableEntity firstEntity = new TableEntity(partitionKey, firstEntityRowKey)
            .addProperty("Type", "Dry")
            .addProperty("Color", "Red");

        transactionActions.add(new TableTransactionAction(TableTransactionActionType.CREATE, firstEntity));

        System.out.printf("Added create action for entity with partition key '%s', and row key '%s'.%n", partitionKey,
            firstEntityRowKey);

        TableEntity secondEntity = new TableEntity(partitionKey, secondEntityRowKey)
            .addProperty("Type", "Wet")
            .addProperty("Color", "Blue");

        transactionActions.add(new TableTransactionAction(TableTransactionActionType.CREATE, secondEntity));

        System.out.printf("Added create action for entity with partition key '%s', and row key '%s'.%n", partitionKey,
            secondEntityRowKey);

        TableTransactionResult tableTransactionResult = tableClient.submitTransaction(transactionActions);

        System.out.print("Submitted transaction. The ordered response status codes for the actions are:");

        tableTransactionResult.getTransactionActionResponses().forEach(tableTransactionActionResponse ->
            System.out.printf("%n%d", tableTransactionActionResponse.getStatusCode()));
        // END: com.azure.data.tables.tableClient.submitTransaction#List

        // BEGIN: com.azure.data.tables.tableClient.submitTransactionWithError#List
        try {
            TableTransactionResult transactionResult = tableClient.submitTransaction(transactionActions);

            System.out.print("Submitted transaction. The ordered response status codes for the actions are:");

            transactionResult.getTransactionActionResponses().forEach(tableTransactionActionResponse ->
                System.out.printf("%n%d", tableTransactionActionResponse.getStatusCode()));
        } catch (TableTransactionFailedException e) {
            // If the transaction fails, the resulting exception contains the index of the first action that failed.
            int failedActionIndex = e.getFailedTransactionActionIndex();
            // You can use this index to modify the offending action or remove it from the list of actions to send in
            // the transaction, for example.
            transactionActions.remove(failedActionIndex);
            // And then retry submitting the transaction.
        }
        // END: com.azure.data.tables.tableClient.submitTransactionWithError#List

        // BEGIN: com.azure.data.tables.tableClient.submitTransactionWithResponse#List-Duration-Context
        List<TableTransactionAction> myTransactionActions = new ArrayList<>();

        String myPartitionKey = "markers";
        String myFirstEntityRowKey = "m001";
        String mySecondEntityRowKey = "m002";

        TableEntity myFirstEntity = new TableEntity(myPartitionKey, myFirstEntityRowKey)
            .addProperty("Type", "Dry")
            .addProperty("Color", "Red");

        myTransactionActions.add(new TableTransactionAction(TableTransactionActionType.CREATE, myFirstEntity));

        System.out.printf("Added create action for entity with partition key '%s', and row key '%s'.%n", myPartitionKey,
            myFirstEntityRowKey);

        TableEntity mySecondEntity = new TableEntity(myPartitionKey, mySecondEntityRowKey)
            .addProperty("Type", "Wet")
            .addProperty("Color", "Blue");

        myTransactionActions.add(new TableTransactionAction(TableTransactionActionType.CREATE, mySecondEntity));

        System.out.printf("Added create action for entity with partition key '%s', and row key '%s'.%n", myPartitionKey,
            mySecondEntityRowKey);

        Response<TableTransactionResult> response = tableClient.submitTransactionWithResponse(myTransactionActions,
            Duration.ofSeconds(5), new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. The ordered response status codes of the submitted"
            + " actions are:", response.getStatusCode());

        response.getValue().getTransactionActionResponses().forEach(tableTransactionActionResponse ->
            System.out.printf("%n%d", tableTransactionActionResponse.getStatusCode()));
        // END: com.azure.data.tables.tableClient.submitTransactionWithResponse#List-Duration-Context

        // BEGIN: com.azure.data.tables.tableClient.submitTransactionWithResponseWithError#List-Duration-Context
        try {
            Response<TableTransactionResult> transactionResultResponse =
                tableClient.submitTransactionWithResponse(myTransactionActions, Duration.ofSeconds(5),
                    new Context("key1", "value1"));

            System.out.printf("Response successful with status code: %d. The ordered response status codes of the"
                + " submitted actions are:", transactionResultResponse.getStatusCode());

            transactionResultResponse.getValue().getTransactionActionResponses()
                .forEach(tableTransactionActionResponse ->
                    System.out.printf("%n%d", tableTransactionActionResponse.getStatusCode()));
        } catch (TableTransactionFailedException e) {
            // If the transaction fails, the resulting exception contains the index of the first action that failed.
            int failedActionIndex = e.getFailedTransactionActionIndex();
            // You can use this index to modify the offending action or remove it from the list of actions to send in
            // the transaction, for example.
            myTransactionActions.remove(failedActionIndex);
            // And then retry submitting the transaction.
        }
        // END: com.azure.data.tables.tableClient.submitTransactionWithResponseWithError#List-Duration-Context
    }
}
