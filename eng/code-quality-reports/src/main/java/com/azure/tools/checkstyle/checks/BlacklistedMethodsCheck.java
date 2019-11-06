// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Blacklist methods that should not be used in the Azure SDK. Such as it could avoid inherently unsafe method.
 */
public class BlacklistedMethodsCheck extends AbstractCheck {

    /**
     * Specified full name of invalid methods.
     */
    private final Set<String> methods = new HashSet<>();

    /**
     * Specified message for the invalid methods
     */
    private String message;
    /**
     * Setter to specifies invalid methods full name.
     * @param methods the specified full name of invalid methods.
     */
    public void setMethods(String... methods) {
        if (methods != null) {
            Collections.addAll(this.methods, methods);
        }
    }

    /**
     * Setter to specifies invalid methods's message.
     * @param message the specified message for the invalid methods
     */
    public void setMessage(String message) {
        this.message = message;
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
        return new int[] {
            TokenTypes.METHOD_CALL
        };
    }

    @Override
    public void visitToken(DetailAST token) {
        if (token.getType() == TokenTypes.METHOD_CALL) {
            blacklistedMethodsCheck(token);
        }
    }

    private void blacklistedMethodsCheck(DetailAST methodCallToken) {
        final String methodCallName = FullIdent.createFullIdentBelow(methodCallToken).getText();
        // FullIdent.getText() will never return null but an empty string
        if (methodCallName.isEmpty()) {
            return;
        }

        if (methods.contains(methodCallName)) {
            log(methodCallToken, String.format(message, methodCallName));
        }
    }
}
