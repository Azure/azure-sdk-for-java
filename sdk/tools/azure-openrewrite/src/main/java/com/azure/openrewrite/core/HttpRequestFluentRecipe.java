package com.azure.openrewrite.core;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

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
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            private final MethodMatcher methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(..)");

            @Override
            public J.NewClass visitNewClass(J.NewClass statement, ExecutionContext ctx) {
                System.out.printf("Statement: %s\n", statement);
                return super.visitNewClass(statement, ctx);
            }
        };
    }
}
