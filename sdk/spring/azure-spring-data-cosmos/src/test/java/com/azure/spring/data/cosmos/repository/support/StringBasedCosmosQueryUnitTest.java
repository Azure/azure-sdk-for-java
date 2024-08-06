// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.support;

import com.azure.spring.data.cosmos.core.CosmosOperations;
import com.azure.spring.data.cosmos.repository.query.CosmosQueryMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class StringBasedCosmosQueryUnitTest {
    @Mock
    CosmosQueryMethod cosmosQueryMethod;

    @Mock
    CosmosOperations cosmosOperations;

    @Test
    public void testStripExtraWhitespaceFromString() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String expectedResult = "select * from c where c.city = @city";

        StringBasedCosmosQuery sbcq = new StringBasedCosmosQuery(cosmosQueryMethod, cosmosOperations);
        final Method method = sbcq.getClass().getDeclaredMethod("stripExtraWhitespaceFromString", String.class);
        method.setAccessible(true);

        String query1 = "select * \n from c where \n c.city = @city \n";
        Object[] args1 = new Object[1];
        args1[0] = query1;
        String result1 = (String) method.invoke(sbcq, args1);
        assertThat(result1).isEqualTo(expectedResult);

        String query2 = "select * \n from c \n where c.city = @city \n";
        Object[] args2 = new Object[1];
        args2[0] = query2;
        String result2 = (String) method.invoke(sbcq, args2);
        assertThat(result2).isEqualTo(expectedResult);

        String query3 = "        select * \n\n\n\n\n\n from                c \n where c.city  \n    = @city \n    ";
        Object[] args3 = new Object[1];
        args3[0] = query3;
        String result3 = (String) method.invoke(sbcq, args3);
        assertThat(result3).isEqualTo(expectedResult);
    }
}
