package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

import java.util.*;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.*;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.WARNING;

/**
 * Reviews annotations used throughout the source code to ensure that they do not come from our list of known
 * 'bad' annotations. If we detect one being used, we provide feedback to the user about their options.
 */
public class BadAnnotationDiagnosticRule implements DiagnosticRule {

    private final List<BadAnnotation> badAnnotations;

    public BadAnnotationDiagnosticRule(BadAnnotation... badAnnotations) {
        this.badAnnotations = Arrays.asList(badAnnotations);
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        getClasses(cu).forEach(typeDeclaration -> {
            // check annotations on the type itself
            typeDeclaration.getAnnotations()
                    .forEach(annotation -> checkForBadAnnotations(listing, typeDeclaration, annotation));

            // check annotations on fields
            getPublicOrProtectedFields(typeDeclaration)
                    .forEach(field -> field.getAnnotations()
                            .forEach(annotation -> checkForBadAnnotations(listing, field, annotation)));

            // check annotations on constructors
            getPublicOrProtectedConstructors(typeDeclaration)
                    .forEach(constructor -> constructor.getAnnotations()
                            .forEach(annotation -> checkForBadAnnotations(listing, constructor, annotation)));

            // check annotations on methods
            getPublicOrProtectedMethods(typeDeclaration)
                    .forEach(method -> method.getAnnotations()
                            .forEach(annotation -> checkForBadAnnotations(listing, method, annotation)));
        });
    }

    private void checkForBadAnnotations(APIListing listing, NodeWithAnnotations<?> nodeWithAnnotations, AnnotationExpr annotation) {
        badAnnotations.forEach(badAnnotation -> {
            // check if the badAnnotation is the same as the found annotation
            if (badAnnotation.annotation.equals(annotation.getNameAsString())) {
                // we've got a match - file it as a diagnostic
                listing.addDiagnostic(new Diagnostic(WARNING, makeId(annotation, nodeWithAnnotations),
                        badAnnotation.errorMessage));
            }
        });
    }

    public static class BadAnnotation {
        private final String annotation;
        private final String errorMessage;

        public BadAnnotation(String annotation, String errorMessage) {
            this.annotation = annotation;
            this.errorMessage = errorMessage;
        }
    }
}
