// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.ApiListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getClassName;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedMethods;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.WARNING;

/**
 * Checks upper case naming conventions.
 */
public class UpperCaseNamingDiagnosticRule implements DiagnosticRule {

    private final List<String> illegalNames;

    /**
     * Creates an upper case naming diagnostic rule.
     *
     * @param illegalNames the illegal names
     * @throws IllegalArgumentException if the operation cannot be completed
     */
    public UpperCaseNamingDiagnosticRule(String... illegalNames) {
        if (illegalNames == null || illegalNames.length == 0) {
            throw new IllegalArgumentException("UpperCaseNamingDiagnosticRule created with no illegal names");
        }
        this.illegalNames = Arrays.asList(illegalNames);
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final ApiListing listing) {
        // check class name
        getClassName(cu).ifPresent(name -> check(name, () -> makeId(cu), listing));

        // check all public / protected methods
        getPublicOrProtectedMethods(cu).forEach(
            methodDeclaration -> check(methodDeclaration.getNameAsString(), () -> makeId(methodDeclaration), listing));
    }

    private void check(String name, Supplier<String> makeId, ApiListing listing) {
        if (illegalNames.stream().anyMatch(name::contains)) {
            listing.addDiagnostic(new Diagnostic(WARNING, makeId.get(), "This is named with incorrect casing."));
        }
    }
}
