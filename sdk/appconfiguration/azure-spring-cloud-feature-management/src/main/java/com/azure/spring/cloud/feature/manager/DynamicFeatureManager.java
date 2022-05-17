// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.manager.entities.FeatureDefinition;
import com.azure.spring.cloud.feature.manager.entities.FeatureVariant;
import com.azure.spring.cloud.feature.manager.entities.IFeatureVariantAssigner;
import com.azure.spring.cloud.feature.manager.entities.IFeatureVariantAssignerMetadata;
import com.azure.spring.cloud.feature.manager.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.manager.implementation.entities.DynamicFeature;

import reactor.core.publisher.Mono;

/**
 * Holds information on Feature Management properties and can check if a given feature is enabled.
 */
@Component("DynamicFeatureManagement")
public class DynamicFeatureManager {

    @Autowired
    private transient ApplicationContext context;

    /**
     * ConfigurationProperties for accessing the different types of feature variants.
     */
    @Autowired
    private ObjectProvider<IDynamicFeatureProperties> propertiesProvider;

    @Autowired
    private FeatureManagementProperties featureManagementConfigurations;

    /**
     * Returns a feature variant of the type given.
     *
     * @param <T> Type of the feature that will be returned.
     * @param variantName name of the feature being checked.
     * @param returnClass Type of the feature being checked.
     * @return variant of the provided type
     * @throws FilterNotFoundException if a Filter with the given name isn't found
     */
    public <T> Mono<T> getVariantAsync(String variantName, Class<T> returnClass) throws FilterNotFoundException {
        return Mono.just(generateVariant(variantName, returnClass));
    }

    @SuppressWarnings("unchecked")
    private <T> T generateVariant(String featureName, Class<T> type) {

        if (!StringUtils.hasText(featureName)) {
            throw new IllegalArgumentException("Feature Variant name can not be empty or null.");
        }

        if (!featureManagementConfigurations.getDynamicFeatures().containsKey(featureName)) {
            throw new FeatureManagementException("The Dynamic Feature " + featureName + " can not be found.");
        }

        FeatureVariant variant = null;

        DynamicFeature dynamicFeature = featureManagementConfigurations.getDynamicFeatures().get(featureName);

        FeatureDefinition featureDefinition = new FeatureDefinition(featureName, dynamicFeature);

        FeatureVariant defaultVariant = validateDynamicFeature(featureDefinition, featureName);

        IFeatureVariantAssignerMetadata assigner = null;

        try {
            assigner = (IFeatureVariantAssignerMetadata) context.getBean(featureDefinition.getAssigner());
        } catch (NoSuchBeanDefinitionException e) {
            throw new FeatureManagementException("The feature variant assigner " + featureDefinition.getAssigner()
                + " specified for feature " + featureName + " was not found.");
        }

        if (assigner instanceof IFeatureVariantAssigner) {
            variant = ((IFeatureVariantAssigner) assigner).assignVariantAsync(featureDefinition).block();
        }

        if (variant == null) {
            variant = defaultVariant;
        }

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

    private FeatureVariant validateDynamicFeature(FeatureDefinition featureDefinition, String featureName) {
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
        return defaultVariant;
    }
}
