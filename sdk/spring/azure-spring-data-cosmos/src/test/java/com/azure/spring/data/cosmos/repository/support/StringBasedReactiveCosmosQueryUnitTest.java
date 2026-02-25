// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.support;

import com.azure.spring.data.cosmos.core.ReactiveCosmosOperations;
import com.azure.spring.data.cosmos.repository.query.ReactiveCosmosQueryMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class StringBasedReactiveCosmosQueryUnitTest {
    @Mock
    ReactiveCosmosQueryMethod reactiveCosmosQueryMethod;

    @Mock
    ReactiveCosmosOperations reactiveCcosmosOperations;

    @Test
    public void testStripExtraWhitespaceFromString() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String expectedResult = "select * from a where a.city = @city";

        StringBasedReactiveCosmosQuery sbrcq = new StringBasedReactiveCosmosQuery(reactiveCosmosQueryMethod, reactiveCcosmosOperations);
        final Method method = sbrcq.getClass().getDeclaredMethod("stripExtraWhitespaceFromString", String.class);
        method.setAccessible(true);

        String query1 = "select * \n from a where \n a.city = @city \n";
        Object[] args1 = new Object[1];
        args1[0] = query1;
        String result1 = (String) method.invoke(sbrcq, args1);
        assertThat(result1).isEqualTo(expectedResult);

        String query2 = "select * \n from a \n where a.city = @city \n";
        Object[] args2 = new Object[1];
        args2[0] = query2;
        String result2 = (String) method.invoke(sbrcq, args2);
        assertThat(result2).isEqualTo(expectedResult);

        String query3 = "        select * \n\n\n\n\n\n from                a \n where a.city  \n    = @city \n    ";
        Object[] args3 = new Object[1];
        args3[0] = query3;
        String result3 = (String) method.invoke(sbrcq, args3);
        assertThat(result3).isEqualTo(expectedResult);
    }
}
