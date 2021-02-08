// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.models.RecordingRedactor;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test for {@link RecordingRedactor} that redact the sensitive information while recording.
 */
public class RecordingRedactorTests {
    private static final String DUMMY_SENSITIVE_INFORMATION = "sensitiveInformation";

    // Non-sensitive content data, keep the content as it is
    private static final String NON_SENSITIVE_DATA_CONTENT = "\"Response\" : {\n"
        + "      \"Body\" : \"{\"a\":\"a\",\"expirationDateTimeTicks\":637270217074441783}\",\n"
        + "    },";

    // Access token value pair at Body
    private static final String ACCESS_TOKEN_FIRST_PAIR = "\"Response\" : {\n"
        + "      \"Body\" : \"{\"accessToken\":\"sensitiveData\",\"expirationDateTimeTicks\":637270217074441783}\",\n"
        + "    },";
    private static final String EXPECTED_ACCESS_TOKEN_FIRST_PAIR_REDACTED = "\"Response\" : {\n"
        + "      \"Body\" : \"{\"accessToken\":\"REDACTED\",\"expirationDateTimeTicks\":637270217074441783}\",\n"
        + "    },";

    // Access token pair at the end of Body
    private static final String ACCESS_TOKEN_LAST_PAIR = "\"Response\" : {\n"
        + "      \"Body\" : \"{\"accessToken\":\"sensitiveData\"}\",\n"
        + "    },";
    private static final String EXPECTED_ACCESS_TOKEN_LAST_PAIR_REDACTED = "\"Response\" : {\n"
        + "      \"Body\" : \"{\"accessToken\":\"REDACTED\"}\",\n"
        + "    },";

    // User delegation key: <Value> XML tag
    private static final String USER_DELEGATION_KEY_FOR_VALUE_RESPONSE = "\"Response\" : {\n"
        + "   \"Body\" : <UserDelegationKey><Value>sensitiveInformation=</Value></UserDelegationKey>\",\n"
        + "    },";

    private static final String EXPECTED_USER_DELEGATION_KEY_FOR_VALUE_RESPONSE_REDACTED = "\"Response\" : {\n"
        + "   \"Body\" : <UserDelegationKey><Value>UkVEQUNURUQ=</Value></UserDelegationKey>\",\n"
        + "    },";

    // User delegation key: <SignedOid> XML tag
    private static final String USER_DELEGATION_KEY_FOR_SIGNED_OID_RESPONSE = "\"Response\" : {\n"
        + "   \"Body\" : <UserDelegationKey><SignedOid>sensitiveInformation=</SignedOid></UserDelegationKey>\",\n"
        + "    },";

    // User delegation key: <SignedTid> XML tag
    private static final String USER_DELEGATION_KEY_FOR_SIGNED_TID_RESPONSE = "\"Response\" : {\n"
        + "   \"Body\" : <UserDelegationKey><SignedTid>sensitiveInformation</SignedTid></UserDelegationKey>\",\n"
        + "    },";

    private static final String CONNECTION_STRING_RESPONSE_BODY = "\"dataSourceParameter\":{\"connectionString"
        + "\":\"Server=test-sample.db.windows.net,1433;Initial =sample;MultipleActive;Encrypt=True;Connection "
        + "Timeout=30;\",\"query\":\"select * from adsample2 where Timestamp = @StartTime\"";

    private static final String REDACTED_CONNECTION_STRING_RESPONSE = "\"dataSourceParameter"
        + "\":{\"connectionString\":\"REDACTED\",\"query\":\"select * from adsample2 where Timestamp = @StartTime\"";

    private static final String USERNAME_RESPONSE_BODY = "\"dataSourceParameter\":{\"query"
        + "\":\"select * from adsample2 where Timestamp = @StartTime\";User ID=testUser1;\"userName\":\"testUser\"}";

    private static final String REDACTED_USERNAME_RESPONSE = "\"dataSourceParameter\":{\"query"
        + "\":\"select * from adsample2 where Timestamp = @StartTime\";User ID=REDACTED;\"userName\":\"REDACTED\"}";

    private static final String PASSWORD_RESPONSE_BODY = "\"dataSourceParameter\":{\"query"
        + "\":\"select * from adsample2 where Timestamp = @StartTime\";\"password\":\"testUserPwd\",Password=testPWD;";

