// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Checks that only allowed external dependencies are exposed in public APIs.
 * <p>
 * {@code allowedDependencyPrefixes} (optional): An array of prefixes of allowed dependencies that can be exposed by
 * public APIs. Defined in the checkstyle.xml config file. If left empty, no external dependencies will be allowed,
 * other than {@code java.} and {@code javax.} prefixes.
 * <p>
 * {@code java.} and {@code javax.} are always allowed prefixes.
 */
public class ExternalDependencyExposedCheck extends ImplementationExcludingCheck {
    private static final String EXTERNAL_DEPENDENCY_ERROR
        = "Class ''%s'', is a class from external dependency. You should not use it as a %s type.";

    private static final String[] ALWAYS_ALLOWED = new String[] { "java.", "javax." };

    private final Map<String, String> simpleClassNameToQualifiedNameMap = new HashMap<>();

    private boolean isPublicClass;
    private String[] allowedDependencyPrefixes = new String[0];

    /**
     * Creates a new instance of {@link ExternalDependencyExposedCheck}.
     */
    public ExternalDependencyExposedCheck() {
    }

    /**
     * Sets the allowed dependency prefixes that can be exposed by public APIs.
     * <p>
     * The prefixes should conclude with a dot (.) to ensure that the prefix is matched correctly. An example of a valid
     * prefix is {@code io.clientcore.}.
     *
     * @param allowedDependencyPrefixes The prefixes of allowed dependencies that can be exposed by public APIs.
     */
    public final void setAllowedDependencyPrefixes(String... allowedDependencyPrefixes) {
        if (allowedDependencyPrefixes != null) {
            this.allowedDependencyPrefixes = Arrays.copyOf(allowedDependencyPrefixes, allowedDependencyPrefixes.length);
        }
    }

    @Override
    public void beforeTree(DetailAST rootAST) {
        simpleClassNameToQualifiedNameMap.clear();
    }

    @Override
    public int[] getTokensForCheck() {
        return new int[] { TokenTypes.IMPORT, TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF };
    }

    @Override
    public void processToken(DetailAST token) {
        int tokenType = token.getType();
        if (tokenType == TokenTypes.IMPORT) {
            // Add all imported classes into a map, key is the name of class and value is the full package
            // path of class.
            final String importClassPath = FullIdent.createFullIdentBelow(token).getText();
            final String className = importClassPath.substring(importClassPath.lastIndexOf(".") + 1);
            simpleClassNameToQualifiedNameMap.put(className, importClassPath);
        } else if (tokenType == TokenTypes.CLASS_DEF) {
            // CLASS_DEF always has MODIFIERS
            isPublicClass = SdkCheckUtils.isPublicOrProtected(token);
        } else if (tokenType == TokenTypes.METHOD_DEF) {
            if (!isPublicClass) {
                return;
            }
            checkNoExternalDependencyExposed(token);
        }
    }

    /**
     * Checks for external dependency, log the error if it is an invalid external dependency.
     *
     * @param methodDefToken METHOD_DEF AST node
     */
    private void checkNoExternalDependencyExposed(DetailAST methodDefToken) {
        // Getting the modifier of the method to determine if it is 'public' or 'protected'.
        // Ignore the check if it is neither of 'public' nor 'protected',
        if (!SdkCheckUtils.isPublicOrProtected(methodDefToken)) {
            return;
        }

        // Checks for the return type of method
        final DetailAST typeToken = methodDefToken.findFirstToken(TokenTypes.TYPE);
        if (typeToken != null) {
            getInvalidReturnTypes(typeToken).forEach((token, returnTypeName) -> log(token,
                String.format(EXTERNAL_DEPENDENCY_ERROR, returnTypeName, "return")));
        }

        // Checks for the parameters of the method
        final DetailAST parametersToken = methodDefToken.findFirstToken(TokenTypes.PARAMETERS);
        if (parametersToken != null) {
            getInvalidParameterTypes(parametersToken).forEach((token, parameterTypeName) -> log(token,
                String.format(EXTERNAL_DEPENDENCY_ERROR, parameterTypeName, "method argument")));
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
            getInvalidParameterType(typeArgumentsToken, invalidReturnTypeMap);
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
                getInvalidParameterType(ast.findFirstToken(TokenTypes.TYPE), invalidParameterTypesMap);
            }
        }
        return invalidParameterTypesMap;
    }

    /**
     * Get all invalid AST nodes from a given token. DFS tree traversal used to find all invalid nodes.
     *
     * @param token TYPE_ARGUMENT, TYPE_ARGUMENTS or TYPE AST node
     */
    private void getInvalidParameterType(DetailAST token, Map<DetailAST, String> invalidTypesMap) {
        if (token == null) {
            return;
        }

        for (DetailAST ast = token.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            final int tokenType = ast.getType();
            if (tokenType == TokenTypes.IDENT) {
                final String identName = ast.getText();
                if (!isValidClassDependency(identName)) {
                    invalidTypesMap.put(ast, identName);
                }
            } else if (tokenType == TokenTypes.TYPE_ARGUMENT || tokenType == TokenTypes.TYPE_ARGUMENTS) {
                getInvalidParameterType(ast, invalidTypesMap);
            }
        }
    }

    /**
     * A helper function that checks for whether a class is from a valid internal dependency or is a suppression class
     *
     * @param typeName the type name of class
     * @return true if the class is a suppression class, otherwise, return false.
     */
    private boolean isValidClassDependency(String typeName) {
        String qualifiedName = simpleClassNameToQualifiedNameMap.get(typeName);
        // If the qualified class name does not exist in the map,
        // it implies the type is a primitive Java type (ie. int, long, etc).
        if (qualifiedName == null) {
            return true;
        }

        for (String allowedPrefix : ALWAYS_ALLOWED) {
            if (qualifiedName.startsWith(allowedPrefix)) {
                return true;
            }
        }

        for (String prefix : allowedDependencyPrefixes) {
            if (qualifiedName.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }
}
