// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query.criteria;

import com.azure.spring.data.gremlin.common.Constants;

public enum CriteriaType {
    IS_EQUAL,
    OR,
    AND,
    EXISTS,
    AFTER,
    BEFORE,
    BETWEEN;

    /**
     * Parse criteria types to gremlin primitives.
     *
     * @param type The criteria type.
     * @return Gremlin primitive.
     * @throws UnsupportedOperationException If criteria type is not supported.
     */
    public static String criteriaTypeToGremlin(CriteriaType type) {
        switch (type) {
            case OR:
                return Constants.GREMLIN_PRIMITIVE_OR;
            case AND:
                return Constants.GREMLIN_PRIMITIVE_AND;
            case AFTER:
                return Constants.GREMLIN_PRIMITIVE_IS_GT;
            case BEFORE:
                return Constants.GREMLIN_PRIMITIVE_IS_LT;
            case BETWEEN:
                return Constants.GREMLIN_PRIMITIVE_IS_BETWEEN;
            default:
                throw new UnsupportedOperationException("Unsupported criteria type.");
        }
    }
}
