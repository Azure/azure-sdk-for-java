// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.utils;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.AuthenticateChallenge;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Parses a {@link HttpHeaderName#WWW_AUTHENTICATE} or {@link HttpHeaderName#PROXY_AUTHENTICATE} header value into the
 * pieces required to create one or more {@link AuthenticateChallenge}.
 */
public final class AuthenticateChallengeParser {
    private static final ClientLogger LOGGER = new ClientLogger(AuthenticateChallengeParser.class);

    private static final boolean[] VALID_TOKEN_CHARS = new boolean[128];
    private static final boolean[] VALID_TOKEN68_CHARS = new boolean[128];

    static {
        // Setup VALID_TOKEN68_CHARS first as it is mostly a subset of VALID_TCHARS.
        // The only exception is that token68 allows '/', set that after copying.
        // This is also excluding the '=' character as that is only allowed at the end of a token68 and will be handled
        // externally to this lookup table.
        Arrays.fill(VALID_TOKEN68_CHARS, '0', '9' + 1, true);
        Arrays.fill(VALID_TOKEN68_CHARS, 'A', 'Z' + 1, true);
        Arrays.fill(VALID_TOKEN68_CHARS, 'a', 'z' + 1, true);
        VALID_TOKEN68_CHARS['-'] = true;
        VALID_TOKEN68_CHARS['.'] = true;
        VALID_TOKEN68_CHARS['_'] = true;
        VALID_TOKEN68_CHARS['~'] = true;
        VALID_TOKEN68_CHARS['+'] = true;

        System.arraycopy(VALID_TOKEN68_CHARS, 0, VALID_TOKEN_CHARS, 0, 128);
        VALID_TOKEN_CHARS['!'] = true;
        VALID_TOKEN_CHARS['#'] = true;
        VALID_TOKEN_CHARS['$'] = true;
        VALID_TOKEN_CHARS['%'] = true;
        VALID_TOKEN_CHARS['&'] = true;
        VALID_TOKEN_CHARS['\''] = true;
        VALID_TOKEN_CHARS['*'] = true;
        VALID_TOKEN_CHARS['^'] = true;
        VALID_TOKEN_CHARS['`'] = true;
        VALID_TOKEN_CHARS['|'] = true;

        VALID_TOKEN68_CHARS['/'] = true;
    }

    private final String challenge;
    private final int challengeLength;

    private State state = State.BEGINNING;
    private int currentIndex;
    private AuthenticateChallengeToken token;

    /**
     * Creates an instance of AuthenticateChallengeParser.
     *
     * @param challenge The challenge to parse.
     */
    public AuthenticateChallengeParser(String challenge) {
        this.challenge = Objects.requireNonNull(challenge, "challenge cannot be null.");
        this.challengeLength = challenge.length();
        this.currentIndex = 0;

        // WWW-Authenticate and Proxy-Authenticate use the form:
        //
        // *( "," OWS ) challenge *( OWS "," [ OWS challenge ] )
        //
        // Which means the header may begin with any number of ',', SP (U+0020 / ' '), and HTAB (U+0009 / '\t')
        // characters. Skip those characters.
        while (currentIndex < challengeLength) {
            char currentCharacter = challenge.charAt(currentIndex);
            if (currentCharacter != ',' && currentCharacter != ' ' && currentCharacter != '\t') {
                break;
            }

            currentIndex++;
        }
    }

