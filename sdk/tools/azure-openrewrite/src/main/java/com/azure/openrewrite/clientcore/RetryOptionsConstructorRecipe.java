// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.clientcore;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeTree;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
/**
 * RetryOptionsRecipe changes RetryOptions constructor to HttpRetryOptions constructor.
 * It also removes any references to FixedDelay and ExponentialDelay and changes
 * com.azure.core.http.policy.RetryOptions to io.clientcore.core.http.models.HttpRetryOptions
 * --------------------------------------------------
 * Before applying this recipe:
 * import com.azure.core.http.policy.RetryOptions;
 * ...
 * new RetryOptions(new FixedDelayOptions(3, Duration.ofMillis(50)))
 * --------------------------------------------------
 * After applying this recipe:
 * import io.clientcore.core.http.models.HttpRetryOptions;
 * ...
 * new HttpRetryOptions(3, Duration.ofMillis(50))
 * --------------------------------------------------
 */
public class RetryOptionsConstructorRecipe extends Recipe {
    /**
     * Method to return a simple short description of RetryOptionsRecipe
     * @return A simple short description/name of the recipe
     */
    @Override
    public String getDisplayName() {
        return "Change RetryOptions constructor";
    }
    /**
     * Method to return a description of RetryOptionsRecipe
     * @return A short description of the recipe
     */
    @Override
    public String getDescription() {
        return "This recipe changes the constructor for RetryOptions to HttpRetryOptions.\n" +
                "This includes removing any references to FixedDelay and ExponentialDelay and changing\n" +
                " * com.azure.core.http.policy.RetryOptions to io.clientcore.core.http.models.HttpRetryOptions.";
    }
    /**
     * Method to return the visitor that changes RetryOptions constructor to HttpRetryOptions constructor
     * @return A TreeVisitor to change RetryOptions constructor to HttpRetryOptions constructor
     */
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RetryVisitor();
    }
    /**
     * Visitor to change RetryOptions constructor to HttpRetryOptions constructor
     */
    private static class RetryVisitor extends JavaIsoVisitor<ExecutionContext> {

        private final Map<String, List<Expression>> variableToArgsMap = new HashMap<>();

        /**
         * Method to visit variable declaration for FixedDelay or ExponentialDelay
         */
        @Override
        public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations variableDeclarations, ExecutionContext executionContext) {
            J.VariableDeclarations vd = super.visitVariableDeclarations(variableDeclarations, executionContext);
            for (J.VariableDeclarations.NamedVariable variable : vd.getVariables()) {
                J.NewClass newClass = null;
                try {
                    newClass = (J.NewClass) variable.getInitializer();
                } catch (Exception e) {
                    return vd;
                }
                if (newClass != null) {
                    if (newClass.getType() != null) {
                        String className = newClass.getType().toString();
                        if (className.contains("FixedDelayOptions") || className.contains("ExponentialDelayOptions")) {
                            List<Expression> args = new ArrayList<>(newClass.getArguments());
                            variableToArgsMap.put(variable.getSimpleName(), args);
                        }
                    }
                }
            }
            return vd;
        }

        /**
         * Method to visit constructor for RetryOptions
         */
        @Override
        public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext executionContext) {
            J.NewClass visitedNewClass = super.visitNewClass(newClass, executionContext);
            if (visitedNewClass.toString().contains("new HttpRetryOptions")) {
                if (visitedNewClass.getArguments().size() == 1) {
                    Expression constructorArg = visitedNewClass.getArguments().get(0);
                    if (constructorArg instanceof J.Identifier) {
                        String variableName = ((J.Identifier) constructorArg).getSimpleName();
                        List<Expression> args = variableToArgsMap.get(variableName);
                        if (args != null) {
                            return visitedNewClass.withArguments(args);
                        }
                    } else if (constructorArg instanceof J.NewClass) {
                        J.NewClass newArg = (J.NewClass) constructorArg;
                        List<Expression> args = new ArrayList<>(newArg.getArguments());
                        return visitedNewClass.withArguments(args);
                    }
                }
            }
            return visitedNewClass;
        }

        /**
         * Method to change RetryOptions to HttpRetryOptions
         */
        @Override
        public J.Identifier visitIdentifier(J.Identifier identifier, ExecutionContext ctx) {
            J.Identifier visitedIdentifier = super.visitIdentifier(identifier, ctx);
            if (visitedIdentifier.getSimpleName().equals("RetryOptions")) {
                return visitedIdentifier.withSimpleName("HttpRetryOptions");
            }
            return visitedIdentifier;
        }

        /**
         * Method to change import to HttpRetryOptions
         */
        @Override
        public J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext ctx) {
            J.FieldAccess visitedFieldAccess = super.visitFieldAccess(fieldAccess, ctx);
            String fullyQualified = visitedFieldAccess.getTarget() + "." + visitedFieldAccess.getSimpleName();
            if (fullyQualified.equals("com.azure.core.http.policy.HttpRetryOptions")) {
                return TypeTree.build(" io.clientcore.core.http.models.HttpRetryOptions");
            }
            return visitedFieldAccess;
        }

        /**
         * Method to change usages of retryOptions builder method to httpRetryOptions
         */
        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
            J.MethodInvocation visitedMethodInv = super.visitMethodInvocation(method, executionContext);
            if (visitedMethodInv.getSimpleName().equals("retryOptions")) {
                return visitedMethodInv.withName(visitedMethodInv.getName().withSimpleName("httpRetryOptions"));
            }
            return visitedMethodInv;
        }
    }
}
