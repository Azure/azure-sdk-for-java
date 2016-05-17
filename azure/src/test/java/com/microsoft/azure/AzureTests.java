package com.microsoft.azure;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.implementation.Azure;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.api.AccountType;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class AzureTests {
    private static final ServiceClientCredentials credentials = new ApplicationTokenCredentials(
            System.getenv("client-id"),
            System.getenv("domain"),
            System.getenv("secret"),
            AzureEnvironment.AZURE);
    private static final String subscriptionId = System.getenv("subscription-id");
    private Subscriptions subscriptions;
    private Azure azure, azure2;

    public static void main(String[] args) throws IOException, CloudException {
    	final File credFile = new File("azureauth.properties");
    	Azure azure = Azure.authenticate(credFile)
    		.withDefaultSubscription();
    	System.out.println(String.valueOf(azure.resourceGroups().list().size()));
    	
    	Azure.configure().withLogLevel(Level.BASIC).authenticate(credFile);
    	System.out.println(String.valueOf(azure.resourceGroups().list().size()));
    }
    
    @Before
    public void setup() throws Exception {
        // Authenticate based on credentials instance
    	Azure.Authenticated azureAuthed = Azure.configure()
                .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                .withUserAgent("AzureTests")
                .authenticate(credentials);

        subscriptions = azureAuthed.subscriptions();
        azure = azureAuthed.withSubscription(subscriptionId);
        
        // Authenticate based on file
    	this.azure2 = Azure.authenticate(new File("my.auth"))
        	.withDefaultSubscription();
    }

    /**
     * Tests the Public IP Address implementation
     * @throws Exception
     */
    @Test public void testPublicIpAddresses() throws Exception {
    	// Verify creation of a new public IP address 
    	String suffix = String.valueOf(System.currentTimeMillis());
    	String newPipName = "pip" + suffix;
    	PublicIpAddress pip = azure2.publicIpAddresses().define(newPipName)
    		.withRegion(Region.US_WEST)
    		.withNewGroup()
    		.withDynamicIp()
    		.withLeafDomainLabel(newPipName)
    		.create();
    	
    	// Verify list
    	int publicIpAddressCount = azure2.publicIpAddresses().list().size();
    	System.out.println(publicIpAddressCount);
    	Assert.assertTrue(0 < publicIpAddressCount);
    	
    	// Verify get
    	String resourceGroupName = pip.resourceGroupName();
    	pip = azure2.publicIpAddresses().get(resourceGroupName, newPipName);
    	Assert.assertTrue(pip.name().equalsIgnoreCase(newPipName));
    	printPublicIpAddress(pip);
    	
    	// Verify update
    	pip = pip.update()
    		.withStaticIp()
    		.withLeafDomainLabel(newPipName + "xx")
    		.withReverseFqdn(pip.leafDomainLabel() + "." + pip.region() + ".cloudapp.azure.com")
    		.apply();
    	printPublicIpAddress(pip);
    	pip = azure2.publicIpAddresses().get(pip.id());
    	printPublicIpAddress(pip);    	
    	
    	// Verify delete
    	azure2.publicIpAddresses().delete(pip.id());
    	azure2.resourceGroups().delete(resourceGroupName);
    	azure2.resourceGroups().delete(pip.resourceGroupName());
    }
    
    private void printPublicIpAddress(PublicIpAddress pip) {
    	System.out.println(new StringBuilder().append("Public IP Address: ").append(pip.id())
    			.append("\n\tIP Address: ").append(pip.ipAddress())
    			.append("\n\tLeaf domain label: ").append(pip.leafDomainLabel())
    			.append("\n\tResource group: ").append(pip.resourceGroupName())
    			.append("\n\tFQDN: ").append(pip.fqdn())
    			.append("\n\tReverse FQDN: ").append(pip.reverseFqdn())
    			.toString());
    }
    
    
    @Test
    public void listSubscriptions() throws Exception {
        Assert.assertTrue(0 < subscriptions.list().size());
    }

    @Test
    public void listResourceGroups() throws Exception {
        Assert.assertTrue(0 < azure.resourceGroups().list().size());
    }

    @Test
    public void listStorageAccounts() throws Exception {
        Assert.assertTrue(0 < azure.storageAccounts().list().size());
    }
    
    @Test
    public void createStorageAccount() throws Exception {
        StorageAccount storageAccount = azure.storageAccounts().define("my-stg1")
                .withRegion(Region.ASIA_EAST)
                .withNewGroup()
                .withAccountType(AccountType.PREMIUM_LRS)
                .create();

        Assert.assertSame(storageAccount.name(), "my-stg1");
    }

    @Test
    public void createStorageAccountInResourceGroupContext() throws Exception {
        StorageAccount storageAccount = azure.resourceGroups().get("my-grp")
                .storageAccounts()
                .define("my-stg2")
                .withAccountType(AccountType.PREMIUM_LRS)
                .create();

        Assert.assertSame(storageAccount.name(), "my-stg2");
    }
}
