// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Model Class Method check requirements:
 * <ol>
*  <li>Fluent Methods: All methods that return an instance of the class, and that have one parameter.</li>
 * <li>The method name should not start with {@code avoidStartWords}.</li>
 * <li>All methods should not be declared to throws any checked exceptions.</li>
 * </ol>
 */
public class FluentMethodNameCheck extends AbstractCheck {

    private static final String FLUENT_METHOD_ERR = "\"%s\" fluent method name should not start with keyword \"%s\".";

    /**
     *  This is a custom defined set which contains all prefixes that are not allowed.
     */
    private Set<String> avoidStartWords = new HashSet<>();

    /**
     *  Use this stack to track the status of the inner class names when traversals the AST tree.
     */
    private Deque<String> classNameStack = new ArrayDeque<>();

    /**
     * Setter to specifies valid identifiers
     * @param avoidStartWords the starting strings that should not start with in fluent method
     */
    public final void setAvoidStartWords(String... avoidStartWords) {
        Collections.addAll(this.avoidStartWords, avoidStartWords);
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
            TokenTypes.CLASS_DEF,
            TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                classNameStack.addLast(token.findFirstToken(TokenTypes.IDENT).getText());
                break;
            case TokenTypes.METHOD_DEF:
                if (!isFluentMethod(token)) {
                    return;
                }
                checkMethodNamePrefix(token);

                // logs error if the @Fluent method has 'throws' at the method declaration.
                if (token.findFirstToken(TokenTypes.LITERAL_THROWS) != null) {
                    log(token, String.format("Fluent Method ''%s'' must not be declared to throw any checked exceptions"));
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    @Override
    public void leaveToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                if (!classNameStack.isEmpty()) {
                    classNameStack.removeLast();
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Log the error if the method name is not start with {@code avoidStartWord}
     * @param methodDefToken METHOD_DEF AST node
     */
    private void checkMethodNamePrefix(DetailAST methodDefToken) {
        // 1, parameter count should be 1
        if (!TokenUtil.findFirstTokenByPredicate(methodDefToken,
            c -> c.getType() == TokenTypes.PARAMETERS && c.getChildCount() == 1).isPresent()) {
            return;
        }

        // 2, method type should be matched with class name
        final DetailAST typeToken = methodDefToken.findFirstToken(TokenTypes.TYPE);
        if (classNameStack.isEmpty()) {
            return;
        }

        if (!TokenUtil.findFirstTokenByPredicate(
            typeToken, c -> c.getType() == TokenTypes.IDENT && c.getText().equals(classNameStack.peekLast())).isPresent()) {
            return;
        }

        final String methodName = methodDefToken.findFirstToken(TokenTypes.IDENT).getText();
        // 3, log if the method name start with avoid substring
        avoidStartWords.forEach(avoidStartWord -> {
            if (methodName.length() >= avoidStartWord.length() && methodName.startsWith(avoidStartWord)) {
                log(methodDefToken, String.format(FLUENT_METHOD_ERR, methodName, avoidStartWord));
            }
        });
    }

    /**
     * Checks if the method is annotated with annotation @Fluent
     *
     * @param methodDefToken the METHOD_DEF AST node
     * @return true if the class is annotated with @Fluent, false otherwise.
     */
    private boolean isFluentMethod(DetailAST methodDefToken) {
        // Always has MODIFIERS node
        final DetailAST modifiersToken = methodDefToken.findFirstToken(TokenTypes.MODIFIERS);

        for (DetailAST annotationToken = modifiersToken.getFirstChild(); annotationToken != null;
             annotationToken = annotationToken.getNextSibling()) {
            if (annotationToken.getType() != TokenTypes.ANNOTATION) {
                continue;
            }
            // One class could have multiple annotations, return true if found one.
            final DetailAST annotationIdent = annotationToken.findFirstToken(TokenTypes.IDENT);
            if (annotationIdent != null && "Fluent".equals(annotationIdent.getText())) {
                return true;
            }
        }
        // If no @Fluent annotated with this class, return false
        return false;
    }
}
