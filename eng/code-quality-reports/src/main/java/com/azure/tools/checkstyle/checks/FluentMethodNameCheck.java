package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Model Class Method:
 *  Fluent Methods: All methods that return an instance of the class, and with one parameter.
 *  The method name should not start with {@code avoidStartWord}.
 */
public class FluentMethodNameCheck extends AbstractCheck {

    private static final String FLUENT_METHOD_ERR = "Fluent Method name should not start with keyword %s.";

    // Specifies valid identifier: default start word is 'with'
    private String avoidStartWord = "with";

    private String className;

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
            TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void visitToken(DetailAST ast) {

        switch (ast.getType()) {
            case TokenTypes.CLASS_DEF:
                className = ast.findFirstToken(TokenTypes.IDENT).getText();
                break;
            case TokenTypes.METHOD_DEF:
                isMethodNameStartWith(ast);
                break;
        }
    }

    /**
     * @param ast METHOD_DEF AST node
     * @return
     */
    private void isMethodNameStartWith(DetailAST ast) {
        String methodType = ast.findFirstToken(TokenTypes.TYPE).getFirstChild().getText();

        if (methodType.equals(className)) {
            String methodName = ast.findFirstToken(TokenTypes.IDENT).getText();
            int paramtersCount = ast.findFirstToken(TokenTypes.PARAMETERS).getChildCount();
            if (methodName.contains(avoidStartWord) && methodName.substring(0, avoidStartWord.length()).equals(avoidStartWord)
                && paramtersCount == 1) {
                log(ast.getLineNo(), String.format(FLUENT_METHOD_ERR, avoidStartWord));
            }
        }
    }

    /**
     * Setter to specifies valid identifiers
     * @param word the starting string that should not start with in fluent method
     */
    public void setAvoidStartWord(String word) {
        this.avoidStartWord = word;
    }
}
