// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.SearchOptions;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to help construct valid OData filter expressions, like the kind used by {@link
 * SearchOptions#setFilter(String)} by automatically replacing, quoting, and escaping string parameters.
 * <p>
 * For more information, see <a href="https://docs.microsoft.com/azure/search/search-filters">Filters in Azure Cognitive
 * Search</a>.
 */
public final class SearchFilter {
    private static final ClientLogger LOGGER = new ClientLogger(SearchFilter.class);
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
        new SimpleDateFormat(DateTimeFormatter.ISO_INSTANT.toString());

    private static final Set<Class<?>> SAFE_CLASSES = new HashSet<>(Arrays.asList(
        boolean.class, Boolean.class,
        byte.class, Byte.class,
        short.class, Short.class,
        int.class, Integer.class,
        long.class, Long.class,
        float.class, Float.class,
        double.class, Double.class
    ));

    /**
     * Create an OData filter expression from a formattable string.
     * <p>
     * The format argument values will be quoted and escaped as necessary.
     *
     * @param formattableString The formattable string.
     * @param args The arguments for the formattable string.
     * @return A valid OData filter expression.
     */
    public static String create(String formattableString, Object... args) {
        if (formattableString == null) {
            return null;
        }

        if (CoreUtils.isNullOrEmpty(args)) {
            return formattableString;
        }

        Object[] cleanedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                cleanedArgs[i] = "null";
                continue;
            }

            Class<?> argClass = arg.getClass();
            if (SAFE_CLASSES.contains(argClass)) {
                cleanedArgs[i] = arg;
            } else if (argClass.isAssignableFrom(Date.class)) {
                cleanedArgs[i] = SIMPLE_DATE_FORMAT.format((Date) arg);
            } else if (argClass.isAssignableFrom(OffsetDateTime.class)) {
                cleanedArgs[i] = DateTimeFormatter.ISO_INSTANT.format((OffsetDateTime) arg);
            } else if (argClass.isAssignableFrom(CharSequence.class)) {
                cleanedArgs[i] = quote(((CharSequence) arg).toString());
            } else if (argClass.isAssignableFrom(char.class) || argClass.isAssignableFrom(Character.class)) {
                cleanedArgs[i] = quote(((Character) arg).toString());
            } else if (argClass.isAssignableFrom(StringBuilder.class)) {
                cleanedArgs[i] = quote(((StringBuilder) arg).toString());
            } else {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(
                    "Unable to convert argument %s from type %s to an OData literal.", arg, argClass.getName())));
            }
        }

        return String.format(formattableString, cleanedArgs);
    }

    /*
     * Quote and escape OData strings.
     */
    private static String quote(String text) {
        if (text == null) {
            return "null";
        }

        // Optimistically allocate an extra 5% for escapes
        StringBuilder builder = new StringBuilder(2 + (int) (text.length() * 1.05))
            .append("'");

        for (char ch : text.toCharArray()) {
            builder.append(ch);
            if (ch == '\'') {
                builder.append(ch);
            }
        }

        return builder.append("'").toString();
    }
}
