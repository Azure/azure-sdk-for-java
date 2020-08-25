/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;

import java.util.Objects;

class Ancestors {

    class OneAncestor {
        private final String resourceGroupName;
        private final String ancestor1Name;

        OneAncestor(String resourceGroupName, String ancestor1Name) {
            this.resourceGroupName = Objects.requireNonNull(resourceGroupName);
            this.ancestor1Name = Objects.requireNonNull(ancestor1Name);
        }

        OneAncestor(ResourceId resourceId) {
            Objects.requireNonNull(resourceId);
            Objects.requireNonNull(resourceId.parent());

            this.resourceGroupName = Objects.requireNonNull(resourceId.resourceGroupName());
            this.ancestor1Name = Objects.requireNonNull(resourceId.parent().name());
        }

        OneAncestor(String resourceId) {
            this(ResourceId.fromString(Objects.requireNonNull(resourceId)));
        }

        public String resourceGroupName() {
            return this.resourceGroupName;
        }

        public String ancestor1Name() {
            return this.ancestor1Name;
        }
    }

    class TwoAncestor extends OneAncestor {
        private final String ancestor2Name;

        TwoAncestor(String resourceGroupName, String ancestor1Name, String ancestor2Name) {
            super(resourceGroupName, ancestor1Name);
            this.ancestor2Name = Objects.requireNonNull(ancestor2Name);
        }

        TwoAncestor(ResourceId resourceId) {
            super(resourceId);
            Objects.requireNonNull(resourceId.parent().parent());

            this.ancestor2Name = Objects.requireNonNull(resourceId.parent().parent().name());
        }

        TwoAncestor(String resourceId) {
            this(ResourceId.fromString(Objects.requireNonNull(resourceId)));
        }

        public String ancestor2Name() {
            return this.ancestor2Name;
        }
    }

    class ThreeAncestor extends TwoAncestor {
        private final String ancestor3Name;

        ThreeAncestor(String resourceGroupName, String ancestor1Name, String ancestor2Name, String ancestor3Name) {
            super(resourceGroupName, ancestor1Name, ancestor2Name);

            this.ancestor3Name = Objects.requireNonNull(ancestor3Name);
        }

        ThreeAncestor(ResourceId resourceId) {
            super(resourceId);
            Objects.requireNonNull(resourceId.parent().parent().parent());

            this.ancestor3Name = Objects.requireNonNull(resourceId.parent().parent().parent().name());
        }

        ThreeAncestor(String resourceId) {
            this(ResourceId.fromString(Objects.requireNonNull(resourceId)));
        }

        public String ancestor3Name() {
            return this.ancestor3Name;
        }
    }
}
