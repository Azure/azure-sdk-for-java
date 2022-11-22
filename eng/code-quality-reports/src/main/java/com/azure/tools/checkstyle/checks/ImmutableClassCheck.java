// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.Scope;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;

/**
 * Verify the classes with annotation {@code @Immutable} should have the following rules:
 * <ol>
 *   <li>No public or protected fields</li>
 *   <li>No public or protected setter methods</li>
 * </ol>
 */
public class ImmutableClassCheck extends AbstractCheck {
    private static final String IMMUTABLE_NOTATION = "Immutable";

    static final String PUBLIC_FIELD_ERROR_TEMPLATE =
        "Classes annotated with @Immutable cannot have non-final public or protect fields. "
            + "Found non-final public field: %s.";
    static final String SETTER_METHOD_ERROR_TEMPLATE =
        "Classes annotated with @Immutable cannot have public or protected setter methods. "
            + "Found public setter method: %s.";

    private boolean hasImmutableAnnotation;

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
        return new int[]{
            TokenTypes.CLASS_DEF,
            TokenTypes.VARIABLE_DEF,
            TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void beginTree(DetailAST root) {
        hasImmutableAnnotation = false;
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                hasImmutableAnnotation = hasImmutableAnnotation(token);
                break;

            case TokenTypes.VARIABLE_DEF:
                if (hasImmutableAnnotation) {
                    checkForPublicField(token);
                }
                break;

            case TokenTypes.METHOD_DEF:
                if (hasImmutableAnnotation) {
                    checkForSetterMethod(token);
                }
                break;

            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /*
     * Checks if the class is annotated with annotation {@literal @Immutable}. A class could have multiple annotations.
     *
     * @param classDefToken the CLASS_DEF AST node
     * @return true if the class is annotated with {@literal @Immutable}, false otherwise.
     */
    private static boolean hasImmutableAnnotation(DetailAST classDefinition) {
        DetailAST immutableAnnotation = AnnotationUtil.getAnnotation(classDefinition, IMMUTABLE_NOTATION);
        return immutableAnnotation != null;
    }

    private void checkForPublicField(DetailAST variableDefinition) {
        DetailAST modifiers = variableDefinition.findFirstToken(TokenTypes.MODIFIERS);

        // Field has no modifiers or is final, therefore it's immutable for all intents and purposes.
        if (modifiers == null || modifiers.findFirstToken(TokenTypes.FINAL) != null) {
            return;
        }

        Scope scope = ScopeUtil.getScopeFromMods(modifiers);
        if (scope == Scope.PUBLIC || scope == Scope.PROTECTED) {
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

        Scope scope = ScopeUtil.getScope(methodDefinition);
        if (scope == Scope.PUBLIC || scope == Scope.PROTECTED) {
            // Setter method is 'public' or 'protected', immutable classes cannot have public setters.
            log(methodDefinition, String.format(SETTER_METHOD_ERROR_TEMPLATE, methodName));
        }
    }

    private static boolean isSetterMethod(String methodName) {
        return methodName.startsWith("set") && methodName.length() >= 4 && Character.isUpperCase(methodName.charAt(3));
    }
}
