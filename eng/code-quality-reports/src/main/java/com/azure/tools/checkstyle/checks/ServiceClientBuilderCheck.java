package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

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
        if (hasServiceClientBuilderAnnotation) {
            if (!hasAsyncClientBuilder) {
                log(root, String.format("ServiceClientBuilder missing an asynchronous method, ''%s''", BUILD_ASYNC_CLIENT));
            }
            if (!hasClientBuilder) {
                log(root, String.format("ServiceClientBuilder missing an synchronous method, ''%s''", BUILD_CLIENT));
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
            TokenTypes.IMPORT,
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
                if (annotationToken != null) {
                    hasServiceClientBuilderAnnotation = true;
                    checkAnnotationMemberValuePair(annotationToken);
                } else {
                    hasServiceClientBuilderAnnotation = false;
                }
                checkForClassNamingAndAnnotation(token);
                break;
            case TokenTypes.METHOD_DEF:
                checkBuilderMethods(token);
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     *  Checks if the class is annotated with @ServiceClientBuilder
     *
     * @param classDefToken the CLASS_DEF AST node
     * @return true if the class is annotated with @ServiceClientBuilder, false otherwise.
     */
    private DetailAST getServiceClientAnnotation(DetailAST classDefToken) {
        final DetailAST modifiersToken = classDefToken.findFirstToken(TokenTypes.MODIFIERS);
        for (DetailAST ast = modifiersToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() != TokenTypes.ANNOTATION) {
                continue;
            }
            if (SERVICE_CLIENT_BUILDER.equals(ast.findFirstToken(TokenTypes.IDENT).getText())) {
                return ast;
            }
        }
        return null;
    }

    private void checkForClassNamingAndAnnotation(DetailAST classDefToken) {
        final String className = classDefToken.findFirstToken(TokenTypes.IDENT).getText();
        if (!className.endsWith("ClientBuilder")) {
            log(classDefToken, "Service client builder class should be named <ServiceName>ClientBuilder.");
        } else {
            if (!hasServiceClientBuilderAnnotation) {
                log(classDefToken, String.format("Class ''%s'' should be annotated with @ServiceClientBuilder.", className));
            }
        }
    }

    /**
     * Checks if the {@code ServiceClientBuilder} annotation has a service client prop named 'serviceClients'.
     *
     * @param annotationToken the ANNOTATION AST node
     */
    private void checkAnnotationMemberValuePair(DetailAST annotationToken) {
        boolean hasServiceClientPropName = false;

        for (DetailAST ast = annotationToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() != TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
                continue;
            }

            DetailAST identAST = ast.findFirstToken(TokenTypes.IDENT);
            if (identAST == null) {
                continue;
            }

            if ("serviceClients".equals(identAST.getText())) {
                hasServiceClientPropName = true;
                break;
            }
        }

        if (!hasServiceClientPropName) {
            log(annotationToken, String.format(
                "Annotation @%s should have ''serviceClients'' as property of annotation and should list all of the service clients it can build.",
                SERVICE_CLIENT_BUILDER));
        }
    }

    /**
     *  Every service client builder should have a sync client builder and async client builder,
     *  buildClient() and buildAsyncClient(), respectively.
     *
     * @param methodDefToken METHOD_DEF AST node
     */
    private void checkBuilderMethods(DetailAST methodDefToken) {
        final String methodName = methodDefToken.findFirstToken(TokenTypes.IDENT).getText();
        if (methodName.equals(BUILD_ASYNC_CLIENT)) {
            hasAsyncClientBuilder = true;
        } else if (methodName.equals(BUILD_CLIENT)) {
            hasClientBuilder = true;
        }
    }
}
