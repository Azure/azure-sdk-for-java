package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.generated.EasmDefenderClientTestBase;
import com.azure.analytics.defender.easm.models.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataConnectionsTest extends EasmDefenderClientTestBase {

    String dataConnectionName = "shad-data";
    String newDataConnectionName = "sample-dc";
    String clusterName = "sample-cluster";
    String databaseName = "sample-db";


    @Test
    public void testdataConnectionsListWithResponse(){
        DataConnectionPageResponse dataConnectionPageResponse = dataConnectionsClient.list();
        DataConnection DataConnection = dataConnectionPageResponse.getValue().get(0);
        assertNotNull(DataConnection.getName());
        assertNotNull(DataConnection.getDisplayName());
    }

    @Test
    public void testdataConnectionsValidateWithResponse(){
        AzureDataExplorerDataConnectionProperties properties = new AzureDataExplorerDataConnectionProperties()
                .setClusterName(clusterName)
                .setDatabaseName(databaseName)
                .setRegion("eastus");
        AzureDataExplorerDataConnectionData request = new AzureDataExplorerDataConnectionData(properties)
                .setName(newDataConnectionName)
                .setContent(DataConnectionContent.ASSETS)
                .setFrequency(DataConnectionFrequency.DAILY);
        ValidateResponse response = dataConnectionsClient.validate(request);
        assertNull(response.getError());
    }

    @Test
    public void testdataConnectionsGetWithResponse(){
        DataConnection response = dataConnectionsClient.get(dataConnectionName);
        assertEquals(dataConnectionName, response.getName());
        assertEquals(dataConnectionName, response.getDisplayName());
    }

    @Test
    public void testdataConnectionsPutWithResponse(){
        AzureDataExplorerDataConnectionProperties properties = new AzureDataExplorerDataConnectionProperties()
                                                                    .setClusterName(clusterName)
                                                                    .setDatabaseName(databaseName)
                                                                    .setRegion("eastus");
        AzureDataExplorerDataConnectionData request = new AzureDataExplorerDataConnectionData(properties)
                                                            .setContent(DataConnectionContent.ASSETS)
                                                            .setFrequency(DataConnectionFrequency.DAILY);
        DataConnection DataConnection = dataConnectionsClient.put(newDataConnectionName, request);

        assertEquals(newDataConnectionName, DataConnection.getName());
        assertEquals(newDataConnectionName, DataConnection.getDisplayName());
    }

    @Test
    public void testdataConnectionsDeleteWithResponse(){
        dataConnectionsClient.delete(dataConnectionName);
    }
}
