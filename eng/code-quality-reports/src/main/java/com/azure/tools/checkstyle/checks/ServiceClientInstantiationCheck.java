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
import java.util.stream.Stream;

/**
 * Verify the classes with annotation @ServiceClient should have following rules:
 * <ol>
 *   <li>No public or protected constructors</li>
 *   <li>No public static method named 'builder'</li>
 *   <li>Since these classes are supposed to be immutable, all fields in the service client classes should be final.</li>
 * </ol>
 *
 * All methods that has a @ServiceMethod annotation in a class annotated with @ServiceClient should follow below rules:
 * <ol>
 *   <li>Method naming pattern. Refer to Java Spec:</li>
 *   <li>Methods should not have "Async" added to the method name</li>
 *   <ol>Return type of async and sync clients should be as per guidelines:
 *     <li>Return type for async collection should be of type? extends Flux</li>
 *     <li>Return type for async single value should be of type? extends Mono</li>
 *     <li>Return type for sync collection should be of type? extends Stream</li>
 *     <li>Return type for sync single value should be of type? extends Response</li>
 *   </ol>
 * </ol>
 */
public class ServiceClientInstantiationCheck extends AbstractCheck {
    private static final String ASYNC = "Async";
    private static final String ASYNC_CLIENT ="AsyncClient";
    private static final String BUILDER = "builder";
    private static final String CLIENT = "Client";
    private static final String IS_ASYNC = "isAsync";
    private static final String SERVICE_CLIENT = "ServiceClient";

    private static final String COLLECTION_RETURN_TYPE = "ReturnType.COLLECTION";
    private static final String SINGLE_RETURN_TYPE = "ReturnType.SINGLE";

    private static final String FLUX = "reactor.core.publisher.Flux";
    private static final String MONO = "reactor.core.publisher.Mono";
    private static final String RESPONSE = "com.azure.core.http.rest.response";

    private static final String COLLECTION_RETURN_ERROR = "%s should either be a ''Flux'' class or class extends it if returns an ''async'' collection, " +
        "or a ''Stream'' class or class extends it if returns a ''sync'' collection.";
    private static final String FAILED_TO_LOAD_MESSAGE = "%s class failed to load, ServiceClientChecks will be ignored.";
    private static final String SINGLE_VALUE_RETURN_ERROR = "%s should either be a ''Mono'' class or class extends it if returns an ''async'' single value, " +
        "or a ''Response'' class or class extends it if returns a ''sync'' single value.";

