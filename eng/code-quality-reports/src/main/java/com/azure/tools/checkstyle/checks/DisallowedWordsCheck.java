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
    private static final String MISSING_DISALLOWED_WORDS_PROPERTY = "The disallowedWords property is required for the " + DisallowedWordsCheck.class.getSimpleName() + " module. Please specify which words should be disallowed from being used.";
    private Set<String> disallowedWords = new HashSet<>();
    private String errorMessage = "";
    private boolean applyToPublic = true;
    private boolean loggedAlready = false;

    /**
     * Adds words that Classes, Methods and Variables that should follow Camelcasing standards
     * @param disallowedWords words that should follow normal Camelcasing standards
     */
    public final void setDisallowedWords(String... disallowedWords) {
        if (this.disallowedWords != null) {
            Collections.addAll(this.disallowedWords, disallowedWords);
            errorMessage = String.format("All Public API Classes, Fields and Methods should follow "
                + "Camelcase standards for the following words: %s.", this.disallowedWords.stream().collect(Collectors.joining(", ", "", "")));
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
            case TokenTypes.VARIABLE_DEF:
                if (disallowedWords.size() == 0 && !loggedAlready) {
                    log(1, 0, String.format(MISSING_DISALLOWED_WORDS_PROPERTY));
                    loggedAlready = true;
                    break;
                }
                String tokenName = token.findFirstToken(TokenTypes.IDENT).getText();
                if (shouldCheckInScope(token)) {
                    String result = getDisallowedWords(tokenName);
                    if (result != null) {
                        log(token, String.format(errorMessage));
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
     * @param ast
     *                modifiers of member to check.
     * @return true if we should check such member.
     */
    private boolean shouldCheckInScope(DetailAST ast) {
        final DetailAST modifiersAST =
            ast.findFirstToken(TokenTypes.MODIFIERS);
        final boolean isPublic = modifiersAST
            .findFirstToken(TokenTypes.LITERAL_PUBLIC) != null;
        return applyToPublic && isPublic;
    }

    /**
     * Gets the disallowed abbreviation contained in given String.
     * @param str
     *        the given String.
     * @return the disallowed abbreviation contained in given String as a
     *         separate String.
     */
    private String getDisallowedWords(String str) {
        int beginIndex = 0;
        boolean abbrStarted = false;
        String result = null;

        for (int index = 0; index < str.length(); index++) {
            final char symbol = str.charAt(index);

            if (Character.isUpperCase(symbol)) {
                if (!abbrStarted) {
                    abbrStarted = true;
                    beginIndex = index;
                }
            }
            else if (abbrStarted) {
                abbrStarted = false;

                final int endIndex = index - 1;
                result = getAbbreviationIfIllegal(str, beginIndex, endIndex);
                if (result != null) {
                    break;
                }
                beginIndex = -1;
            }
        }
        // if abbreviation at the end of name (example: scaleX)
        if (abbrStarted) {
            final int endIndex = str.length() - 1;
            result = getAbbreviationIfIllegal(str, beginIndex, endIndex);
        }
        return result;
    }

    /**
     * Get Abbreviation if it is illegal, where {@code beginIndex} and {@code endIndex} are
     * inclusive indexes of a sequence of consecutive upper-case characters.
     * @param str name
     * @param beginIndex begin index
     * @param endIndex end index
     * @return the abbreviation if it is bigger than required and not in the
     *         ignore list, otherwise {@code null}
     */
    private String getAbbreviationIfIllegal(String str, int beginIndex, int endIndex) {
        String result = null;
        final String abbr = getAbbreviation(str, beginIndex, endIndex);
        if (disallowedWords.contains(abbr)) {
            result = abbr;
        }
        return result;
    }

    private static String getAbbreviation(String str, int beginIndex, int endIndex) {
        String result;
        if (endIndex == str.length() - 1) {
            result = str.substring(beginIndex);
        } else {
            result = str.substring(beginIndex, endIndex);
        }

        return result;
    }
}
