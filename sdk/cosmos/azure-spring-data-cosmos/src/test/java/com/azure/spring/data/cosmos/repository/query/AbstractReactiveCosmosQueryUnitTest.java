// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import com.azure.spring.data.cosmos.core.ReactiveCosmosOperations;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RunWith(MockitoJUnitRunner.class)
public class AbstractReactiveCosmosQueryUnitTest {

    @Mock
    ReactiveCosmosQueryMethod method;

    @Test
    public void testShouldUseMultiEntityExecutionIfMethodHasFluxReactiveWrapper() {
        Mockito.<Class<?>>when(method.getReactiveWrapper()).thenReturn(Flux.class);
        TestReactiveCosmosQuery cosmosQuery = new TestReactiveCosmosQuery(method, null);
        ReactiveCosmosQueryExecution execution = cosmosQuery.getExecution(null);
        Assert.isInstanceOf(ReactiveCosmosQueryExecution.MultiEntityExecution.class, execution);
    }

    @Test
    public void testShouldUseSingleExecutionIfMethodHasMonoReactiveWrapper() {
        Mockito.<Class<?>>when(method.getReactiveWrapper()).thenReturn(Mono.class);
        TestReactiveCosmosQuery cosmosQuery = new TestReactiveCosmosQuery(method, null);
        ReactiveCosmosQueryExecution execution = cosmosQuery.getExecution(null);
        Assert.isInstanceOf(ReactiveCosmosQueryExecution.SingleEntityExecution.class, execution);
    }

    private class TestReactiveCosmosQuery extends AbstractReactiveCosmosQuery {

        TestReactiveCosmosQuery(ReactiveCosmosQueryMethod method, ReactiveCosmosOperations operations) {
            super(method, operations);
        }

        @Override
        protected CosmosQuery createQuery(ReactiveCosmosParameterAccessor accessor) {
            return null;
        }

        @Override
        protected boolean isDeleteQuery() {
            return false;
        }

        @Override
        protected boolean isExistsQuery() {
            return false;
        }

        @Override
        protected boolean isCountQuery() {
            return false;
        }

        @Override
        protected boolean isPageQuery() {
            return false;
        }
    }
}
