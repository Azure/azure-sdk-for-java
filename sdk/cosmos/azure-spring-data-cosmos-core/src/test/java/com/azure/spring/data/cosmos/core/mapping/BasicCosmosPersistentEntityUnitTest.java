// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.mapping;

import com.azure.spring.data.cosmos.domain.Person;
import org.junit.Test;
import org.springframework.data.util.ClassTypeInformation;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicCosmosPersistentEntityUnitTest {

    @Test
    public void testGetCollection() {
        final BasicCosmosPersistentEntity<Person> entity = new BasicCosmosPersistentEntity<Person>(
                ClassTypeInformation.from(Person.class));
        assertThat(entity.getContainer()).isEqualTo("");
    }

    @Test
    public void testGetLanguage() {
        final BasicCosmosPersistentEntity<Person> entity = new BasicCosmosPersistentEntity<Person>(
                ClassTypeInformation.from(Person.class));
        assertThat(entity.getLanguage()).isEqualTo("");
    }

}
