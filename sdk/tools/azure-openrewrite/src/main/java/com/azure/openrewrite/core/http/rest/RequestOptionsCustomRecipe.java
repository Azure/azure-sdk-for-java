package com.azure.openrewrite.core.http.rest;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Flag;
import org.openrewrite.java.tree.J;

import com.azure.openrewrite.util.ConfiguredParserJavaTemplateBuilder;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;

/**
 * A custom OpenRewrite recipe to migrate the use of RequestOptions.
 *
 * <p>This recipe performs the following transformation:</p>
 * <ul>
 *   <li>Replaces the constructor of RequestOptions with a static method invocation of RequestContext.none().</li>
 * </ul>
 *
 * <p>Example transformation:</p>
 * <pre>
 * Before: com.azure.core.http.rest.RequestOptions &lt;constructor&gt;()
 * After: io.clientcore.core.http.models.RequestContext.none()
 * </pre>
 */
public class RequestOptionsCustomRecipe extends Recipe {

    /**
     * Default constructor for {@link RequestOptionsCustomRecipe}.
     */
    public RequestOptionsCustomRecipe() {
        super();
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        ConfiguredParserJavaTemplateBuilder templateBuilder = ConfiguredParserJavaTemplateBuilder.defaultBuilder();
        final String FLUENT_KEY = "NewClassFluent";
        return new JavaIsoVisitor<ExecutionContext>() {

            @Override
            public @Nullable J visit(@Nullable Tree tree, ExecutionContext executionContext) {
                doAfterVisit(new ConvertToImmutableVisitor());
                return super.visit(tree, executionContext);
            }

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                J.MethodInvocation visitedInvocation = super.visitMethodInvocation(method, executionContext);
                ConfiguredParserJavaTemplateBuilder templateBuilder = ConfiguredParserJavaTemplateBuilder.defaultBuilder();
                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;

                //Before: com.azure.core.http.rest.RequestOptions setHeader(String, String)
                //After: io.clientcore.core.http.models.RequestContext#Builder addHeader(HttpHeaderName, String)
                methodMatcher = new MethodMatcher("com.azure.core.http.rest.RequestOptions addHeader(java.lang.String, java.lang.String)");
                if (methodMatcher.matches(visitedInvocation)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("addHeader(HttpHeaderName.fromString(#{any(java.lang.String)}), #{any(java.lang.String)})")
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    visitedInvocation = replacementTemplate.apply(getCursor(), visitedInvocation.getCoordinates().replaceMethod(), visitedInvocation.getArguments().toArray());
                    return visitedInvocation;
                }

                return visitedInvocation;
            }
        };
    }

    private static class ConvertToImmutableVisitor extends JavaVisitor<ExecutionContext> {
        ConfiguredParserJavaTemplateBuilder templateBuilder = ConfiguredParserJavaTemplateBuilder.defaultBuilder();
        final String FLUENT_KEY = "NewClassFluent";

        @Override
        public @Nullable J visit(@Nullable Tree tree, ExecutionContext executionContext) {
            Recipe formattingRecipe = new org.openrewrite.java.format.AutoFormat();
            //doAfterVisit(formattingRecipe.getVisitor());
            return super.visit(tree, executionContext);
        }

        @Override
        public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            // replace RequestOptions constructor with RequestContext static method
            // Before: com.azure.core.http.rest.RequestOptions <constructor>()
            // After: io.clientcore.core.http.models.RequestContext none()
            J n = (J.NewClass) super.visitNewClass(newClass, ctx);
            MethodMatcher methodMatcher;
            JavaTemplate replacementTemplate;


            methodMatcher = new MethodMatcher("com.azure.core.http.rest.RequestOptions <constructor>()");
            if (methodMatcher.matches((J.NewClass) n)) {
                boolean isFluent = ctx.getMessage(FLUENT_KEY) != null
                    && (Boolean) ctx.getMessage(FLUENT_KEY);

                if (isFluent) {
                    // If the new class is a fluent API, we need to replace it with a fluent API
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("RequestContext.builder()")
                        .imports("io.clientcore.core.http.models.RequestContext")
                        .build();
                    ctx.putMessage(FLUENT_KEY, null);
                    n = replacementTemplate.apply(updateCursor(n), newClass.getCoordinates().replace());
                } else {
                    // If the new class is not a fluent API, we need to replace it with a non-fluent API
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("RequestContext.none()")
                        .imports("io.clientcore.core.http.models.RequestContext")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), newClass.getCoordinates().replace());
                }

            }

            return n;
        }

        @Override
        public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            // replace RequestOptions constructor with RequestContext static method
            // Before: com.azure.core.http.rest.RequestOptions <constructor>()
            // After: io.clientcore.core.http.models.RequestContext none()
            J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);
            boolean isFluent = isFluentChainMethod(method);

            // reassign variable since RequestContext is immutable
            JavaType.Method methodType = method.getMethodType();
            if (methodType != null &&
                !isStaticMethod(method) &&
                methodType.getReturnType() == methodType.getDeclaringType() &&
                (method.getSelect() instanceof J.Identifier)) {
                J.Assignment reassignment = doVariableReassignment(m);
                return reassignment;
            }

            if (method.getSelect() != null && method.getSelect() instanceof J.NewClass) {
                ctx.putMessage(FLUENT_KEY, true);
                J.MethodInvocation invokeBuilder = (J.MethodInvocation) visit(method.getSelect(), ctx);
                m = m.withSelect(invokeBuilder);
                return m;
            }

            if (isFluent) {
                m = invokeBuildOnBuilderChain(m, updateCursor(m));
                return m;
            }


            return m;
        }

        private boolean isStaticMethod(J.MethodInvocation methodInvocation) {
            // Returns true if the method is a static method.
            // This is determined by checking if the method has a return type of the same class as the method being called.
            JavaType.Method methodType = methodInvocation.getMethodType();
            if (methodType == null) {
                return false;
            }

            return methodType.getFlags().contains(Flag.Static);
        }

        private J.Assignment doVariableReassignment(J.MethodInvocation methodInvocation) {
            // add toBuilder() and build() call
            String adjustmentString = methodInvocation.toString().replace(methodInvocation.getSelect().toString(),
                methodInvocation.getSelect().toString() + ".toBuilder()");
            JavaTemplate addBuilderTemplate = templateBuilder.getJavaTemplateBuilder(adjustmentString)
                .imports("io.clientcore.core.http.models.RequestContext")
                .build();
            J.MethodInvocation adjustedMethodInvocation = addBuilderTemplate.apply(getCursor(), methodInvocation.getCoordinates().replace());
            Cursor cursor = new Cursor(getCursor().getParentTreeCursor(), adjustedMethodInvocation);
            adjustedMethodInvocation = invokeBuildOnBuilderChain(adjustedMethodInvocation, cursor);

            J.Identifier variableName = (J.Identifier) methodInvocation.getSelect();
            String code = String.format("%s = %s", variableName.getSimpleName(), adjustedMethodInvocation);
            JavaTemplate replacementTemplate = templateBuilder.getJavaTemplateBuilder(code)
                .imports("io.clientcore.core.http.models.RequestContext")
                .build();
            return replacementTemplate.apply(updateCursor(methodInvocation), methodInvocation.getCoordinates().replace());
        }

        private boolean isFluentChainMethod(J.MethodInvocation methodInvocation) {
            // Returns true if the method is part a fluent API chain.
            // This is determined by checking if the method has a return type of the same class as the method being called.
            JavaType.Method methodType = methodInvocation.getMethodType();
            if (methodType == null) {
                return false;
            }
            return methodType.getReturnType() == methodType.getDeclaringType() && methodInvocation.getSelect() instanceof J.MethodInvocation;
        }

        // assumes that the method is a fluent API. Do not call on non-fluent APIs.
        private J.MethodInvocation invokeBuildOnBuilderChain(J.MethodInvocation methodInvocation, Cursor cursor) {
            // Returns true if the method is the end of a fluent call chain.
            // This is determined by checking if the method has a return type of the same class as the method being called.

            if (methodInvocation.getSelect() instanceof J.MethodInvocation && !(cursor.getParentTreeCursor().getValue() instanceof J.MethodInvocation)) {
                JavaTemplate replacementTemplate = templateBuilder.getJavaTemplateBuilder("build()")
                    .imports("io.clientcore.core.http.models.RequestContext")
                    .build();
                J.MethodInvocation invokeBuild = replacementTemplate.apply(cursor, methodInvocation.getCoordinates().replace());
                return invokeBuild.withSelect(methodInvocation.withPrefix(Space.EMPTY));
            }
            return methodInvocation;
        }
    }


}
