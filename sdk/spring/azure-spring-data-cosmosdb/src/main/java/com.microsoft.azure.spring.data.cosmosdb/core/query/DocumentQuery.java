// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.core.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * Class for document query
 */
public class DocumentQuery {

    private final Criteria criteria;

    private Sort sort = Sort.unsorted();

    private Pageable pageable = Pageable.unpaged();

    /**
     * Initialization
     *
     * @param criteria object
     */
    public DocumentQuery(@NonNull Criteria criteria) {
        this.criteria = criteria;
    }

    /**
     * To get Criteria object
     *
     * @return Criteria
     */
    public Criteria getCriteria() {
        return criteria;
    }

    /**
     * To get Sort object
     *
     * @return Sort
     */
    public Sort getSort() {
        return sort;
    }

    /**
     * To get Pageable object
     *
     * @return Pageable
     */
    public Pageable getPageable() {
        return pageable;
    }

    /**
     * With Sort
     *
     * @param sort Sort
     * @return DocumentQuery object
     */
    public DocumentQuery with(@NonNull Sort sort) {
        if (sort.isSorted()) {
            this.sort = sort.and(this.sort);
        }

        return this;
    }

    /**
     * With Sort
     *
     * @param pageable Sort
     * @return DocumentQuery object
     */
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
     * @return If DocumentQuery should enable cross partition query
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

    /**
     * To get criteria by type
     * @param criteriaType the criteria type
     * @return Optional
     */
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
