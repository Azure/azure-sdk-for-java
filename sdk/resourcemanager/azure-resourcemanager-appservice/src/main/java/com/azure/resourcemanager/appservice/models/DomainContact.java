// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryPhoneCode;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** A domain contact definition. */
@Fluent
public interface DomainContact extends HasInnerModel<Contact>, ChildResource<AppServiceDomain> {

    /** @return contact's mailing address */
    Address addressMailing();

    /** @return contact's email address */
    String email();

    /** @return contact's fax number */
    String fax();

    /** @return contact's job title */
    String jobTitle();

    /** @return contact's first name */
    String firstName();

    /** @return contact's last name */
    String lastName();

    /** @return contact's middle name */
    String middleName();

    /** @return contact's organization */
    String organization();

    /** @return contact's phone number */
    String phone();

    /**
     * The entirety of a domain contact definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithFirstName<ParentT>,
            DefinitionStages.WithMiddleName<ParentT>,
            DefinitionStages.WithAddressLine1<ParentT>,
            DefinitionStages.WithAddressLine2<ParentT>,
            DefinitionStages.WithCity<ParentT>,
            DefinitionStages.WithStateOrProvince<ParentT>,
            DefinitionStages.WithCountry<ParentT>,
            DefinitionStages.WithPostalCode<ParentT>,
            DefinitionStages.WithEmail<ParentT>,
            DefinitionStages.WithPhoneCountryCode<ParentT>,
            DefinitionStages.WithPhoneNumber<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of domain contact stages applicable as part of a domain creation. */
    interface DefinitionStages {
        /**
         * The first stage of a domain contact definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface Blank<ParentT> extends WithFirstName<ParentT> {
        }

        /**
         * The stage of contact definition allowing first name to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithFirstName<ParentT> {
            /**
             * Specifies the first name.
             *
             * @param firstName the first name
             * @return the next stage of the definition
             */
            WithMiddleName<ParentT> withFirstName(String firstName);
        }

        /**
         * The stage of contact definition allowing middle name to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithMiddleName<ParentT> extends WithLastName<ParentT> {
            /**
             * Specifies the middle name.
             *
             * @param middleName the middle name
             * @return the next stage of the definition
             */
            WithLastName<ParentT> withMiddleName(String middleName);
        }

        /**
         * The stage of contact definition allowing last name to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithLastName<ParentT> {
            /**
             * Specifies the last name.
             *
             * @param lastName the last name
             * @return the next stage of the definition
             */
            WithEmail<ParentT> withLastName(String lastName);
        }

        /**
         * The stage of contact definition allowing email to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithEmail<ParentT> {
            /**
             * Specifies the email.
             *
             * @param email contact's email address
             * @return the next stage of the definition
             */
            WithAddressLine1<ParentT> withEmail(String email);
        }

        /**
         * The stage of contact definition allowing 1st line of address to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithAddressLine1<ParentT> {
            /**
             * Specifies the 1st line of the address.
             *
             * @param addressLine1 the 1st line of the address
             * @return the next stage of the definition
             */
            WithAddressLine2<ParentT> withAddressLine1(String addressLine1);
        }

        /**
         * The stage of contact definition allowing 2nd line of address to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithAddressLine2<ParentT> extends WithCity<ParentT> {
            /**
             * Specifies the 2nd line of the address.
             *
             * @param addressLine2 the 2nd line of the address
             * @return the next stage of the definition
             */
            WithCity<ParentT> withAddressLine2(String addressLine2);
        }

        /**
         * The stage of contact definition allowing city to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithCity<ParentT> {
            /**
             * Specifies the city of the address.
             *
             * @param city the city of the address
             * @return the next stage of the definition
             */
            WithStateOrProvince<ParentT> withCity(String city);
        }

        /**
         * The stage of contact definition allowing state/province to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithStateOrProvince<ParentT> {
            /**
             * Specifies the state or province of the address.
             *
             * @param stateOrProvince the state or province of the address
             * @return the next stage of the definition
             */
            WithCountry<ParentT> withStateOrProvince(String stateOrProvince);
        }

        /**
         * The stage of contact definition allowing country to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithCountry<ParentT> {
            /**
             * Specifies the country of the address.
             *
             * @param country the country of the address
             * @return the next stage of the definition
             */
            WithPostalCode<ParentT> withCountry(CountryIsoCode country);
        }

        /**
         * The stage of contact definition allowing postal/zip code to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithPostalCode<ParentT> {
            /**
             * Specifies the postal code or zip code of the address.
             *
             * @param postalCode the postal code of the address
             * @return the next stage of the definition
             */
            WithPhoneCountryCode<ParentT> withPostalCode(String postalCode);
        }

        /**
         * The stage of contact definition allowing phone country code to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithPhoneCountryCode<ParentT> {
            /**
             * Specifies the country code of the phone number.
             *
             * @param code the country code
             * @return the next stage of the definition
             */
            WithPhoneNumber<ParentT> withPhoneCountryCode(CountryPhoneCode code);
        }

        /**
         * The stage of contact definition allowing phone number to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithPhoneNumber<ParentT> {
            /**
             * Specifies the phone number.
             *
             * @param phoneNumber phone number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPhoneNumber(String phoneNumber);
        }

        /**
         * The stage of contact definition allowing fax number to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithFaxNumber<ParentT> {
            WithAttach<ParentT> withFaxNumber(String faxNumber);
        }

        /**
         * The stage of contact definition allowing organization to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithOrganization<ParentT> {
            WithAttach<ParentT> withOrganization(String organziation);
        }

        /**
         * The stage of contact definition allowing job title to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching
         */
        interface WithJobTitle<ParentT> {
            WithAttach<ParentT> withJobTitle(String jobTitle);
        }

        /**
         * The final stage of the domain contact definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the domain contact definition can be
         * attached to the parent domain definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>,
                DefinitionStages.WithOrganization<ParentT>,
                DefinitionStages.WithJobTitle<ParentT>,
                DefinitionStages.WithFaxNumber<ParentT> {
            Contact build();
        }
    }
}
