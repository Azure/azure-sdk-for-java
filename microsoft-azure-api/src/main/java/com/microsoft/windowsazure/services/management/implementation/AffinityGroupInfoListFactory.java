/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.management.implementation;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.windowsazure.services.management.models.AffinityGroupInfo;

/**
 * A factory for creating AffinityGroupInfoList objects.
 */
public class AffinityGroupInfoListFactory {

    /**
     * Gets the item.
     * 
     * @param affinityGroups
     *            the affinity groups
     * @return the item
     */
    public static List<AffinityGroupInfo> getItem(AffinityGroups affinityGroups) {
        List<AffinityGroupInfo> result = new ArrayList<AffinityGroupInfo>();
        List<AffinityGroup> affinityGroupList = affinityGroups.getAffinityGroups();
        for (AffinityGroup affinityGroup : affinityGroupList) {
            AffinityGroupInfo affinityGroupInfo = AffinityGroupInfoFactory.getItem(affinityGroup);
            result.add(affinityGroupInfo);
        }
        return result;
    }

}
