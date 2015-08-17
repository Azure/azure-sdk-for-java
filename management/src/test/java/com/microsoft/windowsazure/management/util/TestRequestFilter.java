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
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;

public class TestRequestFilter implements ServiceRequestFilter
{
    private int called;
    private String identifier;
    
    public int getCalled() { return this.called; }
    
    public String getIdentifier() { return this.identifier; }
    
    public TestRequestFilter(String identifier)
    {
        this.called = 0;
        this.identifier = identifier;
    }
    
    public TestRequestFilter()
    {
        this.called = 0;
    }
    
    @Override
    public void filter (ServiceRequestContext request)
    {
        if (request.getProperty("executed") == null)
        {
            request.setProperty("executed", true);

            called++;
        }
    }
}