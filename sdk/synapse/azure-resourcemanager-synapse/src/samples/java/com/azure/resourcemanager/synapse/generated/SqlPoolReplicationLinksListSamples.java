// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.synapse.generated;

/**
 * Samples for SqlPoolReplicationLinks List.
 */
public final class SqlPoolReplicationLinksListSamples {
    /*
     * x-ms-original-file:
     * specification/synapse/resource-manager/Microsoft.Synapse/stable/2021-03-01/examples/ListSqlPoolReplicationLinks.
     * json
     */
    /**
     * Sample code: Lists a Sql Analytic pool's replication links.
     * 
     * @param manager Entry point to SynapseManager.
     */
    public static void
        listsASqlAnalyticPoolSReplicationLinks(com.azure.resourcemanager.synapse.SynapseManager manager) {
        manager.sqlPoolReplicationLinks()
            .list("sqlcrudtest-4799", "sqlcrudtest-6440", "testdb", com.azure.core.util.Context.NONE);
    }
}
