package com.azure.spring.sample.cosmos.multi.database.multiple.account.repository.mysql;

import javax.persistence.Entity;

@Entity
public class MysqlUser {

    @javax.persistence.Id
    private String id;

    private String email;

    private String name;

    private String address;

    public MysqlUser(){

    }

    public MysqlUser(String id, String email, String name, String address) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String toString() {
        return String.format("%s: %s %s %s", this.id, this.email, this.name, this.address);
    }
}
