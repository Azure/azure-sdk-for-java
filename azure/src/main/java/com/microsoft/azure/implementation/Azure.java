/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.Tenants;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.collection.*;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import java.io.File;
import java.io.IOException;

public final class Azure {
    private final ResourceGroups resourceGroups;
    private final ResourceManager resourceManager;
    private final StorageManager storageManager;
    private final ComputeManager computeManager;
    private final NetworkManager networkManager;
    private final String subscriptionId;
    
    public static Authenticated authenticate(ServiceClientCredentials credentials) {
        return new AuthenticatedImpl(
                AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .build());
    }

    /**
     * Authenticates API access using a properties file containing the required credentials.
     * @param credentialsFile the file containing the credentials in the standard Java properties file format,
     * with the following keys:<p>
     * <code>
     	* 	subscription= #subscription ID<br>
        * 	tenant= #tenant ID<br>
        * 	client= #client id<br>
        * 	key= #client key<br>
        * 	managementURI= #management URI<br>
        * 	baseURL= #base URL<br>
        * 	authURL= #authentication URL<br>
     *</code>
     * @return authenticated Azure client
     * @throws IOException 
     * @throws CloudException 
     */
    public static Authenticated authenticate(File credentialsFile) throws IOException {
        ApplicationTokenCredentials credentials = ApplicationTokenCredentials.fromFile(credentialsFile);
    	return new AuthenticatedImpl(AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .build()).withDefaultSubscription(credentials.defaultSubscriptionId());
    }
    
    /**
     * Authenticates API access using a {@link RestClient} instance.
     * @param restClient the {@link RestClient} configured with Azure authentication credentials
     * @return authenticated Azure client
     */
    public static Authenticated authenticate(RestClient restClient) {
        return new AuthenticatedImpl(restClient);
    }

    public static Configurable configure() {
        return new ConfigurableImpl();
    }

    public interface Configurable extends AzureConfigurable<Configurable> {
    	/**
    	 * Authenticates API access based on the provided credentials
    	 * @param credentials The credentials to authenticate API access with
    	 * @return
    	 */
        Authenticated authenticate(ServiceClientCredentials credentials);
        
        /**
         * Authenticates API access using a properties file containing the required credentials.
         * @param credentialsFile the file containing the credentials in the standard Java properties file format following
         * the same schema as {@link Azure#authenticate(File)}.<p>
         * @return Authenticated Azure client
     	 * @throws IOException 
     	 */
        Authenticated authenticate(File credentialsFile) throws IOException;
    }

    
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public Authenticated authenticate(ServiceClientCredentials credentials) {
            return Azure.authenticate(buildRestClient(credentials));
        }

		@Override
		public Authenticated authenticate(File credentialsFile) throws IOException {
			return Azure.authenticate(credentialsFile);
		}
    }

    
    /**
     * Provides authenticated access to a subset of Azure APIs that do not require a specific subscription.
     * <p>
     * To access the subscription-specific APIs, use {@link Authenticated#withSubscription(String)}, 
     * or {@link Authenticated#withDefaultSubscription()} if a default subscription has already been previously specified
     * (for example, in a previously specified authentication file).
     * @see Azure#authenticate(File)
     */
    public interface Authenticated {
        /**
         * Entry point to subscription management
         * @return Subscriptions interface providing access to subscription management
         */
        Subscriptions subscriptions();
        Tenants tenants();
        
        /**
         * Selects a specific subscription for the APIs to work with.
         * <p>
         * Most Azure APIs require a specific subscription to be selected.
         * @param subscriptionId the ID of the subscription
         * @return an authenticated Azure client configured to work with the specified subscription
         */
        Azure withSubscription(String subscriptionId);
        
        /**
         * Selects the default subscription as the subscription for the APIs to work with.
         * <p>
         * The default subscription can be specified inside the authentication file using {@link Azure#authenticate(File)}. 
         * If no default subscription has been previously provided, the first subscription as 
         * returned by {@link Authenticated#subscriptions()} will be selected.
         * @return an authenticated Azure client configured to work with the default subscription
         * @throws IOException 
         * @throws CloudException 
         */
        Azure withDefaultSubscription() throws CloudException, IOException;
    }

    private static final class AuthenticatedImpl implements Authenticated {
        final private RestClient restClient;
        final private ResourceManager.Authenticated resourceManagerAuthenticated;
        private String defaultSubscription;
        
        private AuthenticatedImpl(RestClient restClient) {
            this.resourceManagerAuthenticated = ResourceManager.authenticate(restClient);
            this.restClient = restClient;
        }

        private AuthenticatedImpl withDefaultSubscription(String subscriptionId) throws IOException {
        	this.defaultSubscription = subscriptionId;
        	return this;
        }
        
        @Override
        public Subscriptions subscriptions() {
            return resourceManagerAuthenticated.subscriptions();
        }

        @Override
        public Tenants tenants() {
            return resourceManagerAuthenticated.tenants();
        }

        @Override
        public Azure withSubscription(String subscriptionId) {
            return new Azure(restClient, subscriptionId);
        }

		@Override
		public Azure withDefaultSubscription() throws CloudException, IOException {
		    if(this.defaultSubscription != null) {
		        return withSubscription(this.defaultSubscription);
		    } else {
		        PagedList<Subscription> subs = this.subscriptions().list();
		        if(!subs.isEmpty()) {
		            return withSubscription(subs.get(0).subscriptionId());
		        } else {
		            return withSubscription(null);
		        }
		    }
		}
    }

    public interface ResourceGroups extends SupportsListing<Azure.ResourceGroup>,
            SupportsGettingByName<Azure.ResourceGroup>,
            SupportsCreating<Azure.ResourceGroup.DefinitionBlank>,
            SupportsDeleting {
    }

    public interface ResourceGroup extends com.microsoft.azure.management.resources.ResourceGroup {
    }

    private Azure(RestClient restClient, String subscriptionId) {
        ResourceManagementClientImpl resourceManagementClient = new ResourceManagementClientImpl(restClient);
        resourceManagementClient.withSubscriptionId(subscriptionId);
        this.resourceGroups = new AzureResourceGroupsImpl(resourceManagementClient);
        this.resourceManager = ResourceManager.authenticate(restClient).withSubscription(subscriptionId);
        this.storageManager = StorageManager.authenticate(restClient, subscriptionId);
        this.computeManager = ComputeManager.authenticate(restClient, subscriptionId);
        this.networkManager = NetworkManager.authenticate(restClient, subscriptionId);
        this.subscriptionId = subscriptionId;
    }

    /**
     * @return the currently selected subscription ID this client is configured to work with
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }
    
    /**
     * @return entry point to managing resource groups
     */
    public ResourceGroups resourceGroups() {
        return resourceGroups;
    }

    public GenericResources genericResources() {
        return resourceManager.genericResources();
    }

    /**
     * @return entry point to managing storage accounts
     */
    public StorageAccounts storageAccounts() {
        return storageManager.storageAccounts();
    }

    public Usages storageUsages() {
        return storageManager.usages();
    }

    /**
     * @return entry point to managing availability sets.
     */
    public AvailabilitySets availabilitySets() {
        return computeManager.availabilitySets();
    }

    /**
     * @return entry point to managing virtual networks
     */
    public Networks networks() {
        return networkManager.networks();
    }

    /** 
     * @return entry point to managing virtual machines
     */
    public VirtualMachines virtualMachines() {
        return computeManager.virtualMachines();
    }
    
    /**
     * @return entry point to managing public IP addresses
     */
    public PublicIpAddresses publicIpAddresses() {
    	return this.networkManager.publicIpAddresses();
    }
}
