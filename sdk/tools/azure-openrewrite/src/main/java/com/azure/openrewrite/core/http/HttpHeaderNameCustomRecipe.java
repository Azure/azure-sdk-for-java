package com.azure.openrewrite.core.http;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

public class HttpHeaderNameCustomRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "HttpRequestHeaderConstants";
    }

    @Override
    public String getDescription() {
        return "Convert String Literals to HttpHeaders.";
    }

    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {

        };
    }
}
