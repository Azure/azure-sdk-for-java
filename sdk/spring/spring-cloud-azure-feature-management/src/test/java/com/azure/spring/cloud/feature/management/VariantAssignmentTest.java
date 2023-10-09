package com.azure.spring.cloud.feature.management;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import com.azure.spring.cloud.feature.management.implementation.models.Allocation;
import com.azure.spring.cloud.feature.management.implementation.models.GroupAllocation;
import com.azure.spring.cloud.feature.management.implementation.models.UserAllocation;
import com.azure.spring.cloud.feature.management.implementation.models.VariantReference;
import com.azure.spring.cloud.feature.management.targeting.TargetingContext;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;

import reactor.core.publisher.Mono;

public class VariantAssignmentTest {

    private VariantAssignment variantAssignment;

    @Mock
    private ApplicationContext context;

    private TargetingContextAccessor contextAccessor;

    private TargetingEvaluationOptions evaluationOptions;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        contextAccessor = new TargetingContextAccessor() {

            @Override
            public void configureTargetingContext(TargetingContext context) {
                context.setUserId("test-user-id");
                context.setGroups(List.of("test-group-id1", "test-group-id2"));
            }
        };
        evaluationOptions = new TargetingEvaluationOptions();
        evaluationOptions.setIgnoreCase(true);
        variantAssignment = new VariantAssignment(contextAccessor, evaluationOptions);
    }

    @Test
    public void noAllocation() {
        Allocation allocation = new Allocation();

        Mono<Variant> variant = variantAssignment.assignVariant(allocation, null);
        assertNull(variant.block());
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

        Mono<Variant> variant = variantAssignment.assignVariant(allocation, null);
        assertNull(variant.block());

        users.put("1", "test-user-id");
        userAllocation.setUsers(users);
        usersAllocations.put("0", userAllocation);
        allocation.setUsers(usersAllocations);
        variant = variantAssignment.assignVariant(allocation, null);
        assertNull(variant.block());

        List<VariantReference> variantReferences = new ArrayList<>();
        VariantReference small = new VariantReference();
        small.setName("small");
        small.setConfigurationValue("1");
        variantReferences.add(small);

        variant = variantAssignment.assignVariant(allocation, variantReferences);
        assertNotNull(variant.block());
    }
    
    @Test
    public void groupAllocation() {
        Allocation allocation = new Allocation();
        Map<String,GroupAllocation> groupAllocations = new HashMap<>();
        GroupAllocation groupAllocation = new GroupAllocation();
        Map<String, String> groups = new HashMap<>();
        groups.put("0", "prod");
        groupAllocation.setVariant("small");
        groupAllocation.setGroups(groups);
        groupAllocations.put("0", groupAllocation);
        allocation.setGroups(groupAllocations);

        Mono<Variant> variant = variantAssignment.assignVariant(allocation, null);
        assertNull(variant.block());

        groups.put("1", "test-group-id2");
        groupAllocation.setGroups(groups);
        groupAllocations.put("0", groupAllocation);
        allocation.setGroups(groupAllocations);
        variant = variantAssignment.assignVariant(allocation, null);
        assertNull(variant.block());

        List<VariantReference> variantReferences = new ArrayList<>();
        VariantReference small = new VariantReference();
        small.setName("small");
        small.setConfigurationValue("1");
        variantReferences.add(small);

        variant = variantAssignment.assignVariant(allocation, variantReferences);
        assertNotNull(variant.block());
    }

}
