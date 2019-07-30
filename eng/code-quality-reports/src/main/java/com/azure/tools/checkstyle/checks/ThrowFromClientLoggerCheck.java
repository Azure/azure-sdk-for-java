// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * To throw an exception,
 * <ol>
 *   <li>Must do it through a 'logger.logAndThrow', rather than by directly calling 'throw exception'</li>
 *   <li>Always call return after calling 'logger.logAndThrow'.</li>
 * </ol>
 *
 * Skip check if throwing exception from
 * <ol>
 *   <li>Static method</li>
 *   <li>Static class</li>
 *   <li>Constructor</li>
 * </ol>
 */
public class ThrowFromClientLoggerCheck extends AbstractCheck {
    private static final String LOGGER_LOG_AND_THROW = "logger.logAndThrow";

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
            TokenTypes.LITERAL_THROW,
            TokenTypes.METHOD_CALL
        };
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.LITERAL_THROW:
                if (!isValidScope(token)) {
                    return;
                }
                log(token, String.format("Directly throwing an exception is disallowed. Replace the throw statement with" +
                    " a call to ''%s''.", LOGGER_LOG_AND_THROW));
                break;
            case TokenTypes.METHOD_CALL:
                String methodCall = FullIdent.createFullIdent(token.findFirstToken(TokenTypes.DOT)).getText();
                if (!LOGGER_LOG_AND_THROW.equals(methodCall)) {
                    return;
                }

                if (!isValidScope(token)) {
                    return;
                }

                DetailAST exprToken = token.getParent();
                if (exprToken.getNextSibling() == null || exprToken.getNextSibling().getNextSibling() == null
                    || exprToken.getNextSibling().getNextSibling().getType() != TokenTypes.LITERAL_RETURN) {
                    log(token, String.format("Always call ''return'' after calling ''%s''.", LOGGER_LOG_AND_THROW));
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Given a token as a root node, traversal back to find the if the token is belong to a static method.
     * If not, check if it is in the constructor or a static class. Use this method to define the scope of the token.
     *
     * @param token the given token as a root to traversal back to one of the ancestors.
     * @return false if the token is in a scope of static method, constructor or static class. otherwise, return true.
     */
    private boolean isValidScope(DetailAST token) {
        DetailAST parentToken = token.getParent();
        boolean throwsFromMethod = false;
        while (parentToken != null) {
            int parentType = parentToken.getType();
            // Skip check if the token is from static method
            if (parentType == TokenTypes.METHOD_DEF) {
                DetailAST modifiersToken = parentToken.findFirstToken(TokenTypes.MODIFIERS);
                if (modifiersToken.branchContains(TokenTypes.LITERAL_STATIC)) {
                    return false;
                }
                throwsFromMethod = true;
            }
            // Skip check if the token is from static class
            if (parentType == TokenTypes.CLASS_DEF) {
                DetailAST modifiersToken = parentToken.findFirstToken(TokenTypes.MODIFIERS);
                if (modifiersToken.branchContains(TokenTypes.LITERAL_STATIC)) {
                    return false;
                }
                break;
            }
            // Skip check if the token from constructor
            if (!throwsFromMethod && parentType == TokenTypes.CTOR_DEF) {
                return false;
            }
            parentToken = parentToken.getParent();
        }

        return true;
    }
}
