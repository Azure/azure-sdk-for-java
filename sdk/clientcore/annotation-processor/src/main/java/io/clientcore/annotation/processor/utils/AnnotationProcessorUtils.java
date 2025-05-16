// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Utility class for annotation processor.
 */
public class AnnotationProcessorUtils {

    /**
     * Generates a JavaParser Statement for creating a ParameterizedType for the given return type.
     * @param returnType The {@link TypeMirror} representing the return type to generate a {@code ParameterizedType} for.
     * @param body The {@link BlockStmt} to which imports may be added if necessary.
     * @return A JavaParser {@link Statement} that creates a {@code ParameterizedType} for the given return type.
     */
    public static Statement createParameterizedTypeStatement(TypeMirror returnType, BlockStmt body) {
        if (returnType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) returnType;
            TypeElement typeElement = (TypeElement) declaredType.asElement();

            String outerType = ((TypeElement) declaredType.asElement()).getQualifiedName().toString() + ".class";

            if (!declaredType.getTypeArguments().isEmpty()) {
                TypeMirror firstGenericType = declaredType.getTypeArguments().get(0);
                if (firstGenericType.getKind() == TypeKind.ARRAY) {
                    ArrayType arrayType = (ArrayType) firstGenericType;
                    String componentTypeName = arrayType.getComponentType().toString();
                    return StaticJavaParser
                        .parseStatement("ParameterizedType returnType = CoreUtils.createParameterizedType(" + outerType
                            + ", " + componentTypeName + "[].class);");
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
                            return StaticJavaParser
                                .parseStatement("ParameterizedType returnType = CoreUtils.createParameterizedType("
                                    + genericType + ".class, " + innerType + ".class);");
                        }
                    } else {
                        return StaticJavaParser
                            .parseStatement("ParameterizedType returnType = CoreUtils.createParameterizedType("
                                + outerType + ", " + genericType + ".class);");
                    }
                }
            }
            return StaticJavaParser
                .parseStatement("ParameterizedType returnType = CoreUtils.createParameterizedType(" + outerType + ");");
        } else {
            return StaticJavaParser.parseStatement("ParameterizedType returnType = null;");
        }
    }
}
