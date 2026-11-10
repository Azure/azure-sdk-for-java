package com.azure.tools.apiview.processor.diagnostics.rules.clientcore;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getClassName;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedMethods;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.ERROR;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.WARNING;

/**
 * Diagnostic rule to validate if types extending from ExpandableStringEnum include
 * fromString() and values() methods.
 */
public class ExpandableEnumDiagnosticRule implements DiagnosticRule {
    private final String parentTypeName;

    public ExpandableEnumDiagnosticRule(String parentTypeName) {
        this.parentTypeName = parentTypeName;
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        cu.getPrimaryType().ifPresent(typeDeclaration -> {
            if (typeDeclaration instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration) typeDeclaration;
                // check if the type extends ExpandableStringEnum
                if (classDeclaration.getExtendedTypes() != null
                        && classDeclaration.getExtendedTypes().stream()
                        .anyMatch(extendedType -> extendedType.getNameAsString().equals(parentTypeName))) {
                    checkRequiredMethodsExist(cu, listing);
                }
            }
        });
    }

    private void checkRequiredMethodsExist(CompilationUnit cu, APIListing listing) {
        getClassName(cu).ifPresent(className -> {
            AtomicBoolean hasFromStringMethod = new AtomicBoolean(false);
            AtomicBoolean hasValuesMethod = new AtomicBoolean(false);
            getPublicOrProtectedMethods(cu).forEach(method -> {
                final String methodName = method.getNameAsString();
                if (methodName.equals("fromString") && method.isPublic() && method.isStatic()) {
                    hasFromStringMethod.set(true);
                }
                if (methodName.equals(("values")) && method.isPublic() && method.isStatic()) {
                    hasValuesMethod.set(true);
                }
            });

            if (!hasFromStringMethod.get()) {
                // missing public fromString method is an error.
                listing.addDiagnostic(new Diagnostic(
                        ERROR,
                        makeId(cu),
                        "Types extending " + parentTypeName + " should include public static fromString() method."));
            }
            if (!hasValuesMethod.get()) {
                // Missing values method can be logged at warning level.
                listing.addDiagnostic(new Diagnostic(
                        WARNING,
                        makeId(cu),
                        "Types extending " + parentTypeName + " should include public static values() method."));
            }
        });
    }
}
