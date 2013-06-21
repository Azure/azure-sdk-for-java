package com.microsoft.windowsazure.services.management.implementation;

import com.microsoft.windowsazure.services.management.models.AffinityGroupInfo;

public class AffinityGroupInfoFactory {

    public static AffinityGroupInfo getItem(AffinityGroup affinityGroup) {
        return new AffinityGroupInfo().setName(affinityGroup.getName()).setLabel(affinityGroup.getLabel())
                .setLocation(affinityGroup.getLocation()).setDescription(affinityGroup.getDescription());
    }

}
