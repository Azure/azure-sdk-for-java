// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.feature.evaluators;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.cloud.feature.manager.TargetingException;
import com.azure.spring.cloud.feature.manager.feature.filters.TargetingFilter;
import com.azure.spring.cloud.feature.manager.implementation.targeting.Audience;
import com.azure.spring.cloud.feature.manager.implementation.targeting.GroupRollout;
import com.azure.spring.cloud.feature.manager.implementation.targeting.TargetingFilterSettings;
import com.azure.spring.cloud.feature.manager.models.FeatureDefinition;
import com.azure.spring.cloud.feature.manager.models.FeatureVariant;
import com.azure.spring.cloud.feature.manager.models.IFeatureVariantAssigner;
import com.azure.spring.cloud.feature.manager.targeting.ITargetingContextAccessor;
import com.azure.spring.cloud.feature.manager.targeting.TargetingContext;
import com.azure.spring.cloud.feature.manager.targeting.TargetingEvaluationOptions;

import reactor.core.publisher.Mono;

/**
 * Evaluator for Dynamic Feature and Feature Filters.
 */
public final class TargetingEvaluator extends TargetingFilter implements IFeatureVariantAssigner {

	private static final Logger LOGGER = LoggerFactory.getLogger(TargetingEvaluator.class);

	/**
	 * `Microsoft.TargetingFilter` evaluates a user/group/overall rollout of a
	 * feature.
	 * 
	 * @param contextAccessor Context for evaluating the users/groups.
	 */
	public TargetingEvaluator(ITargetingContextAccessor contextAccessor) {
		super(contextAccessor);
	}

	/**
	 * `Microsoft.TargetingFilter` evaluates a user/group/overall rollout of a
	 * feature.
	 * 
	 * @param contextAccessor Context for evaluating the users/groups.
	 * @param options         enables customization of the filter.
	 */
	public TargetingEvaluator(ITargetingContextAccessor contextAccessor, TargetingEvaluationOptions options) {
		super(contextAccessor, options);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Mono<FeatureVariant> assignVariantAsync(FeatureDefinition featureDefinition) throws TargetingException {
		if (contextAccessor == null) {
			LOGGER.warn("No Context Accessor set for TargetingEvaluator.");
			return Mono.justOrEmpty(null);
		}
		
		TargetingContext targetingContext = contextAccessor.getContextAsync().block();

		if (targetingContext == null) {
			LOGGER.warn("No targeting context available for targeting evaluation.");
			return Mono.justOrEmpty(null);
		}

		Map<FeatureVariant, Audience> assignments = new HashMap<>();

		List<FeatureVariant> variants = featureDefinition.getVariants();

		validateVariantSettings(variants);

		HashMap<String, Double> totalGroupPerentages = new HashMap<>();
		double totalDefaultPercentage = 0;

		for (FeatureVariant variant : variants) {

			LinkedHashMap<String, Object> parameters = variant.getAssignmentParameters();

			if (parameters != null) {
				Object audienceObject = parameters.get(AUDIENCE);
				if (audienceObject != null) {
					parameters = (LinkedHashMap<String, Object>) audienceObject;
				}

				this.<String>updateValueFromMapToList(parameters, USERS);
				updateValueFromMapToList(parameters, GROUPS);
				
				assignments.put(variant, OBJECT_MAPPER.convertValue(parameters, Audience.class));
			}
			
		}

		// First, we need to check if a users is assigned to a variant.
		for (Entry<FeatureVariant, Audience> assignment : assignments.entrySet()) {
			Audience audience = assignment.getValue();
			if (targetingContext.getUserId() != null && audience.getUsers() != null && audience.getUsers().stream()
					.anyMatch(user -> compareStrings(targetingContext.getUserId(), user))) {
				return Mono.just(assignment.getKey());
			}
		}

		// Second, is the user part of of a group and in the groups rollout percentage
		for (Entry<FeatureVariant, Audience> assignment : assignments.entrySet()) {
			Audience audience = assignment.getValue();
			if (targetingContext.getGroups() != null && audience.getGroups() != null) {
				for (String group : targetingContext.getGroups()) {
					Optional<GroupRollout> groupRollout = audience.getGroups().stream()
							.filter(g -> compareStrings(g.getName(), group)).findFirst();

					if (groupRollout.isPresent()) {
						String audienceContextId = targetingContext.getUserId() + "\n" + featureDefinition.getName()
								+ "\n" + group;

						double chance = totalGroupPerentages.getOrDefault(group, (double) 0);

						if (isTargetedPercentage(audienceContextId) < groupRollout.get().getRolloutPercentage()
								+ chance) {
							return Mono.just(assignment.getKey());
						}
						totalGroupPerentages.put(group, chance + groupRollout.get().getRolloutPercentage());
					}
				}
			}
		}

		// Third, is the user part of the default rollout
		for (Entry<FeatureVariant, Audience> assignment : assignments.entrySet()) {
			Audience audience = assignment.getValue();
			String defaultContextId = targetingContext.getUserId() + "\n" + featureDefinition.getName();

			if (isTargetedPercentage(defaultContextId) < audience.getDefaultRolloutPercentage()
					+ totalDefaultPercentage) {
				return Mono.just(assignment.getKey());
			}
			totalDefaultPercentage += audience.getDefaultRolloutPercentage();
		}

		// Null is returned when the user needs to be assigned the default variant
		return Mono.justOrEmpty(null);
	}

	/**
	 * Validates the settings of the variant.
	 * 
	 * @param variantSettings variant settings
	 * @throws TargetingException thrown when percentage range is greater than 100
	 */
	@SuppressWarnings("unchecked")
	protected void validateVariantSettings(List<FeatureVariant> variantSettings) throws TargetingException {
		Map<String, Double> groupUsed = new HashMap<>();

		for (FeatureVariant variant : variantSettings) {
			TargetingFilterSettings settings = new TargetingFilterSettings();
			LinkedHashMap<String, Object> parameters = variant.getAssignmentParameters();

			if (parameters != null) {
				Object audienceObject = parameters.get(AUDIENCE);
				if (audienceObject != null) {
					parameters = (LinkedHashMap<String, Object>) audienceObject;
				}

				this.<String>updateValueFromMapToList(parameters, USERS);
				updateValueFromMapToList(parameters, GROUPS);

				settings.setAudience(OBJECT_MAPPER.convertValue(parameters, Audience.class));
			}

			validateSettings(settings);

			Audience audience = settings.getAudience();

			List<GroupRollout> groups = audience.getGroups();

			if (groups != null) {
			    
			    groups.forEach(groupRollout -> {
			        Double currentSize = groupUsed.getOrDefault(groupRollout.getName(), (double) 0);
                    currentSize += groupRollout.getRolloutPercentage();
                    if (currentSize > 100) {
                        throw new TargetingException(groupRollout.getName() + " : " + OUT_OF_RANGE);
                    }
                    groupUsed.put(groupRollout.getName(), currentSize);
			    });
			}
		}
	}
}
