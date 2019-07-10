// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * The @ServiceInterface class should have following rules:
 *   1) has annotation property 'name' and should be non-empty
 *   2) length of value of property 'name' should be less than 10 characters and without space
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
     * The @ServiceInterface class should have following rules:
     *   1) has annotation property 'name' and should be non-empty
     *   2) length of value of property 'name' should be less than 10 characters and without space
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

        // Missing 'name' property
        if(nameValue == null) {
            log(serviceInterfaceAnnotationNode, "@ServiceInterface annotation missing ''name'' property.");
        } else {
            // No Space allowed
            if (nameValue.contains(" ")) {
                log(serviceInterfaceAnnotationNode, "The ''name'' property of @ServiceInterface should not contain white space.");
            }
            // Length should less than or equal to 10 characters
            if (nameValue.length() > 10) {
                log(serviceInterfaceAnnotationNode, "The ''name'' property of @ServiceInterface should not have a length > 10.");
            }
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

        return nameValueToken.getText();
    }
}
