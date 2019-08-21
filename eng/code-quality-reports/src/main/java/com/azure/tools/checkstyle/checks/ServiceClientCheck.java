// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.naming.AccessModifier;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Verify the classes with annotation @ServiceClient should have following rules:
 * <ol>
 * <li>No public or protected constructors</li>
 * <li>No public static method named 'builder'</li>
 * <li>Since these classes are supposed to be immutable, all fields in the service client classes should be final</li>
 * </ol>
 *
 * All methods that has a @ServiceMethod annotation in a class annotated with @ServiceClient should follow below rules:
 * <ol>
 * <li>Follows method naming pattern. Refer to <a href="https://azure.github.io/azure-sdk/java_introduction.html">Java Spec:</a></li>
 * <li>Methods should not have "Async" added to the method name</li>
 * <li>Return type of async and sync clients should be as per guidelines:
 * <ol>
 * <li>The return type for async collection should be of type that <code>extends PagedFlux</code></li>
 * <li>The return type for async single value should be of type that <code>extends Mono</code></li>
 * <li>The return type for sync collection should be of type that <code>extends PagedIterable</code></li>
 * <li>The return type for sync single value should be of type that <code>extends Response</code></li>
 * </ol>
 * </li>
 * </ol>
 */
public class ServiceClientCheck extends AbstractCheck {
    private static final String ASYNC = "Async";
    private static final String SERVICE_CLIENT = "ServiceClient";
    private static final String BUILDER = "builder";
    private static final String ASYNC_CLIENT = "AsyncClient";
    private static final String CLIENT = "Client";
    private static final String IS_ASYNC = "isAsync";
    private static final String CONTEXT = "Context";

    private static final String RESPONSE_BRACKET = "Response<";
    private static final String MONO_BRACKET = "Mono<";
    private static final String MONO_RESPONSE_BRACKET = "Mono<Response<";
    private static final String PAGED_FLUX_BRACKET = "PagedFlux<";
    private static final String PAGED_ITERABLE_BRACKET = "PagedIterable<";

    private static final String WITH_RESPONSE = "WithResponse";

    private static final String COLLECTION_RETURN_TYPE = "ReturnType.COLLECTION";
    private static final String SINGLE_RETURN_TYPE = "ReturnType.SINGLE";

    private static final String JAVA_SPEC_LINK = "https://azure.github.io/azure-sdk/java_introduction.html";
    private static final String PAGED_FLUX = "PagedFlux";
    private static final String MONO = "Mono";
    private static final String RESPONSE = "Response";
    private static final String PAGED_ITERABLE = "PagedIterable";

    private static final String RETURN_TYPE_WITH_RESPONSE_ERROR = "Return type is ''%s'', the method name must %s end with ''%s''.";
    private static final String RETURN_TYPE_ERROR =  "''%s'' service client with ''%s'' should use type ''%s'' as the return type.";
    private static final String RESPONSE_METHOD_NAME_ERROR = "''%s'' service client with ''%s', should always use return type ''%s'' if method name ends with ''%s'' or should always named method name ends with ''%s'' if the return type is ''%s''.";
    private static final String ASYNC_CONTEXT_ERROR = "Asynchronous method with annotation @ServiceMethod must not has ''%s'' as a method parameter.";
    private static final String SYNC_CONTEXT_ERROR = "Synchronous method with annotation @ServiceMethod must has ''%s'' as a method parameter.";

