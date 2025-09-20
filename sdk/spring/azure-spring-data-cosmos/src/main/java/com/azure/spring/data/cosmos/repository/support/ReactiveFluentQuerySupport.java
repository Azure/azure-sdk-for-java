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
 * Support class for {@link org.springframework.data.repository.query.FluentQuery.ReactiveFluentQuery} implementations.
 */
abstract class ReactiveFluentQuerySupport<P, T> implements FluentQuery.ReactiveFluentQuery<T> {

    private final P predicate;
    private final Sort sort;
    private final int limit;
    private final Class<T> resultType;
    private final List<String> fieldsToInclude;

    ReactiveFluentQuerySupport(P predicate, Sort sort, int limit, Class<T> resultType, List<String> fieldsToInclude) {
        this.predicate = predicate;
        this.sort = sort;
        this.limit = limit;
        this.resultType = resultType;
        this.fieldsToInclude = fieldsToInclude;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.ReactiveFluentQuery#sortBy(org.springframework.data.domain.Sort)
     */
    @Override
    public ReactiveFluentQuery<T> sortBy(Sort sort) {

        Assert.notNull(sort, "Sort must not be null");

        return create(predicate, sort, limit, resultType, fieldsToInclude);
    }

    @Override
    public ReactiveFluentQuery<T> limit(int limit) {

        Assert.isTrue(limit > 0, "Limit must be greater zero");

        return create(predicate, sort, limit, resultType, fieldsToInclude);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.ReactiveFluentQuery#as(java.lang.Class)
     */
    @Override
    public <R> ReactiveFluentQuery<R> as(Class<R> projection) {

        Assert.notNull(projection, "Projection target type must not be null");

        return create(predicate, sort, limit, projection, fieldsToInclude);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.ReactiveFluentQuery#project(java.util.Collection)
     */
    @Override
    public ReactiveFluentQuery<T> project(Collection<String> properties) {

        Assert.notNull(properties, "Projection properties must not be null");

        return create(predicate, sort, limit, resultType, new ArrayList<>(properties));
    }

    protected abstract <R> ReactiveFluentQuerySupport<P, R> create(P predicate, Sort sort, int limit, Class<R> resultType,
                                                                   List<String> fieldsToInclude);

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

    List<String> getFieldsToInclude() {
        return fieldsToInclude;
    }
}
