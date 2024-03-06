// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

// BEGIN: readme-sample-CosmosClientBeanCodeSnippet
@SpringBootApplication
public class CosmosClientBeanCodeSnippet {

    @Autowired
    private ApplicationContext applicationContext;

    public void cosmosClientBean() {
        CosmosClient cosmosClient = applicationContext.getBean(CosmosClient.class);
        CosmosContainer myContainer = cosmosClient.getDatabase("myDatabase").getContainer("myContainer");
        //  Creating a stored procedure
        myContainer.getScripts().createStoredProcedure(
            new CosmosStoredProcedureProperties("storedProcedureId", "function(){}"),
            new CosmosStoredProcedureRequestOptions());
        //  Reading a stored procedure
        myContainer.getScripts().getStoredProcedure("storedProcedureId").read();
    }

    public void cosmosAsyncClientBean() {
        CosmosAsyncClient cosmosAsyncClient = applicationContext.getBean(CosmosAsyncClient.class);
        CosmosAsyncContainer myAsyncContainer = cosmosAsyncClient.getDatabase("myDatabase").getContainer("myContainer");
        //  Creating a stored procedure
        myAsyncContainer.getScripts().createStoredProcedure(
            new CosmosStoredProcedureProperties("storedProcedureId", "function(){}"),
            new CosmosStoredProcedureRequestOptions()).subscribe();
        //  Reading a stored procedure
        myAsyncContainer.getScripts().getStoredProcedure("storedProcedureId").read().subscribe();
    }
}
// END: readme-sample-CosmosClientBeanCodeSnippet
