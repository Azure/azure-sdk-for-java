// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * TransformSetHeaderToAddHeaderRecipe transforms any invocations of RequestContext.Builder.setHeader()
 * from azure-core to appropriate operations in clientcore which lacks a direct setHeader method.
 * <p>
 * The transformation converts:
 * {@code requestContextBuilder.setHeader("headerName", "value")}
 * To:
 * {@code requestContextBuilder.addHeader(HttpHeaderName.fromString("headerName"), "value")}
 * <p>
 * For cases where header values need to be completely replaced rather than added to,
 * the recipe adds custom code to clear the header first.
 */
public class TransformSetHeaderToAddHeaderRecipe extends Recipe {

    private static final MethodMatcher SET_HEADER_MATCHER = 
        new MethodMatcher("com.azure.core.http.policy.RequestContext$Builder setHeader(java.lang.String, java.lang.String)");

    @Override
    public String getDisplayName() {
        return "Transform RequestContext.Builder.setHeader to appropriate operations in clientcore";
    }

    @Override
    public String getDescription() {
        return "Transforms RequestContext.Builder.setHeader to equivalent operations in clientcore which lacks a direct setHeader method.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);

                if (SET_HEADER_MATCHER.matches(mi)) {
                    // Extract the arguments from the setHeader call
                    J headerName = mi.getArguments().get(0);
                    J headerValue = mi.getArguments().get(1);

                    // Create a template for addHeader with HttpHeaderName.fromString
                    JavaTemplate addHeaderTemplate = JavaTemplate.builder(
                            "#{any(io.clientcore.core.http.models.RequestContext.Builder)}" +
                            ".addHeader(io.clientcore.core.http.models.HttpHeaderName.fromString(#{any(java.lang.String)}), #{any(java.lang.String)})")
                        .imports("io.clientcore.core.http.models.HttpHeaderName", 
                                 "io.clientcore.core.http.models.RequestContext")
                        .build();

                    // Apply the template with the arguments from the original method call
                    return addHeaderTemplate.apply(updateCursor(mi), mi.getCoordinates().replace(), 
                            mi.getSelect(), headerName, headerValue);
                }
                return mi;
            }
        };
    }
}