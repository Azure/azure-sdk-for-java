// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Checks exception logging, throwing of created logging events, and preservation of caught exception causes.
 * The {@code loggingApi} property selects {@code clientcore} (the default) or {@code azure-core} logging policies.
 * Individual validations can be configured separately; all violations are reported under this rule's name.
 */
public class ExceptionHandlingCheck extends SdkCheck {
    static final String CREATED_EXCEPTION_NOT_THROWN_MESSAGE = "An exception is created and logged, but not thrown. "
        + "Ensure the exception is either thrown or not created at all. See "
        + "https://github.com/Azure/azure-sdk-for-java/blob/main/docs/azure-v2.md#logging-best-practices for more details.";
    static final String RAW_EXCEPTION_ERROR = "Directly throwing a new exception is disallowed. Use the "
        + "\"io.clientcore.core.instrumentation.logging.ClientLogger\" API instead, such as "
        + "\"logger.throwableAtError()\" or \"logger.throwableAtWarning()\". See "
        + "https://github.com/Azure/azure-sdk-for-java/blob/main/docs/azure-v2.md#logging-best-practices for more details.";
    static final String UNUSED_CAUGHT_EXCEPTION_ERROR = "Caught and thrown exceptions should include the caught "
        + "exception as the cause in the newly thrown exception. Dropping the causal exception makes it more difficult "
        + "to troubleshoot issues when they arise. Include the caught exception variable %s as the cause.";
    static final String THROW_LOGGER_EXCEPTION_MESSAGE = String.format("Directly throwing an exception is disallowed. "
        + "Must throw through \"ClientLogger\" API, either of \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", or \"%s\" where "
        + "\"logger\" is type of ClientLogger from Azure Core package.", "logger.logExceptionAsError",
        "logger.logThrowableAsError", "logger.atError().log", "logger.logExceptionAsWarning",
        "logger.logThrowableAsWarning", "logger.atWarning().log");

    private static final String[] IGNORED_EXCEPTIONS = {
        "NullPointerException",
        "IllegalArgumentException",
        "IllegalStateException",
        "UnsupportedOperationException" };
    private static final int[] TOKENS = {
        TokenTypes.CLASS_DEF,
        TokenTypes.INTERFACE_DEF,
        TokenTypes.ENUM_DEF,
        TokenTypes.METHOD_DEF,
        TokenTypes.CTOR_DEF,
        TokenTypes.LITERAL_CATCH,
        TokenTypes.LITERAL_THROW,
        TokenTypes.METHOD_CALL };

    private final Deque<DetailAST> scopes = new ArrayDeque<>();
    private final Deque<CaughtException> caughtExceptions = new ArrayDeque<>();
    private boolean azureCore;
    private boolean checkThrownExceptions = true;
    private boolean checkUnthrownExceptions = true;
    private boolean checkCaughtExceptionCause = true;

    /** Creates a check with all ClientCore exception policies enabled. */
    public ExceptionHandlingCheck() {
    }

    /**
     * Selects the exception-logging API. Azure Core retains its constructor and static-scope exemptions;
     * ClientCore retains its immediate-validation exception exemptions.
     *
     * @param loggingApi either {@code clientcore} or {@code azure-core}.
     * @throws IllegalArgumentException if the logging API is not supported.
     */
    public final void setLoggingApi(String loggingApi) {
        if (!"clientcore".equals(loggingApi) && !"azure-core".equals(loggingApi)) {
            throw new IllegalArgumentException("loggingApi must be clientcore or azure-core.");
        }
        azureCore = "azure-core".equals(loggingApi);
    }

    /**
     * Sets whether thrown exceptions must use the selected logging API. Enabled by default.
     *
     * @param checkThrownExceptions whether to validate thrown exceptions.
     */
    public final void setCheckThrownExceptions(boolean checkThrownExceptions) {
        this.checkThrownExceptions = checkThrownExceptions;
    }

