/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.network.implementation.api.SubnetInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of a subnet of a virtual network.
 */
public interface Subnet extends
    Wrapper<SubnetInner>,
    ChildResource {

    /**
     * @return the address space prefix, in CIDR notation, assigned to this subnet
     */
    String addressPrefix();
    //TODO: String networkSecurityGroup();

    /**
     * The entirety of a Subnet definition.
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionBlank<ParentT>,
        DefinitionAttachable<ParentT> {
    }

    /**
     * The first stage of the subnet definition.
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionBlank<ParentT> {
        DefinitionAttachable<ParentT> withAddressPrefix(String cidr);
    }

    /** The final stage of the subnet definition.
     * <p>
     * At this stage, any remaining optional settings can be specified, or the subnet definition
     * can be attached to the parent virtual network definition using {@link DefinitionAttachable#attach()}.
     * @param <ParentT> the return type of {@link DefinitionAttachable#attach()}
     */
    interface DefinitionAttachable<ParentT> extends
        Attachable<ParentT> { //TODO: WithNSG
    }
}
