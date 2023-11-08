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
 * This check ensures that an exception thrown includes the current caught exception cause.
 * New exception should use the original/cause exception object to provide the full stack trace for the problem.
 *
 * // DO
 * try {
 *     url = new URL(urlString);
 * } catch (MalformedURLException ex) {
 *     throw new RuntimeException(ex);
 * }
 *
 * // DON'T
 * try {
 *     url = new URL(urlString);
 * } catch (MalformedURLException ex) {
 *     throw new RuntimeException("Invalid URL string was given."); // "ex" is ignored.
 * }
 */
public class UseCaughtExceptionCauseCheck extends AbstractCheck {
    static final String UNUSED_CAUGHT_EXCEPTION_ERROR = "Caught and rethrown exceptions should include the caught"
        + " exception as the cause in the rethrown exception. Dropping the causal exception makes it more difficult"
        + " to troubleshoot issues when they arise. Include the caught exception variable %s as the cause.";

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
        // get the caught exception variable name from the catch block
        final DetailAST catchStatement = catchBlockToken.findFirstToken(TokenTypes.PARAMETER_DEF);
        final String caughtExceptionVariableName = catchStatement.findFirstToken(TokenTypes.IDENT).getText();

        // get all throw statements from current catch block
        final List<DetailAST> throwStatements = getThrowStatements(catchBlockToken);

        // get possible exception names to which the original exception might be assigned to
        final List<String> wrappedExceptions =
            getWrappedExceptions(catchBlockToken, catchBlockToken, caughtExceptionVariableName);

        throwStatements.forEach(throwToken -> {
            final List<String> throwParamNames = new LinkedList<>();
            getThrowParamNames(throwToken, throwParamNames);
            // include the original exception name to the list to look for in throw statements
            wrappedExceptions.add(caughtExceptionVariableName);

            // throwParamNames = [ex, p]
            // exceptionsList = [ex, cause]
            // Caught exception variable is used if an intersection is between the throw statements param names
            // used and the actual exception names being thrown.
            List<String> intersect =
                wrappedExceptions.stream().filter(throwParamNames::contains).collect(Collectors.toList());
            if (intersect.size() == 0) {
                log(throwToken, String.format(UNUSED_CAUGHT_EXCEPTION_ERROR, caughtExceptionVariableName));
            }
        });
    }

    /**
     * Returns the list of exceptions that wrapped the current exception tokens
     *
     * @param currentCatchAST current catch block token
     * @param detailAST catch block throw parent token
     * @param caughtExceptionVariableName list containing the exception tokens
     * @return list of wrapped exception tokens
     */
    private List<String> getWrappedExceptions(DetailAST currentCatchAST, DetailAST detailAST,
                                              String caughtExceptionVariableName) {

        final List<String> wrappedExceptionNames = new LinkedList<>();

        for (DetailAST currentNode : getChildrenNodes(detailAST)) {
            // Recursively traverse through the children of the parent node to collect references where the
            // caught exception variable is used.
            if (currentNode.getType() == TokenTypes.IDENT
                && currentNode.getText().equals(caughtExceptionVariableName)) {
                getWrappedExceptionVariable(currentCatchAST, wrappedExceptionNames, currentNode);
            }

            // add collection in case of last node on this level
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

        // Get the node assigning the caught exception variable, traversing upwards starting from the current node.
        while (!temp.equals(currentCatchBlock) && temp.getType() != TokenTypes.ASSIGN) {
            temp = temp.getParent();
        }

        if (temp.getType() == TokenTypes.ASSIGN) {
            final DetailAST wrappedException;
            // Get the variable definition param name to which the caught exception variable is assigned.
            if (temp.getParent().getType() == TokenTypes.VARIABLE_DEF) {
                wrappedException = temp.getParent().findFirstToken(TokenTypes.IDENT);
            } else if (temp.findFirstToken(TokenTypes.DOT) != null) {
                // Get the variable name if assigned to a 'this' variable
                wrappedException = temp.findFirstToken(TokenTypes.DOT).findFirstToken(TokenTypes.IDENT);
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
        // get all param names by recursively going through all the throw statements retrieving the token type IDENT
        // for the text
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
     * Gets all the children by traversing the tree generated from the current parent node.
     *
     * @param token parent node.
     * @return List of children of the current node.
     */
    private static List<DetailAST> getChildrenNodes(DetailAST token) {
        final List<DetailAST> result = new LinkedList<>();

        DetailAST currNode = token.getFirstChild();

        // Add all the nodes on the current level of the tree and then move to the next level
        while (currNode != null) {
            result.add(currNode);
            currNode = currNode.getNextSibling();
        }

        return result;
    }
}
