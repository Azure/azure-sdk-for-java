package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.*;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.WARNING;

/**
 * This diagnostic encourages developers to use upper case enum values, with underscores between words.
 */
public class UpperCaseEnumValuesDiagnosticRule implements DiagnosticRule {

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        // if the CompilationUnit is an enum, check that all of the enum values are upper case, adding a diagnostic
        // if they are not.
        getClasses(cu)
            .filter(TypeDeclaration::isEnumDeclaration)
            .map(TypeDeclaration::asEnumDeclaration)
            .forEach(enumDeclaration -> enumDeclaration.getEntries().forEach(enumConstantDeclaration -> {
                String name = enumConstantDeclaration.getName().asString();
                if (!name.equals(name.toUpperCase())) {
                    listing.addDiagnostic(new Diagnostic(
                        WARNING,
                        makeId(enumConstantDeclaration),
                        "All enum constants should be upper case, using underscores as necessary between words."));
                }
            }));
    }
}
