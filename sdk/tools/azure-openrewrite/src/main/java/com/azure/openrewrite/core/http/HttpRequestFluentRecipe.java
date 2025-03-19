package com.azure.openrewrite.core.http;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.*;

public class HttpRequestFluentRecipe extends Recipe {
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
            private final MethodMatcher methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(io.clientcore.core.http.models.HttpMethod, java.lang.String)");

            private final JavaTemplate template = JavaTemplate.builder("new HttpRequest()\n" +
                    ".setMethod(#{any()})\n" +
                    ".setUri(#{any()})")
                //.contextSensitive()
                .build();

            @Override
            public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                System.out.println("Visiting new class: " + newClass.toString());
                if (!methodMatcher.matches(newClass)) {
                    return super.visitNewClass(newClass, ctx);
                }
                J n = template.apply(updateCursor(newClass), newClass.getCoordinates().replace(), newClass.getArguments().get(0), newClass.getArguments().get(1));
                return super.visit(n, ctx);
            }

        };
    }
}
