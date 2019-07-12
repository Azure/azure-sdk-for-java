// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Stack;

/**
 * The @ServiceClientBuilder class should have the following rules:
 *  1) All service client builder should be named <ServiceName>ClientBuilder and annotated with @ServiceClientBuilder.
 *  2) No other method have prefix 'build' other than 'buildClient' or 'buildAsyncClient'.
 */
public class ServiceClientBuilderCheck extends AbstractCheck {
    private static final String SERVICE_CLIENT_BUILDER = "ServiceClientBuilder";
    private static final String BUILD_CLIENT = "buildClient";
    private static final String BUILD_ASYNC_CLIENT = "buildAsyncClient";

    private Stack<Boolean> hasServiceClientBuilderAnnotationStack = new Stack();
    private boolean hasServiceClientBuilderAnnotation;

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
            TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void leaveToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                // end of CLASS_DEF node, reset the value back to previous state
                hasServiceClientBuilderAnnotation = hasServiceClientBuilderAnnotationStack.pop();
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                // Save the state of variable 'hasServiceClientBuilderAnnotation' to limit the scope of accessibility
                hasServiceClientBuilderAnnotationStack.push(hasServiceClientBuilderAnnotation);

                final DetailAST serviceClientAnnotationBuilderToken = getServiceClientBuilderAnnotation(token);
                final String className = token.findFirstToken(TokenTypes.IDENT).getText();

                hasServiceClientBuilderAnnotation = serviceClientAnnotationBuilderToken != null;
                if (hasServiceClientBuilderAnnotation) {
                    // Don't need to check if the 'serviceClients' exist. It is required when using @ServiceClientBuilder

                    // HAS @ServiceClientBuilder annotation but NOT named the class <ServiceName>ClientBuilder
                    if (!className.endsWith("ClientBuilder")) {
                        log(token, String.format("Class annotated with @ServiceClientBuilder ''%s'' should be named <ServiceName>ClientBuilder.", className));
                    }
                } else {
                    // No @ServiceClientBuilder annotation but HAS named the class <ServiceName>ClientBuilder
                    if (className.endsWith("ClientBuilder")) {
                        log(token, String.format("Class ''%s'' should be annotated with @ServiceClientBuilder.", className));
                    }
                }
                break;
            case TokenTypes.METHOD_DEF:
                if (!hasServiceClientBuilderAnnotation) {
                    return;
                }

                final String methodName = token.findFirstToken(TokenTypes.IDENT).getText();
                // method name has prefix 'build' but not 'buildClient' or 'buildAsyncClient'
                if (methodName.startsWith("build") && !BUILD_ASYNC_CLIENT.equals(methodName) && !BUILD_CLIENT.equals(methodName)) {
                    log(token, String.format(
                        "@ServiceClientBuilder class should not have a method name, '''' starting with ''build'' " +
                            "other than ''buildClient'' or ''buildAsyncClient''." , methodName));
                }

                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Checks if the class is annotated with @ServiceClientBuilder.
     *
     * @param classDefToken the CLASS_DEF AST node
     * @return the annotation node if the class is annotated with @ServiceClientBuilder, null otherwise.
     */
    private DetailAST getServiceClientBuilderAnnotation(DetailAST classDefToken) {
        final DetailAST modifiersToken = classDefToken.findFirstToken(TokenTypes.MODIFIERS);

        if (!modifiersToken.branchContains(TokenTypes.ANNOTATION)) {
            return null;
        }

        DetailAST annotationToken = modifiersToken.findFirstToken(TokenTypes.ANNOTATION);
        if (!SERVICE_CLIENT_BUILDER.equals(annotationToken.findFirstToken(TokenTypes.IDENT).getText())) {
            return null;
        }

        return annotationToken;
    }
}