    private static final Set<String> COMMON_NAMING_PREFIX_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "upsert", "set", "create", "update", "replace", "delete", "add", "get", "list"
    )));

    private boolean isAsync;
    private boolean isServiceClientAnnotation;
    private final Map<String, String> simpleClassNameToQualifiedNameMap = new HashMap<>();

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
            TokenTypes.CTOR_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.OBJBLOCK
        };
    }

    @Override
    public void beginTree(DetailAST root) {
        isServiceClientAnnotation = false;
        isAsync = false;
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.IMPORT:
                addImportedClassPath(token);
                break;
            case TokenTypes.CLASS_DEF:
                isServiceClientAnnotation = hasServiceClientAnnotation(token);
                if (!isServiceClientAnnotation) {
                    return;
                }
                checkServiceClientNaming(token);
                break;
            case TokenTypes.CTOR_DEF:
                if (!isServiceClientAnnotation) {
                    return;
                }
                checkConstructor(token);
                break;
            case TokenTypes.METHOD_DEF:
                if (!isServiceClientAnnotation) {
                    return;
                }
                checkMethodNameBuilder(token);
                checkMethodNamingPattern(token);
                break;
            case TokenTypes.OBJBLOCK:
                if (!isServiceClientAnnotation) {
                    return;
                }
                checkClassField(token);
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Checks for public or protected constructor for the service client class.
     * Log error if the service client has public or protected constructor.
     *
     * @param ctorToken the CTOR_DEF AST node
     */
    private void checkConstructor(DetailAST ctorToken) {
        final DetailAST modifiersToken = ctorToken.findFirstToken(TokenTypes.MODIFIERS);
        // find constructor's modifier accessibility, no public or protected constructor
        final AccessModifier accessModifier = CheckUtil.getAccessModifierFromModifiersToken(modifiersToken);
        if (accessModifier.equals(AccessModifier.PUBLIC) || accessModifier.equals(AccessModifier.PROTECTED)) {
            log(modifiersToken, "@ServiceClient class should not have any public or protected constructor.");
        }
    }

    /**
     * Checks for public static method named 'builder'. Should avoid to use method name, 'builder'.
     *
     * @param methodDefToken the METHOD_DEF AST node
     */
    private void checkMethodNameBuilder(DetailAST methodDefToken) {
        final DetailAST methodNameToken = methodDefToken.findFirstToken(TokenTypes.IDENT);
        if (!BUILDER.equals(methodNameToken.getText())) {
            return;
        }

        final DetailAST modifiersToken = methodDefToken.findFirstToken(TokenTypes.MODIFIERS);
        // find method's modifier accessibility, should not have a public static method called 'builder'
        final AccessModifier accessModifier = CheckUtil.getAccessModifierFromModifiersToken(modifiersToken);
        if (accessModifier.equals(AccessModifier.PUBLIC) && modifiersToken.branchContains(TokenTypes.LITERAL_STATIC)) {
            log(modifiersToken, "@ServiceClient class should not have a public static method named ''builder''.");
        }
    }

    /**
     * Checks that the field variables in the @ServiceClient are final. ServiceClients should be immutable.
     *
     * @param objBlockToken the OBJBLOCK AST node
     */
    private void checkClassField(DetailAST objBlockToken) {
        final Optional<DetailAST> varDefTokenOption = TokenUtil.findFirstTokenByPredicate(objBlockToken, node ->
            node.getType() == TokenTypes.VARIABLE_DEF && !node.findFirstToken(TokenTypes.MODIFIERS).branchContains(TokenTypes.FINAL));
        if (varDefTokenOption.isPresent()) {
            final DetailAST varDefToken = varDefTokenOption.get();
            final String varName = varDefToken.findFirstToken(TokenTypes.IDENT).getText();
            log(varDefToken, String.format("The variable field ''%s'' of class ''%s'' should be final. Classes "
                + "annotated with @ServiceClient are supposed to be immutable.", varName, objBlockToken.getPreviousSibling().getText()));
        }
    }

    /**
     * Checks for the class name of Service Client. It should be named <ServiceName>AsyncClient or <ServiceName>Client.
     *
     * @param classDefToken the CLASS_DEF AST node
     */
    private void checkServiceClientNaming(DetailAST classDefToken) {
        final String className = classDefToken.findFirstToken(TokenTypes.IDENT).getText();
        // Async client must be named <ServiceName>AsyncClient, and Sync client must be named <ServiceName>Client
        if (isAsync && !className.endsWith(ASYNC_CLIENT)) {
            log(classDefToken, String.format("Asynchronous class ''%s'' must be named <ServiceName>AsyncClient, which "
                + "concatenates by service name and a fixed word 'AsyncClient'.", className));
        } else if (!isAsync && !className.endsWith(CLIENT)) {
            log(classDefToken, String.format("Synchronous class %s must be named <ServiceName>Client,"
                + " which concatenates by service name and a fixed word 'Client'.", className));
        }

        // Class named <ServiceName>AsyncClient, the property 'isAsync' must set to true
        // Class named <ServiceName>Client, the property 'isAsync' must to be false or use the default value
        if (className.endsWith(ASYNC_CLIENT) && !isAsync) {
            log(classDefToken, String.format("Asynchronous Client, class ''%s'' must set property ''%s'' to true.", className, IS_ASYNC));
        } else if (className.endsWith(CLIENT) && !className.endsWith(ASYNC_CLIENT) && isAsync) {
            log(classDefToken, String.format("Synchronous Client, class ''%s'' must set property ''%s'' to false or without the property.", className, IS_ASYNC));
        }
    }

    /**
     * Verify all methods that have a @ServiceMethod annotation in a class annotated with @ServiceClient should
     *    follow below rules:
     *    1) Follows method naming pattern. Refer to Java Spec.
     *    2) Methods should not have "Async" added to the method name.
     *    3) The return type of async and sync clients should be as per guidelines:
     *      3.1) The return type for async collection should be of type? extends PagedFlux.
     *      3.2) The return type for async single value should be of type? extends Mono.
     *      3.3) The return type for sync collection should be of type? extends PagedIterable.
     *      3.4) The return type for sync single value should be of type? extends Response.
     *    4) naming pattern for 'WithResponse'.
     *    5) Synchronous method with annotation @ServiceMethod has to have {@code Context} as a parameter.
     *    Asynchronous method with annotation @ServiceMethod must not has {@code Context} as a parameter.
     *
     * @param methodDefToken METHOD_DEF AST node
     */
    private void checkMethodNamingPattern(DetailAST methodDefToken) {
        final DetailAST modifiersToken = methodDefToken.findFirstToken(TokenTypes.MODIFIERS);
        final Optional<DetailAST> serviceMethodAnnotationOption = TokenUtil.findFirstTokenByPredicate(modifiersToken,
            node -> node.getType() == TokenTypes.ANNOTATION && node.findFirstToken(TokenTypes.IDENT) != null
                && node.findFirstToken(TokenTypes.IDENT).getText().equals("ServiceMethod"));
        // NOT a @ServiceMethod method
        if (!serviceMethodAnnotationOption.isPresent()) {
            return;
        }

        final DetailAST serviceMethodAnnotation = serviceMethodAnnotationOption.get();
        final String methodName = methodDefToken.findFirstToken(TokenTypes.IDENT).getText();

        // 1) Follows method naming pattern. Refer to Java Spec.
        // prefix of method name that contains all lower letters
        final String prefix = methodName.split("[A-Z]", 2)[0];
        if (!methodName.endsWith("Exists") && !COMMON_NAMING_PREFIX_SET.contains(prefix)) {
            log(methodDefToken, String.format("Method name ''%s'' should follow a common vocabulary. Refer to Java Spec: %s.",
                methodName, JAVA_SPEC_LINK));
        }

        // 2) Methods should not have "Async" added to the method name
        if (methodName.contains(ASYNC)) {
            log(methodDefToken, String.format("Method name ''%s'' should not contain ''%s'' in the method name.",
                methodName, ASYNC));
        }

        // 3) The return type of async and sync clients should be as per guidelines
        checkServiceClientMethodReturnType(methodDefToken, serviceMethodAnnotation, methodName);

        // 4) Check 'withResponse' naming pattern
        checkReturnTypeNamingPattern(methodDefToken, methodName);

        // 5) Synchronous method with annotation @ServiceMethod has to have {@code Context} as a parameter.
        // Asynchronous method with annotation @ServiceMethod must not has {@code Context} as a parameter.
        checkContextInRightPlace(methodDefToken);
    }

    /**
     * Checks for the return type of async and sync clients should be as per guidelines:
     *     1) The return type for async collection should be of type? extends PagedFlux
     *     2) The return type for async single value should be of type? extends Mono
     *     3) The return type for sync collection should be of type? extends PagedIterable
     *     4) The return type for sync single value should be of type? extends Response
     *
     * @param methodDefToken METHOD_DEF AST node
     * @param serviceMethodAnnotation ANNOTATION AST node which used to find the if the annotation has 'return' key,
     * @param methodName method name
     * if found. return the value of member'return'.
     */
    private void checkServiceClientMethodReturnType(DetailAST methodDefToken, DetailAST serviceMethodAnnotation, String methodName) {
        // Find the annotation member 'returns' value
        String returnsAnnotationMemberValue = null;
        final Optional<DetailAST> returnValueOption = TokenUtil.findFirstTokenByPredicate(serviceMethodAnnotation, node ->
            node.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR && node.findFirstToken(TokenTypes.IDENT).getText().equals("returns")
                && !FullIdent.createFullIdentBelow(node.findFirstToken(TokenTypes.EXPR)).getText().isEmpty());

        if (returnValueOption.isPresent()) {
            returnsAnnotationMemberValue = FullIdent.createFullIdentBelow(returnValueOption.get().findFirstToken(TokenTypes.EXPR)).getText();
        }

        final String returnType = getReturnType(methodDefToken.findFirstToken(TokenTypes.TYPE), new StringBuilder()).toString();

        if (isAsync) {
            if (SINGLE_RETURN_TYPE.equals(returnsAnnotationMemberValue)) {
                // If value of 'returns' is SINGLE, and then log error if the return type of the method is not start with {@code Mono<T>}
                if (!returnType.startsWith(MONO_BRACKET)) {
                    log(methodDefToken, String.format(RETURN_TYPE_ERROR, "Asynchronous", SINGLE_RETURN_TYPE, MONO));
                }
            } else if (COLLECTION_RETURN_TYPE.equals(returnsAnnotationMemberValue)) {
                // If value of 'returns' is COLLECTION, and then log error if the return type of the method is not start with {@code PagedFlux<T>}
                if (!returnType.startsWith(PAGED_FLUX_BRACKET)) {
                    log(methodDefToken, String.format(RETURN_TYPE_ERROR, "Asynchronous", COLLECTION_RETURN_TYPE, PAGED_FLUX));
                }
            }
        } else {
            if (SINGLE_RETURN_TYPE.equals(returnsAnnotationMemberValue)) {
                // If value of 'returns' is SINGLE, and then log error if the return type of the method is not start
                // with {@code Response<T>} when the method name ends with 'WithResponse'.
                if (returnType.startsWith(RESPONSE_BRACKET) && !methodName.endsWith(WITH_RESPONSE)
                    || !returnType.startsWith(RESPONSE_BRACKET) && methodName.endsWith(WITH_RESPONSE)) {
                    log(methodDefToken, String.format(RESPONSE_METHOD_NAME_ERROR, "Synchronous", SINGLE_RETURN_TYPE,
                        RESPONSE, WITH_RESPONSE, WITH_RESPONSE, RESPONSE));
                }
            } else if (COLLECTION_RETURN_TYPE.equals(returnsAnnotationMemberValue)) {
                // If value of 'returns' is COLLECTION, and then log error if the return type of the method is not start with {@code PagedIterable<T>}
                if (!returnType.startsWith(PAGED_ITERABLE_BRACKET)) {
                    log(methodDefToken, String.format(RETURN_TYPE_ERROR, "Synchronous", COLLECTION_RETURN_TYPE, PAGED_ITERABLE));
                }
            }
        }
    }

    /**
     * Given the method is already annotated @ServiceMethod. Checks if the return type is {@code Response<T>} or
     * {@code Mono<Response<T>>},
     * Sync:
     *  If the return type is Response<T>, the method name must end with WithResponse.
     *  If the return type is T, the method name must NOT end with WithResponse.
     * Async:
     *  If the return type is Mono<Response<T>>, the method name must end with WithResponse.
     *  If the return type is Mono<T>, the method name must NOT end with WithResponse.
     *
     * @param methodDefToken METHOD_DEF AST node
     */
    private void checkReturnTypeNamingPattern(DetailAST methodDefToken, String methodName) {
        final DetailAST typeToken = methodDefToken.findFirstToken(TokenTypes.TYPE);
        // Use recursion to get the return type
        final String returnType = getReturnType(typeToken, new StringBuilder()).toString();

        if (methodName.endsWith(WITH_RESPONSE)) {
            if (!returnType.startsWith(RESPONSE_BRACKET) && !returnType.startsWith(MONO_RESPONSE_BRACKET)) {
                log(methodDefToken, String.format(RETURN_TYPE_WITH_RESPONSE_ERROR, returnType, "", WITH_RESPONSE));
            }
        } else {
            if (returnType.startsWith(RESPONSE_BRACKET) || returnType.startsWith(MONO_RESPONSE_BRACKET)) {
                log(methodDefToken, String.format(RETURN_TYPE_WITH_RESPONSE_ERROR, returnType, "not", WITH_RESPONSE));
            }
        }
    }

    /**
     * Checks the type Context should be in the right place.
     * Synchronous method with annotation @ServiceMethod has to have {@code Context} as a parameter.
     * Asynchronous method with annotation @ServiceMethod must not has {@code Context} as a parameter.
     *
     * @param methodDefToken METHOD_DEF AST token
     */
    private void checkContextInRightPlace(DetailAST methodDefToken) {
        final DetailAST parametersToken = methodDefToken.findFirstToken(TokenTypes.PARAMETERS);
        final String returnType = getReturnType(methodDefToken, new StringBuilder()).toString();

        final boolean containsTypeParameter = TokenUtil.findFirstTokenByPredicate(parametersToken,
            node -> node.getType() == TokenTypes.PARAMETER_DEF && node.findFirstToken(TokenTypes.TYPE).findFirstToken(TokenTypes.IDENT) != null
            && node.findFirstToken(TokenTypes.TYPE).findFirstToken(TokenTypes.IDENT).getText().equals(CONTEXT)).isPresent();

        if (containsTypeParameter) {
            if (returnType.startsWith(MONO_BRACKET) || returnType.startsWith(PAGED_FLUX_BRACKET)) {
                log(methodDefToken, String.format(ASYNC_CONTEXT_ERROR, CONTEXT));
            }
        } else {
            if (returnType.startsWith(RESPONSE_BRACKET) || returnType.startsWith(PAGED_ITERABLE_BRACKET)) {
                log(methodDefToken, String.format(SYNC_CONTEXT_ERROR, CONTEXT));
            }
        }
    }

    /**
     * Checks if the class is annotated with annotation @ServiceClient. A class could have multiple annotations.
     *
     * @param classDefToken the CLASS_DEF AST node
     * @return true if the class is annotated with @ServiceClient, false otherwise.
     */
    private boolean hasServiceClientAnnotation(DetailAST classDefToken) {
        // Always has MODIFIERS node
        final DetailAST modifiersToken = classDefToken.findFirstToken(TokenTypes.MODIFIERS);
        final Optional<DetailAST> serviceClientAnnotationOption = TokenUtil.findFirstTokenByPredicate(modifiersToken,
            node -> node.getType() == TokenTypes.ANNOTATION && node.findFirstToken(TokenTypes.IDENT) != null
                && SERVICE_CLIENT.equals(node.findFirstToken(TokenTypes.IDENT).getText()));

        if (serviceClientAnnotationOption.isPresent()) {
            isAsync = isAsyncServiceClient(serviceClientAnnotationOption.get());
            return true;
        }
        // If no @ServiceClient annotated with this class, return false
        return false;
    }

    /**
     * Add all imported classes into a map, key is the name of class and value is the full package path of class.
     *
     * @param token the IMPORT AST node
     */
    private void addImportedClassPath(DetailAST token) {
        final String importClassPath = FullIdent.createFullIdentBelow(token).getText();
        final String className = importClassPath.substring(importClassPath.lastIndexOf(".") + 1);
        simpleClassNameToQualifiedNameMap.put(className, importClassPath);
    }

    /**
     * A function checks if the annotation node has a member key is {@code IS_ASYNC} with value equals to 'true'.
     * If the value equals 'true', which indicates the @ServiceClient is an asynchronous client.
     * If the member pair is missing. By default, it is a synchronous service client.
     *
     * @param annotationToken the ANNOTATION AST node
     * @return true if the annotation has {@code IS_ASYNC} value 'true', otherwise, false.
     */
    private boolean isAsyncServiceClient(DetailAST annotationToken) {
        for (DetailAST ast = annotationToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() != TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
                continue;
            }

            // skip this annotation member value pair if no IDENT found, since we are looking for member, 'isAsync'.
            final DetailAST identToken = ast.findFirstToken(TokenTypes.IDENT);
            if (identToken == null) {
                continue;
            }

            // skip this annotation member value pair if the member is not 'isAsync'.
            if (!IS_ASYNC.equals(identToken.getText())) {
                continue;
            }

            // skip this annotation member value pair if the member has no EXPR value
            final DetailAST exprToken = ast.findFirstToken(TokenTypes.EXPR);
            if (exprToken == null) {
                continue;
            }

            // true if isAsync = true, false otherwise.
            return exprToken.branchContains(TokenTypes.LITERAL_TRUE);
        }
        // By default, if the IS_ASYNC doesn't exist, the service client is a synchronous client.
        return false;
    }

    /**
     * Get full name of return type. Such as Response, Mono<Response>.
     *
     * @param token a token could be a TYPE, TYPE_ARGUMENT, TYPE_ARGUMENTS token
     * @param sb a StringBuilder that used to collect method return type.
     */
    private StringBuilder getReturnType(DetailAST token, StringBuilder sb) {
        for (DetailAST currentToken = token.getFirstChild(); currentToken != null; currentToken = currentToken.getNextSibling()) {
            switch (currentToken.getType()) {
                case TokenTypes.TYPE_ARGUMENT:
                case TokenTypes.TYPE_ARGUMENTS:
                    getReturnType(currentToken, sb);
                    break;
                default:
                    sb.append(currentToken.getText());
            }
        }
        return sb;
    }
}
