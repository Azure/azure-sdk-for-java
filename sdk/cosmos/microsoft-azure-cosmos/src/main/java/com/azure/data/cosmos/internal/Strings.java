/*
 * Copyright (C) 2010 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.azure.data.cosmos.internal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class Strings {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static final String Emtpy = "";

    public static boolean isNullOrWhiteSpace(String str) {
        return StringUtils.isEmpty(str) || StringUtils.isWhitespace(str);
    }
    public static boolean isNullOrEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    public static String toString(boolean value) {
        return Boolean.toString(value);
    }

    public static String toString(int value) {
        return Integer.toString(value);
    }

    public static boolean areEqual(String str1, String str2) {
        return StringUtils.equals(str1, str2);
    }

    public static boolean areEqualIgnoreCase(String str1, String str2) {
        return StringUtils.equalsIgnoreCase(str1, str2);
    }

    public static boolean containsIgnoreCase(String str1, String str2) {
        return StringUtils.containsIgnoreCase(str1, str2);
    }

    public static int compare(String str1, String str2) {
        return StringUtils.compare(str1, str2);
    }

    public static String toCamelCase(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1, str.length()).toLowerCase();
    }
    
    public static String fromCamelCaseToUpperCase(String str) {
        if (str == null) {
            return null;
        }

        StringBuilder result = new StringBuilder(str);

        int i = 1;
        while (i < result.length()) {
            if (Character.isUpperCase(result.charAt(i))) {
                result.insert(i, '_');
                i += 2;
            } else {
                result.replace(i, i + 1, Character.toString(Character.toUpperCase(result.charAt(i))));
                i ++;
            }
        }

        return result.toString();
    }

    /**
     * Returns the given {@code template} string with each occurrence of {@code "%s"} replaced with
     * the corresponding argument value from {@code args}; or, if the placeholder and argument counts
     * do not match, returns a best-effort form of that string. Will not throw an exception under
     * normal conditions.
     *
     * <p><b>Note:</b> For most string-formatting needs, use {@link String#format String.format},
     * {@link java.io.PrintWriter#format PrintWriter.format}, and related methods. These support the
     * full range of <a
     * href="https://docs.oracle.com/javase/9/docs/api/java/util/Formatter.html#syntax">format
     * specifiers</a>, and alert you to usage errors by throwing {@link
     * java.util.IllegalFormatException}.
     *
     * <p>In certain cases, such as outputting debugging information or constructing a message to be
     * used for another unchecked exception, an exception during string formatting would serve little
     * purpose except to supplant the real information you were trying to provide. These are the cases
     * this method is made for; it instead generates a best-effort string with all supplied argument
     * values present. This method is also useful in environments such as GWT where {@code
     * String.format} is not available. As an example, method implementations of the {@link
     * com.google.common.base.Preconditions} class use this formatter, for both of the reasons just discussed.
     *
     * <p><b>Warning:</b> Only the exact two-character placeholder sequence {@code "%s"} is
     * recognized.
     *
     * @param template a string containing zero or more {@code "%s"} placeholder sequences. {@code
     *     null} is treated as the four-character string {@code "null"}.
     * @param args the arguments to be substituted into the message template. The first argument
     *     specified is substituted for the first occurrence of {@code "%s"} in the template, and so
     *     forth. A {@code null} argument is converted to the four-character string {@code "null"};
     *     non-null values are converted to strings using {@link Object#toString()}.
     * @since guava - 25.1
     */
    // TODO(diamondm) consider using Arrays.toString() for array parameters
    public static String lenientFormat(
        String template, Object ... args) {
        template = String.valueOf(template); // null -> "null"

        if (args == null) {
            args = new Object[] {"(Object[])null"};
        } else {
            for (int i = 0; i < args.length; i++) {
                args[i] = lenientToString(args[i]);
            }
        }

        // start substituting the arguments into the '%s' placeholders
        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;
        while (i < args.length) {
            int placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }
            builder.append(template, templateStart, placeholderStart);
            builder.append(args[i++]);
            templateStart = placeholderStart + 2;
        }
        builder.append(template, templateStart, template.length());

        // if we run out of placeholders, append the extra args in square braces
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);
            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }
            builder.append(']');
        }

        return builder.toString();
    }

    private static String lenientToString(Object o) {
        try {
            return String.valueOf(o);
        } catch (Exception e) {
            // Default toString() behavior - see Object.toString()
            String objectToString =
                o.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(o));
            // Logger is created inline with fixed name to avoid forcing Proguard to create another class.
            logger.warn("Exception during lenientFormat for " + objectToString, e);
            return "<" + objectToString + " threw " + e.getClass().getName() + ">";
        }
    }
}
