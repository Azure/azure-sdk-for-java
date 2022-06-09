// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.Scope;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Verifies that:
 * 1) no return classes are from the implementation package
 * 2) no class of implementation package as method's parameters
 */
public class NoImplInPublicAPI extends AbstractCheck {
    private static final String ALTERNATIVE_MOVE_TO_PUBLIC_API = "Alternatively, it can be removed from the "
        + "implementation package and made public API, after appropriate API review.";
    static final String TYPE_PARAM_TYPE_ERROR = "\"%s\" is in an implementation package and should not be used as a "
        + "type parameter type in public API. " + ALTERNATIVE_MOVE_TO_PUBLIC_API;
    static final String IMPLEMENTS_TYPE_ERROR = "\"%s\" is in an implementation package and should not be implemented "
        + "by a type in public API. " + ALTERNATIVE_MOVE_TO_PUBLIC_API;
    static final String EXTENDS_TYPE_ERROR = "\"%s\" is in an implementation package and should not be extended by a "
        + "type in public API. " + ALTERNATIVE_MOVE_TO_PUBLIC_API;
    static final String PARAM_TYPE_ERROR = "\"%s\" is in an implementation package and should not be used as a "
        + "parameter type in public API. " + ALTERNATIVE_MOVE_TO_PUBLIC_API;
    static final String RETURN_TYPE_ERROR = "\"%s\" is in an implementation package and should not be a return type "
        + "for public API. " + ALTERNATIVE_MOVE_TO_PUBLIC_API;

    // Pattern that matches either an import statement or a fully-qualified type reference for being implementation.
    private static final Pattern IMPLEMENTATION_CLASS = Pattern.compile("com\\.azure.*?\\.implementation.*?\\.(\\w+)");

    private Set<String> implementationClassSet = new HashSet<>();

