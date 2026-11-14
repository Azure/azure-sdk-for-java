package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
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

public class MissingJavaDocDiagnosticRule implements DiagnosticRule {
    private static final boolean IGNORE_OVERRIDES = true;

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        getClasses(cu).forEach(typeDeclaration -> {
            if (!attemptToFindJavadocComment(typeDeclaration).isPresent()) {
                // the type is missing JavaDoc
                listing.addDiagnostic(new Diagnostic(WARNING, makeId(typeDeclaration), "This API is missing JavaDoc."));
            }

            getPublicOrProtectedConstructors(typeDeclaration).forEach(n -> checkJavaDoc(n, () -> makeId(n), listing));
            getPublicOrProtectedFields(typeDeclaration).forEach(n -> checkJavaDoc(n, () -> makeId(n), listing));

            getPublicOrProtectedMethods(typeDeclaration).forEach(n -> {
                if (IGNORE_OVERRIDES && n.isAnnotationPresent("Override")) {
                    // no-op - we don't check for Javadoc on methods that are overrides - we assume that the parent
                    // class from which this method is specified already has sufficiently detailed JavaDoc and we have
                    // validated that elsewhere.
                } else {
                    checkJavaDoc(n, () -> makeId(n), listing);
                }
            });
        });
    }

    private void checkJavaDoc(BodyDeclaration<?> n, Supplier<String> idSupplier, APIListing listing) {
        if (!attemptToFindJavadocComment(n).isPresent()) {
            listing.addDiagnostic(new Diagnostic(WARNING, idSupplier.get(), "This API is missing JavaDoc."));
        }
    }
}
