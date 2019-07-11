// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * The @ServiceInterface class should have the following rules:
 *   1) The annotation property 'name' should be non-empty
 *   2) The length of value of property 'name' should be less than 10 characters and without space
 */
public class ServiceInterfaceCheck extends AbstractCheck {
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
            TokenTypes.INTERFACE_DEF
        };
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.INTERFACE_DEF:
                checkServiceInterface(token);
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     *  The @ServiceInterface class should have the following rules:
     *   1) The annotation property 'name' should be non-empty
     *   2) The length of value of property 'name' should be less than 10 characters and without space
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
            // ANNOTATION
            for (DetailAST annotationChild = ast.getFirstChild(); annotationChild != null;
                 annotationChild = annotationChild.getNextSibling()) {

                // IDENT
                if (annotationChild.getType() == TokenTypes.IDENT) {
                    // Skip this annotation if it is not @ServiceInterface,
                    if (!"ServiceInterface".equals(annotationChild.getText())) {
                        break;
                    } else {
                        serviceInterfaceAnnotationNode = ast;
                    }
                }

                // ANNOTATION_MEMBER_VALUE_PAIR
                if (annotationChild.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
                    if ("name".equals(annotationChild.findFirstToken(TokenTypes.IDENT).getText())) {
                        nameValue = getNamePropertyValue(annotationChild.findFirstToken(TokenTypes.EXPR));
                    }
                }
            }
        }

        // Checks the rules:
        // Skip the check if no @ServiceInterface annotation found
        if (serviceInterfaceAnnotationNode == null) {
            return;
        }

        // 'name' is required at @ServiceInterface
        // 'name' should not be empty
        if (nameValue.isEmpty()) {
            log(serviceInterfaceAnnotationNode, String.format("The ''name'' property of @ServiceInterface, ''%s'' should not be empty.", nameValue));
        }

        // No Space allowed
        if (nameValue.contains(" ")) {
            log(serviceInterfaceAnnotationNode, String.format("The ''name'' property of @ServiceInterface, ''%s'' should not contain white space.", nameValue));
        }
        // Length should less than or equal to 10 characters
        if (nameValue.length() > 10) {
            log(serviceInterfaceAnnotationNode, "[DEBUG] length = " + nameValue.length() + ",  name = " + nameValue);
            log(serviceInterfaceAnnotationNode, String.format("The ''name'' property of @ServiceInterface ''%s'' should not have a length > 10.", nameValue));
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
