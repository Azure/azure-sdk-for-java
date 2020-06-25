package com.azure.data.tables;

import com.azure.core.http.*;
import com.azure.core.http.policy.*;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.AzureTableImpl;
import com.azure.data.tables.implementation.AzureTableImplBuilder;

import com.azure.data.tables.implementation.TablesImpl;
import com.azure.data.tables.implementation.models.*;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;

import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;
import org.junit.jupiter.api.*;
import reactor.test.StepVerifier;


import java.util.*;
import java.util.concurrent.TimeUnit;

public class autorestTest {
    final String tableA = "tableA";
    final String tableB = "tableB";
    final String tableZ = "tableZ";
    final AzureTableImpl azureTable = auth();
    final String pk = "PartitionKey";
    final String rk = "RowKey";
    Map<String, Object> propertiesB = new HashMap<>();


    void createAndUpdateTableFixed() {
        final String connectionString = System.getenv("azure_tables_connection_string");
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        StorageConnectionString storageConnectionString
            = StorageConnectionString.create(connectionString, new ClientLogger("tables"));

        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        StorageSharedKeyCredential sharedKeyCredential = new StorageSharedKeyCredential(authSettings.getAccount().getName(),
            authSettings.getAccount().getAccessKey());

        //storagesharedkey object and the storage auth object
        policies.add(new AddDatePolicy());
        policies.add(new StorageSharedKeyCredentialPolicy(sharedKeyCredential));
        //HttpLoggingPolicy()

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(null)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();

        AzureTableImplBuilder azureTableImplBuilder = new AzureTableImplBuilder();
        AzureTableImpl azureTable = azureTableImplBuilder
            .pipeline(pipeline)
            .url("/https://telboytrial.table.core.windows.net")
            .buildClient();

        try{
            TablesImpl tables = azureTable.getTables();

            StepVerifier.create(tables.deleteWithResponseAsync("ebTable","ID23",Context.NONE))
                .assertNext(response -> {
                    System.out.println(response);
                    Assertions.assertEquals(200, response.getStatusCode());
                })
                .expectComplete()
                .verify();
        } catch (Exception e){
            System.out.print(e);
        }

    }


