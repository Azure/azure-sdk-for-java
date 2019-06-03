package com.azure.keyvault.certificates.models;

import java.util.List;

public class Issuer extends IssuerBase {

    private String accountId;
    private String password;
    private String organizationId;
    private List<Administrator> administrators;

    public Issuer(String name){
        super.name(name);
    }

    public String accountId() {
        return accountId;
    }

    public void accountId(String accountId) {
        this.accountId = accountId;
    }

    public String password() {
        return password;
    }

    public void password(String password) {
        this.password = password;
    }

    public String organizationId() {
        return organizationId;
    }

    public void organizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public List<Administrator> administrators() {
        return administrators;
    }

    public void administrators(List<Administrator> administrators) {
        this.administrators = administrators;
    }
}
