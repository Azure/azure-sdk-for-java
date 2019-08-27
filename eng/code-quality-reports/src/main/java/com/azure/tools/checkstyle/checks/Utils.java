// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Common utils amount custom checks
 */
public class Utils {
    /*
     * Set of modifiers that cannot be combined with final because it causes a violation.
     */
    private static final Set INVALID_FINAL_COMBINATION = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        TokenTypes.LITERAL_TRANSIENT,
        TokenTypes.LITERAL_VOLATILE,
        TokenTypes.LITERAL_DEFAULT,
        TokenTypes.LITERAL_PROTECTED
    )));

    /*
     * Set of annotations that cannot be combined with modifier 'final' because it would break serialization.
     */
    private static final Set INVALID_FINAL_ANNOTATIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "JsonProperty",
        "JsonAlias",
        "JacksonXmlProperty"
    )));

    /**
     * Check if variable modifiers contains any of the illegal combination with final modifier
     * For instance, we don't want to combine transient or volatile with final
     *
     * @param modifiers a DetailAST pointing to a Variable list of modifiers
     * @return true if there is any modifier that shouldn't be combined with final
     */
    protected static boolean hasIllegalCombination(DetailAST modifiers) {
        if (modifiers.getType() != TokenTypes.MODIFIERS) {
            // can't check other node but MODIFIERS
            return false;
        }

        Optional<DetailAST> illegalCombination = TokenUtil.findFirstTokenByPredicate(modifiers, (node) -> {
            final int type = node.getType();
            return INVALID_FINAL_COMBINATION.contains(node.getType()) || (TokenTypes.ANNOTATION == type
                && INVALID_FINAL_ANNOTATIONS.contains(node.findFirstToken(TokenTypes.IDENT).getText()));
        });

        return illegalCombination.isPresent();
    }
}
