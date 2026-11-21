// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.generator;

import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.junit.jupiter.api.Assertions.*;

public class NativeQueryGeneratorTest {

    @Mock
    private EmptyQueryGenerator queryGenerator;

    @BeforeEach
    public void setUp() {
        this.queryGenerator = new EmptyQueryGenerator();
    }

    @Test
    public void generateSortedQueryTest() {
        List<SqlParameter> sqlParameters = new ArrayList<>();
        sqlParameters.add(new SqlParameter("@firstName", "TREVOR"));

        SqlQuerySpec querySpec = new SqlQuerySpec("select * from a where a.firstName = @firstName", sqlParameters);
        final SqlQuerySpec sortedQuerySpec = NativeQueryGenerator.getInstance().generateSortedQuery(querySpec, Sort.by(ASC, "id"));

        assertEquals(sortedQuerySpec.getQueryText(), "select * from a where a.firstName = @firstName ORDER BY a.id ASC");
    }

    @Test
    public void concurrentSortedQueriesUseTheirOwnAliases() {
        NativeQueryGenerator generator = NativeQueryGenerator.getInstance();
        SqlQuerySpec firstQuery = new SqlQuerySpec("SELECT * FROM c WHERE c.active = true");
        SqlQuerySpec secondQuery = new SqlQuerySpec("SELECT * FROM r WHERE r.active = true");
        Sort firstSort = Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.asc("id"));
        Sort secondSort = Sort.by(ASC, "name");
        Sort interleavedSort = spy(firstSort);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            // Generate the second query while the first is about to format its ORDER BY columns.
            doAnswer(invocation -> {
                SqlQuerySpec sortedSecondQuery = executor.submit(() -> generator.generateSortedQuery(secondQuery, secondSort))
                    .get(10, TimeUnit.SECONDS);
                assertEquals("SELECT * FROM r WHERE r.active = true ORDER BY r.name ASC",
                    sortedSecondQuery.getQueryText());
                return firstSort.stream();
            }).when(interleavedSort).stream();

            assertEquals("SELECT * FROM c WHERE c.active = true ORDER BY c.updatedAt DESC,c.id ASC",
                generator.generateSortedQuery(firstQuery, interleavedSort).getQueryText());
        } finally {
            executor.shutdownNow();
        }
    }

    private static class EmptyQueryGenerator extends NativeQueryGenerator implements QuerySpecGenerator {

        @Override
        public SqlQuerySpec generateCosmos(CosmosQuery query) {
            return this.generateCosmos(query);
        }
    }
}
