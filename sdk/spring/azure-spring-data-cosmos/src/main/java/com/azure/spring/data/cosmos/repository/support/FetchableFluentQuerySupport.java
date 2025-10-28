// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.util.Assert;

/**
 * Support class for {@link org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery} implementations.
 *
 * @author Javier Mino
 * @since 3.3
 */
abstract class FetchableFluentQuerySupport<P, T> implements FluentQuery.FetchableFluentQuery<T> {

    private final P predicate;
    private final Sort sort;

    private final int limit;

    private final Class<T> resultType;
    private final List<String> fieldsToInclude;

    FetchableFluentQuerySupport(P predicate, Sort sort, int limit, Class<T> resultType, List<String> fieldsToInclude) {
        this.predicate = predicate;
        this.sort = sort;
        this.limit = limit;
        this.resultType = resultType;
        this.fieldsToInclude = fieldsToInclude;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery#sortBy(org.springframework.data.domain.Sort)
     */
    @Override
    public FluentQuery.FetchableFluentQuery<T> sortBy(Sort sort) {

        Assert.notNull(sort, "Sort must not be null");

        return create(predicate, sort, limit, resultType, fieldsToInclude);
    }

    @Override
    public FluentQuery.FetchableFluentQuery<T> limit(int limit) {

        Assert.isTrue(limit > 0, "Limit must be greater zero");

        return create(predicate, sort, limit, resultType, fieldsToInclude);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery#as(java.lang.Class)
     */
    @Override
    public <R> FluentQuery.FetchableFluentQuery<R> as(Class<R> projection) {

        Assert.notNull(projection, "Projection target type must not be null");

        return create(predicate, sort, limit, projection, fieldsToInclude);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery#project(java.util.Collection)
     */
    @Override
    public FluentQuery.FetchableFluentQuery<T> project(Collection<String> properties) {

        Assert.notNull(properties, "Projection properties must not be null");

        return create(predicate, sort, limit, resultType, new ArrayList<>(properties));
    }

    protected abstract <R> FetchableFluentQuerySupport<P, R> create(P predicate, Sort sort, int limit,
                                                                    Class<R> resultType, List<String> fieldsToInclude);

    P getPredicate() {
        return predicate;
    }

    Sort getSort() {
        return sort;
    }

    int getLimit() {
        return limit;
    }

    Class<T> getResultType() {
        return resultType;
    }
}
