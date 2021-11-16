// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.util;

import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.aware.ClientAware;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

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

        if (source.getClient() instanceof ClientAware.HttpClient
            && target.getClient() instanceof ClientAware.HttpClient) {
            ClientAware.HttpClient sourceClient = (ClientAware.HttpClient) source.getClient();
            ClientAware.HttpClient targetClient = (ClientAware.HttpClient) target.getClient();
            BeanUtils.copyProperties(sourceClient.getLogging(), targetClient.getLogging());
            targetClient.getLogging().getAllowedHeaderNames().addAll(sourceClient.getLogging().getAllowedHeaderNames());
            targetClient.getLogging().getAllowedQueryParamNames().addAll(sourceClient.getLogging().getAllowedQueryParamNames());
        }

        BeanUtils.copyProperties(source.getProxy(), target.getProxy());
        BeanUtils.copyProperties(source.getRetry(), target.getRetry());
        BeanUtils.copyProperties(source.getRetry().getBackoff(), target.getRetry().getBackoff());
        BeanUtils.copyProperties(source.getProfile(), target.getProfile());
        BeanUtils.copyProperties(source.getProfile().getEnvironment(), target.getProfile().getEnvironment());
        BeanUtils.copyProperties(source.getCredential(), target.getCredential());
    }

    // TODO (xiada): add tests for this
    public static <T extends AzureProperties> void copyAzureCommonPropertiesIgnoreNull(AzureProperties source, T target) {
        copyPropertiesIgnoreNull(source.getClient(), target.getClient());

        if (source.getClient() instanceof ClientAware.HttpClient
            && target.getClient() instanceof ClientAware.HttpClient) {
            ClientAware.HttpClient sourceClient = (ClientAware.HttpClient) source.getClient();
            ClientAware.HttpClient targetClient = (ClientAware.HttpClient) target.getClient();
            copyPropertiesIgnoreNull(sourceClient.getLogging(), targetClient.getLogging());
            targetClient.getLogging().getAllowedHeaderNames().addAll(sourceClient.getLogging().getAllowedHeaderNames());
            targetClient.getLogging().getAllowedQueryParamNames().addAll(sourceClient.getLogging().getAllowedQueryParamNames());
        }

        copyPropertiesIgnoreNull(source.getProxy(), target.getProxy());
        copyPropertiesIgnoreNull(source.getRetry(), target.getRetry());
        copyPropertiesIgnoreNull(source.getRetry().getBackoff(), target.getRetry().getBackoff());
        copyPropertiesIgnoreNull(source.getProfile(), target.getProfile());
        BeanUtils.copyProperties(source.getProfile().getEnvironment(), target.getProfile().getEnvironment());
        copyPropertiesIgnoreNull(source.getCredential(), target.getCredential());
    }

    public static <T extends AzureProperties> void mergeAzureCommonProperties(AzureProperties defaultProperties,
                                                                              AzureProperties properties,
                                                                              T target) {
        copyAzureCommonProperties(defaultProperties, target);
        copyAzureCommonPropertiesIgnoreNull(properties, target);
    }

    public static <T> void copyPropertiesWhenTargetIsNull(T source, T target) {
        BeanUtils.copyProperties(source, target, findNonNullPropertyNames(target));
    }

    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        BeanUtils.copyProperties(source, target, findNullPropertyNames(source));
    }

    private static String[] findPropertyNames(Object source, Predicate<Object> predicate) {
        final Set<String> emptyNames = new HashSet<>();

        final BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = beanWrapper.getPropertyDescriptors();

        for (PropertyDescriptor pd : pds) {
            Object srcValue = beanWrapper.getPropertyValue(pd.getName());
            if (predicate.test(srcValue)) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }

    private static String[] findNonNullPropertyNames(Object source) {
        return findPropertyNames(source, Objects::nonNull);
    }

    private static String[] findNullPropertyNames(Object source) {
        return findPropertyNames(source, Objects::isNull);
    }


}
