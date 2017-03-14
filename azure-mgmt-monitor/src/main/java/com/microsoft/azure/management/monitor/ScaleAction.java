/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import org.joda.time.Period;

/**
 */
@Fluent
public interface ScaleAction {

    interface Definition<ParentT> extends
            DefinitionStages.WithApply<ParentT>,
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithType<ParentT>,
            DefinitionStages.WithCooldown<ParentT>,
            DefinitionStages.WithValue<ParentT> {
    }

    interface DefinitionStages {
        interface Blank<ParentT> {
            WithType<ParentT> withDirection(ScaleDirection direction);
        }

        interface WithType<ParentT> {
            WithCooldown<ParentT> withType(ScaleType type);
        }

        interface WithCooldown<ParentT> {
            WithValue<ParentT> withCooldown(Period cooldown);
        }

        interface WithValue<ParentT> extends
                WithApply<ParentT> {
            WithValue<ParentT> withValue(String value);
        }
        interface WithApply<ParentT> {
            ParentT apply();
        }
    }

    interface Update<ParentT> extends
            UpdateStages.Blank<ParentT> {
    }

    interface UpdateStages {
        interface Blank<ParentT> {
            Update<ParentT> withDirection(ScaleDirection direction);
            Update<ParentT> withType(ScaleType type);
            Update<ParentT> withCooldown(Period cooldown);
            Update<ParentT> withValue(String value);
            ParentT apply();
        }
    }
}
