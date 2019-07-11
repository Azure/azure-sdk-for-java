// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * To throws an exception, must do it through a 'clientLogger.logAndThrow',
 * rather than by directly calling 'throw exception'
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
            TokenTypes.LITERAL_THROWS
        };
    }

    @Override
    public void visitToken(DetailAST token) {

        switch (token.getType()) {
            case TokenTypes.LITERAL_THROWS:
                log(token, "To throws an exception, must do it through a ''clientLogger.logAndThrow'', rather than "
                    + "by directly calling ''throw exception''.");
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }
}
