// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.SearchOptions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class is used to help construct valid OData filter expressions, like the kind used by {@link
 * SearchOptions#setFilter(String)} by automatically replacing, quoting, and escaping string parameters.
 * <p>
 * For more information, see <a href="https://docs.microsoft.com/azure/search/search-filters">Filters in Azure Cognitive
 * Search</a>.
 */
public final class SearchFilter {
    private static final ClientLogger LOGGER;
    private static final Set<Class<?>> SAFE_CLASSES;

    static {
        LOGGER = new ClientLogger(SearchFilter.class);
        SAFE_CLASSES = new HashSet<>(20);
        SAFE_CLASSES.add(boolean.class);
        SAFE_CLASSES.add(Boolean.class);
        SAFE_CLASSES.add(byte.class);
        SAFE_CLASSES.add(Byte.class);
        SAFE_CLASSES.add(short.class);
        SAFE_CLASSES.add(Short.class);
        SAFE_CLASSES.add(int.class);
        SAFE_CLASSES.add(Integer.class);
        SAFE_CLASSES.add(long.class);
        SAFE_CLASSES.add(Long.class);
        SAFE_CLASSES.add(float.class);
        SAFE_CLASSES.add(Float.class);
        SAFE_CLASSES.add(double.class);
        SAFE_CLASSES.add(Double.class);
    }

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
                cleanedArgs[i] = null;
                continue;
            }

            Class<?> argClass = arg.getClass();
            if (Objects.equals(arg, Float.NEGATIVE_INFINITY) || Objects.equals(arg, Double.NEGATIVE_INFINITY)) {
                cleanedArgs[i] = "-INF";
            } else if (Objects.equals(arg, Float.POSITIVE_INFINITY) || Objects.equals(arg, Double.POSITIVE_INFINITY)) {
                cleanedArgs[i] = "INF";
            } else if (SAFE_CLASSES.contains(argClass)) {
                cleanedArgs[i] = arg;
            } else if (arg instanceof Date) {
                cleanedArgs[i] = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    .format(OffsetDateTime.ofInstant(((Date) arg).toInstant(), ZoneOffset.UTC));
            } else if (arg instanceof OffsetDateTime) {
                cleanedArgs[i] = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format((OffsetDateTime) arg);
            } else if (arg instanceof CharSequence) {
                cleanedArgs[i] = quote(((CharSequence) arg).toString());
            } else if (argClass.isAssignableFrom(char.class) || arg instanceof Character) {
                cleanedArgs[i] = quote(((Character) arg).toString());
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

    private SearchFilter() {
    }
}
