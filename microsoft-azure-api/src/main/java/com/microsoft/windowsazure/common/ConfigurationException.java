package com.microsoft.windowsazure.common;

public class ConfigurationException extends RuntimeException {

    private static final long serialVersionUID = -5570476914992165380L;

    public ConfigurationException() {
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
