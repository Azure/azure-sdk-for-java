// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.util.HashMap;
import java.util.Map;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure virtual machine extension. */
@Fluent
public interface VirtualMachineExtension
    extends VirtualMachineExtensionBase, ExternalChildResource<VirtualMachineExtension, VirtualMachine> {
    /**
     * @return a representation of the deferred computation of this call returning the virtual machine extension
     *     instance view
     */
    Mono<VirtualMachineExtensionInstanceView> getInstanceViewAsync();

    /** @return the instance view of the virtual machine extension */
    VirtualMachineExtensionInstanceView getInstanceView();

    /** Grouping of virtual machine extension definition stages as a part of parent virtual machine definition. */
    interface DefinitionStages {
        /**
         * The first stage of a virtual machine extension definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithImageOrPublisher<ParentT> {
        }

        /**
         * The stage of the virtual machine extension definition allowing to specify extension image or specify name of
         * the virtual machine extension publisher.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithImageOrPublisher<ParentT> extends WithPublisher<ParentT> {
            /**
             * Specifies the virtual machine extension image to use.
             *
             * @param image the image
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withImage(VirtualMachineExtensionImage image);
        }

        /**
         * The stage of the virtual machine extension definition allowing to specify the publisher of the virtual
         * machine extension image this extension is based on.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPublisher<ParentT> {
            /**
             * Specifies the name of the virtual machine extension image publisher.
             *
             * @param extensionImagePublisherName the publisher name
             * @return the next stage of the definition
             */
            WithType<ParentT> withPublisher(String extensionImagePublisherName);
        }

        /**
         * The stage of the virtual machine extension definition allowing to specify the type of the virtual machine
         * extension image this extension is based on.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithType<ParentT> {
            /**
             * Specifies the type of the virtual machine extension image.
             *
             * @param extensionImageTypeName the image type name
             * @return the next stage of the definition
             */
            WithVersion<ParentT> withType(String extensionImageTypeName);
        }

        /**
         * The stage of the virtual machine extension definition allowing to specify the type of the virtual machine
         * extension version this extension is based on.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithVersion<ParentT> {
            /**
             * Specifies the version of the virtual machine image extension.
             *
             * @param extensionImageVersionName the version name
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withVersion(String extensionImageVersionName);
        }

        /**
         * The final stage of the virtual machine extension definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>,
                WithAutoUpgradeMinorVersion<ParentT>,
                WithSettings<ParentT>,
                WithTags<ParentT> {
        }

        /**
         * The stage of the virtual machine extension definition allowing to enable or disable auto upgrade of the
         * extension when when a new minor version of virtual machine extension image gets published.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAutoUpgradeMinorVersion<ParentT> {
            /**
             * enables auto upgrade of the extension.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMinorVersionAutoUpgrade();

            /**
             * disables auto upgrade of the extension.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withoutMinorVersionAutoUpgrade();
        }

        /**
         * The stage of the virtual machine extension definition allowing to specify the public and private settings.
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

        /**
         * The stage of the virtual machine extension definition allowing to specify the tags.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithTags<ParentT> {
            /**
             * Specifies tags for the virtual machine extension.
             *
             * @param tags the tags to associate
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTags(Map<String, String> tags);

            /**
             * Adds a tag to the virtual machine extension.
             *
             * @param key the key for the tag
             * @param value the value for the tag
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTag(String key, String value);
        }
    }

    /**
     * The entirety of a virtual machine extension definition as a part of parent definition.
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

    /** Grouping of virtual machine extension definition stages as part of parent virtual machine update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a virtual machine extension definition.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithImageOrPublisher<ParentT> {
        }

        /**
         * The stage of the virtual machine extension allowing to specify extension image or specify name of the virtual
         * machine extension publisher.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithImageOrPublisher<ParentT> extends WithPublisher<ParentT> {
            /**
             * Specifies the virtual machine extension image to use.
             *
             * @param image the image
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withImage(VirtualMachineExtensionImage image);
        }

        /**
         * The stage of the virtual machine extension definition allowing to specify the publisher of the virtual
         * machine extension image this extension is based on.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithPublisher<ParentT> {
            /**
             * Specifies the name of the virtual machine extension image publisher.
             *
             * @param extensionImagePublisherName the publisher name
             * @return the next stage of the definition
             */
            WithType<ParentT> withPublisher(String extensionImagePublisherName);
        }

        /**
         * The stage of the virtual machine extension definition allowing to specify the type of the virtual machine
         * extension image this extension is based on.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithType<ParentT> {
            /**
             * Specifies the type of the virtual machine extension image.
             *
             * @param extensionImageTypeName the image type name
             * @return the next stage of the definition
             */
            WithVersion<ParentT> withType(String extensionImageTypeName);
        }

        /**
         * The stage of the virtual machine extension definition allowing to specify the type of the virtual machine
         * extension version this extension is based on.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithVersion<ParentT> {
            /**
             * Specifies the version of the virtual machine image extension.
             *
             * @param extensionImageVersionName the version name
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withVersion(String extensionImageVersionName);
        }

        /**
         * The final stage of the virtual machine extension definition.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InUpdate<ParentT>,
                WithAutoUpgradeMinorVersion<ParentT>,
                WithSettings<ParentT>,
                WithTags<ParentT> {
        }

        /**
         * The stage of the virtual machine extension definition allowing to enable or disable auto upgrade of the
         * extension when when a new minor version of virtual machine extension image gets published.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithAutoUpgradeMinorVersion<ParentT> {
            /**
             * enables auto upgrade of the extension.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMinorVersionAutoUpgrade();

            /**
             * disables auto upgrade of the extension.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withoutMinorVersionAutoUpgrade();
        }

        /**
         * The stage of the virtual machine extension definition allowing to specify the public and private settings.
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

        /**
         * The stage of the virtual machine extension definition allowing to specify the tags.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithTags<ParentT> {
            /**
             * Specifies tags for the resource.
             *
             * @param tags tags to associate with the resource
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTags(Map<String, String> tags);

            /**
             * Adds a tag to the resource.
             *
             * @param key the key for the tag
             * @param value the value for the tag
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTag(String key, String value);
        }
    }

    /**
     * The entirety of a virtual machine extension definition as a part of parent update.
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
         * The stage of the virtual machine extension update allowing to enable or disable auto upgrade of the extension
         * when when a new minor version of virtual machine extension image gets published.
         */
        interface WithAutoUpgradeMinorVersion {
            /**
             * enables auto upgrade of the extension.
             *
             * @return the next stage of the update
             */
            Update withMinorVersionAutoUpgrade();

            /**
             * enables auto upgrade of the extension.
             *
             * @return the next stage of the update
             */
            Update withoutMinorVersionAutoUpgrade();
        }

        /** The stage of the virtual machine extension update allowing to add or update public and private settings. */
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

        /** The stage of the virtual machine extension update allowing to add or update tags. */
        interface WithTags {
            /**
             * Specifies tags for the virtual machine extension.
             *
             * @param tags tags indexed by name
             * @return the next stage of the update
             */
            Update withTags(Map<String, String> tags);

            /**
             * Adds a tag to the virtual machine extension.
             *
             * @param key the key for the tag
             * @param value the value for the tag
             * @return the next stage of the update
             */
            Update withTag(String key, String value);

            /**
             * Removes a tag from the virtual machine extension.
             *
             * @param key the key of the tag to remove
             * @return the next stage of the update
             */
            Update withoutTag(String key);
        }
    }

    /** The entirety of virtual machine extension update as a part of parent virtual machine update. */
    interface Update
        extends Settable<VirtualMachine.Update>,
            UpdateStages.WithAutoUpgradeMinorVersion,
            UpdateStages.WithSettings,
            UpdateStages.WithTags {
    }
}
