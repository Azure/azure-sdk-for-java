// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import jdk.nashorn.internal.parser.Token;

/**
 * To throw an exception,
 * <ol>
 *   <li>Must do it through a 'logger.logAndThrow', rather than by directly calling 'throw exception'</li>
 *   <li>Always call return after called 'logger.logAndThrow'.</li>
 * </ol>
 *
 * Skip check if throw exception from static method.
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
                while (parentToken != null) {
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
                log(token, "To throw an exception, must do it through a ''logger.logAndThrow'', rather than "
                    + "by directly calling ''throw exception''.");
                break;

            case TokenTypes.METHOD_CALL:
                String methodCall = FullIdent.createFullIdent(token.findFirstToken(TokenTypes.DOT)).getText();
                if (!"logger.logAndThrow".equals(methodCall)) {
                    return;
                }
                DetailAST exprToken = token.getParent();
                if (exprToken.getNextSibling().getType() != TokenTypes.LITERAL_RETURN) {
                    log(token, "Always call ''return'' after called ''logger.logAndThrow''");
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }
}
