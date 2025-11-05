// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.util;

import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;

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

        // Only copy scopes if explicitly set in source, not if using computed default
        // This ensures that when cloud-type is set globally but scopes are not explicitly set,
        // the target will use its own default scopes based on the correct cloud type
        if (isScopesExplicitlySet(source)) {
            target.setScopes(source.getScopes());
        }
        target.setPasswordlessEnabled(source.isPasswordlessEnabled());
    }

    /**
     * Check if scopes are explicitly set in the source object (not using computed default).
     * Uses reflection to access the scopes field directly since getScopes() may return computed defaults.
     *
     * @param source The source PasswordlessProperties object
     * @return true if scopes are explicitly set, false otherwise
     */
    private static boolean isScopesExplicitlySet(PasswordlessProperties source) {
        try {
            Field scopesField = source.getClass().getDeclaredField("scopes");
            scopesField.setAccessible(true);
            Object scopesValue = scopesField.get(source);
            return scopesValue != null;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // If reflection fails, fall back to always copying (maintains backward compatibility)
            return true;
        }
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
