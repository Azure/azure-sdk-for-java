// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

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

    private static final Set<String> validLibrarySet = new HashSet<>();
    private static Map<String, String> classPathMap = new HashMap<>();

    @Override
    public void init() {
        validLibrarySet.add(JAVA);
        validLibrarySet.add(COM_AZURE);
        validLibrarySet.add(REACTOR);
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
                collectAllImportedClassPath(token);
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
     * Collect all imported classes into a map, key is the name of class and value is the full package path of class.
     *
     * @param token the IMPORT AST node
     */
    private void collectAllImportedClassPath(DetailAST token) {
        final String importClassPath = FullIdent.createFullIdentBelow(token).getText();
        final String className = importClassPath.substring(importClassPath.lastIndexOf(DOT) + 1);
        classPathMap.put(className, importClassPath);
    }

    /**
     * A Check for external dependency, log the error if it is an invalid external dependency.
     *
     * @param methodDefToken METHOD_DEF AST node
     */
    private void checkNoExternalDependencyExposed(DetailAST methodDefToken) {
        final DetailAST modifiersToken = methodDefToken.findFirstToken(TokenTypes.MODIFIERS);
        // Public API
        if (modifiersToken != null &&
            (modifiersToken.branchContains(TokenTypes.LITERAL_PUBLIC)
                || modifiersToken.branchContains(TokenTypes.LITERAL_PROTECTED))) {
            checkReturnType(methodDefToken.findFirstToken(TokenTypes.TYPE));
            checkParametersType(methodDefToken.findFirstToken(TokenTypes.PARAMETERS));
        }
    }

    /**
     * Checks for return type
     *
     * @param typeToken TYPE AST node
     */
    private void checkReturnType(DetailAST typeToken) {
        final int typeCount = typeToken.getChildCount();
        // The TYPE node has more than one child, such as Map<T, T>
        if (typeCount == 1) {
            final String returnTypeName = typeToken.getFirstChild().getText();
            // if not exist in the classPathMap, that implies the type is java default types, such as int.
            if (classPathMap.containsKey(returnTypeName)) {
                if (!isValidLibrary(classPathMap.get(returnTypeName))) {
                    log(typeToken, String.format(EXTERNAL_DEPENDENCY_ERROR, returnTypeName));
                }
            }
        } else {
            checkTypeArguments(typeToken);
        }
    }

    /**
     * Checks for input parameters
     *
     * @param parametersTypeToken PARAMETERS AST node
     */
    private void checkParametersType(DetailAST parametersTypeToken) {
        for (DetailAST ast = parametersTypeToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() == TokenTypes.PARAMETER_DEF) {
                checkTypeArguments(ast.findFirstToken(TokenTypes.TYPE));
            }
        }
    }

    /**
     *  A helper function that checks TYPE AST node. Since both return type and input parameter argument type has
     *  TYPE AST node under. This function applied to both.
     *
     * @param typeToken TYPE AST node
     */
    private void checkTypeArguments(DetailAST typeToken) {
        final DetailAST identToken = typeToken.findFirstToken(TokenTypes.IDENT);
        // if there is no IDENT token, implies the token is default java types.
        if (identToken != null) {
            final String typeName = identToken.getText();
            // if not exist in the classPathMap, that implies the type is java default types, such as int.
            if (classPathMap.containsKey(typeName)) {
                if (!isValidLibrary(classPathMap.get(typeName))) {
                    log(typeToken, String.format(EXTERNAL_DEPENDENCY_ERROR, typeName));
                } else {
                    // Checks multiple type arguments
                    final DetailAST typeArgumentsToken = typeToken.findFirstToken(TokenTypes.TYPE_ARGUMENTS);
                    if (typeArgumentsToken != null) {
                        for (DetailAST ast = typeArgumentsToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
                            if (ast.getType() == TokenTypes.TYPE_ARGUMENT) {
                                checkSingleTypeArgument(ast);
                            }
                        }
                    }
                    // Checks single type argument
                    final DetailAST typeArgumentToken = typeToken.findFirstToken(TokenTypes.TYPE_ARGUMENT);
                    if (typeArgumentToken != null) {
                        checkSingleTypeArgument(typeArgumentToken);
                    }
                }
            }
        }
    }

    /**
     *  A helper function that checks for single type arguments scenario.
     *
     * @param typeArgumentToken TYPE_ARGUMENT AST node
     */
    private void checkSingleTypeArgument(DetailAST typeArgumentToken) {
        final DetailAST identToken = typeArgumentToken.findFirstToken(TokenTypes.IDENT);
        // if there is no IDENT token, implies the token is default java types.
        if (identToken != null) {
            final String typeName = identToken.getText();
            // if not exist in the classPathMap, that implies the type is java default types, such as int.
            if (classPathMap.containsKey(typeName)) {
                if (!isValidLibrary(classPathMap.get(typeName))) {
                    log(typeArgumentToken, String.format(EXTERNAL_DEPENDENCY_ERROR, typeName));
                }
            }
        }
    }

    /**
     *  A helper function that checks for whether a class is from a valid internal dependency or is a suppression class
     *
     * @param classPath the full class path for a given class
     * @return true if the class  is a suppression class, otherwise, return false.
     */
    private boolean isValidLibrary(String classPath) {
        boolean isValidLibrary = false;
        for (String lib : validLibrarySet) {
            if (classPath.startsWith(lib)) {
                isValidLibrary = true;
            }
        }
        return isValidLibrary;
    }
}
