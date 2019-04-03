package com.azure.customcheckstyles;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.util.function.Function;
import java.util.function.Predicate;

final class CustomCheckUtils {
    /**
     * Finds the next sibling node that passes the given predicate.
     * @param ast Node whose siblings are being checked
     * @param predicate Check that the node needs to pass
     * @return The first sibling that passes the check, null if none are found
     */
    static DetailAST findNextSiblingOfType(DetailAST ast, Predicate<DetailAST> predicate) {
        return findSiblingOfTypeHelper(ast, predicate, DetailAST::getNextSibling);
    }

    /**
     * Finds the previous sibling node that passes the given predicate.
     * @param ast Node whose siblings are being checked
     * @param predicate Check that the node needs to pass
     * @return The first sibling that passes the check, null if none are found
     */
    static DetailAST findPreviousSiblingOfType(DetailAST ast, Predicate<DetailAST> predicate) {
        return findSiblingOfTypeHelper(ast, predicate, DetailAST::getPreviousSibling);
    }

    private static DetailAST findSiblingOfTypeHelper(DetailAST ast, Predicate<DetailAST> predicate, Function<DetailAST, DetailAST> function) {
        for (DetailAST sibling = function.apply(ast); sibling != null; sibling = function.apply(sibling)){
            if (predicate.test(sibling)) {
                return sibling;
            }
        }

        return null;
    }

    /**
     * Checks that the modifier node contains all the passed modifiers
     * @param modifierNode Node that contains modifiers
     * @param requiredModifiers Modifiers that need to be contained in the modifier node
     * @return True if the node contains all the modifiers, false otherwise
     */
    static boolean hasAllModifiers(DetailAST modifierNode, int... requiredModifiers) {
        for (int requiredModifier : requiredModifiers) {
            if (!hasModifier(modifierNode, requiredModifier)) {
                return false;
            }
        }

        return true;
    }

    static boolean hasAnyModifier(DetailAST modifierNode, int... anyModifiers) {
        for (int anyModifier : anyModifiers) {
            if (hasModifier(modifierNode, anyModifier)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasModifier(DetailAST modifierNode, int modifierType) {
        return modifierNode.findFirstToken(modifierType) != null;
    }
}
