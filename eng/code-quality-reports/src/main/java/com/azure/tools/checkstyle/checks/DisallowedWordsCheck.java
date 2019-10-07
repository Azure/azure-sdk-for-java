// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DisallowedWordsCheck extends AbstractCheck {
    private Set<String> disallowedWords = new HashSet<>(Arrays.asList());
    private String errorMessage = "%s, All Public API Classes, Fields and Methods should follow " +
        "Camelcase standards for the following words: %s.";

    /**
     * Adds words that Classes, Methods and Variables that should follow Camelcasing standards
     * @param disallowedWords words that should follow normal Camelcasing standards
     */
    public final void setDisallowedWords(String... disallowedWords) {
        if (disallowedWords != null) {
            Collections.addAll(this.disallowedWords, disallowedWords);
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
        TokenTypes.VARIABLE_DEF,
        TokenTypes.METHOD_DEF};
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
            case TokenTypes.METHOD_DEF:
                if (isPublicAPI(token)) {
                    String tokenName = token.findFirstToken(TokenTypes.IDENT).getText();
                    boolean found = getDisallowedWords(tokenName);
                    if (found) {
                        log(token, String.format(errorMessage, tokenName, this.disallowedWords.stream().collect(Collectors.joining(", ", "", ""))));
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
     * @param token
     *                modifiers of member to check.
     * @return true if we should check such member.
     */
    private boolean isPublicAPI(DetailAST token) {
        final DetailAST modifiersAST =
            token.findFirstToken(TokenTypes.MODIFIERS);
        final boolean isStatic = modifiersAST.findFirstToken(TokenTypes.LITERAL_STATIC) != null;
        final boolean isPublic = modifiersAST
            .findFirstToken(TokenTypes.LITERAL_PUBLIC) != null;
        final boolean isProtected = modifiersAST.findFirstToken(TokenTypes.LITERAL_PROTECTED) != null;
        return (isPublic || isProtected) && !isStatic;
    }

    /**
     * Gets the disallowed abbreviation contained in given String.
     * @param tokenName
     *        the given String.
     * @return the disallowed abbreviation contained in given String as a
     *         separate String.
     */
    private boolean getDisallowedWords(String tokenName) {
        boolean result = false;
        for (String disallowedWord : disallowedWords) {
            if (tokenName.contains(disallowedWord)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Get Abbreviation if it is illegal, where {@code beginIndex} and {@code endIndex} are
     * inclusive indexes of a sequence of consecutive upper-case characters.
     * @param tokenName name
     * @param beginIndex begin index
     * @param endIndex end index
     * @return the abbreviation if it is bigger than required and not in the
     *         ignore list, otherwise {@code null}
     */
    private String getAbbreviationIfIllegal(String tokenName, int beginIndex, int endIndex) {
        String result = null;
        final String abbr = getAbbreviation(tokenName, beginIndex, endIndex);
        if (disallowedWords.contains(abbr)) {
            result = abbr;
        }
        return result;
    }

    private static String getAbbreviation(String tokenName, int beginIndex, int endIndex) {
        String result;
        if (endIndex == tokenName.length() - 1) {
            result = tokenName.substring(beginIndex);
        } else {
            result = tokenName.substring(beginIndex, endIndex);
        }
        return result;
    }
}
