package com.azure.openrewrite.core.http;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
            MethodMatcher methodMatcher;
            JavaTemplate replacementTemplate;

            private final JavaTemplate template = JavaTemplate.builder("new HttpRequest()\n.setMethod(#{any()})\n.setUri(#{any()})")
                .contextSensitive()
                .build();

            @Override
            public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                // replace HttpRequest constructor with fluent API
                // Before: com.azure.core.http.HttpRequest <constructor>(io.clientcore.core.http.models.HttpMethod, java.lang.String)
                // After: io.clientcore.core.http.models.HttpRequest <constructor>().setMethod(io.clientcore.core.http.models.HttpMethod).setUri(java.lang.String)
                J n = super.visitNewClass(newClass, ctx);

                MethodMatcher methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(io.clientcore.core.http.models.HttpMethod, java.lang.String)");
                if (methodMatcher.matches((J.NewClass) n)) {
                    replacementTemplate = JavaTemplate.builder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.lang.String)})")
                        .imports("io.clientcore.core.http.models.HttpMethod")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), newClass.getCoordinates().replace(), newClass.getArguments().get(0), newClass.getArguments().get(1));
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod", false);
                    Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                    doAfterVisit(formatter.getVisitor());
                    return n;
                }

                /*
                Before: com.azure.core.http.HttpRequest <constructor>(HttpMethod, java.net.URL)
                After: io.clientcore.core.http.models.HttpRequest <constructor>().setMethod(HttpMethod).setUri(java.net.URL.toUri())
                 */
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(io.clientcore.core.http.models.HttpMethod, java.net.URL)");
                if (methodMatcher.matches((J.NewClass) n)) {
                    replacementTemplate = JavaTemplate.builder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.net.URL)}.toURI())")
                        .imports("io.clientcore.core.http.models.HttpMethod")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), newClass.getCoordinates().replace(), newClass.getArguments().get(0), newClass.getArguments().get(1));
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod", false);
                    Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                    doAfterVisit(formatter.getVisitor());
                    return n;
                }

                /*
                Before: com.azure.core.http.HttpRequest <constructor>(io.clientcore.core.http.models.HttpMethod, java.net.URI, io.clientcore.core.http.models.HttpHeaders)
                After: io.clientcore.core.http.models.HttpRequest <constructor>().setMethod(io.clientcore.core.http.models.HttpMethod).setUri(java.net.URI).setHeaders(io.clientcore.core.http.models.HttpHeaders)
                 */
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(io.clientcore.core.http.models.HttpMethod, java.net.URL, io.clientcore.core.http.models.HttpHeaders)");
                if (methodMatcher.matches((J.NewClass) n)) {
                    replacementTemplate = JavaTemplate.builder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.net.URL)}.toURI())\n.setHeaders(#{any(io.clientcore.core.http.models.HttpHeaders)})")
                        .imports("io.clientcore.core.http.models.HttpMethod")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), newClass.getCoordinates().replace(), newClass.getArguments().get(0), newClass.getArguments().get(1), newClass.getArguments().get(2));
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod", false);
                    Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                    doAfterVisit(formatter.getVisitor());
                    return n;
                }

                /*
                Before: com.azure.core.http.HttpRequest <constructor>(io.clientcore.core.http.models.HttpMethod, java.net.URI, io.clientcore.core.http.models.HttpHeaders, io.clientcore.core.models.binarydata.BinaryData)
                After: io.clientcore.core.http.models.HttpRequest <constructor>().setMethod(io.clientcore.core.http.models.HttpMethod).setUri(java.net.URI).setHeaders(io.clientcore.core.http.models.HttpHeaders).setBody(io.clientcore.core.models.binarydata.BinaryData)
                 */
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(io.clientcore.core.http.models.HttpMethod, java.net.URL, io.clientcore.core.http.models.HttpHeaders, com.azure.core.util.BinaryData)");
                if (methodMatcher.matches((J.NewClass) n)) {
                    replacementTemplate = JavaTemplate.builder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.net.URL)}.toURI())\n.setHeaders(#{any(io.clientcore.core.http.models.HttpHeaders)})\n.setBody(#{any(io.clientcore.core.models.binarydata.BinaryData)})")
                        .imports("io.clientcore.core.http.models.HttpMethod")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), newClass.getCoordinates().replace(), newClass.getArguments().get(0), newClass.getArguments().get(1), newClass.getArguments().get(2), newClass.getArguments().get(3));
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod", false);
                    Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                    doAfterVisit(formatter.getVisitor());
                    return n;
                }


                return n;
            }

            @Override
            public J visitMethodInvocation(J.MethodInvocation methodInvocation, ExecutionContext ctx) {
                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;
                J n = super.visitMethodInvocation(methodInvocation, ctx);
                // replace core HttpRequest methods with clientcore HttpRequest methods


                // Before: com.azure.core.http.HttpRequest setBody(java.lang.String)
                // After: io.clientcore.core.http.models.HttpRequest setBody(io.clientcore.core.models.binarydata.BinaryData)
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setBody(java.lang.String)");
                if (methodMatcher.matches((J.MethodInvocation) n, false)) {
                    replacementTemplate = JavaTemplate.builder("setBody(BinaryData.fromString(#{any(java.lang.String)}))")
                        .imports("io.clientcore.core.models.binarydata.BinaryData")
                        .build();
                    n = replacementTemplate.apply(getCursor(), methodInvocation.getCoordinates().replaceMethod(), methodInvocation.getArguments().get(0));
                    maybeAddImport("io.clientcore.core.models.binarydata.BinaryData", false);
                    return n;
                }

                /*
                Before: com.azure.core.http.HttpRequest setUrl
                After: io.clientcore.core.http.models.HttpRequest setUri(java.net.URL.toURI())
                 */
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setUrl(java.net.URL)");
                if (methodMatcher.matches((J.MethodInvocation) n, false)) {
                    replacementTemplate = JavaTemplate.builder("setUri(#{any(java.net.URL)}.toURI())")
                        .imports("java.net.URI")
                        .build();
                    n = replacementTemplate.apply(getCursor(), methodInvocation.getCoordinates().replaceMethod(), methodInvocation.getArguments().get(0));
                    maybeAddImport("java.net.URI");
                    return n;
                }

                /*
                Before: com.azure.core.http.HttpRequest getUrl
                After: io.clientcore.core.http.models.HttpRequest getUri().toURL()
                 */
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest getUrl()");
                if (methodMatcher.matches((J.MethodInvocation) n, false)) {
                    replacementTemplate = JavaTemplate.builder(String.format("%s.getUri().toURL()", ((J.MethodInvocation) n).getSelect().toString()))
                        .imports("java.net.URL")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), ((J.MethodInvocation) n).getCoordinates().replace());
                    maybeAddImport("java.net.URL");
                    return n;
                }

                /*
                Before: com.azure.core.http.HttpRequest setHeader(java.lang.String, java.lang.String)
                After: io.clientcore.core.http.models.HttpRequest getHeaders().setHeader(HttpHeaderName, java.lang.String)
                 */
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setHeader(java.lang.String, java.lang.String)");
                if (methodMatcher.matches((J.MethodInvocation) n, false)) {
                    replacementTemplate = JavaTemplate.builder(String.format("%s\n.getHeaders().set(#{any(java.lang.String)}, #{any(java.lang.String)})",methodInvocation.getSelect()))
                        .imports("io.clientcore.core.http.models.HttpHeaders")
                        .build();
                    n = replacementTemplate.apply(getCursor(), methodInvocation.getCoordinates().replace(), methodInvocation.getArguments().get(0), methodInvocation.getArguments().get(1));
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaders", false);
                    return n;
                }

                return n;
            }

        };
    }
}
