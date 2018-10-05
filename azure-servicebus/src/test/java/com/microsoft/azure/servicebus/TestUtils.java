package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.StringUtil;
import com.microsoft.azure.servicebus.primitives.Util;

public class TestUtils {
	
	private static final String NAMESPACE_CONNECTION_STRING_ENVIRONMENT_VARIABLE_NAME = "AZURE_SERVICEBUS_JAVA_CLIENT_TEST_CONNECTION_STRING";
    public static final String FIRST_SUBSCRIPTION_NAME = "subscription1";

    private static final String RUN_WITH_PROXY_ENV_VAR = "RUN_WITH_PROXY";
    private static final String PROXY_HOSTNAME_ENV_VAR = "PROXY_HOSTNAME";
    private static final String PROXY_PORT_ENV_VAR = "PROXY_PORT";

	private static String namespaceConnectionString;
	private static ConnectionStringBuilder namespaceConnectionStringBuilder;

	private static Boolean runWithProxy;
	private static String proxyHostName;
	private static int proxyPort;
	
	static
	{
		// Read connection string
        namespaceConnectionString = System.getenv(NAMESPACE_CONNECTION_STRING_ENVIRONMENT_VARIABLE_NAME);
		if(namespaceConnectionString == null || namespaceConnectionString.isEmpty())
		{			
			System.err.println(NAMESPACE_CONNECTION_STRING_ENVIRONMENT_VARIABLE_NAME + " environment variable not set. Tests will not be able to connect to to any service bus entity.");
		}
		namespaceConnectionStringBuilder = new ConnectionStringBuilder(namespaceConnectionString);

		// Read proxy settings only if transport type is WebSockets
        runWithProxy = Boolean.valueOf(System.getenv(RUN_WITH_PROXY_ENV_VAR));
        proxyHostName = System.getenv(PROXY_HOSTNAME_ENV_VAR);
        proxyPort = System.getenv(PROXY_PORT_ENV_VAR) == null ?
                        0 : Integer.valueOf(System.getenv(PROXY_PORT_ENV_VAR));
	}
	
	public static URI getNamespaceEndpointURI()
    {
        return namespaceConnectionStringBuilder.getEndpoint();
    }

    public static String getNamespaceConnectionString() { return namespaceConnectionString; }

    public static int getProxyPort() { return proxyPort; }

    public static Boolean isProxyEnabled() { return !StringUtil.isNullOrEmpty(proxyHostName) && proxyPort != 0; }

    public static ClientSettings getClientSettings() {
        if (runWithProxy) {
            setDefaultProxySelector();
        }
        return Util.getClientSettingsFromConnectionStringBuilder(namespaceConnectionStringBuilder);
    }
    
    // AADTokens cannot yet be used for management operations, sent directly to gateway
    public static ClientSettings getManagementClientSettings()
    {
        return Util.getClientSettingsFromConnectionStringBuilder(namespaceConnectionStringBuilder);
    }

    private static void setDefaultProxySelector() {
	    // TODO: this needs a less confusing name (not "set") as it doesn't allow the caller to set the default
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                List<Proxy> proxies = new LinkedList<>();
                proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHostName, proxyPort)));
                return proxies;
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                // no-op
            }
        });
    }

	public static String randomizeEntityName(String entityName)
	{
	    return entityName + getRandomString();
	}

    public static String getRandomString()
    {
    	return UUID.randomUUID().toString();
    }
    
    /**
     * Tells this class whether to create an entity for every test and delete it after the test. Creating an entity for every test makes the tests independent of 
     * each other and advisable if the SB namespace allows it. If the namespace doesn't allow creation and deletion of many entities in a short span of time, the suite
     * will create one entity at the start, uses it for all test and deletes the entity at the end.
     * @return true if each test should create and delete its own entity. Else return false.
     */
    public static boolean shouldCreateEntityForEveryTest()
    {
        return true;
    }
}
