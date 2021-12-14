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
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoImplInPublicAPI extends AbstractCheck {
    static final String PARAM_TYPE_ERROR =
        "\"%s\" is in an implementation package and should not be used as a parameter type in public API. "
            + "Alternatively, it can be removed from the implementation package and made public API, after "
            + "appropriate API review.";
    static final String RETURN_TYPE_ERROR =
        "\"%s\" is in an implementation package and should not be a return type for public API. "
            + "Alternatively, it can be removed from the implementation package and made public API, after "
            + "appropriate API review.";

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
        switch (ast.getType()) {
            case TokenTypes.PACKAGE_DEF:
                String packageName = FullIdent.createFullIdentBelow(ast).getText();
                inImplementationClass = packageName.contains("implementation");
                break;

            case TokenTypes.IMPORT:
                if (inImplementationClass) {
                    return;
                }

                String importClassPath = FullIdent.createFullIdentBelow(ast).getText();
                Matcher implementationMatch = IMPLEMENTATION_CLASS.matcher(importClassPath);
                if (implementationMatch.matches()) {
                    implementationClassSet.add(implementationMatch.group(1));
                }
                break;

            case TokenTypes.METHOD_DEF:
                if (inImplementationClass) {
                    return;
                }

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
