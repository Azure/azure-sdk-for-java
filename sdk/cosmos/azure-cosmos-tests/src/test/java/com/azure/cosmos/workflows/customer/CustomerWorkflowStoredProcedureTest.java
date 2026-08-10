// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.workflows.customer;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerWorkflowStoredProcedureTest extends CustomerWorkflowTestBase {

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public CustomerWorkflowStoredProcedureTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fi-customer-workflows"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        initializeSharedSinglePartitionContainer("Customer stored procedure workflow tests");
    }

    @AfterClass(groups = {"fi-customer-workflows"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        closeClient();
    }

    @Test(groups = {"fi-customer-workflows"}, timeOut = TIMEOUT)
    public void storedProcedureCreateReadExecuteWithMetadataFaultRule() {
        String storedProcedureId = "customer-sproc-" + UUID.randomUUID();
        CosmosStoredProcedureProperties storedProcedureProperties = new CosmosStoredProcedureProperties(
            storedProcedureId,
            "function(input) {" +
                "  var value = input || 'workflow';" +
                "  console.log('stored procedure workflow ' + value);" +
                "  getContext().getResponse().setBody('sproc-ok:' + value);" +
                "}");

        CosmosStoredProcedureResponse createResponse = this.container
            .getScripts()
            .createStoredProcedure(storedProcedureProperties)
            .block();

        assertThat(createResponse).isNotNull();
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);
        assertThat(createResponse.getDiagnostics()).isNotNull();

        FaultInjectionRule metadataDelayRule = configureResponseDelayRule(
            this.container,
            FaultInjectionOperationType.METADATA_REQUEST_CONTAINER,
            Duration.ofMillis(100),
            1);

        try {
            CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
            options.setPartitionKey(new PartitionKey("sproc-workflow"));
            options.setScriptLoggingEnabled(true);

            CosmosStoredProcedureResponse readResponse = withStoredProcedureReplicationRetry(() -> this.container
                .getScripts()
                .getStoredProcedure(storedProcedureId)
                .read()
                .block());

            assertThat(readResponse).isNotNull();
            assertThat(readResponse.getProperties().getId()).isEqualTo(storedProcedureId);

            CosmosStoredProcedureResponse executeResponse = withStoredProcedureReplicationRetry(() -> this.container
                .getScripts()
                .getStoredProcedure(storedProcedureId)
                .execute(Collections.singletonList("workflow"), options)
                .block());

            assertThat(executeResponse).isNotNull();
            assertThat(executeResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
            assertThat(executeResponse.getResponseAsString()).contains("sproc-ok:workflow");
            assertThat(executeResponse.getScriptLog()).contains("stored procedure workflow workflow");
            assertThat(executeResponse.getDiagnostics()).isNotNull();
        } finally {
            metadataDelayRule.disable();
            try {
                this.container.getScripts().getStoredProcedure(storedProcedureId).delete().block();
            } catch (Exception error) {
                // best-effort cleanup of the stored procedure created by this test
            }
        }
    }

    /**
     * Retries a stored-procedure operation while it returns 404. A stored procedure that was just created can
     * be temporarily not found when the request is routed to a region the metadata has not yet replicated to
     * (possible on a multi-write account, where stored-procedure metadata is not covered by session
     * read-your-write the way document operations are).
     */
    private CosmosStoredProcedureResponse withStoredProcedureReplicationRetry(Supplier<CosmosStoredProcedureResponse> operation) {
        Duration deadline = Duration.ofSeconds(30);
        long deadlineNanos = System.nanoTime() + deadline.toNanos();
        CosmosException lastNotFound = null;

        while (System.nanoTime() < deadlineNanos) {
            try {
                return operation.get();
            } catch (CosmosException error) {
                if (error.getStatusCode() != HttpConstants.StatusCodes.NOTFOUND) {
                    throw error;
                }
                lastNotFound = error;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError("Interrupted while waiting for stored procedure replication.", interrupted);
                }
            }
        }

        throw new AssertionError("Stored procedure was not available to read within " + deadline, lastNotFound);
    }
}