    // Flag that indicates if the current definition is contained in an implementation package.
    // Definitions contained in implementation can be ignored as implementation doesn't have public API.
    private boolean inImplementationClass = false;

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
            TokenTypes.PACKAGE_DEF,
            TokenTypes.IMPORT,
            TokenTypes.CLASS_DEF,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.ENUM_DEF,
            TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void beginTree(DetailAST root) {
        this.implementationClassSet.clear();
        this.inImplementationClass = false;
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (inImplementationClass) {
            return;
        }

        switch (ast.getType()) {
            case TokenTypes.PACKAGE_DEF:
                String packageName = FullIdent.createFullIdent(ast.findFirstToken(TokenTypes.DOT)).getText();
                inImplementationClass = packageName.contains("implementation");
                break;

            case TokenTypes.IMPORT:
                String importClassPath = FullIdent.createFullIdentBelow(ast).getText();
                Matcher implementationMatch = IMPLEMENTATION_CLASS.matcher(importClassPath);
                if (implementationMatch.matches()) {
                    implementationClassSet.add(implementationMatch.group(1));
                }
                break;

            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
                if (isNonPublicDefinition(ast)) {
                    return;
                }

                // Check the type parameters for being implementation.
                CheckUtil.getTypeParameters(ast).forEach(this::checkIfTypeParameterImplementationType);

                // Check the extends and implements for being implementation.
                checkExtendsAndImplements(ast);

                break;

            case TokenTypes.ENUM_DEF:
                if (isNonPublicDefinition(ast)) {
                    return;
                }

                // Check the implements for being implementation.
                checkExtendsAndImplements(ast);

                break;

            case TokenTypes.METHOD_DEF:
                // Static initializers aren't part of public API.
                if (isInStaticInitializer(ast)) {
                    return;
                }

                // If the method isn't contained in a public or protected scope skip it.
                Scope surroundingScope = ScopeUtil.getSurroundingScope(ast);
                if (surroundingScope != Scope.PUBLIC && surroundingScope != Scope.PROTECTED) {
                    return;
                }

                Scope methodScope = ScopeUtil.getScopeFromMods(ast.findFirstToken(TokenTypes.MODIFIERS));
                if (methodScope == Scope.PUBLIC || methodScope == Scope.PROTECTED) {
                    DetailAST typeAST = ast.findFirstToken(TokenTypes.TYPE);
                    String returnType = FullIdent.createFullIdentBelow(typeAST).getText();
                    if (isImplementationType(returnType, implementationClassSet)) {
                        log(typeAST, String.format(RETURN_TYPE_ERROR, returnType));
                    }


                    DetailAST paramAST = ast.findFirstToken(TokenTypes.PARAMETERS);

                    TokenUtil.forEachChild(paramAST, TokenTypes.PARAMETER_DEF, paramDefAst -> {
                        DetailAST paramTypeAST = paramDefAst.findFirstToken(TokenTypes.TYPE);
                        String paramType = FullIdent.createFullIdentBelow(paramTypeAST).getText();
                        if (isImplementationType(paramType, implementationClassSet)) {
                            log(paramTypeAST, String.format(PARAM_TYPE_ERROR, paramType));
                        }
                    });
                }
                break;

            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    private static boolean isNonPublicDefinition(DetailAST definitionAst) {
        Scope definitionScope = ScopeUtil.getScope(definitionAst);

        // If the current definition scope isn't public or protected this isn't a public definition.
        if (definitionScope != Scope.PUBLIC && definitionScope != Scope.PROTECTED) {
            return true;
        }

        // If the definition is public then check if it is contained in another public definition.
        Scope containingScope = ScopeUtil.getSurroundingScope(definitionAst);

        // The surrounding scope will be null if the definition is the outermost definition in a Java file.
        // Otherwise, if it isn't null check that it is public or protected.
        return containingScope != null && containingScope != Scope.PUBLIC && containingScope != Scope.PROTECTED;
    }

    private void checkIfTypeParameterImplementationType(DetailAST typeParameterAst) {
        // Type parameters come in three flavors:
        //
        // Basic - Set<String> or Set<java.lang.String>
        // Upper Bound - Set<? extends String> or Set<A extends java.lang.String>
        // Lower Bound - Set<? super String> or Set<A super java.lang.String>
        //
        // Each has its own handling to validate it doesn't expose implementation.
        TokenUtil.findFirstTokenByPredicate(typeParameterAst, ast -> ast.getType() == TokenTypes.TYPE_UPPER_BOUNDS)
            .ifPresent(upperBoundsAst -> checkAndLogTypeParam(typeParameterAst, upperBoundsAst));

        TokenUtil.findFirstTokenByPredicate(typeParameterAst, ast -> ast.getType() == TokenTypes.TYPE_LOWER_BOUNDS)
            .ifPresent(lowerBoundsAst -> checkAndLogTypeParam(typeParameterAst, lowerBoundsAst));

        checkAndLogTypeParam(typeParameterAst, typeParameterAst);
    }

    private void checkAndLogTypeParam(DetailAST typeParameterAst, DetailAST astToCheck) {
        String type = FullIdent.createFullIdentBelow(astToCheck).getText();
        if (isImplementationType(type, implementationClassSet)) {
            log(typeParameterAst, String.format(TYPE_PARAM_TYPE_ERROR, type));
        }
    }

    private void checkExtendsAndImplements(DetailAST definitionAst) {
        TokenUtil.findFirstTokenByPredicate(definitionAst, ast -> ast.getType() == TokenTypes.EXTENDS_CLAUSE)
            .ifPresent(extendsAst -> checkAndLogExtendsOrImplements(extendsAst, true));

        TokenUtil.findFirstTokenByPredicate(definitionAst, ast -> ast.getType() == TokenTypes.IMPLEMENTS_CLAUSE)
            .ifPresent(implementsAst -> checkAndLogExtendsOrImplements(implementsAst, false));
    }

    private void checkAndLogExtendsOrImplements(DetailAST extendsOrImplements, boolean isExtends) {
        TokenUtil.forEachChild(extendsOrImplements, TokenTypes.IDENT, extendsOrImplementsType -> {
            String type = FullIdent.createFullIdent(extendsOrImplementsType).getText();
            if (isImplementationType(type, implementationClassSet)) {
                log(extendsOrImplementsType, String.format(isExtends ? EXTENDS_TYPE_ERROR : IMPLEMENTS_TYPE_ERROR,
                    type));
            }
        });
    }

    private static boolean isInStaticInitializer(DetailAST ast) {
        for (DetailAST token = ast.getParent(); token != null; token = token.getParent()) {
            if (TokenUtil.isOfType(token, TokenTypes.STATIC_INIT)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isImplementationType(String type, Set<String> implementationImports) {
        // Check if the type is contained in the known implementation class imports.
        if (implementationImports.contains(type)) {
            return true;
        }

        // If it isn't contained in the known imports check if it is a fully-qualified import.
        return IMPLEMENTATION_CLASS.matcher(type).matches();
    }
}
