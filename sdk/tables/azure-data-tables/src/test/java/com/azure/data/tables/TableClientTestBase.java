// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.test.TestBase;
import org.junit.jupiter.api.Test;

public abstract class TableClientTestBase extends TestBase {
    @Test
    public abstract void createTable();

    @Test
    public abstract void createTableWithResponse();

    @Test
    public abstract void createEntity();

    @Test
    public abstract void createEntityWithResponse();

    @Test
    public abstract void createEntityWithAllSupportedDataTypes();

    /*@Test
    public abstract void createEntitySubclass();*/

    @Test
    public abstract void deleteTable();

    @Test
    public abstract void deleteNonExistingTable();

    @Test
    public abstract void deleteTableWithResponse();

    @Test
    public abstract void deleteNonExistingTableWithResponse();

    @Test
    public abstract void deleteEntity();

    @Test
    public abstract void deleteNonExistingEntity();

    @Test
    public abstract void deleteEntityWithResponse();

    @Test
    public abstract void deleteNonExistingEntityWithResponse();

    @Test
    public abstract void deleteEntityWithResponseMatchETag();

    @Test
    public abstract void getEntityWithResponse();

    @Test
    public abstract void getEntityWithResponseWithSelect();

    /*@Test
    public abstract void getEntityWithResponseSubclass();*/

    @Test
    public abstract void updateEntityWithResponseReplace();

    @Test
    public abstract void updateEntityWithResponseMerge();

    /*@Test
    public abstract void updateEntityWithResponseSubclass();*/

    @Test
    public abstract void listEntities();

    @Test
    public abstract void listEntitiesWithFilter();

    @Test
    public abstract void listEntitiesWithSelect();

    @Test
    public abstract void listEntitiesWithTop();

    /*@Test
    public abstract void listEntitiesSubclass();*/

    @Test
    public abstract void submitTransaction();

    @Test
    public abstract void submitTransactionAsyncAllActions();

    @Test
    public abstract void submitTransactionAsyncWithFailingAction();

    @Test
    public abstract void submitTransactionAsyncWithSameRowKeys();

    @Test
    public abstract void submitTransactionAsyncWithDifferentPartitionKeys();

    @Test
    public abstract void generateSasTokenWithMinimumParameters();

    @Test
    public abstract void generateSasTokenWithAllParameters();

    @Test
    public abstract void canUseSasTokenToCreateValidTableClient();

    @Test
    public abstract void setAndListAccessPolicies();

    @Test
    public abstract void setAndListMultipleAccessPolicies();
}
