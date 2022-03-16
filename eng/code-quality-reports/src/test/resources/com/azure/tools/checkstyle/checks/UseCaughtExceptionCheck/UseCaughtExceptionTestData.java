import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.Locale;

public class UseCaughtExceptionTestData {
    private IOException lastError;

    ClientLogger logger = new ClientLogger(UseCaughtExceptionTestData.class);

    public errorCaughtExceptionUnused() {
        try {
            String s = null;
            System.out.println(s.toLowerCase(Locale.ROOT));
        } catch (NullPointerException e) {
            throw logger.logExceptionAsError(new RuntimeException("Does not return error cause"));
        }
    }

    public caughtExceptionUsed() {
        try {
            String s = null;
            System.out.println(s.toLowerCase(Locale.ROOT));
        } catch (NullPointerException e) {
            throw logger.logExceptionAsError(new RuntimeException("Return the exception cause", e));
        }
    }

    public validThrowCaughtExceptionUsed() {
        try {
            String s = null;
            System.out.println(s.toLowerCase(Locale.ROOT));
        } catch (NullPointerException exception) {
            throw logger.logExceptionAsError(new NullPointerException(exception));
        }
    }

    public validUseCaughtException() {
        try {
            String s = null;
            System.out.println(s.toLowerCase(Locale.ROOT));
        } catch (NullPointerException exception) {
            System.out.println(exception.getCause());
        }
    }

    public validUseCaughtExceptionInFinally() {
        try {
            String s = null;
            System.out.println(s.toLowerCase(Locale.ROOT));
        } catch (NullPointerException e) {
            System.out.println("error occurred.");
        } finally {
            throw logger.logExceptionAsError(new NullPointerException(e));
        }
    }

    public validUseCaughtExceptionChaining() {
        try {
            Exception exec1 = new NullPointerException();
            throw new IllegalArgumentException(exec1);
        } catch (IllegalArgumentException iae) {
            throw logger.logExceptionAsError(iae.getCause());
        }
    }

    public validSuppressedUseCaughtException() {
        try {
            Exception exec1 = new NullPointerException();
            IllegalArgumentException exec2 = new IllegalArgumentException();
            exec2.addSuppressed(exec1);
            throw ex2;
        } catch (IllegalArgumentException iae) {
            throw logger.logExceptionAsError(iae.getCause());
        }
    }

    public caughtExceptionUsedChaining() {
        try {
            String s = null;
            System.out.println(s.toLowerCase(Locale.ROOT));
        } catch (NullPointerException e) {
            throw logger.logExceptionAsError(
                new RuntimeException(new IllegalArgumentException("Return the chaining exception cause", e)));
        }
    }

    public validCaughtExceptionWrapped() {
        try {
            throw new PrivilegedActionException();
        } catch (PrivilegedActionException ex) {
            Throwable cause = ex.getCause();
            // If the privileged call failed due to an IOException unwrap it.
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(cause));
        }
    }

    public validMultipleCatchBlock() {
        try {
            throw new PrivilegedActionException();
        } catch (IOException | PrivilegedActionException ex) {
            RuntimeException p = new RuntimeException(ex);
            throw LOGGER.logExceptionAsError(p);
        }
    }

    public validCaughtExceptionThisInitialize() {
        try {
            throw new PrivilegedActionException();
        } catch (final PrivilegedActionException e) {
            this.lastError = new IOException(e);

            throw this.lastError;
        }
    }
}
