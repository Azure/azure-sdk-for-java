// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.openrewrite.core.http.rest;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adds the string continuation-token type to classic paged types after they are migrated to ClientCore.
 *
 * <p>Classic {@code PagedIterable} and {@code PagedResponse} use string continuation tokens. Classic APIs that use
 * arbitrary token types are represented by the separate continuable paging family and aren't handled by this
 * visitor.</p>
 */
final class PagedTypesVisitor extends JavaIsoVisitor<ExecutionContext> {
    private static final String CLIENTCORE_PAGED_ITERABLE = "io.clientcore.core.http.paging.PagedIterable";
    private static final String CLIENTCORE_PAGED_RESPONSE = "io.clientcore.core.http.paging.PagedResponse";

    @Override
    public J.ParameterizedType visitParameterizedType(J.ParameterizedType parameterizedType,
        ExecutionContext executionContext) {
        J.ParameterizedType visited = super.visitParameterizedType(parameterizedType, executionContext);
        if (visited.getTypeParameters().size() != 1
            || (!TypeUtils.isOfClassType(visited.getType(), CLIENTCORE_PAGED_ITERABLE)
                && !TypeUtils.isOfClassType(visited.getType(), CLIENTCORE_PAGED_RESPONSE))) {
            return visited;
        }

        JavaType stringType = JavaType.buildType("java.lang.String");
        J.Identifier stringIdentifier = new J.Identifier(Tree.randomId(), Space.SINGLE_SPACE, Markers.EMPTY,
            Collections.emptyList(), "String", stringType, null);
        List<Expression> typeParameters = new ArrayList<>(visited.getTypeParameters());
        typeParameters.add(stringIdentifier);

        JavaType.Parameterized attributedType = TypeUtils.asParameterized(visited.getType());
        if (attributedType == null) {
            return visited.withTypeParameters(typeParameters);
        }

        List<JavaType> attributedTypeParameters = new ArrayList<>(attributedType.getTypeParameters());
        attributedTypeParameters.add(stringType);
        return visited.withTypeParameters(typeParameters)
            .withType(attributedType.withTypeParameters(attributedTypeParameters));
    }
}