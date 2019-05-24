package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.DetailNodeTreeStringPrinter;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.BlockCommentPosition;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class JavadocThrowsChecks extends AbstractCheck {
    private static final String MISSING_DESCRIPTION_MESSAGE = "@throws tag requires a description explaining when the error is thrown.";
    private static final String MISSING_THROWS_TAG_MESSAGE = "Javadoc @throws tag required for unchecked throw.";
    private static final int[] TOKENS = new int[] {
        TokenTypes.BLOCK_COMMENT_BEGIN,
        TokenTypes.METHOD_DEF,
        TokenTypes.LITERAL_THROWS,
        TokenTypes.LITERAL_THROW,
        TokenTypes.LITERAL_CATCH,
    };

    private Map<String, HashSet<String>> methodJavadocThrowsMapping;
    private String currentMethodIdentifier;
    private boolean currentMethodNeedsChecking;

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
        return TOKENS;
    }

    @Override
    public boolean isCommentNodesRequired() {
        return true;
    }

    @Override
    public void beginTree(DetailAST rootToken) {
        methodJavadocThrowsMapping = new HashMap<>();
        currentMethodNeedsChecking = false;
        currentMethodIdentifier = "";
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.BLOCK_COMMENT_BEGIN:
                findJavadocThrows(token);
                break;

            case TokenTypes.METHOD_DEF:
                setMethodIdentifierAndCheckStatus(token);
                break;

            case TokenTypes.LITERAL_THROWS:
                if (currentMethodNeedsChecking) {
                    verifyCheckedThrowJavadoc(token);
                }
                break;

            case TokenTypes.LITERAL_THROW:
                if (currentMethodNeedsChecking) {
                    verifyThrowJavadoc(token);
                }
                break;
        }
    }

    /*
     * Gets the current method identifier and determines if it needs to be checked.
     * @param methodDefToken Method definition token.
     */
    private void setMethodIdentifierAndCheckStatus(DetailAST methodDefToken) {
        currentMethodIdentifier = methodDefToken.findFirstToken(TokenTypes.IDENT).getText() + methodDefToken.getLineNo();
        currentMethodNeedsChecking = visibilityIsPublicOrProtectedAndNotAbstract(methodDefToken.findFirstToken(TokenTypes.MODIFIERS));
    }

    /*
     * Determines if the modifiers contains either public or protected and isn't abstract.
     * @param modifiersToken Modifiers token.
     * @return True if the method if public or protected and isn't abstract.
     */
    private boolean visibilityIsPublicOrProtectedAndNotAbstract(DetailAST modifiersToken) {
        if (modifiersToken == null) {
            return false;
        }

        for (DetailAST modifier = modifiersToken.getFirstChild(); modifier != null; modifier = modifier.getNextSibling()) {
            if (modifier.getType() == TokenTypes.ABSTRACT) {
                return false;
            }

            if (modifier.getType() == TokenTypes.LITERAL_PUBLIC || modifier.getType() == TokenTypes.LITERAL_PROTECTED) {
                return true;
            }
        }

        return false;
    }

    /*
     * Checks if the comment is on a method, if so it searches for the documented Javadoc @throws statements.
     * @param blockCommentToken Block comment token.
     */
    private void findJavadocThrows(DetailAST blockCommentToken) {
        if (!BlockCommentPosition.isOnMethod(blockCommentToken)) {
            return;
        }

        // Turn the DetailAST into a Javadoc DetailNode.
        DetailNode javadocNode = DetailNodeTreeStringPrinter.parseJavadocAsDetailNode(blockCommentToken);
        if (javadocNode == null) {
            return;
        }

        // Append the line number to differentiate overloads.
        HashSet<String> javadocThrows = methodJavadocThrowsMapping.getOrDefault(currentMethodIdentifier, new HashSet<>());

        // Iterate through all the top level nodes in the Javadoc, looking for the @throws statements.
        for (DetailNode node : javadocNode.getChildren()) {
            if (node.getType() != JavadocTokenTypes.JAVADOC_TAG || JavadocUtil.findFirstToken(node, JavadocTokenTypes.THROWS_LITERAL) == null) {
                continue;
            }

            // Add the class being thrown to the set of documented throws.
            javadocThrows.add(JavadocUtil.findFirstToken(node, JavadocTokenTypes.CLASS_NAME).getText());

            if (JavadocUtil.findFirstToken(node, JavadocTokenTypes.DESCRIPTION) == null) {
                log(node.getLineNumber(), MISSING_DESCRIPTION_MESSAGE);
            }
        }

        methodJavadocThrowsMapping.put(currentMethodIdentifier, javadocThrows);
    }

    /*
     * Verifies that the checked exceptions, those in the throws statement, are documented.
     * @param throwsToken Throws token.
     */
    private void verifyCheckedThrowJavadoc(DetailAST throwsToken) {
        TokenUtil.forEachChild(throwsToken, TokenTypes.IDENT, (throwTypeToken) -> {
            if (!methodJavadocThrowsMapping.get(currentMethodIdentifier).contains(throwTypeToken.getText())) {
                log(throwTypeToken, MISSING_THROWS_TAG_MESSAGE);
            }
        });
    }

    /*
     * Checks if the throw statement has documentation in the Javadoc.
     * @param throwToken Throw statement token.
     */
    private void verifyThrowJavadoc(DetailAST throwToken) {
        // Early out check for method that don't have Javadocs, they cannot have @throws documented.
        HashSet<String> methodJavadocThrows = methodJavadocThrowsMapping.get(currentMethodIdentifier);
        if (methodJavadocThrows == null) {
            log(throwToken, MISSING_THROWS_TAG_MESSAGE);
            return;
        }

        String throwType;
        DetailAST throwExpression = throwToken.findFirstToken(TokenTypes.EXPR);

        // Check if the throw is constructing the exception or throwing an instantiated exception.
        DetailAST literalNewToken = throwExpression.findFirstToken(TokenTypes.LITERAL_NEW);
        if (literalNewToken != null) {
            throwType = literalNewToken.findFirstToken(TokenTypes.IDENT).getText();
        } else {
            // Determine what is being thrown.
            String searchingFor;
            DetailAST thrownIdent = throwExpression.findFirstToken(TokenTypes.IDENT);
            if (thrownIdent != null) {
                searchingFor = thrownIdent.getText();
            } else {
                // More complex throw clause
                searchingFor = "";
            }

            log(throwToken, searchingFor);
            throwType = findInstantiatedThrow(searchingFor);
        }

        if ("RuntimeException".equals(throwType)) {
            return;
        }

        if (!methodJavadocThrows.contains(throwType)) {
            log(throwToken, MISSING_THROWS_TAG_MESSAGE);
        }
    }

    private String findInstantiatedThrow(String variableName) {
        return "";
    }
}
