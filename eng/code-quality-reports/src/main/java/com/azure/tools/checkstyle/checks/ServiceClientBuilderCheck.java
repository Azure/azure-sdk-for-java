// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * The @ServiceClientBuilder class should have following rules:
 *     1) All service client builder should be named <ServiceName>ClientBuilder and annotated with @ServiceClientBuilder.
 *     2) A property named 'serviceClients'.
*      3) Has a method 'buildClient()' to build a synchronous client,
 *     4) Has a method 'buildAsyncClient()' to build a asynchronous client
 */
public class ServiceClientBuilderCheck extends AbstractCheck {
    private static final String SERVICE_CLIENT_BUILDER = "ServiceClientBuilder";
    private static final String BUILD_CLIENT = "buildClient";
    private static final String BUILD_ASYNC_CLIENT = "buildAsyncClient";

    private static boolean hasServiceClientBuilderAnnotation;
    private static boolean hasAsyncClientBuilder;
    private static boolean hasClientBuilder;

    @Override
    public void beginTree(DetailAST root) {
        hasServiceClientBuilderAnnotation = true;
        hasAsyncClientBuilder = false;
        hasClientBuilder = false;
    }

    @Override
    public void finishTree(DetailAST root) {
        // Checks if the @ServiceClientBuilder class has an asynchronous and synchronous method.
        if (hasServiceClientBuilderAnnotation) {
            if (!hasAsyncClientBuilder) {
                log(root, String.format("ServiceClientBuilder missing an asynchronous method, ''%s''", BUILD_ASYNC_CLIENT));
            }
            if (!hasClientBuilder) {
                log(root, String.format("ServiceClientBuilder missing a synchronous method, ''%s''", BUILD_CLIENT));
            }
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
            TokenTypes.CLASS_DEF,
            TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void visitToken(DetailAST token) {
        if (!hasServiceClientBuilderAnnotation) {
            return;
        }

        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                final DetailAST annotationToken = getServiceClientAnnotation(token);
                final String className = token.findFirstToken(TokenTypes.IDENT).getText();

                hasServiceClientBuilderAnnotation = annotationToken != null;

                if (hasServiceClientBuilderAnnotation) {
                    // Checks if the ANNOTATION has property named 'serviceClients'
                    if (!hasServiceClientsAnnotationProperty(annotationToken)) {
                        log(annotationToken, String.format(
                            "Annotation @%s should have ''serviceClients'' as property of annotation and should list all of the service clients it can build.",
                            SERVICE_CLIENT_BUILDER));
                    }
                    // HAS @ServiceClientBuilder annotation but NOT named the class <ServiceName>ClientBuilder
                    if (!className.endsWith("ClientBuilder")) {
                        log(token, "Service client builder class should be named <ServiceName>ClientBuilder.");
                    }
                } else {
                    // No @ServiceClientBuilder annotation but HAS named the class <ServiceName>ClientBuilder
                    if (className.endsWith("ClientBuilder")) {
                        log(token, String.format("Class ''%s'' should be annotated with @ServiceClientBuilder.", className));
                    }
                }
                break;
            case TokenTypes.METHOD_DEF:
                final String methodName = token.findFirstToken(TokenTypes.IDENT).getText();

                if (BUILD_ASYNC_CLIENT.equals(methodName)) {
                    hasAsyncClientBuilder = true;
                }

                if (BUILD_CLIENT.equals(methodName)) {
                    hasClientBuilder = true;
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
    private DetailAST getServiceClientAnnotation(DetailAST classDefToken) {
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

    /**
     * Checks if the {@code ServiceClientBuilder} annotation has a service client prop named 'serviceClients'.
     *
     * @param annotationToken the ANNOTATION AST node
     * @return true if the @ServiceClientBuilder has property named 'serviceClients', false if none
     */
    private boolean hasServiceClientsAnnotationProperty(DetailAST annotationToken) {
        for (DetailAST ast = annotationToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() != TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
                continue;
            }
            // if there is ANNOTATION_MEMBER_VALUE_PAIR exist, it always has IDENT node
            if ("serviceClients".equals(ast.findFirstToken(TokenTypes.IDENT).getText())) {
                return true;
            }
        }
        return false;
    }
}
