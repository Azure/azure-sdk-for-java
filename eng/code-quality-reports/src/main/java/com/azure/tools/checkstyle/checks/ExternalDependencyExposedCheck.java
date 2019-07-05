// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.naming.AccessModifier;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *  No external dependency exposed in the public API
 */
public class ExternalDependencyExposedCheck extends AbstractCheck {
    private static final String JAVA = "java";
    private static final String COM_AZURE = "com.azure";
    private static final String REACTOR = "reactor";
    private static final String DOT = ".";

    private static final String EXTERNAL_DEPENDENCY_ERROR =
        "Class ''%s'', is a class from external dependency. You should not use it as a return or parameter argument type";

    private static final Set<String> VALID_DEPENDENCY_PACKAGE_NAMES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        JAVA, COM_AZURE, REACTOR
    )));

    private Map<String, String> classPathMap = new HashMap<>();

    @Override
    public void beginTree(DetailAST rootAST) {
        classPathMap.clear();
    }

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {
            TokenTypes.IMPORT,
            TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.IMPORT:
                addImportedClassPath(token);
                break;
            case TokenTypes.METHOD_DEF:
                checkNoExternalDependencyExposed(token);
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Add all imported classes into a map, key is the name of class and value is the full package path of class.
     *
     * @param token the IMPORT AST node
     */
    private void addImportedClassPath(DetailAST token) {
        final String importClassPath = FullIdent.createFullIdentBelow(token).getText();
        final String className = importClassPath.substring(importClassPath.lastIndexOf(DOT) + 1);
        classPathMap.put(className, importClassPath);
    }

    /**
     * Checks for external dependency, log the error if it is an invalid external dependency.
     *
     * @param methodDefToken METHOD_DEF AST node
     */
    private void checkNoExternalDependencyExposed(DetailAST methodDefToken) {
        final DetailAST modifiersToken = methodDefToken.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiersToken == null) {
            return;
        }

        AccessModifier accessModifier = CheckUtil.getAccessModifierFromModifiersToken(modifiersToken);
        if (!accessModifier.equals(AccessModifier.PUBLIC) && !accessModifier.equals(AccessModifier.PROTECTED)) {
            return;
        }

        DetailAST typeToken = methodDefToken.findFirstToken(TokenTypes.TYPE);
        if (typeToken != null) {
            getInvalidReturnTypes(typeToken).forEach(
                (token, returnTypeName) -> log(token, String.format(EXTERNAL_DEPENDENCY_ERROR, returnTypeName)));
        }

        DetailAST parametersToken = methodDefToken.findFirstToken(TokenTypes.PARAMETERS);
        if (parametersToken != null) {
            getInvalidParameterTypes(parametersToken).forEach(
                (token, returnTypeName) -> log(token, String.format(EXTERNAL_DEPENDENCY_ERROR, returnTypeName)));;
        }
    }

    /**
     * Get invalid return types from a given TYPE node.
     *
     * @param typeToken TYPE AST node
     * @return a map that maps the invalid TYPE node and the type name.
     */
    private Map<DetailAST, String> getInvalidReturnTypes(DetailAST typeToken) {
        Map<DetailAST, String> invalidReturnTypeMap = new HashMap<>();
        final int typeCount = typeToken.getChildCount();
        // The TYPE node could has more than one child, such as Map<T, T>
        if (typeCount == 1) {
            final String returnTypeName = typeToken.getFirstChild().getText();
            // if not exist in the classPathMap, that implies the type is java default types, such as int.
            if (classPathMap.containsKey(returnTypeName) && !isValidLibrary(classPathMap.get(returnTypeName))) {
                invalidReturnTypeMap.put(typeToken, returnTypeName);
            }
        } else {
            invalidReturnTypeMap = getInvalidTypeFromTypeArguments(typeToken);
        }
        return invalidReturnTypeMap;
    }

    /**
     * Get invalid parameter types from a given PARAMETERS node.
     *
     * @param parametersTypeToken PARAMETERS AST node
     * @return a map that maps all the invalid TYPE_ARGUMENT node and the type name
     */
    private Map<DetailAST, String> getInvalidParameterTypes(DetailAST parametersTypeToken) {
        final Map<DetailAST, String> invalidParameterTypesMap = new HashMap<>();
        for (DetailAST ast = parametersTypeToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() == TokenTypes.PARAMETER_DEF) {
                invalidParameterTypesMap.putAll(getInvalidTypeFromTypeArguments(ast.findFirstToken(TokenTypes.TYPE)));
            }
        }
        return invalidParameterTypesMap;
    }

    /**
     *  A helper function that checks TYPE AST node. Since both return type and input parameter argument type has
     *  TYPE AST node under. This function applied to both.
     *
     * @param typeToken TYPE AST node
     * @return a map that maps all the invalid TYPE_ARGUMENT node and the type name
     */
    private Map<DetailAST, String> getInvalidTypeFromTypeArguments(DetailAST typeToken) {
        final Map<DetailAST, String> invalidTypesMap = new HashMap<>();

        if (typeToken == null) {
            return invalidTypesMap;
        }

        final DetailAST identToken = typeToken.findFirstToken(TokenTypes.IDENT);
        // if there is no IDENT token, implies the token is a default java type.
        if (identToken == null) {
            return invalidTypesMap;
        }

        final String typeName = identToken.getText();
        // if does not exist in the classPathMap, that implies the type is java default types, such as int.
        if (classPathMap.containsKey(typeName)) {
            if (!isValidLibrary(classPathMap.get(typeName))) {
                invalidTypesMap.put(typeToken, typeName);
            } else {
                // Checks multiple type arguments
                final DetailAST typeArgumentsToken = typeToken.findFirstToken(TokenTypes.TYPE_ARGUMENTS);
                if (typeArgumentsToken != null) {
                    for (DetailAST ast = typeArgumentsToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
                        if (ast.getType() == TokenTypes.TYPE_ARGUMENT) {
                            String invalidTypeName = getInvalidTypeNameFromTypeArgument(ast);
                            if (invalidTypeName != null) {
                                invalidTypesMap.put(ast, invalidTypeName);
                            }
                        }
                    }
                } else {
                    // Checks single type argument
                    final DetailAST typeArgumentToken = typeToken.findFirstToken(TokenTypes.TYPE_ARGUMENT);
                    String invalidTypeName = getInvalidTypeNameFromTypeArgument(typeArgumentToken);
                    if (invalidTypeName != null) {
                        invalidTypesMap.put(typeArgumentToken, invalidTypeName);
                    }
                }
            }
        }
        return invalidTypesMap;
    }

    /**
     * Get invalid type name.
     *
     * @param typeArgumentToken TYPE_ARGUMENT AST node
     * @return an invalid type name if it is an invalid library. Otherwise, returns null.
     */
    private String getInvalidTypeNameFromTypeArgument(DetailAST typeArgumentToken) {
        if (typeArgumentToken == null) {
            return null;
        }

        final DetailAST identToken = typeArgumentToken.findFirstToken(TokenTypes.IDENT);
        // if there is no IDENT token, implies the token is default java types.
        if (identToken != null) {
            final String typeName = identToken.getText();
            // if not exist in the classPathMap, that implies the type is java default types, such as int.
            if (classPathMap.containsKey(typeName) && !isValidLibrary(classPathMap.get(typeName))) {
                return typeName;
            }
        }
        return null;
    }

    /**
     *  A helper function that checks for whether a class is from a valid internal dependency or is a suppression class
     *
     * @param classPath the full class path for a given class
     * @return true if the class  is a suppression class, otherwise, return false.
     */
    private boolean isValidLibrary(String classPath) {
        return VALID_DEPENDENCY_PACKAGE_NAMES.stream().anyMatch(validPackageName -> classPath.startsWith(validPackageName));
    }
}
