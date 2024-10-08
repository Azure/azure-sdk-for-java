// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;

import com.azure.spring.cloud.feature.management.TestConfiguration;
import com.azure.spring.cloud.feature.management.VariantProperties;
import com.azure.spring.cloud.feature.management.implementation.models.Allocation;
import com.azure.spring.cloud.feature.management.implementation.models.GroupAllocation;
import com.azure.spring.cloud.feature.management.implementation.models.UserAllocation;
import com.azure.spring.cloud.feature.management.implementation.models.VariantReference;
import com.azure.spring.cloud.feature.management.models.FeatureManagementException;
import com.azure.spring.cloud.feature.management.models.Variant;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;
import com.azure.spring.cloud.feature.management.targeting.TargetingFilterContext;
import com.azure.spring.cloud.feature.management.testobjects.DiscountBanner;

public class VariantAssignmentTest {

    private VariantAssignment variantAssignment;

    @Mock
    private ApplicationContext context;

    @Mock
    ObjectProvider<VariantProperties> objectProviderMock;

    @Mock
    VariantProperties variantPropertiesMock;

    private TargetingEvaluationOptions evaluationOptions;

    private TargetingFilterContext targetingContext;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        targetingContext = new TargetingFilterContext();
        targetingContext.setUserId("test-user-id");
        List<String> groups = new ArrayList<String>();
        groups.add("test-group-id1");
        groups.add("test-group-id2");
        targetingContext.setGroups(groups);

        evaluationOptions = new TargetingEvaluationOptions();
        evaluationOptions.setIgnoreCase(true);
        variantAssignment = new VariantAssignment(evaluationOptions, objectProviderMock);
    }

    @Test
    public void noAllocation() {
        assertNull(variantAssignment.assignVariant(new Allocation(), targetingContext));
    }

    @Test
    public void userAllocation() {
        Allocation allocation = new Allocation();
        Map<String, UserAllocation> usersAllocations = new HashMap<>();
        UserAllocation userAllocation = new UserAllocation();
        Map<String, String> users = new HashMap<>();
        users.put("0", "Jane");
        userAllocation.setVariant("small");
        userAllocation.setUsers(users);
        usersAllocations.put("0", userAllocation);
        allocation.setUsers(usersAllocations);

        assertNull(variantAssignment.assignVariant(allocation, targetingContext));

        users.put("1", "test-user-id");
        userAllocation.setUsers(users);
        usersAllocations.put("0", userAllocation);
        allocation.setUsers(usersAllocations);

        String assignedVariant = variantAssignment.assignVariant(allocation, targetingContext);
        assertEquals("small", assignedVariant);
        assertNull(variantAssignment.getVariant(null, assignedVariant).block());

        List<VariantReference> variantReferences = new ArrayList<>();
        VariantReference small = new VariantReference();
        small.setName("small");
        small.setConfigurationValue("1");
        variantReferences.add(small);

        assertNotNull(variantAssignment.getVariant(variantReferences, assignedVariant).block());
    }

    @Test
    public void groupAllocation() {
        Allocation allocation = new Allocation();
        Map<String, GroupAllocation> groupAllocations = new HashMap<>();
        GroupAllocation groupAllocation = new GroupAllocation();
        Map<String, String> groups = new HashMap<>();
        groups.put("0", "prod");
        groupAllocation.setVariant("small");
        groupAllocation.setGroups(groups);
        groupAllocations.put("0", groupAllocation);
        allocation.setGroups(groupAllocations);

        assertNull(variantAssignment.assignVariant(allocation, targetingContext));

        groups.put("1", "test-group-id2");
        groupAllocation.setGroups(groups);
        groupAllocations.put("0", groupAllocation);
        allocation.setGroups(groupAllocations);

        String assignedVariant = variantAssignment.assignVariant(allocation, targetingContext);
        assertEquals("small", assignedVariant);
        assertNull(variantAssignment.getVariant(null, assignedVariant).block());

        List<VariantReference> variantReferences = new ArrayList<>();
        VariantReference small = new VariantReference();
        small.setName("small");
        small.setConfigurationValue("1");
        variantReferences.add(small);

        assertNotNull(variantAssignment.getVariant(variantReferences, assignedVariant).block());
    }

    @Test
    public void getVariantTest() {
        Map<Integer, VariantReference> variants = new LinkedHashMap<Integer, VariantReference>();
        VariantReference v1 = new VariantReference();
        v1.setName("Small");
        v1.setConfigurationReference("Banner.Small");
        VariantReference v2 = new VariantReference();
        v2.setName("Big");
        v2.setConfigurationReference("Banner.Big");
        variants.put(1, v1);
        variants.put(2, v2);

        List<VariantProperties> properties = new ArrayList<>();
        TestConfiguration testConfiguration = new TestConfiguration();

        Map<String, DiscountBanner> testBanners = new HashMap<>();
        testBanners.put("Small", new DiscountBanner().setColor("Azure").setSize(1));
        testBanners.put("Big", new DiscountBanner().setColor("Orange").setSize(9));

        testConfiguration.setBanner(testBanners);

        properties.add(testConfiguration);

        when(objectProviderMock.stream()).thenReturn(properties.stream());

        Variant assignedVariant = variantAssignment.getVariant(variants.values(), "Small").block();
        assertEquals(assignedVariant.getName(), "Small");
        assertEquals(1, ((DiscountBanner) assignedVariant.getValue()).getSize());
        assertEquals("Azure", ((DiscountBanner) assignedVariant.getValue()).getColor());
    }

    @Test
    public void getVariantTestNoProperties() {
        Map<Integer, VariantReference> variants = new LinkedHashMap<Integer, VariantReference>();
        VariantReference v1 = new VariantReference();
        v1.setName("Small");
        v1.setConfigurationReference("Banner.Small");
        VariantReference v2 = new VariantReference();
        v2.setName("Big");
        v2.setConfigurationReference("Banner.Big");
        variants.put(1, v1);
        variants.put(2, v2);

        List<VariantProperties> properties = new ArrayList<>();

        when(objectProviderMock.stream()).thenReturn(properties.stream());

        Exception exception = assertThrows(FeatureManagementException.class,
            () -> variantAssignment.getVariant(variants.values(), "Small").block());
        assertEquals(
            "Failed to load getBanner. No ConfigurationProperties where found containing it.. Make sure it exists and is publicly accessible.",
            exception.getMessage());
    }
}