    /**
     * Parses the authenticate header into a list of {@link AuthenticateChallenge}.
     *
     * @return A list of {@link AuthenticateChallenge}.
     * @throws IllegalArgumentException If the authenticate header is malformed.
     */
    public List<AuthenticateChallenge> parse() {
        // At a high-level the authenticate headers take the form of:
        // WWW-Authenticate: <scheme> <parameters>, <scheme> <parameters>, ...
        // Proxy-Authenticate: <scheme> <parameters>, <scheme> <parameters>, ...
        //
        // At a more technical level, which this method will parse the format (using ABNF) is:
        //
        // authenticate-header = 1#challenge (at lease one challenge, delimited by ',' and optional spaces)
        // challenge = auth-scheme [ 1*SP ( token68 / #auth-param ) ] (may contain a token68 or 0 or more auth-params)
        // auth-scheme = token
        // auth-param = token BWS "=" BWS ( token / quoted-string )
        // token68 = 1*( ALPHA / DIGIT / - / . / _ / ~ / + / '/' ) *"="
        // quoted-string = DQUOTE *( qdtext / quoted-pair ) DQUOTE
        // qdtext = HTAB / SP / ! / '#' - '[' / ']' - '~' / obs-text
        // quoted-pair = "\" ( HTAB / SP / VCHAR / obs-text )
        // obs-text = U+0080 - U+00FF (extended ASCII)
        // token = 1*( tchar )
        // tchar = ALPHA / DIGIT / ! / # / $ / % / & / ' / * / + / - / . / ^ / _ / ` / | / ~
        // VCHAR = U+0021 - U+007E (! - ~, or all printable ASCII characters except space and delete)
        //
        // BWS is optional spaces (SP / HTAB) that exists for historical reasons and should be handled during parsing
        // but must not be generated.
        //
        // All information above is taken from RFC 7230 and RFC 7235.
        // https://www.rfc-editor.org/rfc/rfc7230
        // https://www.rfc-editor.org/rfc/rfc7235
        //
        // WWW-Authenticate and Proxy-Authenticate use the form:
        //
        // *( "," OWS ) challenge *( OWS "," [ OWS challenge ] )
        //
        // Which means the header may begin with any number of ',', SP (U+0020 / ' '), and HTAB (U+0009 / '\t')
        // characters. Skip those characters.
        //
        // Then replacing 'challenge' with its definition it becomes:
        //
        // *( "," OWS ) auth-scheme [ 1*SP ( token68 / #auth-param ) ]
        //     *( OWS "," [ OWS auth-scheme [ 1*SP ( token68 / #auth-param ) ] ] )
        //
        // Where the auth-scheme and token68 / auth-params are separated by SP characters only. The logic for parsing
        // will be the following:
        //
        // 1. Skip any leading spaces and commas.
        // 2. Split the authenticate header into chunks delimited by commas that aren't within a quoted string.
        // 3. Remove any leading or trailing OWS (optional spaces) from each chunk.
        // 4. Process each chunk, keeping track of the current state of the parser, using the following logic:
        //
        // I. If it's the first chunk being processed it must contain a scheme. Optionally, that chunk may also include
        //    a token68 or an auth-param after the scheme separated by SP characters. If the first chunk isn't a scheme,
        //    an IllegalArgumentException will be thrown.
        // II. Subsequent chunks will use the following logic:
        //    i. If the chunk contains unquoted SP characters separating token characters (or roughly the equivalent for
        //       valid token68 characters), then the chunk is a new challenge scheme.
        //    ii. If the chunk contains equal signs, then the chunk is either a token68 or an auth-param. Determine
        //       which one based on where the equal signs are, if the equal signs are the trailing characters of the
        //       chunk it's a token68 otherwise it's an auth-param.
        // III. If the chunk is a token68 or an auth-param, then add it to the current challenge.
        //    i. If the current challenge already contains a token68 then any subsequent token68 or auth-params will
        //       throw an IllegalArgumentException.
        // IV. Once a new challenge scheme is found, the previous challenge is added to the list and state is reset.
        List<AuthenticateChallenge> authenticateChallenges = new ArrayList<>();

        String scheme = null;
        String token68 = null;
        Map<String, String> parameters = null;

        while (next()) {
            if (token.scheme != null) {
                // This piece contained a scheme.
                // This is either the first scheme or a new scheme, handle it appropriately.
                if (scheme != null) {
                    // This is a new scheme, add the previous challenge to the list.
                    authenticateChallenges.add(createChallenge(scheme, token68, parameters));
                    parameters = null;
                    token68 = null;
                }

                scheme = token.scheme;
            } else if (token.token68 != null) {
                if (scheme == null) {
                    throw LOGGER.throwableAtError()
                        .addKeyValue("challenge", challenge)
                        .log("Challenge had token68 before scheme.", IllegalArgumentException::new);
                } else if (token68 != null) {
                    throw LOGGER.throwableAtError()
                        .addKeyValue("challenge", challenge)
                        .log("Challenge had multiple token68s.", IllegalArgumentException::new);
                }

                token68 = token.token68;
            } else if (token.authParam != null) {
                if (scheme == null) {
                    throw LOGGER.throwableAtError()
                        .addKeyValue("challenge", challenge)
                        .log("Challenge had auth-param before scheme.", IllegalArgumentException::new);
                }

                if (parameters == null) {
                    parameters = new LinkedHashMap<>();
                }

                if (parameters.put(token.authParam.getKey(), token.authParam.getValue()) != null) {
                    throw LOGGER.throwableAtError()
                        .addKeyValue("challenge", challenge)
                        .log("Challenge had duplicate auth-param.", IllegalArgumentException::new);
                }
            }
        }

        if (scheme != null) {
            authenticateChallenges.add(createChallenge(scheme, token68, parameters));
        }

        return authenticateChallenges;
    }

