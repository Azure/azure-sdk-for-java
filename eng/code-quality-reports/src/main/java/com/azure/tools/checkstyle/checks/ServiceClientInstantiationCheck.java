// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.xml.soap.Detail;

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

    private static boolean hasServiceClientAnnotation;
    private static boolean notServiceClass;

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
            TokenTypes.LITERAL_CLASS,
            TokenTypes.OBJBLOCK
        };
    }

    @Override
    public void beginTree(DetailAST root) {
        notServiceClass = false;
    }

    @Override
    public void visitToken(DetailAST token) {
        if (notServiceClass) {
            return;
        }

        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                hasServiceClientAnnotation = hasServiceClientAnnotation(token);
                if (!hasServiceClientAnnotation) {
                    notServiceClass = true;
                }
                break;
            case TokenTypes.CTOR_DEF:
                checkNonPublicOrProtectedConstructor(token);
                break;
            case TokenTypes.METHOD_DEF:
                checkNonPublicStaticBuilderMethodName(token);
                break;
            case TokenTypes.LITERAL_CLASS:
                checkServiceClientNaming(token);
                break;
            case TokenTypes.OBJBLOCK:
                checkClassFieldFinal(token);
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     *  Check if the class is annotated with @ServiceClient
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

    /**
     *  Check fo public or protected constructor for the service client class.
     *  Log error if the service client has public or protected constructor.
     *
     * @param token the CTOR_DEF AST node
     */
    private void checkNonPublicOrProtectedConstructor(DetailAST token) {
        DetailAST modifiersToken = token.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiersToken != null) {
            if (modifiersToken.branchContains(TokenTypes.LITERAL_PUBLIC)
                || modifiersToken.branchContains(TokenTypes.LITERAL_PROTECTED)) {
                log(modifiersToken, "Subclass of ServiceClient should not have any public or protected constructor.");
            }
        }
    }

    /**
     * Check for public static method named 'builder'. Should avoid to use method name, 'builder'.
     *
     * @param token the METHOD_DEF AST node
     */
    private void checkNonPublicStaticBuilderMethodName(DetailAST token) {
        DetailAST methodNameToken = token.findFirstToken(TokenTypes.IDENT);
        if (methodNameToken != null && BUILDER.equals(methodNameToken.getText())) {
            DetailAST modifiersToken = token.findFirstToken(TokenTypes.MODIFIERS);
            if (modifiersToken.branchContains(TokenTypes.LITERAL_PUBLIC)
                && modifiersToken.branchContains(TokenTypes.LITERAL_STATIC)) {
                log(modifiersToken, "Subclass of ServiceClient should not have a public static method named ''builder''.");
            }
        }
    }

    /**
     * Check for the variable field of the subclass of ServiceClient.
     * These fields should be final because these classes supposed to be immutable class.
     *
     * @param token the MODIFIERS AST node
     */
    private void checkClassFieldFinal(DetailAST token) {
        for (DetailAST ast = token.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (TokenTypes.VARIABLE_DEF == ast.getType()) {
                DetailAST modifiersToken = ast.findFirstToken(TokenTypes.MODIFIERS);
                if (!modifiersToken.branchContains(TokenTypes.FINAL)) {
                    log(modifiersToken, "The variable field of the subclass of ServiceClient should be final. These classes supposed to be immutable.");
                }
            }
        }
    }

    /**
     * Check for the class name of Service Client. It class should be named <ServiceName>AsyncClient or <ServiceName>Client.
     *
     * @param token the LITERAL_CLASS AST node
     */
    private void checkServiceClientNaming(DetailAST token) {
        for (DetailAST ast = token; ast != null; ast = ast.getNextSibling()) {
            if (TokenTypes.IDENT == ast.getType()) {
                String className = ast.getText();
                if (!className.endsWith(ASYNC_CLIENT) && !className.endsWith(CLIENT)) {
                    log(ast, String.format("Class name %s should named <ServiceName>AsyncClient or <ServiceName>Client.", className));
                }
                break;
            }
        }
    }
}
