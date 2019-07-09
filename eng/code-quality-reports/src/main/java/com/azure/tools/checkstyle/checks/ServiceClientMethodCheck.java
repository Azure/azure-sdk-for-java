// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Service Client Method Checks. All methods that has a @ServiceMethod annotation in a class annotated with
 * @ServiceClient should follow below rules:
 *
 * (1) Method naming pattern. Refer to Java Spec:
 * (2) Methods should not have "Async" added to the method name
 * (3) Return type of async and sync clients should be as per guidelines:
 *   (a) Return type for async collection should be of type? extends Flux
 *   (b) Return type for async single value should be of type? extends Mono
 *   (c) Return type for sync collection should be of type? extends Stream
 *   (d) Return type for sync single value should be of type? extends Response
 */
public class ServiceClientMethodCheck extends AbstractCheck {
    private static final String DOT = ".";
    private static final String SERVICE_CLIENT = "ServiceClient";
    private static final String SERVICE_METHOD = "ServiceMethod";
    private static final String ASYNC = "Async";
    private static final String RETURNS = "returns";
    private static final String SINGLE_RETURN_TYPE = "ReturnType.SINGLE";
    private static final String COLLECTION_RETURN_TYPE = "ReturnType.COLLECTION";

    private static final String FLUX = "reactor.core.publisher.Flux";
    private static final String MONO = "reactor.core.publisher.Mono";
    private static final String RESPONSE = "com.azure.core.http.rest.response";

    private static final String FAILED_TO_LOAD_MESSAGE = "%s class failed to load, ServiceClientChecks will be ignored.";
    private static final String SINGLE_VALUE_RETURN_ERROR = "%s should either be a ''Mono'' class or class extends it if returns an ''async'' single value, " +
        "or a ''Response'' class or class extends it if returns a ''sync'' single value.";
    private static final String COLLECTION_RETURN_ERROR = "%s should either be a ''Flux'' class or class extends it if returns an ''async'' collection, " +
        "or a ''Stream'' class or class extends it if returns a ''sync'' collection.";

