// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.clientcore;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeTree;

/**
 * ContextRecipe changes all instances of Context.NONE (from azure core v1) to Context.none() (from azure core v2).
 * This recipe also updates the import statements for the aforementioned class.
 * --------------------------------------------------
 * Before applying this recipe:
 * import com.azure.core.util.Context;
 * com.azure.core.http.rest.RequestOptions;
 * ...
 * public void context(){ print(Context.NONE); }
 * --------------------------------------------------
 * After applying this recipe:
 * import io.clientcore.core.util.Context;
 * io.clientcore.core.http.models.RequestOptions
 * ...
 * public void context(){ print(Context.none()); }
 * --------------------------------------------------
 * @author Ali Soltanian Fard Jahromi
 */
public class ContextRecipe extends Recipe {
    /**
     * Method to return a simple short description of ContextRecipe
     * @return A simple short description/name of the recipe
     */
    @Override
    public @NotNull String getDisplayName() {
        return "Change static field 'Context.NONE' to Method 'Context.none()'";
    }
    /**
     * Method to return a description of ContextRecipe
     * @return A short description of the recipe
     */
    @Override
    public @NotNull String getDescription() {
        return "This recipe changes any calls to Context.NONE to Context.none().\n" +
                "It also changes the import statement of com.azure.core.util.Context to io.clientcore.core.util.Context.";
    }
    /**
     * Method to return the visitor that visits the Context.NONE identifier
     * @return A TreeVisitor to visit the NONE identifier and change it to none()
     */
    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ChangeStaticFieldToMethodVisitor();
    }
    /**
     * Visitor to change NONE identifier to none()
     */
    private static class ChangeStaticFieldToMethodVisitor extends JavaIsoVisitor<ExecutionContext> {
        /**
         * Method to change com.azure.core.util.Context to io.clientcore.core.util.Context
         */
        @Override
        public J.@NotNull FieldAccess visitFieldAccess(J.@NotNull FieldAccess fieldAccess, @NotNull ExecutionContext ctx) {
            J.FieldAccess visitedFieldAccess = super.visitFieldAccess(fieldAccess, ctx);
            String fullyQualified = visitedFieldAccess.getTarget() + "." + visitedFieldAccess.getSimpleName();
            if (fullyQualified.equals("com.azure.core.http.rest.RequestOptions")) {
                return TypeTree.build(" io.clientcore.core.http.models.RequestOptions");
            }
            if (fullyQualified.equals("com.azure.core.util.Context")) {
                return TypeTree.build(" io.clientcore.core.util.Context");
            }
            if (fullyQualified.equals("Context.NONE")){
                return TypeTree.build("Context.none()");
            }
            return visitedFieldAccess;
        }
    }
}
