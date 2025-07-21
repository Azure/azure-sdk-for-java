// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.Scope;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;

import java.util.Stack;

/**
 * Verify the classes with annotation {@code @Immutable} should have the following rules:
 * <ol>
 *   <li>No public or protected fields</li>
 *   <li>No public or protected setter methods</li>
 * </ol>
 */
public class ImmutableClassCheck extends AbstractCheck {
    private static final String IMMUTABLE_NOTATION = "Immutable";

    static final String PUBLIC_FIELD_ERROR_TEMPLATE
        = "Classes annotated with @Immutable cannot have non-final public or protect fields. Found non-final public field: %s.";
    static final String SETTER_METHOD_ERROR_TEMPLATE
        = "Classes annotated with @Immutable cannot have public or protected setter methods. Found public setter method: %s.";

    private Stack<Boolean> hasImmutableAnnotationStack;

    /**
     * Creates a new instance of {@link ImmutableClassCheck}.
     */
    public ImmutableClassCheck() {
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
        return new int[] { TokenTypes.CLASS_DEF, TokenTypes.VARIABLE_DEF, TokenTypes.METHOD_DEF };
    }

    @Override
    public void beginTree(DetailAST root) {
        hasImmutableAnnotationStack = new Stack<>();
    }

    @Override
    public void visitToken(DetailAST token) {
        if (token.getType() == TokenTypes.CLASS_DEF) {
            hasImmutableAnnotationStack.add(hasImmutableAnnotation(token));
        } else if (token.getType() == TokenTypes.VARIABLE_DEF && isInImmutableClass()) {
            checkForPublicField(token);
        } else if (token.getType() == TokenTypes.METHOD_DEF && isInImmutableClass()) {
            checkForSetterMethod(token);
        }
    }

    private boolean isInImmutableClass() {
        return !hasImmutableAnnotationStack.isEmpty() && hasImmutableAnnotationStack.peek();
    }

    @Override
    public void leaveToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.CLASS_DEF && !hasImmutableAnnotationStack.isEmpty()) {
            hasImmutableAnnotationStack.pop();
        }
    }

    /*
     * Checks if the class is annotated with annotation {@literal @Immutable}. A class could have multiple annotations.
     *
     * @param classDefToken the CLASS_DEF AST node
     * @return true if the class is annotated with {@literal @Immutable}, false otherwise.
     */
    private static boolean hasImmutableAnnotation(DetailAST classDefinition) {
        return AnnotationUtil.getAnnotation(classDefinition, IMMUTABLE_NOTATION) != null;
    }

    private void checkForPublicField(DetailAST variableDefinition) {
        DetailAST modifiers = variableDefinition.findFirstToken(TokenTypes.MODIFIERS);

        // Field has no modifiers or is final, therefore it's immutable for all intents and purposes.
        if (modifiers == null || modifiers.findFirstToken(TokenTypes.FINAL) != null) {
            return;
        }

        if (isScopeAndSurroundingScopePublic(variableDefinition)) {
            // Field is 'public' or 'protected', immutable classes cannot have public fields.
            log(variableDefinition, String.format(PUBLIC_FIELD_ERROR_TEMPLATE,
                variableDefinition.findFirstToken(TokenTypes.IDENT).getText()));
        }
    }

    private void checkForSetterMethod(DetailAST methodDefinition) {
        String methodName = methodDefinition.findFirstToken(TokenTypes.IDENT).getText();

        if (!isSetterMethod(methodName)) {
            return;
        }

        if (isScopeAndSurroundingScopePublic(methodDefinition)) {
            // Setter method is 'public' or 'protected', immutable classes cannot have public setters.
            log(methodDefinition, String.format(SETTER_METHOD_ERROR_TEMPLATE, methodName));
        }
    }

    private static boolean isSetterMethod(String methodName) {
        return methodName.startsWith("set") && methodName.length() >= 4 && Character.isUpperCase(methodName.charAt(3));
    }

    private static boolean isScopeAndSurroundingScopePublic(DetailAST detailAST) {
        Scope scope = ScopeUtil.getScope(detailAST);
        Scope surroundingScope = ScopeUtil.getSurroundingScope(detailAST);

        return (scope == Scope.PUBLIC || scope == Scope.PROTECTED)
            && (surroundingScope == Scope.PUBLIC || surroundingScope == Scope.PROTECTED);
    }
}
