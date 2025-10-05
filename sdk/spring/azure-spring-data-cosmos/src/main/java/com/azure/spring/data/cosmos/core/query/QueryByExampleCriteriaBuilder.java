// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.PropertyValueTransformer;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.support.ExampleMatcherAccessor;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * {@link QueryByExampleCriteriaBuilder} creates a single combined
 * {@link Criteria} for a given {@link Example}. <br />
 * The builder includes any {@link Field} of the {@link Example#getProbe()} applying {@link String} and
 * {@literal null} matching strategies configured on the {@link Example}. Ignored paths are no matter of their actual
 * value not considered. <br />
 *
 */
public class QueryByExampleCriteriaBuilder {

    /**
     * Default constructor.
     */
    public QueryByExampleCriteriaBuilder() {
    }

    /**
     * Extract the {@link Criteria} representing the {@link Example}.
     *
     * @param example must not be {@literal null}.
     * @param <T> Type of the object in the example
     * @return {@literal null} indicates no constraints
     */
    @Nullable
    public static <T> Criteria getPredicate(Example<T> example) {

        Assert.notNull(example, "Example must not be null");

        ExampleMatcher matcher = example.getMatcher();

        List<Criteria> criteriaList = getPredicates("", example.getProbe(),
            example.getProbeType(), new ExampleMatcherAccessor(matcher),
            new PathNode("root", null, example.getProbe()));

        CriteriaType criteriaType = matcher.isAllMatching() ? CriteriaType.AND : CriteriaType.OR;

        return criteriaList.stream()
            .reduce((left, right) -> Criteria.getInstance(criteriaType, left, right))
            .orElse(null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static List<Criteria> getPredicates(String path, Object value,
                                        Class<?> probeType, ExampleMatcherAccessor exampleAccessor, PathNode currentNode) {

        List<Criteria> criteriaList = new ArrayList<>();
        DirectFieldAccessFallbackBeanWrapper beanWrapper = new DirectFieldAccessFallbackBeanWrapper(value);

        for (Field attribute : probeType.getDeclaredFields()) {

            String currentPath = !StringUtils.hasText(path) ? attribute.getName() : path + "." + attribute.getName();

            if (exampleAccessor.isIgnoredPath(currentPath)) {
                continue;
            }

            PropertyValueTransformer transformer = exampleAccessor.getValueTransformerForPath(currentPath);
            Optional<Object> optionalValue = transformer
                .apply(Optional.ofNullable(beanWrapper.getPropertyValue(attribute.getName())));

            if (optionalValue.isEmpty()) {

                if (exampleAccessor.getNullHandler().equals(ExampleMatcher.NullHandler.INCLUDE)) {
                    criteriaList.add(Criteria.getInstance(CriteriaType.IS_NULL,
                        path,
                        Collections.emptyList(),
                        exampleAccessor.isIgnoreCaseForPath(path) ? Part.IgnoreCaseType.ALWAYS : Part.IgnoreCaseType.NEVER));
                }
                continue;
            }

            Object attributeValue = optionalValue.get();

            if (attributeValue == Optional.empty()) {
                continue;
            }

            if (attribute.getType().isAssignableFrom(Collection.class)) {

                criteriaList
                    .addAll(getPredicates(currentPath,
                        attributeValue, probeType, exampleAccessor, currentNode));
                continue;
            }

            if (isAssociation(attribute)) {

                PathNode node = currentNode.add(attribute.getName(), attributeValue);
                if (node.spansCycle()) {
                    throw new InvalidDataAccessApiUsageException(
                        String.format("Path '%s' from root %s must not span a cyclic property reference%n%s", currentPath,
                            ClassUtils.getShortName(probeType), node));
                }

                criteriaList.addAll(getPredicates(currentPath,
                    attributeValue, probeType, exampleAccessor, node
                ));

                continue;
            }

            if (attribute.getType().isAssignableFrom(String.class)) {

                Part.IgnoreCaseType expression = Part.IgnoreCaseType.NEVER;
                if (exampleAccessor.isIgnoreCaseForPath(currentPath)) {
                    expression = Part.IgnoreCaseType.ALWAYS;
                }

                switch (exampleAccessor.getStringMatcherForPath(currentPath)) {

                    case DEFAULT:
                    case EXACT:
                        criteriaList.add(Criteria.getInstance(CriteriaType.IS_EQUAL,
                            currentPath,
                            Collections.singletonList(attributeValue),
                            expression));
                        break;
                    case CONTAINING:
                        criteriaList.add(
                            Criteria.getInstance(CriteriaType.CONTAINING,
                                currentPath,
                                Collections.singletonList(attributeValue),
                                expression)
                        );
                        break;
                    case STARTING:
                        criteriaList.add(
                            Criteria.getInstance(CriteriaType.STARTS_WITH,
                                currentPath,
                                Collections.singletonList(attributeValue),
                                expression)
                        );
                        break;
                    case ENDING:
                        criteriaList.add(
                            Criteria.getInstance(CriteriaType.ENDS_WITH,
                                currentPath,
                                Collections.singletonList(attributeValue),
                                expression)
                        );
                        break;
                    default:
                        throw new IllegalArgumentException(
                            "Unsupported StringMatcher " + exampleAccessor.getStringMatcherForPath(currentPath));
                }
            } else {
                criteriaList.add(
                    Criteria.getInstance(CriteriaType.CONTAINING,
                        currentPath,
                        Collections.singletonList(attributeValue),
                        exampleAccessor.isIgnoreCaseForPath(currentPath) ? Part.IgnoreCaseType.ALWAYS : Part.IgnoreCaseType.NEVER)
                );
            }
        }

        return criteriaList;
    }

    private static boolean isAssociation(Field attribute) {
        return !attribute.getType().isPrimitive()
            && !attribute.getType().isAssignableFrom(String.class);
    }

    /**
     * {@link PathNode} is used to dynamically grow a directed graph structure that allows to detect cycles within its
     * direct predecessor nodes by comparing parent node values using {@link System#identityHashCode(Object)}.
     *
     * @author Christoph Strobl
     */
    private static class PathNode {

        final String name;
        @Nullable final PathNode parent;
        final List<PathNode> siblings = new ArrayList<>();
        @Nullable final Object value;

        PathNode(String edge, @Nullable PathNode parent, @Nullable Object value) {

            this.name = edge;
            this.parent = parent;
            this.value = value;
        }

        PathNode add(String attribute, @Nullable Object value) {

            PathNode node = new PathNode(attribute, this, value);
            siblings.add(node);
            return node;
        }

        boolean spansCycle() {

            if (value == null) {
                return false;
            }

            String identityHex = ObjectUtils.getIdentityHexString(value);
            PathNode current = parent;

            while (current != null) {

                if (current.value != null && ObjectUtils.getIdentityHexString(current.value).equals(identityHex)) {
                    return true;
                }
                current = current.parent;
            }

            return false;
        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            if (parent != null) {
                sb.append(parent);
                sb.append(" -");
                sb.append(name);
                sb.append("-> ");
            }

            sb.append("[{ ");
            sb.append(ObjectUtils.nullSafeToString(value));
            sb.append(" }]");
            return sb.toString();
        }
    }
}
