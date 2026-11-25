// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.ApiListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

import static com.azure.tools.apiview.processor.model.DiagnosticKind.WARNING;
import java.util.Arrays;
import java.util.List;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getClasses;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedConstructors;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedFields;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedMethods;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;

/**
 * Reviews annotations used throughout the source code to ensure that they do not come from our list of known
 * 'bad' annotations. If we detect one being used, we provide feedback to the user about their options.
 */
public class BadAnnotationDiagnosticRule implements DiagnosticRule {

    private final List<BadAnnotation> badAnnotations;

    /**
     * Creates a bad annotation diagnostic rule.
     *
     * @param badAnnotations the bad annotations
     */
    public BadAnnotationDiagnosticRule(BadAnnotation... badAnnotations) {
        this.badAnnotations = Arrays.asList(badAnnotations);
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final ApiListing listing) {
        getClasses(cu).forEach(typeDeclaration -> {
            // check annotations on the type itself
            typeDeclaration.getAnnotations()
                .forEach(annotation -> checkForBadAnnotations(listing, typeDeclaration, annotation));

            // check annotations on fields
            getPublicOrProtectedFields(typeDeclaration).forEach(field -> field.getAnnotations()
                .forEach(annotation -> checkForBadAnnotations(listing, field, annotation)));

            // check annotations on constructors
            getPublicOrProtectedConstructors(typeDeclaration).forEach(constructor -> constructor.getAnnotations()
                .forEach(annotation -> checkForBadAnnotations(listing, constructor, annotation)));

            // check annotations on methods
            getPublicOrProtectedMethods(typeDeclaration).forEach(method -> method.getAnnotations()
                .forEach(annotation -> checkForBadAnnotations(listing, method, annotation)));
        });
    }

    private void checkForBadAnnotations(ApiListing listing, NodeWithAnnotations<?> nodeWithAnnotations,
        AnnotationExpr annotation) {
        badAnnotations.forEach(badAnnotation -> {
            // check if the badAnnotation is the same as the found annotation
            if (badAnnotation.annotation.equals(annotation.getNameAsString())) {
                // we've got a match - file it as a diagnostic
                listing.addDiagnostic(
                    new Diagnostic(WARNING, makeId(annotation, nodeWithAnnotations), badAnnotation.errorMessage));
            }
        });
    }

    /**
     * An annotation name and the reason it is discouraged.
     */
    public static class BadAnnotation {
        private final String annotation;
        private final String errorMessage;

        /**
         * Creates a bad annotation.
         *
         * @param annotation the annotation
         * @param errorMessage the error message
         */
        public BadAnnotation(String annotation, String errorMessage) {
            this.annotation = annotation;
            this.errorMessage = errorMessage;
        }
    }
}