    private AuthenticateChallenge createChallenge(String scheme, String token68, Map<String, String> parameters) {
        if (token68 == null && parameters == null) {
            return new AuthenticateChallenge(scheme);
        } else if (token68 == null) {
            return new AuthenticateChallenge(scheme, parameters);
        } else if (parameters == null) {
            return new AuthenticateChallenge(scheme, token68);
        }

        throw LOGGER.throwableAtError()
            .addKeyValue("challenge", challenge)
            .log("Challenge had both token68 and auth-params.", IllegalArgumentException::new);
    }

    boolean next() {
        if (currentIndex >= challengeLength) {
            return false;
        }

        if (state == State.BEGINNING) {
            handleBeginning();
        } else if (state == State.SCHEME) {
            handleScheme();
        } else if (state == State.CHALLENGE_SEPARATOR) {
            handleChallenge();
        }

        return true;
    }

    private char iterateUntil(Predicate<Character> until) {
        while (currentIndex < challengeLength) {
            char c = challenge.charAt(currentIndex);
            if (until.test(c)) {
                return c;
            }

            currentIndex++;
        }

        return '\0';
    }

    private char iterateUntilNextNonSpace() {
        currentIndex++;
        return iterateUntil(c -> c != ' ' && c != '\t');
    }

    private char iterateUntilEqualsSpaceOrComma() {
        return iterateUntil(c -> c == '=' || c == ' ' || c == ',');
    }

    private void handleBeginning() {
        // If the state is BEGINNING, then the next token must be a scheme.
        // Beginning state is the state before any characters have been processed (except leading spaces and commas).
        // The only valid characters in this case are one or more token characters followed by one of spaces or comma.
        int start = currentIndex;
        char c = iterateUntil(c1 -> c1 == ' ' || c1 == '\t' || c1 == ',');

        token = handleSchemeToken(start, currentIndex, c, false);
    }

    private AuthenticateChallengeToken handleSchemeToken(int schemeStartInclusive, int schemeEndExclusive,
        char currentChar, boolean alreadyInNextState) {
        String scheme = challenge.substring(schemeStartInclusive, schemeEndExclusive);
        if (!isValidToken(scheme)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("challenge", challenge)
                .addKeyValue("scheme", scheme)
                .log("Scheme contained an invalid character.", IllegalArgumentException::new);
        }

        // Iterate until the next non-space character, unless the scheme terminated with a comma.
        currentChar = (alreadyInNextState || currentChar == ',') ? currentChar : iterateUntilNextNonSpace();
        if (currentIndex < challengeLength && currentChar == ',') {
            // The next character is a comma, update the state to CHALLENGE_SEPARATOR and continue.
            // This is important as SCHEME indicates that the next token is either a token68 or an auth-param.
            // CHALLENGE_SEPARATOR indicates that the next token can be anything.
            state = State.CHALLENGE_SEPARATOR;
            iterateUntilNextNonSpace(); // Iterate to the first character in the challenge piece.
        } else {
            state = State.SCHEME;
        }

        return new AuthenticateChallengeToken(scheme, null, null);
    }

