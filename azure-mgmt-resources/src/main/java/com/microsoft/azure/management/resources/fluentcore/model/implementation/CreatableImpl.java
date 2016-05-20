/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
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
