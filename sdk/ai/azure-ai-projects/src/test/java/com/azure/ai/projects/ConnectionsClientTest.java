// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.generated.AIProjectClientTestBase;
import com.azure.ai.projects.implementation.models.GetConnectionResponse;
import com.azure.ai.projects.implementation.models.GetWorkspaceResponse;
import com.azure.ai.projects.implementation.models.ListConnectionsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConnectionsClientTest extends AIProjectClientTestBase {

    @BeforeEach
    void setUp() {
        this.beforeTest();
    }

    @Test
    void getWorkspace() {
        GetWorkspaceResponse workspace = connectionsClient.getWorkspace();
        assertNotNull(workspace);
    }

    @Test
    void listConnections() {
        ListConnectionsResponse connections = connectionsClient.listConnections();
        assertNotNull(connections);
    }

    @Test
    void getConnection() {
        GetConnectionResponse connection = connectionsClient.getConnection("jayant-hub-2aqa-connection-AISearch");
        assertNotNull(connection);
    }
}
