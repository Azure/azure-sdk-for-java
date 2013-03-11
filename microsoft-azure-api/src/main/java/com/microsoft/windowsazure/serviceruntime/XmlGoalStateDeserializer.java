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
package com.microsoft.windowsazure.serviceruntime;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.InputSource;

/**
 * 
 */
class XmlGoalStateDeserializer {
    public XmlGoalStateDeserializer() {
    }

    public GoalState deserialize(String document) {
        try {
            JAXBContext context = JAXBContext.newInstance(GoalStateInfo.class.getPackage().getName());
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputSource source = new InputSource(new StringReader(document));

            @SuppressWarnings("unchecked")
            GoalStateInfo goalStateInfo = ((JAXBElement<GoalStateInfo>) unmarshaller.unmarshal(source)).getValue();

            return new GoalState(goalStateInfo.incarnation, Enum.valueOf(ExpectedState.class,
                    goalStateInfo.expectedState.toString()), goalStateInfo.getRoleEnvironmentPath(),
                    goalStateInfo.getDeadline(), goalStateInfo.getCurrentStateEndpoint());

        }
        catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
