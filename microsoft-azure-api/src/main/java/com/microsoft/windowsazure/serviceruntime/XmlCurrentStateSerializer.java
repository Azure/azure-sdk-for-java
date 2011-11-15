/**
 * 
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
