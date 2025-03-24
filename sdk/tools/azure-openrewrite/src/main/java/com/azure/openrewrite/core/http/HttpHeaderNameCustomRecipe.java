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

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;
                // call super to visit the method invocation
                J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);


                methodMatcher = new MethodMatcher("com.azure.core.http.HttpHeaders set(java.lang.String, java.lang.String)");
                if (methodMatcher.matches(mi)) {
                    String templateHttpHeaderName = "";
                    String headerNameString =  mi.getArguments().get(0).toString();
                    System.out.println("Header Name: " + headerNameString);
                    if (headerNameString.compareToIgnoreCase("Content-Type")  == 0) {
                        templateHttpHeaderName = "HttpHeaderName.CONTENT_TYPE";
                    }

                    String templateInsert = String.format("set(%s, #{})", templateHttpHeaderName);
                    replacementTemplate = JavaTemplate.builder(templateInsert)
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    mi = replacementTemplate.apply(getCursor(), mi.getCoordinates().replaceMethod(), mi.getArguments().get(1));
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName", false);
                    return mi;
                }

                return mi;

            }

        };
    }
}
