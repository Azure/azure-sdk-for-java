package com.azure.openrewrite.core.http.rest;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
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
    public JavaVisitor<ExecutionContext> getVisitor() {
        ConfiguredParserJavaTemplateBuilder templateBuilder = ConfiguredParserJavaTemplateBuilder.defaultBuilder();
        final String FLUENT_KEY = "NewClassFluent";
        return new JavaVisitor<ExecutionContext>() {

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
                        n = replacementTemplate.apply(getCursor(), newClass.getCoordinates().replace());
                    } else {
                        // If the new class is not a fluent API, we need to replace it with a non-fluent API
                        replacementTemplate = templateBuilder.getJavaTemplateBuilder("RequestContext.none()")
                                .imports("io.clientcore.core.http.models.RequestContext")
                                .build();
                        n = replacementTemplate.apply(updateCursor(newClass), newClass.getCoordinates().replace());
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
                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;
                boolean isFluent = isFluentChainMethod(method);
                Recipe formatter = new org.openrewrite.java.format.TabsAndIndents();
                doAfterVisit(formatter.getVisitor());

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
                J.Identifier variableName = (J.Identifier) methodInvocation.getSelect();
                String code = String.format("%s = %s", variableName.getSimpleName(), methodInvocation.toString());
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
                            .contextSensitive()
                            .build();
                    J.MethodInvocation invokeBuild = replacementTemplate.apply(updateCursor(methodInvocation), methodInvocation.getCoordinates().replace());
                    return invokeBuild.withSelect(methodInvocation);
                }
                return methodInvocation;
            }
        };
    }


}
