import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;

public class ThrowsClassField {
    private static final IOException staticException = new IOException("Common exception");
    private IOException exception;

    /**
     * Method that throws exception in class using "this".
     */
    public void throwExceptionWithThis() {
        throw this.exception;
    }

    /**
     * Method that throws exception in class.
     */
    public void throwException() {
        throw exception;
    }

    /**
     * Method that throws static exception in class.
     */
    public void throwStaticException() {
        throw staticException;
    }

    /**
     * Method that throws static exception in class with classname qualifier.
     */
    public void throwStaticExceptionWithClassname() {
        throw ThrowsClassField.staticException;
    }

}
