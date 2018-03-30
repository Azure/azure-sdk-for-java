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
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.User;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;


public class UserCrudTest extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(UserCrudTest.class);

    private Database createdDatabase;
    
    private AsyncDocumentClient.Builder clientBuilder;
    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public UserCrudTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createUser() throws Exception {
        //create user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        
        Observable<ResourceResponse<User>> createObservable = client.createUser(getDatabaseLink(), user, null);

        // validate user creation
        ResourceResponseValidator<User> validator = new ResourceResponseValidator.Builder<User>()
                .withId(user.getId())
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readUser() throws Exception {
 
        //create user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
       
        User readBackUser = client.createUser(getDatabaseLink(), user, null).toBlocking().single().getResource();

        // read user
        Observable<ResourceResponse<User>> readObservable = client.readUser(readBackUser.getSelfLink(), null);
        
        //validate user read
        ResourceResponseValidator<User> validator = new ResourceResponseValidator.Builder<User>()
                .withId(readBackUser.getId())
                .notNullEtag()
                .build();
        
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteUser() throws Exception {
        //create user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        
        User readBackUser = client.createUser(getDatabaseLink(), user, null).toBlocking().single().getResource();

        // delete user
        Observable<ResourceResponse<User>> deleteObservable = client.deleteUser(readBackUser.getSelfLink(), null);

        // validate user delete
        ResourceResponseValidator<User> validator = new ResourceResponseValidator.Builder<User>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);

        //TODO validate after deletion the resource is actually deleted (not found)
    }
    
    @Test(groups = { "simple" }, timeOut = 30000)
    public void upsertUser() throws Exception {

        //create user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        
        User readBackUser = client.upsertUser(getDatabaseLink(), user, null).toBlocking().single().getResource();
        
        // read user to validate creation
        Observable<ResourceResponse<User>> readObservable = client.readUser(readBackUser.getSelfLink(), null);
        
        //validate user read
        ResourceResponseValidator<User> validatorForRead = new ResourceResponseValidator.Builder<User>()
                .withId(readBackUser.getId())
                .notNullEtag()
                .build();
        
        validateSuccess(readObservable, validatorForRead);
        
        client.readUsers(getDatabaseLink(), null).toBlocking().subscribe(users -> {
            try {
                int initialNumberOfUsers = users.getResults().size();
                //update user
                readBackUser.setId(UUID.randomUUID().toString());

                Observable<ResourceResponse<User>> updateObservable = client.upsertUser(getDatabaseLink(), readBackUser, null);

                // validate user upsert
                ResourceResponseValidator<User> validatorForUpdate = new ResourceResponseValidator.Builder<User>()
                        .withId(readBackUser.getId())
                        .notNullEtag()
                        .build();
                
                validateSuccess(updateObservable, validatorForUpdate);
                
                //verify that new user is added due to upsert with changed id
                client.readUsers(getDatabaseLink(), null).toBlocking().subscribe(newUsers ->{
                    int finalNumberOfUsers = newUsers.getResults().size();
                    assertThat(finalNumberOfUsers).isEqualTo(initialNumberOfUsers + 1);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }); 
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceUser() throws Exception {

        //create user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        
        User readBackUser = client.createUser(getDatabaseLink(), user, null).toBlocking().single().getResource();
        
        // read user to validate creation
        Observable<ResourceResponse<User>> readObservable = client.readUser(readBackUser.getSelfLink(), null);
        
        //validate user read
        ResourceResponseValidator<User> validatorForRead = new ResourceResponseValidator.Builder<User>()
                .withId(readBackUser.getId())
                .notNullEtag()
                .build();
        
        validateSuccess(readObservable, validatorForRead);
        
        //update user
        readBackUser.setId(UUID.randomUUID().toString());

        Observable<ResourceResponse<User>> updateObservable = client.replaceUser(readBackUser, null);

        // validate user replace
        ResourceResponseValidator<User> validatorForUpdate = new ResourceResponseValidator.Builder<User>()
                .withId(readBackUser.getId())
                .notNullEtag()
                .build();
        
        validateSuccess(updateObservable, validatorForUpdate);  
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
        safeClose(client);
    }

    private String getDatabaseLink() {
        return createdDatabase.getSelfLink();
    }
}
