// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.query;

import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.core.mapping.CosmosPersistentProperty;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.repository.query.parser.Part;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Utility class that converts a Spring Data {@link Example} into a Cosmos DB {@link Criteria} tree.
 * <p>
 * Uses {@link CosmosMappingContext} metadata (NOT raw reflection) for property iteration,
 * respects {@link ExampleMatcher} settings for null handling, string matching, ignored paths,
 * and supports nested/embedded objects via dot-notation property paths.
 */
public final class CosmosExampleCriteriaBuilder {

    private CosmosExampleCriteriaBuilder() {
    }

    /**
     * Converts an {@link Example} into a {@link Criteria} tree suitable for Cosmos DB queries.
     *
     * @param example the example containing the probe entity and matcher settings
     * @param mappingContext the Cosmos mapping context for entity metadata
     * @param <S> the entity type
     * @return a {@link Criteria} representing the query conditions, or {@code CriteriaType.ALL} if no conditions apply
     */
    public static <S> Criteria buildCriteria(Example<S> example,
                                             MappingContext<?, CosmosPersistentProperty> mappingContext) {
        S probe = example.getProbe();
        ExampleMatcher matcher = example.getMatcher();
        Class<S> probeType = example.getProbeType();

        @SuppressWarnings("unchecked")
        BasicPersistentEntity<S, CosmosPersistentProperty> entity =
            (BasicPersistentEntity<S, CosmosPersistentProperty>) mappingContext.getRequiredPersistentEntity(probeType);

        PersistentPropertyAccessor<S> accessor = entity.getPropertyAccessor(probe);
        Set<String> ignoredPaths = matcher.getIgnoredPaths();

        List<Criteria> criteriaList = new ArrayList<>();
        buildCriteriaFromEntity(entity, accessor, matcher, ignoredPaths, "", criteriaList, mappingContext);

        if (criteriaList.isEmpty()) {
            return Criteria.getInstance(CriteriaType.ALL);
        }

        return combineCriteria(criteriaList, matcher);
    }

    @SuppressWarnings("unchecked")
    private static <S> void buildCriteriaFromEntity(
        BasicPersistentEntity<S, CosmosPersistentProperty> entity,
        PersistentPropertyAccessor<S> accessor,
        ExampleMatcher matcher,
        Set<String> ignoredPaths,
        String pathPrefix,
        List<Criteria> criteriaList,
        MappingContext<?, CosmosPersistentProperty> mappingContext) {

        entity.doWithProperties((CosmosPersistentProperty property) -> {
            String propertyName = property.getName();
            String dotPath = pathPrefix.isEmpty() ? propertyName : pathPrefix + "." + propertyName;

            // Skip ignored paths
            if (ignoredPaths.contains(dotPath)) {
                return;
            }

            // Skip @Version fields
            if (property.isVersionProperty()) {
                return;
            }

            Object value = accessor.getProperty(property);

            // Skip null @Id fields
            if (property.isIdProperty() && value == null) {
                return;
            }

            if (value == null) {
                handleNullValue(matcher, dotPath, criteriaList);
                return;
            }

            // Handle nested/embedded objects: check if mapping context knows about this type
            if (!property.getTypeInformation().isCollectionLike()) {
                BasicPersistentEntity<Object, CosmosPersistentProperty> nestedEntity = null;
                try {
                    nestedEntity = (BasicPersistentEntity<Object, CosmosPersistentProperty>)
                        mappingContext.getPersistentEntity(property.getTypeInformation());
                } catch (Exception e) {
                    // ignore
                }
                if (nestedEntity != null) {
                    PersistentPropertyAccessor<Object> nestedAccessor = nestedEntity.getPropertyAccessor(value);
                    buildCriteriaFromEntity(nestedEntity, nestedAccessor, matcher, ignoredPaths,
                        dotPath, criteriaList, mappingContext);
                    return;
                }
            }

            // Build criteria for this property value
            Criteria criteria = buildPropertyCriteria(dotPath, value, matcher);
            if (criteria != null) {
                criteriaList.add(criteria);
            }
        });
    }

    private static void handleNullValue(ExampleMatcher matcher, String dotPath, List<Criteria> criteriaList) {
        ExampleMatcher.NullHandler nullHandler = matcher.getNullHandler();
        if (nullHandler == ExampleMatcher.NullHandler.INCLUDE) {
            criteriaList.add(Criteria.getInstance(CriteriaType.IS_NULL, dotPath,
                Collections.emptyList(), Part.IgnoreCaseType.NEVER));
        }
        // IGNORE (default): skip null fields
    }

    private static Criteria buildPropertyCriteria(String dotPath, Object value, ExampleMatcher matcher) {
        if (value instanceof String) {
            return buildStringCriteria(dotPath, (String) value, matcher);
        }
        // Non-string types: always use IS_EQUAL
        return Criteria.getInstance(CriteriaType.IS_EQUAL, dotPath,
            Collections.singletonList(value), Part.IgnoreCaseType.NEVER);
    }

    private static Criteria buildStringCriteria(String dotPath, String value, ExampleMatcher matcher) {
        // Determine case sensitivity
        Part.IgnoreCaseType ignoreCaseType = Part.IgnoreCaseType.NEVER;
        if (matcher.isIgnoreCaseEnabled()) {
            ignoreCaseType = Part.IgnoreCaseType.ALWAYS;
        }

        // Check for property-specific matcher
        ExampleMatcher.PropertySpecifier specifier = getPropertySpecifier(matcher, dotPath);
        ExampleMatcher.StringMatcher stringMatcher = matcher.getDefaultStringMatcher();

        if (specifier != null) {
            if (specifier.getStringMatcher() != null) {
                stringMatcher = specifier.getStringMatcher();
            }
            if (specifier.getIgnoreCase() != null && specifier.getIgnoreCase()) {
                ignoreCaseType = Part.IgnoreCaseType.ALWAYS;
            }
        }

        CriteriaType criteriaType;
        switch (stringMatcher) {
            case STARTING:
                criteriaType = CriteriaType.STARTS_WITH;
                break;
            case ENDING:
                criteriaType = CriteriaType.ENDS_WITH;
                break;
            case CONTAINING:
                criteriaType = CriteriaType.CONTAINING;
                break;
            case REGEX:
                criteriaType = CriteriaType.STRING_EQUALS;
                break;
            case EXACT:
            case DEFAULT:
            default:
                if (ignoreCaseType == Part.IgnoreCaseType.ALWAYS) {
                    criteriaType = CriteriaType.STRING_EQUALS;
                } else {
                    criteriaType = CriteriaType.IS_EQUAL;
                }
                break;
        }

        return Criteria.getInstance(criteriaType, dotPath,
            Collections.singletonList(value), ignoreCaseType);
    }

    private static ExampleMatcher.PropertySpecifier getPropertySpecifier(ExampleMatcher matcher, String path) {
        try {
            return matcher.getPropertySpecifiers().getForPath(path);
        } catch (Exception e) {
            return null;
        }
    }

    private static Criteria combineCriteria(List<Criteria> criteriaList, ExampleMatcher matcher) {
        if (criteriaList.size() == 1) {
            return criteriaList.get(0);
        }

        CriteriaType combiner = matcher.isAllMatching() ? CriteriaType.AND : CriteriaType.OR;

        Criteria combined = criteriaList.get(0);
        for (int i = 1; i < criteriaList.size(); i++) {
            combined = Criteria.getInstance(combiner, combined, criteriaList.get(i));
        }
        return combined;
    }
}
