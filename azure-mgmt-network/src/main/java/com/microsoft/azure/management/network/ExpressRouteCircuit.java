/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ExpressRouteCircuitInner;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * Entry point for Express Route Circuit management API in Azure.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_4_0)
public interface ExpressRouteCircuit extends
        GroupableResource<NetworkManager, ExpressRouteCircuitInner>,
        Refreshable<ExpressRouteCircuit>,
        Updatable<ExpressRouteCircuit.Update> {

        // Actions


        // Getters

        /**
         * The entirety of the express route circuit definition.
         */
        interface Definition extends
                DefinitionStages.Blank,
                DefinitionStages.WithGroup,
                DefinitionStages.WithSku,
                DefinitionStages.WithCreate {
        }

        /**
         * Grouping of express route circuit definition stages.
         */
        interface DefinitionStages {
            /**
             * The first stage of express route circuit definition.
             */
            interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
            }

            /**
             * The stage of express route circuit definition allowing to specify the resource group.
             */
            interface WithGroup
                    extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithCreate> {
            }

            /**
             * The stage of express route circuit definition allowing to specify SKU.
             */
            interface WithSku {
                WithCreate withSku(ExpressRouteCircuitSku skuName);
            }

            /**
             * The stage of the express route circuit definition which contains all the minimum required inputs for
             * the resource to be created, but also allows
             * for any other optional settings to be specified.
             */
            interface WithCreate extends
                    Creatable<ExpressRouteCircuit>,
                    Resource.DefinitionWithTags<WithCreate> {
            }
        }

        /**
         * Grouping of express route circuit update stages.
         */
        interface UpdateStages {
            /**
             * The stage of express route circuit update allowing to change SKU.
             */
            interface WithSku {
                Update withSku(ExpressRouteCircuitSku skuName);
            }
        }

        /**
         * The template for a express route circuit update operation, containing all the settings that
         * can be modified.
         */
        interface Update extends
                Appliable<ExpressRouteCircuit>,
                Resource.UpdateWithTags<Update>,
                UpdateStages.WithSku {
        }
    }
