// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.naming.AccessModifier;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;

/**
 * Verify the classes with annotation @ServiceClient should have following rules:
 * <0l>
 *   <li>No public or protected constructors</li>
 *   <li>No public static method named 'builder'</li>
 *   <li>Since these classes are supposed to be immutable, all fields in the service client classes should be final.</li>
 * </0l>
 */
public class ServiceClientInstantiationCheck extends AbstractCheck {
    private static final String SERVICE_CLIENT = "ServiceClient";
    private static final String BUILDER = "builder";
    private static final String ASYNC_CLIENT ="AsyncClient";
    private static final String CLIENT = "Client";
    private static final String IS_ASYNC = "isAsync";

    private static boolean hasServiceClientAnnotation;
    private static boolean isAsync;

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
            TokenTypes.CLASS_DEF,
            TokenTypes.CTOR_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.OBJBLOCK
        };
    }

    @Override
    public void beginTree(DetailAST root) {
        hasServiceClientAnnotation = true;
        isAsync = false;
    }

    @Override
    public void visitToken(DetailAST token) {
        if (!hasServiceClientAnnotation) {
            return;
        }

        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                hasServiceClientAnnotation = hasServiceClientAnnotation(token);
                checkServiceClientNaming(token);
                break;
            case TokenTypes.CTOR_DEF:
                checkConstructor(token);
                break;
            case TokenTypes.METHOD_DEF:
                checkMethodName(token);
                break;
            case TokenTypes.OBJBLOCK:
                checkClassField(token);
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
     * Checks for the variable field of the subclass of ServiceClient.
     * These fields should be final because these classes supposed to be immutable class.
     *
     * @param objBlockToken the OBJBLOCK AST node
     */
    private void checkClassField(DetailAST objBlockToken) {
        for (DetailAST ast = objBlockToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (TokenTypes.VARIABLE_DEF != ast.getType()) {
                continue;
            }
            final DetailAST modifiersToken = ast.findFirstToken(TokenTypes.MODIFIERS);
            //VARIABLE_DEF token will always MODIFIERS token. If there is no modifier at the variable, no child under
            // MODIFIERS token
            if (!modifiersToken.branchContains(TokenTypes.FINAL)) {
                log(modifiersToken, String.format("The variable field ''%s'' of @ServiceClient should be final. The class annotated with @ServiceClient supposed to be immutable.",
                    ast.findFirstToken(TokenTypes.IDENT).getText()));
            }
        }
    }

    /**
     * Checks for the class name of Service Client. It should be named <ServiceName>AsyncClient or <ServiceName>Client.
     *
     * @param classDefToken the CLASS_DEF AST node
     */
    private void checkServiceClientNaming(DetailAST classDefToken) {
        if (!hasServiceClientAnnotation) {
            return;
        }

        final String className = classDefToken.findFirstToken(TokenTypes.IDENT).getText();
        // Async service client
        if (isAsync && !className.endsWith(ASYNC_CLIENT)) {
            log(classDefToken, String.format("Async class ''%s'' should named <ServiceName>AsyncClient ", className));
        }
        // Sync service client
        if (!isAsync && !className.endsWith(CLIENT)) {
            log(classDefToken, String.format("Sync class %s should named <ServiceName>Client.", className));
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
}
