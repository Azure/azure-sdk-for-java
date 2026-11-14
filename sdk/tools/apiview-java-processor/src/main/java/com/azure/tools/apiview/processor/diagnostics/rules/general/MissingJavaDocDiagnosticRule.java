// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.ApiListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;

import java.util.function.Supplier;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.attemptToFindJavadocComment;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getClasses;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedConstructors;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedFields;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedMethods;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.WARNING;

/**
 * Checks missing java doc conventions.
 */
public class MissingJavaDocDiagnosticRule implements DiagnosticRule {
    private static final boolean IGNORE_OVERRIDES = true;

    @Override
    public void scanIndividual(final CompilationUnit cu, final ApiListing listing) {
        getClasses(cu).forEach(typeDeclaration -> {
            if (!attemptToFindJavadocComment(typeDeclaration).isPresent()) {
                // the type is missing JavaDoc
                listing.addDiagnostic(new Diagnostic(WARNING, makeId(typeDeclaration), "This API is missing JavaDoc."));
            }

            getPublicOrProtectedConstructors(typeDeclaration).forEach(n -> checkJavaDoc(n, () -> makeId(n), listing));
            getPublicOrProtectedFields(typeDeclaration).forEach(n -> checkJavaDoc(n, () -> makeId(n), listing));

            getPublicOrProtectedMethods(typeDeclaration).forEach(n -> {
                if (!IGNORE_OVERRIDES || !n.isAnnotationPresent("Override")) {
                    checkJavaDoc(n, () -> makeId(n), listing);
                }
            });
        });
    }

    private void checkJavaDoc(BodyDeclaration<?> n, Supplier<String> idSupplier, ApiListing listing) {
        if (!attemptToFindJavadocComment(n).isPresent()) {
            listing.addDiagnostic(new Diagnostic(WARNING, idSupplier.get(), "This API is missing JavaDoc."));
        }
    }
}
