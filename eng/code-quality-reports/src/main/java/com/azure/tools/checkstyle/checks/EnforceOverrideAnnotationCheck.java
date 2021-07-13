// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/*
 * Enforces overridden methods to be annotated with @Override.
 */
public class EnforceOverrideAnnotationCheck extends AbstractCheck {

    /**
     * Character separate package names in qualified name of java class.
     */
    private static final String PACKAGE_SEPARATOR = ".";

    /**
     * Full qualified name of the package.
     */
    private String packageName;

    /**
     * Full qualified name of the class.
     */
    private String className;

    /**
     * {@link Override Override} annotation name.
     */
    private static final String OVERRIDE = "Override";

    /**
     * Fully-qualified {@link Override Override} annotation name.
     */
    private static final String FQ_OVERRIDE = "java.lang." + OVERRIDE;

    private static final String ANNOTATION_MISSING_OVERRIDE = "Must include @java.lang.Override or @Override annotation";

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
        return new int[]{TokenTypes.PACKAGE_DEF, TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF};
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.PACKAGE_DEF:
                packageName = extractQualifiedName(token.getFirstChild().getNextSibling());
                break;
            case TokenTypes.CLASS_DEF:
                className = token.findFirstToken(TokenTypes.IDENT).getText();
                break;
            case TokenTypes.METHOD_DEF:
                String methodName = token.findFirstToken(TokenTypes.IDENT).getText();

                if (isMethodOverridden(methodName, getMethodParameters(token)) &&
                    !AnnotationUtil.containsAnnotation(token, OVERRIDE) &&
                    !AnnotationUtil.containsAnnotation(token, FQ_OVERRIDE)) {
                    log(token, ANNOTATION_MISSING_OVERRIDE);
                }
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Get name of class (with qualified package if specified) in {@code ast}.
     *
     * @param ast ast to extract class name from
     * @return qualified name
     */
    private static String extractQualifiedName(DetailAST ast) {
        return FullIdent.createFullIdent(ast).getText();
    }

    private Class<?>[] getMethodParameters(DetailAST token) {
        DetailAST parameters = token.findFirstToken(TokenTypes.PARAMETERS);
        DetailAST child = parameters.findFirstToken(TokenTypes.PARAMETER_DEF);
        List<Class<?>> classes = new ArrayList<>();
        while (child != null) {
            if (child.getType() == TokenTypes.PARAMETER_DEF) {
                classes.add(child.findFirstToken(TokenTypes.TYPE).getFirstChild().getClass());
            }
            child = child.getNextSibling();
        }
        Class<?>[] params = new Class<?>[classes.size()];
        return classes.toArray(params);
    }

    private boolean isMethodOverridden(String name, Class<?>... parameterTypes) {

        Class<?> klass;
        try {
            klass = Class.forName(packageName + PACKAGE_SEPARATOR + className);
        } catch (ClassNotFoundException e) {
            return false;
        }

        Method method = null;
        Class<?> declaringClass = null;
        try {
            method = klass.getMethod(name, parameterTypes);
            declaringClass = method.getDeclaringClass();
            if (declaringClass.equals(klass)) {
                return false;
            }
            declaringClass.getSuperclass().getMethod(method.getName(), method.getParameterTypes());
            return true;
        } catch (NoSuchMethodException e) {
            assert declaringClass != null;
            for (Class<?> iface : declaringClass.getInterfaces()) {
                try {
                    iface.getMethod(method.getName(), method.getParameterTypes());
                    return true;
                } catch (NoSuchMethodException ignored) {
                }
            }
            return false;
        }
    }
}
