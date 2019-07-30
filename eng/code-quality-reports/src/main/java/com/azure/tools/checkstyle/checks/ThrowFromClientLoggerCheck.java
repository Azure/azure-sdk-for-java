// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.Deque;

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

    private Deque<Boolean> classStaticDeque = new ArrayDeque<>();
    private Deque<Boolean> methodStaticDeque = new ArrayDeque<>();

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
            TokenTypes.LITERAL_THROW,
            TokenTypes.METHOD_DEF,
            TokenTypes.METHOD_CALL
        };
    }

    @Override
    public void leaveToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                classStaticDeque.pop();
                break;
            case TokenTypes.METHOD_DEF:
                methodStaticDeque.pop();
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                DetailAST modifiersToken = token.findFirstToken(TokenTypes.MODIFIERS);
                classStaticDeque.addLast(modifiersToken.branchContains(TokenTypes.LITERAL_STATIC));
                break;
            case TokenTypes.METHOD_DEF:
                DetailAST methodModifiersToken = token.findFirstToken(TokenTypes.MODIFIERS);
                methodStaticDeque.addLast(methodModifiersToken.branchContains(TokenTypes.LITERAL_STATIC));
                break;
            case TokenTypes.LITERAL_THROW:
                if (classStaticDeque.isEmpty() || methodStaticDeque.isEmpty()
                    || classStaticDeque.peekLast() || methodStaticDeque.peekLast()) {
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

                if (classStaticDeque.isEmpty() || methodStaticDeque.isEmpty() ||
                    classStaticDeque.peekLast() || methodStaticDeque.peekLast()) {
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
}
