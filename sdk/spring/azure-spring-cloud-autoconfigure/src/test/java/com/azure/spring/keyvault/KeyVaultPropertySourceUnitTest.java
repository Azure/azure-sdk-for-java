// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


public class KeyVaultPropertySourceUnitTest {

    private static final String TEST_PROPERTY_NAME_1 = "testPropertyName1";

    private AutoCloseable closeable;

    @Mock
    KeyVaultOperation keyVaultOperation;

    KeyVaultPropertySource keyVaultPropertySource;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);

        final String[] propertyNameList = new String[]{TEST_PROPERTY_NAME_1};

        when(keyVaultOperation.getProperty(anyString())).thenReturn(TEST_PROPERTY_NAME_1);
        when(keyVaultOperation.getPropertyNames()).thenReturn(propertyNameList);

        keyVaultPropertySource = new KeyVaultPropertySource(keyVaultOperation);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
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
