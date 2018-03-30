/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.StoredProcedure;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;


public class StoredProcedureUpsertReplaceTest extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(StoredProcedureUpsertReplaceTest.class);

    private Database createdDatabase;
    private DocumentCollection createdCollection;

    private AsyncDocumentClient.Builder clientBuilder;
    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public StoredProcedureUpsertReplaceTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void upsertStoredProcedure() throws Exception {
        
        // create a stored procedure
        StoredProcedure storedProcedureDef = new StoredProcedure();
        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 10;}");
        StoredProcedure readBackSp = client.upsertStoredProcedure(getCollectionLink(), storedProcedureDef, null).toBlocking().single().getResource();

        //read back stored procedure
        Observable<ResourceResponse<StoredProcedure>> readObservable = client.readStoredProcedure(readBackSp.getSelfLink(), null);

        // validate stored procedure creation
        ResourceResponseValidator<StoredProcedure> validatorForRead = new ResourceResponseValidator.Builder<StoredProcedure>()
                .withId(readBackSp.getId())
                .withStoredProcedureBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validatorForRead);
        
        //update stored procedure
        readBackSp.setBody("function() {var x = 11;}");

        Observable<ResourceResponse<StoredProcedure>> updateObservable = client.upsertStoredProcedure(getCollectionLink(), readBackSp, null);

        // validate stored procedure update
        ResourceResponseValidator<StoredProcedure> validatorForUpdate = new ResourceResponseValidator.Builder<StoredProcedure>()
                .withId(readBackSp.getId())
                .withStoredProcedureBody("function() {var x = 11;}")
                .notNullEtag()
                .build();
        validateSuccess(updateObservable, validatorForUpdate);   
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceStoredProcedure() throws Exception {

        // create a stored procedure
        StoredProcedure storedProcedureDef = new StoredProcedure();
        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 10;}");        
        StoredProcedure readBackSp = client.createStoredProcedure(getCollectionLink(), storedProcedureDef, null).toBlocking().single().getResource();
        
        // read stored procedure to validate creation
        Observable<ResourceResponse<StoredProcedure>> readObservable = client.readStoredProcedure(readBackSp.getSelfLink(), null);

        // validate stored procedure creation
        ResourceResponseValidator<StoredProcedure> validatorForRead = new ResourceResponseValidator.Builder<StoredProcedure>()
                .withId(readBackSp.getId())
                .withStoredProcedureBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validatorForRead);
        
        //update stored procedure
        readBackSp.setBody("function() {var x = 11;}");

        Observable<ResourceResponse<StoredProcedure>> replaceObservable = client.replaceStoredProcedure(readBackSp, null);

        //validate stored procedure replace
        ResourceResponseValidator<StoredProcedure> validatorForReplace = new ResourceResponseValidator.Builder<StoredProcedure>()
                .withId(readBackSp.getId())
                .withStoredProcedureBody("function() {var x = 11;}")
                .notNullEtag()
                .build();
        validateSuccess(replaceObservable, validatorForReplace);   
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void executeStoredProcedure() throws Exception {
        // create a stored procedure
        StoredProcedure storedProcedureDef = new StoredProcedure(
                "{" +
                        "  'id': '" +UUID.randomUUID().toString() + "'," +
                        "  'body':" +
                        "    'function () {" +
                        "      for (var i = 0; i < 10; i++) {" +
                        "        getContext().getResponse().appendValue(\"Body\", i);" +
                        "      }" +
                        "    }'" +
                        "}");
        
        StoredProcedure storedProcedure = client.createStoredProcedure(getCollectionLink(), storedProcedureDef, null).toBlocking().single().getResource();

        // execute
        String result = client.executeStoredProcedure(storedProcedure.getSelfLink(), null).toBlocking().single().getResponseAsString();
        assertThat(result).isEqualTo("\"0123456789\"");
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();;       
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinitionSinglePartition());
    }
    
    private static DocumentCollection getCollectionDefinitionSinglePartition() {
        
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());

        return collectionDefinition;
    }
    
    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
        safeClose(client);
    }

    private String getCollectionLink() {
        return createdCollection.getSelfLink();
    }
}
