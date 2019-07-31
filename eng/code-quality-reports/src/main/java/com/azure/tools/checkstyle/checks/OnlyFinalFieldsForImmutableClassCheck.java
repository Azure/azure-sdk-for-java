// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Verify the classes with annotation @Immutable should have following rules:
 * <ol>
 *   <li>Only final fields allowed</li>
 * </ol>
 */
public class OnlyFinalFieldsForImmutableClassCheck extends AbstractCheck {
    private static final String IMMUTABLE_NOTATION = "Immutable";
    private static final String ERROR_MSG = "The variable field ''%s'' of class ''%s'' should be final." +
        "Classes annotated with @Immutable are supposed to be immutable.";

    private static boolean hasImmutableAnnotation;

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
            TokenTypes.VARIABLE_DEF,
            TokenTypes.OBJBLOCK
        };
    }

    @Override
    public void beginTree(DetailAST root) {
        hasImmutableAnnotation = false;
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                hasImmutableAnnotation = hasImmutableAnnotation(token);
                break;
            case TokenTypes.OBJBLOCK:
                if (hasImmutableAnnotation) {
                    checkForOnlyFinalFields(token);
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Checks if the class is annotated with annotation @Immutable. A class could have multiple annotations.
     *
     * @param classDefToken the CLASS_DEF AST node
     * @return true if the class is annotated with @Immutable, false otherwise.
     */
    private boolean hasImmutableAnnotation(DetailAST classDefToken) {
        // Always has MODIFIERS node
        final DetailAST modifiersToken = classDefToken.findFirstToken(TokenTypes.MODIFIERS);

        for (DetailAST ast = modifiersToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() == TokenTypes.ANNOTATION) {
                // One class could have multiple annotations, return true if found Immutable.
                final DetailAST annotationIdent = ast.findFirstToken(TokenTypes.IDENT);
                return annotationIdent != null && IMMUTABLE_NOTATION.equals(annotationIdent.getText());
            }
        }
        // If no @Immutable annotated with this class, return false
        return false;
    }

    /**
     * Checks all field definitions within the first level of a class are final
     *
     * @param objBlockToken the OBJBLOCK AST node
     */
    private void checkForOnlyFinalFields(DetailAST objBlockToken) {
        for (DetailAST ast = objBlockToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (TokenTypes.VARIABLE_DEF == ast.getType()) {
                final DetailAST modifiersToken = ast.findFirstToken(TokenTypes.MODIFIERS);
                if (!modifiersToken.branchContains(TokenTypes.FINAL)) {
                    log(modifiersToken, String.format(ERROR_MSG, ast.findFirstToken(TokenTypes.IDENT).getText(),
                        objBlockToken.getPreviousSibling().getText()));
                }
            }
        }
    }

}
