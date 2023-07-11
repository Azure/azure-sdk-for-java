// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import com.azure.spring.data.cosmos.core.CosmosOperations;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.Assert;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractCosmosQueryUnitTest {

    @Mock
    CosmosQueryMethod method;

    @Mock
    CosmosParameterAccessor accessor;

    @Test
    public void testShouldUseMultiEntityExecutionIfMethodIsCollectionQuery() {
        when(method.isCollectionQuery()).thenReturn(true);
        TestCosmosQuery cosmosQuery = new TestCosmosQuery(method, null);
        CosmosQueryExecution execution = cosmosQuery.getExecution(null, null);
        Assert.isInstanceOf(CosmosQueryExecution.MultiEntityExecution.class, execution);
    }

    @Test
    public void testShouldUseSingleExecutionAsFinalCase() {
        when(method.isCollectionQuery()).thenReturn(false);
        TestCosmosQuery cosmosQuery = new TestCosmosQuery(method, null);
        CosmosQueryExecution execution = cosmosQuery.getExecution(null, null);
        Assert.isInstanceOf(CosmosQueryExecution.SingleEntityExecution.class, execution);
    }

    @Test
    public void testShouldUseSliceExecutionIfMethodIsSliceQuery() {
        when(method.isSliceQuery()).thenReturn(true);
        when(accessor.getPageable()).thenReturn(CosmosPageRequest.of(0, 10));
        TestCosmosQuery cosmosQuery = new TestCosmosQuery(method, null);
        CosmosQueryExecution execution = cosmosQuery.getExecution(accessor, null);
        Assert.isInstanceOf(CosmosQueryExecution.SliceExecution.class, execution);
    }

    private class TestCosmosQuery extends AbstractCosmosQuery {

        TestCosmosQuery(CosmosQueryMethod method, CosmosOperations operations) {
            super(method, operations);
        }

        @Override
        protected CosmosQuery createQuery(CosmosParameterAccessor accessor) {
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
