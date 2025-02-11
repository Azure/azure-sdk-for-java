// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.util;

import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import org.springframework.beans.BeanUtils;

import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.copyPropertiesIgnoreNull;

/**
 * Util class for AzurePasswordlessProperties.
 */
public final class AzurePasswordlessPropertiesUtils {

    private AzurePasswordlessPropertiesUtils() {

    }

    /**
     * Copy common properties from source {@link AzureProperties} object to target {@link T extends PasswordlessProperties} object.
     * If a field x.y.z exists in both source and target object, the source value will override the target value.
     *
     * @param source The source {@link AzureProperties} object.
     * @param target The target object.
     * @param <T> The type of the target that extends PasswordlessProperties.
     */
    public static <T extends PasswordlessProperties> void copyAzureCommonProperties(AzureProperties source, T target) {
        // call explicitly for these fields could be defined as final

        BeanUtils.copyProperties(source.getProfile(), target.getProfile());
        BeanUtils.copyProperties(source.getProfile().getEnvironment(), target.getProfile().getEnvironment());
        BeanUtils.copyProperties(source.getCredential(), target.getCredential());

    }

    /**
     * Copy common properties from source {@link PasswordlessProperties} object to target {@link T extends PasswordlessProperties} object.
     * Ignore the source value:
     *   1. if it is null.
     *   2. if it's a primitive type and value is the default value.
     *
     * @param source The source {@link PasswordlessProperties} object.
     * @param target The target object.
     * @param <T> The type of the target that extends PasswordlessProperties.
     */
    public static <T extends PasswordlessProperties> void copyAzureCommonPropertiesIgnoreNull(PasswordlessProperties source, T target) {

        copyPropertiesIgnoreNull(source.getProfile(), target.getProfile());
        copyPropertiesIgnoreNull(source.getProfile().getEnvironment(), target.getProfile().getEnvironment());
        copyPropertiesIgnoreNull(source.getCredential(), target.getCredential());

        target.setScopes(source.getScopes());
        target.setPasswordlessEnabled(source.isPasswordlessEnabled());
    }

    /**
     * Merge properties from a {@link AzureProperties} object and a {@link PasswordlessProperties} object. If a same property appears in both two objects, the
     * value from the latter will take precedence.
     * @param defaultProperties The default properties, the merge result will take value from this property as default.
     * @param properties The overridden properties, the merge result will take value from this if a same property
     *                   appears in two property objects.
     * @param target The merge result.
     * @param <T> The type of the merge result.
     */
    public static <T extends PasswordlessProperties> void mergeAzureCommonProperties(AzureProperties defaultProperties,
                                                                                     PasswordlessProperties properties,
                                                                                     T target) {
        copyAzureCommonProperties(defaultProperties, target);
        copyAzureCommonPropertiesIgnoreNull(properties, target);
    }
}
