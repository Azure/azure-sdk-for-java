package com.azure.tools.apiview.processor.diagnostics.rules.utils;

import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.util.Optional;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.WARNING;

public class MiscUtils {

    /**
     * Checks that a given method declaration accepts a type whose name ends with the given suffix.
     */
    public static Optional<Diagnostic> checkMethodParameterTypeSuffix(MethodDeclaration methodDeclaration, String suffix) {
        return checkMethodParameterTypeSuffix(methodDeclaration, 0, suffix);
    }

    /**
     * Checks that a given method declaration accepts a type whose name ends with the given suffix.
     */
    public static Optional<Diagnostic> checkMethodParameterTypeSuffix(MethodDeclaration methodDeclaration,
                                                                      int paramIndex,
                                                                      String suffix) {
        if (paramIndex >= methodDeclaration.getParameters().size()) {
            // The method does not have the expected number of parameters.
            return Optional.of(new Diagnostic(WARNING, makeId(methodDeclaration),
            "Incorrect number of parameters for this builder method. Expected " + paramIndex + " but was "
                + methodDeclaration.getParameters().size() + "."));
        }

        Type parameterType = methodDeclaration.getParameter(paramIndex).getType();
        ClassOrInterfaceType classOrInterfaceType = parameterType.asClassOrInterfaceType();
        if (!classOrInterfaceType.getNameAsString().endsWith(suffix)) {
            return Optional.of(new Diagnostic(WARNING, makeId(methodDeclaration),
            "Incorrect type being supplied to this builder method. Expected a type ending with '"
                + suffix + "' but was " + classOrInterfaceType.getNameAsString() + "."));
        }
        return Optional.empty();
    }
}
