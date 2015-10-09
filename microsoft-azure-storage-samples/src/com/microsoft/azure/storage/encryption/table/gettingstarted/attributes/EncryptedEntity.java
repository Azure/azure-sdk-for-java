package com.microsoft.azure.storage.encryption.table.gettingstarted.attributes;

import com.microsoft.azure.storage.table.Encrypt;
import com.microsoft.azure.storage.table.TableServiceEntity;

public class EncryptedEntity extends TableServiceEntity {

    private String encryptedProperty1;
    private String encryptedProperty2;
    private String notEncryptedProperty;
    private int notEncryptedIntProperty;

    @Encrypt
    public String getEncryptedProperty1() {
        return this.encryptedProperty1;
    }

    @Encrypt
    public String getEncryptedProperty2() {
        return this.encryptedProperty2;
    }

    public String getNotEncryptedProperty() {
        return this.notEncryptedProperty;
    }

    public int getNotEncryptedIntProperty() {
        return this.notEncryptedIntProperty;
    }

    @Encrypt
    public void setEncryptedProperty1(String encryptedProperty1) {
        this.encryptedProperty1 = encryptedProperty1;
    }

    @Encrypt
    public void setEncryptedProperty2(String encryptedProperty2) {
        this.encryptedProperty2 = encryptedProperty2;
    }

    public void setNotEncryptedProperty(String notEncryptedProperty) {
        this.notEncryptedProperty = notEncryptedProperty;
    }

    public void setNotEncryptedIntProperty(int notEncryptedIntProperty) {
        this.notEncryptedIntProperty = notEncryptedIntProperty;
    }

    public EncryptedEntity() {

    }

    public EncryptedEntity(String pk, String rk) {
        super(pk, rk);
    }

    public void Populate() {
        this.setEncryptedProperty1("");
        this.setEncryptedProperty2("foo");
        this.setNotEncryptedProperty("b");
        this.setNotEncryptedIntProperty(1234);
    }
}
