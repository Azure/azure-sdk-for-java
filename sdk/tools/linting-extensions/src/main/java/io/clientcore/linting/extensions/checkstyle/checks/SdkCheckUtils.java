// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.naming.AccessModifierOption;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

/**
 * Shared utilities used by custom Checkstyle checks.
 */
public final class SdkCheckUtils {
    /**
     * Check if variable modifiers contains any of the illegal combination with final modifier.
     * <p>
     * For instance, we don't want to combine transient or volatile with final.
     *
     * @param modifiers A {@link DetailAST} pointing to a Variable list of modifiers
     * @return true if there is any modifier that shouldn't be combined with final
     */
    public static boolean hasIllegalCombination(DetailAST modifiers) {
        if (modifiers.getType() != TokenTypes.MODIFIERS) {
            // can't check other node but MODIFIERS
            return false;
        }

        return TokenUtil.findFirstTokenByPredicate(modifiers, (node) -> {
            final int type = node.getType();

            return invalidFinalCombination(type) || invalidFinalAnnotation(type, node);
        }).isPresent();
    }

    private static boolean invalidFinalCombination(int type) {
        // Set of modifiers that cannot be combined with final because it causes a violation.
        return type == TokenTypes.LITERAL_PROTECTED
            || type == TokenTypes.LITERAL_TRANSIENT
            || type == TokenTypes.LITERAL_VOLATILE
            || type == TokenTypes.LITERAL_DEFAULT;
    }

    private static boolean invalidFinalAnnotation(int type, DetailAST ast) {
        if (type != TokenTypes.ANNOTATION) {
            // Only check annotations
            return false;
        }

        // Set of annotations that cannot be combined with modified 'final' because it would break serialization.
        String annotationName = ast.findFirstToken(TokenTypes.IDENT).getText();
        return "JsonProperty".equals(annotationName)
            || "JsonAlias".equals(annotationName)
            || "JacksonXmlProperty".equals(annotationName);
    }

    /**
     * Check if the given AST node has public or protected access modifier.
     *
     * @param astWithModifiers A {@link DetailAST} that contains modifiers, such as a class, method,
     * or variable definition.
     * @return Whether the access modifier is public or protected.
     */
    public static boolean isPublicOrProtected(DetailAST astWithModifiers) {
        AccessModifierOption access = CheckUtil.getAccessModifierFromModifiersToken(astWithModifiers);
        return access == AccessModifierOption.PUBLIC || access == AccessModifierOption.PROTECTED;
    }

    /**
     * Gets the called method's identifier without materializing the receiver expression as a string.
     *
     * @param methodCall a method-call node.
     * @return the called method name, or an empty string for an unsupported expression.
     */
    public static String getCalledMethodName(DetailAST methodCall) {
        DetailAST target = methodCall.getFirstChild();
        if (target != null && target.getType() == TokenTypes.DOT) {
            target = target.getLastChild();
        }
        return target != null && target.getType() == TokenTypes.IDENT ? target.getText() : "";
    }

    /**
     * Finds the throwable logging event in a {@code throwableAt*().addKeyValue(...).log(...)} chain.
     * Arguments to unrelated calls are deliberately not searched.
     *
     * @param methodCall the terminal {@code log} call.
     * @return the throwable event method call, or null if this is not a throwable logging chain.
     */
    public static DetailAST findThrowableLoggingEvent(DetailAST methodCall) {
        if (!"log".equals(getCalledMethodName(methodCall))) {
            return null;
        }
        DetailAST current = methodCall.getFirstChild();
        while (current != null && current.getType() == TokenTypes.DOT) {
            current = current.getFirstChild();
            if (current.getType() != TokenTypes.METHOD_CALL) {
                return null;
            }
            String name = getCalledMethodName(current);
            if ("throwableAtError".equals(name) || "throwableAtWarning".equals(name)) {
                DetailAST receiver = current.getFirstChild();
                return receiver != null && receiver.getType() == TokenTypes.DOT ? current : null;
            }
            current = current.getFirstChild();
        }
        return null;
    }

    private SdkCheckUtils() {
    }
}
