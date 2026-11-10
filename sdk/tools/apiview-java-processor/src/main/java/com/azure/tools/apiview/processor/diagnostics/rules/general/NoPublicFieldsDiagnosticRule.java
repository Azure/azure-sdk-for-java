package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.*;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.*;

public class NoPublicFieldsDiagnosticRule implements DiagnosticRule {

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        getPublicOrProtectedFields(cu)
                .filter(fieldDecl -> !fieldDecl.isStatic())
                .forEach(fieldDecl -> {
                    final String fieldId = makeId(fieldDecl);
                    listing.addDiagnostic(new Diagnostic(
                        ERROR,
                        fieldId,
                        "There should not be non-static public or protected fields in any class."));
                });
    }
}
