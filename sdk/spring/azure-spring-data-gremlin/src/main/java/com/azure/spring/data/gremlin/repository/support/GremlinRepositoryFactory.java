// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository.support;

import com.azure.spring.data.gremlin.query.GremlinOperations;
import com.azure.spring.data.gremlin.query.query.GremlinQueryMethod;
import com.azure.spring.data.gremlin.query.query.PartTreeGremlinQuery;
import org.springframework.context.ApplicationContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;

public class GremlinRepositoryFactory extends RepositoryFactorySupport {

    private final ApplicationContext context;
    private final GremlinOperations operations;

    public GremlinRepositoryFactory(@NonNull GremlinOperations operations, ApplicationContext context) {
        this.operations = operations;
        this.context = context;
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleGremlinRepository.class;
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {
        final EntityInformation<?, Serializable> entityInfo = this.getEntityInformation(information.getDomainType());

        return getTargetRepositoryViaReflection(information, entityInfo, this.context);
    }

    @Override
    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return new GremlinEntityInformation<>(domainClass);
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(
            QueryLookupStrategy.Key key, QueryMethodEvaluationContextProvider provider) {
        return Optional.of(new GremlinQueryLookupStrategy(this.operations));
    }

    private static class GremlinQueryLookupStrategy implements QueryLookupStrategy {

        private final GremlinOperations operations;

        GremlinQueryLookupStrategy(@NonNull GremlinOperations operations) {
            this.operations = operations;
        }

        @Override
        public RepositoryQuery resolveQuery(@NonNull Method method, RepositoryMetadata metadata,
                                            ProjectionFactory factory, NamedQueries namedQueries) {
            final GremlinQueryMethod queryMethod = new GremlinQueryMethod(method, metadata, factory);

            Assert.notNull(queryMethod, "queryMethod should not be null");
            Assert.notNull(this.operations, "operations should not be null");

            return new PartTreeGremlinQuery(queryMethod, this.operations);
        }
    }
}
