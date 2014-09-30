/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.credentials;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;

public class AdalAuthFilter implements ServiceRequestFilter {
	private AdalAuthConfig adalAuthConfig;
	
	public AdalAuthFilter(AdalAuthConfig adalAuthConfig) {
		this.adalAuthConfig = adalAuthConfig;
	}
	
	@Override
	public void filter(ServiceRequestContext request) {
    	ExecutorService service = null;

		try {
			service = Executors.newFixedThreadPool(1);
	    	AuthenticationContext authenticationContext = new AuthenticationContext(
				this.adalAuthConfig.getAuthorityUrl() + this.adalAuthConfig.getTenantId() + "/",
				true,
		        service);
            Future<AuthenticationResult> future = authenticationContext.acquireToken(
                    this.adalAuthConfig.getResource(),
                    new ClientCredential(this.adalAuthConfig.getClientId(),
                            this.adalAuthConfig.getClientSecret()), null);
            AuthenticationResult result = future.get();
            
            // TODO: missing a step here. Need to get access token from soemthing else
            
            request.setHeader("Authorization", "Bearer " + result.getAccessToken());
		} catch (InterruptedException e) {
			// Do nothing.
		} catch (ExecutionException e) {
			// Do nothing.
		} catch (MalformedURLException e) {
        	// Do nothing.
		} finally {
        	if (service != null) {
        		service.shutdown();
        	}
        }
	}
}