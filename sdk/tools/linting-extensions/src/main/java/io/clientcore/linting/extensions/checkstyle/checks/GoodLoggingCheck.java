// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

/**
 * Checks that the logging practices in the code follow the good logging practices.
 * <p>
 * Good Logging Practice:
 * <ol>
 * <li>A non-static instance logger in a non-static method.</li>
 * <li>ClientLogger in public API should all named 'logger', public API classes are those classes that are declared
 *     as public and that do not exist in an implementation package or subpackage.</li>
 * <li>Should not use any external logger class, only use ClientLogger. No SLF4J, log4j, or other logging imports are
 *     allowed.</li>
 * <li>'System.out' and 'System.err' is not allowed as well.</li>
 * </ol>
 * <p>
 * {@code fullyQualifiedLoggerName} (required): The fully-qualified class name of the logger type. Defined in the
 * checkstyle.xml config file. If left empty, an exception will be thrown by the check.
 * <p>
 * {@code simpleClassName} (required): The simple class name of the logger type. Defined in the checkstyle.xml config
 * file. If left empty, an exception will be thrown by the check.
 * <p>
 * {@code loggerName} (required): The case-insensitive name of the logger instance. Defined in the checkstyle.xml config
 * file. If left empty, an exception will be thrown by the check.
 */
public class GoodLoggingCheck extends AbstractCheck {
    private static final int[] REQUIRED_TOKENS = new int[] {
        TokenTypes.IMPORT,
        TokenTypes.INTERFACE_DEF,
        TokenTypes.ENUM_DEF,
        TokenTypes.CLASS_DEF,
        TokenTypes.LITERAL_NEW,
        TokenTypes.VARIABLE_DEF,
        TokenTypes.METHOD_CALL };

    static final String LOGGER_NAME_ERROR
        = "ClientLogger instance naming: use \"%s\" instead of \"%s\" for consistency.";
    static final String NOT_CLIENT_LOGGER_ERROR
        = "Do not use %s class. Use \"%s\" as a logging mechanism instead of \"%s\".";
    static final String LOGGER_NAME_MISMATCH_ERROR
        = "Not newing a ClientLogger with matching class name. Use \"%s.class\" " + "instead of \"%s\".";

    // Boolean indicator that indicates if the java class imports ClientLogger
    private boolean hasClientLoggerImported;
    // A LIFO queue stores the class names, pop top element if exist the class name AST node
    private final Queue<String> classNameDeque = Collections.asLifoQueue(new ArrayDeque<>());
    // Collection of Invalid logging packages
    private static final String[] INVALID_LOGS
        = new String[] { "org.slf4j", "org.apache.logging.log4j", "java.util.logging" };

    private String fullyQualifiedLoggerName;
    private String simpleClassName;
    private String loggerName;

    /**
     * Creates a new instance of {@link GoodLoggingCheck}.
     */
    public GoodLoggingCheck() {
    }

    /**
     * Sets the fully qualified logger name.
     *
     * @param fullyQualifiedLoggerName the fully qualified logger name.
     * @throws IllegalArgumentException if the fully qualified logger name is null or empty.
     */
    public final void setFullyQualifiedLoggerName(String fullyQualifiedLoggerName) {
        if (fullyQualifiedLoggerName == null || fullyQualifiedLoggerName.isEmpty()) {
            throw new IllegalArgumentException("fullyQualifiedLoggerName cannot be null or empty.");
        }

        this.fullyQualifiedLoggerName = fullyQualifiedLoggerName;
    }

    /**
     * Sets the simple class name for the logger.
     *
     * @param simpleClassName the simple class name for the logger.
     * @throws IllegalArgumentException if the simple class name is null or empty.
     */
    public final void setSimpleClassName(String simpleClassName) {
        if (simpleClassName == null || simpleClassName.isEmpty()) {
            throw new IllegalArgumentException("simpleClassName cannot be null or empty.");
        }

        this.simpleClassName = simpleClassName;
    }

    /**
     * Sets the case-insensitive name of the logger instance.
     *
     * @param loggerName the case-insensitive name of the logger instance.
     * @throws IllegalArgumentException if the logger name is null or empty.
     */
    public final void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
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
        return REQUIRED_TOKENS;
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        List<String> missingConfig = new ArrayList<>(3);

        if (fullyQualifiedLoggerName == null || fullyQualifiedLoggerName.isEmpty()) {
            missingConfig.add("fullyQualifiedLoggerName");
        }
        if (simpleClassName == null || simpleClassName.isEmpty()) {
            missingConfig.add("simpleClassName");
        }
        if (loggerName == null || loggerName.isEmpty()) {
            missingConfig.add("loggerName");
        }

