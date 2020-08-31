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
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

/**
 * Inherit QueryMethod class to generate a method that is designated to execute a finder query.
 */
public class CosmosQueryMethod extends QueryMethod {

    private final Map<Class<? extends Annotation>, Optional<Annotation>> annotationCache;
    private CosmosEntityMetadata<?> metadata;
    final Method method;

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
        this.method = method;
        this.annotationCache = new ConcurrentReferenceHashMap<>();
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
     * @return if the query method has an annotated query
     */
    public boolean hasAnnotatedQuery() {
        return findAnnotatedQuery().isPresent();
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
        return findAnnotatedQuery().orElse(null);
    }

    private Optional<String> findAnnotatedQuery() {

        return lookupQueryAnnotation()
                   .map(Query::value)
                   .filter(StringUtils::hasText);
    }

    Optional<Query> lookupQueryAnnotation() {
        return doFindAnnotation(Query.class);
    }

    /**
     * Returns the field specification to be used for the query.
     */
    @Nullable
    String getFieldSpecification() {

        return lookupQueryAnnotation() //
                   .map(Query::fields) //
                   .filter(StringUtils::hasText) //
                   .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private <A extends Annotation> Optional<A> doFindAnnotation(Class<A> annotationType) {
        return (Optional<A>) this.annotationCache
                                 .computeIfAbsent(annotationType, it -> Optional.ofNullable(AnnotatedElementUtils
                                                                                .findMergedAnnotation(method, it)));
    }
}
