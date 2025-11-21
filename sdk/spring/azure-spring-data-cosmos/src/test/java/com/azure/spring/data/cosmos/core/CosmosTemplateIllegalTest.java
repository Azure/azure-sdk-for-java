// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.domain.Person;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CosmosTemplateIllegalTest {
    private static final String NULL_STR = null;
    private static final String DUMMY_COLL = "dummy";
    private static final String DUMMY_ID = "ID_1";
    private static final PartitionKey DUMMY_KEY = new PartitionKey("dummy");
    private static final String EMPTY_STR = StringUtils.EMPTY;
    private static final String WHITESPACES_STR = "  ";
    private static final String CHECK_FAILURE_MSG = "Illegal argument is not checked";

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private CosmosTemplate cosmosTemplate;
    private Class<?> cosmosTemplateClass;

    @BeforeEach
    public void setUp() {
        cosmosTemplateClass = cosmosTemplate.getClass();
    }

    @Test
    public void deleteIllegalShouldFail() throws NoSuchMethodException {
        final Method method = cosmosTemplateClass.getMethod("delete",
            CosmosQuery.class, Class.class, String.class);
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL,
            "faker", Arrays.asList("faker-value"), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);

        checkIllegalArgument(method, null, Person.class, DUMMY_COLL);
        checkIllegalArgument(method, query, null, DUMMY_COLL);
        checkIllegalArgument(method, query, Person.class, null);
    }

    @Test
    public void deleteIllegalContainerShouldFail() throws NoSuchMethodException {
        final Method method = cosmosTemplateClass.getDeclaredMethod("deleteAll", String.class, Class.class);

        checkIllegalArgument(method, NULL_STR, Person.class);
        checkIllegalArgument(method, EMPTY_STR, Person.class);
        checkIllegalArgument(method, WHITESPACES_STR, Person.class);
    }

    @Test
    public void findByIdIllegalArgsShouldFail() throws NoSuchMethodException {
        final Method method = cosmosTemplateClass.getDeclaredMethod("findById", Object.class, Class.class);

        checkIllegalArgument(method, DUMMY_ID, null);
    }

    /**
     * Check IllegalArgumentException is thrown for illegal parameters
     * @param method method type
     * @param args Method invocation parameters
     */
    private void checkIllegalArgument(Method method, Object... args) {
        try {
            method.invoke(cosmosTemplate, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Assert.isTrue(e.getCause() instanceof IllegalArgumentException, CHECK_FAILURE_MSG);
            return; // Test passed
        }

        throw new IllegalStateException(CHECK_FAILURE_MSG, null);
    }
}
