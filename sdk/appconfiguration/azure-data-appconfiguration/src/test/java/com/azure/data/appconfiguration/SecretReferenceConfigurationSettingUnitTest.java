// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SecretReferenceConfigurationSettingUnitTest {
    // Original value
    static final String NEW_KEY = "newKey";
    static final String SECRET_ID_VALUE = "https://www.google.com/";
    // Updated value
    static final String UPDATED_SECRET_ID_VALUE = "https://www.google.com/updated";
    @Test
    public void accessingStronglyTypedPropertiesAfterSettingDifferentSecretReferenceJSON() {
        // Create a new configuration setting,
        SecretReferenceConfigurationSetting setting = getSecretReferenceConfigurationSetting(NEW_KEY, SECRET_ID_VALUE);
        String expectedNewSettingValue = getSecretReferenceConfigurationSettingValue(SECRET_ID_VALUE);
        assertEquals(expectedNewSettingValue, setting.getValue());

        String expectedUpdatedSettingValue = getSecretReferenceConfigurationSettingValue(UPDATED_SECRET_ID_VALUE);
        // Set the Value to some pre-populated
        setting.setValue(expectedUpdatedSettingValue);
        // Access strongly-typed property values
        assertEquals(expectedUpdatedSettingValue, setting.getValue());
        assertEquals(UPDATED_SECRET_ID_VALUE, setting.getSecretId());
    }

    @Test
    public void accessingValueAfterChangingStronglyTypedProperties() {
        // Create a new feature flag configuration setting,
        SecretReferenceConfigurationSetting setting = getSecretReferenceConfigurationSetting(NEW_KEY, SECRET_ID_VALUE);
        String expectedNewSettingValue = getSecretReferenceConfigurationSettingValue(SECRET_ID_VALUE);
        assertEquals(expectedNewSettingValue, setting.getValue());
        // Change  strongly-type properties.
        setting.setSecretId(UPDATED_SECRET_ID_VALUE);
        String expectedUpdatedSettingValue = getSecretReferenceConfigurationSettingValue(UPDATED_SECRET_ID_VALUE);
        // make sure the value reflect to the changes
        assertEquals(expectedUpdatedSettingValue, setting.getValue());
    }

    @Test
    public void throwExceptionWhenInvalidNonJsonSecretReferenceValue() {
        // Create a new feature flag configuration setting,
        SecretReferenceConfigurationSetting setting = getSecretReferenceConfigurationSetting(NEW_KEY, SECRET_ID_VALUE);
        // Throws IllegalStateException when setting value to non-JSON
        assertThrows(IllegalStateException.class, () -> setting.setValue("Hello World"));
    }

    String getSecretReferenceConfigurationSettingValue(String secretId) {
        return String.format("{\"uri\":\"%s\"}", secretId);
    }

    private SecretReferenceConfigurationSetting getSecretReferenceConfigurationSetting(String key, String secretId) {
        return new SecretReferenceConfigurationSetting(key, secretId);
    }
}
