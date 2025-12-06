package com.azure.openrewrite.core.http;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;

import com.azure.openrewrite.util.ConfiguredParserJavaTemplateBuilder;

/**
 * A custom OpenRewrite recipe to migrate the use of HttpHeaders.
 *
 * <p>This recipe performs the following transformations:</p>
 * <ul>
 *   <li>Replaces the add method of HttpHeaders to use HttpHeaderName instead of String.</li>
 *   <li>Replaces the set method of HttpHeaders to use HttpHeaderName instead of String.</li>
 * </ul>
 *
 * <p>Example transformations:</p>
 * <pre>
 * Before: com.azure.core.http.HttpHeaders add(java.lang.String, java.lang.String)
 * After: io.clientcore.core.http.models.HttpHeaders add(io.clientcore.core.http.models.HttpHeaderName, java.lang.String)
 *
 * Before: com.azure.core.http.HttpHeaders set(java.lang.String, java.lang.String)
 * After: io.clientcore.core.http.models.HttpHeaders set(io.clientcore.core.http.models.HttpHeaderName, java.lang.String)
 * </pre>
 */
public class HttpHeadersCustomRecipe extends Recipe {

    /**
     * Default constructor for {@link HttpHeadersCustomRecipe}.
     */
    public HttpHeadersCustomRecipe() {
        super();
    }
    @Override
    public String getDisplayName() {
        return "HttpHeaders Custom Recipe";
    }

    @Override
    public String getDescription() {
        return "Custom recipe to migrate the use of HttpHeaders.";
    }

    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        ConfiguredParserJavaTemplateBuilder templateBuilder = ConfiguredParserJavaTemplateBuilder.defaultBuilder();
        return new JavaIsoVisitor<ExecutionContext>() {

            // replace core HttpHeader methods calls with clientcore HttpHeader method calls
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                J.MethodInvocation visitedMethodInvocation = super.visitMethodInvocation(method, executionContext);

                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpHeaders add(java.lang.String, java.lang.String)");
                if (methodMatcher.matches(visitedMethodInvocation, true)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("add(HttpHeaderName.fromString(#{any(java.lang.String)}), #{any(java.lang.String)})")
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    visitedMethodInvocation = replacementTemplate.apply(updateCursor(visitedMethodInvocation), visitedMethodInvocation.getCoordinates().replaceMethod(), visitedMethodInvocation.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName");
                    return visitedMethodInvocation;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpHeaders put(java.lang.String, java.lang.String)");
                if (methodMatcher.matches(visitedMethodInvocation)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("set(HttpHeaderName.fromString(#{any(java.lang.String)}), #{any(java.lang.String)})")
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    visitedMethodInvocation = replacementTemplate.apply(updateCursor(visitedMethodInvocation), visitedMethodInvocation.getCoordinates().replaceMethod(), visitedMethodInvocation.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName");
                    return visitedMethodInvocation;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpHeaders set(java.lang.String, ..)");
                if (methodMatcher.matches(visitedMethodInvocation, true)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("set(HttpHeaderName.fromString(#{any(java.lang.String)}), #{any(java.lang.String)})")
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    visitedMethodInvocation = replacementTemplate.apply(updateCursor(visitedMethodInvocation), visitedMethodInvocation.getCoordinates().replaceMethod(), visitedMethodInvocation.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName");
                    return visitedMethodInvocation;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpHeaders setAll(java.util.Map)");
                if (methodMatcher.matches(visitedMethodInvocation, true)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("setAll(#{any(java.util.Map)}.entrySet().stream().collect(\n" +
                    "HttpHeaders::new,\n" +
                    "(newHeaders, entry) -> newHeaders.set(HttpHeaderName.fromString(entry.getKey()), entry.getValue()),\n" +
                    "HttpHeaders::setAll\n))")
                        .imports("io.clientcore.core.http.models.HttpHeaders", "io.clientcore.core.http.models.HttpHeaderName", "java.util.Collections")
                        .build();
                    visitedMethodInvocation = replacementTemplate.apply(updateCursor(visitedMethodInvocation), visitedMethodInvocation.getCoordinates().replaceMethod(), visitedMethodInvocation.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaders");
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName");
                    return visitedMethodInvocation;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpHeaders get(java.lang.String)");
                if (methodMatcher.matches(visitedMethodInvocation)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("get(HttpHeaderName.fromString(#{any(java.lang.String)}))")
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    visitedMethodInvocation = replacementTemplate.apply(updateCursor(visitedMethodInvocation), visitedMethodInvocation.getCoordinates().replaceMethod(), visitedMethodInvocation.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName");
                    return visitedMethodInvocation;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpHeaders remove(java.lang.String)");
                if (methodMatcher.matches(visitedMethodInvocation)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("remove(HttpHeaderName.fromString(#{any(java.lang.String)}))")
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    visitedMethodInvocation = replacementTemplate.apply(updateCursor(visitedMethodInvocation), visitedMethodInvocation.getCoordinates().replaceMethod(), visitedMethodInvocation.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName");
                    return visitedMethodInvocation;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpHeaders getValue(java.lang.String)");
                if (methodMatcher.matches(visitedMethodInvocation)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("getValue(HttpHeaderName.fromString(#{any(java.lang.String)}))")
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    visitedMethodInvocation = replacementTemplate.apply(updateCursor(visitedMethodInvocation), visitedMethodInvocation.getCoordinates().replaceMethod(), visitedMethodInvocation.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName");
                    return visitedMethodInvocation;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpHeaders getValues(java.lang.String)");
                if (methodMatcher.matches(visitedMethodInvocation)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("getValues(HttpHeaderName.fromString(#{any(java.lang.String)}))")
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    J.MethodInvocation methodInvocation = replacementTemplate.apply(updateCursor(visitedMethodInvocation), visitedMethodInvocation.getCoordinates().replaceMethod(), visitedMethodInvocation.getArguments().toArray());
                    visitedMethodInvocation = methodInvocation.withSelect(visitedMethodInvocation.getSelect());
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("toArray(new String[0])").build();
                    methodInvocation = replacementTemplate.apply(getCursor(), visitedMethodInvocation.getCoordinates().replaceMethod());
                    visitedMethodInvocation = methodInvocation.withSelect(visitedMethodInvocation).withPrefix(Space.EMPTY);
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName");
                    return visitedMethodInvocation;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpHeaders getValues(com.azure.core.http.HttpHeaderName)");
                if (methodMatcher.matches(visitedMethodInvocation, true)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("toArray(new String[0])")
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    J.MethodInvocation methodInvocation = replacementTemplate.apply(updateCursor(visitedMethodInvocation), visitedMethodInvocation.getCoordinates().replace());
                    visitedMethodInvocation = methodInvocation.withSelect(visitedMethodInvocation).withPrefix(Space.EMPTY);
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName");
                    return visitedMethodInvocation;
                }

                return visitedMethodInvocation;
            }
        };
    }
}
