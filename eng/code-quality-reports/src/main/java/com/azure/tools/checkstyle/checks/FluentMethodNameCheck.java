// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Model Class Method check requirements:
 * <ol>
*  <li>Fluent Methods: All methods that return an instance of the class, and that have one parameter.</li>
 * <li>The method name should not start with {@code avoidStartWords}.</li>
 * <li>All methods should not throw checked exceptions.</li>
 * </ol>
 */
public class FluentMethodNameCheck extends AbstractCheck {
    /**
     * This is a custom defined set which contains all prefixes that are not allowed.
     */
    private final Set<String> avoidStartWords = new HashSet<>();

    /**
     * A LIFO Queue tracks the status of the inner class names when traversals the AST tree.
     */
    private final Queue<String> classNameStack = Collections.asLifoQueue(new ArrayDeque<>());

    /**
     * Adds words that methods in fluent classes should not be prefixed with.
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
                classNameStack.offer(token.findFirstToken(TokenTypes.IDENT).getText());
                break;
            case TokenTypes.METHOD_DEF:
                if (!isFluentMethod(token)) {
                    return;
                }
                checkMethodNamePrefix(token);

                // logs error if the @Fluent method has 'throws' at the method declaration.
                if (token.findFirstToken(TokenTypes.LITERAL_THROWS) != null) {
                    log(token, String.format(
                        "Fluent Method ''%s'' must not be declared to throw any checked exceptions.",
                        token.findFirstToken(TokenTypes.IDENT).getText()));
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    @Override
    public void leaveToken(DetailAST token) {
        if (token.getType() == TokenTypes.CLASS_DEF && !classNameStack.isEmpty()) {
            classNameStack.poll();
        }
    }

    /**
     * Log the error if the method name is not start with {@code avoidStartWord}
     * @param methodDefToken METHOD_DEF AST node
     */
    private void checkMethodNamePrefix(DetailAST methodDefToken) {
        // A fluent method should only has one parameter.
        if (TokenUtil.findFirstTokenByPredicate(methodDefToken, parameters ->
            parameters.getType() == TokenTypes.PARAMETERS && parameters.getChildCount() != 1).isPresent()) {
            log(methodDefToken, "A fluent method should only have one parameter.");
        }

        // A fluent method's return type should be the class itself
        final DetailAST typeToken = methodDefToken.findFirstToken(TokenTypes.TYPE);
        if (TokenUtil.findFirstTokenByPredicate(typeToken, ident -> ident.getType() == TokenTypes.IDENT
            && !ident.getText().equals(classNameStack.peek())).isPresent()) {
            log(methodDefToken, "Return type of fluent method should be the class itself");
        }

        final String methodName = methodDefToken.findFirstToken(TokenTypes.IDENT).getText();
        // method name should not start with words in the avoid string list
        avoidStartWords.forEach(avoidStartWord -> {
            if (methodName.length() >= avoidStartWord.length() && methodName.startsWith(avoidStartWord)) {
                log(methodDefToken, String.format("''%s'' fluent method name should not start with keyword ''%s''.",
                    methodName, avoidStartWord));
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
        // If no @Fluent annotated with this class, return false
        return TokenUtil.findFirstTokenByPredicate(modifiersToken,
            annotationToken -> annotationToken.getType() == TokenTypes.ANNOTATION
                && TokenUtil.findFirstTokenByPredicate(annotationToken,
                    identToken -> identToken.getType() == TokenTypes.IDENT
                        && "Fluent".equals(identToken.getText())).isPresent())
            .isPresent();
    }
}
