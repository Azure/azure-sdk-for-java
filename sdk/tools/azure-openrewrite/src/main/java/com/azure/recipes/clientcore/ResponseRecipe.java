package com.azure.recipes.core.v2;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeTree;

/**
 * ResponseRecipe changes all instances of Response (from azure core v1) to Response (from azure core v2).
 * This recipe also updates the import statements for the aforementioned class.
 * --------------------------------------------------
 * Before applying this recipe:
 * import com.azure.core.http.rest.Response;
 * --------------------------------------------------
 * After applying this recipe:
 * import io.clientcore.core.http.models.Response;
 * --------------------------------------------------
 * @author Ali Soltanian Fard Jahromi
 */
public class ResponseRecipe extends Recipe {
    /**
     * Method to return a simple short description of ResponseRecipe
     * @return A simple short description/name of the recipe
     */
    @Override
    public @NotNull String getDisplayName() {
        return "Update Response type to v2 version";
    }
    /**
     * Method to return a description of ResponseRecipe
     * @return A short description of the recipe
     */
    @Override
    public @NotNull String getDescription() {
        return "This recipe changes com.azure.core.http.rest.Response to io.clientcore.core.http.models.Response.";
    }
    /**
     * Method to return the visitor that visits the Response class
     * @return A TreeVisitor to visit the Response class and update it
     */
    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new UpdateResponseVisitor();
    }
    /**
     * Visitor to update Response
     */
    private static class UpdateResponseVisitor extends JavaIsoVisitor<ExecutionContext> {
        /**
         * Method to change com.azure.core.http.rest.Response to io.clientcore.core.http.models.Response
         */
        @Override
        public J.@NotNull FieldAccess visitFieldAccess(J.@NotNull FieldAccess fieldAccess, @NotNull ExecutionContext ctx) {
            J.FieldAccess visitedFieldAccess = super.visitFieldAccess(fieldAccess, ctx);
            String fullyQualified = visitedFieldAccess.getTarget() + "." + visitedFieldAccess.getSimpleName();
            if (fullyQualified.equals("com.azure.core.http.rest.Response")) {
                return TypeTree.build(" io.clientcore.core.http.models.Response");
            }
            return visitedFieldAccess;
        }
    }
}
