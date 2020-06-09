/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.data.cosmosdb.core.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

public class DocumentQuery {

    private final Criteria criteria;

    private Sort sort = Sort.unsorted();

    private Pageable pageable = Pageable.unpaged();

    public DocumentQuery(@NonNull Criteria criteria) {
        this.criteria = criteria;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public Sort getSort() {
        return sort;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public DocumentQuery with(@NonNull Sort sort) {
        if (sort.isSorted()) {
            this.sort = sort.and(this.sort);
        }

        return this;
    }

    public DocumentQuery with(@NonNull Pageable pageable) {
        Assert.notNull(pageable, "pageable should not be null");

        this.pageable = pageable;
        return this;
    }

    private boolean isCrossPartitionQuery(@NonNull String keyName) {
        Assert.hasText(keyName, "PartitionKey should have text.");

        final Optional<Criteria> criteria = this.getSubjectCriteria(this.criteria, keyName);

        return criteria.map(criteria1 -> criteria1.getType() != CriteriaType.IS_EQUAL).orElse(true);
    }

    private boolean hasKeywordOr() {
        // If there is OR keyword in DocumentQuery, the top node of Criteria must be OR type.
        return this.criteria.getType() == CriteriaType.OR;
    }

    /**
     * Indicate if DocumentQuery should enable cross partition query.
     *
     * @param partitionKeys The list of partitionKey names.
     * @return
     */
    public boolean isCrossPartitionQuery(@NonNull List<String> partitionKeys) {
        if (partitionKeys.isEmpty()) {
            return true;
        }

        return partitionKeys.stream().filter(this::isCrossPartitionQuery)
                .findFirst()
                .map(p -> true)
                .orElse(hasKeywordOr());
    }

    public Optional<Criteria> getCriteriaByType(@NonNull CriteriaType criteriaType) {
        return getCriteriaByType(criteriaType, this.criteria);
    }

    private Optional<Criteria> getCriteriaByType(@NonNull CriteriaType criteriaType, @NonNull Criteria criteria) {
        if (criteria.getType().equals(criteriaType)) {
            return Optional.of(criteria);
        }

        for (final Criteria subCriteria: criteria.getSubCriteria()) {
            if (getCriteriaByType(criteriaType, subCriteria).isPresent()) {
                return Optional.of(subCriteria);
            }
        }

        return Optional.empty();
    }

    private Optional<Criteria> getSubjectCriteria(@NonNull Criteria criteria, @NonNull String keyName) {
        if (keyName.equals(criteria.getSubject())) {
            return Optional.of(criteria);
        }

        final List<Criteria> subCriteriaList = criteria.getSubCriteria();

        for (final Criteria c : subCriteriaList) {
            final Optional<Criteria> subjectCriteria = getSubjectCriteria(c, keyName);

            if (subjectCriteria.isPresent()) {
                return subjectCriteria;
            }
        }

        return Optional.empty();
    }
}
