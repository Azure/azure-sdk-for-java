// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.mapping;

import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Simple constant holder for a {@link SimpleTypeHolder} enriched with Cosmos specific simple types.
 */
final class CosmosSimpleTypes {

    private static final Set<Class<?>> COSMOS_SIMPLE_TYPES;

    static {
        Set<Class<?>> simpleTypes = new HashSet<>();
        simpleTypes.add(UUID.class);

        COSMOS_SIMPLE_TYPES = Collections.unmodifiableSet(simpleTypes);
    }

    /**
     * The Cosmos {@link SimpleTypeHolder}.
     */
    public static final SimpleTypeHolder HOLDER = new SimpleTypeHolder(COSMOS_SIMPLE_TYPES, true) {

        @Override
        public boolean isSimpleType(Class<?> type) {

            if (type.isEnum()) {
                return true;
            }

            return super.isSimpleType(type);
        }
    };

}
