// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;

/**
 * Checks that methods in fluent classes follow the naming conventions and requirements.
 * <p>
 * Model Class Method check requirements:
 * <ol>
*  <li>Fluent Methods: All methods that return an instance of the class, and that have one parameter.</li>
 * <li>The method name should not start with {@code disallowedPrefixes}.</li>
 * <li>All methods should not throw checked exceptions.</li>
 * </ol>
 * <p>
 * {@code disallowedPrefixes} (optional): An array of prefixes that aren't allowed. Defined in the checkstyle.xml config
 * file. If left empty, no prefixes will be checked.
 */
public class FluentMethodNameCheck extends AbstractCheck {
    /**
     * A LIFO Queue tracks the status of the inner class names when traversals the AST tree.
     */
    private final Queue<String> classNameStack = Collections.asLifoQueue(new ArrayDeque<>());

    /**
     * This is a custom defined set which contains all prefixes that are not allowed.
     */
    private String[] disallowedPrefixes = new String[0];

    /**
     * Creates a new instance of {@link FluentMethodNameCheck}.
     */
    public FluentMethodNameCheck() {
    }

    /**
     * Adds words that methods in fluent classes should not be prefixed with.
     * @param disallowedPrefixes the starting strings that should not start with in fluent method
     */
    public final void setDisallowedPrefixes(String... disallowedPrefixes) {
        if (disallowedPrefixes != null) {
            this.disallowedPrefixes = Arrays.copyOf(disallowedPrefixes, disallowedPrefixes.length);
        }
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
        return new int[] { TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF };
    }

    @Override
    public void visitToken(DetailAST token) {
        if (token.getType() == TokenTypes.CLASS_DEF) {
            classNameStack.offer(token.findFirstToken(TokenTypes.IDENT).getText());
        } else if (token.getType() == TokenTypes.METHOD_DEF && isFluentMethod(token)) {
            checkMethodNamePrefix(token);

            // logs error if the @Fluent method has 'throws' at the method declaration.
            if (token.findFirstToken(TokenTypes.LITERAL_THROWS) != null) {
                log(token, String.format("Fluent Method ''%s'' must not be declared to throw any checked exceptions.",
                    token.findFirstToken(TokenTypes.IDENT).getText()));
            }
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
        // A fluent method should only have one parameter.
        if (TokenUtil
            .findFirstTokenByPredicate(methodDefToken,
                parameters -> parameters.getType() == TokenTypes.PARAMETERS && parameters.getChildCount() != 1)
            .isPresent()) {
            log(methodDefToken, "A fluent method should only have one parameter.");
        }

        // A fluent method's return type should be the class itself
        final DetailAST typeToken = methodDefToken.findFirstToken(TokenTypes.TYPE);
        if (TokenUtil
            .findFirstTokenByPredicate(typeToken,
                ident -> ident.getType() == TokenTypes.IDENT && !ident.getText().equals(classNameStack.peek()))
            .isPresent()) {
            log(methodDefToken, "Return type of fluent method should be the class itself");
        }

        final String methodName = methodDefToken.findFirstToken(TokenTypes.IDENT).getText();
        // method name should not start with words in the avoid string list
        for (String avoidStartWord : disallowedPrefixes) {
            if (methodName.startsWith(avoidStartWord)) {
                log(methodDefToken, String.format("''%s'' fluent method name should not start with keyword ''%s''.",
                    methodName, avoidStartWord));
            }
        }
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
        return TokenUtil
            .findFirstTokenByPredicate(modifiersToken,
                annotationToken -> annotationToken.getType() == TokenTypes.ANNOTATION)
            .flatMap(annotationToken -> TokenUtil.findFirstTokenByPredicate(annotationToken,
                identToken -> identToken.getType() == TokenTypes.IDENT && "Fluent".equals(identToken.getText())))
            .isPresent();
    }
}
