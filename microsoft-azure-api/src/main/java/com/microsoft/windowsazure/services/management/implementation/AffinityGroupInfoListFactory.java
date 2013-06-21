package com.microsoft.windowsazure.services.management.implementation;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.windowsazure.services.management.models.AffinityGroupInfo;

public class AffinityGroupInfoListFactory {

    public static List<AffinityGroupInfo> getItem(AffinityGroups affinityGroups) {
        List<AffinityGroupInfo> result = new ArrayList<AffinityGroupInfo>();
        List<AffinityGroup> affinityGroupList = affinityGroups.getAffinityGroup();
        for (AffinityGroup affinityGroup : affinityGroupList) {
            AffinityGroupInfo affinityGroupInfo = AffinityGroupInfoFactory.getItem(affinityGroup);
            result.add(affinityGroupInfo);
        }
        return result;
    }

}