    static AzureTableImpl auth() {
        final String connectionString = System.getenv("azure_tables_connection_string");

        StorageConnectionString storageConnectionString
            = StorageConnectionString.create(connectionString, new ClientLogger("tables"));

        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        TablesSharedKeyCredential sharedKeyCredential = new TablesSharedKeyCredential(authSettings.getAccount().getName(),
            authSettings.getAccount().getAccessKey());

        final List<HttpPipelinePolicy> policies = Arrays.asList(
            new AddDatePolicy(),
            new AddHeadersPolicy(new HttpHeaders().put("Accept", OdataMetadataFormat.APPLICATION_JSON_ODATA_MINIMALMETADATA.toString())),
            new TablesSharedKeyCredentialPolicy(sharedKeyCredential),
            new HttpLoggingPolicy(new HttpLogOptions()
                .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
        );
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
        AzureTableImpl azureTable = new AzureTableImplBuilder()
            .pipeline(pipeline)
            .version("2019-02-02")
            .url("https://telboytrial.table.core.windows.net")
            .buildClient();
        return azureTable;
    }


    @BeforeEach
     void beforeTests() {
        createTableHelper(tableA);
        createTableHelper(tableB);

        propertiesB.put("PartitionKey", "Store");
        propertiesB.put("RowKey", "Boston");
        propertiesB.put("Employees", "200");
        insertEntityHelper(tableA, propertiesB);

        Map<String, Object> propertiesA = new HashMap<>();
        propertiesA.put("PartitionKey", "Store");
        propertiesA.put("RowKey", "Atlanta");
        propertiesA.put("Employees", "50");
        insertEntityHelper(tableA, propertiesA);


    }

    @AfterEach
    void afterTests() throws InterruptedException {
        deleteTableHelper(tableA);
        deleteTableHelper(tableB);

    }

    void createTableHelper(String tableName){
        AzureTableImpl azureTable = auth();

        TableProperties tableProperties = new TableProperties().setTableName(tableName);
        String requestId = UUID.randomUUID().toString();

        azureTable.getTables().createWithResponseAsync(tableProperties, requestId,
            ResponseFormat.RETURN_CONTENT, null, Context.NONE).block();

    }

    void deleteTableHelper(String tableName) throws InterruptedException {

        AzureTableImpl azureTable = auth();
        String requestId = UUID.randomUUID().toString();


        azureTable.getTables().deleteWithResponseAsync(tableName, requestId,
            Context.NONE).block();
        TimeUnit.SECONDS.sleep(2);

    }

    void insertEntityHelper(String tableName, Map<String, Object> properties){
        String requestId = UUID.randomUUID().toString();

        azureTable.getTables().insertEntityWithResponseAsync(tableName, 500,
            requestId, ResponseFormat.RETURN_CONTENT, properties, null, Context.NONE).log().block();
    }

    @Test
    void createTable() throws InterruptedException {
        TableProperties tableProperties = new TableProperties().setTableName(tableZ);
        String requestId = UUID.randomUUID().toString();

        //successful path
        StepVerifier.create(azureTable.getTables().createWithResponseAsync(tableProperties, requestId,
            ResponseFormat.RETURN_CONTENT, null, Context.NONE))
            .assertNext(response -> {
                System.out.println(response);
                Assertions.assertEquals(201, response.getStatusCode());
            })
            .expectComplete()
            .verify();

        //error if it tries to create a table with the same name that already exists
        StepVerifier.create(azureTable.getTables().createWithResponseAsync(tableProperties, requestId,
            ResponseFormat.RETURN_CONTENT, null, Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();

        //delete table
        deleteTableHelper(tableZ);

    }

    @Test
    void deleteTable() {

        //create Table
        createTableHelper(tableZ);

        //delete a table, successful path
        StepVerifier.create(azureTable.getTables().deleteWithResponseAsync(tableZ,  UUID.randomUUID().toString(),
            Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(204, response.getStatusCode());
            })
            .expectComplete()
            .verify();

        //try to delete table that is already deleted, should return a TableServiceError
        StepVerifier.create(azureTable.getTables().deleteWithResponseAsync(tableZ,  UUID.randomUUID().toString(),
            Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void queryTable(){
        String requestId = UUID.randomUUID().toString();
        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_MINIMALMETADATA);

        //Verify both are returned with a query without criteria
        StepVerifier.create(azureTable.getTables().queryWithResponseAsync(requestId, null,
            queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(200, response.getStatusCode());
                Assertions.assertEquals(response.getValue().getValue().get(0).getTableName(), tableA);
                Assertions.assertEquals(response.getValue().getValue().get(1).getTableName(), tableB);
            })
            .expectComplete()
            .verify();

        queryOptions.setTop(1);

        //Verify both only first is returned with top filter
        StepVerifier.create(azureTable.getTables().queryWithResponseAsync(requestId, null,
            queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(200, response.getStatusCode());
                Assertions.assertEquals(response.getValue().getValue().size(), 1);
            })
            .expectComplete()
            .verify();
    }

    @Test
    void insertNoEtag(){
        Map<String, Object> properties = new HashMap<>();
        properties.put(pk, "Store");
        properties.put(rk, "Seattle");


        AzureTableImpl azureTable = auth();
        String requestId = UUID.randomUUID().toString();

        StepVerifier.create(azureTable.getTables().insertEntityWithResponseAsync(tableB, 500,
            requestId, ResponseFormat.RETURN_CONTENT, properties, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(201, response.getStatusCode());
            })
            .expectComplete()
            .verify();

    }

    @Test
    void mergeEntity(){
        propertiesB.put("Address", "23 Newbury Street");
        StepVerifier.create(azureTable.getTables().mergeEntityWithResponseAsync(tableA, propertiesB.get("PartitionKey").toString(),
            propertiesB.get("RowKey").toString(), 500, UUID.randomUUID().toString(), "*", propertiesB, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(204, response.getStatusCode());
            })
            .expectComplete()
            .verify();

        //TODO: Query and check
    }

    @Test
    void updateEntity(){
        propertiesB.remove("Size");
        propertiesB.put("Manager", "Jessica Davis");

        StepVerifier.create(azureTable.getTables().updateEntityWithResponseAsync(tableA, propertiesB.get("PartitionKey").toString(),
            propertiesB.get("RowKey").toString(), 500, UUID.randomUUID().toString(), "*", propertiesB, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(204, response.getStatusCode());
            })
            .expectComplete()
            .verify();

        //TODO: Query and check
    }

    @Test
    void deleteEntity(){
        String requestId = UUID.randomUUID().toString();
        Map<String, Object> propertiesC = new HashMap<>();
        propertiesC.put(pk, "Store");
        propertiesC.put(rk, "Chicago");
        insertEntityHelper(tableB, propertiesC);

        StepVerifier.create(azureTable.getTables().deleteEntityWithResponseAsync(tableB, propertiesC.get("PartitionKey").toString(),
            propertiesC.get("RowKey").toString(), "*", 500, requestId, null, Context.NONE))
            .assertNext(response -> {
                System.out.println(response);
                Assertions.assertEquals(204, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryEntity() throws InterruptedException {
        String requestId = UUID.randomUUID().toString();
        QueryOptions queryOptions = new QueryOptions();

        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableA, null,
            requestId, null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(200, response.getStatusCode());
                Assertions.assertEquals(true, response.getValue().getValue().get(0).containsValue("Atlanta"));
                Assertions.assertEquals(true, response.getValue().getValue().get(1).containsValue("Boston"));

            })
            .expectComplete()
            .verify();

        queryOptions.setSelect("Employees");
        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableA, null,
            requestId, null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(200, response.getStatusCode());
                Assertions.assertEquals(true, response.getValue().getValue().get(0).containsValue("50"));
                Assertions.assertEquals(true, response.getValue().getValue().get(1).containsValue("200"));

            })
            .expectComplete()
            .verify();

        //queryOptions.setSelect("");
        queryOptions.setFilter("RowKey eq Boston");
        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableA, null,
            requestId, null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                System.out.print("Here");
                for (Iterator<Map<String, Object>> it = response.getValue().getValue().iterator(); it.hasNext(); ) {
                    Map<String, Object> m = it.next();
                    System.out.println(m);

                }
                Assertions.assertEquals(200, response.getStatusCode());
                Assertions.assertEquals(true, response.getValue().getValue().get(0).containsValue("Boston"));

            })
            .expectComplete()
            .verify();

        TimeUnit.SECONDS.sleep(5);
    }


}
