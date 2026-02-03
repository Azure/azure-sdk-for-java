package com.azure.openrewrite.core.http;

import com.azure.openrewrite.util.ConfiguredParserJavaTemplateBuilder;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

/**
 * A custom OpenRewrite recipe to migrate the use of HttpRequest.
 *
 * <p>This recipe performs the following transformations:</p>
 * <ul>
 *   <li>Replaces the HttpRequest constructor with a fluent API.</li>
 *   <li>Replaces the HttpRequest methods with clientcore HttpRequest methods.</li>
 * </ul>
 *
 * <p>Example transformations:</p>
 * <pre>
 * Before: com.azure.core.http.HttpRequest &lt;constructor&gt;(com.azure.core.http.HttpMethod, java.lang.String)
 * After: io.clientcore.core.http.models.HttpRequest &lt;constructor&gt;().setMethod(io.clientcore.core.http.models.HttpMethod).setUri(java.lang.String)
 *
 * Before: com.azure.core.http.HttpRequest setBody(java.lang.String)
 * After: io.clientcore.core.http.models.HttpRequest setBody(io.clientcore.core.models.binarydata.BinaryData)
 *
 * Before: com.azure.core.http.HttpRequest getUrl()
 * After: io.clientcore.core.http.models.HttpRequest getUri().toURL()
 * </pre>
 */
public class HttpRequestCustomRecipe extends Recipe {

    /**
     * Default constructor for {@link HttpRequestCustomRecipe}.
     */
    public HttpRequestCustomRecipe() {
        super();
    }
    @Override
    public String getDisplayName() {
        return "HttpRequest Custom Recipe";
    }

    @Override
    public String getDescription() {
        return "This recipe replaces the HttpRequest constructor with a fluent API and replaces the HttpRequest methods with clientcore HttpRequest methods.";
    }



    @Override
    public JavaVisitor<ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {
            MethodMatcher methodMatcher;
            JavaTemplate replacementTemplate;
            private ConfiguredParserJavaTemplateBuilder configuredParserJavaTemplateBuilder = ConfiguredParserJavaTemplateBuilder.defaultBuilder();

            @Override
            public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                J visited = super.visitNewClass(newClass, ctx);
                Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                doAfterVisit(formatter.getVisitor());

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.lang.String)");
                if (methodMatcher.matches((J.NewClass) visited)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.lang.String)})")
                        .imports("io.clientcore.core.http.models.HttpMethod")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), newClass.getCoordinates().replace(), newClass.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");

                    return visited;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.net.URL)");
                if (methodMatcher.matches((J.NewClass) visited)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.net.URL)}.toURI())")
                        .imports("io.clientcore.core.http.models.HttpMethod")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), newClass.getCoordinates().replace(), newClass.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");
                    return visited;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.net.URL, com.azure.core.http.HttpHeaders)");
                if (methodMatcher.matches((J.NewClass) visited)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.net.URL)}.toURI())\n.setHeaders(#{any(io.clientcore.core.http.models.HttpHeaders)})")
                        .imports("io.clientcore.core.http.models.HttpMethod")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), newClass.getCoordinates().replace(), newClass.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");
                    return visited;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.net.URL, com.azure.core.http.HttpHeaders, com.azure.core.util.BinaryData)");
                if (methodMatcher.matches((J.NewClass) visited)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.net.URL)}.toURI())\n.setHeaders(#{any(io.clientcore.core.http.models.HttpHeaders)})\n.setBody(#{any(io.clientcore.core.models.binarydata.BinaryData)})")
                        .imports("io.clientcore.core.http.models.HttpMethod", "io.clientcore.core.models.binarydata.BinaryData")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), newClass.getCoordinates().replace(), newClass.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");
                    maybeAddImport("io.clientcore.core.models.binarydata.BinaryData");
                    return visited;
                }


                return visited;
            }

            @Override
            public J visitMethodInvocation(J.MethodInvocation methodInvocation, ExecutionContext ctx) {
                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;
                J visited = super.visitMethodInvocation(methodInvocation, ctx);

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setBody(java.lang.String)");
                if (methodMatcher.matches((J.MethodInvocation) visited)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("setBody(BinaryData.fromString(#{any(java.lang.String)}))")
                        .imports("io.clientcore.core.models.binarydata.BinaryData")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), methodInvocation.getCoordinates().replaceMethod(), methodInvocation.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.models.binarydata.BinaryData");
                    return visited;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setUrl(java.net.URL)");
                if (methodMatcher.matches((J.MethodInvocation) visited)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("setUri(#{any(java.net.URL)}.toURI())")
                        .imports("java.net.URI")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), methodInvocation.getCoordinates().replaceMethod(), methodInvocation.getArguments().toArray());
                    maybeAddImport("java.net.URI");
                    return visited;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest getUrl()");
                if (methodMatcher.matches((J.MethodInvocation) visited)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder(String.format("%s.getUri().toURL()", ((J.MethodInvocation) visited).getSelect().toString()))
                        .imports("java.net.URL")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), ((J.MethodInvocation) visited).getCoordinates().replace());
                    maybeAddImport("java.net.URL");
                    return visited;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setHeader(..)");
                if (methodMatcher.matches((J.MethodInvocation) visited)) {
                    J variableIdentifier = ((J.MethodInvocation) visited).getSelect();
                    while ((variableIdentifier instanceof J.MethodInvocation)) {

                        variableIdentifier = ((J.MethodInvocation) variableIdentifier).getSelect();
                    }
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder(String.format("setHeaders(%s.getHeaders().set(#{any()},#{any()})", variableIdentifier))
                        .contextSensitive()
                        .imports("io.clientcore.core.http.models.HttpHeaderName", "io.clientcore.core.http.models.HttpHeaders")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), ((J.MethodInvocation) visited).getCoordinates().replaceMethod(), ((J.MethodInvocation) visited).getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName");
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaders");
                    Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                    doAfterVisit(formatter.getVisitor());
                    return visited;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest getBodyAsBinaryData()");
                if (methodMatcher.matches((J.MethodInvocation) visited)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("getBody()")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), ((J.MethodInvocation) visited).getCoordinates().replaceMethod());
                    return visited;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setBody(byte[])");
                if (methodMatcher.matches((J.MethodInvocation) visited)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("setBody(BinaryData.fromBytes(#{anyArray(byte)})")
                        .imports("io.clientcore.core.models.binarydata.BinaryData")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), ((J.MethodInvocation) visited).getCoordinates().replaceMethod(), ((J.MethodInvocation) visited).getArguments().toArray());
                    maybeAddImport("io.clientcore.core.models.binarydata.BinaryData");
                    return visited;
                }

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest copy()");
                if (methodMatcher.matches((J.MethodInvocation) visited)) {
                    J variableIdentifier = ((J.MethodInvocation) visited).getSelect();
                    while ((variableIdentifier instanceof J.MethodInvocation)) {

                        variableIdentifier = ((J.MethodInvocation) variableIdentifier).getSelect();
                    }
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder(String.format("new HttpRequest()\n.setMethod(%1$s.getHttpMethod())\n.setUri(%1$s.getUri())\n.setHeaders(new HttpHeaders(%1$s.getHeaders()))\n.setBody(%1$s.getBody())", variableIdentifier))
                        .imports("io.clientcore.core.http.models.HttpMethod", "io.clientcore.core.http.models.HttpHeaders")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), ((J.MethodInvocation) visited).getCoordinates().replace());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaders");
                    Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                    doAfterVisit(formatter.getVisitor());
                    return visited;
                }

                return visited;
            }

        };
    }
}
