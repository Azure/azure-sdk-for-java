package com.microsoft.windowsazure.services.blob.models;

public class GetContainerACLResult {
    private ContainerACL containerACL;

    public ContainerACL getContainerACL() {
        return containerACL;
    }

    public void setValue(ContainerACL containerACL) {
        this.containerACL = containerACL;
    }
}
