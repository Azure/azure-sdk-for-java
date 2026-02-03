package com.azure.openrewrite.core.http.rest;

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
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;

import com.azure.openrewrite.util.ConfiguredParserJavaTemplateBuilder;

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

    public static final String FULLY_QUALIFIED_NAME = "com.azure.core.http.rest.RequestOptions";

    /**
     * Default constructor for {@link RequestOptionsCustomRecipe}.
     */
    public RequestOptionsCustomRecipe() {
        super();
    }

    @Override
    public String getDisplayName() {
        return "RequestOptions Custom Recipe";
    }

    @Override
    public String getDescription() {
        return "Migrates the usage of RequestOptions to RequestContext.";
    }

    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        ConfiguredParserJavaTemplateBuilder templateBuilder = ConfiguredParserJavaTemplateBuilder.defaultBuilder();
        final String FLUENT_KEY = "NewClassFluent";
        final String FULLY_QUALIFIED_NAME = "com.azure.core.http.rest.RequestOptions";

        return new JavaIsoVisitor<ExecutionContext>() {

            @Override
            public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext executionContext) {
                doAfterVisit(new ConvertToImmutableVisitor());
                return super.visitCompilationUnit(cu, executionContext);
            }

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                J.MethodInvocation visitedMethodInvocation = super.visitMethodInvocation(method, executionContext);
                ConfiguredParserJavaTemplateBuilder templateBuilder = ConfiguredParserJavaTemplateBuilder.defaultBuilder();
                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;

                methodMatcher = new MethodMatcher("com.azure.core.http.rest.RequestOptions setHeader(java.lang.String, java.lang.String)");
                if (methodMatcher.matches(visitedMethodInvocation)) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("setHeader(HttpHeaderName.fromString(#{any(java.lang.String)}), #{any(java.lang.String)})")
                        .imports("io.clientcore.core.http.models.HttpHeaderName")
                        .build();
                    visitedMethodInvocation = replacementTemplate.apply(getCursor(), visitedMethodInvocation.getCoordinates().replaceMethod(), visitedMethodInvocation.getArguments().toArray());
                    return visitedMethodInvocation;
                }

                return visitedMethodInvocation;
            }
        };
    }

    /**
     * A visitor that converts the usage of RequestOptions to RequestContext.
     */
    private static class ConvertToImmutableVisitor extends JavaVisitor<ExecutionContext> {
        ConfiguredParserJavaTemplateBuilder templateBuilder = ConfiguredParserJavaTemplateBuilder.defaultBuilder();
        final String FLUENT_KEY = "fluentConstructor";

        @Override
        public J visit(Tree tree, ExecutionContext executionContext) {
            return super.visit(tree, executionContext);
        }

        @Override
        public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            J visited = super.visitNewClass(newClass, ctx);
            MethodMatcher methodMatcher;
            JavaTemplate replacementTemplate;

            JavaType.Method constructorType = newClass.getConstructorType();
            if (constructorType == null) {
                return visited;
            }

            if (!constructorType.getDeclaringType().getFullyQualifiedName().equals(FULLY_QUALIFIED_NAME)) {
                return visited;
            }

            methodMatcher = new MethodMatcher("com.azure.core.http.rest.RequestOptions <constructor>()");
            if (methodMatcher.matches((J.NewClass) visited)) {
                boolean isFluent = ctx.getMessage(FLUENT_KEY) != null
                    && (Boolean) ctx.getMessage(FLUENT_KEY);

                if (isFluent) {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("RequestContext.builder()")
                        .imports("io.clientcore.core.http.models.RequestContext")
                        .build();
                    ctx.putMessage(FLUENT_KEY, null);
                    visited = replacementTemplate.apply(updateCursor(visited), newClass.getCoordinates().replace());
                } else {
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("RequestContext.none()")
                        .imports("io.clientcore.core.http.models.RequestContext")
                        .build();
                    visited = replacementTemplate.apply(updateCursor(visited), newClass.getCoordinates().replace());
                }

            }

            return visited;
        }

        @Override
        public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {

            JavaType.Method methodType = method.getMethodType();
            if (methodType == null) {
                return super.visitMethodInvocation(method, ctx);
            }

            if (!methodType.getDeclaringType().getFullyQualifiedName().equals(FULLY_QUALIFIED_NAME)) {
                return super.visitMethodInvocation(method, ctx);
            }

            J.MethodInvocation visited = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);
            boolean isFluent = isFluentChainMethod(method);
            if (!isStaticMethod(method) &&
                methodType.getReturnType() == methodType.getDeclaringType() &&
                (method.getSelect() instanceof J.Identifier)) {
                return doVariableReassignment(visited);
            }

            if (method.getSelect() != null && method.getSelect() instanceof J.NewClass) {
                ctx.putMessage(FLUENT_KEY, true);
                J.MethodInvocation invokeBuilder = (J.MethodInvocation) visit(method.getSelect(), ctx);
                visited = visited.withSelect(invokeBuilder);
                return visited;
            }

            if (isFluent) {
                visited = invokeBuildOnBuilderChain(visited, updateCursor(visited));
                return visited;
            }


            return visited;
        }

        private boolean isStaticMethod(J.MethodInvocation methodInvocation) {
            JavaType.Method methodType = methodInvocation.getMethodType();
            if (methodType == null) {
                return false;
            }

            return methodType.getFlags().contains(Flag.Static);
        }

        private J.Assignment doVariableReassignment(J.MethodInvocation methodInvocation) {
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
            JavaType.Method methodType = methodInvocation.getMethodType();
            if (methodType == null) {
                return false;
            }
            return methodType.getReturnType() == methodType.getDeclaringType() && methodInvocation.getSelect() instanceof J.MethodInvocation;
        }

        private J.MethodInvocation invokeBuildOnBuilderChain(J.MethodInvocation methodInvocation, Cursor cursor) {
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
