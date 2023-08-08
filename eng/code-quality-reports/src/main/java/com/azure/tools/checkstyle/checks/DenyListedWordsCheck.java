// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.naming.AccessModifierOption;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Ensure that code is not using words or abbreviations that are deny listed by this Checkstyle. denyListedWords: the
 * words that have been denied in the checkstyle.xml config file
 * <p>
 * Prints out a message stating the location and the class, method or variable as well as the list of deny listed words.
 */
public class DenyListedWordsCheck extends AbstractCheck {
    private final Set<String> denyListedWords = new HashSet<>();

    static final String ERROR_MESSAGE = "%s, All Public API Classes, Fields and Methods should follow "
        + "Camelcase standards for the following words: %s.";

    /**
     * Adds words that Classes, Methods and Variables that should follow Camelcasing standards
     *
     * @param denyListedWords words that should follow normal Camelcasing standards
     */
    public final void setDenyListedWords(String... denyListedWords) {
        if (denyListedWords != null) {
            Collections.addAll(this.denyListedWords, denyListedWords);
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
        return new int[]{TokenTypes.CLASS_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.VARIABLE_DEF};
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
            case TokenTypes.METHOD_DEF:
            case TokenTypes.VARIABLE_DEF:
                if (!isPublicApi(token)) {
                    break;
                }

                final String tokenName = token.findFirstToken(TokenTypes.IDENT).getText();
                if (!hasDenyListedWords(tokenName)) {
                    break;
                }

                // In an interface all the fields (variables) are by default public, static and final.
                if (token.getType() == TokenTypes.VARIABLE_DEF && ScopeUtil.isInInterfaceBlock(token)) {
                    break;
                }

                log(token, String.format(ERROR_MESSAGE, tokenName, String.join(", ", this.denyListedWords)));

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
        final DetailAST modifiersAST = token.findFirstToken(TokenTypes.MODIFIERS);
        final AccessModifierOption accessModifier = CheckUtil.getAccessModifierFromModifiersToken(token);
        final boolean isStatic = modifiersAST.findFirstToken(TokenTypes.LITERAL_STATIC) != null;
        return (accessModifier.equals(AccessModifierOption.PUBLIC) || accessModifier.equals(AccessModifierOption.PROTECTED)) && !isStatic;
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
