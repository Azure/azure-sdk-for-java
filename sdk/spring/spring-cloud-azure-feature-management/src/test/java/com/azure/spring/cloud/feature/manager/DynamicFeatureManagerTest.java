// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.azure.spring.cloud.feature.manager.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.manager.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.manager.implementation.models.DynamicFeature;
import com.azure.spring.cloud.feature.manager.models.FeatureDefinition;
import com.azure.spring.cloud.feature.manager.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.manager.models.FeatureVariant;
import com.azure.spring.cloud.feature.manager.models.IFeatureFilter;
import com.azure.spring.cloud.feature.manager.models.IFeatureVariantAssigner;
import com.azure.spring.cloud.feature.manager.testobjects.DiscountBanner;
import com.azure.spring.cloud.feature.manager.testobjects.MockableProperties;

import reactor.core.publisher.Mono;

/**
 * Unit tests for FeatureManager.
 */
@SpringBootTest(classes = { TestConfiguration.class, SpringBootTest.class })
public class DynamicFeatureManagerTest {

    private static final String USERS = "users";

    private static final String GROUPS = "groups";

    private static final String DEFAULT_ROLLOUT_PERCENTAGE = "defaultRolloutPercentage";

    private static final LinkedHashMap<String, Object> EMPTY_MAP = new LinkedHashMap<>();

    private DynamicFeatureManager featureManager;

    @Mock
    private ApplicationContext context;

    @Mock
    private FeatureManagementConfigProperties properties;

    @Mock
    private FeatureManagementProperties featureManagementPropertiesMock;

    @Mock
    private MockFilter filterMock;

    @Mock
    private MockableProperties variantProperties;

    @Mock
    private ObjectProvider<IDynamicFeatureProperties> propertiesProviderMock;

    @Mock
    private Stream<IDynamicFeatureProperties> streamMock;

    @Mock
    private Stream<IDynamicFeatureProperties> filterStreamMock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(properties.isFailFast()).thenReturn(true);

        DiscountBanner discountBannerBig = new DiscountBanner();
        discountBannerBig.setColor("#DDD");
        discountBannerBig.setSize(400);
        DiscountBanner discountBannerSmall = new DiscountBanner();
        discountBannerSmall.setColor("#999");
        discountBannerSmall.setSize(150);

        Map<String, DiscountBanner> discountBannerMap = new HashMap<>();

        discountBannerMap.put("Big", discountBannerBig);
        discountBannerMap.put("Small", discountBannerSmall);

        when(variantProperties.getDiscountBanner()).thenReturn(discountBannerMap);
        when(propertiesProviderMock.stream()).thenReturn(streamMock);
        when(streamMock.filter(Mockito.any())).thenReturn(filterStreamMock);

        featureManager = new DynamicFeatureManager(context, propertiesProviderMock, featureManagementPropertiesMock);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void getVariantAsyncDefaultBasic() {
        when(context.getBean(Mockito.matches("Test.Assigner"))).thenReturn(filterMock);

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("Test.Assigner");

        Map<String, FeatureVariant> variants = new LinkedHashMap<>();

        variants.put("0", createFeatureVariant("DiscountBanner.Big", EMPTY_MAP, EMPTY_MAP, 100));
        variants.get("0").setDefault(true);
        dynamicFeature.setVariants(variants);

        when(filterMock.assignVariantAsync(Mockito.any())).thenReturn(Mono.just(variants.get("0")));

        Map<String, DynamicFeature> params = new LinkedHashMap<>();
        params.put("DiscountBanner", dynamicFeature);

        Optional<IDynamicFeatureProperties> optionalProp = Optional.of(variantProperties);

        when(featureManagementPropertiesMock.getDynamicFeatures()).thenReturn(params);
        when(filterStreamMock.findFirst()).thenReturn(optionalProp);

        DiscountBanner testObject = featureManager.getVariantAsync("DiscountBanner", DiscountBanner.class)
            .block();

        assertNotNull(testObject);
        assertEquals(400, testObject.getSize());
    }

    @Test
    public void getVariantNoDefault() {
        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("Test.Assigner");

        Map<String, FeatureVariant> variants = new LinkedHashMap<>();

        variants.put("0", createFeatureVariant("DiscountBanner.Big", EMPTY_MAP, EMPTY_MAP, 100));

        dynamicFeature.setVariants(variants);

        Map<String, DynamicFeature> params = new LinkedHashMap<>();
        params.put("DiscountBanner", dynamicFeature);

        Optional<IDynamicFeatureProperties> optionalProp = Optional.of(variantProperties);

        when(featureManagementPropertiesMock.getDynamicFeatures()).thenReturn(params);
        when(filterStreamMock.findFirst()).thenReturn(optionalProp);

        FeatureManagementException e = assertThrows(FeatureManagementException.class,
            () -> featureManager.getVariantAsync("DiscountBanner", Object.class).block());
        assertEquals("A default variant cannot be found for the feature DiscountBanner", e.getMessage());
    }

