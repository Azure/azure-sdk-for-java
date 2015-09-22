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

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;

import java.util.concurrent.ExecutorService;

public class AdalAuthFilter implements ServiceRequestFilter {
	private String token;
	
	public AdalAuthFilter(String token) {
		this.token = token;
	}
	
	@Override
	public void filter(ServiceRequestContext request) {
    	ExecutorService service = null;

		try {
            request.setHeader("Authorization", "Bearer " + token);
		} catch (Exception e) {
            // silently fail
        } finally {
        	if (service != null) {
        		service.shutdown();
        	}
        }
	}
}