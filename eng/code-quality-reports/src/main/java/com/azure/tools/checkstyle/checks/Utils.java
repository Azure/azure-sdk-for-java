package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Common utils amount custom checks
 */
public class Utils {
    private static final Set<Integer> INVALID_FINAL_COMBINATION = new HashSet<>(Arrays.asList(
        TokenTypes.LITERAL_TRANSIENT,
        TokenTypes.LITERAL_VOLATILE
    ));

    private static final Set<String> INVALID_FINAL_ANNOTATIONS = new HashSet<>(Arrays.asList(
        "JsonProperty"
    ));

    /**
     * Check if variable modifiers contains any of the illegal combination with final modifier
     * For instance, we don't want to combine transient or volatile with final
     *
     * @param modifiers a DetailAST pointing to a Variable list of modifiers
     * @return true if there is any modifier that shouldn't be combined with final
     */
    protected static boolean hasIllegalCombination(DetailAST modifiers) {
        for (DetailAST modifier = modifiers.getFirstChild(); modifier != null; modifier = modifier.getNextSibling()) {
            int modifierType = modifier.getType();
            // Do not consider field with some annotations
            if (TokenTypes.ANNOTATION == modifierType) {
                if (INVALID_FINAL_ANNOTATIONS.contains(modifier.findFirstToken(TokenTypes.IDENT).getText())) {
                    return true;
                }
            }
            if (INVALID_FINAL_COMBINATION.contains(modifierType)) {
                return true;
            }
        }
        return false;
    }
}