    @Test
    public void getVariantAsyncNonDefault() throws FilterNotFoundException, FeatureManagementException {
        FeatureVariant variant = createFeatureVariant("DiscountBanner.Small", EMPTY_MAP, EMPTY_MAP, 100);

        when(context.getBean(Mockito.matches("Test.Assigner"))).thenReturn(filterMock);
        when(filterMock.assignVariantAsync(Mockito.any())).thenReturn(Mono.just(variant));

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("Test.Assigner");

        Map<String, FeatureVariant> variants = new LinkedHashMap<>();

        variants.put("0", createFeatureVariant("DiscountBanner.Big", EMPTY_MAP, EMPTY_MAP, 0));
        variants.get("0").setDefault(true);
        variants.put("1", variant);
        dynamicFeature.setVariants(variants);

        Map<String, DynamicFeature> params = new LinkedHashMap<>();
        params.put("DiscountBanner", dynamicFeature);

        Optional<IDynamicFeatureProperties> optionalProp = Optional.of(variantProperties);

        when(featureManagementPropertiesMock.getDynamicFeatures()).thenReturn(params);
        when(filterStreamMock.findFirst()).thenReturn(optionalProp);

        DiscountBanner testObject = featureManager.getVariantAsync("DiscountBanner", DiscountBanner.class)
            .block();
        assertNotNull(testObject);
        assertEquals(150, testObject.getSize());
    }

    @Test
    public void featureWithoutAName() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> featureManager.getVariantAsync("", DiscountBanner.class).block());
        assertThat(e).hasMessage("Feature Variant name can not be empty or null.");
    }

    @Test
    public void featureAssignerNotFound() {
        FeatureVariant variant = createFeatureVariant("DiscountBanner.Small", EMPTY_MAP, EMPTY_MAP, 100);

        when(context.getBean(Mockito.matches("FeatureAssignerThatDoesntExist")))
            .thenThrow(new NoSuchBeanDefinitionException(""));
        when(filterMock.assignVariantAsync(Mockito.any())).thenReturn(Mono.just(variant));

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("FeatureAssignerThatDoesntExist");

        Map<String, FeatureVariant> variants = new LinkedHashMap<>();

        variants.put("0", createFeatureVariant("DiscountBanner.Big", EMPTY_MAP, EMPTY_MAP, 0));
        variants.get("0").setDefault(true);
        variants.put("1", variant);
        dynamicFeature.setVariants(variants);

        Map<String, DynamicFeature> params = new LinkedHashMap<>();
        params.put("FeatureAssignerThatDoesntExist", dynamicFeature);

        Optional<IDynamicFeatureProperties> optionalProp = Optional.of(variantProperties);

        when(featureManagementPropertiesMock.getDynamicFeatures()).thenReturn(params);
        when(filterStreamMock.findFirst()).thenReturn(optionalProp);

        FeatureManagementException e = assertThrows(FeatureManagementException.class,
            () -> featureManager.getVariantAsync("FeatureAssignerThatDoesntExist", DiscountBanner.class).block());
        assertThat(e).hasMessage(
            "The feature variant assigner FeatureAssignerThatDoesntExist specified for feature FeatureAssignerThatDoesntExist was not found.");
    }

    class MockFilter implements IFeatureFilter, IFeatureVariantAssigner {

        @Override
        public Mono<FeatureVariant> assignVariantAsync(FeatureDefinition featureDefinition) {
            return null;
        }

        @Override
        public boolean evaluate(FeatureFilterEvaluationContext context) {
            return false;
        }

    }

    private FeatureVariant createFeatureVariant(String variantName, LinkedHashMap<String, Object> users,
        LinkedHashMap<String, Object> groups, int defautPercentage) {
        return createFeatureVariant(variantName, "", users, groups, defautPercentage);
    }

    private FeatureVariant createFeatureVariant(String variantName, String additionalRerence,
        LinkedHashMap<String, Object> users,
        LinkedHashMap<String, Object> groups, int defautPercentage) {
        FeatureVariant variant = new FeatureVariant();
        variant.setName(variantName);
        variant.setDefault(false);
        variant.setConfigurationReference(variantName + additionalRerence);

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        parameters.put(USERS, users);
        parameters.put(GROUPS, groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, defautPercentage);

        variant.setAssignmentParameters(parameters);

        return variant;
    }

}
