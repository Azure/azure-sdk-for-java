// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.codesnippets;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.data.tables.TableAsyncClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableAccessPolicy;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableEntityUpdateMode;
import com.azure.data.tables.models.TableSignedIdentifier;
import com.azure.data.tables.models.TableTransactionAction;
import com.azure.data.tables.models.TableTransactionActionType;
import com.azure.data.tables.models.TableTransactionFailedException;
import reactor.util.context.Context;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets for {@link TableAsyncClient}.
 */
public class TableAsyncClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link TableAsyncClient}.
     *
     * @return An instance of {@link TableAsyncClient}.
     */
    public TableAsyncClient createAsyncClient() {
        // BEGIN: com.azure.data.tables.tableAsyncClient.instantiation
        TableAsyncClient tableAsyncClient = new TableClientBuilder()
            .endpoint("https://myaccount.core.windows.net/")
            .credential(new AzureNamedKeyCredential("name", "key"))
            .tableName("myTable")
            .buildAsyncClient();
        // END: com.azure.data.tables.tableAsyncClient.instantiation

        return tableAsyncClient;
    }

    /**
     * Generates code samples for using {@link TableAsyncClient#createTable()} and
     * {@link TableAsyncClient#createTableWithResponse()}.
     */
    public void createTable() {
        TableAsyncClient tableAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableAsyncClient.createTable
        tableAsyncClient.createTable()
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(tableItem ->
                System.out.printf("Table with name '%s' was created.", tableItem.getName()));
        // END: com.azure.data.tables.tableAsyncClient.createTable

        // BEGIN: com.azure.data.tables.tableAsyncClient.createTableWithResponse
        tableAsyncClient.createTableWithResponse()
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Table with name '%s' was created.",
                    response.getStatusCode(), response.getValue().getName()));
        // END: com.azure.data.tables.tableAsyncClient.createTableWithResponse
    }

    /**
     * Generates code samples for using {@link TableAsyncClient#deleteTable()} and
     * {@link TableAsyncClient#deleteTableWithResponse()}.
     */
    public void deleteTable() {
        TableAsyncClient tableAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableAsyncClient.deleteTable
        tableAsyncClient.deleteTable()
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(unused -> System.out.print("Table was deleted."));
        // END: com.azure.data.tables.tableAsyncClient.deleteTable

        // BEGIN: com.azure.data.tables.tableAsyncClient.deleteTableWithResponse
        tableAsyncClient.deleteTableWithResponse()
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response ->
                System.out.printf("Table was deleted successfully with status code: %d.",
                    response.getStatusCode()));
        // END: com.azure.data.tables.tableAsyncClient.deleteTableWithResponse
    }

    /**
     * Generates code samples for using {@link TableAsyncClient#createEntity(TableEntity)} and
     * {@link TableAsyncClient#createEntityWithResponse(TableEntity)}.
     */
    public void createEntity() {
        TableAsyncClient tableAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableAsyncClient.createEntity#TableEntity
        String partitionKey = "partitionKey";
        String rowKey = "rowKey";

        TableEntity tableEntity = new TableEntity(partitionKey, rowKey)
            .addProperty("Property", "Value");

        tableAsyncClient.createEntity(tableEntity)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(unused ->
                System.out.printf("Table entity with partition key '%s' and row key '%s' was created.", partitionKey,
                    rowKey));
        // END: com.azure.data.tables.tableAsyncClient.createEntity#TableEntity

        // BEGIN: com.azure.data.tables.tableAsyncClient.createEntityWithResponse#TableEntity
        String myPartitionKey = "partitionKey";
        String myRowKey = "rowKey";

        TableEntity myTableEntity = new TableEntity(myPartitionKey, myRowKey)
            .addProperty("Property", "Value");

        tableAsyncClient.createEntityWithResponse(myTableEntity)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Table entity with partition key '%s' and"
                    + " row key '%s' was created.", response.getStatusCode(), myPartitionKey, myRowKey));
        // END: com.azure.data.tables.tableAsyncClient.createEntityWithResponse#TableEntity
    }

    /**
     * Generates code samples for using {@link TableAsyncClient#upsertEntity(TableEntity)} and
     * {@link TableAsyncClient#upsertEntityWithResponse(TableEntity, TableEntityUpdateMode)}.
     */
    public void upsertEntity() {
        TableAsyncClient tableAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableAsyncClient.upsertEntity#TableEntity
        String partitionKey = "partitionKey";
        String rowKey = "rowKey";

        TableEntity tableEntity = new TableEntity(partitionKey, rowKey)
            .addProperty("Property", "Value");

        tableAsyncClient.upsertEntity(tableEntity)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(unused ->
                System.out.printf("Table entity with partition key '%s' and row key '%s' was updated/created.",
                    partitionKey, rowKey));
        // END: com.azure.data.tables.tableAsyncClient.upsertEntity#TableEntity

        // BEGIN: com.azure.data.tables.tableAsyncClient.upsertEntityWithResponse#TableEntity-TableEntityUpdateMode
        String myPartitionKey = "partitionKey";
        String myRowKey = "rowKey";

        TableEntity myTableEntity = new TableEntity(myPartitionKey, myRowKey)
            .addProperty("Property", "Value");

        tableAsyncClient.upsertEntityWithResponse(myTableEntity, TableEntityUpdateMode.REPLACE)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Table entity with partition key '%s' and"
                    + " row key '%s' was updated/created.", response.getStatusCode(), partitionKey, rowKey));
        // END: com.azure.data.tables.tableAsyncClient.upsertEntityWithResponse#TableEntity-TableEntityUpdateMode
    }

    /**
     * Generates code samples for using {@link TableAsyncClient#updateEntity(TableEntity)},
     * {@link TableAsyncClient#updateEntity(TableEntity, TableEntityUpdateMode)} and
     * {@link TableAsyncClient#updateEntityWithResponse(TableEntity, TableEntityUpdateMode, boolean)}.
     */
    public void updateEntity() {
        TableAsyncClient tableAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableAsyncClient.updateEntity#TableEntity
        String partitionKey = "partitionKey";
        String rowKey = "rowKey";

        TableEntity tableEntity = new TableEntity(partitionKey, rowKey)
            .addProperty("Property", "Value");

        tableAsyncClient.updateEntity(tableEntity)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(unused ->
                System.out.printf("Table entity with partition key '%s' and row key '%s' was updated/created.",
                    partitionKey, rowKey));
        // END: com.azure.data.tables.tableAsyncClient.updateEntity#TableEntity

        // BEGIN: com.azure.data.tables.tableAsyncClient.updateEntity#TableEntity-TableEntityUpdateMode
        String myPartitionKey = "partitionKey";
        String myRowKey = "rowKey";

        TableEntity myTableEntity = new TableEntity(myPartitionKey, myRowKey)
            .addProperty("Property", "Value");

        tableAsyncClient.updateEntity(myTableEntity, TableEntityUpdateMode.REPLACE)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(unused ->
                System.out.printf("Table entity with partition key '%s' and row key '%s' was updated/created.",
                    partitionKey, rowKey));
        // END: com.azure.data.tables.tableAsyncClient.updateEntity#TableEntity-TableEntityUpdateMode

        // BEGIN: com.azure.data.tables.tableAsyncClient.updateEntityWithResponse#TableEntity-TableEntityUpdateMode-boolean
        String somePartitionKey = "partitionKey";
        String someRowKey = "rowKey";

        TableEntity someTableEntity = new TableEntity(somePartitionKey, someRowKey)
            .addProperty("Property", "Value");

        tableAsyncClient.updateEntityWithResponse(someTableEntity, TableEntityUpdateMode.REPLACE, true)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Table entity with partition key '%s' and"
                    + " row key '%s' was updated.", response.getStatusCode(), partitionKey, rowKey));
        // END: com.azure.data.tables.tableAsyncClient.updateEntityWithResponse#TableEntity-TableEntityUpdateMode-boolean
    }

    /**
     * Generates code samples for using {@link TableAsyncClient#deleteEntity(String, String)},
     * {@link TableAsyncClient#deleteEntity(TableEntity)} and
     * {@link TableAsyncClient#deleteEntityWithResponse(TableEntity, boolean)} .
     */
    public void deleteEntity() {
        TableAsyncClient tableAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableAsyncClient.deleteEntity#String-String
        String partitionKey = "partitionKey";
        String rowKey = "rowKey";

        tableAsyncClient.deleteEntity(partitionKey, rowKey)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(unused ->
                System.out.printf("Table entity with partition key '%s' and row key '%s' was deleted.", partitionKey,
                    rowKey));
        // END: com.azure.data.tables.tableAsyncClient.deleteEntity#String-String

        // BEGIN: com.azure.data.tables.tableAsyncClient.deleteEntity#TableEntity
        String myPartitionKey = "partitionKey";
        String myRowKey = "rowKey";

        TableEntity myTableEntity = new TableEntity(myPartitionKey, myRowKey)
            .addProperty("Property", "Value");

        tableAsyncClient.deleteEntity(myTableEntity)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(unused ->
                System.out.printf("Table entity with partition key '%s' and row key '%s' was created.", partitionKey,
                    rowKey));
        // END: com.azure.data.tables.tableAsyncClient.deleteEntity#TableEntity

        // BEGIN: com.azure.data.tables.tableAsyncClient.deleteEntityWithResponse#TableEntity
        String somePartitionKey = "partitionKey";
        String someRowKey = "rowKey";

        TableEntity someTableEntity = new TableEntity(somePartitionKey, someRowKey)
            .addProperty("Property", "Value");

        tableAsyncClient.deleteEntityWithResponse(someTableEntity, true)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Table entity with partition key '%s' and"
                    + " row key '%s' was deleted.", response.getStatusCode(), somePartitionKey, someRowKey));
        // END: com.azure.data.tables.tableAsyncClient.deleteEntityWithResponse#TableEntity
    }

    /**
     * Generates code samples for using {@link TableAsyncClient#listEntities()} and
     * {@link TableAsyncClient#listEntities(ListEntitiesOptions)}.
     */
    public void listEntities() {
        TableAsyncClient tableAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableAsyncClient.listEntities
        tableAsyncClient.listEntities()
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(tableEntity ->
                System.out.printf("Retrieved entity with partition key '%s' and row key '%s'.%n",
                    tableEntity.getPartitionKey(), tableEntity.getRowKey()));
        // END: com.azure.data.tables.tableAsyncClient.listEntities

        // BEGIN: com.azure.data.tables.tableAsyncClient.listEntities#ListEntitiesOptions
        List<String> propertiesToSelect = new ArrayList<>();
        propertiesToSelect.add("name");
        propertiesToSelect.add("lastname");
        propertiesToSelect.add("age");

        ListEntitiesOptions listEntitiesOptions = new ListEntitiesOptions()
            .setTop(15)
            .setFilter("PartitionKey eq 'MyPartitionKey' and RowKey eq 'MyRowKey'")
            .setSelect(propertiesToSelect);

        tableAsyncClient.listEntities(listEntitiesOptions)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(tableEntity -> {
                System.out.printf("Retrieved entity with partition key '%s', row key '%s' and properties:%n",
                    tableEntity.getPartitionKey(), tableEntity.getRowKey());

                tableEntity.getProperties().forEach((key, value) ->
                    System.out.printf("Name: '%s'. Value: '%s'.%n", key, value));
            });
        // END: com.azure.data.tables.tableAsyncClient.listEntities#ListEntitiesOptions
    }

    /**
     * Generates code samples for using {@link TableAsyncClient#getEntity(String, String)}  and
     * {@link TableAsyncClient#getEntityWithResponse(String, String, List)}.
     */
    public void getEntity() {
        TableAsyncClient tableAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableAsyncClient.getEntity#String-String
        String partitionKey = "partitionKey";
        String rowKey = "rowKey";

        tableAsyncClient.getEntity(partitionKey, rowKey)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(tableEntity ->
                System.out.printf("Retrieved entity with partition key '%s' and row key '%s'.",
                    tableEntity.getPartitionKey(), tableEntity.getRowKey()));
        // END: com.azure.data.tables.tableAsyncClient.getEntity#String-String

        // BEGIN: com.azure.data.tables.tableAsyncClient.getEntityWithResponse#String-String-ListEntitiesOptions
        String myPartitionKey = "partitionKey";
        String myRowKey = "rowKey";

        List<String> propertiesToSelect = new ArrayList<>();
        propertiesToSelect.add("name");
        propertiesToSelect.add("lastname");
        propertiesToSelect.add("age");

        tableAsyncClient.getEntityWithResponse(myPartitionKey, myRowKey, propertiesToSelect)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response -> {
                TableEntity tableEntity = response.getValue();

                System.out.printf("Response successful with status code: %d. Retrieved entity with partition key '%s',"
                        + " row key '%s' and properties:", response.getStatusCode(), tableEntity.getPartitionKey(),
                    tableEntity.getRowKey());

                tableEntity.getProperties().forEach((key, value) ->
                    System.out.printf("%nName: '%s'. Value: '%s'.", key, value));
            });
        // END: com.azure.data.tables.tableAsyncClient.getEntityWithResponse#String-String-ListEntitiesOptions
    }

    /**
     * Generates code samples for using {@link TableAsyncClient#getAccessPolicies()}  and
     * {@link TableAsyncClient#getAccessPoliciesWithResponse()}.
     */
    public void getAccessPolicies() {
        TableAsyncClient tableAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableAsyncClient.getAccessPolicies
        tableAsyncClient.getAccessPolicies()
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(accessPolicies ->
                accessPolicies.getIdentifiers().forEach(signedIdentifier ->
                    System.out.printf("Retrieved table access policy with id '%s'.", signedIdentifier.getId())));
        // END: com.azure.data.tables.tableAsyncClient.getAccessPolicies

        // BEGIN: com.azure.data.tables.tableAsyncClient.getAccessPoliciesWithResponse
        List<String> propertiesToSelect = new ArrayList<>();
        propertiesToSelect.add("name");
        propertiesToSelect.add("lastname");
        propertiesToSelect.add("age");

        tableAsyncClient.getAccessPoliciesWithResponse()
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response -> {
                System.out.printf("Response successful with status code: %d. Retrieved table access policies with the"
                    + " following IDs:", response.getStatusCode());

                response.getValue().getIdentifiers().forEach(signedIdentifier ->
                    System.out.printf("%n%s", signedIdentifier.getId()));
            });
        // END: com.azure.data.tables.tableAsyncClient.getAccessPoliciesWithResponse
    }

    /**
     * Generates code samples for using {@link TableAsyncClient#setAccessPolicies(List)}   and
     * {@link TableAsyncClient#setAccessPoliciesWithResponse(List)}.
     */
    public void setAccessPolicies() {
        TableAsyncClient tableAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableAsyncClient.setAccessPolicies#List
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

        tableAsyncClient.setAccessPolicies(signedIdentifiers)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(unused -> System.out.print("Set table access policies."));
        // END: com.azure.data.tables.tableAsyncClient.setAccessPolicies#List

        // BEGIN: com.azure.data.tables.tableAsyncClient.setAccessPoliciesWithResponse#List
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

        tableAsyncClient.setAccessPoliciesWithResponse(mySignedIdentifiers)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response ->
                System.out.printf("Set table access policies successfully with status code: %d.",
                    response.getStatusCode()));
        // END: com.azure.data.tables.tableAsyncClient.setAccessPoliciesWithResponse#List
    }

    /**
     * Generates code samples for using {@link TableAsyncClient#submitTransaction(List)} and
     * {@link TableAsyncClient#submitTransactionWithResponse(List)}.
     */
    public void submitTransaction() {
        TableAsyncClient tableAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableAsyncClient.submitTransaction#List
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

        tableAsyncClient.submitTransaction(transactionActions)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(tableTransactionResult -> {
                System.out.print("Submitted transaction. The ordered response status codes for the actions are:");

                tableTransactionResult.getTransactionActionResponses().forEach(tableTransactionActionResponse ->
                    System.out.printf("%n%d", tableTransactionActionResponse.getStatusCode()));
            });
        // END: com.azure.data.tables.tableAsyncClient.submitTransaction#List

        // BEGIN: com.azure.data.tables.tableAsyncClient.submitTransactionWithError#List

        tableAsyncClient.submitTransaction(transactionActions)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .doOnError(TableTransactionFailedException.class, e -> {
                // If the transaction fails, the resulting exception contains the index of the first action that failed.
                int failedActionIndex = e.getFailedTransactionActionIndex();
                // You can use this index to modify the offending action or remove it from the list of actions to send
                // in the transaction, for example.
                transactionActions.remove(failedActionIndex);
                // And then retry submitting the transaction.
            })
            .subscribe(tableTransactionResult -> {
                System.out.print("Submitted transaction. The ordered response status codes for the actions are:");

                tableTransactionResult.getTransactionActionResponses().forEach(tableTransactionActionResponse ->
                    System.out.printf("%n%d", tableTransactionActionResponse.getStatusCode()));
            });
        // END: com.azure.data.tables.tableAsyncClient.submitTransactionWithError#List

        // BEGIN: com.azure.data.tables.tableAsyncClient.submitTransactionWithResponse#List
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

        tableAsyncClient.submitTransactionWithResponse(myTransactionActions)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response -> {
                System.out.printf("Response successful with status code: %d. The ordered response status codes of the"
                    + " submitted actions are:", response.getStatusCode());

                response.getValue().getTransactionActionResponses().forEach(tableTransactionActionResponse ->
                    System.out.printf("%n%d", tableTransactionActionResponse.getStatusCode()));
            });
        // END: com.azure.data.tables.tableAsyncClient.submitTransactionWithResponse#List

        // BEGIN: com.azure.data.tables.tableAsyncClient.submitTransactionWithResponseWithError#List
        tableAsyncClient.submitTransactionWithResponse(myTransactionActions)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .doOnError(TableTransactionFailedException.class, e -> {
                // If the transaction fails, the resulting exception contains the index of the first action that failed.
                int failedActionIndex = e.getFailedTransactionActionIndex();
                // You can use this index to modify the offending action or remove it from the list of actions to send
                // in the transaction, for example.
                transactionActions.remove(failedActionIndex);
                // And then retry submitting the transaction.
            })
            .subscribe(response -> {
                System.out.printf("Response successful with status code: %d. The ordered response status codes of the"
                    + " submitted actions are:", response.getStatusCode());

                response.getValue().getTransactionActionResponses().forEach(tableTransactionActionResponse ->
                    System.out.printf("%n%d", tableTransactionActionResponse.getStatusCode()));
            });
        // END: com.azure.data.tables.tableAsyncClient.submitTransactionWithResponseWithError#List
    }
}
