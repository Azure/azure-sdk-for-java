/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.keyvault.spring;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyVaultPropertySourceUnitTest {
    private static final String testPropertyName1 = "testPropertyName1";
    @Mock
    KeyVaultOperation keyVaultOperation;
    KeyVaultPropertySource keyVaultPropertySource;

    @Before
    public void setup() {
        final String[] propertyNameList = new String[]{testPropertyName1};

        when(keyVaultOperation.get(anyString())).thenReturn(testPropertyName1);
        when(keyVaultOperation.list()).thenReturn(propertyNameList);

        keyVaultPropertySource = new KeyVaultPropertySource(keyVaultOperation);
    }

    @Test
    public void testGetPropertyNames() {
        final String[] result = keyVaultPropertySource.getPropertyNames();

        assertThat(result.length).isEqualTo(1);
        assertThat(result[0]).isEqualTo(testPropertyName1);
    }

    @Test
    public void testGetProperty() {
        final String result = (String) keyVaultPropertySource.getProperty(testPropertyName1);
        assertThat(result).isEqualTo(testPropertyName1);
    }
}
