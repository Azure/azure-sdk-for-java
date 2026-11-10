package com.azure.tools.apiview.processor.diagnostics.rules.utils;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedMethods;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.ERROR;

public abstract class FluentSetterReturnTypeDiagnosticRule implements DiagnosticRule {
    private final Predicate<TypeDeclaration<?>> isFluentType;

    public FluentSetterReturnTypeDiagnosticRule(Predicate<TypeDeclaration<?>> isFluentType) {
        this.isFluentType = isFluentType;
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        cu.getTypes().stream()
            .filter(isFluentType)
            .forEach(type -> processFluentType(type, listing));
    }

    private void processFluentType(final TypeDeclaration<?> type, final APIListing listing) {
        // get all setter methods (we will find them just by looking for all methods that start with 'set')
        final String typeName = type.getNameAsString();

        getPublicOrProtectedMethods(type)
                .filter(method -> method.getNameAsString().startsWith("set"))
                .forEach(method -> {
                    if (!method.getType().asString().equals(typeName)) {
                        listing.addDiagnostic(new Diagnostic(
                            ERROR,
                            makeId(method),
                            "Setter methods in a fluent class must return the same type as the fluent type."));
                    }
                });
    }
}
