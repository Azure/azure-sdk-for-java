// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

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
        if (!(returnType instanceof DeclaredType)) {
            return "CoreUtils.createParameterizedType(" + createTypeExpression(returnType) + ")";
        }

        DeclaredType declaredType = (DeclaredType) returnType;
        String rawType = ((TypeElement) declaredType.asElement()).getQualifiedName().toString() + ".class";
        if (declaredType.getTypeArguments().isEmpty()) {
            return "CoreUtils.createParameterizedType(" + rawType + ")";
        }

        String typeArguments = declaredType.getTypeArguments()
            .stream()
            .map(AnnotationProcessorUtils::createTypeExpression)
            .collect(Collectors.joining(", "));
        return "CoreUtils.createParameterizedType(" + rawType + ", " + typeArguments + ")";
    }

    private static String createTypeExpression(TypeMirror type) {
        if (type instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) type;
            if (!declaredType.getTypeArguments().isEmpty()) {
                return createParameterizedTypeStatement(declaredType, null);
            }

            return ((TypeElement) declaredType.asElement()).getQualifiedName().toString() + ".class";
        }

        if (type.getKind() == TypeKind.WILDCARD) {
            WildcardType wildcardType = (WildcardType) type;
            TypeMirror bound = wildcardType.getExtendsBound();
            return bound == null ? "Object.class" : createTypeExpression(bound);
        }

        if (type.getKind() == TypeKind.TYPEVAR) {
            TypeMirror upperBound = ((TypeVariable) type).getUpperBound();
            return upperBound == null ? "Object.class" : createTypeExpression(upperBound);
        }

        return type + ".class";
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
