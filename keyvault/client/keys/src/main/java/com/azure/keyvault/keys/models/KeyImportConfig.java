package com.azure.keyvault.keys.models;


import com.azure.keyvault.webkey.JsonWebKey;

public class KeyImportConfig extends KeyBase {

    private JsonWebKey keyMaterial;

    private Boolean hsm;

    public KeyImportConfig(String name, JsonWebKey keyMaterial){
        super.name = name;
        this.keyMaterial = keyMaterial;
    }

    public KeyImportConfig hsm(Boolean hsm){
        this.hsm = hsm;
        return this;
    }

    public Boolean hsm(){
        return this.hsm;
    }

    public JsonWebKey keyMaterial(){
        return keyMaterial;
    }

}
