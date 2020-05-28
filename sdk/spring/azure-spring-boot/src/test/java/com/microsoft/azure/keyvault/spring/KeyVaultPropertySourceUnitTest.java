// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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

    private static final String TEST_PROPERTY_NAME_1 = "testPropertyName1";
    @Mock
    KeyVaultOperation keyVaultOperation;
    KeyVaultPropertySource keyVaultPropertySource;

    @Before
    public void setup() {
        final String[] propertyNameList = new String[]{TEST_PROPERTY_NAME_1};

        when(keyVaultOperation.get(anyString())).thenReturn(TEST_PROPERTY_NAME_1);
        when(keyVaultOperation.getPropertyNames()).thenReturn(propertyNameList);

        keyVaultPropertySource = new KeyVaultPropertySource(keyVaultOperation);
    }

    @Test
    public void testGetPropertyNames() {
        final String[] result = keyVaultPropertySource.getPropertyNames();

        assertThat(result.length).isEqualTo(1);
        assertThat(result[0]).isEqualTo(TEST_PROPERTY_NAME_1);
    }

    @Test
    public void testGetProperty() {
        final String result = (String) keyVaultPropertySource.getProperty(TEST_PROPERTY_NAME_1);
        assertThat(result).isEqualTo(TEST_PROPERTY_NAME_1);
    }
}
