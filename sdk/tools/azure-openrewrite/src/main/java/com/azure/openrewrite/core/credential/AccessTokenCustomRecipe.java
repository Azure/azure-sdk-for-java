package com.azure.openrewrite.core.credential;

import com.azure.openrewrite.util.ConfiguredParserJavaTemplateBuilder;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;

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
 * After: io.clientcore.core.credentials.oauth.AccessTokenType Duration.between(OffsetDateTime.now(), accessToken1.getExpiresAt())
 * </pre>
 */
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
        ConfiguredParserJavaTemplateBuilder javaTemplateBuilder = ConfiguredParserJavaTemplateBuilder.defaultBuilder();

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
                        .imports("io.clientcore.core.credentials.oauth.AccessTokenType")
                        .build();
                    n = replacementTemplate.apply(updateCursor(n), n.getCoordinates().replace(), n.getArguments().toArray());
                    maybeAddImport("io.clientcore.core.credentials.oauth.AccessTokenType");
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
                    replacementTemplate = javaTemplateBuilder.getJavaTemplateBuilder("getValue()")
                        .contextSensitive()
                        .build();
                    m = replacementTemplate.apply(updateCursor(m), m.getCoordinates().replaceMethod());
                    return m.withSelect(method).withPrefix(Space.EMPTY);
                }

                /*
                Before: com.azure.core.credential.AccessToken getDurationUntilExpiration()
                After: io.clientcore.core.credentials.oauth.AccessTokenType Duration.between(OffsetDateTime.now(), accessToken1.getExpiresAt())
                 */
                methodMatcher = new MethodMatcher("com.azure.core.credential.AccessToken getDurationUntilExpiration()");
                if (methodMatcher.matches(m)) {
                    replacementTemplate = javaTemplateBuilder.getJavaTemplateBuilder(String.format("Duration.between(OffsetDateTime.now(), %s.getExpiresAt())", m.getSelect()))
                        .imports("java.time.Duration")
                        .build();
                    m = replacementTemplate.apply(updateCursor(m), m.getCoordinates().replace());
                    maybeAddImport("java.time.Duration");
                    return m;
                }

                return m;
            }
        };
    }
}
