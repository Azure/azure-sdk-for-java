package com.azure.openrewrite.core.http;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.*;

public class HttpRequestCustomRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "Fluent HTTP request";
    }

    @Override
    public String getDescription() {
        return "Convert to fluent HTTP request.";
    }


    @Override
    public JavaVisitor<ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {


            private final JavaTemplate template = JavaTemplate.builder("new HttpRequest()\n" +
                    ".setMethod(#{any()})\n" +
                    ".setUri(#{any()})")
                //.contextSensitive()
                .build();

            @Override
            public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                // replace HttpRequest constructor with fluent API
                // Before: com.azure.core.http.HttpRequest <constructor>(io.clientcore.core.http.models.HttpMethod, java.lang.String)
                // After: io.clientcore.core.http.models.HttpRequest <constructor>().setMethod(io.clientcore.core.http.models.HttpMethod).setUri(java.lang.String)
                MethodMatcher methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(io.clientcore.core.http.models.HttpMethod, java.lang.String)");
                if (!methodMatcher.matches(newClass)) {
                    return super.visitNewClass(newClass, ctx);
                }
                J n = template.apply(updateCursor(newClass), newClass.getCoordinates().replace(), newClass.getArguments().get(0), newClass.getArguments().get(1));
                return super.visit(n, ctx);
            }

            @Override
            public J visitMethodInvocation(J.MethodInvocation methodInvocation, ExecutionContext ctx) {
                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;
                // replace core HttpRequest methods with clientcore HttpRequest methods


                // Before: com.azure.core.http.HttpRequest setBody(java.lang.String)
                // After: io.clientcore.core.http.models.HttpRequest setBody(io.clientcore.core.models.binarydata.BinaryData)
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setBody(java.lang.String)");
                if (methodMatcher.matches(methodInvocation, false)) {
                    replacementTemplate = JavaTemplate.builder("setBody(BinaryData.fromString(#{any(java.lang.String)}))")
                        .imports("io.clientcore.core.models.binarydata.BinaryData")
                        .build();
                    methodInvocation = replacementTemplate.apply(getCursor(), methodInvocation.getCoordinates().replaceMethod(), methodInvocation.getArguments().get(0));
                    maybeAddImport("io.clientcore.core.models.binarydata.BinaryData", false);
                }

                return super.visitMethodInvocation(methodInvocation, ctx);
            }

        };
    }
}
