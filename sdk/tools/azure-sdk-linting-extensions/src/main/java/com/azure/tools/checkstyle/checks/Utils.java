// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

/**
 * Common utils amount custom checks
 */
public class Utils {
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

        return TokenUtil.findFirstTokenByPredicate(modifiers,
                node -> invalidFinalCombination(node) || invalidFinalAnnotation(node))
            .isPresent();
    }

    /*
     * Set of modifiers that cannot be combined with final because it causes a violation.
     */
    private static boolean invalidFinalCombination(DetailAST ast) {
        int type = ast.getType();
        return type == TokenTypes.LITERAL_TRANSIENT
            || type == TokenTypes.LITERAL_VOLATILE
            || type == TokenTypes.LITERAL_DEFAULT
            || type == TokenTypes.LITERAL_PROTECTED;
    }

    /*
     * Set of annotations that cannot be combined with modified 'final' because it would break serialization.
     */
    private static boolean invalidFinalAnnotation(DetailAST ast) {
        if (ast.getType() != TokenTypes.ANNOTATION) {
            return false;
        }

        String annotationName = ast.findFirstToken(TokenTypes.IDENT).getText();
        return "JacksonXmlProperty".equals(annotationName)
            || "JsonAlias".equals(annotationName)
            || "JsonProperty".equals(annotationName);
    }
}
