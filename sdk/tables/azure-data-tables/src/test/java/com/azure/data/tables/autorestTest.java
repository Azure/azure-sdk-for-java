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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;


import java.util.*;

public class autorestTest {


    @Test
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


    @Test
    AzureTableImpl auth () {
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

    @Test
    void createTable() {
        String tableName = "testTable3";
        AzureTableImpl azureTable = auth();

        TableProperties tableProperties = new TableProperties().setTableName(tableName);
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
        //deleteTableHelper(tableName);

    }


    void deleteTableHelper(String tableName) {

        AzureTableImpl azureTable = auth();
        String requestId = UUID.randomUUID().toString();


        StepVerifier.create(azureTable.getTables().deleteWithResponseAsync(tableName, requestId,
            Context.NONE))
            .assertNext(response -> {
                System.out.println(response);
                Assertions.assertEquals(204, response.getStatusCode());
            })
            .expectComplete()
            .verify();

    }

    @Test
    void deleteTable() {
        String tableName = "testDeleteTable2";
        AzureTableImpl azureTable = auth();
        TableProperties tableProperties = new TableProperties().setTableName(tableName);

        //create than delete a table, successful path
        azureTable.getTables().createWithResponseAsync(tableProperties, UUID.randomUUID().toString(),
            ResponseFormat.RETURN_CONTENT, null, Context.NONE).subscribe(Void -> {
            StepVerifier.create(azureTable.getTables().deleteWithResponseAsync(tableName,  UUID.randomUUID().toString(),
                Context.NONE))
                .assertNext(response -> {
                    Assertions.assertEquals(204, response.getStatusCode());
                })
                .expectComplete()
                .verify();
        });

        StepVerifier.create(azureTable.getTables().deleteWithResponseAsync(tableName,  UUID.randomUUID().toString(),
            Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void queryTable(){
        String tableName = "testTable3";
        AzureTableImpl azureTable = auth();
        String requestId = UUID.randomUUID().toString();
        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_MINIMALMETADATA)
            .setTop(2);

        StepVerifier.create(azureTable.getTables().queryWithResponseAsync(requestId, null,
            queryOptions, Context.NONE))
            .assertNext(response -> {
                System.out.println("OUT" + response.getValue().getValue().get(0).getTableName());
                //System.out.println((TableQueryResponse) response);
                Assertions.assertEquals(200, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

//    @Test
//    void insertAndDeleteWithEtag(){
//        String tableName = "table3";
//        AzureTableImpl azureTable = auth();
//        String requestId = UUID.randomUUID().toString();
//        String pk = "Product";
//        String rk = "whiteboard2";
//        String etag = "";
//
//        Map<String, Object> properties = new HashMap<>();
//        properties.put("PartitionKey", pk);
//        properties.put("RowKey", rk);
//
//        TestPublisher<String> testPublisher = TestPublisher.create();
//        //insert
//        StepVerifier.create(azureTable.getTables().insertEntityWithResponseAsync(tableName, 500,
//            requestId, ResponseFormat.RETURN_CONTENT, properties, null, Context.NONE))
//            .assertNext(response -> {
//                System.out.println(response);
//                response.getValue().get("odata.etag");
//                Assertions.assertEquals(201, response.getStatusCode());
//
//                StepVerifier.create(azureTable.getTables().deleteEntityWithResponseAsync(tableName, pk,
//                    rk, response.getValue().get("odata.etag").toString(), 500, requestId, null, Context.NONE))
//                    .assertNext(response2 -> {
//                        System.out.println(response2);
//                        Assertions.assertEquals(204, response2.getStatusCode());
//                    })
//                    .expectComplete()
//                    .verify();
//
//            })
//            .expectComplete()
//            .verify();
//    }

    @Test
    void insertAndDeleteNoEtag(){
//        String tableName = "table3";
//        String pk = "product";
//        String rk = "glue";
//        insertEntity(tableName,pk, rk);
//        deleteEntity(tableName,pk,rk);
    }

    @Test
    void insertMergeDeleteEntity(){
        String tableName = "table3";
        Map<String, Object> properties = new HashMap<>();
        properties.put("PartitionKey", "Store");
        properties.put("RowKey", "Atlanta");
        properties.put("Size", "200");
        insertEntity(tableName, properties);
        properties.put("Employees", "15");
        properties.remove("Size");
        mergeEntity(tableName,properties);
        //deleteEntity(tableName,properties);
    }

    @Test
    void insertUpdateDeleteEntity(){
        String tableName = "table3";
        Map<String, Object> properties = new HashMap<>();
        properties.put("PartitionKey", "Store");
        properties.put("RowKey", "Boston");
        properties.put("Size", "200");
        insertEntity(tableName,properties);
        properties.put("Employees", "15");
        properties.remove("Size");
        updateEntity(tableName, properties);
        deleteEntity(tableName, properties);
    }

    void updateEntity(String tableName, Map<String, Object> properties) {
        AzureTableImpl azureTable = auth();
        StepVerifier.create(azureTable.getTables().updateEntityWithResponseAsync(tableName, properties.get("PartitionKey").toString(),
            properties.get("RowKey").toString(), 500, UUID.randomUUID().toString(), "*", properties, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(204, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void mergeEntity(String tableName, Map<String, Object> properties ){
        AzureTableImpl azureTable = auth();

        StepVerifier.create(azureTable.getTables().mergeEntityWithResponseAsync(tableName, properties.get("PartitionKey").toString(),
            properties.get("RowKey").toString(), 500, UUID.randomUUID().toString(), "*", properties, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(204, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void insertEntity(String tableName, Map<String, Object> properties){
        AzureTableImpl azureTable = auth();
        String requestId = UUID.randomUUID().toString();

        StepVerifier.create(azureTable.getTables().insertEntityWithResponseAsync(tableName, 500,
            requestId, ResponseFormat.RETURN_CONTENT, properties, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(201, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void deleteEntity(String tableName, Map<String, Object> properties){
        AzureTableImpl azureTable = auth();
        String requestId = UUID.randomUUID().toString();

        StepVerifier.create(azureTable.getTables().deleteEntityWithResponseAsync(tableName, properties.get("PartitionKey").toString(),
            properties.get("RowKey").toString(), "*", 500, requestId, null, Context.NONE))
            .assertNext(response -> {
                System.out.println(response);
                Assertions.assertEquals(204, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryEntity(String tableName){
        AzureTableImpl azureTable = auth();
        String requestId = UUID.randomUUID().toString();
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setSelect("name");

        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableName, null,
            requestId, null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                System.out.println(response);
                Assertions.assertEquals(204, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void allTests() {
        String tableName = "table3";
        //deleteTable(tableName);
        //createTable(tableName);
        //insertEntity(tableName);
        //queryEntity(tableName);
        //queryTable(tableName);
        //deleteTable(tableName);

    }




    @Test
    void TablesAuth() {
        final String connectionString = System.getenv("azure_tables_connection_string");
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        StorageConnectionString storageConnectionString
            = StorageConnectionString.create(connectionString, new ClientLogger("tables"));
        System.out.println(storageConnectionString);

        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();

        TablesSharedKeyCredential sharedKeyCredential = new TablesSharedKeyCredential(authSettings.getAccount().getName(),
            authSettings.getAccount().getAccessKey());

        //storagesharedkey object and the storage auth object
        policies.add(new AddDatePolicy());
        policies.add(new TablesSharedKeyCredentialPolicy(sharedKeyCredential));
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));
        //HttpLoggingPolicy()

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(null)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();

        AzureTableImplBuilder azureTableImplBuilder = new AzureTableImplBuilder();
        AzureTableImpl azureTable = azureTableImplBuilder
            .pipeline(pipeline)
            .url("https://telboytrial.table.core.windows.net/")
            .buildClient();

        try{
            TablesImpl tables = azureTable.getTables();

            StepVerifier.create(tables.createWithResponseAsync(new TableProperties().setTableName("ebTable"),
                "ID23",
                ResponseFormat.RETURN_CONTENT,
                null,null))
                .assertNext(response -> {
                    System.out.println(response);
                    Assertions.assertEquals(200, response.getStatusCode());
                })
                .expectComplete()
                .verify();
        } catch (Exception e){
            System.out.print(e);
        }

//        try{
//            TablesImpl tables = azureTable.getTables();
//
//            StepVerifier.create(tables.deleteWithResponseAsync("ebTable","ID23",Context.NONE))
//                .assertNext(response -> {
//                    System.out.println(response);
//                    Assertions.assertEquals(200, response.getStatusCode());
//                })
//                .expectComplete()
//                .verify();
//        } catch (Exception e){
//            System.out.print(e);
//        }

    }









//    @Test
//    void createTableTest() {
//        StringToSign = VERB + "\n" +
//            Content-MD5 + "\n" +
//            Content-Type + "\n" +
//            Date + "\n" +
//            CanonicalizedResource;
//
//    }
//
//
//    @Test
//    void createAndUpdateTable() throws InterruptedException {
//        TablesImpl ti = new TablesImpl( new AzureTableImpl());
//        Mono<TablesCreateResponse> c = ti.createWithResponseAsync( new TableProperties(), "requestId", RETURN_CONTENT,
//            null , new Context("key", "value"));
//    }

}
