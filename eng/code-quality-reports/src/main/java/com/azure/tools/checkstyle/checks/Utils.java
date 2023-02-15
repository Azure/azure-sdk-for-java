// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.Arrays;
import java.util.Optional;

/**
 * Common utils amount custom checks
 */
public class Utils {
    /*
     * Set of modifiers that cannot be combined with final because it causes a violation.
     *
     * This is an int array instead of a Set<Integer> as there are so few values and cache locality is better.
     */
    private static final int[] INVALID_FINAL_COMBINATION;

    /*
     * Set of annotations that cannot be combined with modified 'final' because it would break serialization.
     *
     * This is a String array instead of a Set<String> as there are so few values and cache locality is better.
     */
    private static final String[] INVALID_FINAL_ANNOTATIONS;

    static {
        INVALID_FINAL_COMBINATION = new int[] {
            TokenTypes.LITERAL_TRANSIENT,
            TokenTypes.LITERAL_VOLATILE,
            TokenTypes.LITERAL_DEFAULT,
            TokenTypes.LITERAL_PROTECTED
        };
        Arrays.sort(INVALID_FINAL_COMBINATION);

        INVALID_FINAL_ANNOTATIONS = new String[] {
            "JacksonXmlProperty",
            "JsonAlias",
            "JsonProperty"
        };
        Arrays.sort(INVALID_FINAL_ANNOTATIONS);
    }

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

            return invalidFinalCombination(type) || invalidFinalAnnotation(type, node);
        });

        return illegalCombination.isPresent();
    }

    private static boolean invalidFinalCombination(int type) {
        return Arrays.binarySearch(INVALID_FINAL_COMBINATION, type) != -1;
    }

    private static boolean invalidFinalAnnotation(int type, DetailAST ast) {
        return type == TokenTypes.ANNOTATION
            && Arrays.binarySearch(INVALID_FINAL_ANNOTATIONS, ast.findFirstToken(TokenTypes.IDENT).getText(), String::compareTo) != -1;
    }
}
