package com.azure.openrewrite.core.http.rest;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

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
 * Before: com.azure.core.http.rest.RequestOptions <constructor>()
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
                    replacementTemplate = templateBuilder.getJavaTemplateBuilder("RequestContext.none()")
                            .imports("io.clientcore.core.http.models.RequestContext")
                            .build();

                    n = replacementTemplate.apply(updateCursor(newClass), newClass.getCoordinates().replace());
                }

                return n;
            }
        };
    }


}
