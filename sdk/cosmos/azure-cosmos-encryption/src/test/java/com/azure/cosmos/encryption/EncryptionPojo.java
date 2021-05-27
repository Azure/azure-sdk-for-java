// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import java.util.List;

public class EncryptionPojo {
    private String id;
    private String mypk;
    private String nonSensitive;
    private String sensitiveString;
    private int sensitiveInt;
    private float sensitiveFloat;
    private long sensitiveLong;
    private double sensitiveDouble;
    private boolean sensitiveBoolean;
    private EncryptionPojo sensitiveNestedPojo;
    private int[] sensitiveIntArray;
    private String[] sensitiveStringArray;
    private String[][][] sensitiveString3DArray;
    private EncryptionPojo[][] sensitiveChildPojo2DArray;
    private List<EncryptionPojo> sensitiveChildPojoList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMypk() {
        return mypk;
    }

    public void setMypk(String mypk) {
        this.mypk = mypk;
    }

    public String getNonSensitive() {
        return nonSensitive;
    }

    public void setNonSensitive(String nonSensitive) {
        this.nonSensitive = nonSensitive;
    }

    public String getSensitiveString() {
        return sensitiveString;
    }

    public void setSensitiveString(String sensitiveString) {
        this.sensitiveString = sensitiveString;
    }

    public int getSensitiveInt() {
        return sensitiveInt;
    }

    public void setSensitiveInt(int sensitiveInt) {
        this.sensitiveInt = sensitiveInt;
    }

    public float getSensitiveFloat() {
        return sensitiveFloat;
    }

    public void setSensitiveFloat(float sensitiveFloat) {
        this.sensitiveFloat = sensitiveFloat;
    }

    public long getSensitiveLong() {
        return sensitiveLong;
    }

    public void setSensitiveLong(long sensitiveLong) {
        this.sensitiveLong = sensitiveLong;
    }

    public double getSensitiveDouble() {
        return sensitiveDouble;
    }

    public void setSensitiveDouble(double sensitiveDouble) {
        this.sensitiveDouble = sensitiveDouble;
    }

    public boolean isSensitiveBoolean() {
        return sensitiveBoolean;
    }

    public void setSensitiveBoolean(boolean sensitiveBoolean) {
        this.sensitiveBoolean = sensitiveBoolean;
    }

    public EncryptionPojo getSensitiveNestedPojo() {
        return sensitiveNestedPojo;
    }

    public void setSensitiveNestedPojo(EncryptionPojo sensitiveNestedPojo) {
        this.sensitiveNestedPojo = sensitiveNestedPojo;
    }

    public int[] getSensitiveIntArray() {
        return sensitiveIntArray;
    }

    public void setSensitiveIntArray(int[] sensitiveIntArray) {
        this.sensitiveIntArray = sensitiveIntArray;
    }

    public String[] getSensitiveStringArray() {
        return sensitiveStringArray;
    }

    public void setSensitiveStringArray(String[] sensitiveStringArray) {
        this.sensitiveStringArray = sensitiveStringArray;
    }

    public String[][][] getSensitiveString3DArray() {
        return sensitiveString3DArray;
    }

    public void setSensitiveString3DArray(String[][][] sensitiveString3DArray) {
        this.sensitiveString3DArray = sensitiveString3DArray;
    }

    public EncryptionPojo[][] getSensitiveChildPojo2DArray() {
        return sensitiveChildPojo2DArray;
    }

    public void setSensitiveChildPojo2DArray(EncryptionPojo[][] sensitiveChildPojo2DArray) {
        this.sensitiveChildPojo2DArray = sensitiveChildPojo2DArray;
    }

    public List<EncryptionPojo> getSensitiveChildPojoList() {
        return sensitiveChildPojoList;
    }

    public void setSensitiveChildPojoList(List<EncryptionPojo> sensitiveChildPojoList) {
        this.sensitiveChildPojoList = sensitiveChildPojoList;
    }
}
