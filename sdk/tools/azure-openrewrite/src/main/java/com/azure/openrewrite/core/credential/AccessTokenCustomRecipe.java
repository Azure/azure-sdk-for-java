package com.azure.openrewrite.core.credential;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;

import com.azure.openrewrite.util.ConfiguredParserJavaTemplateBuilder;

/**
 * A custom OpenRewrite recipe to migrate the use of AccessToken.
 *
 * <p>This recipe performs the following transformations:</p>
 * <ul>
 *   <li>Replaces the constructor of AccessToken with a new constructor that uses AccessTokenType instead of String.</li>
 *   <li>Replaces the getTokenType() method invocation with getTokenType().value().</li>
 *   <li>Replaces the getDurationUntilExpiration() method invocation with a Duration calculation using OffsetDateTime.now() and getExpiresAt().</li>
 * </ul>
 *
 * <p>Example transformations:</p>
 * <pre>
 * Before: new AccessToken(String, OffsetDateTime, OffsetDateTime, String)
 * After: new AccessToken(String, OffsetDateTime, OffsetDateTime, AccessTokenType.fromString(String))
 *
 * Before: com.azure.core.credential.AccessToken getTokenType()
 * After: io.clientcore.core.credentials.oauth.AccessTokenType getTokenType().value()
 *
 * Before: com.azure.core.credential.AccessToken getDurationUntilExpiration()
 * After: io.clientcore.core.credentials.oauth.AccessTokenType Duration.between(OffsetDateTime.now(), accessToken.getExpiresAt())
 * </pre>
 */
public class AccessTokenCustomRecipe extends Recipe {

    /**
     * Default constructor for {@link AccessTokenCustomRecipe}.
     */
    public AccessTokenCustomRecipe() {
        super();
    }

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
        ConfiguredParserJavaTemplateBuilder javaTemplateBuilder = ConfiguredParserJavaTemplateBuilder.defaultBuilder();

        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext executionContext) {
                J.NewClass visitedNewClass = super.visitNewClass(newClass, executionContext);
                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;

                methodMatcher = new MethodMatcher("com.azure.core.credential.AccessToken <constructor>(java.lang.String, java.time.OffsetDateTime, java.time.OffsetDateTime, java.lang.String)");
                if (methodMatcher.matches(visitedNewClass)) {
                    replacementTemplate = javaTemplateBuilder.getJavaTemplateBuilder("new AccessToken(#{any(java.lang.String)}, #{any(java.time.OffsetDateTime)}, #{any(java.time.OffsetDateTime)}, AccessTokenType.fromString(#{any(java.lang.String)}))")
                        .imports("io.clientcore.core.credentials.oauth.AccessTokenType")
                        .build();
                    visitedNewClass = replacementTemplate.apply(updateCursor(visitedNewClass), visitedNewClass.getCoordinates().replace(), visitedNewClass.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.credentials.oauth.AccessTokenType");
                    return visitedNewClass;
                }

                return visitedNewClass;
            }

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                J.MethodInvocation visitedMethodInvocation = super.visitMethodInvocation(method, executionContext);
                MethodMatcher methodMatcher;
                JavaTemplate replacementTemplate;

                methodMatcher = new MethodMatcher("com.azure.core.credential.AccessToken getTokenType()");
                if (methodMatcher.matches(visitedMethodInvocation)) {
                    replacementTemplate = javaTemplateBuilder.getJavaTemplateBuilder("getValue()")
                        .contextSensitive()
                        .build();
                    visitedMethodInvocation = replacementTemplate.apply(updateCursor(visitedMethodInvocation), visitedMethodInvocation.getCoordinates().replaceMethod());
                    return visitedMethodInvocation.withSelect(method).withPrefix(Space.EMPTY);
                }

                methodMatcher = new MethodMatcher("com.azure.core.credential.AccessToken getDurationUntilExpiration()");
                if (methodMatcher.matches(visitedMethodInvocation)) {
                    replacementTemplate = javaTemplateBuilder.getJavaTemplateBuilder(String.format("Duration.between(OffsetDateTime.now(), %s.getExpiresAt())", visitedMethodInvocation.getSelect()))
                        .imports("java.time.Duration")
                        .build();
                    visitedMethodInvocation = replacementTemplate.apply(updateCursor(visitedMethodInvocation), visitedMethodInvocation.getCoordinates().replace());
                    maybeAddImport("java.time.Duration");
                    return visitedMethodInvocation;
                }

                return visitedMethodInvocation;
            }
        };
    }
}
