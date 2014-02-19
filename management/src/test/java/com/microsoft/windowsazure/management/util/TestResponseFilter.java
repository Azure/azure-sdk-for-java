/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.microsoft.windowsazure.management.util;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;

public class TestResponseFilter implements ServiceResponseFilter
{
    private int called;
    private String identifier;
    
    public int getCalled() { return this.called; }
    
    public String getIdentifier() { return this.identifier; }
    
    public TestResponseFilter(String identifier)
    {
        this.called = 0;
        this.identifier = identifier;
    }
    
    public TestResponseFilter()
    {
        this.called = 0;
    }
    
    @Override
    public void filter (ServiceRequestContext request, ServiceResponseContext response)
    {
        if (response.getProperty("executed") == null)
        {
            response.setProperty("executed", true);

            called++;
        }
    }
}