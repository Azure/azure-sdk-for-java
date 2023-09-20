// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.query;

import org.springframework.data.repository.query.parser.Part;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Class of criteria
 */
public final class Criteria {

    private String subject;
    private List<Object> subjectValues;
    private final CriteriaType type;
    private final List<Criteria> subCriteria;
    private Part.IgnoreCaseType ignoreCase;

    /**
     * Ignore case flag
     * @return ignore case flag
     */
    public Part.IgnoreCaseType getIgnoreCase() {
        return ignoreCase;
    }

    /**
     * To get subject
     * @return subject value
     */
    public String getSubject() {
        return subject;
    }

    /**
     * To get CriteriaType
     * @return CriteriaType
     */
    public List<Object> getSubjectValues() {
        return subjectValues;
    }

    /**
     * To get CriteriaType
     * @return CriteriaType
     */
    public CriteriaType getType() {
        return type;
    }

    /**
     * To get sub criteria
     * @return List of sub criteria
     */
    public List<Criteria> getSubCriteria() {
        return subCriteria;
    }

    private Criteria(CriteriaType type) {
        this.type = type;
        this.subCriteria = new ArrayList<>();
    }

    /**
     * To get a criteria instance with subject and ignore case
     * @param type CriteriaType
     * @param subject subject
     * @param values subject value
     * @param ignoreCase ignore case flag
     * @return Criteria instance
     */
    public static Criteria getInstance(CriteriaType type, @NonNull String subject,
                                       @NonNull List<Object> values, @NonNull Part.IgnoreCaseType ignoreCase) {
        final Criteria criteria = new Criteria(type);

        criteria.subject = subject;
        criteria.subjectValues = values;
        criteria.ignoreCase = ignoreCase;
        return criteria;
    }

    /**
     * To get a criteria instance with sub criteria
     * @param type CriteriaType
     * @param left Criteria
     * @param right Criteria
     * @return Criteria instance
     */
    public static Criteria getInstance(CriteriaType type, @NonNull Criteria left, @NonNull Criteria right) {
        final Criteria criteria = new Criteria(type);

        criteria.subCriteria.add(left);
        criteria.subCriteria.add(right);

        return criteria;
    }

    /**
     * To get a new criteria instance
     * @param type CriteriaType
     * @return Criteria instance
     */
    public static Criteria getInstance(CriteriaType type) {
        return new Criteria(type);
    }
}
