/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.website;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryISOCode;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryPhoneCode;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * A domain contact definition.
 */
@Fluent
public interface DomainContact extends
    Wrapper<Contact>,
    ChildResource<Domain> {

    /**
     * @return contact's mailing address
     */
    Address addressMailing();

    /**
     * @return contact's email address
     */
    String email();

    /**
     * @return contact's fax number
     */
    String fax();

    /**
     * @return contact's job title
     */
    String jobTitle();

    /**
     * @return contact's first name
     */
    String firstName();

    /**
     * @return contact's last name
     */
    String lastName();

    /**
     * @return contact's middle name
     */
    String middleName();

    /**
     * @return contact's organization
     */
    String organization();

    /**
     * @return contact's phone number
     */
    String phone();

    /**
     * The entirety of a domain contact definition.
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
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

    /**
     * Grouping of security rule definition stages applicable as part of a network security group creation.
     */
    interface DefinitionStages {
        /**
         * The first stage of a security rule definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithFirstName<ParentT> {
        }

        interface WithFirstName<ParentT> {
            WithMiddleName<ParentT> withFirstName(String firstName);
        }

        interface WithMiddleName<ParentT> extends WithLastName<ParentT> {
            WithLastName<ParentT> withMiddleName(String middleName);
        }

        interface WithLastName<ParentT> {
            WithEmail<ParentT> withLastName(String lastName);
        }

        interface WithEmail<ParentT> {
            WithAddressLine1<ParentT> withEmail(String email);
        }

        interface WithAddressLine1<ParentT> {
            WithAddressLine2<ParentT> withAddressLine1(String addressLine1);
        }

        interface WithAddressLine2<ParentT> extends WithCity<ParentT> {
            WithCity<ParentT> withAddressLine2(String addressLine2);
        }

        interface WithCity<ParentT> {
            WithStateOrProvince<ParentT> withCity(String city);
        }

        interface WithStateOrProvince<ParentT> {
            WithCountry<ParentT> withStateOrProvince(String stateOrProvince);
        }

        interface WithCountry<ParentT> {
            WithPostalCode<ParentT> withCountry(CountryISOCode country);
        }

        interface WithPostalCode<ParentT> {
            WithPhoneCountryCode<ParentT> withPostalCode(String postalCode);
        }

        interface WithPhoneCountryCode<ParentT> {
            WithPhoneNumber<ParentT> withPhoneCountryCode(CountryPhoneCode code);
        }

        interface WithPhoneNumber<ParentT> {
            WithAttach<ParentT> withPhoneNumber(String phoneNumber);
        }

        interface WithFaxNumber<ParentT> {
            WithAttach<ParentT> withFaxNumber(String faxNumber);
        }

        interface WithOrganization<ParentT> {
            WithAttach<ParentT> withOrganziation(String organziation);
        }

        interface WithJobTitle<ParentT> {
            WithAttach<ParentT> withJobTitle(String jobTitle);
        }

        /** The final stage of the security rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the security rule definition
         * can be attached to the parent network security group definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT>,
                DefinitionStages.WithOrganization<ParentT>,
                DefinitionStages.WithJobTitle<ParentT>,
                DefinitionStages.WithFaxNumber<ParentT> {
            Contact build();
        }
    }
 }