    /**
     * Sets whether exceptions created through ClientCore throwable logging events must be thrown. Enabled by default.
     * This policy does not apply to Azure Core's logging API.
     *
     * @param checkUnthrownExceptions whether to validate throwable logging events.
     */
    public final void setCheckUnthrownExceptions(boolean checkUnthrownExceptions) {
        this.checkUnthrownExceptions = checkUnthrownExceptions;
    }

    /**
     * Sets whether throws inside catch blocks must retain the caught exception. Enabled by default.
     *
     * @param checkCaughtExceptionCause whether to validate caught exception causes.
     */
    public final void setCheckCaughtExceptionCause(boolean checkCaughtExceptionCause) {
        this.checkCaughtExceptionCause = checkCaughtExceptionCause;
    }

    @Override
    public int[] getDefaultTokens() {
        return TOKENS;
    }

    @Override
    public int[] getAcceptableTokens() {
        return TOKENS;
    }

    @Override
    public int[] getRequiredTokens() {
        return TOKENS;
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        super.beginTree(rootAST);
        scopes.clear();
        caughtExceptions.clear();
    }

    @Override
    public void visitToken(DetailAST token) {
        if (isIgnoredSource()) {
            return;
        }
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
            case TokenTypes.ENUM_DEF:
            case TokenTypes.METHOD_DEF:
            case TokenTypes.CTOR_DEF:
                scopes.push(token);
                break;

            case TokenTypes.LITERAL_CATCH:
                if (checkCaughtExceptionCause) {
                    DetailAST parameter = token.findFirstToken(TokenTypes.PARAMETER_DEF);
                    String name = parameter.findFirstToken(TokenTypes.IDENT).getText();
                    CaughtException caught = new CaughtException(name, scopes.peek());
                    collectAliases(token, token, caught);
                    caughtExceptions.push(caught);
                }
                break;

            case TokenTypes.LITERAL_THROW:
                checkThrow(token);
                break;

            case TokenTypes.METHOD_CALL:
                if (!azureCore && checkUnthrownExceptions) {
                    DetailAST event = SdkCheckUtils.findThrowableLoggingEvent(token);
                    if (event != null && !isInsideThrow(token)) {
                        log(event, CREATED_EXCEPTION_NOT_THROWN_MESSAGE);
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void leaveToken(DetailAST token) {
        if (isIgnoredSource()) {
            return;
        }
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
            case TokenTypes.ENUM_DEF:
            case TokenTypes.METHOD_DEF:
            case TokenTypes.CTOR_DEF:
                scopes.pop();
                break;

            case TokenTypes.LITERAL_CATCH:
                if (checkCaughtExceptionCause) {
                    caughtExceptions.pop();
                }
                break;

            default:
                break;
        }
    }

    private void checkThrow(DetailAST token) {
        if (checkThrownExceptions) {
            if (azureCore) {
                checkAzureCoreThrow(token);
            } else {
                checkClientCoreThrow(token);
            }
        }
        for (CaughtException caught : caughtExceptions) {
            if (caught.scope == scopes.peek() && !containsAlias(token, caught.aliases)) {
                log(token, String.format(UNUSED_CAUGHT_EXCEPTION_ERROR, caught.name));
            }
        }
    }

    private void checkClientCoreThrow(DetailAST token) {
        DetailAST expression = token.findFirstToken(TokenTypes.EXPR);
        if (expression == null) {
            return;
        }
        DetailAST constructed = expression.findFirstToken(TokenTypes.LITERAL_NEW);
        if (constructed != null) {
            String name = FullIdent.createFullIdentBelow(constructed).getText();
            for (String ignored : IGNORED_EXCEPTIONS) {
                if (name.endsWith(ignored)) {
                    return;
                }
            }
            log(constructed, RAW_EXCEPTION_ERROR);
        } else {
            DetailAST call = expression.findFirstToken(TokenTypes.METHOD_CALL);
            if (call != null
                && "from".equals(SdkCheckUtils.getCalledMethodName(call))
                && "CoreException.from".equals(FullIdent.createFullIdentBelow(call).getText())) {
                log(call, RAW_EXCEPTION_ERROR);
            }
        }
    }

    private void checkAzureCoreThrow(DetailAST token) {
        if (!isAzureCoreCheckedScope() || containsLogCall(token)) {
            return;
        }
        DetailAST expression = token.findFirstToken(TokenTypes.EXPR);
        DetailAST call = expression == null ? null : expression.findFirstToken(TokenTypes.METHOD_CALL);
        if (call != null) {
            String name = FullIdent.createFullIdentBelow(call).getText();
            if ("logger.logExceptionAsError".equalsIgnoreCase(name)
                || "logger.logThrowableAsError".equalsIgnoreCase(name)
                || "logger.logExceptionAsWarning".equalsIgnoreCase(name)
                || "logger.logThrowableAsWarning".equalsIgnoreCase(name)) {
                return;
            }
        }
        log(token, THROW_LOGGER_EXCEPTION_MESSAGE);
    }

    private boolean isAzureCoreCheckedScope() {
        boolean inMethod = false;
        for (DetailAST scope : scopes) {
            DetailAST modifiers = scope.findFirstToken(TokenTypes.MODIFIERS);
            if (scope.getType() == TokenTypes.CTOR_DEF
                || (modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null)) {
                return false;
            }
            if (scope.getType() == TokenTypes.METHOD_DEF) {
                inMethod = true;
            } else {
                return inMethod;
            }
        }
        return false;
    }

    private static boolean containsLogCall(DetailAST token) {
        if (token.getType() == TokenTypes.METHOD_CALL && "log".equals(SdkCheckUtils.getCalledMethodName(token))) {
            DetailAST target = token.getFirstChild();
            if (target != null && target.getType() == TokenTypes.DOT) {
                return true;
            }
        }
        for (DetailAST child = token.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (containsLogCall(child)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInsideThrow(DetailAST token) {
        DetailAST parent = token.getParent();
        while (parent != null
            && (parent.getType() == TokenTypes.DOT
                || parent.getType() == TokenTypes.EXPR
                || parent.getType() == TokenTypes.TYPECAST
                || parent.getType() == TokenTypes.METHOD_CALL)) {
            parent = parent.getParent();
        }
        return parent != null && parent.getType() == TokenTypes.LITERAL_THROW;
    }

    private static void collectAliases(DetailAST catchToken, DetailAST token, CaughtException caught) {
        for (DetailAST child = token.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == TokenTypes.CLASS_DEF
                || child.getType() == TokenTypes.INTERFACE_DEF
                || child.getType() == TokenTypes.ENUM_DEF) {
                continue;
            }
            if (child.getType() == TokenTypes.IDENT && caught.name.equals(child.getText())) {
                DetailAST assignment = child;
                while (assignment != catchToken && assignment.getType() != TokenTypes.ASSIGN) {
                    assignment = assignment.getParent();
                }
                if (assignment.getType() == TokenTypes.ASSIGN) {
                    DetailAST variable;
                    if (assignment.getParent().getType() == TokenTypes.VARIABLE_DEF) {
                        variable = assignment.getParent().findFirstToken(TokenTypes.IDENT);
                    } else {
                        DetailAST dot = assignment.findFirstToken(TokenTypes.DOT);
                        variable = dot == null
                            ? assignment.findFirstToken(TokenTypes.IDENT)
                            : dot.findFirstToken(TokenTypes.IDENT);
                    }
                    if (variable != null) {
                        caught.aliases.add(variable.getText());
                    }
                }
            }
            collectAliases(catchToken, child, caught);
        }
    }

    private static boolean containsAlias(DetailAST token, Set<String> aliases) {
        if (token.getType() == TokenTypes.IDENT && aliases.contains(token.getText())) {
            return true;
        }
        for (DetailAST child = token.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (containsAlias(child, aliases)) {
                return true;
            }
        }
        return false;
    }

    private static final class CaughtException {
        private final String name;
        private final DetailAST scope;
        private final Set<String> aliases = new HashSet<>();

        private CaughtException(String name, DetailAST scope) {
            this.name = name;
            this.scope = scope;
            aliases.add(name);
        }
    }
}
