package com.azure.spring.cloud.feature.manager.feature.evaluators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.azure.spring.cloud.feature.manager.TargetingException;
import com.azure.spring.cloud.feature.manager.implementation.targeting.GroupRollout;
import com.azure.spring.cloud.feature.manager.models.FeatureDefinition;
import com.azure.spring.cloud.feature.manager.models.FeatureVariant;
import com.azure.spring.cloud.feature.manager.targeting.ITargetingContextAccessor;
import com.azure.spring.cloud.feature.manager.targeting.TargetingContext;

import reactor.core.publisher.Mono;

public class TargetingEvaluatorTest {

	/**
	 * users field in the filter
	 */
	protected static final String USERS = "users";

	/**
	 * groups field in the filter
	 */
	protected static final String GROUPS = "groups";

	/**
	 * Audience in the filter
	 */
	protected static final String AUDIENCE = "Audience";

	@Mock
	private FeatureDefinition featureDefinitionMock;

	private TargetingEvaluator targetingEvaluator;

	private LinkedHashMap<String, Object> assignmentParameters;

	private LinkedHashMap<String, Object> assignedUsers;

	private List<String> users;

	private List<GroupRollout> groups;

	private List<FeatureVariant> variants;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		targetingEvaluator = new TargetingEvaluator(new TestTargetingContextAccessor());

		assignmentParameters = new LinkedHashMap<>();
		assignedUsers = new LinkedHashMap<>();
		users = new ArrayList<>();
		groups = new ArrayList<>();
		variants = new ArrayList<>();

		when(featureDefinitionMock.getVariants()).thenReturn(variants);

		when(featureDefinitionMock.getName()).thenReturn("TestFeatureDefinition");
	}

	@Test
	public void evalulateByUser() {
		users.add("Doe");

		assignedUsers.put(USERS, users);

		assignmentParameters.put(AUDIENCE, assignedUsers);

		variants.add(createFeatureVariant(true));

		FeatureVariant returnedVariant = targetingEvaluator.assignVariantAsync(featureDefinitionMock).block();
		assertNotNull(returnedVariant);
		assertEquals(variants.get(0), returnedVariant);

	}

	@Test
	public void evalulateByGroup() {
		groups = new ArrayList<>();

		GroupRollout gr = new GroupRollout();

		gr.setName("G1");
		gr.setRolloutPercentage(100);

		groups.add(gr);

		assignedUsers.put(GROUPS, groups);

		assignmentParameters.put(AUDIENCE, assignedUsers);

		variants.add(createFeatureVariant(true));

		FeatureVariant returnedVariant = targetingEvaluator.assignVariantAsync(featureDefinitionMock).block();
		assertNotNull(returnedVariant);
		assertEquals(variants.get(0), returnedVariant);

	}

	@Test
	public void evalulateByDefaultPercentage() {
		assignmentParameters.put("defaultRolloutPercentage", 100);

		variants.add(createFeatureVariant(true));

		FeatureVariant returnedVariant = targetingEvaluator.assignVariantAsync(featureDefinitionMock).block();
		assertNotNull(returnedVariant);
		assertEquals(variants.get(0), returnedVariant);
	}

	@Test
	public void evalulateByDefault() {
		variants.add(createFeatureVariant(true));

		FeatureVariant returnedVariant = targetingEvaluator.assignVariantAsync(featureDefinitionMock).block();
		assertNotNull(returnedVariant);
		assertEquals(variants.get(0), returnedVariant);
	}

	@Test
	public void noContextAccessor() {
		assertEquals(Mono.justOrEmpty(null), new TargetingEvaluator(null).assignVariantAsync(featureDefinitionMock));
	}

	@Test
	public void noContext() {
		assertEquals(Mono.justOrEmpty(null), new TargetingEvaluator(new InvlaidTargetingContextAccessor())
				.assignVariantAsync(featureDefinitionMock));
	}

	@Test
	public void groupOutOfRange() {
		groups = new ArrayList<>();

		GroupRollout gr = new GroupRollout();

		gr.setName("G1");
		gr.setRolloutPercentage(50);

		groups.add(gr);
		
		gr.setName("G2");
		gr.setRolloutPercentage(51);

		groups.add(gr);

		assignedUsers.put(GROUPS, groups);

		assignmentParameters.put(AUDIENCE, assignedUsers);

		variants.add(createFeatureVariant(true));

		assertThrows(TargetingException.class,
				() -> targetingEvaluator.assignVariantAsync(featureDefinitionMock).block());
	}

	@Test
	public void defaultPercentageOutOfRange() {
		assignmentParameters.put("defaultRolloutPercentage", 101);

		variants.add(createFeatureVariant(true));

		assertThrows(TargetingException.class,
				() -> targetingEvaluator.assignVariantAsync(featureDefinitionMock).block());

	}

	private FeatureVariant createFeatureVariant(Boolean isDefault) {
		FeatureVariant featureVariant = new FeatureVariant();

		featureVariant.setAssignmentParameters(assignmentParameters);

		featureVariant.setDefault(isDefault);

		return featureVariant;
	}

	private class TestTargetingContextAccessor implements ITargetingContextAccessor {

		@Override
		public Mono<TargetingContext> getContextAsync() {
			TargetingContext context = new TargetingContext();
			context.setUserId("Doe");

			List<String> groups = new ArrayList<>();
			groups.add("G1");

			context.setGroups(groups);
			return Mono.just(context);
		}

	}

	private class InvlaidTargetingContextAccessor implements ITargetingContextAccessor {

		@Override
		public Mono<TargetingContext> getContextAsync() {
			return Mono.justOrEmpty(null);
		}

	}

}
