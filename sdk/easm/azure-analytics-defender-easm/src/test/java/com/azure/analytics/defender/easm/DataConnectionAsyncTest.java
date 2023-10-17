// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.generated.EasmClientTestBase;
import com.azure.analytics.defender.easm.models.*;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

public class DataConnectionAsyncTest extends EasmClientTestBase {
    String dataConnectionName = "sample-dc";
    String newDataConnectionName = "new-sample-dc";
    String logAnalyticsKey = "sample-key";
    String logAnalyticsWorkspace = "sample-workspace";

    @Test
    public void testDataConnectionListAsync() {
        PagedFlux<DataConnection> dataConnectionPagedFlux = easmAsyncClient.listDataConnection(0);
        Mono<DataConnection> dataConnectionMono = dataConnectionPagedFlux.next();
        dataConnectionMono.subscribe(
            value -> {
                assertNotNull(value);
                assertNotNull(value.getName());
                assertNotNull(value.getDisplayName());
                System.out.println("Display Name is: " + value.getDisplayName());
            },
            error -> {
                System.err.println("Failed data connection list " + error);
            }
        );
    }

    @Test
    public void testDataConnectionValidateAsync() {
        LogAnalyticsDataConnectionProperties properties = new LogAnalyticsDataConnectionProperties()
            .setApiKey(logAnalyticsKey)
            .setWorkspaceId(logAnalyticsWorkspace);
        LogAnalyticsDataConnectionData request = new LogAnalyticsDataConnectionData(properties)
            .setName(newDataConnectionName)
            .setContent(DataConnectionContent.ATTACK_SURFACE_INSIGHTS)
            .setFrequency(DataConnectionFrequency.WEEKLY);
        Mono<ValidateResult> validateResultMono = easmAsyncClient.validateDataConnection(request);
        validateResultMono.subscribe(
            validateResult -> {
                assertNull(validateResult);
            },
            error -> {
                System.out.println("Failed validate data connection " + error);
            }
        );
    }

    @Test
    public void testDataConnectionsPutAsync() {
        LogAnalyticsDataConnectionProperties properties = new LogAnalyticsDataConnectionProperties()
            .setWorkspaceId(logAnalyticsWorkspace)
            .setApiKey(logAnalyticsKey);
        LogAnalyticsDataConnectionData request = new LogAnalyticsDataConnectionData(properties)
            .setName(newDataConnectionName)
            .setContent(DataConnectionContent.ATTACK_SURFACE_INSIGHTS)
            .setFrequency(DataConnectionFrequency.WEEKLY);
        Mono<DataConnection> dataConnectionMono = easmAsyncClient.createOrReplaceDataConnection(newDataConnectionName, request);
        dataConnectionMono.subscribe(
            dataConnection -> {
                assertNotNull(dataConnection);
                assertEquals(newDataConnectionName, dataConnection.getName());
                assertEquals(newDataConnectionName, dataConnection.getDisplayName());
            },
            error -> {
                System.err.println("Failed create or replace data connection " + error);
            }
        );
    }

    @Test
    public void testDataConnectionDeleteAsync() {
        easmAsyncClient.deleteDataConnection(dataConnectionName);
    }
}
