// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.ApiListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;

import java.util.regex.Pattern;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getClassName;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPackageName;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.ERROR;

/**
 * Checks package name conventions.
 */
public class PackageNameDiagnosticRule implements DiagnosticRule {
    private final Pattern regex;

    /**
     * Creates a package name diagnostic rule.
     *
     * @param regex the regex
     */
    public PackageNameDiagnosticRule(Pattern regex) {
        this.regex = regex;
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final ApiListing listing) {
        getPackageName(cu).ifPresent(packageName -> {
            // we need to map the issue to the class id, because package text isn't printed in the APIView output
            getClassName(cu).map(listing.getKnownTypes()::get).ifPresent(typeId -> {
                if (!regex.matcher(packageName).matches()) {
                    listing.addDiagnostic(
                        new Diagnostic(ERROR, typeId, "Package name match the following regex: " + regex.pattern()));
                }
            });
        });
    }
}
