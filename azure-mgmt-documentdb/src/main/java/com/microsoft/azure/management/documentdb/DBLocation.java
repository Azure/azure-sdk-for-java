/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.documentdb;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * An immutable client-side representation of an Azure document db location.
 */
@Fluent
public interface DBLocation extends
    ChildResource<DatabaseAccount>,
    HasInner<Location> {

    /**
     * @return the connection endpoint for the specific region
     */
    String documentEndpoint();

    /**
     * @return the failover priority of the region
     */
    int failoverPriority();



    /**
     * The entirety of a document db location definition as a part of a parent definition.
     *
     * @param <ParentT>  the stage of the document db definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends
            DBLocation.DefinitionStages.WithAttach<ParentT>,
            DBLocation.DefinitionStages.Blank<ParentT> {
    }

    /**
     * Grouping of document db location definition stages as a part of parent container service definition.
     */
    interface DefinitionStages {

        /** The final stage of a document db location definition.
         * At this stage, any remaining optional settings can be specified, or the document db location
         * can be attached to the parent document db definition.
         * @param <ParentT> the stage of the document db location definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT> {
        }

        /**
         * The first stage of a document db location definition.
         * @param <ParentT>  the stage of the document db location definition to return to after attaching this definition
         */
        interface Blank<ParentT> {
            /**
             * The failover priority of the region. A failover priority of 0 indicates a write region.
             * The maximum value for a failover priority = (total number of regions - 1).
             * Failover priority values must be unique for each of the regions in which the database account exists.
             * @param failoverPriority the failover priority
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFailoverPriority(int failoverPriority);
        }
    }

    /**
     * The entirety of a document db location definition as a part of a parent definition.
     * @param <ParentT>  the stage of the document db location definition to return to after attaching this definition
     */
    interface Update<ParentT> extends
            DBLocation.UpdateStages.WithAttach<ParentT>,
            DBLocation.UpdateStages.WithFailoverPriority<ParentT> {
    }

    /**
     * Grouping of document db location definition stages as a part of parent document db definition.
     */
    interface UpdateStages {

        /** The final stage of a document db location update.
         * @param <ParentT> the stage of the document db location definition to return to after attaching this update
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT> {
        }

        /** The stage of a document db location update allowing the updating of the failover priority.
         * @param <ParentT> the stage of the document db location update to return to after attaching this update
         */
        interface WithFailoverPriority<ParentT> {
            /**
             * The failover priority of the region. A failover priority of 0 indicates a write region.
             * The maximum value for a failover priority = (total number of regions - 1).
             * Failover priority values must be unique for each of the regions in which the database account exists.
             * @param failoverPriority the failover priority
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFailoverPriority(int failoverPriority);
        }
    }
}
