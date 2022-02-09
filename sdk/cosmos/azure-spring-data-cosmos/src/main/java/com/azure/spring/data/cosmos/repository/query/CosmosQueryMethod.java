// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import com.azure.spring.data.cosmos.repository.Query;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.EntityMetadata;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Inherit QueryMethod class to generate a method that is designated to execute a finder query.
 */
public class CosmosQueryMethod extends QueryMethod {

    private CosmosEntityMetadata<?> metadata;
    private final String annotatedQueryValue;

    /**
     * Creates a new {@link CosmosQueryMethod} from the given parameters. Looks up the correct query to use
     * for following invocations of the method given.
     *
     * @param method must not be {@literal null}.
     * @param metadata must not be {@literal null}.
     * @param factory must not be {@literal null}.
     */
    public CosmosQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);
        this.annotatedQueryValue = findAnnotatedQuery(method).orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public EntityMetadata<?> getEntityInformation() {
        final Class<Object> domainType = (Class<Object>) getDomainClass();
        final CosmosEntityInformation<Object, String> entityInformation =
            new CosmosEntityInformation<Object, String>(domainType);

        this.metadata = new SimpleCosmosEntityMetadata<Object>(domainType, entityInformation);
        return this.metadata;
    }

    /**
     * Returns whether the method has an annotated query.
     *
     * @return if the query method has an annotated query
     */
    public boolean hasAnnotatedQuery() {
        return annotatedQueryValue != null;
    }

    /**
     * Returns the query string declared in a {@link Query} annotation or {@literal null} if neither the annotation
     * found
     * nor the attribute was specified.
     *
     * @return the query string or null
     */
    @Nullable
    public String getQueryAnnotation() {
        return annotatedQueryValue;
    }

    private Optional<String> findAnnotatedQuery(Method method) {
        return Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(method, Query.class))
                   .map(Query::value)
                   .filter(StringUtils::hasText);
    }

}
