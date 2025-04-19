package com.azure.openrewrite.core.http.rest;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

public class RequestOptionsCustomRecipe extends Recipe {
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
                    replacementTemplate = JavaTemplate.builder("RequestContext.none()")
                            .imports("io.clientcore.core.http.models.RequestContext")
                            .build();

                    n = replacementTemplate.apply(updateCursor(newClass), newClass.getCoordinates().replace());

                }

                return n;
            }
        };
    }


}