    private static final Set<String> COMMON_NAMING_PREFIX_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "upsert", "set", "create", "update", "replace", "delete", "add", "get", "list"
    )));

    private static boolean isAsync;
    private static boolean hasServiceClientAnnotation;
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
    public void beginTree(DetailAST root) {
        hasServiceClientAnnotation = false;
        isAsync = false;
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.IMPORT:
                addImportedClassPath(token);
                break;
            case TokenTypes.CLASS_DEF:
                hasServiceClientAnnotation = hasServiceClientAnnotation(token);
                if (hasServiceClientAnnotation) {
                    checkServiceClientNaming(token);
                }
                break;
            case TokenTypes.CTOR_DEF:
                if (hasServiceClientAnnotation) {
                    checkConstructor(token);
                }
                break;
            case TokenTypes.METHOD_DEF:
                if (hasServiceClientAnnotation) {
                    checkMethodName(token);
                    checkMethodNamingPattern(token);
                }
                break;
            case TokenTypes.OBJBLOCK:
                if (hasServiceClientAnnotation) {
                    checkClassField(token);
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
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

        for (DetailAST ast = modifiersToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() != TokenTypes.ANNOTATION) {
                continue;
            }
            // One class could have multiple annotations, return true if found one.
            final DetailAST annotationIdent = ast.findFirstToken(TokenTypes.IDENT);
            if (annotationIdent != null && SERVICE_CLIENT.equals(annotationIdent.getText())) {
                isAsync = isAsyncServiceClient(ast);
                return true;
            }
        }
        // If no @ServiceClient annotated with this class, return false
        return false;
    }

    /**
     *  Checks for public or protected constructor for the service client class.
     *  Log error if the service client has public or protected constructor.
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
    private void checkMethodName(DetailAST methodDefToken) {
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
        for (DetailAST ast = objBlockToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (TokenTypes.VARIABLE_DEF != ast.getType()) {
                continue;
            }
            final DetailAST modifiersToken = ast.findFirstToken(TokenTypes.MODIFIERS);
            // VARIABLE_DEF token will always MODIFIERS token. If there is no modifier at the variable, no child under
            // MODIFIERS token. Also the previous sibling of OBJBLOCK will always be class name IDENT node.
            if (!modifiersToken.branchContains(TokenTypes.FINAL)) {
                log(modifiersToken, String.format("The variable field ''%s'' of class ''%s'' should be final. Classes annotated with @ServiceClient are supposed to be immutable.",
                    ast.findFirstToken(TokenTypes.IDENT).getText(), objBlockToken.getPreviousSibling().getText()));
            }
        }
    }

    /**
     * Checks for the class name of Service Client. It should be named <ServiceName>AsyncClient or <ServiceName>Client.
     *
     * @param classDefToken the CLASS_DEF AST node
     */
    private void checkServiceClientNaming(DetailAST classDefToken) {
        final String className = classDefToken.findFirstToken(TokenTypes.IDENT).getText();
        // Async service client
        if (isAsync && !className.endsWith(ASYNC_CLIENT)) {
            log(classDefToken, String.format("Async class ''%s'' must be named <ServiceName>AsyncClient ", className));
        }
        // Sync service client
        if (!isAsync && !className.endsWith(CLIENT)) {
            log(classDefToken, String.format("Sync class %s must be named <ServiceName>Client.", className));
        }
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


    private void checkMethodNamingPattern(DetailAST methodDefToken) {
        DetailAST modifiersToken = methodDefToken.findFirstToken(TokenTypes.MODIFIERS);
        DetailAST serviceMethodAnnotation = hasServiceMethodAnnotation(modifiersToken);
        // NOT a @ServiceMethod method
        if (serviceMethodAnnotation == null) {
            return;
        }

        String methodName = methodDefToken.findFirstToken(TokenTypes.IDENT).getText();
        if (methodName.contains(ASYNC)) {
            log(methodDefToken, String.format("Method name ''%s'' should not contain ''%s'' in the method name",
                methodName, ASYNC));
        }

        if (!isCommonNamingPattern(methodName)) {
            log(methodDefToken, String.format("Method name ''%s'' should follow a common vocabulary. Refer to Java Spec. ", methodName));
        }

        // Find the annotation member 'returns' value
        String returnsAnnotationMemberValue = getAnnotationMemberReturnsValue(serviceMethodAnnotation);

        String returnType = methodDefToken.findFirstToken(TokenTypes.TYPE).getText();
        if (!simpleClassNameToQualifiedNameMap.containsKey(returnType)) {
            if (SINGLE_RETURN_TYPE.equals(returnsAnnotationMemberValue)) {
                log(methodDefToken, String.format(SINGLE_VALUE_RETURN_ERROR, SINGLE_RETURN_TYPE));
            } else if (COLLECTION_RETURN_TYPE.equals(returnsAnnotationMemberValue)) {
                log(methodDefToken, String.format(COLLECTION_RETURN_ERROR, COLLECTION_RETURN_TYPE));
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

        if (SINGLE_RETURN_TYPE.equals(returnsAnnotationMemberValue)) {
            if (!qualifiedReturnTypeInstance.isInstance(monoObj)
                && !qualifiedReturnTypeInstance.isInstance(responseObj)) {
                log(methodDefToken, String.format(SINGLE_VALUE_RETURN_ERROR, SINGLE_RETURN_TYPE));
            }
        } else if (COLLECTION_RETURN_TYPE.equals(returnsAnnotationMemberValue)) {
            if (!qualifiedReturnTypeInstance.isInstance(fluxObj)
                && !qualifiedReturnTypeInstance.isInstance(Stream.class)) {
                log(methodDefToken, String.format(COLLECTION_RETURN_ERROR, COLLECTION_RETURN_TYPE));
            }
        } else {
            log(serviceMethodAnnotation, String.format("''returns'' value = ''%s'' is neither SINGLE nor COLLECTION return type.", returnsAnnotationMemberValue));
        }
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
     *
     * @param modifiersToken
     * @return
     */
    private DetailAST hasServiceMethodAnnotation(DetailAST modifiersToken) {
        for (DetailAST ast = modifiersToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() != TokenTypes.ANNOTATION) {
                continue;
            }

            DetailAST identToken = ast.findFirstToken(TokenTypes.IDENT);
            if (identToken == null || !"ServiceMethod".equals(identToken.getText())) {
                continue;
            }
            return ast;
        }

        return null;
    }

    /**
     *
     * @param methodName
     * @return
     */
    private boolean isCommonNamingPattern(String methodName) {
        boolean isCommonNamingPattern = COMMON_NAMING_PREFIX_SET.stream().anyMatch(
            commonName -> methodName.startsWith(commonName));
        if (!isCommonNamingPattern) {
            isCommonNamingPattern = methodName.endsWith("Exists");
        }
        return isCommonNamingPattern;
    }

    /**
     * Find the annotation member 'returns' value
     *
     * @param serviceMethodAnnotation ANNOTATION_MEMBER_VALUE_PAIR AST node
     * @return annotation member 'returns' value if found, null otherwise.
     */
    private String getAnnotationMemberReturnsValue(DetailAST serviceMethodAnnotation) {
        for (DetailAST annotationChild = serviceMethodAnnotation.getFirstChild(); annotationChild != null;
             annotationChild = annotationChild.getNextSibling()) {
            // Skip if not ANNOTATION_MEMBER_VALUE_PAIR
            if (annotationChild.getType() != TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
                continue;
            }
            // Skip if the annotation member is not 'returns'
            String annotationParamName = annotationChild.findFirstToken(TokenTypes.IDENT).getText();
            if (!"returns".equals(annotationParamName)) {
                continue;
            }
            // value of Annotation member 'returns'
            String returnsValue = FullIdent.createFullIdentBelow(annotationChild.findFirstToken(TokenTypes.EXPR)).getText();
            if (returnsValue != null && !returnsValue.isEmpty()) {
                return returnsValue;
            }
        }
        return null;
    }
}
