// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.*;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataConnectionAsyncTest extends EasmClientTestBase {
    String dataConnectionName = "sample-dc";
    String newDataConnectionName = "new-sample-dc";
    String logAnalyticsKey = "sample-key";
    String logAnalyticsWorkspace = "sample-workspace";

    @Test
    public void testDataConnectionListAsync() {
        PagedFlux<DataConnection> dataConnectionPagedFlux = easmAsyncClient.listDataConnection(0);
        List<DataConnection> dataConnectionList = new ArrayList<>();
        StepVerifier.create(dataConnectionPagedFlux)
            .thenConsumeWhile(dataConnectionList::add)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
        for (DataConnection dataConnection : dataConnectionList) {
            assertNotNull(dataConnection);
            assertNotNull(dataConnection.getName());
            assertNotNull(dataConnection.getDisplayName());
        }
    }

    @Test
    public void testDataConnectionValidateAsync() {
        LogAnalyticsDataConnectionProperties properties
            = new LogAnalyticsDataConnectionProperties().setApiKey(logAnalyticsKey)
                .setWorkspaceId(logAnalyticsWorkspace);
        LogAnalyticsDataConnectionData request
            = new LogAnalyticsDataConnectionData(properties).setName(newDataConnectionName)
                .setContent(DataConnectionContent.ATTACK_SURFACE_INSIGHTS)
                .setFrequency(DataConnectionFrequency.WEEKLY)
                .setFrequencyOffset(1);
        Mono<ValidateResult> validateResultMono = easmAsyncClient.validateDataConnection(request);
        StepVerifier.create(validateResultMono).assertNext(validateResult -> {
            assertNull(validateResult.getError());
        }).expectComplete().verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void testDataConnectionsPutAsync() {
        LogAnalyticsDataConnectionProperties properties
            = new LogAnalyticsDataConnectionProperties().setWorkspaceId(logAnalyticsWorkspace)
                .setApiKey(logAnalyticsKey);
        LogAnalyticsDataConnectionData request
            = new LogAnalyticsDataConnectionData(properties).setName(newDataConnectionName)
                .setContent(DataConnectionContent.ATTACK_SURFACE_INSIGHTS)
                .setFrequency(DataConnectionFrequency.WEEKLY)
                .setFrequencyOffset(1);
        Mono<DataConnection> dataConnectionMono
            = easmAsyncClient.createOrReplaceDataConnection(newDataConnectionName, request);
        StepVerifier.create(dataConnectionMono).assertNext(dataConnection -> {
            assertNotNull(dataConnection);
            assertEquals(newDataConnectionName, dataConnection.getName());
            assertEquals(newDataConnectionName, dataConnection.getDisplayName());
        }).expectComplete().verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void testDataConnectionDeleteAsync() {
        Mono<Void> deleteMono = easmAsyncClient.deleteDataConnection(dataConnectionName);
        StepVerifier.create(deleteMono).expectComplete();
    }
}