    private static final String REDACTED_PASSWORD_RESPONSE_BODY = "\"dataSourceParameter\":{\"query"
        + "\":\"select * from adsample2 where Timestamp = @StartTime\";\"password\":\"REDACTED\",Password=REDACTED;";

    private static final String APPLICATION_ID_RESPONSE_BODY = "\"dataSourceParameter\":{\"query"
        + "\":\"select * from adsample2 where Timestamp = @StartTime\";\"applicationId\":\"app_insights_app_id\"}";

    private static final String REDACTED_APPLICATION_ID_RESPONSE_BODY = "\"dataSourceParameter\":{\"query"
        + "\":\"select * from adsample2 where Timestamp = @StartTime\";\"applicationId\":\"REDACTED\"}";

    private static final String API_ID_RESPONSE_BODY = "\"dataSourceParameter\":{\"apiKey\":\"api_key"
        + "\",;\"applicationId\":\"APP_INSIGHT_APP_ID\"}";

    private static final String REDACTED_API_ID_RESPONSE_BODY = "\"dataSourceParameter\":{\"apiKey\":\"REDACTED"
        + "\",;\"applicationId\":\"REDACTED\"}";

    private static final String HTTP_URL_RESPONSE_BODY = "\"dataSourceParameter\":{\"httpMethod\":\"GET\","
        + "\"url\":\"http://url\"}";

    private static final String REDACTED_HTTP_URL_RESPONSE_BODY = "\"dataSourceParameter\":{\"httpMethod\":\"GET\","
        + "\"url\":\"REDACTED\"}";;

    private static final String HOST_RESPONSE_BODY = "\"dataSourceParameter\":{\"port\":\"9200\","
        + "\"query\":\"select * from adsample2 where Timestamp = @StartTime\",\"host\":\"host.azure.com\"}";

    private static final String REDACTED_HOST_RESPONSE_BODY = "\"dataSourceParameter\":{\"port\":\"9200\","
        + "\"query\":\"select * from adsample2 where Timestamp = @StartTime\",\"host\":\"REDACTED\"}";

    private static final String ACCOUNT_NAME_RESPONSE_BODY = "\"dataSourceParameter\":{\"fileTemplate\":\"adsample"
        + ".json\",\"accountName\":\"sampleAccountName\",\"directoryTemplate\":\"%Y/%m/%d\"}";

    private static final String REDACTED_ACCOUNT_NAME_RESPONSE_BODY = "\"dataSourceParameter\":{\"fileTemplate"
        + "\":\"adsample.json\",\"accountName\":\"REDACTED\",\"directoryTemplate\":\"%Y/%m/%d\"}";

    private static final String ACCOUNT_KEY_RESPONSE_BODY = "\"dataSourceParameter\":{\"fileSystemName"
        + "\":\"adsample\",\"accountKey\":\"Pi5n+zrzyZZEtSjFoamPe622ZsmwiOZzdPvRKpvmuzxSC+tA==\"}";

    private static final String REDACTED_ACCOUNT_KEY_RESPONSE_BODY = "\"dataSourceParameter\":{\"fileSystemName"
        + "\":\"adsample\",\"accountKey\":\"REDACTED\"}";

    private static final String AUTH_HEADER_RESPONSE_BODY = "\"dataSourceParameter\":"
        + "{\"authHeader\":\"XYZ\",\"port\":\"9200\"";

    private static final String REDACTED_AUTH_HEADER_RESPONSE_BODY = "\"dataSourceParameter\":"
        + "{\"authHeader\":\"REDACTED\",\"port\":\"9200\"";

    private static final String EMPTY_KEY_RESPONSE_BODY = "\"dataSourceParameter\":"
        + "{\"username\":\"\",\"port\":\"9200\"";

    private static final String REDACTED_EMPTY_KEY_RESPONSE_BODY = "\"dataSourceParameter\":"
        + "{\"username\":\"\",\"port\":\"9200\"";

    private static final String PII_RESPONSE_DATA = "\"name\":\"Foo\", \"ssn\":\"123-45-6789\"";
    private static final String REDACTED_PII_RESPONSE_DATA = "\"name\":\"Foo\", \"ssn\":\"REDACTED\"";

    /**
     * Verify if the given content is redacted successfully.
     */
    @ParameterizedTest
    @MethodSource("sensitiveDataSupplier")
    public void redactSensitiveContent(String sensitiveData, String redactedContent) {
        assertEquals(redactedContent, new RecordingRedactor().redact(sensitiveData));
    }

