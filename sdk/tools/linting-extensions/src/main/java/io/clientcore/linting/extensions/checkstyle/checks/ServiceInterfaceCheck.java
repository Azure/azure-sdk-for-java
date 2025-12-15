// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks that the {@code @ServiceInterface} annotation is used correctly on interfaces.
 * <ul>
 *     <li>The {@code name} property should be non-empty.</li>
 * </ul>
 */
public class ServiceInterfaceCheck extends AbstractCheck {

    /**
     * Creates a new instance of {@link ServiceInterfaceCheck}.
     */
    public ServiceInterfaceCheck() {
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
        return new int[] { TokenTypes.INTERFACE_DEF };
    }

    @Override
    public void visitToken(DetailAST token) {
        if (token.getType() == TokenTypes.INTERFACE_DEF) {
            checkServiceInterface(token);
        }
    }

    /**
     *  The @ServiceInterface annotation should have a non-empty 'name' property.
     *
     * @param interfaceDefToken INTERFACE_DEF AST node
     */
    private void checkServiceInterface(DetailAST interfaceDefToken) {
        DetailAST serviceInterfaceAnnotationNode = null;
        String nameValue = null;

        DetailAST modifiersToken = interfaceDefToken.findFirstToken(TokenTypes.MODIFIERS);
        // Find the @ServiceInterface and the property 'name' and the corresponding value
        for (DetailAST ast = modifiersToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            // We care about only the ANNOTATION type
            if (ast.getType() != TokenTypes.ANNOTATION) {
                continue;
            }
            // Skip if not @ServiceInterface annotation
            if (!"ServiceInterface".equals(ast.findFirstToken(TokenTypes.IDENT).getText())) {
                continue;
            }

            // Get the @ServiceInterface annotation node
            serviceInterfaceAnnotationNode = ast;

            // Get the 'name' property value of @ServiceInterface
            // @ServiceInterface requires 'name' property
            DetailAST annotationMemberValuePairToken = ast.findFirstToken(TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR);
            if ("name".equals(annotationMemberValuePairToken.findFirstToken(TokenTypes.IDENT).getText())) {
                nameValue = getNamePropertyValue(annotationMemberValuePairToken.findFirstToken(TokenTypes.EXPR));
                break;
            }
        }

        // Checks the rules:
        // Skip the check if no @ServiceInterface annotation found
        if (serviceInterfaceAnnotationNode == null) {
            return;
        }

        // 'name' is required at @ServiceInterface
        // 'name' should not be null or empty
        if (nameValue == null || nameValue.isEmpty()) {
            log(serviceInterfaceAnnotationNode, String.format(
                "The ''name'' property of @ServiceInterface, ''%s'' should be not be null or empty", nameValue));
        }
    }

    /**
     * Get the name property value from the EXPR node
     *
     * @param exprToken EXPR
     * @return null if EXPR node doesn't exist or no STRING_LITERAL. Otherwise, returns the value of the property.
     */
    private String getNamePropertyValue(DetailAST exprToken) {
        if (exprToken == null) {
            return null;
        }

        final DetailAST nameValueToken = exprToken.findFirstToken(TokenTypes.STRING_LITERAL);
        if (nameValueToken == null) {
            return null;
        }

        String nameValue = nameValueToken.getText();

        // remove the beginning and ending double quote
        return nameValue.replaceAll("^\"|\"$", "");
    }
}
