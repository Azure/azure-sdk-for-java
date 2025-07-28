// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;

import java.util.Stack;

/**
 * The {@literal @ServiceClientBuilder} class should have the following rules:
 * <ol>
 * <li>All service client builder should be named &lt;ServiceName&gt;ClientBuilder and annotated with
 * {@literal @ServiceClientBuilder}.</li>
 * <li>No other method have prefix 'build' other than 'build*Client' or 'build*AsyncClient'.</li>
 * </ol>
 */
public class ServiceClientBuilderCheck extends AbstractCheck {
    private final Stack<Boolean> hasServiceClientBuilderAnnotationStack = new Stack<>();
    private final Stack<Boolean> hasBuildMethodStack = new Stack<>();
    private boolean hasServiceClientBuilderAnnotation;
    private boolean hasBuildMethod;

    /**
     * Creates a new instance of {@link ServiceClientBuilderCheck}.
     */
    public ServiceClientBuilderCheck() {
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
        return new int[] { TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF };
    }

    @Override
    public void leaveToken(DetailAST token) {
        if (token.getType() == TokenTypes.CLASS_DEF) {
            hasServiceClientBuilderAnnotation = hasServiceClientBuilderAnnotationStack.pop();
            hasBuildMethod = hasBuildMethodStack.pop();
            if (hasServiceClientBuilderAnnotation && !hasBuildMethod) {
                log(token, "Class with @ServiceClientBuilder annotation must have a method starting with ''build'' "
                    + "and ending with ''Client''.");
            }
        }
    }

    @Override
    public void visitToken(DetailAST token) {
        if (token.getType() == TokenTypes.CLASS_DEF) {
            // Save the state of variable 'hasServiceClientBuilderAnnotation' to limit the scope of accessibility
            hasServiceClientBuilderAnnotationStack.push(hasServiceClientBuilderAnnotation);
            hasBuildMethodStack.push(hasBuildMethod);
            final DetailAST serviceClientAnnotationBuilderToken = getServiceClientBuilderAnnotation(token);
            final String className = token.findFirstToken(TokenTypes.IDENT).getText();

            hasServiceClientBuilderAnnotation = serviceClientAnnotationBuilderToken != null;
            if (hasServiceClientBuilderAnnotation) {
                // Don't need to check if the 'serviceClients' exist. It is required when using
                // @ServiceClientBuilder

                // HAS @ServiceClientBuilder annotation but NOT named the class <ServiceName>ClientBuilder
                if (!className.endsWith("ClientBuilder")) {
                    log(token, "Class annotated with @ServiceClientBuilder ''" + className + "'' should be named "
                        + "<ServiceName>ClientBuilder.");
                }
            } else {
                // No @ServiceClientBuilder annotation but HAS named the class <ServiceName>ClientBuilder
                if (className.endsWith("ClientBuilder")) {
                    log(token, "Class ''" + className + "'' should be annotated with @ServiceClientBuilder.");
                }
            }
        } else if (token.getType() == TokenTypes.METHOD_DEF && hasServiceClientBuilderAnnotation) {
            final String methodName = token.findFirstToken(TokenTypes.IDENT).getText();
            if (!methodName.startsWith("build")) {
                return;
            }

            hasBuildMethod = true;
            // method name has prefix 'build' but not 'build*Client' or 'build*AsyncClient'
            if (!methodName.endsWith("Client")) {
                log(token, "@ServiceClientBuilder class should not have a method name, ''" + methodName + "'' starting "
                    + "with ''build'' but not ending with ''Client''.");
            }
        }
    }

    /**
     * Checks if the class is annotated with @ServiceClientBuilder.
     *
     * @param classDefToken the CLASS_DEF AST node
     * @return the annotation node if the class is annotated with @ServiceClientBuilder, null otherwise.
     */
    private static DetailAST getServiceClientBuilderAnnotation(DetailAST classDefToken) {
        return AnnotationUtil.getAnnotation(classDefToken, "ServiceClientBuilder");
    }
}
