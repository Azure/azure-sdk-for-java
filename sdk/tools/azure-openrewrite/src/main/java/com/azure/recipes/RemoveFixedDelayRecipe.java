package com.azure.recipes;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

/**
 * RemoveFixedDelayRecipe removes any leftover imports for FixedDelayOptions
 * and any variables declared with the FixedDelayOptions type.
 * --------------------------------------------------
 * Removes code such as:
 * import com.azure.core.http.policy.FixedDelayOptions;
 * ...
 * FixedDelayOptions s;
 * --------------------------------------------------
 * @author Ali Soltanian Fard Jahromi
 */
public class RemoveFixedDelayRecipe extends Recipe {
    /**
     * Method to return a simple short description of RemoveFixedDelayRecipe
     * @return A simple short description/name of the recipe
     */
    @Override
    public @NotNull String getDisplayName() {
        return "Removes imports and variable declarations for FixedDelayOptions";
    }
    /**
     * Method to return a description of RemoveFixedDelayRecipe
     * @return A short description of the recipe
     */
    @Override
    public @NotNull String getDescription() {
        return "This recipe removes any leftover imports and variables using FixedDelayOptions.";
    }
    /**
     * Method to return the visitor that visits the usages of FixedDelayOptions
     * @return A TreeVisitor to visit the usages of FixedDelayOptions
     */
    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new FixedDelayVisitor();
    }
    /**
     * Visitor to remove FixedDelayOptions
     */
    private static class FixedDelayVisitor extends JavaIsoVisitor<ExecutionContext> {
        /**
         * Method to remove unnecessary import for FixedDelay
         */
        @Override
        public J.Import visitImport(J.Import _import, ExecutionContext executionContext) {
            J.Import visitedImport = super.visitImport(_import, executionContext);
            if (visitedImport.getQualid() != null){
                if (visitedImport.getQualid().getSimpleName().contains("FixedDelay")){
                    return null;
                }
            }
            return visitedImport;
        }
        /**
         * Method to remove unnecessary variable declarations for FixedDelay
         */
        @Override
        public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, ExecutionContext executionContext) {
            J.VariableDeclarations visitedVar = super.visitVariableDeclarations(multiVariable, executionContext);
            if (visitedVar.getTypeExpression() == null) {
                return visitedVar;
            }
            if (visitedVar.getTypeExpression().toString().contains("FixedDelayOptions")) {
                // Return null to remove the block
                return null;
            }
            return visitedVar;
        }
    }
}
