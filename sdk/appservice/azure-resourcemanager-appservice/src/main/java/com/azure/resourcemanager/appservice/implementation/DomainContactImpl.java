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
        innerModel().withAddressMailing(new Address());
    }

    @Override
    public String name() {
        return firstName() + " " + lastName();
    }

    @Override
    public Address addressMailing() {
        return innerModel().addressMailing();
    }

    @Override
    public String email() {
        return innerModel().email();
    }

    @Override
    public String fax() {
        return innerModel().fax();
    }

    @Override
    public String jobTitle() {
        return innerModel().jobTitle();
    }

    @Override
    public String firstName() {
        return innerModel().nameFirst();
    }

    @Override
    public String lastName() {
        return innerModel().nameLast();
    }

    @Override
    public String middleName() {
        return innerModel().nameMiddle();
    }

    @Override
    public String organization() {
        return innerModel().organization();
    }

    @Override
    public String phone() {
        return innerModel().phone();
    }

    @Override
    public AppServiceDomainImpl attach() {
        return parent().withRegistrantContact(innerModel());
    }

    @Override
    public DomainContactImpl withFirstName(String firstName) {
        innerModel().withNameFirst(firstName);
        return this;
    }

    @Override
    public DomainContactImpl withMiddleName(String middleName) {
        innerModel().withNameMiddle(middleName);
        return this;
    }

    @Override
    public DomainContactImpl withLastName(String lastName) {
        innerModel().withNameLast(lastName);
        return this;
    }

    @Override
    public DomainContactImpl withEmail(String email) {
        innerModel().withEmail(email);
        return this;
    }

    @Override
    public DomainContactImpl withAddressLine1(String addressLine1) {
        innerModel().addressMailing().withAddress1(addressLine1);
        return this;
    }

    @Override
    public DomainContactImpl withAddressLine2(String addressLine2) {
        innerModel().addressMailing().withAddress2(addressLine2);
        return this;
    }

    @Override
    public DomainContactImpl withCity(String city) {
        innerModel().addressMailing().withCity(city);
        return this;
    }

    @Override
    public DomainContactImpl withStateOrProvince(String stateOrProvince) {
        innerModel().addressMailing().withState(stateOrProvince);
        return this;
    }

    @Override
    public DomainContactImpl withCountry(CountryIsoCode country) {
        innerModel().addressMailing().withCountry(country.toString());
        return this;
    }

    @Override
    public DomainContactImpl withPostalCode(String postalCode) {
        innerModel().addressMailing().withPostalCode(postalCode);
        return this;
    }

    @Override
    public DomainContactImpl withPhoneCountryCode(CountryPhoneCode code) {
        innerModel().withPhone(code.toString() + ".");
        return this;
    }

    @Override
    public DomainContactImpl withPhoneNumber(String phoneNumber) {
        innerModel().withPhone(innerModel().phone() + phoneNumber);
        return this;
    }

    @Override
    public DomainContactImpl withFaxNumber(String faxNumber) {
        innerModel().withFax(faxNumber);
        return this;
    }

    @Override
    public DomainContactImpl withOrganization(String organziation) {
        innerModel().withOrganization(organziation);
        return this;
    }

    @Override
    public DomainContactImpl withJobTitle(String jobTitle) {
        innerModel().withJobTitle(jobTitle);
        return this;
    }

    @Override
    public Contact build() {
        return innerModel();
    }
}
