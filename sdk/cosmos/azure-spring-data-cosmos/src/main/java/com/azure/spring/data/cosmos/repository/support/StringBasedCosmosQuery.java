// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.support;

import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.core.CosmosOperations;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.repository.query.AbstractCosmosQuery;
import com.azure.spring.data.cosmos.repository.query.CosmosEntityMetadata;
import com.azure.spring.data.cosmos.repository.query.CosmosParameterAccessor;
import com.azure.spring.data.cosmos.repository.query.CosmosParameterParameterAccessor;
import com.azure.spring.data.cosmos.repository.query.CosmosQueryMethod;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ResultProcessor;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter.toCosmosDbValue;

/**
 * Cosmos query class to handle the annotated queries. This overrides the execution and runs the query directly
 */
public class StringBasedCosmosQuery extends AbstractCosmosQuery {
    private static final Pattern COUNT_QUERY_PATTERN = Pattern.compile("^\\s*select\\s+value\\s+count.*", Pattern.CASE_INSENSITIVE);

    private final String query;

    /**
     * Constructor
     * @param queryMethod the CosmosQueryMethod
     * @param dbOperations the CosmosOperations
     */
    public StringBasedCosmosQuery(CosmosQueryMethod queryMethod, CosmosOperations dbOperations) {
        super(queryMethod, dbOperations);
        this.query = queryMethod.getQueryAnnotation();
    }

    @Override
    protected CosmosQuery createQuery(CosmosParameterAccessor accessor) {
        return null;
    }

    @Override
    public Object execute(final Object[] parameters) {
        final CosmosParameterAccessor accessor = new CosmosParameterParameterAccessor(getQueryMethod(), parameters);
        final ResultProcessor processor = getQueryMethod().getResultProcessor().withDynamicProjection(accessor);

        List<SqlParameter> sqlParameters = getQueryMethod().getParameters().stream()
                            .filter(p -> !Pageable.class.isAssignableFrom(p.getType()) && !Sort.class.isAssignableFrom(p.getType()))
                            .map(p -> new SqlParameter("@" + p.getName().orElse(""),
                                                       toCosmosDbValue(parameters[p.getIndex()])))
                            .collect(Collectors.toList());

        SqlQuerySpec querySpec = new SqlQuerySpec(query, sqlParameters);
        if (isPageQuery()) {
            return this.operations.runPaginationQuery(querySpec, accessor.getPageable(), processor.getReturnedType().getDomainType(),
                                                      processor.getReturnedType().getReturnedType());
        } else if (isCountQuery()) {
            final String container = ((CosmosEntityMetadata<?>) getQueryMethod().getEntityInformation()).getContainerName();
            return this.operations.count(querySpec, container);
        } else {
            return this.operations.runQuery(querySpec, accessor.getSort(), processor.getReturnedType().getDomainType(),
                                            processor.getReturnedType().getReturnedType());
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

    @Override
    protected boolean isCountQuery() {
        return isCountQuery(query, getQueryMethod().getReturnedObjectType());
    }

    static boolean isCountQuery(String query, Class<?> returnedType) {
        if (isCountQueryReturnType(returnedType)) {
            return COUNT_QUERY_PATTERN.matcher(query).matches();
        } else {
            return false;
        }
    }

    private static boolean isCountQueryReturnType(Class<?> returnedType) {
        return returnedType == Long.class
            || returnedType == long.class
            || returnedType == Integer.class
            || returnedType == int.class;
    }

}
