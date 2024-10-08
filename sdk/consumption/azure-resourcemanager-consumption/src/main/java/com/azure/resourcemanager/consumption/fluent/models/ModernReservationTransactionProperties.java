// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.consumption.fluent.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * The properties of a modern reservation transaction.
 */
@Immutable
public final class ModernReservationTransactionProperties
    implements JsonSerializable<ModernReservationTransactionProperties> {
    /*
     * The charge of the transaction.
     */
    private BigDecimal amount;

    /*
     * This is the ARM Sku name. It can be used to join with the serviceType field in additional info in usage records.
     */
    private String armSkuName;

    /*
     * The billing frequency, which can be either one-time or recurring.
     */
    private String billingFrequency;

    /*
     * Billing profile Id.
     */
    private String billingProfileId;

    /*
     * Billing profile name.
     */
    private String billingProfileName;

    /*
     * The ISO currency in which the transaction is charged, for example, USD.
     */
    private String currency;

    /*
     * The description of the transaction.
     */
    private String description;

    /*
     * The date of the transaction
     */
    private OffsetDateTime eventDate;

    /*
     * The type of the transaction (Purchase, Cancel or Refund).
     */
    private String eventType;

    /*
     * Invoice Number
     */
    private String invoice;

    /*
     * Invoice Id as on the invoice where the specific transaction appears.
     */
    private String invoiceId;

    /*
     * Invoice Section Id
     */
    private String invoiceSectionId;

    /*
     * Invoice Section Name.
     */
    private String invoiceSectionName;

    /*
     * The subscription guid that makes the transaction.
     */
    private UUID purchasingSubscriptionGuid;

    /*
     * The subscription name that makes the transaction.
     */
    private String purchasingSubscriptionName;

    /*
     * The quantity of the transaction.
     */
    private BigDecimal quantity;

    /*
     * The region of the transaction.
     */
    private String region;

    /*
     * The reservation order ID is the identifier for a reservation purchase. Each reservation order ID represents a
     * single purchase transaction. A reservation order contains reservations. The reservation order specifies the VM
     * size and region for the reservations.
     */
    private String reservationOrderId;

    /*
     * The name of the reservation order.
     */
    private String reservationOrderName;

    /*
     * This is the term of the transaction.
     */
    private String term;

    /**
     * Creates an instance of ModernReservationTransactionProperties class.
     */
    public ModernReservationTransactionProperties() {
    }

    /**
     * Get the amount property: The charge of the transaction.
     * 
     * @return the amount value.
     */
    public BigDecimal amount() {
        return this.amount;
    }

    /**
     * Get the armSkuName property: This is the ARM Sku name. It can be used to join with the serviceType field in
     * additional info in usage records.
     * 
     * @return the armSkuName value.
     */
    public String armSkuName() {
        return this.armSkuName;
    }

    /**
     * Get the billingFrequency property: The billing frequency, which can be either one-time or recurring.
     * 
     * @return the billingFrequency value.
     */
    public String billingFrequency() {
        return this.billingFrequency;
    }

    /**
     * Get the billingProfileId property: Billing profile Id.
     * 
     * @return the billingProfileId value.
     */
    public String billingProfileId() {
        return this.billingProfileId;
    }

    /**
     * Get the billingProfileName property: Billing profile name.
     * 
     * @return the billingProfileName value.
     */
    public String billingProfileName() {
        return this.billingProfileName;
    }

    /**
     * Get the currency property: The ISO currency in which the transaction is charged, for example, USD.
     * 
     * @return the currency value.
     */
    public String currency() {
        return this.currency;
    }

    /**
     * Get the description property: The description of the transaction.
     * 
     * @return the description value.
     */
    public String description() {
        return this.description;
    }

    /**
     * Get the eventDate property: The date of the transaction.
     * 
     * @return the eventDate value.
     */
    public OffsetDateTime eventDate() {
        return this.eventDate;
    }

    /**
     * Get the eventType property: The type of the transaction (Purchase, Cancel or Refund).
     * 
     * @return the eventType value.
     */
    public String eventType() {
        return this.eventType;
    }

    /**
     * Get the invoice property: Invoice Number.
     * 
     * @return the invoice value.
     */
    public String invoice() {
        return this.invoice;
    }

    /**
     * Get the invoiceId property: Invoice Id as on the invoice where the specific transaction appears.
     * 
     * @return the invoiceId value.
     */
    public String invoiceId() {
        return this.invoiceId;
    }

    /**
     * Get the invoiceSectionId property: Invoice Section Id.
     * 
     * @return the invoiceSectionId value.
     */
    public String invoiceSectionId() {
        return this.invoiceSectionId;
    }

    /**
     * Get the invoiceSectionName property: Invoice Section Name.
     * 
     * @return the invoiceSectionName value.
     */
    public String invoiceSectionName() {
        return this.invoiceSectionName;
    }

    /**
     * Get the purchasingSubscriptionGuid property: The subscription guid that makes the transaction.
     * 
     * @return the purchasingSubscriptionGuid value.
     */
    public UUID purchasingSubscriptionGuid() {
        return this.purchasingSubscriptionGuid;
    }

    /**
     * Get the purchasingSubscriptionName property: The subscription name that makes the transaction.
     * 
     * @return the purchasingSubscriptionName value.
     */
    public String purchasingSubscriptionName() {
        return this.purchasingSubscriptionName;
    }

    /**
     * Get the quantity property: The quantity of the transaction.
     * 
     * @return the quantity value.
     */
    public BigDecimal quantity() {
        return this.quantity;
    }

    /**
     * Get the region property: The region of the transaction.
     * 
     * @return the region value.
     */
    public String region() {
        return this.region;
    }

    /**
     * Get the reservationOrderId property: The reservation order ID is the identifier for a reservation purchase. Each
     * reservation order ID represents a single purchase transaction. A reservation order contains reservations. The
     * reservation order specifies the VM size and region for the reservations.
     * 
     * @return the reservationOrderId value.
     */
    public String reservationOrderId() {
        return this.reservationOrderId;
    }

    /**
     * Get the reservationOrderName property: The name of the reservation order.
     * 
     * @return the reservationOrderName value.
     */
    public String reservationOrderName() {
        return this.reservationOrderName;
    }

    /**
     * Get the term property: This is the term of the transaction.
     * 
     * @return the term value.
     */
    public String term() {
        return this.term;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ModernReservationTransactionProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ModernReservationTransactionProperties if the JsonReader was pointing to an instance of
     * it, or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ModernReservationTransactionProperties.
     */
    public static ModernReservationTransactionProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ModernReservationTransactionProperties deserializedModernReservationTransactionProperties
                = new ModernReservationTransactionProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("amount".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.amount
                        = reader.getNullable(nonNullReader -> new BigDecimal(nonNullReader.getString()));
                } else if ("armSkuName".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.armSkuName = reader.getString();
                } else if ("billingFrequency".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.billingFrequency = reader.getString();
                } else if ("billingProfileId".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.billingProfileId = reader.getString();
                } else if ("billingProfileName".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.billingProfileName = reader.getString();
                } else if ("currency".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.currency = reader.getString();
                } else if ("description".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.description = reader.getString();
                } else if ("eventDate".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.eventDate = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("eventType".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.eventType = reader.getString();
                } else if ("invoice".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.invoice = reader.getString();
                } else if ("invoiceId".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.invoiceId = reader.getString();
                } else if ("invoiceSectionId".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.invoiceSectionId = reader.getString();
                } else if ("invoiceSectionName".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.invoiceSectionName = reader.getString();
                } else if ("purchasingSubscriptionGuid".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.purchasingSubscriptionGuid
                        = reader.getNullable(nonNullReader -> UUID.fromString(nonNullReader.getString()));
                } else if ("purchasingSubscriptionName".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.purchasingSubscriptionName = reader.getString();
                } else if ("quantity".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.quantity
                        = reader.getNullable(nonNullReader -> new BigDecimal(nonNullReader.getString()));
                } else if ("region".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.region = reader.getString();
                } else if ("reservationOrderId".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.reservationOrderId = reader.getString();
                } else if ("reservationOrderName".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.reservationOrderName = reader.getString();
                } else if ("term".equals(fieldName)) {
                    deserializedModernReservationTransactionProperties.term = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedModernReservationTransactionProperties;
        });
    }
}
