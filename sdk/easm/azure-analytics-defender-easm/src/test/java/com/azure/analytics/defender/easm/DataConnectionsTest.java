// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.*;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataConnectionsTest extends EasmClientTestBase {

    String dataConnectionName = "sample-dc";
    String newDataConnectionName = "sample-dc";
    String clusterName = "sample-cluster";
    String databaseName = "sample-db";

    @Test
    public void testdataConnectionsListWithResponse() {
        PagedIterable<DataConnection> dataConnectionPageResult = easmClient.listDataConnection();
        DataConnection dataConnection = dataConnectionPageResult.stream().iterator().next();
        assertEquals("new-sample-dc", dataConnection.getName());
        assertNotNull("new-sample-dc", dataConnection.getDisplayName());

    }

    @Test
    public void testdataConnectionsValidateWithResponse() {
        AzureDataExplorerDataConnectionProperties properties
            = new AzureDataExplorerDataConnectionProperties().setClusterName(clusterName)
                .setDatabaseName(databaseName)
                .setRegion("eastus");
        AzureDataExplorerDataConnectionData request
            = new AzureDataExplorerDataConnectionData(properties).setName(newDataConnectionName)
                .setContent(DataConnectionContent.ASSETS)
                .setFrequency(DataConnectionFrequency.DAILY);
        ValidateResult response = easmClient.validateDataConnection(request);
        assertNull(response.getError());
    }

    @Test
    public void testdataConnectionsGetWithResponse() {
        DataConnection response = easmClient.getDataConnection(dataConnectionName);
        assertEquals(dataConnectionName, response.getName());
        assertEquals(dataConnectionName, response.getDisplayName());
    }

    @Test
    public void testdataConnectionsPutWithResponse() {
        AzureDataExplorerDataConnectionProperties properties
            = new AzureDataExplorerDataConnectionProperties().setClusterName(clusterName)
                .setDatabaseName(databaseName)
                .setRegion("eastus");
        AzureDataExplorerDataConnectionData request
            = new AzureDataExplorerDataConnectionData(properties).setName(newDataConnectionName)
                .setContent(DataConnectionContent.ASSETS)
                .setFrequency(DataConnectionFrequency.DAILY);
        DataConnection dataConnectionResponse
            = easmClient.createOrReplaceDataConnection(newDataConnectionName, request);

        assertEquals(newDataConnectionName, dataConnectionResponse.getName());
        assertEquals(newDataConnectionName, dataConnectionResponse.getDisplayName());
    }

    @Test
    public void testdataConnectionsDeleteWithResponse() {
        easmClient.deleteDataConnection(dataConnectionName);
    }
}
