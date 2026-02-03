package com.azure.openrewrite.core.http;

import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

import com.azure.openrewrite.util.ConfiguredParserJavaTemplateBuilder;

/**
 * A custom OpenRewrite recipe to migrate the use of HttpResponse.
 *
 * <p>This recipe performs the following transformations:</p>
 * <ul>
 *   <li>Replaces the getHeaderValue(String) method with getHeaders().getValue(HttpHeaderName.fromString(String)).</li>
 *   <li>Replaces the getHeaderValue(HttpHeaderName) method with getHeaders().getValue(HttpHeaderName).</li>
 *   <li>Replaces the getBodyAsBinaryData() method with BinaryData.fromObject(getValue()).</li>
 * </ul>
 *
 * <p>Example transformations:</p>
 * <pre>
 * Before: String getHeaderValue(java.lang.String)
 * After: String getHeaders().getValue(io.clientcore.core.http.models.HttpHeaderName.fromString(java.lang.String))
 *
 * Before: java.lang.String getHeaderValue(com.azure.core.http.HttpHeaderName)
 * After: String getHeaders().getValue(io.clientcore.core.http.models.HttpHeaderName)
 *
 * Before: BinaryData getBodyAsBinaryData()
 * After: BinaryData BinaryData.fromObject(getValue())
 * </pre>
 */
public class HttpResponseCustomRecipe extends Recipe {

    /**
     * Default constructor for {@link HttpResponseCustomRecipe}.
     */
    public HttpResponseCustomRecipe() {
        super();
    }

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "HttpResponse Custom Recipe";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Custom recipe to migrate the use of HttpResponse.";
    }

    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        ConfiguredParserJavaTemplateBuilder templateBuilder = ConfiguredParserJavaTemplateBuilder.defaultBuilder();
        return new JavaIsoVisitor<ExecutionContext>() {

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                J.MethodInvocation visitedMethod = super.visitMethodInvocation(method, executionContext);
                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpResponse getHeaderValue(java.lang.String)");
                if (methodMatcher.matches(visitedMethod)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder(String.format("%s.getHeaders().getValue(HttpHeaderName.fromString(#{any(java.lang.String)}))", visitedMethod.getSelect().toString()))
                            .imports("io.clientcore.core.http.models.HttpHeaderName")
                            .build();
                    return replacementTemplate.apply(updateCursor(visitedMethod), visitedMethod.getCoordinates().replace(), visitedMethod.getArguments().get(0));

                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpResponse getHeaderValue(io.clientcore.core.http.models.HttpHeaderName)");
                if (methodMatcher.matches(visitedMethod)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder(String.format("%s.getHeaders().getValue(#{any(io.clientcore.core.http.models.HttpHeaderName)})", visitedMethod.getSelect().toString()))
                            .imports("io.clientcore.core.http.models.HttpHeaderName")
                            .build();
                    return replacementTemplate.apply(updateCursor(visitedMethod), visitedMethod.getCoordinates().replace(), visitedMethod.getArguments().toArray());
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpResponse getHeaderValue(java.lang.String)");
                if (methodMatcher.matches(visitedMethod)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("getHeaders().getValue(io.clientcore.core.http.models.HttpHeaderName.fromString(#{any(java.lang.String)}))")
                            .imports("io.clientcore.core.http.models.HttpHeaderName")
                            .build();
                    return replacementTemplate.apply(updateCursor(visitedMethod), visitedMethod.getCoordinates().replaceMethod(), visitedMethod.getArguments().toArray());
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpResponse getHeaderValue(com.azure.core.http.HttpHeaderName)");
                if (methodMatcher.matches(visitedMethod)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("getHeaders().getValue(#{any(com.azure.core.http.HttpHeaderName)})")
                            .imports("io.clientcore.core.http.models.HttpHeaderName")
                            .build();
                    return replacementTemplate.apply(updateCursor(visitedMethod), visitedMethod.getCoordinates().replaceMethod(), visitedMethod.getArguments().toArray());
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpResponse getBodyAsBinaryData()");
                if (methodMatcher.matches(visitedMethod)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder(String.format("BinaryData.fromObject(%s.getValue())", visitedMethod.getSelect().toString()))
                            .imports("io.clientcore.core.http.models.HttpHeaderName")
                            .build();
                    return replacementTemplate.apply(updateCursor(visitedMethod), visitedMethod.getCoordinates().replace());
                }

                return visitedMethod;
            }
        };
    }
}
