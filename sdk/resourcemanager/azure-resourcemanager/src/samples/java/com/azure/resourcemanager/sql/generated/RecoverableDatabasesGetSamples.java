// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.sql.generated;

/**
 * Samples for RecoverableDatabases Get.
 */
public final class RecoverableDatabasesGetSamples {
    /*
     * x-ms-original-file:
     * specification/sql/resource-manager/Microsoft.Sql/stable/2021-11-01/examples/RecoverableDatabaseGet.json
     */
    /**
     * Sample code: Get a recoverable database.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void getARecoverableDatabase(com.azure.resourcemanager.AzureResourceManager azure) {
        azure.sqlServers()
            .manager()
            .serviceClient()
            .getRecoverableDatabases()
            .getWithResponse("recoverabledatabasetest-6852", "recoverabledatabasetest-2080",
                "recoverabledatabasetest-9187", com.azure.core.util.Context.NONE);
    }
}
