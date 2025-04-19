package com.azure.openrewrite.core.credential;

import com.azure.openrewrite.util.ConfiguredParserJavaTemplateBuilder;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

public class AccessTokenCustomRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "AccessToken Custom Recipe";
    }

    @Override
    public String getDescription() {
        return "Custom recipe to migrate the use of AccessToken.";
    }

    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        ConfiguredParserJavaTemplateBuilder javaTemplateBuilder = new ConfiguredParserJavaTemplateBuilder(
            JavaParser.fromJavaVersion().classpath("azure-core", "core")
        );

        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext executionContext) {
                J.NewClass n = super.visitNewClass(newClass, executionContext);
                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;

                /*
                Before: <constructor> AccessToken(String, OffsetDateTime, OffsetDateTime, String)
                After: <constructor> AccessToken(String, OffsetDateTime, OffsetDateTime, AccessTokenType)
                 */
                methodMatcher = new MethodMatcher("com.azure.core.credential.AccessToken <constructor>(java.lang.String, java.time.OffsetDateTime, java.time.OffsetDateTime, java.lang.String)");
                if (methodMatcher.matches(n)) {
                    replacementTemplate = javaTemplateBuilder.getJavaTemplateBuilder("new AccessToken(#{any(java.lang.String)}, #{any(java.time.OffsetDateTime)}, #{any(java.time.OffsetDateTime)}, AccessTokenType.fromString(#{any(java.lang.String)}))")
                        .imports("com.azure.core.credential.AccessTokenType")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), n.getCoordinates().replace(), n.getArguments().toArray());
                    maybeAddImport("com.azure.core.credential.AccessTokenType");
                    return n;
                }

                return n;
            }

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                J.MethodInvocation m = super.visitMethodInvocation(method, executionContext);
                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;

                /*
                Before: com.azure.core.credential.AccessToken getTokenType()
                After: io.clientcore.core.credentials.oauth.AccessTokenType getTokenType().value()
                 */
                methodMatcher = new MethodMatcher("com.azure.core.credential.AccessToken getTokenType()");
                if (methodMatcher.matches(m)) {
                    replacementTemplate = javaTemplateBuilder.getJavaTemplateBuilder("value()")
                        .contextSensitive()
                        .build();
                    m.getSideEffects().add(replacementTemplate.apply(updateCursor(m), m.getCoordinates().replaceMethod()));
                    return m;
                }

                return m;
            }
        };
    }
}
