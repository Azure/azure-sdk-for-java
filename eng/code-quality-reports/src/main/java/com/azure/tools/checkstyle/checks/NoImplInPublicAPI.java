package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
import java.util.Set;

public class NoImplInPublicAPI extends AbstractCheck {

    private static final String COM_AZURE = "com.azure";
    private static final String IMPLEMENTATION_PATH = "com.azure.core.implementation";
    private static final String PARAM_TYPE_ERROR = "\"%s\" class is in the implementation package, and it should not used as parameter type in public or protected API.";
    private static final String RETURN_TYPE_ERROR = "\"%s\" class is in the implementation package, and it should not be a return type.";

    private static boolean isTrackTwo;
    private static boolean hasImplementationPath;
    private Set<String> implementationClassSet = new HashSet<>();

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
            TokenTypes.PACKAGE_DEF,
            TokenTypes.IMPORT,
            TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void beginTree(DetailAST root) {
        this.implementationClassSet.clear();
        this.isTrackTwo = false;
        this.hasImplementationPath = false;
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getType() != TokenTypes.PACKAGE_DEF && !this.isTrackTwo) {
            return;
        }
        switch (ast.getType()) {
            case TokenTypes.PACKAGE_DEF:
                String packageName = FullIdent.createFullIdent(ast.findFirstToken(TokenTypes.DOT)).getText();
                this.isTrackTwo = packageName.startsWith(COM_AZURE);
                break;
            case TokenTypes.IMPORT:
                String importClassPath = FullIdent.createFullIdentBelow(ast).getText();
                if (importClassPath.startsWith(IMPLEMENTATION_PATH)) {
                    this.hasImplementationPath = true;
                    String remainingPath = importClassPath.substring(IMPLEMENTATION_PATH.length());
                    if (remainingPath.length() > 1) {
                        String className = remainingPath.substring(remainingPath.lastIndexOf(".") + 1);
                        implementationClassSet.add(className);
                    }
                }
                break;
            case TokenTypes.METHOD_DEF:
                if (!this.hasImplementationPath) {
                    return;
                }
                DetailAST modifiersAST = ast.findFirstToken(TokenTypes.MODIFIERS);
                if (modifiersAST.branchContains(TokenTypes.LITERAL_PUBLIC)
                    || modifiersAST.branchContains(TokenTypes.LITERAL_PROTECTED)) {
                    DetailAST typeAST = ast.findFirstToken(TokenTypes.TYPE);
                    String returnType = typeAST.getFirstChild().getText();
                    if (implementationClassSet.contains(returnType)) {
                        log(typeAST, String.format(RETURN_TYPE_ERROR, returnType));
                    }

                    DetailAST paramAST = ast.findFirstToken(TokenTypes.PARAMETERS);

                    for (DetailAST curr = paramAST.getFirstChild(); curr != null; curr = curr.getNextSibling()) {
                        if (curr.getType() == TokenTypes.PARAMETER_DEF) {
                            DetailAST paramTypeAST = curr.findFirstToken(TokenTypes.TYPE);
                            String paramType = paramTypeAST.getFirstChild().getText();
                            if (implementationClassSet.contains(paramType)) {
                                log(paramTypeAST, String.format(PARAM_TYPE_ERROR, paramType));
                            }
                        }
                    }
                }
                break;
        }

    }


}


