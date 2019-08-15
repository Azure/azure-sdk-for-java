// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.DetailNodeTreeStringPrinter;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.BlockCommentPosition;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @codesnippet description should match naming pattern requirement below:
 * <ol>
 *   <li>Package, class and method names should be concatenated by dot '.'. Ex., packageName.className.methodName</li>
 *   <li>Methods arguments should be concatenated by dash '-'. Ex. string-string  for methodName(String s, String s2)</li>
 *   <li>Use '#' to concatenate 1) and 2), ex packageName.className.methodName#string-string</li>
 * </ol>
 *
 */
public class JavadocCodeSnippetCheck extends AbstractCheck {

    private static final String CODE_SNIPPET_ANNOTATION = "@codesnippet";
    private static final String MISSING_CODESNIPPET_TAG_MESSAGE = "Javadoc @codesnippet tag required description.";


    private static final int[] TOKENS = new int[] {
        TokenTypes.PACKAGE_DEF,
        TokenTypes.BLOCK_COMMENT_BEGIN,
        TokenTypes.CLASS_DEF,
        TokenTypes.METHOD_DEF
    };

    private String packageName;
    // A container to contains all class name visited, remove the class name when leave the same token
    private Deque<String> classNameStack = new ArrayDeque<>();
    // A container to contains all METHOD_DEF node visited, remove the node whenever leave the same token
    private Deque<DetailAST> methodDefStack = new ArrayDeque<>();

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
    public void leaveToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                if (!classNameStack.isEmpty()) {
                    classNameStack.pop();
                }
                break;
            case TokenTypes.METHOD_DEF:
                if (!methodDefStack.isEmpty()) {
                    methodDefStack.pop();
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.PACKAGE_DEF:
                packageName = FullIdent.createFullIdent(token.findFirstToken(TokenTypes.DOT)).getText();
                break;
            case TokenTypes.CLASS_DEF:
                classNameStack.push(token.findFirstToken(TokenTypes.IDENT).getText());
                break;
            case TokenTypes.METHOD_DEF:
                methodDefStack.push(token);
                break;
            case TokenTypes.BLOCK_COMMENT_BEGIN:
                checkNamingPattern(token);
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Check if the given block comment is on method. If not, skip the check.
     * Otherwise, check if the {@literal {@codesnippet}} has matching the naming pattern
     *
     * @param blockCommentToken BLOCK_COMMENT_BEGIN token
     */
    private void checkNamingPattern(DetailAST blockCommentToken) {
        if (!BlockCommentPosition.isOnMethod(blockCommentToken)) {
            return;
        }

        // Turn the DetailAST into a Javadoc DetailNode.
        DetailNode javadocNode = null;
        try {
            javadocNode = DetailNodeTreeStringPrinter.parseJavadocAsDetailNode(blockCommentToken);
        } catch (IllegalArgumentException ex) {
            // Exceptions are thrown if the JavaDoc has invalid formatting.
        }

        if (javadocNode == null) {
            return;
        }

        // Iterate through all the top level nodes in the Javadoc, looking for the @throws statements.
        for (DetailNode node : javadocNode.getChildren()) {
            if (node.getType() != JavadocTokenTypes.JAVADOC_INLINE_TAG) {
                continue;
            }
            // Skip if not codesnippet annotation
            DetailNode customNameNode = JavadocUtil.findFirstToken(node, JavadocTokenTypes.CUSTOM_NAME);
            if (customNameNode == null || !CODE_SNIPPET_ANNOTATION.equals(customNameNode.getText())) {
                return;
            }
            // Missing Description
            DetailNode descriptionNode = JavadocUtil.findFirstToken(node, JavadocTokenTypes.DESCRIPTION);
            if (descriptionNode == null) {
                log(node.getLineNumber(), MISSING_CODESNIPPET_TAG_MESSAGE);
                return;
            }

            String description = JavadocUtil.findFirstToken(descriptionNode, JavadocTokenTypes.TEXT).getText();

            // Find method name
            DetailAST methodDefToken = methodDefStack.peek();
            final String methodName = methodDefToken.findFirstToken(TokenTypes.IDENT).getText();
            final String className = classNameStack.isEmpty() ? "" : classNameStack.peek();
            final String parameters = constructParametersString(methodDefToken);
            String fullPath = packageName + "." + className + "." + methodName;

            if (parameters != null) {
                fullPath = fullPath + "#" + parameters;
            }

            // Check for CodeSnippet naming pattern matching
            if (!description.equalsIgnoreCase(fullPath)) {
                log(node.getLineNumber(), String.format("Naming pattern mismatch. The @codeSnippet description " +
                    "''%s'' doesn't match ''%s''. Case Insensitive.", description, fullPath));
            }
        }
    }

    /**
     * Construct a parameters string if the method has arguments.
     * @param methodDefToken METHOD_DEF token
     * @return a valid parameter string or null if no method arguments exist.
     */
    private String constructParametersString(DetailAST methodDefToken) {
        StringBuilder sb = new StringBuilder();
        // Checks for the parameters of the method
        final DetailAST parametersToken = methodDefToken.findFirstToken(TokenTypes.PARAMETERS);
        for (DetailAST ast = parametersToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() != TokenTypes.PARAMETER_DEF) {
                continue;
            }
            String parameterType = ast.findFirstToken(TokenTypes.TYPE).getFirstChild().getText();
            sb.append(parameterType).append("-");
        }
        int size = sb.length();
        if (size == 0) {
            return null;
        }
        return sb.substring(0, size - 1);
    }
}
