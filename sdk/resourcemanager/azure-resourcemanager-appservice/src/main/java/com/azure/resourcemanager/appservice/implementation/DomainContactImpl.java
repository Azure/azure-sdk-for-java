// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.models.Address;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.appservice.models.Contact;
import com.azure.resourcemanager.appservice.models.DomainContact;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryPhoneCode;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/** Implementation for {@link DomainContact} and its create and update interfaces. */
class DomainContactImpl extends ChildResourceImpl<Contact, AppServiceDomainImpl, AppServiceDomain>
    implements DomainContact, DomainContact.Definition<AppServiceDomain.DefinitionStages.WithCreate> {

    DomainContactImpl(Contact inner, AppServiceDomainImpl parent) {
        super(inner, parent);
        inner().withAddressMailing(new Address());
    }

    @Override
    public String name() {
        return firstName() + " " + lastName();
    }

    @Override
    public Address addressMailing() {
        return inner().addressMailing();
    }

    @Override
    public String email() {
        return inner().email();
    }

    @Override
    public String fax() {
        return inner().fax();
    }

    @Override
    public String jobTitle() {
        return inner().jobTitle();
    }

    @Override
    public String firstName() {
        return inner().nameFirst();
    }

    @Override
    public String lastName() {
        return inner().nameLast();
    }

    @Override
    public String middleName() {
        return inner().nameMiddle();
    }

    @Override
    public String organization() {
        return inner().organization();
    }

    @Override
    public String phone() {
        return inner().phone();
    }

    @Override
    public AppServiceDomainImpl attach() {
        return parent().withRegistrantContact(inner());
    }

    @Override
    public DomainContactImpl withFirstName(String firstName) {
        inner().withNameFirst(firstName);
        return this;
    }

    @Override
    public DomainContactImpl withMiddleName(String middleName) {
        inner().withNameMiddle(middleName);
        return this;
    }

    @Override
    public DomainContactImpl withLastName(String lastName) {
        inner().withNameLast(lastName);
        return this;
    }

    @Override
    public DomainContactImpl withEmail(String email) {
        inner().withEmail(email);
        return this;
    }

    @Override
    public DomainContactImpl withAddressLine1(String addressLine1) {
        inner().addressMailing().withAddress1(addressLine1);
        return this;
    }

    @Override
    public DomainContactImpl withAddressLine2(String addressLine2) {
        inner().addressMailing().withAddress2(addressLine2);
        return this;
    }

    @Override
    public DomainContactImpl withCity(String city) {
        inner().addressMailing().withCity(city);
        return this;
    }

    @Override
    public DomainContactImpl withStateOrProvince(String stateOrProvince) {
        inner().addressMailing().withState(stateOrProvince);
        return this;
    }

    @Override
    public DomainContactImpl withCountry(CountryIsoCode country) {
        inner().addressMailing().withCountry(country.toString());
        return this;
    }

    @Override
    public DomainContactImpl withPostalCode(String postalCode) {
        inner().addressMailing().withPostalCode(postalCode);
        return this;
    }

    @Override
    public DomainContactImpl withPhoneCountryCode(CountryPhoneCode code) {
        inner().withPhone(code.toString() + ".");
        return this;
    }

    @Override
    public DomainContactImpl withPhoneNumber(String phoneNumber) {
        inner().withPhone(inner().phone() + phoneNumber);
        return this;
    }

    @Override
    public DomainContactImpl withFaxNumber(String faxNumber) {
        inner().withFax(faxNumber);
        return this;
    }

    @Override
    public DomainContactImpl withOrganization(String organziation) {
        inner().withOrganization(organziation);
        return this;
    }

    @Override
    public DomainContactImpl withJobTitle(String jobTitle) {
        inner().withJobTitle(jobTitle);
        return this;
    }

    @Override
    public Contact build() {
        return inner();
    }
}