        if (!missingConfig.isEmpty()) {
            throw new IllegalArgumentException("GoodLoggingCheck configuration error, missing the following "
                + "configurations: " + String.join(", ", missingConfig));
        }
    }

    @Override
    public void finishTree(DetailAST ast) {
        hasClientLoggerImported = false;
    }

    @Override
    public void leaveToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.CLASS_DEF
            || ast.getType() == TokenTypes.INTERFACE_DEF
            || ast.getType() == TokenTypes.ENUM_DEF) {
            classNameDeque.poll();
        }
    }

    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.IMPORT:
                final String importClassPath = FullIdent.createFullIdentBelow(ast).getText();
                hasClientLoggerImported = hasClientLoggerImported || importClassPath.equals(fullyQualifiedLoggerName);

                for (String invalidLog : INVALID_LOGS) {
                    if (importClassPath.startsWith(invalidLog)) {
                        log(ast, String.format(NOT_CLIENT_LOGGER_ERROR, "external logger", fullyQualifiedLoggerName,
                            invalidLog));
                    }
                }
                break;

            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
            case TokenTypes.ENUM_DEF:
                classNameDeque.offer(ast.findFirstToken(TokenTypes.IDENT).getText());
                break;

            case TokenTypes.LITERAL_NEW:
                checkLoggerInstantiation(ast);
                break;

            case TokenTypes.VARIABLE_DEF:
                checkLoggerNameMatch(ast);
                break;

            case TokenTypes.METHOD_CALL:
                final DetailAST dotToken = ast.findFirstToken(TokenTypes.DOT);
                if (dotToken == null) {
                    return;
                }
                final String methodCallName = FullIdent.createFullIdentBelow(dotToken).getText();
                if (methodCallName.startsWith("System.out") || methodCallName.startsWith("System.err")) {
                    log(ast, String.format(NOT_CLIENT_LOGGER_ERROR, "Java System", fullyQualifiedLoggerName,
                        methodCallName));
                }
                break;

            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Check if the VARIABLE_DEF AST node type is 'ClientLogger'.
     *
     * @param varDefAST VARIABLE_DEF AST node
     * @return true if the variable type is 'ClientLogger'.
     */
    private boolean isTypeClientLogger(DetailAST varDefAST) {
        final DetailAST typeAST = varDefAST.findFirstToken(TokenTypes.TYPE);
        if (typeAST == null) {
            return false;
        }
        return TokenUtil
            .findFirstTokenByPredicate(typeAST,
                node -> node.getType() == TokenTypes.IDENT && node.getText().equals(simpleClassName))
            .isPresent();
    }

    /**
     * Check if instantiating a matched class name for the same class.
     *
     * @param literalNewToken LITERAL_NEW node
     */
    private void checkLoggerInstantiation(DetailAST literalNewToken) {
        final DetailAST identToken = literalNewToken.findFirstToken(TokenTypes.IDENT);
        // Not ClientLogger instance
        if (identToken == null || !identToken.getText().equals(simpleClassName)) {
            return;
        }
        // LITERAL_NEW node always has ELIST node below
        TokenUtil.findFirstTokenByPredicate(literalNewToken.findFirstToken(TokenTypes.ELIST), exprToken -> {
            // Skip check if not EXPR node or if has no DOT node below. EXPR always has children below
            if (exprToken.getType() != TokenTypes.EXPR || exprToken.getFirstChild().getType() != TokenTypes.DOT) {
                return false;
            }
            // Check instantiation of ClientLogger
            final String containerClassName = FullIdent.createFullIdent(exprToken.getFirstChild()).getText();
            // Add suffix of '.class' at the end of class name
            final String className = classNameDeque.peek();
            if (!Objects.equals(className + ".class", containerClassName)) {
                log(exprToken, String.format(LOGGER_NAME_MISMATCH_ERROR, className, containerClassName));
            }
            return true;
        });
    }

    /**
     * Check if the given ClientLogger named 'logger'
     *
     * @param varToken VARIABLE_DEF node
     */
    private void checkLoggerNameMatch(DetailAST varToken) {
        if (!hasClientLoggerImported || !isTypeClientLogger(varToken)) {
            return;
        }
        // Check if the Logger instance named as 'logger/LOGGER'.
        final DetailAST identAST = varToken.findFirstToken(TokenTypes.IDENT);
        if (identAST != null && !identAST.getText().equalsIgnoreCase(loggerName)) {
            log(varToken, String.format(LOGGER_NAME_ERROR, loggerName, identAST.getText()));
        }
    }
}
