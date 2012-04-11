package com.microsoft.windowsazure.services.core.utils;

import java.util.List;

public class CommaStringBuilder {
    private final StringBuilder sb = new StringBuilder();

    public void add(String representation) {
        if (sb.length() > 0) {
            sb.append(",");
        }
        sb.append(representation);
    }

    public void addValue(boolean value, String representation) {
        if (value) {
            add(representation);
        }
    }

    public static String join(List<String> values) {
        CommaStringBuilder sb = new CommaStringBuilder();

        for (String value : values) {
            sb.add(value);
        }

        return sb.toString();
    }

    public static String join(String... values) {
        CommaStringBuilder sb = new CommaStringBuilder();

        for (String value : values) {
            sb.add(value);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        if (sb.length() == 0)
            return null;
        return sb.toString();
    }
}
