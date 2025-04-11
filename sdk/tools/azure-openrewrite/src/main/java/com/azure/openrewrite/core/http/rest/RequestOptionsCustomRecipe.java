package com.azure.openrewrite.core.http.rest;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaVisitor;
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
                // After: io.clientcore.core.http.models.RequestOptions <constructor>()
                newClass = (J.NewClass) super.visitNewClass(newClass, ctx);
                if (!newClass.getType().getFullyQualifiedName().equals("com.azure.core.http.rest.RequestOptions")) {
                    return super.visitNewClass(newClass, ctx);
                }
                J n = newClass.withType(newClass.getType().withFullyQualifiedName("io.clientcore.core.http.models.RequestOptions"));
                return n;
            }
        }
    }


}
