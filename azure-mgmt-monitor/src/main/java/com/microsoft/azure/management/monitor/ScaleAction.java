/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor;

import com.microsoft.azure.management.apigeneration.Fluent;
import org.joda.time.Period;

/**
 */
@Fluent
public interface ScaleAction {

    StandaloneUpdateStages.Blank update();

    interface StandaloneDefinition extends
            StandaloneDefinitionStages.WithApply,
            StandaloneDefinitionStages.Blank,
            StandaloneDefinitionStages.WithType,
            StandaloneDefinitionStages.WithCooldown,
            StandaloneDefinitionStages.WithValue {
    }

    interface StandaloneDefinitionStages {
        interface Blank {
            WithType withDirection(ScaleDirection direction);
        }

        interface WithType {
            WithCooldown withType(ScaleType type);
        }

        interface WithCooldown {
            WithValue withCooldown(Period cooldown);
        }

        interface WithValue extends
                WithApply {
            WithValue withValue(String value);
        }
        interface WithApply {
            ScaleAction create();
        }
    }
    interface StandaloneUpdate extends
            StandaloneUpdateStages.Blank {
    }

    interface StandaloneUpdateStages {
        interface Blank {
            StandaloneUpdate withDirection(ScaleDirection direction);
            StandaloneUpdate withType(ScaleType type);
            StandaloneUpdate withCooldown(Period cooldown);
            StandaloneUpdate withValue(String value);
            ScaleAction apply();
        }
    }

    interface Definition extends
            DefinitionStages.WithApply,
            DefinitionStages.Blank,
            DefinitionStages.WithType,
            DefinitionStages.WithCooldown,
            DefinitionStages.WithValue {
    }

    interface DefinitionStages {
        interface Blank {
            WithType withDirection(ScaleDirection direction);
        }

        interface WithType {
            WithCooldown withType(ScaleType type);
        }

        interface WithCooldown {
            WithValue withCooldown(Period cooldown);
        }

        interface WithValue extends
                WithApply {
            WithValue withValue(String value);
        }
        interface WithApply {
            ScaleRule.DefinitionStages.WithAttach attach();
        }
    }

    interface ParentUpdateDefinition extends
            ParentUpdateDefinitionStages.WithApply,
            ParentUpdateDefinitionStages.Blank,
            ParentUpdateDefinitionStages.WithType,
            ParentUpdateDefinitionStages.WithCooldown,
            ParentUpdateDefinitionStages.WithValue {
    }

    interface ParentUpdateDefinitionStages {
        interface Blank {
            WithType withDirection(ScaleDirection direction);
        }

        interface WithType {
            WithCooldown withType(ScaleType type);
        }

        interface WithCooldown {
            WithValue withCooldown(Period cooldown);
        }

        interface WithValue extends
                WithApply {
            WithValue withValue(String value);
        }
        interface WithApply {
            ScaleRule.ParentUpdateDefinitionStages.WithAttach attach();
        }
    }

    interface UpdateDefinition extends
            UpdateDefinitionStages.WithApply,
            UpdateDefinitionStages.Blank,
            UpdateDefinitionStages.WithType,
            UpdateDefinitionStages.WithCooldown,
            UpdateDefinitionStages.WithValue {
    }

    interface UpdateDefinitionStages {
        interface Blank {
            WithType withDirection(ScaleDirection direction);
        }

        interface WithType {
            WithCooldown withType(ScaleType type);
        }

        interface WithCooldown {
            WithValue withCooldown(Period cooldown);
        }

        interface WithValue extends
                WithApply {
            WithValue withValue(String value);
        }
        interface WithApply {
            ScaleRule.UpdateDefinitionStages.WithAttach attach();
        }
    }

    interface Update extends
            UpdateStages.Blank {
    }

    interface UpdateStages {
        interface Blank {
            Update withDirection(ScaleDirection direction);
            Update withType(ScaleType type);
            Update withCooldown(Period cooldown);
            Update withValue(String value);
            ScaleRule.Update parent();
        }
    }
}