    /**
     * Verify if the value in the XML tag {@code <SignedOid>} is redacted successfully.
     */
    @Test
    public void replaceUserDelegationKeyForSignedOidTag() {
        assertFalse(new RecordingRedactor().redact(USER_DELEGATION_KEY_FOR_SIGNED_OID_RESPONSE).contains(DUMMY_SENSITIVE_INFORMATION));
    }

    /**
     * Verify if the value in the XML tag {@code <SignedTid>} is redacted successfully.
     */
    @Test
    public void replaceUserDelegationKeyForSignedTidTag() {
        assertFalse(new RecordingRedactor().redact(USER_DELEGATION_KEY_FOR_SIGNED_TID_RESPONSE).contains(DUMMY_SENSITIVE_INFORMATION));
    }

    @Test
    public void customRedactor() {
        Pattern pattern = Pattern.compile("(.*\"ssn\":)(\"[-0-9]{11}\")");
        List<Function<String, String>> redactors = new ArrayList<>();
        redactors.add(data -> redact(data, pattern.matcher(data), "\"REDACTED\""));
        RecordingRedactor recordingRedactor = new RecordingRedactor(redactors);
        assertEquals(recordingRedactor.redact(PII_RESPONSE_DATA), REDACTED_PII_RESPONSE_DATA);
    }

    private String redact(String content, Matcher matcher, String replacement) {
        while (matcher.find()) {
            String captureGroup = matcher.group(2);
            if (!CoreUtils.isNullOrEmpty(captureGroup)) {
                content = content.replace(matcher.group(2), replacement);
            }
        }
        return content;
    }

    private static Stream<Arguments> sensitiveDataSupplier() {
        return Stream.of(
            Arguments.of(NON_SENSITIVE_DATA_CONTENT, NON_SENSITIVE_DATA_CONTENT),
            Arguments.of(API_ID_RESPONSE_BODY, REDACTED_API_ID_RESPONSE_BODY),
            Arguments.of(AUTH_HEADER_RESPONSE_BODY, REDACTED_AUTH_HEADER_RESPONSE_BODY),
            Arguments.of(ACCOUNT_NAME_RESPONSE_BODY, REDACTED_ACCOUNT_NAME_RESPONSE_BODY),
            Arguments.of(ACCOUNT_KEY_RESPONSE_BODY, REDACTED_ACCOUNT_KEY_RESPONSE_BODY),
            Arguments.of(ACCESS_TOKEN_LAST_PAIR, EXPECTED_ACCESS_TOKEN_LAST_PAIR_REDACTED),
            Arguments.of(ACCESS_TOKEN_FIRST_PAIR, EXPECTED_ACCESS_TOKEN_FIRST_PAIR_REDACTED),
            Arguments.of(APPLICATION_ID_RESPONSE_BODY, REDACTED_APPLICATION_ID_RESPONSE_BODY),
            Arguments.of(CONNECTION_STRING_RESPONSE_BODY, REDACTED_CONNECTION_STRING_RESPONSE),
            Arguments.of(HTTP_URL_RESPONSE_BODY, REDACTED_HTTP_URL_RESPONSE_BODY),
            Arguments.of(HOST_RESPONSE_BODY, REDACTED_HOST_RESPONSE_BODY),
            Arguments.of(USER_DELEGATION_KEY_FOR_VALUE_RESPONSE, EXPECTED_USER_DELEGATION_KEY_FOR_VALUE_RESPONSE_REDACTED),
            Arguments.of(PASSWORD_RESPONSE_BODY, REDACTED_PASSWORD_RESPONSE_BODY),
            Arguments.of(USERNAME_RESPONSE_BODY, REDACTED_USERNAME_RESPONSE),
            Arguments.of(REDACTED_EMPTY_KEY_RESPONSE_BODY, EMPTY_KEY_RESPONSE_BODY),
            Arguments.of(NON_SENSITIVE_DATA_CONTENT, NON_SENSITIVE_DATA_CONTENT)
        );
    }


    /**
     * Verify if the value in the json key {@code httpUrl}  is redacted successfully.
     */
    @Test
    public void replaceUrlKey() {
        assertEquals(REDACTED_ACCOUNT_KEY_RESPONSE_BODY, new RecordingRedactor().redact(ACCOUNT_KEY_RESPONSE_BODY));
        assertEquals(REDACTED_AUTH_HEADER_RESPONSE_BODY, new RecordingRedactor().redact(AUTH_HEADER_RESPONSE_BODY));
    }
}
