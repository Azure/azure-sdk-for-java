// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.Optional;

/**
 * Verify the classes with annotation {@code @Immutable} should have following rules:
 * <ol>
 *   <li>Only final fields allowed</li>
 * </ol>
 */
public class OnlyFinalFieldsForImmutableClassCheck extends AbstractCheck {
    private static final String IMMUTABLE_NOTATION = "Immutable";
    private static final String ERROR_MSG = "The variable field ''%s'' should be final." +
        "Classes annotated with @Immutable are supposed to be immutable.";

    private boolean hasImmutableAnnotation;

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

    /*
     * Checks if the class is annotated with annotation {@literal @Immutable}. A class could have multiple annotations.
     *
     * @param classDefToken the CLASS_DEF AST node
     * @return true if the class is annotated with {@literal @Immutable}, false otherwise.
     */
    private boolean hasImmutableAnnotation(DetailAST classDefToken) {
        DetailAST immutableAnnotation = AnnotationUtil.getAnnotation(classDefToken, IMMUTABLE_NOTATION);
        return immutableAnnotation != null;
    }

    /*
     * Checks all field definitions within the first level of a class are final
     *
     * @param objBlockToken the OBJBLOCK AST node
     */
    private void checkForOnlyFinalFields(DetailAST objBlockToken) {
        Optional<DetailAST> nonFinalFieldFound = TokenUtil.findFirstTokenByPredicate(objBlockToken,
            node -> TokenTypes.VARIABLE_DEF == node.getType() && !node.branchContains(TokenTypes.FINAL)
                && !Utils.hasIllegalCombination(node.findFirstToken(TokenTypes.MODIFIERS)));

        if (nonFinalFieldFound.isPresent()) {
            DetailAST field = nonFinalFieldFound.get().findFirstToken(TokenTypes.IDENT);
            log(field, String.format(ERROR_MSG, field.getText()));
        }
    }

}
