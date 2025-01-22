package com.azure.recipes.core.v2;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeTree;

/**
 * HttpLogOptionsRecipe change usage of the com.azure.core.http.policy.HttpLogDetailLevel while also changing
 * the com.azure.core.http.policy.HttpLogOptions Type.
 * The import statements are also updated.
 * Changes:
 * com.azure.core.http.policy.HttpLogDetailLevel -> io.clientcore.core.http.models.HttpLogOptions.HttpLogDetailLevel
 * com.azure.core.http.policy.HttpLogOptions     -> io.clientcore.core.http.models.HttpLogOptions
 * --------------------------------------------------
 * Before applying this recipe:
 * import com.azure.core.http.policy.HttpLogDetailLevel;
 * import com.azure.core.http.policy.HttpLogOptions;
 * ...
 * public void logOptions(){ print(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)); }
 * --------------------------------------------------
 * After applying this recipe:
 * import io.clientcore.core.http.models.HttpLogOptions;
 * ...
 * public void logOptions(){ print(new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY_AND_HEADERS)); }
 * --------------------------------------------------
 * @author Ali Soltanian Fard Jahromi
 */
public class HttpLogOptionsRecipe extends Recipe {
    /**
     * Method to return a simple short description of HttpLogOptionsRecipe
     * @return A simple short description/name of the recipe
     */
    @Override
    public @NotNull String getDisplayName() {
        return "Migrate the HttpLogOptions and HttpLogDetail usages";
    }
    /**
     * Method to return a description of HttpLogOptionsRecipe
     * @return A short description of the recipe
     */
    @Override
    public @NotNull String getDescription() {
        return "This recipe changes any usages of HttpLogOptions from azure core v1 to its respective type from azure core v2.\n" +
                "It also migrates any usages of HttpLogDetailLevel to azure core v2.";
    }
    /**
     * Method to return the visitor that visits the usages of HttpLogOptions and HttpLogDetailLevel
     * @return A TreeVisitor to visit the usages of HttpLogOptions and HttpLogDetailLevel
     */
    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new HttpLogOptionsVisitor();
    }
    /**
     * Visitor to change HttpLogOptions type and change usage of HttpLogDetailLevel
     */
    private static class HttpLogOptionsVisitor extends JavaIsoVisitor<ExecutionContext> {
        /**
         * Method to change usage of the HttpLogDetailLevel while also changing the HttpLogOptions Type
         */
        @Override
        public J.@NotNull FieldAccess visitFieldAccess(J.@NotNull FieldAccess fieldAccess, @NotNull ExecutionContext ctx) {
            J.FieldAccess visitedFieldAccess = super.visitFieldAccess(fieldAccess, ctx);
            String fullyQualified = visitedFieldAccess.getTarget() + "." + visitedFieldAccess.getSimpleName();
            if (fullyQualified.equals("com.azure.core.http.policy.HttpLogOptions")) {
               return TypeTree.build(" io.clientcore.core.http.models.HttpLogOptions");
            }
            if (fullyQualified.equals("com.azure.core.http.policy.HttpLogDetailLevel") &&
                    visitedFieldAccess.getSimpleName().equals("HttpLogDetailLevel") &&
                    visitedFieldAccess.print().contains("com.azure")){
                return TypeTree.build(" io.clientcore.core.http.models.HttpLogOptions.HttpLogDetailLevel");
            }
            return visitedFieldAccess;
        }
        /**
         * Method to remove unnecessary import fo HttpLogDetailLevel as the class is already included in client-core HttpLogOptions class
         */
        @Override
        public J.Import visitImport(J.Import _import, ExecutionContext executionContext) {
            J.Import visitedImport = super.visitImport(_import, executionContext);
            if (visitedImport.getQualid().getSimpleName().contains("HttpLogDetailLevel")){
                return null;
            }
            return visitedImport;
        }
    }
}
