// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark.operations;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.dotnet.benchmark.IBenchmarkOperation;
import com.azure.cosmos.dotnet.benchmark.JsonHelper;
import com.azure.cosmos.dotnet.benchmark.OperationResult;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class InsertBenchmarkOperation implements IBenchmarkOperation {
    private final CosmosAsyncContainer container;
    private final String containerName;
    private final String databaseName;
    private final String partitionKeyPath;
    private final ObjectNode sampleJsonNode;
    private final boolean explicitlyProvidePKAndId;
    private PartitionKey partitionKey;

    public InsertBenchmarkOperation(
        CosmosAsyncClient cosmosClient,
        String databaseName,
        String containerName,
        String partitionKeyPath,
        String sampleJson,
        boolean explicitlyProvidePKAndId) {

        this.databaseName = databaseName;
        this.containerName = containerName;
        this.partitionKeyPath = partitionKeyPath.replace("/", "");
        this.sampleJsonNode = (ObjectNode)JsonHelper.fromJsonString(sampleJson);
        this.container = cosmosClient.getDatabase(databaseName).getContainer(containerName);
        this.explicitlyProvidePKAndId = explicitlyProvidePKAndId;
    }

    @Override
    public Mono<Object> prepare() {
        String newId = UUID.randomUUID().toString();
        this.sampleJsonNode.put("id", newId);

        if ("id".equals(this.partitionKeyPath)) {
            this.partitionKey = new PartitionKey(newId);
        } else {
            String newPartitionKey = UUID.randomUUID().toString();
            this.sampleJsonNode.put(this.partitionKeyPath, newPartitionKey);
            this.partitionKey = new PartitionKey(newPartitionKey);
        }

        return Mono.just("");
    }

    @Override
    public Mono<OperationResult> executeOnce() {
        Mono<CosmosItemResponse<ObjectNode>> createTask = this.explicitlyProvidePKAndId ?
            this.container.createItem(
                this.sampleJsonNode,
                this.partitionKey,
                null)
            : this.container.createItem(this.sampleJsonNode);

        return createTask
            .map((r) -> {
                double ruCharges = r.getRequestCharge();

                OperationResult operationResult = new OperationResult();
                operationResult.setDatabaseName(this.databaseName);
                operationResult.setContainerName(this.containerName);
                operationResult.setRuCharges(ruCharges);
                operationResult.setLazyDiagnostics((dummy) -> r.getDiagnostics().toString());

                return operationResult;
            });
    }
}
