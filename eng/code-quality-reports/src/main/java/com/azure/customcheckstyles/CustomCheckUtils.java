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
}
