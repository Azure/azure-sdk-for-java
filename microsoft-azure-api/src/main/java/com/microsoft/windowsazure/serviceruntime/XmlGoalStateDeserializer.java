/**
 * 
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
