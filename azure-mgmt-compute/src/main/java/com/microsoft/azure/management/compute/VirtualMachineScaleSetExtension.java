package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.implementation.VirtualMachineScaleSetExtensionInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * An immutable client-side representation of an extension associated with virtual machines in a scale set.
 * An extension associated with a virtual machine scale set will be created from a {@link VirtualMachineExtensionImage }.
 */
@Fluent
public interface VirtualMachineScaleSetExtension extends
        Wrapper<VirtualMachineScaleSetExtensionInner>,
        ChildResource<VirtualMachineScaleSet> {
    /**
     * @return the publisher name of the virtual machine scale set extension image this extension is created from
     */
    String publisherName();

    /**
     * @return the type name of the virtual machine scale set extension image this extension is created from
     */
    String typeName();

    /**
     * @return the version name of the virtual machine scale set extension image this extension is created from
     */
    String versionName();

    /**
     * @return true if this extension is configured to upgrade automatically when a new minor version of
     * virtual machine scale set extension image that this extension based on is published
     */
    boolean autoUpgradeMinorVersionEnabled();

    /**
     * @return the public settings of the virtual machine scale set extension as key value pairs
     */
    Map<String, Object> publicSettings();

    /**
     * @return the public settings of the virtual machine extension as a json string
     */
    String publicSettingsAsJsonString();

    /**
     * @return the provisioning state of this virtual machine scale set extension
     */
    String provisioningState();

    /**
     * The entirety of a virtual machine scale set extension definition as a part of parent definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    @LangDefinition(ContainerName = "~/VirtualMachineScaleSetExtension.Definition", ContainerFileName = "IDefinition")
    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithImageOrPublisher<ParentT>,
            DefinitionStages.WithPublisher<ParentT>,
            DefinitionStages.WithType<ParentT>,
            DefinitionStages.WithVersion<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of virtual machine scale set extension definition stages as a part of parent virtual machine scale set definition.
     */
    @LangDefinition(ContainerName = "~/VirtualMachineScaleSetExtension.Definition", ContainerFileName = "IDefinition", IsContainerOnly = true)
    interface DefinitionStages {
        /**
         * The first stage of a virtual machine scale set extension definition.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT>
                extends WithImageOrPublisher<ParentT> {
        }

        /**
         * The stage of the virtual machinescale set extension definition allowing to specify extension image or specify name of
         * the virtual machine scale set extension publisher.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithImageOrPublisher<ParentT>
                extends WithPublisher<ParentT> {
            /**
             * Specifies the virtual machine scale set extension image to use.
             *
             * @param image the image
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withImage(VirtualMachineExtensionImage image);
        }

        /**
         * The stage of the virtual machine scale set extension definition allowing to specify the publisher of the
         * virtual machine scale set extension image this extension is based on.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
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
         * The stage of the virtual machine scale set extension definition allowing to specify the type of the virtual machine
         * scale set extension image this extension is based on.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
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
         * The stage of the virtual machine scale set extension definition allowing to specify the type of the virtual machine
         * scale set extension version this extension is based on.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
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

        /** The final stage of the virtual machine scale set extension definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the virtual machine scale set extension definition
         * can be attached to the parent virtual machine scale set definition using {@link VirtualMachineExtension.DefinitionStages.WithAttach#attach()}.
         * @param <ParentT> the return type of {@link VirtualMachineExtension.DefinitionStages.WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT>,
                WithAutoUpgradeMinorVersion<ParentT>,
                WithSettings<ParentT> {
        }

        /**
         * The stage of the virtual machine scale set extension definition allowing to enable or disable auto upgrade of the
         * extension when when a new minor version of virtual machine scale set extension image gets published.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAutoUpgradeMinorVersion<ParentT> {
            /**
             * enables auto upgrade of the extension.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAutoUpgradeMinorVersionEnabled();

            /**
             * disables auto upgrade of the extension.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAutoUpgradeMinorVersionDisabled();
        }

        /**
         * The stage of the virtual machine scale set extension definition allowing to specify the public and private settings.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
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
     * Grouping of virtual machine scale set extension definition stages as part of parent virtual machine scale set update.
     */
    @LangDefinition(ContainerName = "~/VirtualMachineScaleSetExtension.Update", ContainerFileName = "IUpdateDefinition", IsContainerOnly = true)
    interface UpdateDefinitionStages {
        /**
         * The first stage of a virtual machine scale set extension definition.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT>
                extends WithImageOrPublisher<ParentT> {
        }

        /**
         * The stage of the virtual machine scale set extension allowing to specify extension image or specify name of the
         * virtual machine extension publisher.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithImageOrPublisher<ParentT>
                extends WithPublisher<ParentT> {
            /**
             * Specifies the virtual machine scale set extension image to use.
             *
             * @param image the image
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withImage(VirtualMachineExtensionImage image);
        }

        /**
         * The stage of the virtual machine scale set extension definition allowing to specify the publisher of the
         * virtual machine scale set extension image this extension is based on.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
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
         * The stage of the virtual machine scale set extension definition allowing to specify the type of the virtual machine
         *  scale set extension image this extension is based on.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
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
         * The stage of the virtual machine scale set extension definition allowing to specify the type of the virtual machine
         *  scale set extension version this extension is based on.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
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

        /** The final stage of the virtual machine scale set extension definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the virtual machine scale set extension definition
         * can be attached to the parent virtual machine scale set definition using {@link VirtualMachineExtension.UpdateDefinitionStages.WithAttach#attach()}.
         * @param <ParentT> the return type of {@link VirtualMachineExtension.UpdateDefinitionStages.WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InUpdate<ParentT>,
                WithAutoUpgradeMinorVersion<ParentT>,
                WithSettings<ParentT> {
        }

        /**
         * The stage of the virtual machine scale set extension definition allowing to enable or disable auto upgrade of the
         * extension when when a new minor version of virtual machine scale set extension image gets published.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAutoUpgradeMinorVersion<ParentT> {
            /**
             * Enables auto upgrade of the extension.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAutoUpgradeMinorVersionEnabled();

            /**
             * Disables auto upgrade of the extension.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAutoUpgradeMinorVersionDisabled();
        }

        /**
         * The stage of the virtual machine scale set extension definition allowing to specify the public and private settings.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
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
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    @LangDefinition(ContainerName = "~/VirtualMachineScaleSetExtension.Update", ContainerFileName = "IUpdateDefinition")
    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithImageOrPublisher<ParentT>,
            UpdateDefinitionStages.WithPublisher<ParentT>,
            UpdateDefinitionStages.WithType<ParentT>,
            UpdateDefinitionStages.WithVersion<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of virtual machine extension update stages.
     */
    @LangDefinition(ContainerName = "~/VirtualMachineScaleSetExtension.Update", ContainerFileName = "IUpdate", IsContainerOnly = true)
    interface UpdateStages {
        /**
         * The stage of the virtual machine scale set extension update allowing to enable or disable auto upgrade of the
         * extension when when a new minor version of virtual machine scale set extension image gets published.
         */
        interface WithAutoUpgradeMinorVersion {
            /**
             * enables auto upgrade of the extension.
             *
             * @return the next stage of the update
             */
            Update withAutoUpgradeMinorVersionEnabled();

            /**
             * enables auto upgrade of the extension.
             *
             * @return the next stage of the update
             */
            Update withAutoUpgradeMinorVersionDisabled();
        }

        /**
         * The stage of the virtual machine scale set extension update allowing to add or update public and private settings.
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
    @LangDefinition(ContainerName = "~/VirtualMachineScaleSetExtension.Update", ContainerFileName = "IUpdate")
    interface Update extends
            Settable<VirtualMachineScaleSet.Update>,
            UpdateStages.WithAutoUpgradeMinorVersion,
            UpdateStages.WithSettings {

    }
}
