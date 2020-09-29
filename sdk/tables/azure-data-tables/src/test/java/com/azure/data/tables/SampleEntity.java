package com.azure.data.tables;

import com.azure.data.tables.models.TableEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SampleEntity extends TableEntity {
    private byte[] byteField;
    private boolean booleanField;
    private OffsetDateTime dateTimeField;
    private double doubleField;
    private UUID uuidField;
    private int intField;
    private long longField;
    private String stringField;

    public SampleEntity(String partitionKey, String rowKey) {
        super(partitionKey, rowKey);
    }

    public int getIntField() {
        return intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public byte[] getByteField() {
        return byteField;
    }

    public void setByteField(byte[] byteField) {
        this.byteField = byteField;
    }

    public boolean isBooleanField() {
        return booleanField;
    }

    public boolean getBooleanField() {
        return booleanField;
    }

    public void setBooleanField(boolean booleanField) {
        this.booleanField = booleanField;
    }

    public OffsetDateTime getDateTimeField() {
        return dateTimeField;
    }

    public void setDateTimeField(OffsetDateTime dateTimeField) {
        this.dateTimeField = dateTimeField;
    }

    public double getDoubleField() {
        return doubleField;
    }

    public void setDoubleField(double doubleField) {
        this.doubleField = doubleField;
    }

    public UUID getUuidField() {
        return uuidField;
    }

    public void setUuidField(UUID uuidField) {
        this.uuidField = uuidField;
    }

    public long getLongField() {
        return longField;
    }

    public void setLongField(long longField) {
        this.longField = longField;
    }

    public String getStringField() {
        return stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }
}
