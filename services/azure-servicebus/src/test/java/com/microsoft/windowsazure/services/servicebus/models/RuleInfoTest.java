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
package com.microsoft.windowsazure.services.servicebus.models;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Test;

import com.microsoft.windowsazure.services.servicebus.implementation.Filter;
import com.microsoft.windowsazure.services.servicebus.implementation.RuleAction;

public class RuleInfoTest {

    @Test
    public void testGetSetFilter() {
        // Arrange
        Filter expectedFilter = new Filter();
        RuleInfo RuleInfo = new RuleInfo();

        // Act
        Filter actualFilter = RuleInfo.setFilter(expectedFilter).getFilter();

        // Assert
        assertEquals(expectedFilter, actualFilter);

    }

    @Test
    public void testGetSetAction() {
        // Arrange
        RuleAction expectedAction = new RuleAction();
        RuleInfo RuleInfo = new RuleInfo();

        // Act
        RuleAction actualAction = RuleInfo.setAction(expectedAction)
                .getAction();

        // Assert
        assertEquals(expectedAction, actualAction);
    }

    @Test
    public void testGetSetTag() {
        // Arrange
        String expectedTag = "expectedTag";
        RuleInfo RuleInfo = new RuleInfo();

        // Act
        String actualTag = RuleInfo.setTag(expectedTag).getTag();

        // Assert
        assertEquals(expectedTag, actualTag);
    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "expectedName";
        RuleInfo RuleInfo = new RuleInfo();

        // Act
        String actualName = RuleInfo.setName(expectedName).getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetCreatedAt() {
        // Arrange
        Calendar expectedCreatedAt = Calendar.getInstance();
        RuleInfo RuleInfo = new RuleInfo();

        // Act
        Calendar actualCreatedAt = RuleInfo.setCreatedAt(expectedCreatedAt)
                .getCreatedAt();

        // Assert
        assertEquals(expectedCreatedAt, actualCreatedAt);
    }

}
