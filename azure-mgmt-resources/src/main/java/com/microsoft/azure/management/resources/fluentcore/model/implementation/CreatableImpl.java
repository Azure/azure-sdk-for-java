/**
* Copyright (c) Microsoft Corporation
* 
* All rights reserved. 
* 
* MIT License
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
* (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
* subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
* ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
* THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

import java.util.HashMap;
import java.util.Map;

public abstract class CreatableImpl<FluentModelT, InnerModelT>
        extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>
        implements Creatable<FluentModelT> {
    private Map<String, Creatable<?>> prerequisites;
    private Map<String, Creatable<?>> provisioned;

	protected CreatableImpl(String name, InnerModelT innerObject) {
		super(name, innerObject);
        prerequisites = new HashMap<>();
        provisioned = new HashMap<>();
	}

    @Override
    public Map<String, Creatable<?>> prerequisites() {
        return prerequisites;
    }

    @Override
    public Map<String, Creatable<?>> created() {
        return provisioned;
    }

    // TODO: Add provisicreatingoning() to allow unblocking
}
