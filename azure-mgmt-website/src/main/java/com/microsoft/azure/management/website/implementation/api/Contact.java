/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Contact information for domain registration. If 'Domain Privacy' option is
 * not selected then the contact information will be  be made publicly
 * available through the Whois directories as per ICANN requirements.
 */
public class Contact {
    /**
     * Mailing address.
     */
    private Address addressMailing;

    /**
     * Email address.
     */
    private String email;

    /**
     * Fax number.
     */
    private String fax;

    /**
     * Job title.
     */
    private String jobTitle;

    /**
     * First name.
     */
    private String nameFirst;

    /**
     * Last name.
     */
    private String nameLast;

    /**
     * Middle name.
     */
    private String nameMiddle;

    /**
     * Organization.
     */
    private String organization;

    /**
     * Phone number.
     */
    private String phone;

    /**
     * Get the addressMailing value.
     *
     * @return the addressMailing value
     */
    public Address addressMailing() {
        return this.addressMailing;
    }

    /**
     * Set the addressMailing value.
     *
     * @param addressMailing the addressMailing value to set
     * @return the Contact object itself.
     */
    public Contact withAddressMailing(Address addressMailing) {
        this.addressMailing = addressMailing;
        return this;
    }

    /**
     * Get the email value.
     *
     * @return the email value
     */
    public String email() {
        return this.email;
    }

    /**
     * Set the email value.
     *
     * @param email the email value to set
     * @return the Contact object itself.
     */
    public Contact withEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Get the fax value.
     *
     * @return the fax value
     */
    public String fax() {
        return this.fax;
    }

    /**
     * Set the fax value.
     *
     * @param fax the fax value to set
     * @return the Contact object itself.
     */
    public Contact withFax(String fax) {
        this.fax = fax;
        return this;
    }

    /**
     * Get the jobTitle value.
     *
     * @return the jobTitle value
     */
    public String jobTitle() {
        return this.jobTitle;
    }

    /**
     * Set the jobTitle value.
     *
     * @param jobTitle the jobTitle value to set
     * @return the Contact object itself.
     */
    public Contact withJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    /**
     * Get the nameFirst value.
     *
     * @return the nameFirst value
     */
    public String nameFirst() {
        return this.nameFirst;
    }

    /**
     * Set the nameFirst value.
     *
     * @param nameFirst the nameFirst value to set
     * @return the Contact object itself.
     */
    public Contact withNameFirst(String nameFirst) {
        this.nameFirst = nameFirst;
        return this;
    }

    /**
     * Get the nameLast value.
     *
     * @return the nameLast value
     */
    public String nameLast() {
        return this.nameLast;
    }

    /**
     * Set the nameLast value.
     *
     * @param nameLast the nameLast value to set
     * @return the Contact object itself.
     */
    public Contact withNameLast(String nameLast) {
        this.nameLast = nameLast;
        return this;
    }

    /**
     * Get the nameMiddle value.
     *
     * @return the nameMiddle value
     */
    public String nameMiddle() {
        return this.nameMiddle;
    }

    /**
     * Set the nameMiddle value.
     *
     * @param nameMiddle the nameMiddle value to set
     * @return the Contact object itself.
     */
    public Contact withNameMiddle(String nameMiddle) {
        this.nameMiddle = nameMiddle;
        return this;
    }

    /**
     * Get the organization value.
     *
     * @return the organization value
     */
    public String organization() {
        return this.organization;
    }

    /**
     * Set the organization value.
     *
     * @param organization the organization value to set
     * @return the Contact object itself.
     */
    public Contact withOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    /**
     * Get the phone value.
     *
     * @return the phone value
     */
    public String phone() {
        return this.phone;
    }

    /**
     * Set the phone value.
     *
     * @param phone the phone value to set
     * @return the Contact object itself.
     */
    public Contact withPhone(String phone) {
        this.phone = phone;
        return this;
    }

}
