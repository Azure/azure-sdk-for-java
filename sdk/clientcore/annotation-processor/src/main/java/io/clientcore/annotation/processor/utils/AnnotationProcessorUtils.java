// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Utility class for annotation processor.
 */
public final class AnnotationProcessorUtils {

    /**
     * Generates a JavaParser Statement for creating a ParameterizedType for the given return type.
     *
     * @param returnType The {@link TypeMirror} representing the return type to generate a {@code ParameterizedType}
     * for.
     * @param body The {@link BlockStmt} to which imports may be added if necessary.
     * @return A JavaParser {@link Statement} that creates a {@code ParameterizedType} for the given return type.
     */
    public static String createParameterizedTypeStatement(TypeMirror returnType, BlockStmt body) {
        if (returnType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) returnType;
            String outerType = ((TypeElement) declaredType.asElement()).getQualifiedName().toString() + ".class";

            if (!declaredType.getTypeArguments().isEmpty()) {
                TypeMirror firstGenericType = declaredType.getTypeArguments().get(0);
                if (firstGenericType.getKind() == TypeKind.ARRAY) {
                    ArrayType arrayType = (ArrayType) firstGenericType;
                    String componentTypeName = arrayType.getComponentType().toString();
                    return "CoreUtils.createParameterizedType(" + outerType + ", " + componentTypeName + "[].class)";
                } else if (firstGenericType instanceof DeclaredType) {
                    DeclaredType genericDeclaredType = (DeclaredType) firstGenericType;
                    TypeElement genericTypeElement = (TypeElement) genericDeclaredType.asElement();

                    body.findCompilationUnit()
                        .ifPresent(compilationUnit -> compilationUnit
                            .addImport(genericTypeElement.getQualifiedName().toString()));

                    String genericType = ((DeclaredType) declaredType.getTypeArguments().get(0)).asElement()
                        .getSimpleName()
                        .toString();
                    if (genericTypeElement.getQualifiedName().contentEquals(List.class.getCanonicalName())) {
                        if (!genericDeclaredType.getTypeArguments().isEmpty()) {
                            String innerType
                                = ((DeclaredType) genericDeclaredType.getTypeArguments().get(0)).asElement()
                                    .getSimpleName()
                                    .toString();
                            return "CoreUtils.createParameterizedType(" + genericType + ".class, " + innerType
                                + ".class)";
                        }
                    } else {
                        return "CoreUtils.createParameterizedType(" + outerType + ", " + genericType + ".class)";
                    }
                }
            }
            return "CoreUtils.createParameterizedType(" + outerType + ")";
        } else {
            return "null;";
        }
    }

    /**
     * Generates a JavaParser Statement for creating response code check for the given expected status codes.
     *
     * @param expectedStatusCodes The list of expected status codes to check against.
    
     * @return A JavaParser {@link Statement} that creates a response code check for the given expected status codes.
     */
    public static String generateExpectedResponseCheck(List<Integer> expectedStatusCodes) {
        if (expectedStatusCodes == null || expectedStatusCodes.isEmpty()) {
            // All 2XX codes are considered a success
            return "responseCode >= 200 && responseCode < 300";
        } else if (expectedStatusCodes.size() == 1) {
            return "responseCode == " + expectedStatusCodes.get(0);
        } else {
            return expectedStatusCodes.stream()
                .map(code -> "responseCode == " + code)
                .collect(Collectors.joining(" || ", "(", ")"));
        }
    }

    private AnnotationProcessorUtils() {
    }
}
