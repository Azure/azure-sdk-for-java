// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.support;

import com.azure.spring.data.cosmos.core.ReactiveCosmosOperations;
import com.azure.spring.data.cosmos.repository.query.PartTreeReactiveCosmosQuery;
import com.azure.spring.data.cosmos.repository.query.ReactiveCosmosQueryMethod;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.ReactiveRepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Factory class for reactive cosmos repository contains application context and operations information
 */
public class ReactiveCosmosRepositoryFactory extends ReactiveRepositoryFactorySupport {

    private final ReactiveCosmosOperations cosmosOperations;

    /**
     * Initialization
     *
     * @param cosmosOperations for cosmosDB operations
     */
    public ReactiveCosmosRepositoryFactory(ReactiveCosmosOperations cosmosOperations) {
        this.cosmosOperations = cosmosOperations;
    }

    @Override
    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainType) {
        return new CosmosEntityInformation<>(domainType);
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {
        final EntityInformation<?, Serializable> entityInformation =
            getEntityInformation(information.getDomainType());
        return getTargetRepositoryViaReflection(information, entityInformation, this.cosmosOperations);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleReactiveCosmosRepository.class;
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(
        QueryLookupStrategy.Key key,
        QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional.of(new ReactiveCosmosQueryLookupStrategy(cosmosOperations,
            evaluationContextProvider));
    }

    private static class ReactiveCosmosQueryLookupStrategy implements QueryLookupStrategy {
        private final ReactiveCosmosOperations cosmosOperations;

        ReactiveCosmosQueryLookupStrategy(
            ReactiveCosmosOperations operations, QueryMethodEvaluationContextProvider provider) {
            this.cosmosOperations = operations;
        }

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata,
                                            ProjectionFactory factory, NamedQueries namedQueries) {
            final ReactiveCosmosQueryMethod queryMethod = new ReactiveCosmosQueryMethod(method,
                metadata, factory);

            Assert.notNull(queryMethod, "queryMethod must not be null!");
            Assert.notNull(cosmosOperations, "dbOperations must not be null!");
            return new PartTreeReactiveCosmosQuery(queryMethod, cosmosOperations);

        }
    }

}
