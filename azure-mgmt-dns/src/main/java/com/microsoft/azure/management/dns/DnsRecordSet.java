package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.dns.implementation.RecordSetInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasTags;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of a record set in Azure Dns Zone.
 */
public interface DnsRecordSet<FluentModelT, ParentT> extends
    ExternalChildResource<FluentModelT, ParentT>,
    HasTags,
    Wrapper<RecordSetInner> {

    /**
     * @return the type of records in this record set
     */
    RecordType recordType();

    /**
     * @return TTL of the records in this record set
     */
    long timeToLive();

    /**
     * The entirety of a Dns zone record set definition as a part of parent definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
            DefinitionStages.ARecordSetBlank<ParentT>,
            DefinitionStages.WithARecordIpv4Address<ParentT>,
            DefinitionStages.WithARecordIpv4AddressOrAttachable<ParentT>,
            DefinitionStages.AaaaRecordSetBlank<ParentT>,
            DefinitionStages.WithAaaaRecordIpv6Address<ParentT>,
            DefinitionStages.WithAaaaRecordIpv6AddressOrAttachable<ParentT>,
            DefinitionStages.MxRecordSetBlank<ParentT>,
            DefinitionStages.WithMxRecordMailExchange<ParentT>,
            DefinitionStages.WithMxRecordMailExchangeOrAttachable<ParentT>,
            DefinitionStages.NsRecordSetBlank<ParentT>,
            DefinitionStages.WithNsRecordNameServer<ParentT>,
            DefinitionStages.PtrRecordSetBlank<ParentT>,
            DefinitionStages.WithPtrRecordTargetDomainName<ParentT>,
            DefinitionStages.WithPtrRecordTargetDomainNameOrAttachable<ParentT>,
            DefinitionStages.SrvRecordSetBlank<ParentT>,
            DefinitionStages.WithSrvRecordEntry<ParentT>,
            DefinitionStages.WithSrvRecordEntryOrAttachable<ParentT>,
            DefinitionStages.TxtRecordSetBlank<ParentT>,
            DefinitionStages.WithTxtRecordTextValue<ParentT>,
            DefinitionStages.WithTxtRecordTextValueOrAttachable<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of Dns zone record set definition stages as a part of parent Dns zone definition.
     */
    interface DefinitionStages {
        /**
         * The first stage of a A record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface ARecordSetBlank<ParentT> extends WithARecordIpv4Address<ParentT> {
        }

        /**
         * The stage of the A record set definition allowing to add first A record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithARecordIpv4Address<ParentT> {
            /**
             * Creates an A record with the provided Ipv4 address in this record set.
             *
             * @param ipv4Address the Ipv4 address
             * @return the next stage of the record set definition
             */
            WithARecordIpv4AddressOrAttachable<ParentT> withIpv4Address(String ipv4Address);
        }

        /**
         * The stage of the A record set definition allowing to add additional A records or
         * attach the record set to the parent.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithARecordIpv4AddressOrAttachable<ParentT>
                extends WithARecordIpv4Address<ParentT>, Attachable<ParentT> {
        }

        /**
         * The first stage of a Aaaa record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface AaaaRecordSetBlank<ParentT> extends WithAaaaRecordIpv6Address<ParentT> {
        }

        /**
         * The stage of the Aaaa record set definition allowing to add first Aaaa record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAaaaRecordIpv6Address<ParentT> {
            /**
             * Creates an Aaaa record with the provided Ipv6 address in this record set.
             *
             * @param ipv6Address the Ipv6 address
             * @return the next stage of the record set definition
             */
            WithAaaaRecordIpv6AddressOrAttachable<ParentT> withIpv6Address(String ipv6Address);
        }

        /**
         * The stage of the Aaaa record set definition allowing to add additional A records or
         * attach the record set to the parent.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAaaaRecordIpv6AddressOrAttachable<ParentT>
                extends WithAaaaRecordIpv6Address<ParentT>, Attachable<ParentT> {
        }

        /**
         * The first stage of a Mx record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface MxRecordSetBlank<ParentT> extends WithMxRecordMailExchange<ParentT> {
        }

        /**
         * The stage of the Mx record set definition allowing to add first Mx record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithMxRecordMailExchange<ParentT> {
            /**
             * Creates and assigns priority to a Mx record with the provided mail exchange server in this record set.
             *
             * @param mailExchangeHostName the host name of the mail exchange server
             * @param priority the priority for the mail exchange host, lower the value higher the priority
             * @return the next stage of the record set definition
             */
            WithMxRecordMailExchangeOrAttachable withMailExchange(String mailExchangeHostName, int priority);
        }

        /**
         * The stage of the Mx record set definition allowing to add additional Mx records or attach the record set
         * to the parent.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithMxRecordMailExchangeOrAttachable<ParentT>
                extends WithMxRecordMailExchange<ParentT>, Attachable<ParentT> {
        }

        /**
         * The first stage of a Ns record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface NsRecordSetBlank<ParentT> extends WithNsRecordNameServer<ParentT> {
        }

        /**
         * The stage of the Ns record set definition allowing to add a Ns record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithNsRecordNameServer<ParentT> {
            /**
             * Creates a Ns record with the provided name server in this record set.
             *
             * @param nameServerHostName the name server host name
             * @return the next stage of the record set definition
             */
            Attachable<ParentT> withNameServer(String nameServerHostName);
        }

        /**
         * The first stage of a Ptr record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface PtrRecordSetBlank<ParentT> extends WithPtrRecordTargetDomainName<ParentT> {
        }

        /**
         * The stage of the Ptr record set definition allowing to add first Cname record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithPtrRecordTargetDomainName<ParentT> {
            /**
             * Creates a Ptr record with the provided target domain name in this record set.
             *
             * @param targetDomainName the target domain name
             * @return the next stage of the record set definition
             */
            WithPtrRecordTargetDomainNameOrAttachable<ParentT> withTargetDomainName(String targetDomainName);
        }

        /**
         * The stage of the Ptr record set definition allowing to add additional Ptr records or
         * attach the record set to the parent.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithPtrRecordTargetDomainNameOrAttachable<ParentT>
                extends WithPtrRecordTargetDomainName<ParentT>, Attachable<ParentT> {
        }

        /**
         * The first stage of a Srv record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface SrvRecordSetBlank<ParentT> extends WithSrvRecordEntry<ParentT> {
        }

        /**
         * The stage of the Srv record definition allowing to add first service record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithSrvRecordEntry<ParentT> {
            /**
             * Specifies a service record for a service.
             *
             * @param target the canonical name of the target host running the service
             * @param port the port on which the service is bounded
             * @param priority the priority of the target host, lower the value higher the priority
             * @param weight the relative weight (preference) of the records with the same priority, higher the value more the preference
             * @return the next stage of the record set definition
             */
            WithSrvRecordEntryOrAttachable<ParentT> withRecord(String target, int port, int priority, int weight);
        }

        /**
         * The stage of the Srv record set definition allowing to add additional Srv records or attach the record set
         * to the parent.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithSrvRecordEntryOrAttachable<ParentT>
            extends WithSrvRecordEntry<ParentT>, Attachable<ParentT> {
        }

        /**
         * The first stage of a Txt record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface TxtRecordSetBlank<ParentT> extends WithTxtRecordTextValue<ParentT> {
        }

        /**
         * The stage of the Srv record definition allowing to add first Txt record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithTxtRecordTextValue<ParentT> {
            /**
             * Creates a Txt record with the given text in this record set.
             *
             * @param text the text value
             * @return the next stage of the record set definition
             */
            WithTxtRecordTextValueOrAttachable<ParentT> withText(String text);
        }

        /**
         * The stage of the Txt record set definition allowing to add additional Txt records or attach the record set
         * to the parent.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface  WithTxtRecordTextValueOrAttachable<ParentT>
            extends WithTxtRecordTextValue<ParentT>, Attachable<ParentT> {
        }

        /**
         * The stage of the record set definition allowing to specify Ttl for the records in this record set.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithTtl<ParentT> {
            /**
             * Specifies the Ttl for the records in the record set.
             *
             * @param ttlInSeconds ttl in seconds
             * @return the next stage of the record set definition
             */
            WithAttach<ParentT> withTimeToLive(long ttlInSeconds);
        }

        /** The final stage of the Dns zone record set definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the Dns zone record set
         * definition can be attached to the parent traffic manager profile definition using {@link DnsRecordSet.DefinitionStages.WithAttach#attach()}.
         * @param <ParentT> the return type of {@link DnsRecordSet.DefinitionStages.WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT>,
                DefinitionStages.WithTtl<ParentT> {
        }
    }

    /**
     * The entirety of a Dns zone record set definition as a part of parent update.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.ARecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithARecordIpv4Address<ParentT>,
            UpdateDefinitionStages.WithARecordIpv4AddressOrAttachable<ParentT>,
            UpdateDefinitionStages.AaaaRecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithAaaaRecordIpv6Address<ParentT>,
            UpdateDefinitionStages.WithAaaaRecordIpv6AddressOrAttachable<ParentT>,
            UpdateDefinitionStages.MxRecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithMxRecordMailExchange<ParentT>,
            UpdateDefinitionStages.WithMxRecordMailExchangeOrAttachable<ParentT>,
            UpdateDefinitionStages.NsRecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithNsRecordNameServer<ParentT>,
            UpdateDefinitionStages.PtrRecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithPtrRecordTargetDomainName<ParentT>,
            UpdateDefinitionStages.WithPtrRecordTargetDomainNameOrAttachable<ParentT>,
            UpdateDefinitionStages.SrvRecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithSrvRecordEntry<ParentT>,
            UpdateDefinitionStages.WithSrvRecordEntryOrAttachable<ParentT>,
            UpdateDefinitionStages.TxtRecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithTxtRecordTextValue<ParentT>,
            UpdateDefinitionStages.WithTxtRecordTextValueOrAttachable<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of Dns zone record set definition stages as a part of parent Dns zone update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a A record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface ARecordSetBlank<ParentT> extends WithARecordIpv4Address<ParentT> {
        }

        /**
         * The stage of the A record set definition allowing to add first A record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithARecordIpv4Address<ParentT> {
            /**
             * Creates an A record with the provided Ipv4 address in this record set.
             *
             * @param ipv4Address the Ipv4 address
             * @return the next stage of the record set definition
             */
            WithARecordIpv4AddressOrAttachable<ParentT> withIpv4Address(String ipv4Address);
        }

        /**
         * The stage of the A record set definition allowing to add additional A records or
         * attach the record set to the parent.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithARecordIpv4AddressOrAttachable<ParentT>
                extends WithARecordIpv4Address<ParentT>, Attachable<ParentT> {
        }

        /**
         * The first stage of a Aaaa record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface AaaaRecordSetBlank<ParentT> extends WithAaaaRecordIpv6Address<ParentT> {
        }

        /**
         * The stage of the Aaaa record set definition allowing to add first Aaaa record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAaaaRecordIpv6Address<ParentT> {
            /**
             * Creates an Aaaa record with the provided Ipv6 address in this record set.
             *
             * @param ipv6Address the Ipv6 address
             * @return the next stage of the record set definition
             */
            WithAaaaRecordIpv6AddressOrAttachable<ParentT> withIpv6Address(String ipv6Address);
        }

        /**
         * The stage of the Aaaa record set definition allowing to add additional A records or
         * attach the record set to the parent.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAaaaRecordIpv6AddressOrAttachable<ParentT>
                extends WithAaaaRecordIpv6Address<ParentT>, Attachable<ParentT> {
        }

        /**
         * The first stage of a Mx record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface MxRecordSetBlank<ParentT> extends WithMxRecordMailExchange<ParentT> {
        }

        /**
         * The stage of the Mx record set definition allowing to add first Mx record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithMxRecordMailExchange<ParentT> {
            /**
             * Creates and assigns priority to a Mx record with the provided mail exchange server in this record set.
             *
             * @param mailExchangeHostName the host name of the mail exchange server
             * @param priority the priority for the mail exchange host, lower the value higher the priority
             * @return the next stage of the record set definition
             */
            WithMxRecordMailExchangeOrAttachable withMailExchange(String mailExchangeHostName, int priority);
        }

        /**
         * The stage of the Mx record set definition allowing to add additional Mx records or attach the record set
         * to the parent.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithMxRecordMailExchangeOrAttachable<ParentT>
                extends WithMxRecordMailExchange<ParentT>, Attachable<ParentT> {
        }

        /**
         * The first stage of a Ns record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface NsRecordSetBlank<ParentT> extends WithNsRecordNameServer<ParentT> {
        }

        /**
         * The stage of the Ns record set definition allowing to add a Ns record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithNsRecordNameServer<ParentT> {
            /**
             * Creates a Ns record with the provided name server in this record set.
             *
             * @param nameServerHostName the name server host name
             * @return the next stage of the record set definition
             */
            Attachable<ParentT> withNameServer(String nameServerHostName);
        }

        /**
         * The first stage of a Ptr record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface PtrRecordSetBlank<ParentT> extends WithPtrRecordTargetDomainName<ParentT> {
        }

        /**
         * The stage of the Ptr record set definition allowing to add first Cname record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithPtrRecordTargetDomainName<ParentT> {
            /**
             * Creates a Ptr record with the provided target domain name in this record set.
             *
             * @param targetDomainName the target domain name
             * @return the next stage of the record set definition
             */
            WithPtrRecordTargetDomainNameOrAttachable<ParentT> withTargetDomainName(String targetDomainName);
        }

        /**
         * The stage of the Ptr record set definition allowing to add additional Ptr records or
         * attach the record set to the parent.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithPtrRecordTargetDomainNameOrAttachable<ParentT>
                extends WithPtrRecordTargetDomainName<ParentT>, Attachable<ParentT> {
        }

        /**
         * The first stage of a Srv record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface SrvRecordSetBlank<ParentT> extends WithSrvRecordEntry<ParentT> {
        }

        /**
         * The stage of the Srv record definition allowing to add first service record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithSrvRecordEntry<ParentT> {
            /**
             * Specifies a service record for a service.
             *
             * @param target the canonical name of the target host running the service
             * @param port the port on which the service is bounded
             * @param priority the priority of the target host, lower the value higher the priority
             * @param weight the relative weight (preference) of the records with the same priority, higher the value more the preference
             * @return the next stage of the record set definition
             */
            WithSrvRecordEntryOrAttachable<ParentT> withRecord(String target, int port, int priority, int weight);
        }

        /**
         * The stage of the Srv record set definition allowing to add additional Srv records or attach the record set
         * to the parent.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithSrvRecordEntryOrAttachable<ParentT>
                extends WithSrvRecordEntry<ParentT>, Attachable<ParentT> {
        }

        /**
         * The first stage of a Txt record definition.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface TxtRecordSetBlank<ParentT> extends WithTxtRecordTextValue<ParentT> {
        }

        /**
         * The stage of the Srv record definition allowing to add first Txt record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithTxtRecordTextValue<ParentT> {
            /**
             * Creates a Txt record with the given text in this record set.
             *
             * @param text the text value
             * @return the next stage of the record set definition
             */
            WithTxtRecordTextValueOrAttachable<ParentT> withText(String text);
        }

        /**
         * The stage of the Txt record set definition allowing to add additional Txt records or attach the record set
         * to the parent.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface  WithTxtRecordTextValueOrAttachable<ParentT>
                extends WithTxtRecordTextValue<ParentT>, Attachable<ParentT> {
        }

        /**
         * The stage of the record set definition allowing to specify Ttl for the records in this record set.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithTtl<ParentT> {
            /**
             * Specifies the Ttl for the records in the record set.
             *
             * @param ttlInSeconds ttl in seconds
             * @return the next stage of the record set definition
             */
            WithAttach<ParentT> withTimeToLive(long ttlInSeconds);
        }

        /** The final stage of the Dns zone record set definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the Dns zone record set
         * definition can be attached to the parent traffic manager profile definition
         * using {@link DnsRecordSet.UpdateDefinitionStages.WithAttach#attach()}.
         * @param <ParentT> the return type of {@link DnsRecordSet.UpdateDefinitionStages.WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InUpdate<ParentT>,
                UpdateDefinitionStages.WithTtl<ParentT> {
        }
    }

    /**
     * The entirety of a record sets update as a part of parent Dns zone update.
     */
    interface UpdateCombined extends
            UpdateARecordSet,
            UpdateAaaaRecordSet,
            UpdatePtrRecordSet,
            UpdateMxRecordSet,
            UpdateSrvRecordSet,
            UpdateTxtRecordSet,
            Update {
    }

    /**
     * The entirety of an A record set update as a part of parent Dns zone update.
     */
    interface UpdateARecordSet extends
            UpdateStages.WithARecordIpv4Address,
            Update {
    }

    /**
     * The entirety of an Aaaa record set update as a part of parent Dns zone update.
     */
    interface UpdateAaaaRecordSet extends
            UpdateStages.WithAaaaRecordIpv6Address,
            Update {
    }

    /**
     * The entirety of a Mx record set update as a part of parent Dns zone update.
     */
    interface UpdateMxRecordSet extends
            UpdateStages.WithMxRecordMailExchange,
            Update {
    }

    /**
     * The entirety of a Ns record set update as a part of parent Dns zone update.
     */
    interface UpdateNsRecordSet extends
            UpdateStages.WithNsRecordNameServer,
            Update {
    }

    /**
     * The entirety of a Ptr record set update as a part of parent Dns zone update.
     */
    interface UpdatePtrRecordSet extends
            UpdateStages.WithPtrRecordTargetDomainName,
            Update {
    }

    /**
     * The entirety of a Srv record set update as a part of parent Dns zone update.
     */
    interface UpdateSrvRecordSet extends
            UpdateStages.WithSrvRecordEntry,
            Update {
    }

    /**
     * The entirety of a txt record set update as a part of parent Dns zone update.
     */
    interface UpdateTxtRecordSet extends
            UpdateStages.WithTxtRecordTextValue,
            Update {
    }

    /**
     * the set of configurations that can be updated for Dns record set irrespective of their type {@link RecordType}.
     */
    interface Update extends
            Settable<DnsZone.Update>,
            UpdateStages.WithTtl {
    }

    /**
     * Grouping of Dns zone record set update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the A record set update allowing to add or remove A record.
         */
        interface WithARecordIpv4Address {
            /**
             * Creates an A record with the provided Ipv4 address in the record set.
             *
             * @param ipv4Address the Ipv4 address
             * @return the next stage of the record set update
             */
            UpdateARecordSet withIpv4Address(String ipv4Address);

            /**
             * Removes the A record with the provided Ipv4 address from the record set.
             *
             * @param ipv4Address the Ipv4 address
             * @return the next stage of the record set update
             */
            UpdateARecordSet withoutIpv4Address(String ipv4Address);
        }

        /**
         * The stage of the Aaaa record set update allowing to add or remove Aaaa record.
         */
        interface WithAaaaRecordIpv6Address {
            /**
             * Creates an Aaaa record with the provided Ipv6 address in this record set.
             *
             * @param ipv6Address the Ipv6 address
             * @return the next stage of the record set update
             */
            UpdateAaaaRecordSet withIpv6Address(String ipv6Address);

            /**
             * Removes an Aaaa record with the provided Ipv6 address from this record set.
             *
             * @param ipv6Address the Ipv6 address
             * @return the next stage of the record set update
             */
            UpdateAaaaRecordSet withoutIpv6Address(String ipv6Address);
        }

        /**
         * The stage of the Mx record set definition allowing to add or remove Mx record.
         */
        interface WithMxRecordMailExchange {
            /**
             * Creates and assigns priority to a Mx record with the provided mail exchange server in this record set.
             *
             * @param mailExchangeHostName the host name of the mail exchange server
             * @param priority the priority for the mail exchange host, lower the value higher the priority
             * @return the next stage of the record set update
             */
            UpdateMxRecordSet withMailExchange(String mailExchangeHostName, int priority);

            /**
             * Removes Mx record with the provided mail exchange server and priority from this record set.
             *
             * @param mailExchangeHostName the host name of the mail exchange server
             * @param priority the priority for the mail exchange host, lower the value higher the priority
             * @return the next stage of the record set update
             */
            UpdateMxRecordSet withoutMailExchange(String mailExchangeHostName, int priority);
        }

        /**
         * The stage of the Ns record set definition allowing to add or remove a Ns record.
         */
        interface WithNsRecordNameServer {
            /**
             * Creates a Ns record with the provided name server in this record set.
             *
             * @param nameServerHostName the name server host name
             * @return the next stage of the record set update
             */
            UpdateNsRecordSet withNameServer(String nameServerHostName);

            /**
             * Rmoves a Ns record with the provided name server from this record set.
             *
             * @param nameServerHostName the name server host name
             * @return the next stage of the record set update
             */
            UpdateNsRecordSet withoutNameServer(String nameServerHostName);
        }

        /**
         * The stage of the CName record set definition allowing to add or remove Cname record.
         */
        interface WithPtrRecordTargetDomainName {
            /**
             * Creates a CName record with the provided canonical name in this record set.
             *
             * @param targetDomainName the target domain name
             * @return the next stage of the record set update
             */
            UpdatePtrRecordSet withTargetDomainName(String targetDomainName);

            /**
             * Removes the CName record with the provided canonical name from this record set.
             *
             * @param targetDomainName the target domain name
             * @return the next stage of the record set update
             */
            UpdatePtrRecordSet withoutTargetDomainName(String targetDomainName);
        }

        /**
         * The stage of the Srv record definition allowing to add or remove service record.
         */
        interface WithSrvRecordEntry {
            /**
             * Specifies a service record for a service.
             *
             * @param target the canonical name of the target host running the service
             * @param port the port on which the service is bounded
             * @param priority the priority of the target host, lower the value higher the priority
             * @param weight the relative weight (preference) of the records with the same priority, higher the value more the preference
             * @return the next stage of the record set update
             */
            UpdateSrvRecordSet withRecord(String target, int port, int priority, int weight);

            /**
             * Removes a service record for a service.
             *
             * @param target the canonical name of the target host running the service
             * @param port the port on which the service is bounded
             * @param priority the priority of the target host
             * @param weight the relative weight (preference) of the records
             * @return the next stage of the record set update
             */
            UpdateSrvRecordSet withoutRecord(String target, int port, int priority, int weight);
        }

        /**
         * The stage of the Srv record definition allowing to add or remove Txt record.
         */
        interface WithTxtRecordTextValue {
            /**
             * Creates a Txt record with the given text in this record set.
             *
             * @param text the text value
             * @return the next stage of the record set update
             */
            UpdateTxtRecordSet withText(String text);

            /**
             * Removes a Txt record with the given text from this record set.
             *
             * @param text the text value
             * @return the next stage of the record set update
             */
            UpdateTxtRecordSet withoutText(String text);
        }

        /**
         * The stage of the record set update allowing to specify Ttl for the records in this record set.
         */
        interface WithTtl {
            /**
             * Specifies the Ttl for the records in the record set.
             *
             * @param ttlInSeconds ttl in seconds
             * @return the next stage of the record set update
             */
            Update withTimeToLive(long ttlInSeconds);
        }
    }
}
