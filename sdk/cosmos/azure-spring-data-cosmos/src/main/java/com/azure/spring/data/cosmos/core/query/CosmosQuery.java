// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.query;

import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Class for cosmos query
 */
public class CosmosQuery {

    private final Criteria criteria;

    private Sort sort = Sort.unsorted();

    private Pageable pageable = Pageable.unpaged();

    private int limit;

    /**
     * Initialization
     *
     * @param criteria object
     */
    public CosmosQuery(@NonNull Criteria criteria) {
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
     * To get limit number
     *
     * @return int limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * To set limit number
     *
     * @param limit int
     */
    public void setLimit(int limit) {
        if (this.limit == 0) {
            this.limit = limit;
        }
    }

    /**
     * With Sort
     *
     * @param sort Sort
     * @return DocumentQuery object
     */
    public CosmosQuery with(@NonNull Sort sort) {
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
    public CosmosQuery with(@NonNull Pageable pageable) {
        Assert.notNull(pageable, "pageable should not be null");

        this.pageable = pageable;
        return this;
    }

    private boolean isCrossPartitionQuery(@NonNull String keyName) {
        Assert.hasText(keyName, "PartitionKey should have text.");

        final Optional<Criteria> criteria = this.getSubjectCriteria(this.criteria, keyName);

        return criteria.map(criteria1 -> {
            //  If there is equal criteria, then it is a single partition query
            if (isEqualCriteria(criteria1)) {
                return false;
            }
            //  IN is a special case, where we want to first check if the partition key is used with IN clause
            if (criteria1.getType() == CriteriaType.IN && criteria1.getSubjectValues().size() == 1) {
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) criteria1.getSubjectValues().get(0);
                //  IN query types can have multiple values,
                //  so we are checking the internal collection of the criteria
                return collection.size() != 1;
            }
            return !hasKeywordAnd();
        }).orElse(true);
    }

    private boolean hasKeywordOr() {
        // If there is OR keyword in DocumentQuery, the top node of Criteria must be OR type.
        return this.criteria.getType() == CriteriaType.OR;
    }

    private boolean hasKeywordAnd() {
        // If there is AND keyword in DocumentQuery, the top node of Criteria must be AND type.
        return this.criteria.getType() == CriteriaType.AND;
    }

    private boolean isEqualCriteria(Criteria criteria) {
        return criteria.getType() == CriteriaType.IS_EQUAL;
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
     * Returns true if this criteria or sub-criteria has partition key field present as one of the subjects.
     * @param partitionKeyFieldName partition key field name
     * @return returns true if this criteria or sub criteria has partition key field present as one of the subjects.
     */
    public boolean hasPartitionKeyCriteria(@NonNull String partitionKeyFieldName) {
        if (partitionKeyFieldName.isEmpty()) {
            return false;
        }

        final Optional<Criteria> criteria = this.getSubjectCriteria(this.criteria, partitionKeyFieldName);
        return criteria.isPresent();
    }

    /**
     * Returns partition key value based on the criteria.
     * @param domainType domain type
     * @param <T> entity class type
     * @return Optional of partition key value
     */
    public <T> Optional<Object> getPartitionKeyValue(@NonNull Class<T> domainType) {
        CosmosEntityInformation<?, ?> instance = CosmosEntityInformation.getInstance(domainType);
        String partitionKeyFieldName = instance.getPartitionKeyFieldName();
        if (partitionKeyFieldName == null
            || partitionKeyFieldName.isEmpty()
            || isCrossPartitionQuery(Collections.singletonList(partitionKeyFieldName))) {
            return Optional.empty();
        }

        final Optional<Criteria> criteria = this.getSubjectCriteria(this.criteria, partitionKeyFieldName);
        return criteria.map(criteria1 -> {
            //  If the criteria has ignoreCase, then we cannot set the partition key
            //  because of case sensitivity of partition key
            if (!criteria1.getIgnoreCase().equals(Part.IgnoreCaseType.NEVER)) {
                return null;
            }
            if (criteria1.getType() == CriteriaType.IN && criteria1.getSubjectValues().size() == 1) {
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) criteria1.getSubjectValues().get(0);
                //  IN query types can have multiple values,
                //  so we are checking the internal collection of the criteria
                if (collection.size() == 1) {
                    return collection.iterator().next();
                } else {
                    return null;
                }
            }
            return criteria1.getSubjectValues().get(0);
        });
    }

    /**
     * To get criteria by type
     *
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

        for (final Criteria subCriteria : criteria.getSubCriteria()) {
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
