/**
 * Copyright	 (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NewChildModel;
import com.microsoft.azure.management.network.NewTopLevelModel;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for Subnet and its create and update interfaces.
 */
@LangDefinition
class NewChildModelImpl
    extends ChildResourceImpl<SubnetInner, NewTopLevelModelImpl, NewTopLevelModel>
    implements
        NewChildModel,
        NewChildModel.Definition<NewTopLevelModel.DefinitionStages.WithCreate>,
        NewChildModel.UpdateDefinition<NewTopLevelModel.Update>,
        NewChildModel.Update {

    NewChildModelImpl(SubnetInner inner, NewTopLevelModelImpl parent) {
        super(inner, parent);
    }

    // Getters
    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String addressPrefix() {
        return this.inner().addressPrefix();
    }

    // Fluent setters
    @Override
    public NewChildModelImpl withAddressPrefix(String cidr) {
        this.inner().withAddressPrefix(cidr);
        return this;
    }

    // Verbs

    @Override
    public NewTopLevelModelImpl attach() {
        return this.parent().withNewChildModel(this);
    }
}