    private void handleScheme() {
        // If the state is SCHEME, then the next token must be a token68 or an auth-param.
        // Search until a comma, space, or equal sign is found.
        // If a comma is found or the end of challenge reached the current token is a token68 as auth-param requires an
        // equal sign whereas token68 those are optional padding.
        // If a space is found, the current token could be either an auth-param or a token68. Iterate until the next
        // non-space is found or the end of challenge is reached. If a comma is found or end of challenge reached then
        // it's a token68, if an equal sign is found then it's an auth-param, if anything else is found it's an error.
        // If an equal sign is found, check if there are equal signs until a space, comma, or end of challenge, then
        // it's a token68, otherwise it's an auth-param.
        int start = currentIndex; // This start will never have leading space.
        char c = iterateUntilEqualsSpaceOrComma();

        if (c == ',' || currentIndex == challengeLength) {
            // As stated above, must be a token68.
            token = new AuthenticateChallengeToken(null, validateToken68(challenge, start, currentIndex), null);
        } else if (c == ' ') {
            int token68OrParamKeyEnd = currentIndex;
            c = iterateUntilNextNonSpace();
            if (c != '=' && c != ',' && currentIndex < challengeLength) {
                // The next character is neither a comma nor an equal sign, throw an exception.
                throw LOGGER.throwableAtError()
                    .addKeyValue("challenge", challenge)
                    .log("Challenge had more than one token68 or auth-param in the same comma separator.",
                        IllegalArgumentException::new);
            }

            if (c == ',' || currentIndex == challengeLength) {
                String token68 = validateToken68(challenge, start, currentIndex);
                token = new AuthenticateChallengeToken(null, token68, null);
            } else {
                createAuthParamToken(start, token68OrParamKeyEnd, iterateUntilNextNonSpace());
            }
        } else {
            int equalsIndex = currentIndex;

            // If the equals index is the last character or the next character is another equal sign, then it's a
            // token68.
            if (currentIndex + 1 == challengeLength || challenge.charAt(currentIndex + 1) == '=') {
                // Two equal signs in a row, must be a token68.
                c = iterateUntil(c1 -> c1 != '=');
                token = new AuthenticateChallengeToken(null, validateToken68(challenge, start, currentIndex), null);
            } else {
                // Otherwise check what the next non-space character is after the equals sign. If it's a comma or end of
                // challenge then it's a token68, otherwise it's an auth-param.
                c = iterateUntilNextNonSpace();
                if (c == ',' || currentIndex == challengeLength) {
                    // It's a token68.
                    token = new AuthenticateChallengeToken(null, validateToken68(challenge, start, equalsIndex + 1),
                        null);
                } else {
                    // It's a challenge parameter.
                    c = createAuthParamToken(start, equalsIndex, c);
                }
            }

            // If the character following the last equal sign isn't a comma or end of challenge, there is an error.
            c = (c == ',' || currentIndex == challengeLength) ? c : iterateUntilNextNonSpace();
            if (currentIndex < challengeLength && c != ',') {
                throw LOGGER.throwableAtError()
                    .addKeyValue("challenge", challenge)
                    .log("Challenge had more than one token68 or auth-param in the same comma separator.",
                        IllegalArgumentException::new);
            }
        }

        // Update state to the next state, which is CHALLENGE_SEPARATOR and skip over the comma.
        state = State.CHALLENGE_SEPARATOR;
        iterateUntilNextNonSpace();
    }

    private char createAuthParamToken(int keyStartInclusive, int keyEndExclusive, char currentChar) {
        String authParamKey = challenge.substring(keyStartInclusive, keyEndExclusive);
        if (!isValidToken(authParamKey)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("challenge", challenge)
                .addKeyValue("authParamKey", authParamKey)
                .log("Auth-param key contained an invalid character.", IllegalArgumentException::new);
        }

        int start = currentIndex;
        String authParamValue;
        if (currentChar == '"') {
            // Iterate until the next character, which is either the start of a quoted-string or another '"' denoting an
            // empty quoted-string.
            currentIndex++;
            start++;

            // Iterate until a closing double quote which isn't escaped by a backslash.
            currentChar = iterateUntil(c1 -> c1 == '"' && challenge.charAt(currentIndex - 1) != '\\');
            if (currentChar != '"') {
                // Only time this should happen is reaching the end of the challenge.
                throw LOGGER.throwableAtError()
                    .addKeyValue("challenge", challenge)
                    .log("Quoted-string in challenge was not terminated with a double quote.",
                        IllegalArgumentException::new);
            }

            authParamValue = challenge.substring(start, currentIndex).replace("\\\\", "");
        } else {
            // Handle as a token.
            currentChar = iterateUntil(c1 -> c1 == ' ' || c1 == '\t' || c1 == ',');
            authParamValue = challenge.substring(start, currentIndex);
            if (!isValidToken(authParamValue)) {
                throw LOGGER.throwableAtError()
                    .addKeyValue("challenge", challenge)
                    .addKeyValue("authParamValue", authParamValue)
                    .log("Auth-param value contained an invalid character.", IllegalArgumentException::new);
            }
        }

        // Iterate until the next non-space character.
        currentChar = (currentChar == ',') ? currentChar : iterateUntilNextNonSpace();

        // After the scheme only a single token68 or auth-param is allowed. If after any trailing spaces the next
        // character isn't a comma throw an exception.
        if (currentIndex < challengeLength && currentChar != ',') {
            throw LOGGER.throwableAtError()
                .addKeyValue("challenge", challenge)
                .log("Challenge had more than one token68 or auth-param in the same comma separator.",
                    IllegalArgumentException::new);
        }

        token = new AuthenticateChallengeToken(null, null, new AbstractMap.SimpleEntry<>(authParamKey, authParamValue));
        return currentChar;
    }

