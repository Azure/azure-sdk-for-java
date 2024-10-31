// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * The @ServiceInterface class should have the following rules:
 *   1) The annotation property 'name' should be non-empty
 *   2) The length of value of property 'name' should be less than 20 characters and without space
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
        if (token.getType() == TokenTypes.INTERFACE_DEF) {
            checkServiceInterface(token);
        }
    }

    /**
     *  The @ServiceInterface class should have the following rules:
     *   1) The annotation property 'name' should be non-empty
     *   2) The length of value of property 'name' should be less than 20 characters and without space
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
        // 'name' should not be empty, no Space allowed and the length should less than or equal to 20 characters
        if (invalidServiceName(nameValue)) {
            log(serviceInterfaceAnnotationNode, String.format(
                "The ''name'' property of @ServiceInterface, ''%s'' should be non-empty, alphanumeric and not more "
                    + "than 20 characters", nameValue));
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

        boolean startsWith = nameValue.startsWith("\"");
        boolean endsWith = nameValue.endsWith("\"");

        if (startsWith && endsWith) {
            return nameValue.substring(1, nameValue.length() - 1);
        } else if (startsWith) {
            return nameValue.substring(1);
        } else if (endsWith) {
            return nameValue.substring(0, nameValue.length() - 1);
        } else {
            return nameValue;
        }
    }

    private static boolean invalidServiceName(String nameValue) {
        if (nameValue == null || nameValue.isEmpty() || nameValue.length() > 20) {
            // null, empty name, or a name larger than 20 characters is invalid.
            return true;
        }

        int length = nameValue.length();
        for (int i = 0; i < length; i++) {
            char c = nameValue.charAt(i);
            if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9')) {
                // name can only contain the characters a-z, A-Z, and 0-9.
                return true;
            }
        }

        // name is valid.
        return false;
    }
}
