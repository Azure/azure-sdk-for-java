// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;

import java.util.Arrays;

/**
 * Checks that public class and interface definitions, methods, and variables don't use words or abbreviations that are
 * deny listed by this Checkstyle.
 * <p>
 * {@code denyListedWords} (optional): An array of words that cannot be used. Defined in the checkstyle.xml config file.
 * If left empty, no words will be checked.
 */
public class DenyListedWordsCheck extends ImplementationExcludingCheck {
    static final String ERROR_MESSAGE_TEMPLATE
        = ": All public API classes, methods, and fields should follow camelcase standards for the following words: ";

    private String[] denyListedWords = new String[0];
    String computedErrorMessage;

    /**
     * Creates a new instance of {@link DenyListedWordsCheck}.
     */
    public DenyListedWordsCheck() {
    }

    /**
     * Sets the words that cannot be used in public class and interface definitions, methods, and variables.
     *
     * @param denyListedWords The words that cannot be used.
     */
    public final void setDenyListedWords(String... denyListedWords) {
        if (denyListedWords != null) {
            this.denyListedWords = Arrays.copyOf(denyListedWords, denyListedWords.length);
            this.computedErrorMessage = ERROR_MESSAGE_TEMPLATE + String.join(", ", this.denyListedWords);
        }
    }

    @Override
    public int[] getTokensForCheck() {
        return new int[] {
            TokenTypes.CLASS_DEF,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.VARIABLE_DEF };
    }

    @Override
    public void processToken(DetailAST token) {
        if (!isPublicApi(token)) {
            return;
        }

        final String tokenName = token.findFirstToken(TokenTypes.IDENT).getText();
        if (!hasDenyListedWords(tokenName)) {
            return;
        }

        // In an interface all the fields (variables) are by default public, static, and final.
        if (token.getType() == TokenTypes.VARIABLE_DEF && ScopeUtil.isInInterfaceBlock(token)) {
            return;
        }

        log(token, tokenName + computedErrorMessage);
    }

    /**
     * Should we check member with given modifiers.
     *
     * @param token modifiers of member to check.
     * @return true if we should check such member.
     */
    private boolean isPublicApi(DetailAST token) {
        final DetailAST modifiersAST = token.findFirstToken(TokenTypes.MODIFIERS);
        return SdkCheckUtils.isPublicOrProtected(token)
            && modifiersAST.findFirstToken(TokenTypes.LITERAL_STATIC) == null;
    }

    /**
     * Gets the disallowed abbreviation contained in given String.
     *
     * @param tokenName the given String.
     * @return the disallowed abbreviation contained in given String as a separate String.
     */
    private boolean hasDenyListedWords(String tokenName) {
        for (String denyListedWord : denyListedWords) {
            if (tokenName.contains(denyListedWord)) {
                return true;
            }
        }
        return false;
    }
}
