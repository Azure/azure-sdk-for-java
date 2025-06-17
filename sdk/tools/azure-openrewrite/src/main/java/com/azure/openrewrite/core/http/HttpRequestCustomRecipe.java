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

            /**
             * Helper method to find the base variable identifier from a chained method call.
             * Traverses up the method invocation chain to find the root variable.
             * 
             * @param methodInvocation the method invocation to start from
             * @return the base variable identifier
             */
            private J findBaseVariableIdentifier(J.MethodInvocation methodInvocation) {
                J variableIdentifier = methodInvocation.getSelect();
                while (variableIdentifier instanceof J.MethodInvocation) {
                    variableIdentifier = ((J.MethodInvocation) variableIdentifier).getSelect();
                }
                return variableIdentifier;
            }

            /**
             * Helper method to apply formatting after a transformation.
             */
            private void applyFormatting() {
                Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                doAfterVisit(formatter.getVisitor());
            }

            /**
             * Visits HttpRequest constructor calls and transforms them to use the clientcore fluent API.
             * 
             * Transforms various HttpRequest constructor overloads:
             * - HttpRequest(HttpMethod, String) -> new HttpRequest().setMethod().setUri()
             * - HttpRequest(HttpMethod, URL) -> new HttpRequest().setMethod().setUri()
             * - HttpRequest(HttpMethod, URL, HttpHeaders) -> new HttpRequest().setMethod().setUri().setHeaders()
             * - HttpRequest(HttpMethod, URL, HttpHeaders, BinaryData) -> new HttpRequest().setMethod().setUri().setHeaders().setBody()
             */
            @Override
            public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                J visited = super.visitNewClass(newClass, ctx);
                applyFormatting();

                // Transform HttpRequest(HttpMethod, String) constructor
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.lang.String)");
                if (methodMatcher.matches((J.NewClass) visited)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.lang.String)})")
                        .imports("io.clientcore.core.http.models.HttpMethod")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), newClass.getCoordinates().replace(), newClass.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");
                    return visited;
                }

                // Transform HttpRequest(HttpMethod, URL) constructor
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.net.URL)");
                if (methodMatcher.matches((J.NewClass) visited)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.net.URL)}.toURI())")
                        .imports("io.clientcore.core.http.models.HttpMethod")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), newClass.getCoordinates().replace(), newClass.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");
                    return visited;
                }

                // Transform HttpRequest(HttpMethod, URL, HttpHeaders) constructor  
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest <constructor>(com.azure.core.http.HttpMethod, java.net.URL, com.azure.core.http.HttpHeaders)");
                if (methodMatcher.matches((J.NewClass) visited)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("new HttpRequest()\n.setMethod(#{any(io.clientcore.core.http.models.HttpMethod)})\n.setUri(#{any(java.net.URL)}.toURI())\n.setHeaders(#{any(io.clientcore.core.http.models.HttpHeaders)})")
                        .imports("io.clientcore.core.http.models.HttpMethod")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), newClass.getCoordinates().replace(), newClass.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");
                    return visited;
                }

                // Transform HttpRequest(HttpMethod, URL, HttpHeaders, BinaryData) constructor
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

            /**
             * Visits HttpRequest method invocations and transforms them to use clientcore equivalents.
             * 
             * Handles transformations for:
             * - Body methods: setBody(String), setBody(byte[]), getBodyAsBinaryData()
             * - URL methods: setUrl(URL), getUrl()
             * - Header methods: setHeader(..)
             * - Utility methods: copy()
             */
            @Override
            public J visitMethodInvocation(J.MethodInvocation methodInvocation, ExecutionContext ctx) {
                J visited = super.visitMethodInvocation(methodInvocation, ctx);

                // === BODY-RELATED METHOD TRANSFORMATIONS ===
                
                // Transform setBody(String) to setBody(BinaryData.fromString(...))
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setBody(java.lang.String)");
                if (methodMatcher.matches((J.MethodInvocation) visited)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("setBody(BinaryData.fromString(#{any(java.lang.String)}))")
                        .imports("io.clientcore.core.models.binarydata.BinaryData")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), methodInvocation.getCoordinates().replaceMethod(), methodInvocation.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.models.binarydata.BinaryData");
                    return visited;
                }

                // Transform setBody(byte[]) to setBody(BinaryData.fromBytes(...))
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setBody(byte[])");
                if (methodMatcher.matches((J.MethodInvocation) visited, true)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("setBody(BinaryData.fromBytes(#{anyArray(byte)})")
                        .imports("io.clientcore.core.models.binarydata.BinaryData")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), ((J.MethodInvocation) visited).getCoordinates().replaceMethod(), ((J.MethodInvocation) visited).getArguments().toArray());
                    maybeAddImport("io.clientcore.core.models.binarydata.BinaryData");
                    return visited;
                }

                // Transform getBodyAsBinaryData() to getBody()
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest getBodyAsBinaryData()");
                if (methodMatcher.matches((J.MethodInvocation) visited, true)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("getBody()")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), ((J.MethodInvocation) visited).getCoordinates().replaceMethod());
                    return visited;
                }

                // === URL-RELATED METHOD TRANSFORMATIONS ===
                
                // Transform setUrl(URL) to setUri(URL.toURI())
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setUrl(java.net.URL)");
                if (methodMatcher.matches((J.MethodInvocation) visited, true)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder("setUri(#{any(java.net.URL)}.toURI())")
                        .imports("java.net.URI")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), methodInvocation.getCoordinates().replaceMethod(), methodInvocation.getArguments().toArray());
                    maybeAddImport("java.net.URI");
                    return visited;
                }

                // Transform getUrl() to getUri().toURL()
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest getUrl()");
                if (methodMatcher.matches((J.MethodInvocation) visited, true)) {
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder(String.format("%s.getUri().toURL()", ((J.MethodInvocation) visited).getSelect().toString()))
                        .imports("java.net.URL")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), ((J.MethodInvocation) visited).getCoordinates().replace());
                    maybeAddImport("java.net.URL");
                    return visited;
                }

                // === HEADER-RELATED METHOD TRANSFORMATIONS ===
                
                // Transform setHeader(..) to setHeaders(originalVar.getHeaders().set(...))
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest setHeader(..)");
                if (methodMatcher.matches((J.MethodInvocation) visited, true)) {
                    // Find the base variable identifier by traversing up the method chain
                    J variableIdentifier = findBaseVariableIdentifier((J.MethodInvocation) visited);
                    
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder(String.format("setHeaders(%s.getHeaders().set(#{any()},#{any()})", variableIdentifier))
                        .contextSensitive()
                        .imports("io.clientcore.core.http.models.HttpHeaderName", "io.clientcore.core.http.models.HttpHeaders")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), ((J.MethodInvocation) visited).getCoordinates().replaceMethod(), ((J.MethodInvocation) visited).getArguments().toArray());
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaderName");
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaders");
                    applyFormatting();
                    return visited;
                }

                // === UTILITY METHOD TRANSFORMATIONS ===
                
                // Transform copy() to create new HttpRequest with copied properties
                methodMatcher = new MethodMatcher("com.azure.core.http.HttpRequest copy()");
                if (methodMatcher.matches((J.MethodInvocation) visited, true)) {
                    // Find the base variable identifier by traversing up the method chain
                    J variableIdentifier = findBaseVariableIdentifier((J.MethodInvocation) visited);
                    
                    replacementTemplate = configuredParserJavaTemplateBuilder.getJavaTemplateBuilder(String.format("new HttpRequest()\n.setMethod(%1$s.getHttpMethod())\n.setUri(%1$s.getUri())\n.setHeaders(new HttpHeaders(%1$s.getHeaders()))\n.setBody(%1$s.getBody())", variableIdentifier))
                        .imports("io.clientcore.core.http.models.HttpMethod", "io.clientcore.core.http.models.HttpHeaders")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), ((J.MethodInvocation) visited).getCoordinates().replace());
                    maybeAddImport("io.clientcore.core.http.models.HttpMethod");
                    maybeAddImport("io.clientcore.core.http.models.HttpHeaders");
                    applyFormatting();
                    return visited;
                }

                return visited;
            }

        };
    }
}
