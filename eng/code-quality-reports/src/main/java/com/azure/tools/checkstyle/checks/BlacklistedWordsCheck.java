// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.naming.AccessModifier;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ensure that code is not using words or abbreviations that are blacklisted by this Checkstyle.
 * blacklistedWords: the words that have been blacklisted in the checkstyle.xml config file
 *
 * Prints out a message stating the location and the class, method or variable as well as the list
 * blacklisted words.
 */
public class BlacklistedWordsCheck extends AbstractCheck {
    private final Set<String> blacklistedWords = new HashSet<>(Arrays.asList());
    private final String ERROR_MESSAGE = "%s, All Public API Classes, Fields and Methods should follow " +
        "Camelcase standards for the following words: %s.";

    /**
     * Adds words that Classes, Methods and Variables that should follow Camelcasing standards
     * @param blacklistedWords words that should follow normal Camelcasing standards
     */
    public final void setBlacklistedWords(String... blacklistedWords) {
        if (blacklistedWords != null) {
            Collections.addAll(this.blacklistedWords, blacklistedWords);
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
        return new int[] {TokenTypes.CLASS_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.VARIABLE_DEF};
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
            case TokenTypes.METHOD_DEF:
            case TokenTypes.VARIABLE_DEF:
                if (isPublicApi(token)) {
                    String tokenName = token.findFirstToken(TokenTypes.IDENT).getText();
                    if (hasBlacklistedWords(tokenName)) {
                        log(token, String.format(ERROR_MESSAGE, tokenName, this.blacklistedWords.stream().collect(Collectors.joining(", ", "", ""))));
                    }
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Should we check member with given modifiers.
     *
     * @param token modifiers of member to check.
     * @return true if we should check such member.
     */
    private boolean isPublicApi(DetailAST token) {
        final DetailAST modifiersAST =
            token.findFirstToken(TokenTypes.MODIFIERS);
        final AccessModifier accessModifier = CheckUtil.getAccessModifierFromModifiersToken(modifiersAST);
        final boolean isStatic = modifiersAST.findFirstToken(TokenTypes.LITERAL_STATIC) != null;
        return (accessModifier.equals(AccessModifier.PUBLIC) || accessModifier.equals(AccessModifier.PROTECTED)) && !isStatic;
    }

    /**
     * Gets the disallowed abbreviation contained in given String.
     * @param tokenName the given String.
     * @return the disallowed abbreviation contained in given String as a
     *         separate String.
     */
    private boolean hasBlacklistedWords(String tokenName) {
        for (String blacklistedWord : blacklistedWords) {
            if (tokenName.contains(blacklistedWord)) {
                return true;
            }
        }
        return false;
    }
}
