// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.support;

import com.azure.spring.data.cosmos.core.CosmosOperations;
import com.azure.spring.data.cosmos.repository.query.CosmosQueryMethod;
import com.azure.spring.data.cosmos.repository.query.PartTreeCosmosQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Factory class for cosmos repository contains application context and operations information
 */
public class CosmosRepositoryFactory extends RepositoryFactorySupport {

    private final CosmosOperations cosmosOperations;

    /**
     * Initialization
     *
     * @param cosmosOperations for cosmosDb operations
     */
    public CosmosRepositoryFactory(CosmosOperations cosmosOperations) {
        this.cosmosOperations = cosmosOperations;
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleCosmosRepository.class;
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {
        final EntityInformation<?, Serializable> entityInformation = getEntityInformation(information.getDomainType());
        return getTargetRepositoryViaReflection(information, entityInformation, this.cosmosOperations);
    }

    @Override
    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainType) {
        return new CosmosEntityInformation<>(domainType);
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(
            QueryLookupStrategy.Key key, QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional.of(new CosmosDbQueryLookupStrategy(cosmosOperations, evaluationContextProvider));
    }

    private static class CosmosDbQueryLookupStrategy implements QueryLookupStrategy {
        private final CosmosOperations dbOperations;

        CosmosDbQueryLookupStrategy(
                CosmosOperations operations, QueryMethodEvaluationContextProvider provider) {
            this.dbOperations = operations;
        }

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata,
                                            ProjectionFactory factory, NamedQueries namedQueries) {
            final CosmosQueryMethod queryMethod = new CosmosQueryMethod(method, metadata, factory);

            Assert.notNull(queryMethod, "queryMethod must not be null!");
            Assert.notNull(dbOperations, "dbOperations must not be null!");

            if (queryMethod.hasAnnotatedQuery()) {
                return new StringBasedCosmosQuery(queryMethod, dbOperations);
            } else {
                return new PartTreeCosmosQuery(queryMethod, dbOperations);
            }

        }
    }
}