    private void handleChallenge() {
        // If the state is CHALLENGE_SEPARATOR, then the next token can be either new challenge scheme or an auth-param.
        // Skip any leading spaces.
        // Search until an equal sign, comma, space, or end of challenge is found.
        // If a comma is found or the end of challenge reached the current token is a scheme without any auth-params or
        // a token68.
        // If an equal sign is found, then the current token is an auth-param as that isn't allowed in a scheme.
        // If a space is found search for the next non-space character. If it's an equal sign then it's an auth-param,
        // otherwise this is the beginning of a new scheme.
        int start = currentIndex;

        char c = iterateUntil(c1 -> c1 == ' ' || c1 == '\t' || c1 == ',' || c1 == '=');

        if (c == ',' || currentIndex == challengeLength) {
            // Scheme without any auth-params or a token68.
            // handleSchemeToken will set the state to SCHEME or CHALLENGE_START based on the next character.
            token = handleSchemeToken(start, currentIndex, c, true);
        } else if (c == '=') {
            // Auth-param for the challenge currently being parsed.
            createAuthParamToken(start, currentIndex, iterateUntilNextNonSpace());
            state = State.CHALLENGE_SEPARATOR;
            iterateUntilNextNonSpace();
        } else {
            // Can either be a new scheme or an auth-param based on the next non-space character.
            int end = currentIndex; // This end will either be the new scheme end or the auth-param key end.
            c = iterateUntilNextNonSpace();
            if (c == '=') {
                // Auth-param for the challenge currently being parsed.
                createAuthParamToken(start, end, iterateUntilNextNonSpace());
                state = State.CHALLENGE_SEPARATOR;
                iterateUntilNextNonSpace();
            } else {
                // New scheme.
                token = handleSchemeToken(start, end, c, true);
            }
        }
    }

    private static boolean isValidTokenCharacter(char c) {
        // token = 1*( ALPHA / DIGIT / ! / # / $ / % / & / ' / * / + / - / . / ^ / _ / ` / | / ~ )
        return c < 128 && VALID_TOKEN_CHARS[c];
    }

    private static boolean isValidToken(String token) {
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (!isValidTokenCharacter(c)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidToken68Character(char c) {
        // token68 = 1*( ALPHA / DIGIT / - / . / _ / ~ / + / '/' ) *"="
        return c < 128 && VALID_TOKEN68_CHARS[c];
    }

    private static String validateToken68(String challenge, int start, int end) {
        for (int i = start; i < end; i++) {
            char c = challenge.charAt(i);
            if (c == '=') {
                // From this point onwards the only valid character is '='.
                i++;
                while (i < end) {
                    c = challenge.charAt(i);
                    if (c != '=') {
                        throw LOGGER.throwableAtError()
                            .addKeyValue("challenge", challenge)
                            .addKeyValue("token68", challenge.substring(start, end))
                            .addKeyValue("character", c)
                            .addKeyValue("index", i)
                            .log("Token68 contained an invalid character.", IllegalArgumentException::new);
                    }

                    i++;
                }
            } else if (!isValidToken68Character(c)) {
                throw LOGGER.throwableAtError()
                    .addKeyValue("challenge", challenge)
                    .addKeyValue("token68", challenge.substring(start, end))
                    .addKeyValue("character", c)
                    .addKeyValue("index", i)
                    .log("Token68 contained an invalid character.", IllegalArgumentException::new);
            }
        }

        return challenge.substring(start, end);
    }

    private enum State {
        BEGINNING, CHALLENGE_SEPARATOR, SCHEME
    }

    private static class AuthenticateChallengeToken {
        final String scheme;
        final String token68;
        final Map.Entry<String, String> authParam;

        AuthenticateChallengeToken(String scheme, String token68, Map.Entry<String, String> authParam) {
            this.scheme = scheme;
            this.token68 = token68;
            this.authParam = authParam;
        }
    }
}
