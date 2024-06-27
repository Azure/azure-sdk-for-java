// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.search.documents.indexes.models.FieldBuilderOptions;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateIndexWithFieldBuilderExample {
    /**
     * From the Azure portal, get your Azure Cognitive Search service name and API key and populate ADMIN_KEY and
     * SEARCH_SERVICE_NAME.
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");

    public static void main(String[] args) {
        // Create the SearchIndex client.
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildClient();

        // Use the SearchIndexClient to create SearchFields from your own model that has fields or methods annotated
        // with @SimpleField or @SearchableField.
        List<SearchField> indexFields = SearchIndexClient.buildSearchFields(Hotel.class, new FieldBuilderOptions());
        String indexName = "hotels";
        SearchIndex newIndex = new SearchIndex(indexName, indexFields);

        // Create index.
        client.createIndex(newIndex);

        // Cleanup index resource.
        client.deleteIndex(indexName);
    }

    /**
     * A hotel.
     */
    public static final class Hotel {
        @SimpleField(isKey = true, isFilterable = true, isSortable = true)
        private final String hotelId;

        @SearchableField(isFilterable = true, isSortable = true)
        private String hotelName;

        @SearchableField(analyzerName = "en.lucene")
        private String description;

        @SearchableField(analyzerName = "fr.lucene")
        private String descriptionFr;

        @SearchableField(isFilterable = true, isFacetable = true)
        private List<String> tags;

        // Complex fields are included automatically in an index if not ignored.
        private Address address;

        /**
         * Creates a new Hotel object.
         *
         * @param hotelId The unique identifier of the hotel.
         */
        public Hotel(String hotelId) {
            this.hotelId = hotelId;
        }

        /**
         * Gets the unique identifier of the hotel.
         *
         * @return The unique identifier of the hotel.
         */
        public String getHotelId() {
            return hotelId;
        }

        /**
         * Gets the name of the hotel.
         *
         * @return The name of the hotel.
         */
        public String getHotelName() {
            return hotelName;
        }

        /**
         * Sets the name of the hotel.
         *
         * @param hotelName The name of the hotel.
         * @return The updated Hotel object.
         */
        public Hotel setHotelName(String hotelName) {
            this.hotelName = hotelName;
            return this;
        }

        /**
         * Gets the description for the hotel.
         *
         * @return The description for the hotel.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the description for the hotel.
         *
         * @param description The description for the hotel.
         * @return The updated Hotel object.
         */
        public Hotel setDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Gets the French description for the hotel.
         *
         * @return The French description for the hotel.
         */
        public String getDescriptionFr() {
            return descriptionFr;
        }

        /**
         * Sets the French description for the hotel.
         *
         * @param descriptionFr The French description for the hotel.
         * @return The updated Hotel object.
         */
        public Hotel setDescriptionFr(String descriptionFr) {
            this.descriptionFr = descriptionFr;
            return this;
        }

        /**
         * Gets the tags for the hotel.
         *
         * @return The tags for the hotel.
         */
        public List<String> getTags() {
            return Collections.unmodifiableList(tags);
        }

        /**
         * Sets the tags for the hotel.
         *
         * @param tags The tags for the hotel.
         * @return The updated Hotel object.
         */
        public Hotel setTags(List<String> tags) {
            this.tags = new ArrayList<>(tags);
            return this;
        }

        /**
         * Gets the address of the hotel.
         *
         * @return The address of the hotel.
         */
        public Address getAddress() {
            return address;
        }

        /**
         * Sets the address of the hotel.
         *
         * @param address The address of the hotel.
         * @return The updated Hotel object.
         */
        public Hotel setAddress(Address address) {
            this.address = address;
            return this;
        }
    }

    /**
     * An address.
     */
    public static final class Address {
        @SearchableField
        private String streetAddress;

        @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
        private String city;

        @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
        private String stateProvince;

        @SearchableField(synonymMapNames = {"synonymMapName"}, isFilterable = true, isSortable = true, isFacetable = true)
        private String country;

        @SearchableField(isFilterable = true, isSortable = true, isFacetable = true)
        private String postalCode;

        /**
         * Gets the street address of the address.
         *
         * @return The street address of the address.
         */
        public String getStreetAddress() {
            return streetAddress;
        }

        /**
         * Sets the street address of the address.
         *
         * @param streetAddress The street address of the address.
         * @return The updated Address object.
         */
        public Address setStreetAddress(String streetAddress) {
            this.streetAddress = streetAddress;
            return this;
        }

        /**
         * Gets the city of the address.
         *
         * @return The city of the address.
         */
        public String getCity() {
            return city;
        }

        /**
         * Sets the city of the address.
         *
         * @param city The city of the address.
         * @return The updated Address object.
         */
        public Address setCity(String city) {
            this.city = city;
            return this;
        }

        /**
         * Gets the state or province of the address.
         *
         * @return The state or province of the address.
         */
        public String getStateProvince() {
            return stateProvince;
        }

        /**
         * Sets the state or province of the address.
         *
         * @param stateProvince The state or province of the address.
         * @return The updated Address object.
         */
        public Address setStateProvince(String stateProvince) {
            this.stateProvince = stateProvince;
            return this;
        }

        /**
         * Gets the country of the address.
         *
         * @return The country of the address.
         */
        public String getCountry() {
            return country;
        }

        /**
         * Sets the country of the address.
         *
         * @param country The country of the address.
         * @return The updated Address object.
         */
        public Address setCountry(String country) {
            this.country = country;
            return this;
        }

        /**
         * Gets the postal code of the address.
         *
         * @return The postal code of the address.
         */
        public String getPostalCode() {
            return postalCode;
        }

        /**
         * Sets the postal code of the address.
         *
         * @param postalCode The postal code of the address.
         * @return The updated Address object.
         */
        public Address setPostalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }
    }
}
