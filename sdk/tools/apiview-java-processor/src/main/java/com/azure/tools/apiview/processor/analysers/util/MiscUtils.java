package com.azure.tools.apiview.processor.analysers.util;

import com.azure.tools.apiview.processor.model.ReviewLine;
import com.azure.tools.apiview.processor.model.ReviewToken;
import com.azure.tools.apiview.processor.model.TokenKind;

import java.util.regex.Pattern;

import static com.azure.tools.apiview.processor.model.TokenKind.MAVEN_VALUE;
import static com.azure.tools.apiview.processor.model.TokenKind.PUNCTUATION;

/**
 * Miscellaneous utility methods.
 */
public final class MiscUtils {
    public static final Pattern URL_MATCH = Pattern.compile(
        "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)");

    /**
     * Tokenizes a key-value pair.
     *
     * @param key The key.
     * @param value The value.
     * @return A token representing the key-value pair.
     */
    public static ReviewLine tokeniseMavenKeyValue(ReviewLine parentLine, String key, Object value) {
        return tokeniseMavenKeyValue(parentLine, key, value, false);
    }

    public static ReviewLine tokeniseMavenKeyValue(ReviewLine parentLine, String key, Object value, boolean skipDiff) {
        ReviewToken valueToken = new ReviewToken(MAVEN_VALUE, value == null ? "<default value>" : value.toString());

        if (skipDiff) {
            valueToken.setSkipDiff();
        }

        return parentLine.addChildLine("maven-lineid-" + key + "-" + value)
                .addToken(new ReviewToken(TokenKind.MAVEN_KEY, key))
                .addToken(new ReviewToken(PUNCTUATION, ":"))
                .addToken(valueToken);
    }

    /**
     * Makes all characters in the string lowercase, other than the first character.
     */
    public static String upperCase(String s) {
        return upperCase(s, 0);
    }

    /**
     * Makes all characters in the string lowercase, other than the given index.
     */
    public static String upperCase(String s, int index) {
        return s.substring(0, index).toLowerCase()
                + Character.toUpperCase(s.charAt(index))
                + s.substring(index + 1).toLowerCase();
    }

    private MiscUtils() {
    }
}
