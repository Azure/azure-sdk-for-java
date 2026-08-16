/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.query.DCountInfo;
import com.azure.cosmos.implementation.query.PartitionedQueryExecutionInfo;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.implementation.query.hybridsearch.HybridSearchQueryInfo;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReadManyByPartitionKeyQueryPlanValidationTest {

    @Test(groups = { "unit" })
    public void rejectsDCountQueryPlan() {
        QueryInfo queryInfo = new QueryInfo();
        DCountInfo dCountInfo = new DCountInfo();
        dCountInfo.setDCountAlias("countAlias");
        queryInfo.set("dCountInfo", dCountInfo);

        assertThatThrownBy(() -> RxDocumentClientImpl.validateQueryPlanForReadManyByPartitionKeys(createQueryPlan(queryInfo, null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("DCOUNT");
    }

    @Test(groups = { "unit" })
    public void rejectsOffsetQueryPlan() {
        QueryInfo queryInfo = new QueryInfo();
        queryInfo.set("offset", 10);

        assertThatThrownBy(() -> RxDocumentClientImpl.validateQueryPlanForReadManyByPartitionKeys(createQueryPlan(queryInfo, null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("OFFSET");
    }

    @Test(groups = { "unit" })
    public void rejectsLimitQueryPlan() {
        QueryInfo queryInfo = new QueryInfo();
        queryInfo.set("limit", 10);

        assertThatThrownBy(() -> RxDocumentClientImpl.validateQueryPlanForReadManyByPartitionKeys(createQueryPlan(queryInfo, null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("LIMIT");
    }

    @Test(groups = { "unit" })
    public void rejectsTopQueryPlan() {
        QueryInfo queryInfo = new QueryInfo();
        queryInfo.set("top", 5);

        assertThatThrownBy(() -> RxDocumentClientImpl.validateQueryPlanForReadManyByPartitionKeys(createQueryPlan(queryInfo, null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TOP");
    }

    @Test(groups = { "unit" })
    public void rejectsHybridSearchQueryPlanWithoutDereferencingNullQueryInfo() {
        assertThatThrownBy(() -> RxDocumentClientImpl.validateQueryPlanForReadManyByPartitionKeys(
            createQueryPlan(null, new HybridSearchQueryInfo())))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("hybrid/vector/full-text");
    }

    @Test(groups = { "unit" })
    public void acceptsSimpleQueryPlan() {
        QueryInfo queryInfo = new QueryInfo();

        assertThatCode(() -> RxDocumentClientImpl.validateQueryPlanForReadManyByPartitionKeys(createQueryPlan(queryInfo, null)))
            .doesNotThrowAnyException();
    }

    private PartitionedQueryExecutionInfo createQueryPlan(QueryInfo queryInfo, HybridSearchQueryInfo hybridSearchQueryInfo) {
        ObjectNode content = Utils.getSimpleObjectMapper().createObjectNode();
        content.put("partitionedQueryExecutionInfoVersion", Constants.PartitionedQueryExecutionInfo.VERSION_1);

        if (queryInfo != null) {
            content.set("queryInfo", Utils.getSimpleObjectMapper().valueToTree(queryInfo.getMap()));
        }
        if (hybridSearchQueryInfo != null) {
            content.set("hybridSearchQueryInfo", Utils.getSimpleObjectMapper().createObjectNode());
        }

        return new PartitionedQueryExecutionInfo(content, null);
    }
}
