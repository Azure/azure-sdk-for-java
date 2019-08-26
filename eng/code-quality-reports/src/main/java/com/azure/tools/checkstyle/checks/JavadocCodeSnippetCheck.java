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
import java.util.Locale;

/**
 * Codesnippet description should match naming pattern requirement below:
 * <ol>
 * <li>Package, class and method names should be concatenated by dot '.'. Ex., packageName.className.methodName</li>
 * <li>Methods arguments should be concatenated by dash '-'. Ex. string-string  for methodName(String s, String s2)</li>
 * <li>Use '#' to concatenate 1) and 2), ex packageName.className.methodName#string-string</li>
 * </ol>
 */
public class JavadocCodeSnippetCheck extends AbstractCheck {

    private static final String CODE_SNIPPET_ANNOTATION = "@codesnippet";
    private static final String MISSING_CODESNIPPET_TAG_MESSAGE = "There is a @codesnippet block in the JavaDoc, but it"
        + " does not refer to any sample.";

    private static final int[] TOKENS = new int[] {
        TokenTypes.PACKAGE_DEF,
        TokenTypes.BLOCK_COMMENT_BEGIN,
        TokenTypes.CLASS_DEF,
        TokenTypes.METHOD_DEF
    };

    private String packageName;
    // A container to contains all class name visited, remove the class name when leave the same token
    private Deque<String> classNameStack = new ArrayDeque<>();
    // Current METHOD_DEF token while traversal tree
    private DetailAST methodDefToken = null;

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
                methodDefToken = token;
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
     * Otherwise, check if the codesnippet has matching the naming pattern
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

        // Iterate through all the top level nodes in the Javadoc, looking for the @codesnippet tag.
        for (DetailNode node : javadocNode.getChildren()) {
            if (node.getType() != JavadocTokenTypes.JAVADOC_INLINE_TAG) {
                continue;
            }
            // Skip if not codesnippet
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

            // There will always have TEXT token if there is DESCRIPTION token exists.
            String customDescription = JavadocUtil.findFirstToken(descriptionNode, JavadocTokenTypes.TEXT).getText();

            // Find method name
            final String methodName = methodDefToken.findFirstToken(TokenTypes.IDENT).getText();
            final String className = classNameStack.isEmpty() ? "" : classNameStack.peek();
            final String parameters = constructParametersString(methodDefToken);
            String fullPath = packageName + "." + className + "." + methodName;
            final String fullPathWithoutParameters = fullPath;
            if (parameters != null) {
                fullPath = fullPath + "#" + parameters;
            }

            // Check for CodeSnippet naming pattern matching
            if (customDescription == null || customDescription.isEmpty()
                || !isNamingMatched(customDescription.toLowerCase(Locale.ROOT),
                    fullPathWithoutParameters.toLowerCase(Locale.ROOT), parameters)) {
                log(node.getLineNumber(), String.format("Naming pattern mismatch. The @codesnippet description "
                    + "''%s'' does not match ''%s''. Case Insensitive.", customDescription, fullPath));
            }
        }
    }

    /**
     * Construct a parameters string if the method has arguments.
     *
     * @param methodDefToken METHOD_DEF token
     * @return a valid parameter string or null if no method arguments exist.
     */
    private String constructParametersString(DetailAST methodDefToken) {
        final StringBuilder sb = new StringBuilder();
        // Checks for the parameters of the method
        final DetailAST parametersToken = methodDefToken.findFirstToken(TokenTypes.PARAMETERS);
        for (DetailAST ast = parametersToken.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() != TokenTypes.PARAMETER_DEF) {
                continue;
            }

            final DetailAST typeToken = ast.findFirstToken(TokenTypes.TYPE);
            final DetailAST identToken = typeToken.findFirstToken(TokenTypes.IDENT);
            String parameterType = "";
            if (identToken != null) {
                // For example, Map, String, Mono types
                parameterType = identToken.getText();
            } else {

                DetailAST arrayDeclarator = typeToken.findFirstToken(TokenTypes.ARRAY_DECLARATOR);
                if (arrayDeclarator == null) {
                    // For example, int, boolean, byte primitive types
                    parameterType = typeToken.getFirstChild().getText();
                }

                DetailAST arrayDeclaratorIterator = arrayDeclarator;
                while (arrayDeclaratorIterator != null) {
                    DetailAST temp = arrayDeclaratorIterator.findFirstToken(TokenTypes.ARRAY_DECLARATOR);
                    if (temp == null) {
                        // For example, int[][], byte[] types
                        parameterType = arrayDeclaratorIterator.getFirstChild().getText();
                        break;
                    }
                    arrayDeclaratorIterator = temp;
                }
            }
            sb.append(parameterType).append("-");
        }
        int size = sb.length();
        if (size == 0) {
            return null;
        }
        return sb.substring(0, size - 1);
    }

    /**
     * Check if the given customDescription from codesnippet matches the naming pattern rule.
     *
     * @param customDescription full sample code reference name from annotation codesnippet
     * @param fullPathWithoutParameters a string contains package name, class name, and method name if exist.
     * @param parameters parameters string which concatenate of argument types
     * @return false if the given custom description not matched with naming rule. Otherwise, return true.
     */
    private boolean isNamingMatched(String customDescription, String fullPathWithoutParameters, String parameters) {
        // Two same codesnippet samples should have two different key names,
        // For example, for method name methodName(string, string),
        // (1) packagename.classname.methodname#string-string
        // (2) packagename.classname.methodname#string-string-2
        final String[] descriptionSegments = customDescription.split("#");
        if (descriptionSegments.length == 1) {
            // There exists parameters in the actual Java sample, but there is no custom parameters exist.
            if (parameters != null) {
                return false;
            }

            final String pathUntilMethodName = descriptionSegments[0].split("-")[0];
            if (!fullPathWithoutParameters.equalsIgnoreCase(pathUntilMethodName)) {
                return false;
            }
        }

        if (descriptionSegments.length == 2) {
            // Both of codesnippet name and the method has parameters
            if (parameters != null) {
                return descriptionSegments[1].toLowerCase().startsWith(parameters.toLowerCase());
            }

            // Codesnippet name has parameters but the method does not.
            return false;
        }
        return true;
    }
}
