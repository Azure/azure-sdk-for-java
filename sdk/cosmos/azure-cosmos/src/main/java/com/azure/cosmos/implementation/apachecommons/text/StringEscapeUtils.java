//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.apachecommons.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.azure.cosmos.implementation.apachecommons.text.translate.AggregateTranslator;
import com.azure.cosmos.implementation.apachecommons.text.translate.CharSequenceTranslator;
import com.azure.cosmos.implementation.apachecommons.text.translate.EntityArrays;
import com.azure.cosmos.implementation.apachecommons.text.translate.LookupTranslator;
import com.azure.cosmos.implementation.apachecommons.text.translate.OctalUnescaper;
import com.azure.cosmos.implementation.apachecommons.text.translate.UnicodeUnescaper;

/**
 * This class is shaded from version 1.10.0 of apache commons-text library
 */
public class StringEscapeUtils {
    public static final CharSequenceTranslator UNESCAPE_JAVA;

    private StringEscapeUtils() {
    }

    public static StringEscapeUtils.Builder builder(CharSequenceTranslator translator) {
        return new StringEscapeUtils.Builder(translator);
    }

    public static String unescapeJava(String input) {
        return UNESCAPE_JAVA.translate(input);
    }

    static {
        final Map<CharSequence, CharSequence> unescapeJavaMap = new HashMap<>();
        unescapeJavaMap.put("\\\\", "\\");
        unescapeJavaMap.put("\\\"", "\"");
        unescapeJavaMap.put("\\'", "'");
        unescapeJavaMap.put("\\", "");
        UNESCAPE_JAVA = new AggregateTranslator(
            new OctalUnescaper(),     // .between('\1', '\377'),
            new UnicodeUnescaper(),
            new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE),
            new LookupTranslator(Collections.unmodifiableMap(unescapeJavaMap))
        );
    }

    public static final class Builder {
        private final StringBuilder sb;
        private final CharSequenceTranslator translator;

        private Builder(CharSequenceTranslator translator) {
            this.sb = new StringBuilder();
            this.translator = translator;
        }

        public StringEscapeUtils.Builder escape(String input) {
            this.sb.append(this.translator.translate(input));
            return this;
        }

        public StringEscapeUtils.Builder append(String input) {
            this.sb.append(input);
            return this;
        }

        public String toString() {
            return this.sb.toString();
        }
    }
}

