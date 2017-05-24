/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ApplicationGatewayProbeInner;
import com.microsoft.azure.management.network.model.HasProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * A client-side representation of an application gateway probe.
 */
@Fluent()
public interface ApplicationGatewayProbe extends
    HasInner<ApplicationGatewayProbeInner>,
    ChildResource<ApplicationGateway>,
    HasProtocol<ApplicationGatewayProtocol> {

    /**
     * @return the number of seconds between probe retries
     */
    int timeBetweenProbesInSeconds();

    /**
     * @return the relative path to be called by the probe
     */
    String path();

    /**
     * @return the number of seconds waiting for a response after which the probe times out and it is marked as failed
     * <p>Acceptable values are from 1 to 86400 seconds.
     */
    int timeoutInSeconds();

    /**
     * @return the number of failed retry probes before the backend server is marked as being down
     * <p>Acceptable values are from 1 second to 20.
     */
    int retriesBeforeUnhealthy();

    /**
     * @return host name to send the probe to
     */
    String host();

    /**
     * Grouping of application gateway probe definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway probe definition.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithHost<ParentT> {
        }

        /**
         * Stage of an application gateway probe definition allowing to specify the host to send the probe to.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithHost<ParentT> {
            /**
             * Specifies the host name to send the probe to.
             * @param host a host name
             * @return the next stage of the definition
             */
            WithPath<ParentT> withHost(String host);
        }

        /**
         * Stage of an application gateway probe definition allowing to specify the relative path to send the probe to.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithPath<ParentT> {
            /**
             * Specifies the relative path for the probe to call.
             * <p>A probe is sent to &lt;protocol&gt;://&lt;host&gt;:&lt;port&gt;&lt;path&gt;.
             * @param path a relative path
             * @return the next stage of the definition
             */
            WithProtocol<ParentT> withPath(String path);
        }

        /**
         * Stage of an application gateway probe update allowing to specify the protocol of the probe.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithProtocol<ParentT> extends HasProtocol.DefinitionStages.WithProtocol<WithTimeout<ParentT>, ApplicationGatewayProtocol> {
            /**
             * Specifies HTTP as the probe protocol.
             * @return the next stage of the definition
             */
            WithTimeout<ParentT> withHttp();

            /**
             * Specifies HTTPS as the probe protocol.
             * @return the next stage of the definition
             */
            WithTimeout<ParentT> withHttps();
        }

        /**
         * Stage of an application gateway probe definition allowing to specify the amount of time to after which the probe is considered failed.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithTimeout<ParentT> {
            /**
             * Specifies the amount of time in seconds waiting for a response before the probe is considered failed.
             * @param seconds a number of seconds, between 1 and 86400
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTimeoutInSeconds(int seconds);
        }

        /**
         * Stage of an application gateway probe definition allowing to specify the time interval between consecutive probes.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithInterval<ParentT> {
            /**
             * Specifies the time interval in seconds between consecutive probes.
             * @param seconds a number of seconds between 1 and 86400
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTimeBetweenProbesInSeconds(int seconds);
        }

        /**
         * Stage of an application gateway probe definition allowing to specify the number of retries before the server is considered unhealthy.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithRetries<ParentT> {
            /**
             * Specifies the number of retries before the server is considered unhealthy.
             * @param retryCount a number between 1 and 20
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRetriesBeforeUnhealthy(int retryCount);
        }

        /**
         * The final stage of an application gateway probe definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the probe definition
         * can be attached to the parent application gateway definition.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinitionAlt<ParentT>,
            WithInterval<ParentT>,
            WithRetries<ParentT> {
        }
    }

    /**
     * The entirety of an application gateway probe definition.
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithProtocol<ParentT>,
        DefinitionStages.WithPath<ParentT>,
        DefinitionStages.WithHost<ParentT>,
        DefinitionStages.WithTimeout<ParentT> {
    }

    /**
     * Grouping of application gateway probe update stages.
     */
    interface UpdateStages {
        /**
         * Stage of an application gateway probe update allowing to specify the host to send the probe to.
         */
        interface WithHost {
            /**
             * Specifies the host name to send the probe to.
             * @param host a host name
             * @return the next stage of the update
             */
            Update withHost(String host);
        }

        /**
         * Stage of an application gateway probe update allowing to specify the protocol of the probe.
         */
        interface WithProtocol extends HasProtocol.UpdateStages.WithProtocol<Update, ApplicationGatewayProtocol> {
            /**
             * Specifies HTTP as the probe protocol.
             * @return the next stage of the update
             */
            Update withHttp();

            /**
             * Specifies HTTPS as the probe protocol.
             * @return the next stage of the update
             */
            Update withHttps();
        }

        /**
         * Stage of an application gateway probe update allowing to specify the path to send the probe to.
         */
        interface WithPath {
            /**
             * Specifies the relative path for the probe to call.
             * <p>A probe is sent to &lt;protocol&gt;://&lt;host&gt;:&lt;port&gt;&lt;path&gt;.
             * @param path a relative path
             * @return the next stage of the update
             */
            Update withPath(String path);
        }

        /**
         * Stage of an application gateway probe update allowing to specify the amount of time to after which the probe is considered failed.
         */
        interface WithTimeout {
            /**
             * Specifies the amount of time in seconds waiting for a response before the probe is considered failed.
             * @param seconds a number of seconds between 1 and 86400
             * @return the next stage of the update
             */
            Update withTimeoutInSeconds(int seconds);
        }

        /**
         * Stage of an application gateway probe update allowing to specify the time interval between consecutive probes.
         */
        interface WithInterval {
            /**
             * Specifies the time interval in seconds between consecutive probes.
             * @param seconds a number of seconds between 1 and 86400
             * @return the next stage of the update
             */
            Update withTimeBetweenProbesInSeconds(int seconds);
        }

        /**
         * Stage of an application gateway probe update allowing to specify the number of retries before the server is considered unhealthy.
         */
        interface WithRetries {
            /**
             * Specifies the number of retries before the server is considered unhealthy.
             * @param retryCount a number between 1 and 20
             * @return the next stage of the update
             */
            Update withRetriesBeforeUnhealthy(int retryCount);
        }
    }

    /**
     * The entirety of an application gateway probe update as part of an application gateway update.
     */
    interface Update extends
        Settable<ApplicationGateway.Update>,
        UpdateStages.WithProtocol,
        UpdateStages.WithPath,
        UpdateStages.WithHost,
        UpdateStages.WithTimeout,
        UpdateStages.WithInterval,
        UpdateStages.WithRetries {
    }

    /**
     * Grouping of application gateway probe definition stages applicable as part of an application gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway probe definition.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithHost<ParentT> {
        }

        /**
         * Stage of an application gateway probe definition allowing to specify the host to send the probe to.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithHost<ParentT> {
            /**
             * Specifies the host name to send the probe to.
             * @param host a host name
             * @return the next stage of the definition
             */
            WithPath<ParentT> withHost(String host);
        }

        /**
         * Stage of an application gateway probe definition allowing to specify the protocol of the probe.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithProtocol<ParentT> extends HasProtocol.UpdateDefinitionStages.WithProtocol<WithTimeout<ParentT>, ApplicationGatewayProtocol> {
            /**
             * Specifies HTTP as the probe protocol.
             * @return the next stage of the definition
             */
            WithTimeout<ParentT> withHttp();

            /**
             * Specifies HTTPS as the probe protocol.
             * @return the next stage of the definition
             */
            WithTimeout<ParentT> withHttps();
        }

        /**
         * Stage of an application gateway probe definition allowing to specify the amount of time to after which the probe is considered failed.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithTimeout<ParentT> {
            /**
             * Specifies the amount of time in seconds waiting for a response before the probe is considered failed.
             * @param seconds a number of seconds between 1 and 86400
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTimeoutInSeconds(int seconds);
        }

        /**
         * Stage of an application gateway probe definition allowing to specify the path to send the probe to.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithPath<ParentT> {
            /**
             * Specifies the relative path for the probe to call.
             * <p>A probe is sent to &lt;protocol&gt;://&lt;host&gt;:&lt;port&gt;&lt;path&gt;.
             * @param path a relative path
             * @return the next stage of the definition
             */
            WithProtocol<ParentT> withPath(String path);
        }

        /**
         * Stage of an application gateway probe definition allowing to specify the time interval between consecutive probes.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithInterval<ParentT> {
            /**
             * Specifies the time interval in seconds between consecutive probes.
             * @param seconds a number of seconds between 1 and 86400
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTimeBetweenProbesInSeconds(int seconds);
        }

        /**
         * Stage of an application gateway probe definition allowing to specify the number of retries before the server is considered unhealthy.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithRetries<ParentT> {
            /**
             * Specifies the number of retries before the server is considered unhealthy.
             * @param retryCount a number between 1 and 20
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRetriesBeforeUnhealthy(int retryCount);
        }

        /** The final stage of an application gateway probe definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the probe definition
         * can be attached to the parent application gateway definition.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdateAlt<ParentT>,
            WithInterval<ParentT>,
            WithRetries<ParentT> {
        }
    }

    /** The entirety of an application gateway probe definition as part of an application gateway update.
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT>,
        UpdateDefinitionStages.WithProtocol<ParentT>,
        UpdateDefinitionStages.WithPath<ParentT>,
        UpdateDefinitionStages.WithHost<ParentT>,
        UpdateDefinitionStages.WithTimeout<ParentT>,
        UpdateDefinitionStages.WithInterval<ParentT> {
    }
}
