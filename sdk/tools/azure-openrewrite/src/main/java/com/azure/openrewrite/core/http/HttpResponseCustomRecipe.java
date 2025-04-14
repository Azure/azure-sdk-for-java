package com.azure.openrewrite.core.http;

import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

public class HttpResponseCustomRecipe extends Recipe {

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "HttpResponseCustomRecipe";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "The custom recipe for HttpResponse";
    }

    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                method = (J.MethodInvocation) super.visitMethodInvocation(method, executionContext);
                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;

                // Migrate azure-core HttpResponse method calls to clientcore Response method calls

                /*
                Before: int getStatusCode()
                After: int getStatusCode()
                 */

                // no change needed

                /*
                Before: String getHeaderValue(java.lang.String)
                After: String getHeaders().getValue(io.clientcore.core.http.models.HttpHeaderName)
                 */
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpResponse getHeaderValue(java.lang.String)");
                if (methodMatcher.matches(method, false)) {
                    replacementTemplate = JavaTemplate.builder(String.format("%s.getHeaders().getValue(HttpHeaderName.fromString(#{any(java.lang.String)}))", method.getSelect().toString()))
                            .imports("io.clientcore.core.http.models.HttpHeaderName")
                            .build();
                    return replacementTemplate.apply(updateCursor(method), method.getCoordinates().replace(), method.getArguments().get(0));

                }

                /*
                Before: java.lang.String getHeaderValue(com.azure.core.http.HttpHeaderName)
                After: String getHeaders().getValue(io.clientcore.core.http.models.HttpHeaderName)
                 */
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpResponse getHeaderValue(io.clientcore.core.http.models.HttpHeaderName)");
                if (methodMatcher.matches(method, false)) {
                    replacementTemplate = JavaTemplate.builder(String.format("%s.getHeaders().getValue(#{any(io.clientcore.core.http.models.HttpHeaderName)})", method.getSelect().toString()))
                            .imports("io.clientcore.core.http.models.HttpHeaderName")
                            .build();
                    return replacementTemplate.apply(updateCursor(method), method.getCoordinates().replace(), method.getArguments().get(0));
                }


                /*
                Before: String getHeaderValue(java.lang.String)
                After: String getHeaders().getValue(io.clientcore.core.http.models.HttpHeaderName)
                 */
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpResponse getHeaderValue(java.lang.String)");
                if (methodMatcher.matches(method, false)) {
                    replacementTemplate = JavaTemplate.builder("getHeaders().getValue(io.clientcore.core.http.models.HttpHeaderName.fromString(#{any(java.lang.String)}))")
                            .imports("io.clientcore.core.http.models.HttpHeaderName")
                            .build();
                    return replacementTemplate.apply(updateCursor(method), method.getCoordinates().replaceMethod(), method.getArguments().get(0));
                }

                /*
                Before: java.lang.String getHeaderValue(com.azure.core.http.HttpHeaderName)
                After: String getHeaders().getValue(io.clientcore.core.http.models.HttpHeaderName)
                 */
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpResponse getHeaderValue(com.azure.core.http.HttpHeaderName)");
                if (methodMatcher.matches(method, false)) {
                    replacementTemplate = JavaTemplate.builder("getHeaders().getValue(#{any(com.azure.core.http.HttpHeaderName)})")
                            .imports("io.clientcore.core.http.models.HttpHeaderName")
                            .build();
                    return replacementTemplate.apply(updateCursor(method), method.getCoordinates().replaceMethod(), method.getArguments().get(0));
                }

                /*
                Before: BinaryData getBodyAsBinaryData()
                After: BinaryData BinaryData.fromObject(getValue())
                 */
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpResponse getBodyAsBinaryData()");
                if (methodMatcher.matches(method, false)) {
                    replacementTemplate = JavaTemplate.builder(String.format("BinaryData.fromObject(%s.getValue())", method.getSelect().toString()))
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    return replacementTemplate.apply(updateCursor(method), method.getCoordinates().replace());
                }


                return method;
            }
        };
    }
}
