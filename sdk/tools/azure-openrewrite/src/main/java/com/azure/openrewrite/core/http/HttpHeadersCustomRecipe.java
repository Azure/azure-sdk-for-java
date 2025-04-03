package com.azure.openrewrite.core.http;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

public class HttpHeadersCustomRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "HttpRequestHeaderCustomRecipe";
    }

    @Override
    public String getDescription() {
        return "Convert String Literals to HttpHeaders.";
    }

    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {

            // replace core HttpHeader methods calls with clientcore HttpHeader method calls
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                method = super.visitMethodInvocation(method, executionContext);

                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;

                // before: com.azure.core.http.HttpHeaders HttpHeaders int getSize()
                // after: no change

                // before: com.azure.core.http.HttpHeaders HttpHeaders add(java.lang.String, java.lang.String)
                // after: io.clientcore.core.http.models.HttpHeaders HttpHeaders add(io.clientcore.core.http.models.HttpHeaderName, java.lang.String)
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpHeaders add(java.lang.String, java.lang.String)");
                if (methodMatcher.matches(method, false)) {
                    replacementTemplate = JavaTemplate.builder("add(#{any(io.clientcore.core.http.models.HttpHeaderName)}, #{any(java.lang.String)})")
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    method = replacementTemplate.apply(updateCursor(method), method.getCoordinates().replaceMethod(), method.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName", false);
                }

                // before: com.azure.core.http.HttpHeaders HttpHeaders add(com.azure.core.http.HttpHeaderName, java.lang.String)
                // after: no change

                // before: com.azure.core.http.HttpHeaders HttpHeaders put(java.lang.String, java.lang.String)
                // after: TODO: io.clientcore.core.http.models.HttpHeaders HttpHeaders set(io.clientcore.core.http.models.HttpHeaderName, java.lang.String)

                // before: com.azure.core.http.HttpHeaders HttpHeaders set(java.lang.String, java.lang.String)
                // after: TODO: io.clientcore.core.http.models.HttpHeaders HttpHeaders set(io.clientcore.core.http.models.HttpHeaderName, java.lang.String)
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpHeaders set(java.lang.String, java.lang.String)");
                if (methodMatcher.matches(method, true)) {
                    replacementTemplate = JavaTemplate.builder("set(HttpHeaderName.fromString(#{any(java.lang.String)}), #{any(java.lang.String)})")
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    method = replacementTemplate.apply(updateCursor(method), method.getCoordinates().replaceMethod(), method.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName", false);
                }

                // before: com.azure.core.http.HttpHeaders HttpHeaders set(com.azure.core.http.HttpHeaderName, java.lang.String)
                // after: no change

                // before: com.azure.core.http.HttpHeaders HttpHeaders setAll(java.util.Map<java.lang.String, java.util.List<java.lang.String>>)
                // after: TODO: io.clientcore.core.http.models.HttpHeaders HttpHeaders setAll(io.clientcore.core.http.models.HttpHeaders)

                // before: com.azure.core.http.HttpHeaders HttpHeaders setAllHttpHeaders(com.azure.core.http.HttpHeaders)
                // after: no change

                // before: com.azure.core.http.HttpHeaders HttpHeader get(java.lang.String)
                // after: TODO: io.clientcore.core.http.models.HttpHeaders HttpHeaders get(io.clientcore.core.http.models.HttpHeaderName)

                // before: com.azure.core.http.HttpHeaders HttpHeader get(com.azure.core.http.HttpHeaderName)
                // after: no change

                // before: com.azure.core.http.HttpHeaders HttpHeader remove(java.lang.String)
                // after: TODO: io.clientcore.core.http.models.HttpHeaders HttpHeader remove(io.clientcore.core.http.models.HttpHeaderName)

                // before: com.azure.core.http.HttpHeaders HttpHeader remove(com.azure.core.http.HttpHeaderName)
                // after: no change

                // before: com.azure.core.http.HttpHeaders String getValue(java.lang.String)
                // after: TODO: io.clientcore.core.http.models.HttpHeaders String getValue(io.clientcore.core.http.models.HttpHeaderName)

                // before: com.azure.core.http.HttpHeaders String getValue(com.azure.core.http.HttpHeaderName)
                // after: no change

                // before: com.azure.core.http.HttpHeaders String[] getValues(java.lang.String)
                // after: TODO: io.clientcore.core.http.models.HttpHeaders java.util.List<String> getValues(io.clientcore.core.http.models.HttpHeaderName)

                // before: com.azure.core.http.HttpHeaders String[] getValues(com.azure.core.http.HttpHeaderName)
                // after: TODO: io.clientcore.core.http.models.HttpHeaders java.util.List<String> getValues(io.clientcore.core.http.models.HttpHeaderName)

                // before: com.azure.core.http.HttpHeaders java.util.Map<java.lang.String, java.lang.String> toMap
                // after: TODO: Chiral API change

                // before: com.azure.core.http.HttpHeaders java.util.Map<java.lang.String, java.lang.String[]> toMultiMap()
                // after: TODO: Chiral API change

                // before: com.azure.core.http.HttpHeaders java.util.Iterator<com.azure.core.http.HttpHeader> iterator()
                // after: TODO: Chiral API change

                // before: com.azure.core.http.HttpHeaders java.util.stream.Stream<com.azure.core.http.HttpHeader> stream()
                // after: no change

                return method;
            }
        };
    }
}
