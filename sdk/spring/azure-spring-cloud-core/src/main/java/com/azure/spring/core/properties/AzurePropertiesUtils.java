// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class AzurePropertiesUtils {

    /**
     *
     * @param source The source AzureProperties object.
     * @param target The target AzureProperties object.
     * @param <T> The type of AzureProperties.
     */
    public static <T extends AzureProperties> void copyAzureCommonProperties(AzureProperties source, T target) {
        // call explicitly for these fields could be defined as final
        BeanUtils.copyProperties(source.getClient(), target.getClient());
        BeanUtils.copyProperties(source.getProxy(), target.getProxy());
        BeanUtils.copyProperties(source.getRetry(), target.getRetry());
        BeanUtils.copyProperties(source.getRetry().getBackoff(), target.getRetry().getBackoff());
        BeanUtils.copyProperties(source.getProfile(), target.getProfile());
        BeanUtils.copyProperties(source.getCredential(), target.getCredential());
    }

    // TODO (xiada): add tests for this
    public static <T extends AzureProperties> void copyAzureCommonPropertiesIgnoreNull(AzureProperties source, T target) {
        copyPropertiesIgnoreNull(source.getClient(), target.getClient());
        copyPropertiesIgnoreNull(source.getProxy(), target.getProxy());
        copyPropertiesIgnoreNull(source.getRetry(), target.getRetry());
        copyPropertiesIgnoreNull(source.getRetry().getBackoff(), target.getRetry().getBackoff());
        copyPropertiesIgnoreNull(source.getProfile(), target.getProfile());
        copyPropertiesIgnoreNull(source.getCredential(), target.getCredential());
    }

    private static void copyPropertiesIgnoreNull(Object source, Object target) {
        BeanUtils.copyProperties(source, target, findNullPropertyNames(source));
    }

    private static String[] findNullPropertyNames(Object source) {
        final Set<String> emptyNames = new HashSet<>();

        final BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = beanWrapper.getPropertyDescriptors();

        for (PropertyDescriptor pd : pds) {
            Object srcValue = beanWrapper.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }


}
