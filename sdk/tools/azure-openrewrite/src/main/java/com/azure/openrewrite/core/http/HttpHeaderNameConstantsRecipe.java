package com.azure.openrewrite.core.http;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

import java.util.List;

public class HttpHeaderNameConstantsRecipe extends Recipe {
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

            JavaTemplate constant = JavaTemplate.builder("set(HttpHeaderName.CONTENT_TYPE, #{})")
                .imports("io.clientcore.core.http.models.HttpHeaderName")
                .build();
            MethodMatcher setMethodMatcher = new MethodMatcher("com.azure.core.http.HttpHeaders set(String, String)");

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                // call super to visit the method invocation
                J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);
                if (!setMethodMatcher.matches(mi)) {
                    return mi;
                }

                mi = constant.apply(getCursor(), mi.getCoordinates().replace(), mi.getArguments().get(1));

                return mi;

            }

        };
    }
}
