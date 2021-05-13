// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.support;

import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.core.ReactiveCosmosOperations;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.repository.query.AbstractReactiveCosmosQuery;
import com.azure.spring.data.cosmos.repository.query.ReactiveCosmosParameterAccessor;
import com.azure.spring.data.cosmos.repository.query.ReactiveCosmosParameterParameterAccessor;
import com.azure.spring.data.cosmos.repository.query.ReactiveCosmosQueryMethod;
import com.azure.spring.data.cosmos.repository.query.SimpleReactiveCosmosEntityMetadata;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ResultProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter.toCosmosDbValue;

/**
 * Cosmos query class to handle the annotated queries. This overrides the execution and runs the query directly
 */
public class StringBasedReactiveCosmosQuery extends AbstractReactiveCosmosQuery {

    private final String query;

    /**
     * Constructor
     * @param queryMethod the query method
     * @param dbOperations the reactive cosmos operations
     */
    public StringBasedReactiveCosmosQuery(ReactiveCosmosQueryMethod queryMethod,
                                          ReactiveCosmosOperations dbOperations) {
        super(queryMethod, dbOperations);
        this.query = queryMethod.getQueryAnnotation();
    }

    @Override
    protected CosmosQuery createQuery(ReactiveCosmosParameterAccessor accessor) {
        return null;
    }

    @Override
    public Object execute(final Object[] parameters) {
        final ReactiveCosmosParameterAccessor accessor = new ReactiveCosmosParameterParameterAccessor(getQueryMethod(),
                                                                                              parameters);
        final ResultProcessor processor = getQueryMethod().getResultProcessor().withDynamicProjection(accessor);

        List<SqlParameter> sqlParameters = getQueryMethod().getParameters().stream()
                            .filter(p -> !Sort.class.isAssignableFrom(p.getType()))
                            .map(p -> new SqlParameter("@" + p.getName().orElse(""),
                                                       toCosmosDbValue(parameters[p.getIndex()])))
                            .collect(Collectors.toList());

        SqlQuerySpec querySpec = new SqlQuerySpec(query, sqlParameters);
        if (isCountQuery()) {
            final String container = ((SimpleReactiveCosmosEntityMetadata<?>) getQueryMethod().getEntityInformation()).getContainerName();
            final Mono<Long> mono = this.operations.count(querySpec, container);
            return mono;
        } else {
            Flux<?> flux = this.operations.runQuery(querySpec, accessor.getSort(), processor.getReturnedType().getDomainType(),
                                                    processor.getReturnedType().getReturnedType());
            return flux;
        }
    }

    @Override
    protected boolean isDeleteQuery() {
        return false;
    }

    @Override
    protected boolean isExistsQuery() {
        return false;
    }

    protected boolean isCountQuery() {
        return StringBasedCosmosQuery.isCountQuery(query, getQueryMethod().getReturnedObjectType());
    }

}
