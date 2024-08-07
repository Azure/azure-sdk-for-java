// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.sql.generated;

/**
 * Samples for OutboundFirewallRules Delete.
 */
public final class OutboundFirewallRulesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/sql/resource-manager/Microsoft.Sql/stable/2021-11-01/examples/OutboundFirewallRuleDelete.json
     */
    /**
     * Sample code: Deletes a outbound firewall rule with a given name.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void
        deletesAOutboundFirewallRuleWithAGivenName(com.azure.resourcemanager.AzureResourceManager azure) {
        azure.sqlServers()
            .manager()
            .serviceClient()
            .getOutboundFirewallRules()
            .delete("sqlcrudtest-7398", "sqlcrudtest-6661", "server.database.windows.net",
                com.azure.core.util.Context.NONE);
    }
}
