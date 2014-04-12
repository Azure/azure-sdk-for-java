/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * 
 */
class XmlCurrentStateSerializer implements CurrentStateSerializer {
    public XmlCurrentStateSerializer() {
    }

    public void serialize(CurrentState state, OutputStream stream) {
        try {
            JAXBContext context = JAXBContext.newInstance(GoalStateInfo.class
                    .getPackage().getName());
            Marshaller marshaller = context.createMarshaller();

            ObjectFactory factory = new ObjectFactory();
            CurrentStateInfo info = factory.createCurrentStateInfo();
            StatusLeaseInfo leaseInfo = factory.createStatusLeaseInfo();

            leaseInfo.setClientId(state.getClientId());

            if (state instanceof AcquireCurrentState) {
                AcquireCurrentState acquireState = (AcquireCurrentState) state;
                AcquireLeaseInfo acquire = factory.createAcquireLeaseInfo();

                acquire.setExpiration(acquireState.getExpiration());
                acquire.setIncarnation(acquireState.getIncarnation());

                switch (acquireState.getStatus()) {
                case BUSY:
                    acquire.setStatus(CurrentStatusEnum.BUSY);
                    break;
                case RECYCLE:
                    acquire.setStatus(CurrentStatusEnum.RECYCLE);
                    break;
                case STARTED:
                    acquire.setStatus(CurrentStatusEnum.STARTED);
                    break;
                case STOPPED:
                    acquire.setStatus(CurrentStatusEnum.STOPPED);
                    break;
                default:
                    throw new IllegalArgumentException();
                }

                leaseInfo.setAcquire(acquire);
            } else if (state instanceof ReleaseCurrentState) {
                leaseInfo.setRelease(factory.createStatusLeaseInfoRelease());
            }

            info.setStatusLease(leaseInfo);

            marshaller.marshal(factory.createCurrentState(info), stream);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
