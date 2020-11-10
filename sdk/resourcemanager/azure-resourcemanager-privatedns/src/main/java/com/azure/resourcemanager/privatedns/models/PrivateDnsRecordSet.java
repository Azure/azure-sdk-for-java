// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.privatedns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

import java.util.List;
import java.util.Map;

/** An immutable client-side representation of a record set in Azure Private DNS Zone. */
@Fluent
public interface PrivateDnsRecordSet
    extends ExternalChildResource<PrivateDnsRecordSet, PrivateDnsZone>,
        HasInnerModel<RecordSetInner> {
    /**
     * @return the type of the record set.
     */
    RecordType recordType();
    /**
     * @return the ETag of the record set.
     */
    String etag();

    /**
     * @return the metadata attached to the record set.
     */
    Map<String, String> metadata();

    /**
     * @return the time-to-live of the records in the record set.
     */
    long timeToLive();

    /**
     * @return the fully qualified domain name of the record set.
     */
    String fqdn();

    /**
     * @return the property whether the record set is auto-registered in the private DNS zone
     * through a virtual network link.
     */
    boolean isAutoRegistered();

    /**
     * The entirety of a DNS zone record set definition as a part of parent definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.AaaaRecordSetBlank<ParentT>,
            DefinitionStages.WithAaaaRecordIPv6Address<ParentT>,
            DefinitionStages.WithAaaaRecordIPv6AddressOrAttachable<ParentT>,
            DefinitionStages.ARecordSetBlank<ParentT>,
            DefinitionStages.WithARecordIPv4Address<ParentT>,
            DefinitionStages.WithARecordIPv4AddressOrAttachable<ParentT>,
            DefinitionStages.CNameRecordSetBlank<ParentT>,
            DefinitionStages.WithCNameRecordAlias<ParentT>,
            DefinitionStages.WithCNameRecordSetAttachable<ParentT>,
            DefinitionStages.MXRecordSetBlank<ParentT>,
            DefinitionStages.WithMXRecordMailExchange<ParentT>,
            DefinitionStages.WithMXRecordMailExchangeOrAttachable<ParentT>,
            DefinitionStages.PtrRecordSetBlank<ParentT>,
            DefinitionStages.WithPtrRecordTargetDomainName<ParentT>,
            DefinitionStages.WithPtrRecordTargetDomainNameOrAttachable<ParentT>,
            DefinitionStages.SoaRecordSetBlank<ParentT>,
            DefinitionStages.WithSoaRecordAttributes<ParentT>,
            DefinitionStages.WithSoaRecordAttributesOrAttachable<ParentT>,
            DefinitionStages.SrvRecordSetBlank<ParentT>,
            DefinitionStages.WithSrvRecordEntry<ParentT>,
            DefinitionStages.WithSrvRecordEntryOrAttachable<ParentT>,
            DefinitionStages.TxtRecordSetBlank<ParentT>,
            DefinitionStages.WithTxtRecordTextValue<ParentT>,
            DefinitionStages.WithTxtRecordTextValueOrAttachable<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of DNS zone record set definition stages as a part of parent DNS zone definition. */
    interface DefinitionStages {
        /**
         * The first stage of a AAAA record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface AaaaRecordSetBlank<ParentT> extends WithAaaaRecordIPv6Address<ParentT> {
        }

        /**
         * The stage of the AAAA record set definition allowing to add first AAAA record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAaaaRecordIPv6Address<ParentT> {
            /**
             * Creates an AAAA record with the provided IPv6 address in this record set.
             *
             * @param ipv6Address an IPv6 address
             * @return the next stage of the definition
             */
            WithAaaaRecordIPv6AddressOrAttachable<ParentT> withIPv6Address(String ipv6Address);
        }

        /**
         * The stage of the AAAA record set definition allowing to add additional AAAA records or attach the record set
         * to the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAaaaRecordIPv6AddressOrAttachable<ParentT>
            extends WithAaaaRecordIPv6Address<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The first stage of an A record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface ARecordSetBlank<ParentT> extends WithARecordIPv4Address<ParentT> {
        }

        /**
         * The stage of the A record set definition allowing to add first A record.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithARecordIPv4Address<ParentT> {
            /**
             * Creates an A record with the provided IPv4 address in this record set.
             *
             * @param ipv4Address the IPv4 address
             * @return the next stage of the definition
             */
            WithARecordIPv4AddressOrAttachable<ParentT> withIPv4Address(String ipv4Address);
        }

        /**
         * The stage of the A record set definition allowing to add additional A records or attach the record set to the
         * parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithARecordIPv4AddressOrAttachable<ParentT>
            extends WithARecordIPv4Address<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The first stage of a CNAME record set definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface CNameRecordSetBlank<ParentT> extends WithCNameRecordAlias<ParentT> {
        }

        /**
         * The stage of a CNAME record definition allowing to add alias.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCNameRecordAlias<ParentT> {
            /**
             * Creates a CNAME record with the provided alias.
             *
             * @param alias the alias
             * @return the next stage of the definition
             */
            WithCNameRecordSetAttachable<ParentT> withAlias(String alias);
        }

        /**
         * The stage of the CNAME record set definition allowing attach the record set to the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCNameRecordSetAttachable<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The first stage of a MX record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface MXRecordSetBlank<ParentT> extends WithMXRecordMailExchange<ParentT> {
        }

        /**
         * The stage of the MX record set definition allowing to add first MX record.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithMXRecordMailExchange<ParentT> {
            /**
             * Creates and assigns priority to a MX record with the provided mail exchange server in this record set.
             *
             * @param mailExchangeHostName the host name of the mail exchange server
             * @param priority the priority for the mail exchange host, lower the value higher the priority
             * @return the next stage of the definition
             */
            WithMXRecordMailExchangeOrAttachable<ParentT> withMailExchange(String mailExchangeHostName, int priority);
        }

        /**
         * The stage of the MX record set definition allowing to add additional MX records or attach the record set to
         * the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithMXRecordMailExchangeOrAttachable<ParentT>
            extends WithMXRecordMailExchange<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The first stage of a PTR record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface PtrRecordSetBlank<ParentT> extends WithPtrRecordTargetDomainName<ParentT> {
        }

        /**
         * The stage of the PTR record set definition allowing to add first CNAME record.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPtrRecordTargetDomainName<ParentT> {
            /**
             * Creates a PTR record with the provided target domain name in this record set.
             *
             * @param targetDomainName the target domain name
             * @return the next stage of the definition
             */
            WithPtrRecordTargetDomainNameOrAttachable<ParentT> withTargetDomainName(String targetDomainName);
        }

        /**
         * The stage of the PTR record set definition allowing to add additional PTR records or attach the record set to
         * the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPtrRecordTargetDomainNameOrAttachable<ParentT>
            extends WithPtrRecordTargetDomainName<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The first stage of a SOA record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface SoaRecordSetBlank<ParentT> extends WithSoaRecordAttributes<ParentT> {
        }

        /**
         * The stage of the SOA record set definition allowing to add additional SOA records.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSoaRecordAttributes<ParentT> {
            /**
             * Specifies the authoritative server in this record set.
             *
             * @param authoritativeServerHostName the authoritative server
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withAuthoritativeServer(String authoritativeServerHostName);

            /**
             * Specifies the email server in this record set.
             *
             * @param emailServerHostName the email server
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withEmailServer(String emailServerHostName);

            /**
             * Specifies the serial number for this record set.
             *
             * @param serialNumber the email server
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withSerialNumber(long serialNumber);

            /**
             * Specifies the refresh time in this record set.
             *
             * @param refreshTimeInSeconds the refresh time
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withRefreshTimeInSeconds(long refreshTimeInSeconds);

            /**
             * Specifies the retry time in this record set.
             *
             * @param retryTimeInSeconds the retry time
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withRetryTimeInSeconds(long retryTimeInSeconds);

            /**
             * Specifies the expire time in this record set.
             *
             * @param expireTimeInSeconds the expire time
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withExpireTimeInSeconds(long expireTimeInSeconds);

            /**
             * Specifies the time in seconds that any name server or resolver should cache a negative response.
             *
             * @param negativeCachingTimeToLive the time-to-live for cached negative response
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withNegativeResponseCachingTimeToLiveInSeconds(
                long negativeCachingTimeToLive);
        }

        /**
         * The stage of the SOA record set definition allowing to add additional SOA records or attach the record set to
         * the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSoaRecordAttributesOrAttachable<ParentT>
            extends WithSoaRecordAttributes<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The first stage of a SRV record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface SrvRecordSetBlank<ParentT> extends WithSrvRecordEntry<ParentT> {
        }

        /**
         * The stage of the SRV record definition allowing to add first service record.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSrvRecordEntry<ParentT> {
            /**
             * Specifies a service record for a service.
             *
             * @param target the canonical name of the target host running the service
             * @param port the port on which the service is bounded
             * @param priority the priority of the target host, lower the value higher the priority
             * @param weight the relative weight (preference) of the records with the same priority, higher the value
             *     more the preference
             * @return the next stage of the definition
             */
            WithSrvRecordEntryOrAttachable<ParentT> withRecord(String target, int port, int priority, int weight);
        }

        /**
         * The stage of the SRV record set definition allowing to add additional SRV records or attach the record set to
         * the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSrvRecordEntryOrAttachable<ParentT> extends WithSrvRecordEntry<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The first stage of a TXT record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface TxtRecordSetBlank<ParentT> extends WithTxtRecordTextValue<ParentT> {
        }

        /**
         * The stage of the TXT record definition allowing to add first TXT record.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithTxtRecordTextValue<ParentT> {
            /**
             * Creates a Txt record with the given text in this record set.
             *
             * @param text the text value
             * @return the next stage of the definition
             */
            WithTxtRecordTextValueOrAttachable<ParentT> withText(String text);
        }

        /**
         * The stage of the TXT record set definition allowing to add additional TXT records or attach the record set to
         * the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithTxtRecordTextValueOrAttachable<ParentT>
            extends WithTxtRecordTextValue<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The stage of the record set definition allowing to specify the Time To Live (TTL) for the records in this
         * record set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithTtl<ParentT> {
            /**
             * Specifies the Time To Live for the records in the record set.
             *
             * @param ttlInSeconds TTL in seconds
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTimeToLive(long ttlInSeconds);
        }

        /**
         * The stage of the record set definition allowing to specify metadata.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithMetadata<ParentT> {
            /**
             * Adds a metadata to the resource.
             *
             * @param key the key for the metadata
             * @param value the value for the metadata
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMetadata(String key, String value);
        }

        /**
         * The stage of the record set definition allowing to enable ETag validation.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithETagCheck<ParentT> {
            /**
             * Specifies the If-None-Match header with * to prevent updating an existing record set.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withETagCheck();
        }

        /**
         * The final stage of the DNS zone record set definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the DNS zone record set definition can
         * be attached to the parent traffic manager profile definition using {@link
         * PrivateDnsRecordSet.DefinitionStages.WithAttach#attach()}.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>,
            DefinitionStages.WithTtl<ParentT>,
            DefinitionStages.WithMetadata<ParentT>,
            DefinitionStages.WithETagCheck<ParentT> {
        }
    }

    /**
     * The entirety of a DNS zone record set definition as a part of parent update.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.AaaaRecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithAaaaRecordIPv6Address<ParentT>,
            UpdateDefinitionStages.WithAaaaRecordIPv6AddressOrAttachable<ParentT>,
            UpdateDefinitionStages.ARecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithARecordIPv4Address<ParentT>,
            UpdateDefinitionStages.WithARecordIPv4AddressOrAttachable<ParentT>,
            UpdateDefinitionStages.CNameRecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithCNameRecordAlias<ParentT>,
            UpdateDefinitionStages.WithCNameRecordSetAttachable<ParentT>,
            UpdateDefinitionStages.MXRecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithMXRecordMailExchange<ParentT>,
            UpdateDefinitionStages.WithMXRecordMailExchangeOrAttachable<ParentT>,
            UpdateDefinitionStages.PtrRecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithPtrRecordTargetDomainName<ParentT>,
            UpdateDefinitionStages.WithPtrRecordTargetDomainNameOrAttachable<ParentT>,
            UpdateDefinitionStages.SoaRecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithSoaRecordAttributes<ParentT>,
            UpdateDefinitionStages.WithSoaRecordAttributesOrAttachable<ParentT>,
            UpdateDefinitionStages.SrvRecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithSrvRecordEntry<ParentT>,
            UpdateDefinitionStages.WithSrvRecordEntryOrAttachable<ParentT>,
            UpdateDefinitionStages.TxtRecordSetBlank<ParentT>,
            UpdateDefinitionStages.WithTxtRecordTextValue<ParentT>,
            UpdateDefinitionStages.WithTxtRecordTextValueOrAttachable<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of DNS zone record set definition stages as a part of parent DNS zone update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a AAAA record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface AaaaRecordSetBlank<ParentT> extends WithAaaaRecordIPv6Address<ParentT> {
        }

        /**
         * The stage of the AAAA record set definition allowing to add first AAAA record.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAaaaRecordIPv6Address<ParentT> {
            /**
             * Creates an AAAA record with the provided IPv6 address in this record set.
             *
             * @param ipv6Address an IPv6 address
             * @return the next stage of the definition
             */
            WithAaaaRecordIPv6AddressOrAttachable<ParentT> withIPv6Address(String ipv6Address);
        }

        /**
         * The stage of the AAAA record set definition allowing to add additional AAAA records or attach the record set
         * to the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAaaaRecordIPv6AddressOrAttachable<ParentT>
            extends WithAaaaRecordIPv6Address<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The first stage of an A record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface ARecordSetBlank<ParentT> extends WithARecordIPv4Address<ParentT> {
        }

        /**
         * The stage of the A record set definition allowing to add first A record.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithARecordIPv4Address<ParentT> {
            /**
             * Creates an A record with the provided IPv4 address in this record set.
             *
             * @param ipv4Address the IPv4 address
             * @return the next stage of the definition
             */
            WithARecordIPv4AddressOrAttachable<ParentT> withIPv4Address(String ipv4Address);
        }

        /**
         * The stage of the A record set definition allowing to add additional A records or attach the record set to the
         * parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithARecordIPv4AddressOrAttachable<ParentT>
            extends WithARecordIPv4Address<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The first stage of a CNAME record set definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface CNameRecordSetBlank<ParentT> extends WithCNameRecordAlias<ParentT> {
        }

        /**
         * The stage of a CNAME record definition allowing to add alias.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCNameRecordAlias<ParentT> {
            /**
             * Creates a CNAME record with the provided alias.
             *
             * @param alias the alias
             * @return the next stage of the definition
             */
            WithCNameRecordSetAttachable<ParentT> withAlias(String alias);
        }

        /**
         * The stage of the CNAME record set definition allowing attach the record set to the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCNameRecordSetAttachable<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The first stage of a MX record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface MXRecordSetBlank<ParentT> extends WithMXRecordMailExchange<ParentT> {
        }

        /**
         * The stage of the MX record set definition allowing to add first MX record.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithMXRecordMailExchange<ParentT> {
            /**
             * Creates and assigns priority to a MX record with the provided mail exchange server in this record set.
             *
             * @param mailExchangeHostName the host name of the mail exchange server
             * @param priority the priority for the mail exchange host, lower the value higher the priority
             * @return the next stage of the definition
             */
            WithMXRecordMailExchangeOrAttachable<ParentT> withMailExchange(String mailExchangeHostName, int priority);
        }

        /**
         * The stage of the MX record set definition allowing to add additional MX records or attach the record set to
         * the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithMXRecordMailExchangeOrAttachable<ParentT>
            extends WithMXRecordMailExchange<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The first stage of a PTR record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface PtrRecordSetBlank<ParentT> extends WithPtrRecordTargetDomainName<ParentT> {
        }

        /**
         * The stage of the PTR record set definition allowing to add first CNAME record.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPtrRecordTargetDomainName<ParentT> {
            /**
             * Creates a PTR record with the provided target domain name in this record set.
             *
             * @param targetDomainName the target domain name
             * @return the next stage of the definition
             */
            WithPtrRecordTargetDomainNameOrAttachable<ParentT> withTargetDomainName(String targetDomainName);
        }

        /**
         * The stage of the PTR record set definition allowing to add additional PTR records or attach the record set to
         * the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPtrRecordTargetDomainNameOrAttachable<ParentT>
            extends WithPtrRecordTargetDomainName<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The first stage of a SOA record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface SoaRecordSetBlank<ParentT> extends WithSoaRecordAttributes<ParentT> {
        }

        /**
         * The stage of the SOA record set definition allowing to add additional SOA records.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSoaRecordAttributes<ParentT> {
            /**
             * Specifies the authoritative server in this record set.
             *
             * @param authoritativeServerHostName the authoritative server
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withAuthoritativeServer(String authoritativeServerHostName);

            /**
             * Specifies the email server in this record set.
             *
             * @param emailServerHostName the email server
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withEmailServer(String emailServerHostName);

            /**
             * Specifies the serial number for this record set.
             *
             * @param serialNumber the email server
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withSerialNumber(long serialNumber);

            /**
             * Specifies the refresh time in this record set.
             *
             * @param refreshTimeInSeconds the refresh time
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withRefreshTimeInSeconds(long refreshTimeInSeconds);

            /**
             * Specifies the retry time in this record set.
             *
             * @param retryTimeInSeconds the retry time
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withRetryTimeInSeconds(long retryTimeInSeconds);

            /**
             * Specifies the expire time in this record set.
             *
             * @param expireTimeInSeconds the expire time
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withExpireTimeInSeconds(long expireTimeInSeconds);

            /**
             * Specifies the time in seconds that any name server or resolver should cache a negative response.
             *
             * @param negativeCachingTimeToLive the time-to-live for cached negative response
             * @return the next stage of the definition
             */
            WithSoaRecordAttributesOrAttachable<ParentT> withNegativeResponseCachingTimeToLiveInSeconds(
                long negativeCachingTimeToLive);
        }

        /**
         * The stage of the SOA record set definition allowing to add additional SOA records or attach the record set to
         * the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSoaRecordAttributesOrAttachable<ParentT>
            extends WithSoaRecordAttributes<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The first stage of a SRV record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface SrvRecordSetBlank<ParentT> extends WithSrvRecordEntry<ParentT> {
        }

        /**
         * The stage of the SRV record definition allowing to add first service record.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSrvRecordEntry<ParentT> {
            /**
             * Specifies a service record for a service.
             *
             * @param target the canonical name of the target host running the service
             * @param port the port on which the service is bounded
             * @param priority the priority of the target host, lower the value higher the priority
             * @param weight the relative weight (preference) of the records with the same priority, higher the value
             *     more the preference
             * @return the next stage of the definition
             */
            WithSrvRecordEntryOrAttachable<ParentT> withRecord(String target, int port, int priority, int weight);
        }

        /**
         * The stage of the SRV record set definition allowing to add additional SRV records or attach the record set to
         * the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSrvRecordEntryOrAttachable<ParentT> extends WithSrvRecordEntry<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The first stage of a TXT record definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface TxtRecordSetBlank<ParentT> extends WithTxtRecordTextValue<ParentT> {
        }

        /**
         * The stage of the TXT record definition allowing to add first TXT record.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithTxtRecordTextValue<ParentT> {
            /**
             * Creates a Txt record with the given text in this record set.
             *
             * @param text the text value
             * @return the next stage of the definition
             */
            WithTxtRecordTextValueOrAttachable<ParentT> withText(String text);
        }

        /**
         * The stage of the TXT record set definition allowing to add additional TXT records or attach the record set to
         * the parent.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithTxtRecordTextValueOrAttachable<ParentT>
            extends WithTxtRecordTextValue<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The stage of the record set definition allowing to specify the Time To Live (TTL) for the records in this
         * record set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithTtl<ParentT> {
            /**
             * Specifies the Time To Live for the records in the record set.
             *
             * @param ttlInSeconds TTL in seconds
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTimeToLive(long ttlInSeconds);
        }

        /**
         * The stage of the record set definition allowing to specify metadata.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithMetadata<ParentT> {
            /**
             * Adds a metadata to the resource.
             *
             * @param key the key for the metadata
             * @param value the value for the metadata
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMetadata(String key, String value);
        }

        /**
         * The stage of the record set definition allowing to enable ETag validation.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithETagCheck<ParentT> {
            /**
             * Specifies the If-None-Match header with * to prevent updating an existing record set.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withETagCheck();
        }

        /**
         * The final stage of the DNS zone record set definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the DNS zone record set definition can
         * be attached to the parent traffic manager profile definition using {@link
         * PrivateDnsRecordSet.UpdateDefinitionStages.WithAttach#attach()}.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InUpdate<ParentT>,
            UpdateDefinitionStages.WithTtl<ParentT>,
            UpdateDefinitionStages.WithMetadata<ParentT>,
            UpdateDefinitionStages.WithETagCheck<ParentT> {
        }
    }

    /** The entirety of a record sets update as a part of parent DNS zone update. */
    interface UpdateCombined
        extends UpdateAaaaRecordSet,
            UpdateARecordSet,
            UpdateCNameRecordSet,
            UpdateMXRecordSet,
            UpdatePtrRecordSet,
            UpdateSoaRecord,
            UpdateSrvRecordSet,
            UpdateTxtRecordSet,
            Update {
    }

    /** The entirety of an AAAA record set update as a part of parent DNS zone update. */
    interface UpdateAaaaRecordSet extends UpdateStages.WithAaaaRecordIPv6Address, Update {
    }

    /** The entirety of an A record set update as a part of parent DNS zone update. */
    interface UpdateARecordSet extends UpdateStages.WithARecordIPv4Address, Update {
    }

    /** The entirety of CNAME record set update as part of parent DNS zone update. */
    interface UpdateCNameRecordSet extends UpdateStages.WithCNameRecordAlias, Update {
    }

    /** The entirety of a MX record set update as a part of parent DNS zone update. */
    interface UpdateMXRecordSet extends UpdateStages.WithMXRecordMailExchange, Update {
    }

    /** The entirety of a PTR record set update as a part of parent DNS zone update. */
    interface UpdatePtrRecordSet extends UpdateStages.WithPtrRecordTargetDomainName, Update {
    }

    /** The entirety of a SOA record update as a part of parent DNS zone update. */
    interface UpdateSoaRecord extends UpdateStages.WithSoaRecordAttributes, Update {
    }

    /** The entirety of a SRV record set update as a part of parent DNS zone update. */
    interface UpdateSrvRecordSet extends UpdateStages.WithSrvRecordEntry, Update {
    }

    /** The entirety of a TXT record set update as a part of parent DNS zone update. */
    interface UpdateTxtRecordSet extends UpdateStages.WithTxtRecordTextValue, Update {
    }

    /**
     * the set of configurations that can be updated for DNS record set irrespective of their type {@link RecordType}.
     */
    interface Update
        extends Settable<PrivateDnsZone.Update>,
            UpdateStages.WithTtl,
            UpdateStages.WithMetadata,
            UpdateStages.WithETagCheck {
    }

    /** Grouping of DNS zone record set update stages. */
    interface UpdateStages {
        /** The stage of the AAAA record set update allowing to add or remove AAAA record. */
        interface WithAaaaRecordIPv6Address {
            /**
             * Creates an AAAA record with the provided IPv6 address in this record set.
             *
             * @param ipv6Address the IPv6 address
             * @return the next stage of the record set update
             */
            UpdateAaaaRecordSet withIPv6Address(String ipv6Address);

            /**
             * Removes an AAAA record with the provided IPv6 address from this record set.
             *
             * @param ipv6Address the IPv6 address
             * @return the next stage of the record set update
             */
            UpdateAaaaRecordSet withoutIPv6Address(String ipv6Address);
        }

        /** The stage of the A record set update allowing to add or remove A record. */
        interface WithARecordIPv4Address {
            /**
             * Creates an A record with the provided IPv4 address in the record set.
             *
             * @param ipv4Address an IPv4 address
             * @return the next stage of the record set update
             */
            UpdateARecordSet withIPv4Address(String ipv4Address);

            /**
             * Removes the A record with the provided IPv4 address from the record set.
             *
             * @param ipv4Address an IPv4 address
             * @return the next stage of the record set update
             */
            UpdateARecordSet withoutIPv4Address(String ipv4Address);
        }

        /** The stage of the CNAME record set update allowing to update the CNAME record. */
        interface WithCNameRecordAlias {
            /**
             * The new alias for the CNAME record set.
             *
             * @param alias the alias
             * @return the next stage of the record set update
             */
            UpdateCNameRecordSet withAlias(String alias);
        }

        /** The stage of the MX record set definition allowing to add or remove MX record. */
        interface WithMXRecordMailExchange {
            /**
             * Creates and assigns priority to a MX record with the provided mail exchange server in this record set.
             *
             * @param mailExchangeHostName the host name of the mail exchange server
             * @param priority the priority for the mail exchange host, lower the value higher the priority
             * @return the next stage of the record set update
             */
            UpdateMXRecordSet withMailExchange(String mailExchangeHostName, int priority);

            /**
             * Removes MX record with the provided mail exchange server and priority from this record set.
             *
             * @param mailExchangeHostName the host name of the mail exchange server
             * @param priority the priority for the mail exchange host, lower the value higher the priority
             * @return the next stage of the record set update
             */
            UpdateMXRecordSet withoutMailExchange(String mailExchangeHostName, int priority);
        }

        /** The stage of the CName record set definition allowing to add or remove CName record. */
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

        /** The stage of the SOA record definition allowing to update its attributes. */
        interface WithSoaRecordAttributes {
            /**
             * Specifies the authoritative server in this record set.
             *
             * @param authoritativeServerHostName the authoritative server
             * @return the next stage of the record set update
             */
            UpdateSoaRecord withAuthoritativeServer(String authoritativeServerHostName);

            /**
             * Specifies the email server in this record set.
             *
             * @param emailServerHostName the email server
             * @return the next stage of the record set update
             */
            UpdateSoaRecord withEmailServer(String emailServerHostName);

            /**
             * Specifies the serial number for this record set.
             *
             * @param serialNumber the email server
             * @return the next stage of the record set update
             */
            UpdateSoaRecord withSerialNumber(long serialNumber);

            /**
             * Specifies the refresh time in this record set.
             *
             * @param refreshTimeInSeconds the refresh time
             * @return the next stage of the record set update
             */
            UpdateSoaRecord withRefreshTimeInSeconds(long refreshTimeInSeconds);

            /**
             * Specifies the retry time in this record set.
             *
             * @param retryTimeInSeconds the retry time
             * @return the next stage of the record set update
             */
            UpdateSoaRecord withRetryTimeInSeconds(long retryTimeInSeconds);

            /**
             * Specifies the expire time in this record set.
             *
             * @param expireTimeInSeconds the expire time
             * @return the next stage of the record set update
             */
            UpdateSoaRecord withExpireTimeInSeconds(long expireTimeInSeconds);

            /**
             * Specifies the time in seconds that any name server or resolver should cache a negative response.
             *
             * @param negativeCachingTimeToLive the time-to-live for cached negative response
             * @return the next stage of the record set update
             */
            UpdateSoaRecord withNegativeResponseCachingTimeToLiveInSeconds(long negativeCachingTimeToLive);
        }

        /** The stage of the SRV record definition allowing to add or remove service record. */
        interface WithSrvRecordEntry {
            /**
             * Specifies a service record for a service.
             *
             * @param target the canonical name of the target host running the service
             * @param port the port on which the service is bounded
             * @param priority the priority of the target host, lower the value higher the priority
             * @param weight the relative weight (preference) of the records with the same priority, higher the value
             *     more the preference
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

        /** The stage of the Txt record definition allowing to add or remove TXT record. */
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

            /**
             * Removes a Txt record with the given text (split into 255 char chunks) from this record set.
             *
             * @param textChunks the text value as list
             * @return the next stage of the record set update
             */
            UpdateTxtRecordSet withoutText(List<String> textChunks);
        }

        /** The stage of the record set update allowing to specify TTL for the records in this record set. */
        interface WithTtl {
            /**
             * Specifies the TTL for the records in the record set.
             *
             * @param ttlInSeconds TTL in seconds
             * @return the next stage of the record set update
             */
            Update withTimeToLive(long ttlInSeconds);
        }

        /** An update allowing metadata to be modified for the resource. */
        interface WithMetadata {
            /**
             * Adds a metadata to the record set.
             *
             * @param key the key for the metadata
             * @param value the value for the metadata
             * @return the next stage of the record set update
             */
            Update withMetadata(String key, String value);

            /**
             * Removes a metadata from the record set.
             *
             * @param key the key of the metadata to remove
             * @return the next stage of the record set update
             */
            Update withoutMetadata(String key);
        }

        /** The stage of the record set update allowing to enable ETag validation. */
        interface WithETagCheck {
            /**
             * Specifies the If-Match header with the current etag value associated with the record set.
             *
             * @return the next stage of the update
             */
            Update withETagCheck();

            /**
             * Specifies the If-Match header with the given etag value.
             *
             * @param etagValue the etag value
             * @return the next stage of the update
             */
            Update withETagCheck(String etagValue);
        }
    }
}
