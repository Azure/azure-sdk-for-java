// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * To throws an exception, must do it through a 'logger.logAndThrow',
 * rather than by directly calling 'throw exception'.
 *
 * Skip checks if
 * <o1>
 *     <li>throw exception from static method</li>
 * </o1>
 */
public class ThrownClientLoggerCheck extends AbstractCheck {

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
            TokenTypes.LITERAL_THROW
        };
    }

    @Override
    public void visitToken(DetailAST token) {

        switch (token.getType()) {
            case TokenTypes.LITERAL_THROW:
                DetailAST parentToken = token.getParent();
                while (parentToken!= null) {
                    // Skip throw exception Throw from static method
                    if (parentToken.getType() == TokenTypes.METHOD_DEF) {
                        DetailAST modifiersToken = parentToken.findFirstToken(TokenTypes.MODIFIERS);
                        if (modifiersToken.branchContains(TokenTypes.LITERAL_STATIC)) {
                            return;
                        }
                        break;
                    }
                    parentToken = parentToken.getParent();
                }

                // non static method and class
                log(token, "To throws an exception, must do it through a ''logger.logAndThrow'', rather than "
                    + "by directly calling ''throw exception''.");
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }
}
