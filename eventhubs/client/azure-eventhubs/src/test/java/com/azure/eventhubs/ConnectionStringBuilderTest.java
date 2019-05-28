package com.azure.eventhubs;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.function.Consumer;

@RunWith(Theories.class)
public class ConnectionStringBuilderTest {

    private static final String END_POINT_FORMAT = "sb://%s.%s";
    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String DEFAULT_DOMAIN_NAME = "servicebus.windows.net/";
    private static final String ENTITY_PATH = "dummyEntityPath";
    private static final String SHARED_ACCESS_KEY_NAME = "dummySasKeyName";
    private static final String SHARED_ACCESS_KEY = "dummySasKey";
    private static final String END_POINT_STR = getURI(END_POINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME).toString();

    private static final String correctConnStr = String.format("Endpoint=%s;EntityPath=%s;SharedAccessKeyName=%s;SharedAccessKey=%s",
        END_POINT_STR, ENTITY_PATH, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY);
    private static final Consumer<ConnectionStringBuilder> validateConnStr = connStrBuilder -> {
        Assert.assertTrue(ENTITY_PATH.equals(connStrBuilder.eventHubName()));
        Assert.assertTrue(END_POINT_STR.equals(connStrBuilder.endpoint().toString()));
        Assert.assertTrue(SHARED_ACCESS_KEY.equals(connStrBuilder.sasKey()));
        Assert.assertTrue(SHARED_ACCESS_KEY_NAME.equals(connStrBuilder.sasKeyName()));
    };

    private static URI getURI(String endpointFormat, String namespaceName, String domainName) {
        try {
            return new URI(String.format(Locale.US, endpointFormat, namespaceName, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Invalid namespace name: %s", namespaceName), exception);
        }
    }

    @DataPoints
    public static String[] connStrs() {
        return new String[]{null, "", "wrongConnStr"};
    }

    @Theory
    @Test(expected = IllegalArgumentException.class)
    public void invalidConnStrBuilder(String connStr) {
        new ConnectionStringBuilder(connStr);
    }

    @Test
    public void exchangeConnStrWithEndpoint() {
        final ConnectionStringBuilder firstConnStrBuilder = new ConnectionStringBuilder(correctConnStr);
        final ConnectionStringBuilder secondConnStrBuilder = new ConnectionStringBuilder()
            .endpoint(firstConnStrBuilder.endpoint())
            .sasKey(firstConnStrBuilder.sasKey())
            .sasKeyName(firstConnStrBuilder.sasKeyName())
            .eventHubName(firstConnStrBuilder.eventHubName());
        validateConnStr.accept(new ConnectionStringBuilder(secondConnStrBuilder.toString()));
    }

    @Test
    public void exchangeConnStrWithNamespace() {
        final ConnectionStringBuilder firstConnStrBuilder = new ConnectionStringBuilder(correctConnStr);
        final ConnectionStringBuilder secondConnStrBuilder = new ConnectionStringBuilder()
            .namespaceName(firstConnStrBuilder.namespaceName())
            .sasKey(firstConnStrBuilder.sasKey())
            .sasKeyName(firstConnStrBuilder.sasKeyName())
            .eventHubName(firstConnStrBuilder.eventHubName());
        validateConnStr.accept(new ConnectionStringBuilder(secondConnStrBuilder.toString()));
    }

    @Test
    public void parseValidConnStr() {
        final ConnectionStringBuilder connStrBuilder = new ConnectionStringBuilder(correctConnStr);
        validateConnStr.accept(connStrBuilder);
    }
}
