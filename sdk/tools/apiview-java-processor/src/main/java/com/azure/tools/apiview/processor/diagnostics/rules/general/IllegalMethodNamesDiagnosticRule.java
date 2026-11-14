// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.ApiListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getClassName;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedMethods;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.ERROR;

/**
 * Checks illegal method names conventions.
 */

public class IllegalMethodNamesDiagnosticRule implements DiagnosticRule {

    private final List<Rule> rules;

    /**
     * Creates an illegal method names diagnostic rule.
     *
     * @param rules the rules
     * @throws IllegalArgumentException if the operation cannot be completed
     */
    public IllegalMethodNamesDiagnosticRule(Rule... rules) {
        if (rules == null || rules.length == 0) {
            throw new IllegalArgumentException(
                "IllegalMethodNamesDiagnosticRule created with no illegal method name rules.");
        }
        this.rules = Arrays.asList(rules);
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final ApiListing listing) {
        getClassName(cu).ifPresent(className -> getPublicOrProtectedMethods(cu).forEach(method -> {
            final String methodName = method.getNameAsString();
            for (Rule rule : rules) {
                if (!rule.check(className, methodName)) {
                    listing.addDiagnostic(new Diagnostic(ERROR, makeId(method),
                        "Method '" + methodName + "' is using an illegal method name."));
                }
            }
        }));
    }

    /**
     * A class or method name pattern used by a naming diagnostic.
     */
    public static class Rule {
        private final Pattern classNamePattern;
        private final Pattern methodNamePattern;

        /**
         * Creates a rule.
         *
         * @param methodNamePattern the method name pattern
         */
        public Rule(String methodNamePattern) {
            this(null, methodNamePattern);
        }

        /**
         * Creates a rule.
         *
         * @param classNamePattern the class name pattern
         * @param methodNamePattern the method name pattern
         */
        public Rule(String classNamePattern, String methodNamePattern) {
            this.classNamePattern = classNamePattern == null ? null : Pattern.compile(classNamePattern);
            this.methodNamePattern = methodNamePattern == null ? null : Pattern.compile(methodNamePattern);
        }

        /**
         * Checks the declaration against this rule.
         *
         * @param className the class name
         * @param methodName the method name
         * @return true if the method name is allowed
         */
        public boolean check(String className, String methodName) {
            final boolean methodNameMatches = methodNamePattern.matcher(methodName).matches();

            if (classNamePattern != null) {
                // in this branch we are checking for a match on both class name and method name.
                // if there is a match, we consider that the API is illegal and return false to
                // indicate it is not accepted and a warning should be returned to the user.
                if (classNamePattern.matcher(className).matches()) {
                    // the class name matches, now check if the method name matches
                    if (methodNameMatches) {
                        return false;
                    }
                }
            } else {
                // we have no class name pattern, so we only check for a match on the method name pattern
                if (methodNameMatches) {
                    return false;
                }
            }

            // return true here to indicate that we are happy
            return true;
        }
    }
}
