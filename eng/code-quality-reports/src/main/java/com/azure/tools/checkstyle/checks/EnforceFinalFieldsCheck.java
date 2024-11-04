// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Verify the whenever a field is assigned just once in constructor to be final
 * Tree traversal will pre-compute and fill 3 private containers:
 * nonFinalFields: keep an array of non private fields as tokens (to keep line number)
 * assignmentsFromConstructor: Save a set of string for each field name that gets its value assigned in constructor
 * assignmentsFromMethods: Save a set of strings for each field name that gets updated in any method
 * <p>
 * On finish tree, check what non-final fields get a value only in constructor and nowhere else by looking for
 * strings inside nonFinalFields AND assignmentsFromConstructor but NOT in assignmentsFromMethods
 */
public class EnforceFinalFieldsCheck extends AbstractCheck {
    private static final String ERROR_SUGGESTION = "You should consider making the field final, "
        + "or suppressing the warning.";
    private static final String ERROR_MSG = "Field \"%s\" is only assigned in constructor and it is not final. "
        + ERROR_SUGGESTION;
    private static final String ERROR_FIELD_ALONE = "Field \"%s\" is not assigned in constructor or methods."
        + ERROR_SUGGESTION;

    private List<DetailAST> nonFinalFields;
    private Set<String> assignmentsFromConstructor;
    private Set<String> assignmentsFromMethods;
    private DetailAST scopeParent = null;
    private Set<String> currentScopeParameterSet = null;
    private Map<String, DetailAST> variablesInScope = null;
    private String currentClassName = null;

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
        return new int[] {
            TokenTypes.CLASS_DEF,
            TokenTypes.ASSIGN,
            TokenTypes.PLUS_ASSIGN,
            TokenTypes.BAND_ASSIGN,
            TokenTypes.BOR_ASSIGN,
            TokenTypes.BSR_ASSIGN,
            TokenTypes.BXOR_ASSIGN,
            TokenTypes.DIV_ASSIGN,
            TokenTypes.MINUS_ASSIGN,
            TokenTypes.MOD_ASSIGN,
            TokenTypes.SL_ASSIGN,
            TokenTypes.SR_ASSIGN,
            TokenTypes.STAR_ASSIGN,
            TokenTypes.INC,
            TokenTypes.POST_INC,
            TokenTypes.DEC,
            TokenTypes.POST_DEC,
            TokenTypes.METHOD_DEF,
            TokenTypes.CTOR_DEF,
        };
    }

    @Override
    public void beginTree(DetailAST root) {
        nonFinalFields = new ArrayList<>();
        assignmentsFromConstructor = new HashSet<>();
        assignmentsFromMethods = new HashSet<>();
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                this.currentClassName = token.findFirstToken(TokenTypes.IDENT).getText();
                fillClassFieldDefinitions(token);
                break;
            case TokenTypes.ASSIGN:
            case TokenTypes.PLUS_ASSIGN:
            case TokenTypes.BAND_ASSIGN:
            case TokenTypes.BOR_ASSIGN:
            case TokenTypes.BSR_ASSIGN:
            case TokenTypes.BXOR_ASSIGN:
            case TokenTypes.DIV_ASSIGN:
            case TokenTypes.MINUS_ASSIGN:
            case TokenTypes.MOD_ASSIGN:
            case TokenTypes.SL_ASSIGN:
            case TokenTypes.SR_ASSIGN:
            case TokenTypes.STAR_ASSIGN:
            case TokenTypes.INC:
            case TokenTypes.POST_INC:
            case TokenTypes.DEC:
            case TokenTypes.POST_DEC:
                checkAssignation(token);
                break;
            case TokenTypes.METHOD_DEF:
            case TokenTypes.CTOR_DEF:
                scopeParent = token;
                variablesInScope = new HashMap<>();
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    @Override
    public void leaveToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.METHOD_DEF:
            case TokenTypes.CTOR_DEF:
                scopeParent = null;
                currentScopeParameterSet = null;
                variablesInScope = null;
                break;
            default:
                break;
        }
    }

    @Override
    public void finishTree(DetailAST token) {
        for (DetailAST field : nonFinalFields) {
            final String fieldName = field.findFirstToken(TokenTypes.IDENT).getText();
            if (assignmentsFromConstructor.contains(fieldName) && !assignmentsFromMethods.contains(fieldName)) {
                log(field, String.format(ERROR_MSG, fieldName));
            } else if (field.branchContains(TokenTypes.ASSIGN)
                && !assignmentsFromConstructor.contains(fieldName)
                && !assignmentsFromMethods.contains(fieldName)) {
                log(field, String.format(ERROR_FIELD_ALONE, fieldName));
            }
        }
    }

    /*
     * Get the field token from an assignation token.
     * This method handles cases for fields referenced as `this.field` or only `field`
     * It will get parameters from the method definition to ignore assignations to those parameters
     */
    private DetailAST getAssignedField(final DetailAST assignationToken) {
        final Set<String> scopeParentParameterSet = getParameterSet(scopeParent.findFirstToken(
            TokenTypes.PARAMETERS));
        final DetailAST firstChild = assignationToken.getFirstChild();
        final DetailAST assignationWithDot = firstChild.getType() == TokenTypes.DOT ? firstChild : null;

        if (assignationWithDot != null) {
            if (assignationWithDot.branchContains(TokenTypes.LITERAL_THIS)) {
                return assignationWithDot.findFirstToken(TokenTypes.IDENT);
            } else if (TokenUtil.findFirstTokenByPredicate(assignationWithDot,
                token -> token.getText().equals(this.currentClassName)).isPresent()) {
                // Case when referencing same class for private static fields
                return assignationWithDot.getLastChild();
            } else if (assignationWithDot.getFirstChild().getType() == TokenTypes.IDENT) {
                // Case where setting a field on a variable.
                String variableNameToken = assignationWithDot.getFirstChild().getText();
                DetailAST variableDeclaration = variablesInScope.get(variableNameToken);
                DetailAST parentScope = getParentScope(assignationToken);
                if (variableDeclaration != null && parentScope != null
                    && CheckUtil.isBeforeInSource(variableDeclaration, parentScope)) {
                    return assignationWithDot.getLastChild();
                }
            }
        } else {
            final DetailAST variableNameToken = assignationToken.getFirstChild();
            // make sure the assignation is not for a method parameter
            if (!scopeParentParameterSet.contains(variableNameToken.getText())) {
                return variableNameToken;
            }
        }

        return null;
    }

    private static DetailAST getParentScope(DetailAST ast) {
        DetailAST parent = ast.getParent();
        do {
            if (parent.getType() == TokenTypes.SLIST) {
                return parent;
            }
        } while ((parent = parent.getParent()) != null);

        return null;
    }

    /*
     * Saves a field name to a container depending on the provided type
     */
    private void saveField(final String fieldName, final int scopeParentType) {
        if (scopeParentType == TokenTypes.METHOD_DEF) {
            assignmentsFromMethods.add(fieldName);
        } else if (scopeParentType == TokenTypes.CTOR_DEF) {
            assignmentsFromConstructor.add(fieldName);
        }
    }

    /*
     * Review an assignation to save fields that gets assigned in constructor or in any method
     *
     * @param assignationToken an assignation token
     */
    private void checkAssignation(final DetailAST assignationToken) {
        if (scopeParent == null || assignationToken.getChildCount() == 0) {
            // not inside any method or constructor definition. No need to check anything
            // or this is an assignation from a notation like @Test(timeout = 5000) where assignation has not ChildCount
            return;
        }

        final DetailAST assignationParent = assignationToken.getParent();
        if (assignationParent != null && TokenTypes.VARIABLE_DEF == assignationParent.getType()) {
            String variableType = FullIdent.createFullIdentBelow(assignationParent.findFirstToken(TokenTypes.TYPE)).getText();
            if (Objects.equals(currentClassName, variableType)) {
                // Track variable definitions of the class we're currently in.
                variablesInScope.put(assignationParent.findFirstToken(TokenTypes.IDENT).getText(), assignationParent);
            }

            return;
        }

        DetailAST fieldToken = getAssignedField(assignationToken);

        if (fieldToken != null) {
            saveField(fieldToken.getText(), scopeParent.getType());
        }
    }


    /*
     * Check each non-final field definition from a class and fill nonFinalFields
     *
     * @param classDefinitionAST a class definition AST
     */
    private void fillClassFieldDefinitions(DetailAST classDefinitionAST) {
        final DetailAST classObjBlockAst = classDefinitionAST.findFirstToken(TokenTypes.OBJBLOCK);

        TokenUtil.forEachChild(classObjBlockAst, TokenTypes.VARIABLE_DEF, (definitionToken) -> {
            final DetailAST variableModifiersAst = definitionToken.findFirstToken(TokenTypes.MODIFIERS);
            if (!variableModifiersAst.branchContains(TokenTypes.FINAL)
                && !Utils.hasIllegalCombination(variableModifiersAst)) {
                nonFinalFields.add(definitionToken);
            }
        });
    }

    /*
     * Get a node AST with parameters definition and return the list of all parameter names
     * The set of parameters is created the first time an assignation is check within a method or constructor
     * and we don't need to generate it again until visiting a different method or constructor.
     * Field `currentScopeParameterSet` ensures we don't create the set multiple times for the same method/constructor
     *
     * @param parametersAST a TokenTypes.PARAMETERS
     * @return a set of parameter names
     */
    private Set<String> getParameterSet(DetailAST parametersAST) {
        if (currentScopeParameterSet != null) {
            return currentScopeParameterSet;
        }
        currentScopeParameterSet = new HashSet<>();
        TokenUtil.forEachChild(parametersAST, TokenTypes.PARAMETER_DEF, (paramDefToken) -> {
            final String parameterName = paramDefToken.findFirstToken(TokenTypes.IDENT).getText();
            currentScopeParameterSet.add(parameterName);
        });

        return currentScopeParameterSet;
    }
}
