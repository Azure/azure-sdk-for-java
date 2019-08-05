// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Verify the whenever a field is assigned just once in constructor to be final
 * Tree traversal will pre-compute and fill 3 private containers:
 * nonFinalFields: keep an array of non private fields as tokens (to keep line number)
 * assignmentsFromConstructor: Save a set of string for each field name that gets its value assigned in constructor
 * assignmentsFromMethods: Save a set of strings for each field name that gets updated in any method
 *
 * On finish tree, check what non-final fields get a value only in constructor and nowhere else by looking for
 * strings inside nonFinalFields AND assignmentsFromConstructor but NOT in assignmentsFromMethods
 */
public class AssignedOnceVariableToBeFinalCheck extends AbstractCheck {
    private static final String ERROR_MSG = "Field \"%s\" is only assigned in constructor and it is not final. " +
        "Make field final";
    private static final Set<Integer> INVALID_FINAL_COMBINATION = new HashSet<>(Arrays.asList(
        TokenTypes.LITERAL_TRANSIENT,
        TokenTypes.LITERAL_VOLATILE
    ));

    private static ArrayList<DetailAST> nonFinalFields;
    private static Set<String> assignmentsFromConstructor;
    private static Set<String> assignmentsFromMethods;
    private static DetailAST scopeParent = null;

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
                scopeParent = token;
            case TokenTypes.CTOR_DEF:
                scopeParent = token;
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
            default:
                break;
        }
    }

    @Override
    public void finishTree(DetailAST token) {
        for (DetailAST field: nonFinalFields) {
            final String fieldName = field.findFirstToken(TokenTypes.IDENT).getText();
            if (assignmentsFromConstructor.contains(fieldName) && !assignmentsFromMethods.contains(fieldName)) {
                log(field, String.format(ERROR_MSG, fieldName));
            }
        }
    }

    private void checkAssignation(final DetailAST assignationToken) {
        if (scopeParent == null || assignationToken.getChildCount() == 0) {
            // not inside any method or constructor definition. No need to check anything
            // or this is an assignation from a notation like @Test(timeout = 5000) where assignation has not ChildCount
            return;
        }

        final DetailAST assignationParent = assignationToken.getParent();
        if (assignationParent != null && TokenTypes.VARIABLE_DEF == assignationParent.getType()) {
            // Assignation for a variable definition. No need to check this assignation
            return;
        }

        final HashSet<String> scopeParentParameterList = getParameterList(scopeParent.findFirstToken(
            TokenTypes.PARAMETERS));
        final int scopeParentType = scopeParent.getType();

        final DetailAST assignationWithDot = assignationToken.getFirstChild();
        DetailAST fieldToken = null;
        if (assignationWithDot != null && assignationWithDot.getType() == TokenTypes.DOT) {
            if (assignationWithDot.branchContains(TokenTypes.LITERAL_THIS)) {
                fieldToken = assignationWithDot.findFirstToken(TokenTypes.IDENT);
            }
        } else {
            final DetailAST variableNameToken = assignationToken.getFirstChild();
            // make sure the assignation is not for a method parameter
            if (!scopeParentParameterList.contains(variableNameToken.getText())) {
                fieldToken = variableNameToken;
            }
        }
        if (fieldToken != null) {
            final String fieldName = fieldToken.getText();

            if (scopeParentType == TokenTypes.METHOD_DEF && !assignmentsFromMethods.contains(fieldName)) {
                assignmentsFromMethods.add(fieldName);
            } else if (scopeParentType == TokenTypes.CTOR_DEF && !assignmentsFromConstructor.contains(fieldName)) {
                assignmentsFromConstructor.add(fieldName);
            }
        }
    }

    /**
     * Check if variable modifiers contains any of the illegal combination with final modifier
     * For instance, we don't want to combine transient or volatile with final
     *
     * @param modifiers a DetailAST pointing to a Variable list of modifiers
     * @return true if there is any modifier that shouldn't be combined with final
     */
    private boolean hasIllegalCombination(DetailAST modifiers) {
        for (DetailAST modifier = modifiers.getFirstChild(); modifier != null; modifier = modifier.getNextSibling()) {
            if (INVALID_FINAL_COMBINATION.contains(modifier.getType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check each non-final field definition from a class and fill nonFinalFields
     *
     * @param classDefinitionAST a class definition AST
     */
    private void fillClassFieldDefinitions(DetailAST classDefinitionAST) {
        final DetailAST classObjBlockAst = classDefinitionAST.findFirstToken(TokenTypes.OBJBLOCK);
        for (DetailAST astChild = classObjBlockAst.getFirstChild(); astChild != null;
             astChild = astChild.getNextSibling()) {
            if (TokenTypes.VARIABLE_DEF == astChild.getType()) {
                final DetailAST variableModifiersAst = astChild.findFirstToken(TokenTypes.MODIFIERS);
                if (!variableModifiersAst.branchContains(TokenTypes.FINAL)
                    && !hasIllegalCombination(variableModifiersAst)) {
                    nonFinalFields.add(astChild);
                }
            }
        }
    }

    /**
     * Get a node AST with parameters definition and return the list of all parameter names
     *
     * @param parametersAST a TokenTypes.PARAMETERS
     * @return a set of parameter names
     */
    private HashSet<String> getParameterList (DetailAST parametersAST) {
        final HashSet<String> parameterList = new HashSet<>();
        for (DetailAST parameter = parametersAST.findFirstToken(TokenTypes.PARAMETER_DEF); parameter != null;
             parameter = parameter.getNextSibling()) {
            if (parameter.getType() == TokenTypes.PARAMETER_DEF) {
                final String parameterName = parameter.findFirstToken(TokenTypes.IDENT).getText();
                parameterList.add(parameterName);
            }
        }
        return parameterList;
    }
}
