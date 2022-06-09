// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.util;

import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.ClientOptionsProvider;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
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
public final class AzurePropertiesUtils {

    private AzurePropertiesUtils() {

    }

    /**
     * Copy common properties from source {@link AzureProperties} object to target {@link T} object.
     * @param source The source {@link AzureProperties} object.
     * @param target The target object.
     * @param <T> The type of the target that extends AzureProperties.
     */
    public static <T extends AzureProperties> void copyAzureCommonProperties(AzureProperties source, T target) {
        // call explicitly for these fields could be defined as final
        BeanUtils.copyProperties(source.getClient(), target.getClient());
        copyHttpClientProperties(source, target, false);

        BeanUtils.copyProperties(source.getProxy(), target.getProxy());
        BeanUtils.copyProperties(source.getProfile(), target.getProfile());
        BeanUtils.copyProperties(source.getProfile().getEnvironment(), target.getProfile().getEnvironment());
        BeanUtils.copyProperties(source.getCredential(), target.getCredential());

        if (source instanceof RetryOptionsProvider && target instanceof RetryOptionsProvider) {
            RetryOptionsProvider.RetryOptions sourceRetry = ((RetryOptionsProvider) source).getRetry();
            RetryOptionsProvider.RetryOptions targetRetry = ((RetryOptionsProvider) target).getRetry();
            BeanUtils.copyProperties(sourceRetry, targetRetry);
            BeanUtils.copyProperties(sourceRetry.getExponential(), targetRetry.getExponential());
            BeanUtils.copyProperties(sourceRetry.getFixed(), targetRetry.getFixed());
        }
    }

    // TODO (xiada): add tests for this
    /**
     * Copy common properties from source {@link AzureProperties} object to target {@link T} object. Ignore the source
     * value if it is null.
     *
     * @param source The source {@link AzureProperties} object.
     * @param target The target object.
     * @param <T> The type of the target that extends AzureProperties.
     */
    public static <T extends AzureProperties> void copyAzureCommonPropertiesIgnoreNull(AzureProperties source, T target) {
        copyPropertiesIgnoreNull(source.getClient(), target.getClient());
        copyHttpClientProperties(source, target, true);

        copyPropertiesIgnoreNull(source.getProxy(), target.getProxy());
        copyPropertiesIgnoreNull(source.getProfile(), target.getProfile());
        BeanUtils.copyProperties(source.getProfile().getEnvironment(), target.getProfile().getEnvironment());
        copyPropertiesIgnoreNull(source.getCredential(), target.getCredential());

        if (source instanceof RetryOptionsProvider && target instanceof RetryOptionsProvider) {
            RetryOptionsProvider.RetryOptions sourceRetry = ((RetryOptionsProvider) source).getRetry();
            RetryOptionsProvider.RetryOptions targetRetry = ((RetryOptionsProvider) target).getRetry();
            copyPropertiesIgnoreNull(sourceRetry, targetRetry);
            copyPropertiesIgnoreNull(sourceRetry.getExponential(), targetRetry.getExponential());
            copyPropertiesIgnoreNull(sourceRetry.getFixed(), targetRetry.getFixed());
        }
    }

    /**
     * Merge properties from two {@link AzureProperties} objects. If a same property appears in both two objects, the
     * value from the latter will take precedence.
     * @param defaultProperties The default properties, the merge result will take value from this property as default.
     * @param properties The overridden properties, the merge result will take value from this if a same property
     *                   appears in two property objects.
     * @param target The merge result.
     * @param <T> The type of the merge result.
     */
    public static <T extends AzureProperties> void mergeAzureCommonProperties(AzureProperties defaultProperties,
                                                                              AzureProperties properties,
                                                                              T target) {
        copyAzureCommonProperties(defaultProperties, target);
        copyAzureCommonPropertiesIgnoreNull(properties, target);
    }

    /**
     * Copy common properties from source object to target object. Ignore the source value if it is null.
     *
     * @param source The source object.
     * @param target The target object.
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        BeanUtils.copyProperties(source, target, findNullPropertyNames(source));
    }

    private static <T extends AzureProperties> void copyHttpClientProperties(AzureProperties source,
                                                                             T target,
                                                                             boolean ignoreNull) {
        if (source.getClient() instanceof ClientOptionsProvider.HttpClientOptions
            && target.getClient() instanceof ClientOptionsProvider.HttpClientOptions) {

            ClientOptionsProvider.HttpClientOptions sourceClient = (ClientOptionsProvider.HttpClientOptions) source.getClient();
            ClientOptionsProvider.HttpClientOptions targetClient = (ClientOptionsProvider.HttpClientOptions) target.getClient();
            if (ignoreNull) {
                copyPropertiesIgnoreNull(sourceClient.getLogging(), targetClient.getLogging());
            } else {
                BeanUtils.copyProperties(sourceClient.getLogging(), targetClient.getLogging());
            }
            targetClient.getLogging().getAllowedHeaderNames().addAll(sourceClient.getLogging().getAllowedHeaderNames());
            targetClient.getLogging().getAllowedQueryParamNames().addAll(sourceClient.getLogging().getAllowedQueryParamNames());
            targetClient.getHeaders().addAll(sourceClient.getHeaders());
        }
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

    private static String[] findNullPropertyNames(Object source) {
        return findPropertyNames(source, Objects::isNull);
    }


}
