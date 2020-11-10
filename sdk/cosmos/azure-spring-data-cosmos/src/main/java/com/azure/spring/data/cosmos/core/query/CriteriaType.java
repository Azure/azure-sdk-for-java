// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.query;

import org.springframework.data.repository.query.parser.Part;
import org.springframework.lang.NonNull;

import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum of criteria type
 */
public enum CriteriaType {

    ALL(""),
    IS_EQUAL("="),
    OR("OR"),
    AND("AND"),
    NOT("<>"),
    BEFORE("<"),
    AFTER(">"),
    IN("IN"),
    NOT_IN("NOT IN"),
    IS_NULL("IS_NULL"),
    IS_NOT_NULL("NOT IS_NULL"),
    LESS_THAN("<"),
    LESS_THAN_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_EQUAL(">="),
    CONTAINING("CONTAINS"),
    ENDS_WITH("ENDSWITH"),
    STARTS_WITH("STARTSWITH"),
    TRUE("= true"),
    FALSE("= false"),
    BETWEEN("BETWEEN"),
    ARRAY_CONTAINS("ARRAY_CONTAINS");

    private String sqlKeyword;

    private static final Map<Part.Type, CriteriaType> PART_TREE_TYPE_TO_CRITERIA;

    static {
        final Map<Part.Type, CriteriaType> map = new HashMap<>();

        map.put(Part.Type.NEGATING_SIMPLE_PROPERTY, CriteriaType.NOT);
        map.put(Part.Type.IS_NULL, CriteriaType.IS_NULL);
        map.put(Part.Type.IS_NOT_NULL, CriteriaType.IS_NOT_NULL);
        map.put(Part.Type.SIMPLE_PROPERTY, CriteriaType.IS_EQUAL);
        map.put(Part.Type.BEFORE, CriteriaType.BEFORE);
        map.put(Part.Type.AFTER, CriteriaType.AFTER);
        map.put(Part.Type.IN, CriteriaType.IN);
        map.put(Part.Type.NOT_IN, CriteriaType.NOT_IN);
        map.put(Part.Type.GREATER_THAN, CriteriaType.GREATER_THAN);
        map.put(Part.Type.CONTAINING, CriteriaType.CONTAINING);
        map.put(Part.Type.ENDING_WITH, CriteriaType.ENDS_WITH);
        map.put(Part.Type.STARTING_WITH, CriteriaType.STARTS_WITH);
        map.put(Part.Type.GREATER_THAN_EQUAL, CriteriaType.GREATER_THAN_EQUAL);
        map.put(Part.Type.LESS_THAN, CriteriaType.LESS_THAN);
        map.put(Part.Type.LESS_THAN_EQUAL, CriteriaType.LESS_THAN_EQUAL);
        map.put(Part.Type.TRUE, CriteriaType.TRUE);
        map.put(Part.Type.FALSE, CriteriaType.FALSE);
        map.put(Part.Type.BETWEEN, CriteriaType.BETWEEN);

        PART_TREE_TYPE_TO_CRITERIA = Collections.unmodifiableMap(map);
    }

    @ConstructorProperties({"sqlKeyword"})
    CriteriaType(String sqlKeyword) {
        this.sqlKeyword = sqlKeyword;
    }

    /**
     * To get sql keyword
     * @return String
     */
    public String getSqlKeyword() {
        return sqlKeyword;
    }

    /**
     * Check if PartType is NOT supported.
     *
     * @param partType PartType to be checked.
     * @return True if unsupported, or false.
     */
    public static boolean isPartTypeUnSupported(@NonNull Part.Type partType) {
        return !isPartTypeSupported(partType);
    }

    /**
     * Check if PartType is supported.
     *
     * @param partType PartType to be checked.
     * @return True if supported, or false.
     */
    public static boolean isPartTypeSupported(@NonNull Part.Type partType) {
        return PART_TREE_TYPE_TO_CRITERIA.containsKey(partType);
    }

    /**
     * Convert to criteria type.
     *
     * @param partType PartType to be converted.
     * @return CriteriaType
     * @throws UnsupportedOperationException for unsupported part type
     */
    @SuppressWarnings("")
    public static CriteriaType toCriteriaType(@NonNull Part.Type partType) {
        final CriteriaType criteriaType = PART_TREE_TYPE_TO_CRITERIA.get(partType);

        if (criteriaType == null) {
            throw new UnsupportedOperationException("Unsupported part type: "
                + partType);
        }

        return criteriaType;
    }

    /**
     * Check if CriteriaType operation is closure, with format of (A ops A -&gt; A).
     * Example: AND, OR.
     *
     * @param type CriteriaType operation
     * @return True if match, or false.
     */
    public static boolean isClosed(CriteriaType type) {
        switch (type) {
            case AND:
            case OR:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if CriteriaType operation is binary, with format of (A ops A -&gt; B).
     * Example: IS_EQUAL, AFTER.
     *
     * @param type CriteriaType operation
     * @return True if match, or false.
     */
    public static boolean isBinary(CriteriaType type) {
        switch (type) {
            case IN:
            case NOT_IN:
            case AND:
            case OR:
            case NOT:
            case IS_EQUAL:
            case BEFORE:
            case AFTER:
            case LESS_THAN:
            case LESS_THAN_EQUAL:
            case GREATER_THAN:
            case GREATER_THAN_EQUAL:
            case CONTAINING:
            case ENDS_WITH:
            case STARTS_WITH:
            case ARRAY_CONTAINS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if CriteriaType operation is a function.
     *
     * @param type CriteriaType
     * @return True if match, or false.
     */
    public static boolean isFunction(CriteriaType type) {
        switch (type) {
            case CONTAINING:
            case ENDS_WITH:
            case STARTS_WITH:
            case IS_NULL:
            case IS_NOT_NULL:
            case ARRAY_CONTAINS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if CriteriaType operation is unary, with format of (ops A -&gt; B).
     *
     * @param type CriteriaType
     * @return True if match, or false.
     */
    public static boolean isUnary(CriteriaType type) {
        switch (type) {
            case IS_NULL:
            case IS_NOT_NULL:
            case TRUE:
            case FALSE:
                return true;
            default:
                return false;
        }
    }
}
