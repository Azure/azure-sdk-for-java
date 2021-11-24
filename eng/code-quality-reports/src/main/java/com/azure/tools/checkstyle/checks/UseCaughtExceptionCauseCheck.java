// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ensures caught exceptions are included as exception cause in subsequently thrown exception.
 */
public class UseCaughtExceptionCauseCheck extends AbstractCheck {
    static final String UNUSED_CAUGHT_EXCEPTION_ERROR = "Should use the current exception cause \"%s\".";

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
        return new int[] {TokenTypes.LITERAL_CATCH};
    }

    @Override
    public void visitToken(DetailAST catchBlockToken) {
        final DetailAST catchStatement = catchBlockToken.findFirstToken(TokenTypes.PARAMETER_DEF);
        final String caughtExceptionVariableName = catchStatement.findFirstToken(TokenTypes.IDENT).getText();

        final List<DetailAST> throwStatements = getThrowStatements(catchBlockToken);
        final List<String> wrappedExceptions =
            getWrappedExceptions(catchBlockToken, catchBlockToken, caughtExceptionVariableName);

        throwStatements.forEach(throwToken -> {
            final List<String> throwParamNames = new LinkedList<>();
            getThrowParamNames(throwToken, throwParamNames);
            // all possible exception names to look for in throw statements
            wrappedExceptions.add(caughtExceptionVariableName);

            // throwsList = [ex, p]
            // exceptionsList = [ex, cause]
            List<String> intersect =
                wrappedExceptions.stream().filter(throwParamNames::contains).collect(Collectors.toList());
            if (intersect.size() == 0) {
                log(throwToken, String.format(UNUSED_CAUGHT_EXCEPTION_ERROR, caughtExceptionVariableName));
            }
        });
    }

    /**
     * Returns the wrapped exception tokens
     *
     * @param detailAST catch block throw parent token
     * @param caughtExceptionVariableName list containing the exception tokens
     * @return list of wrapped exception tokens
     */
    private List<String> getWrappedExceptions(DetailAST currentCatchAST, DetailAST detailAST,
                                              String caughtExceptionVariableName) {

        final List<String> wrappedExceptionNames = new LinkedList<>();

        for (DetailAST currentNode : getChildrenNodes(detailAST)) {
            if (currentNode.getType() == TokenTypes.IDENT &&
                currentNode.getText().equals(caughtExceptionVariableName)) {
                getWrappedExceptionVariable(currentCatchAST, wrappedExceptionNames, currentNode);
            }

            if (currentNode.getFirstChild() != null) {
                wrappedExceptionNames.addAll(
                    getWrappedExceptions(currentCatchAST, currentNode, caughtExceptionVariableName));
            }
        }
        return wrappedExceptionNames;
    }

    /**
     * Returns the wrapped exception variable name
     */
    private void getWrappedExceptionVariable(DetailAST currentCatchBlock, List<String> wrappedExceptionNames,
                                             DetailAST currentToken) {
        DetailAST temp = currentToken;

        while (!temp.equals(currentCatchBlock) && temp.getType() != TokenTypes.ASSIGN) {
            temp = temp.getParent();
        }

        if (temp.getType() == TokenTypes.ASSIGN) {
            final DetailAST wrappedException;
            if (temp.getParent().getType() == TokenTypes.VARIABLE_DEF) {
                wrappedException = temp.getParent().findFirstToken(TokenTypes.IDENT);
            } else {
                wrappedException = temp.findFirstToken(TokenTypes.IDENT);
            }
            if (wrappedException != null) {
                wrappedExceptionNames.add(wrappedException.getText());
            }
        }
    }

    /**
     * Returns the parameter names for current throw keyword.
     *
     * @param throwParent The parent throw token
     * @param paramNames The list containing the parameter names
     * @return list of throw param names
     */
    private List<String> getThrowParamNames(DetailAST throwParent, List<String> paramNames) {
        getChildrenNodes(throwParent).forEach(currentNode -> {
            if (currentNode.getType() == TokenTypes.IDENT) {
                paramNames.add(currentNode.getText());
            }
            if (currentNode.getFirstChild() != null) {
                getThrowParamNames(currentNode, paramNames);
            }
        });
        return paramNames;
    }

    /**
     * Recursive method that searches for all the LITERAL_THROW on the current catch token.
     *
     * @param catchBlockToken A start token.
     * @return list of throw tokens
     */
    private List<DetailAST> getThrowStatements(DetailAST catchBlockToken) {
        final List<DetailAST> throwStatements = new LinkedList<>();
        getChildrenNodes(catchBlockToken).forEach(currentNode -> {
            if (TokenTypes.LITERAL_THROW == currentNode.getType()) {
                throwStatements.add(currentNode);
            }
            if (currentNode.getFirstChild() != null) {
                throwStatements.addAll(getThrowStatements(currentNode));
            }
        });
        return throwStatements;
    }

    /**
     * Gets all the children of the current parent node.
     *
     * @param token parent node.
     * @return List of children of the current node.
     */
    private static List<DetailAST> getChildrenNodes(DetailAST token) {
        final List<DetailAST> result = new LinkedList<>();

        DetailAST currNode = token.getFirstChild();

        while (currNode != null) {
            result.add(currNode);
            currNode = currNode.getNextSibling();
        }

        return result;
    }
}
