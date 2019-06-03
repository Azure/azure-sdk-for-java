package com.azure.keyvault.certificates.models;

public class Contact {

    private String firstName;

    private String lastName;
    private String email;

    public Contact(String firstName, String lastName, String email){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String firstName() {
        return firstName;
    }

    public void firstName(String firstName) {
        this.firstName = firstName;
    }

    public String lastName() {
        return lastName;
    }

    public void lastName(String lastName) {
        this.lastName = lastName;
    }

    public String email() {
        return email;
    }

    public void email(String email) {
        this.email = email;
    }

}