    private static final Set<String> COMMON_NAMING_PREFIX_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "upsert", "set", "create", "update", "replace", "delete", "add", "get", "list"
    )));

    private static final String COMMON_NAMING_SUFFIX = "Exists";

    private static boolean hasServiceClientAnnotation;
    private final Map<String, String> simpleClassNameToQualifiedNameMap = new HashMap<>();

    Class<?> monoObj;
    Class<?> fluxObj;
    Class<?> responseObj;

    @Override
    public void init() {
        try {
            fluxObj = Class.forName(FLUX);
        } catch (ClassNotFoundException ex) {
            log(0, String.format(FAILED_TO_LOAD_MESSAGE, FLUX));
        }

        try {
            monoObj = Class.forName(MONO);
        } catch (ClassNotFoundException ex) {
            log(0, String.format(FAILED_TO_LOAD_MESSAGE, MONO));
        }

        try {
            responseObj = Class.forName(RESPONSE);
        } catch (ClassNotFoundException ex) {
            log(0, String.format(FAILED_TO_LOAD_MESSAGE, RESPONSE));
        }
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
            TokenTypes.CLASS_DEF,
            TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void beginTree(DetailAST root) {
        hasServiceClientAnnotation = true;
    }

    @Override
    public void visitToken(DetailAST token) {
        if (!hasServiceClientAnnotation) {
            return;
        }

        switch (token.getType()) {
            case TokenTypes.IMPORT:
                addImportedClassPath(token);
                break;
            case TokenTypes.CLASS_DEF:
                hasServiceClientAnnotation = hasServiceClientAnnotation(token);
                break;
            case TokenTypes.METHOD_DEF:
                checkMethodNamingPattern(token);
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
        simpleClassNameToQualifiedNameMap.put(className, importClassPath);
    }

    /**
     *  Checks if the class is annotated with @ServiceClient annotation
     *
     * @param token the CLASS_DEF AST node
     * @return true if the class is annotated with @ServiceClient, false otherwise.
     */
    private boolean hasServiceClientAnnotation(DetailAST token) {
        DetailAST modifiersToken = token.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiersToken != null) {
            for (DetailAST ast = modifiersToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
                if (ast.getType() == TokenTypes.ANNOTATION) {
                    DetailAST annotationIdent = ast.findFirstToken(TokenTypes.IDENT);
                    if (annotationIdent != null && SERVICE_CLIENT.equals(annotationIdent.getText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void checkMethodNamingPattern(DetailAST methodDefToken) {
        DetailAST modifiersToken = methodDefToken.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiersToken == null) {
            return;
        }
        DetailAST serviceMethodAnnotation = hasServiceMethodAnnotation(modifiersToken);
        if (serviceMethodAnnotation == null) {
            return;
        }

        DetailAST methodNameToken = methodDefToken.findFirstToken(TokenTypes.IDENT);
        if (methodNameToken == null) {
            return;
        }
        String methodName = methodNameToken.getText();
        if (methodName.contains(ASYNC)) {
            log(methodNameToken, String.format("Method name ''%s'' should not contain ''%s'' in the method name",
                methodName, ASYNC));
        }

        if (!isCommonNamingPattern(methodName)) {
            log(methodDefToken, String.format("Method name ''%s'' should follow a common vocabulary. Refer to Java Spec. ", methodName));
        }

        DetailAST typeToken = methodDefToken.findFirstToken(TokenTypes.TYPE);
        if (typeToken == null) {
            log(typeToken, "Missing return type");
            return;
        }

        String returnTypeAnnotation = getReturnTypeFromAnnotation(serviceMethodAnnotation);
        String returnType = methodDefToken.findFirstToken(TokenTypes.TYPE).getText();

        if (!simpleClassNameToQualifiedNameMap.containsKey(returnType)) {
            if (SINGLE_RETURN_TYPE.equals(returnTypeAnnotation)) {
                log(typeToken, String.format(SINGLE_VALUE_RETURN_ERROR, SINGLE_RETURN_TYPE));
            } else if (COLLECTION_RETURN_TYPE.equals(returnTypeAnnotation)) {
                log(typeToken, String.format(COLLECTION_RETURN_ERROR, COLLECTION_RETURN_TYPE));
            }
        }

        String qualifiedReturnName = simpleClassNameToQualifiedNameMap.get(returnType);
        Class<?> qualifiedReturnTypeInstance;
        try {
            qualifiedReturnTypeInstance = Class.forName(qualifiedReturnName);
        } catch (ClassNotFoundException ex) {
            log(methodDefToken, String.format(FAILED_TO_LOAD_MESSAGE, qualifiedReturnName));
            return;
        }

        if (SINGLE_RETURN_TYPE.equals(returnTypeAnnotation)) {
            if (!qualifiedReturnTypeInstance.isInstance(monoObj)
                && !qualifiedReturnTypeInstance.isInstance(responseObj)) {
                log(methodDefToken, String.format(SINGLE_VALUE_RETURN_ERROR, SINGLE_RETURN_TYPE));
            }
        } else if (COLLECTION_RETURN_TYPE.equals(returnTypeAnnotation)) {
            if (!qualifiedReturnTypeInstance.isInstance(fluxObj)
                && !qualifiedReturnTypeInstance.isInstance(Stream.class)) {
                log(methodDefToken, String.format(COLLECTION_RETURN_ERROR, COLLECTION_RETURN_TYPE));
            }
        } else {
            log(serviceMethodAnnotation, String.format("''returns'' value = ''%s'' is neither SINGLE nor COLLECTION return type.", returnTypeAnnotation));
        }
    }


    private String getReturnTypeFromAnnotation(DetailAST annotationMemberValuePairToken) {

        DetailAST identToken = annotationMemberValuePairToken.findFirstToken(TokenTypes.IDENT);
        if (identToken == null || !RETURNS.equals(identToken.getText())) {
            return null;
        }

        String returnType = FullIdent.createFullIdentBelow(annotationMemberValuePairToken.findFirstToken(TokenTypes.EXPR)).getText();
        if (returnType != null && !returnType.isEmpty()) {
            return  returnType;
        }

        return null;
    }

    private DetailAST hasServiceMethodAnnotation(DetailAST modifiersToken) {
        for (DetailAST ast = modifiersToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() != TokenTypes.ANNOTATION) {
                continue;
            }

            DetailAST identToken = ast.findFirstToken(TokenTypes.IDENT);
            if (identToken == null || !SERVICE_METHOD.equals(identToken.getText())) {
                continue;
            }
            return ast;
        }

        return null;
    }

    private boolean isCommonNamingPattern(String methodName) {
        boolean isCommonNamingPattern = COMMON_NAMING_PREFIX_SET.stream().anyMatch(commonName ->
            methodName.startsWith(commonName));
        if (!isCommonNamingPattern) {
            isCommonNamingPattern = methodName.endsWith(COMMON_NAMING_SUFFIX);
        }
        return isCommonNamingPattern;
    }
}
