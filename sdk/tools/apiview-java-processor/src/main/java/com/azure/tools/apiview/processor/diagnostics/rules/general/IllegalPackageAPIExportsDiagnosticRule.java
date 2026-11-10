package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.util.Arrays;
import java.util.List;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.*;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.*;

/**
 * A diagnostic rule that ensures our public API does not return or accept as a parameter into it something that has a
 * fully-qualified type that includes one of the illegal package names passed in as a constructor argument to this class
 */
public class IllegalPackageAPIExportsDiagnosticRule implements DiagnosticRule {

    private final List<String> illegalPackages;

    public IllegalPackageAPIExportsDiagnosticRule(String... packageNames) {
        if (packageNames == null || packageNames.length == 0) {
            throw new IllegalArgumentException("IllegalPackageAPIExportsDiagnosticRule created with no illegal package names");
        }
        this.illegalPackages = Arrays.asList(packageNames);
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        getPublicOrProtectedConstructors(cu)
                .forEach(methodDecl -> validateParameters(methodDecl, makeId(methodDecl), listing));

        getPublicOrProtectedMethods(cu)
                .forEach(methodDecl -> {
                    final String methodId = makeId(methodDecl);

                    if (methodDecl.getType() instanceof ClassOrInterfaceType) {
                        ClassOrInterfaceType returnType = (ClassOrInterfaceType) methodDecl.getType();
                        validateType(methodId, returnType, listing);
                    }

                    validateParameters(methodDecl, methodId, listing);
                });
    }

    private void validateType(String methodName, ClassOrInterfaceType type, final APIListing listing) {
        String typeAsString = type.getNameAsString();

        if (typeAsString.contains(".")) {
            // the type is fully-qualified, e.g. 'com.azure.storage.implementation.models.BlobOptions'.
            // We therefore strip off the last section (in this case, '.BlobOptions', and pass that in to validate
            validatePackageName(methodName,
                typeAsString.substring(0, typeAsString.lastIndexOf(".")),
                listing);
        } else if (listing.getTypeToPackageNameMap().containsKey(typeAsString)) {
            // we know the type based on our previous scans
            validatePackageName(methodName, listing.getTypeToPackageNameMap().get(typeAsString), listing);
        } else {
            // we don't know the type. This is usually because it is a Java class library type, or a generic T type
        }

        // we must also inspect the generic types
        type.getTypeArguments().ifPresent(types -> {
            types.stream()
                    .filter(Type::isClassOrInterfaceType)
                    .map(Type::asClassOrInterfaceType)
                    .forEach(genericType -> validateType(methodName, genericType, listing));
        });
    }

    private void validateParameters(CallableDeclaration<?> methodDecl,
                                    String methodId, APIListing listing) {
        methodDecl.getParameters().stream()
            .map(Parameter::getType)
            .filter(Type::isClassOrInterfaceType)
            .map(Type::asClassOrInterfaceType)
            .forEach(parameter -> validateType(methodId, parameter, listing));
    }

    private void validatePackageName(String methodId, String packageName, APIListing listing) {
        for (String illegalPackage : illegalPackages) {
            if (packageName.contains(illegalPackage)) {
                listing.addDiagnostic(new Diagnostic(
                    ERROR,
                    methodId,
                    "Public API should never expose classes from the " + illegalPackage + " package."));
            }
        }
    }
}
