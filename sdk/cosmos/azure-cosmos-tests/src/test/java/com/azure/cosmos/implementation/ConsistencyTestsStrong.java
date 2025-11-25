// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.*;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.*;
import org.apache.commons.lang3.Range;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsistencyTestsStrong {

    @Test(groups = {"direct"})
    public void injectTooManyRequestsFaultAndVerify429Count(boolean shouldRegionScopedSessionContainerEnabled) throws InterruptedException {
        string databaseName = "newdatabase";

        // Create a fault injection rule for TooManyRequests (429) in direct mode
        FaultInjectionRule tooManyRequestsRule = new FaultInjectionRuleBuilder(
            id: "TooManyRequestsRule-" + Guid.NewGuid(),
            condition: new FaultInjectionConditionBuilder()
            .WithOperationType(FaultInjectionOperationType.ReadItem)
            .Build(),
            result: FaultInjectionResultBuilder.GetResultBuilder(FaultInjectionServerErrorType.TooManyRequests)
            .Build())
        .Build();

        // inject 404/1002 into all regions
        FaultInjectionRule tooManyRequestsRule = new FaultInjectionRuleBuilder("tooManyRequestsRule-" + UUID.randomUUID())
            .condition(new FaultInjectionConditionBuilder())
            .withOperationType(FaultInjectionOperationType.READ_ITEM)
            .result(
                FaultInjectionResultBuilders
                    .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    .build())
            .build();

        // Initialize the fault injector
        FaultInjector faultInjector = new FaultInjector(new List<FaultInjectionRule> { tooManyRequestsRule });

        CosmosClient cosmosClient = new CosmosClientBuilder(
            connectionString: "")
                    .WithConnectionModeDirect().WithFaultInjection(faultInjector)
            .WithConsistencyLevel(Cosmos.ConsistencyLevel.Eventual)
            .WithThrottlingRetryOptions(
                maxRetryWaitTimeOnThrottledRequests: TimeSpan.FromSeconds(5), // Maximum wait time for retries
            maxRetryAttemptsOnThrottledRequests: 2) // Maximum retry attempts
                    .Build();


        ContainerProperties containerProperties = new ContainerProperties(
            id: "test",
            partitionKeyPath: "/id");


        // Create database and container
        await cosmosClient.CreateDatabaseIfNotExistsAsync(databaseName);
        Container container = await cosmosClient.GetDatabase(databaseName).CreateContainerIfNotExistsAsync(containerProperties);

        dynamic testObject = new
        {
            id = Guid.NewGuid().ToString(),
                Company = "Microsoft",
                State = "WA"

        };


        await container.CreateItemAsync<dynamic>(testObject);
        try
        {
            // Attempt to read the item
            ItemResponse<dynamic> itemResponse = await container.ReadItemAsync<dynamic>(
            testObject.id,
            new Cosmos.PartitionKey(testObject.id));

            // Print diagnostics
            Console.WriteLine("Diagnostics:");
            Console.WriteLine(itemResponse.Diagnostics.ToString());
        }

        catch (CosmosException ex)
        {
            // Handle other Cosmos exceptions
            Console.WriteLine($"CosmosException: {ex.StatusCode} - {ex.Message}");
            Console.WriteLine("Diagnostics:");
            Console.WriteLine(ex.Diagnostics.ToString());
        }
        long hitCount = tooManyRequestsRule.GetHitCount();
        Console.WriteLine($"Total 429 responses: {hitCount}");
    }

}
