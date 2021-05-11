// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query.criteria;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public final class Criteria {

    private String subject;
    private List<Object> subValues;
    private final CriteriaType type;
    private final List<Criteria> subCriteria;

    private Criteria(CriteriaType type) {
        this.type = type;
        this.subCriteria = new ArrayList<>();
    }

    private static boolean isBinaryOperation(CriteriaType type) {
        switch (type) {
            case AND:
            case OR:
                return true;
            default:
                return false;
        }
    }

    private static boolean isUnaryOperation(CriteriaType type) {
        switch (type) {
            case EXISTS:
            case AFTER:
            case BEFORE:
            case BETWEEN:
            case IS_EQUAL:
                return true;
            default:
                return false;
        }
    }

    public static Criteria getUnaryInstance(CriteriaType type, @NonNull String subject, @NonNull List<Object> values) {
        Assert.isTrue(isUnaryOperation(type), "type should be Unary operation");

        final Criteria criteria = new Criteria(type);

        criteria.subject = subject;
        criteria.subValues = values;

        return criteria;
    }

    public static Criteria getBinaryInstance(CriteriaType type, @NonNull Criteria left, @NonNull Criteria right) {
        Assert.isTrue(isBinaryOperation(type), "type should be Binary operation");

        final Criteria criteria = new Criteria(type);

        criteria.subCriteria.add(left);
        criteria.subCriteria.add(right);

        Assert.isTrue(criteria.getSubCriteria().size() == 2, "Binary should contain 2 subCriteria");

        return criteria;
    }

    public String getSubject() {
        return subject;
    }

    public List<Object> getSubValues() {
        return subValues;
    }

    public CriteriaType getType() {
        return type;
    }

    public List<Criteria> getSubCriteria() {
        return subCriteria;
    }
}
