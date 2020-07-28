// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineScaleSetExtensionInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.util.HashMap;
import java.util.Map;

/** An immutable client-side representation of an extension associated with virtual machines in a scale set. */
@Fluent
public interface VirtualMachineScaleSetExtension
    extends HasInner<VirtualMachineScaleSetExtensionInner>, ChildResource<VirtualMachineScaleSet> {
    /** @return the publisher name of the virtual machine scale set extension image this extension is created from */
    String publisherName();

    /** @return the type name of the virtual machine scale set extension image this extension is created from */
    String typeName();

    /** @return the version name of the virtual machine scale set extension image this extension is created from */
    String versionName();

    /**
     * @return true if this extension is configured to upgrade automatically when a new minor version of the extension
     *     image that this extension based on is published
     */
    boolean autoUpgradeMinorVersionEnabled();

    /** @return the public settings of the virtual machine scale set extension as key value pairs */
    Map<String, Object> publicSettings();

    /** @return the public settings of the virtual machine extension as a JSON string */
    String publicSettingsAsJsonString();

    /** @return the provisioning state of this virtual machine scale set extension */
    String provisioningState();

    /**
     * The entirety of a virtual machine scale set extension definition as a part of a parent definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithImageOrPublisher<ParentT>,
            DefinitionStages.WithPublisher<ParentT>,
            DefinitionStages.WithType<ParentT>,
            DefinitionStages.WithVersion<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of virtual machine scale set extension definition stages as a part of parent virtual machine scale set
     * definition.
     */
    interface DefinitionStages {
        /**
         * The first stage of a virtual machine scale set extension definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithImageOrPublisher<ParentT> {
        }

        /**
         * The stage of a virtual machines scale set extension definition allowing to specify an extension image or
         * specify name of the virtual machine scale set extension publisher.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithImageOrPublisher<ParentT> extends WithPublisher<ParentT> {
            /**
             * Specifies the virtual machine scale set extension image to use.
             *
             * @param image an extension image
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withImage(VirtualMachineExtensionImage image);
        }

        /**
         * The stage of a virtual machine scale set extension definition allowing to specify the publisher of the
         * extension image this extension is based on.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPublisher<ParentT> {
            /**
             * Specifies the name of the publisher of the virtual machine scale set extension image.
             *
             * @param extensionImagePublisherName a publisher name
             * @return the next stage of the definition
             */
            WithType<ParentT> withPublisher(String extensionImagePublisherName);
        }

        /**
         * The stage of a virtual machine scale set extension definition allowing to specify the type of the virtual
         * machine scale set extension image this extension is based on.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithType<ParentT> {
            /**
             * Specifies the type of the virtual machine scale set extension image.
             *
             * @param extensionImageTypeName the image type name
             * @return the next stage of the definition
             */
            WithVersion<ParentT> withType(String extensionImageTypeName);
        }

        /**
         * The stage of a virtual machine scale set extension definition allowing to specify the type of the virtual
         * machine scale set extension version this extension is based on.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithVersion<ParentT> {
            /**
             * Specifies the version of the virtual machine scale set image extension.
             *
             * @param extensionImageVersionName the version name
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withVersion(String extensionImageVersionName);
        }

        /**
         * The final stage of a virtual machine scale set extension definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>, WithAutoUpgradeMinorVersion<ParentT>, WithSettings<ParentT> {
        }

        /**
         * The stage of a virtual machine scale set extension definition allowing to enable or disable auto upgrade of
         * the extension when when a new minor version of virtual machine scale set extension image gets published.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAutoUpgradeMinorVersion<ParentT> {
            /**
             * Enables auto upgrading of the extension with minor versions.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMinorVersionAutoUpgrade();

            /**
             * Disables auto upgrading the extension with minor versions.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withoutMinorVersionAutoUpgrade();
        }

        /**
         * The stage of a virtual machine scale set extension definition allowing to specify the public and private
         * settings.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSettings<ParentT> {
            /**
             * Specifies a public settings entry.
             *
             * @param key the key of a public settings entry
             * @param value the value of the public settings entry
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPublicSetting(String key, Object value);

            /**
             * Specifies a private settings entry.
             *
             * @param key the key of a private settings entry
             * @param value the value of the private settings entry
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withProtectedSetting(String key, Object value);

            /**
             * Specifies public settings.
             *
             * @param settings the public settings
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPublicSettings(HashMap<String, Object> settings);

            /**
             * Specifies private settings.
             *
             * @param settings the private settings
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withProtectedSettings(HashMap<String, Object> settings);
        }
    }

    /**
     * Grouping of virtual machine scale set extension definition stages as part of parent virtual machine scale set
     * update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a virtual machine scale set extension definition.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithImageOrPublisher<ParentT> {
        }

        /**
         * The stage of a virtual machine scale set extension allowing to specify an extension image or the name of the
         * virtual machine extension publisher.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithImageOrPublisher<ParentT> extends WithPublisher<ParentT> {
            /**
             * Specifies the virtual machine scale set extension image to use.
             *
             * @param image an extension image
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withImage(VirtualMachineExtensionImage image);
        }

        /**
         * The stage of a virtual machine scale set extension definition allowing to specify the publisher of the
         * virtual machine scale set extension image this extension is based on.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithPublisher<ParentT> {
            /**
             * Specifies the name of the virtual machine scale set extension image publisher.
             *
             * @param extensionImagePublisherName the publisher name
             * @return the next stage of the definition
             */
            WithType<ParentT> withPublisher(String extensionImagePublisherName);
        }

        /**
         * The stage of a virtual machine scale set extension definition allowing to specify the type of the virtual
         * machine scale set extension image this extension is based on.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithType<ParentT> {
            /**
             * Specifies the type of the virtual machine scale set extension image.
             *
             * @param extensionImageTypeName an image type name
             * @return the next stage of the definition
             */
            WithVersion<ParentT> withType(String extensionImageTypeName);
        }

        /**
         * The stage of a virtual machine scale set extension definition allowing to specify the type of the virtual
         * machine scale set extension version this extension is based on.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithVersion<ParentT> {
            /**
             * Specifies the version of the virtual machine scale set image extension.
             *
             * @param extensionImageVersionName a version name
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withVersion(String extensionImageVersionName);
        }

        /**
         * The final stage of the virtual machine scale set extension definition.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InUpdate<ParentT>, WithAutoUpgradeMinorVersion<ParentT>, WithSettings<ParentT> {
        }

        /**
         * The stage of a virtual machine scale set extension definition allowing to enable or disable auto upgrade of
         * the extension when when a new minor version of virtual machine scale set extension image gets published.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithAutoUpgradeMinorVersion<ParentT> {
            /**
             * Enables auto upgrading of the extension with minor versions.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMinorVersionAutoUpgrade();

            /**
             * Disables auto upgrade of the extension with minor versions.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withoutMinorVersionAutoUpgrade();
        }

        /**
         * The stage of a virtual machine scale set extension definition allowing to specify the public and private
         * settings.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithSettings<ParentT> {
            /**
             * Specifies a public settings entry.
             *
             * @param key the key of a public settings entry
             * @param value the value of the public settings entry
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPublicSetting(String key, Object value);

            /**
             * Specifies a private settings entry.
             *
             * @param key the key of a private settings entry
             * @param value the value of the private settings entry
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withProtectedSetting(String key, Object value);

            /**
             * Specifies public settings.
             *
             * @param settings the public settings
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPublicSettings(HashMap<String, Object> settings);

            /**
             * Specifies private settings.
             *
             * @param settings the private settings
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withProtectedSettings(HashMap<String, Object> settings);
        }
    }

    /**
     * The entirety of a virtual machine scale set extension definition as a part of parent update.
     *
     * @param <ParentT> the stage of the parent update to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithImageOrPublisher<ParentT>,
            UpdateDefinitionStages.WithPublisher<ParentT>,
            UpdateDefinitionStages.WithType<ParentT>,
            UpdateDefinitionStages.WithVersion<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of virtual machine extension update stages. */
    interface UpdateStages {
        /**
         * The stage of a virtual machine scale set extension update allowing to enable or disable auto upgrade of the
         * extension when when a new minor version of virtual machine scale set extension image gets published.
         */
        interface WithAutoUpgradeMinorVersion {
            /**
             * Enables auto-upgrading of the extension with minor versions.
             *
             * @return the next stage of the update
             */
            Update withMinorVersionAutoUpgrade();

            /**
             * Disables auto upgrading of the extension with minor versions.
             *
             * @return the next stage of the update
             */
            Update withoutMinorVersionAutoUpgrade();
        }

        /**
         * The stage of a virtual machine scale set extension update allowing to add or update public and private
         * settings.
         */
        interface WithSettings {
            /**
             * Specifies a public settings entry.
             *
             * @param key the key of a public settings entry
             * @param value the value of the public settings entry
             * @return the next stage of the update
             */
            Update withPublicSetting(String key, Object value);

            /**
             * Specifies a private settings entry.
             *
             * @param key the key of a private settings entry
             * @param value the value of the private settings entry
             * @return the next stage of the update
             */
            Update withProtectedSetting(String key, Object value);

            /**
             * Specifies public settings.
             *
             * @param settings the public settings
             * @return the next stage of the update
             */
            Update withPublicSettings(HashMap<String, Object> settings);

            /**
             * Specifies private settings.
             *
             * @param settings the private settings
             * @return the next stage of the update
             */
            Update withProtectedSettings(HashMap<String, Object> settings);
        }
    }

    /**
     * The entirety of virtual machine scale set extension update as a part of parent virtual machine scale set update.
     */
    interface Update
        extends Settable<VirtualMachineScaleSet.Update>,
            UpdateStages.WithAutoUpgradeMinorVersion,
            UpdateStages.WithSettings {
    }
}
