// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.ApiListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.util.Arrays;
import java.util.List;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedConstructors;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedMethods;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.ERROR;

/**
 * A diagnostic rule that ensures our public API does not return or accept as a parameter into it something that has a
 * fully-qualified type that includes one of the illegal package names passed in as a constructor argument to this class
 */
public class IllegalPackageApiExportsDiagnosticRule implements DiagnosticRule {

    private final List<String> illegalPackages;

    /**
     * Creates an illegal package api exports diagnostic rule.
     *
     * @param packageNames the package names
     * @throws IllegalArgumentException if the operation cannot be completed
     */
    public IllegalPackageApiExportsDiagnosticRule(String... packageNames) {
        if (packageNames == null || packageNames.length == 0) {
            throw new IllegalArgumentException(
                "IllegalPackageApiExportsDiagnosticRule created with no illegal package names");
        }
        this.illegalPackages = Arrays.asList(packageNames);
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final ApiListing listing) {
        getPublicOrProtectedConstructors(cu)
            .forEach(methodDecl -> validateParameters(methodDecl, makeId(methodDecl), listing));

        getPublicOrProtectedMethods(cu).forEach(methodDecl -> {
            final String methodId = makeId(methodDecl);

            validateType(methodId, methodDecl.getType(), listing);

            validateParameters(methodDecl, methodId, listing);
        });
    }

    private void validateType(String methodName, Type type, final ApiListing listing) {
        if (type.isArrayType()) {
            validateType(methodName, type.asArrayType().getComponentType(), listing);
            return;
        }
        if (type.isWildcardType()) {
            type.asWildcardType().getExtendedType().ifPresent(bound -> validateType(methodName, bound, listing));
            type.asWildcardType().getSuperType().ifPresent(bound -> validateType(methodName, bound, listing));
            return;
        }
        if (!type.isClassOrInterfaceType()) {
            return;
        }

        ClassOrInterfaceType classType = type.asClassOrInterfaceType();
        String typeAsString = classType.getNameWithScope();

        if (typeAsString.contains(".")) {
            // the type is fully-qualified, e.g. 'com.azure.storage.implementation.models.BlobOptions'.
            // We therefore strip off the last section (in this case, '.BlobOptions', and pass that in to validate
            validatePackageName(methodName, typeAsString.substring(0, typeAsString.lastIndexOf(".")), listing);
        } else if (listing.getTypeToPackageNameMap().containsKey(typeAsString)) {
            // we know the type based on our previous scans
            validatePackageName(methodName, listing.getTypeToPackageNameMap().get(typeAsString), listing);
        }

        // we must also inspect the generic types
        classType.getTypeArguments()
            .ifPresent(types -> types.forEach(genericType -> validateType(methodName, genericType, listing)));
    }

    private void validateParameters(CallableDeclaration<?> methodDecl, String methodId, ApiListing listing) {
        methodDecl.getParameters()
            .stream()
            .map(Parameter::getType)
            .forEach(parameter -> validateType(methodId, parameter, listing));
    }

    private void validatePackageName(String methodId, String packageName, ApiListing listing) {
        for (String illegalPackage : illegalPackages) {
            if (packageName.contains(illegalPackage)) {
                listing.addDiagnostic(new Diagnostic(ERROR, methodId,
                    "Public API should never expose classes from the " + illegalPackage + " package."));
            }
        }
    }
}
