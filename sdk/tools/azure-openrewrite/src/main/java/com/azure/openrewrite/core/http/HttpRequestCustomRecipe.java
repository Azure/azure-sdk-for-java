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
                
                // Before: com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.lang.String)
                // After: io.clientcore.core.http.models.HttpRequest <constructor>().setMethod(io.clientcore.core.http.models.HttpMethod).setUri(java.lang.String)
                J n = super.visitNewClass(newClass, ctx);

                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.lang.String)");
                if (methodMatcher.matches((J.NewClass) n)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.lang.String)})")
                        .imports("io.clientcore.core.http.models.HttpMethod")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), newClass.getCoordinates().replace(), newClass.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");
                    Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                    doAfterVisit(formatter.getVisitor());
                    return n;
                }

                // Before: com.azure.core.http.HttpRequest <constructor>(com.azure.core.httpHttpMethod, java.net.URL)
                // After: io.clientcore.core.http.models.HttpRequest <constructor>().setMethod(HttpMethod).setUri(java.net.URL.toUri())
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.net.URL)");
                if (methodMatcher.matches((J.NewClass) n)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.net.URL)}.toURI())")
                        .imports("io.clientcore.core.http.models.HttpMethod")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), newClass.getCoordinates().replace(), newClass.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");
                    Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                    doAfterVisit(formatter.getVisitor());
                    return n;
                }

                // Before: com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.net.URI, io.clientcore.core.http.models.HttpHeaders)
                // After: io.clientcore.core.http.models.HttpRequest <constructor>().setMethod(io.clientcore.core.http.models.HttpMethod).setUri(java.net.URI).setHeaders(io.clientcore.core.http.models.HttpHeaders)
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.net.URL, com.azure.core.http.HttpHeaders)");
                if (methodMatcher.matches((J.NewClass) n)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.net.URL)}.toURI())\n.setHeaders(#{any(io.clientcore.core.http.models.HttpHeaders)})")
                        .imports("io.clientcore.core.http.models.HttpMethod")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), newClass.getCoordinates().replace(), newClass.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");
                    Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                    doAfterVisit(formatter.getVisitor());
                    return n;
                }

                // Before: com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.net.URI, com.azure.core.http.HttpHeaders, com.azure.core.util.BinaryData)
                // After: io.clientcore.core.http.models.HttpRequest <constructor>().setMethod(io.clientcore.core.http.models.HttpMethod).setUri(java.net.URI).setHeaders(io.clientcore.core.http.models.HttpHeaders).setBody(io.clientcore.core.models.binarydata.BinaryData)
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.net.URL, com.azure.core.http.HttpHeaders, com.azure.core.util.BinaryData)");
                if (methodMatcher.matches((J.NewClass) n)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.net.URL)}.toURI())\n.setHeaders(#{any(io.clientcore.core.http.models.HttpHeaders)})\n.setBody(#{any(io.clientcore.core.models.binarydata.BinaryData)})")
                        .imports("io.clientcore.core.http.models.HttpMethod", "io.clientcore.core.models.binarydata.BinaryData")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), newClass.getCoordinates().replace(), newClass.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");
                    maybeAddImport("io.clientcore.core.models.binarydata.BinaryData");
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
                // Before: com.azure.core.http.HttpRequest setBody(java.lang.String)
                // After: io.clientcore.core.http.models.HttpRequest setBody(io.clientcore.core.models.binarydata.BinaryData)
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setBody(java.lang.String)");
                if (methodMatcher.matches((J.MethodInvocation) n)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("setBody(BinaryData.fromString(#{any(java.lang.String)}))")
                        .javaParser(
                            JavaParser.fromJavaVersion()
                                .classpath("azure-core", "core")
                        )
                        .imports("io.clientcore.core.models.binarydata.BinaryData")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), methodInvocation.getCoordinates().replaceMethod(), methodInvocation.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.models.binarydata.BinaryData");
                    return n;
                }

                // Before: com.azure.core.http.HttpRequest setUrl
                // After: io.clientcore.core.http.models.HttpRequest setUri(java.net.URL.toURI())
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setUrl(java.net.URL)");
                if (methodMatcher.matches((J.MethodInvocation) n, false)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("setUri(#{any(java.net.URL)}.toURI())")
                        .imports("java.net.URI")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), methodInvocation.getCoordinates().replaceMethod(), methodInvocation.getArguments().toArray());
                    maybeAddImport("java.net.URI");
                    return n;
                }

                // Before: com.azure.core.http.HttpRequest getUrl
                // After: io.clientcore.core.http.models.HttpRequest getUri().toURL()
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest getUrl()");
                if (methodMatcher.matches((J.MethodInvocation) n, false)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder(String.format("%s.getUri().toURL()", ((J.MethodInvocation) n).getSelect().toString()))
                        .imports("java.net.URL")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), ((J.MethodInvocation) n).getCoordinates().replace());
                    maybeAddImport("java.net.URL");
                    return n;
                }

                // Before: com.azure.core.http.HttpRequest setHeader(java.lang.String, java.lang.String)
                // After: io.clientcore.core.http.models.HttpRequest  setHeaders(getHeaders().set(io.clientcore.core.http.models.HttpHeaderName, java.lang.String))
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setHeader(..)");
                if (methodMatcher.matches((J.MethodInvocation) n, false)) {
                    J variableIdentifier = ((J.MethodInvocation) n).getSelect();
                    while ((variableIdentifier instanceof J.MethodInvocation)) {

                        variableIdentifier = ((J.MethodInvocation) variableIdentifier).getSelect();
                    }
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder(String.format("setHeaders(%s.getHeaders().set(#{any()},#{any()})", variableIdentifier))
                        .contextSensitive()
                        .imports("io.clientcore.core.http.models.HttpHeaderName", "io.clientcore.core.http.models.HttpHeaders")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), ((J.MethodInvocation) n).getCoordinates().replaceMethod(), ((J.MethodInvocation) n).getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName");
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaders");
                    Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                    doAfterVisit(formatter.getVisitor());
                    return n;
                }

                // Before: com.azure.core.http.HttpRequest getBodyAsBinaryData()
                // After: io.clientcore.core.http.models.HttpRequest getBody()
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest getBodyAsBinaryData()");
                if (methodMatcher.matches((J.MethodInvocation) n, false)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("getBody()")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), ((J.MethodInvocation) n).getCoordinates().replaceMethod());
                    return n;
                }

                // Before: com.azure.core.http.HttpRequest setBody(byte[])
                // After: io.clientcore.core.http.models.HttpRequest setBody(io.clientcore.core.models.binarydata.BinaryData)
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setBody(byte[])");
                if (methodMatcher.matches((J.MethodInvocation) n, false)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("setBody(BinaryData.fromBytes(#{anyArray(byte)})")
                        .imports("io.clientcore.core.models.binarydata.BinaryData")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), ((J.MethodInvocation) n).getCoordinates().replaceMethod(), ((J.MethodInvocation) n).getArguments().toArray());
                    maybeAddImport("io.clientcore.core.models.binarydata.BinaryData");
                    return n;
                }

                // Before: com.azure.core.http.HttpRequest copy()
                // After: io.clientcore.core.http.models.HttpRequest setMethod(io.clientcore.core.http.models.HttpMethod).setUri(java.net.URL.toURI()).setHeaders(io.clientcore.core.http.models.HttpHeaders).setBody(io.clientcore.core.models.binarydata.BinaryData)
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest copy()");
                if (methodMatcher.matches((J.MethodInvocation) n, false)) {
                    J variableIdentifier = ((J.MethodInvocation) n).getSelect();
                    while ((variableIdentifier instanceof J.MethodInvocation)) {

                        variableIdentifier = ((J.MethodInvocation) variableIdentifier).getSelect();
                    }
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder(String.format("new HttpRequest()\n.setMethod(%1$s.getHttpMethod())\n.setUri(%1$s.getUri())\n.setHeaders(new HttpHeaders(%1$s.getHeaders()))\n.setBody(%1$s.getBody())", variableIdentifier))
                        .imports("io.clientcore.core.http.models.HttpMethod", "io.clientcore.core.http.models.HttpHeaders")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), ((J.MethodInvocation) n).getCoordinates().replace());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaders");
                    Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                    doAfterVisit(formatter.getVisitor());
                    return n;
                }

                return n;
            }

        };
    }
}
