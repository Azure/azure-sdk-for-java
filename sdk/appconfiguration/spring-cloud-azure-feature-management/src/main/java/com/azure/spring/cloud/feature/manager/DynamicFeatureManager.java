// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.manager.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.manager.implementation.models.DynamicFeature;
import com.azure.spring.cloud.feature.manager.models.FeatureDefinition;
import com.azure.spring.cloud.feature.manager.models.FeatureVariant;
import com.azure.spring.cloud.feature.manager.models.IFeatureVariantAssigner;

import reactor.core.publisher.Mono;

/**
 * Holds information on Feature Management properties and can check if a given feature is enabled.
 */
public class DynamicFeatureManager {

    private transient ApplicationContext context;

    /**
     * ConfigurationProperties for accessing the different types of feature variants.
     */
    private final ObjectProvider<IDynamicFeatureProperties> propertiesProvider;

    private final FeatureManagementProperties featureManagementConfigurations;

    /**
     * Creates Dynamic Feature Manager
     * 
     * @param context ApplicationContext
     * @param propertiesProvider Object Provider for accessing client IDynamicFeatureProperties
     * @param featureManagementConfigurations Configuration Properties for Feature Flags
     */
    public DynamicFeatureManager(ApplicationContext context,
        ObjectProvider<IDynamicFeatureProperties> propertiesProvider,
        FeatureManagementProperties featureManagementConfigurations) {
        this.context = context;
        this.propertiesProvider = propertiesProvider;
        this.featureManagementConfigurations = featureManagementConfigurations;
    }

    /**
     * Returns a feature variant of the type given.
     *
     * @param <T> Type of the feature that will be returned.
     * @param variantName name of the feature being checked.
     * @param returnClass Type of the feature being checked.
     * @return variant of the provided type
     * @throws FeatureManagementException A generic exception for when something goes wrong with the variant generation
     * process.
     */
    public <T> Mono<T> getVariantAsync(String variantName, Class<T> returnClass)
        throws FeatureManagementException {
        return generateVariant(variantName, returnClass);
    }

    @SuppressWarnings("unchecked")
    private <T> Mono<T> generateVariant(String featureName, Class<T> type) throws FeatureManagementException {

        if (!StringUtils.hasText(featureName)) {
            throw new IllegalArgumentException("Feature Variant name can not be empty or null.");
        }

        if (!featureManagementConfigurations.getDynamicFeatures().containsKey(featureName)) {
            throw new FeatureManagementException("The Dynamic Feature " + featureName + " can not be found.");
        }

        DynamicFeature dynamicFeature = featureManagementConfigurations.getDynamicFeatures().get(featureName);

        FeatureDefinition featureDefinition = new FeatureDefinition(featureName, dynamicFeature);

        validateDynamicFeature(featureDefinition, featureName);

        try {
            IFeatureVariantAssigner assigner = (IFeatureVariantAssigner) context
                .getBean(featureDefinition.getAssigner());
            return (Mono<T>) assigner.assignVariantAsync(featureDefinition).map(this::assignVariant);
        } catch (NoSuchBeanDefinitionException e) {
            throw new FeatureManagementException("The feature variant assigner " + featureDefinition.getAssigner()
                + " specified for feature " + featureName + " was not found.");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T assignVariant(FeatureVariant variant) {
        String reference = variant.getConfigurationReference();

        String[] parts = reference.split("\\.");

        String methodName = "get" + parts[0];
        Method method = null;
        Map<String, T> variantMap = null;

        Optional<IDynamicFeatureProperties> variantProperties = propertiesProvider.stream().filter(properties -> {
            try {
                properties.getClass().getMethod(methodName);
                return true;
            } catch (NoSuchMethodException | SecurityException e) {
                return false;
            }
        }).findFirst();

        if (!variantProperties.isPresent()) {
            String message = "Failed to load " + methodName + ". No ConfigurationProperties where found containing it."
                + ". Make sure it exists and is publicly accessible.";
            throw new DynamicFeatureException(message);
        }

        // Dynamically Accesses @ConfigurationProperties and finds the matching method.
        try {
            method = variantProperties.get().getClass().getMethod(methodName);
        } catch (NoSuchMethodException | SecurityException e) {
            String message = "Failed to load " + methodName + " in " + variantProperties.getClass()
                + ". Make sure it exists and is publicly accessible.";
            throw new DynamicFeatureException(message, e);
        }
        // Calls method to get back an Object, this object contains multiple variants
        // each has a get method.
        try {
            variantMap = (Map<String, T>) method.invoke(variantProperties.get());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            String message = "Failed invoking " + methodName + " in " + variantProperties.getClass()
                + ". Make sure it exists and is publicly accessible.";
            throw new DynamicFeatureException(message, e);
        }

        return variantMap.get(parts[1]);
    }

    private void validateDynamicFeature(FeatureDefinition featureDefinition, String featureName)
        throws FeatureManagementException {
        if (!StringUtils.hasText(featureDefinition.getAssigner())) {
            throw new FeatureManagementException(
                "Missing Feature Variant assigner name for the feature " + featureName);
        }

        if (featureDefinition.getVariants() == null || featureDefinition.getVariants().size() == 0) {
            throw new FeatureManagementException("No Variants are registered for the feature " + featureName);
        }

        FeatureVariant defaultVariant = null;

        for (FeatureVariant v : featureDefinition.getVariants()) {
            if (v.getDefault()) {
                if (defaultVariant != null) {
                    throw new FeatureManagementException(
                        "Multiple default variants are registered for the feature " + featureName);
                }
                defaultVariant = v;
            }

            if (!StringUtils.hasText(v.getConfigurationReference())) {
                throw new FeatureManagementException("The variant " + v.getName() + " for the feature " + featureName
                    + " does not have a configuration reference.");
            }
        }

        if (defaultVariant == null) {
            throw new FeatureManagementException("A default variant cannot be found for the feature " + featureName);
        }
    }
}
