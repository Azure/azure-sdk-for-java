// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.domain.BasicItem;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CosmosTemplateBulkFailureUnitTest {

    private static final String DATABASE_NAME = "database";

    private CosmosEntityInformation<BasicItem, String> entityInformation;
    private BasicItem entity;
    private RuntimeException bulkException;
    private CosmosTemplate cosmosTemplate;
    private ReactiveCosmosTemplate reactiveCosmosTemplate;

    @BeforeEach
    public void setUp() {
        CosmosAsyncClient client = mock(CosmosAsyncClient.class);
        CosmosAsyncDatabase database = mock(CosmosAsyncDatabase.class);
        CosmosAsyncContainer container = mock(CosmosAsyncContainer.class);
        MappingCosmosConverter converter = mock(MappingCosmosConverter.class);
        CosmosFactory cosmosFactory = new CosmosFactory(client, DATABASE_NAME);

        entity = new BasicItem("id");
        entityInformation = new CosmosEntityInformation<>(BasicItem.class);
        String containerName = entityInformation.getContainerName();
        bulkException = new RuntimeException("Bulk operation failed");
        CosmosBulkOperationResponse<Object> failedResponse = ModelBridgeInternal.createCosmosBulkOperationResponse(
            null, bulkException, null);

        when(client.getDatabase(DATABASE_NAME)).thenReturn(database);
        when(database.getContainer(containerName)).thenReturn(container);
        when(container.executeBulkOperations(
            ArgumentMatchers.<Flux<CosmosItemOperation>>any(), any(CosmosBulkExecutionOptions.class)))
            .thenReturn(Flux.just(failedResponse));
        when(converter.getTransientFields(entity, entityInformation)).thenReturn(Collections.emptyList());
        when(converter.writeJsonNode(entity)).thenReturn(JsonNodeFactory.instance.objectNode().put("id", "id"));

        CosmosConfig cosmosConfig = CosmosConfig.builder().build();
        cosmosTemplate = new CosmosTemplate(cosmosFactory, cosmosConfig, converter);
        reactiveCosmosTemplate = new ReactiveCosmosTemplate(cosmosFactory, cosmosConfig, converter);
    }

    @Test
    public void insertAllPropagatesBulkExceptionWhenResponseIsMissing() {
        assertThatThrownBy(() -> cosmosTemplate.insertAll(entityInformation, Collections.singleton(entity)))
            .isInstanceOf(CosmosAccessException.class)
            .hasCause(bulkException);
    }

    @Test
    public void deleteEntitiesPropagatesBulkExceptionWhenResponseIsMissing() {
        assertThatThrownBy(() -> cosmosTemplate.deleteEntities(entityInformation, Collections.singleton(entity)))
            .isInstanceOf(CosmosAccessException.class)
            .hasCause(bulkException);
    }

    @Test
    public void reactiveInsertAllPropagatesBulkExceptionWhenResponseIsMissing() {
        StepVerifier.create(reactiveCosmosTemplate.insertAll(entityInformation, Flux.just(entity)))
            .expectErrorSatisfies(error -> assertThat(error)
                .isInstanceOf(CosmosAccessException.class)
                .hasCause(bulkException))
            .verify();
    }

    @Test
    public void reactiveDeleteEntitiesPropagatesBulkExceptionWhenResponseIsMissing() {
        StepVerifier.create(reactiveCosmosTemplate.deleteEntities(entityInformation, Flux.just(entity)))
            .expectErrorSatisfies(error -> assertThat(error)
                .isInstanceOf(CosmosAccessException.class)
                .hasCause(bulkException))
            .verify();
    }
}
