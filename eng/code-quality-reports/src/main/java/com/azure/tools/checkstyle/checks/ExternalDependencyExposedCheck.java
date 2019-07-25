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
 *  No external dependency exposed in public API
 */
public class ExternalDependencyExposedCheck extends AbstractCheck {
    private static final String EXTERNAL_DEPENDENCY_ERROR =
        "Class ''%s'', is a class from external dependency. You should not use it as a return or method argument type.";

    private static final Set<String> VALID_DEPENDENCY_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "java", "com.azure", "reactor"//, "io.netty.buffer.ByteBuf"
    )));

    private final Map<String, String> simpleClassNameToQualifiedNameMap = new HashMap<>();

    private boolean isPublicClass;

    @Override
    public void beginTree(DetailAST rootAST) {
        simpleClassNameToQualifiedNameMap.clear();
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
                // Add all imported classes into a map, key is the name of class and value is the full package path of class.
                final String importClassPath = FullIdent.createFullIdentBelow(token).getText();
                final String className = importClassPath.substring(importClassPath.lastIndexOf(".") + 1);
                simpleClassNameToQualifiedNameMap.put(className, importClassPath);
                break;
            case TokenTypes.CLASS_DEF:
                // CLASS_DEF always has MODIFIERS
                final AccessModifier accessModifier = CheckUtil.getAccessModifierFromModifiersToken(
                    token.findFirstToken(TokenTypes.MODIFIERS));
                isPublicClass = accessModifier.equals(AccessModifier.PUBLIC);
                break;
            case TokenTypes.METHOD_DEF:
                if (!isPublicClass) {
                    return;
                }
                checkNoExternalDependencyExposed(token);
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Checks for external dependency, log the error if it is an invalid external dependency.
     *
     * @param methodDefToken METHOD_DEF AST node
     */
    private void checkNoExternalDependencyExposed(DetailAST methodDefToken) {
        final DetailAST modifiersToken = methodDefToken.findFirstToken(TokenTypes.MODIFIERS);

        // Getting the modifier of the method to determine if it is 'public' or 'protected'.
        // Ignore the check if it is neither of 'public' nor 'protected',
        final AccessModifier accessModifier = CheckUtil.getAccessModifierFromModifiersToken(modifiersToken);
        if (!accessModifier.equals(AccessModifier.PUBLIC) && !accessModifier.equals(AccessModifier.PROTECTED)) {
            return;
        }

        // Checks for the return type of method
        final DetailAST typeToken = methodDefToken.findFirstToken(TokenTypes.TYPE);
        if (typeToken != null) {
            getInvalidReturnTypes(typeToken).forEach(
                (token, returnTypeName) -> log(token, String.format(EXTERNAL_DEPENDENCY_ERROR, returnTypeName)));
        }

        // Checks for the parameters of the method
        final DetailAST parametersToken = methodDefToken.findFirstToken(TokenTypes.PARAMETERS);
        if (parametersToken != null) {
            getInvalidParameterTypes(parametersToken).forEach(
                (token, returnTypeName) -> log(token, String.format(EXTERNAL_DEPENDENCY_ERROR, returnTypeName)));
        }
    }

    /**
     * Get invalid return types from a given TYPE node.
     *
     * @param typeToken TYPE AST node
     * @return a map that maps the invalid TYPE node and the type name.
     */
    private Map<DetailAST, String> getInvalidReturnTypes(DetailAST typeToken) {
        final Map<DetailAST, String> invalidReturnTypeMap = new HashMap<>();

        // Add all invalid external return types to the map
        final DetailAST identToken = typeToken.findFirstToken(TokenTypes.IDENT);
        if (identToken == null) {
            return invalidReturnTypeMap;
        }
        final String typeName = identToken.getText();
        if (!isValidClassDependency(typeName)) {
            invalidReturnTypeMap.put(typeToken, typeName);
        }

        // TYPE_ARGUMENTS, add all invalid external types to the map
        final DetailAST typeArgumentsToken = typeToken.findFirstToken(TokenTypes.TYPE_ARGUMENTS);
        if (typeArgumentsToken != null) {
            invalidReturnTypeMap.putAll(getInvalidTypeFromTypeArguments(typeArgumentsToken));
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
     * @param typeArgumentsToken TYPE_ARGUMENTS AST node
     * @return a map that maps all the invalid TYPE_ARGUMENT node and the type name
     */
    private Map<DetailAST, String> getInvalidTypeFromTypeArguments(DetailAST typeArgumentsToken) {
        final Map<DetailAST, String> invalidTypesMap = new HashMap<>();
        if (typeArgumentsToken == null) {
            return invalidTypesMap;
        }
        // Checks multiple type arguments
        for (DetailAST ast = typeArgumentsToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() != TokenTypes.TYPE_ARGUMENT) {
                continue;
            }

            final String invalidTypeName = getInvalidTypeNameFromTypeArgument(ast);
            if (invalidTypeName != null) {
                invalidTypesMap.put(ast, invalidTypeName);
            }
        }
        return invalidTypesMap;
    }

    /**
     * Get invalid type name from TYPE_ARGUMENT
     *
     * @param typeArgumentToken TYPE_ARGUMENT AST node
     * @return an invalid type name if it is an invalid library. Otherwise, returns null.
     */
    private String getInvalidTypeNameFromTypeArgument(DetailAST typeArgumentToken) {
        final DetailAST identToken = typeArgumentToken.findFirstToken(TokenTypes.IDENT);
        // if there is no IDENT token, implies the token is default java types.
        if (identToken == null) {
            return null;
        }

        final String typeName = identToken.getText();
        // if not exist in the classPathMap, that implies the type is java default types, such as int.
        return isValidClassDependency(typeName) ? null : typeName;
    }

    /**
     * A helper function that checks for whether a class is from a valid internal dependency or is a suppression class
     *
     * @param typeName the type name of class
     * @return true if the class  is a suppression class, otherwise, return false.
     */
    private boolean isValidClassDependency(String typeName) {
        // If the qualified class name does not exist in the map,
        // it implies the type is a primitive Java type (ie. int, long, etc).
        if (!simpleClassNameToQualifiedNameMap.containsKey(typeName)) {
            return true;
        }

        final String qualifiedName = simpleClassNameToQualifiedNameMap.get(typeName);
        return VALID_DEPENDENCY_SET.stream()
            .anyMatch(validPackageName -> qualifiedName.startsWith(validPackageName));
    }
}
