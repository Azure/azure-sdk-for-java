// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.mapping;

import org.junit.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosMappingContextUnitTest {

    @Mock
    ApplicationContext context;

    @Test
    public void mappingContextWithImplicitIdProperty() {
        final CosmosMappingContext context = new CosmosMappingContext();
        final BasicCosmosPersistentEntity<?> entity = context.getPersistentEntity(ClassWithId.class);

        assertThat(entity).isNotNull();
    }

    class ClassWithId {
        String field;
        String id;
    }
}
