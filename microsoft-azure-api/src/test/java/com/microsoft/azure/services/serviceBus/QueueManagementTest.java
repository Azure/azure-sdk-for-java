package com.microsoft.azure.services.serviceBus;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.microsoft.azure.services.serviceBus.contract.EntryModel;
import com.microsoft.azure.services.serviceBus.contract.QueueDescription;
import com.microsoft.azure.services.serviceBus.contract.ServiceBusContract;
import com.sun.syndication.feed.atom.Entry;


public class QueueManagementTest {
	@Test
	public void testGetQueueAcquiresDescriptionFromServer() {
		// Arrange
		ServiceBusContract contract = mock(ServiceBusContract.class);

		EntryModel<QueueDescription> entryModel = new EntryModel<QueueDescription>(new Entry(), new QueueDescription());
		when(contract.getQueue("Hello")).thenReturn(entryModel);

		entryModel.getModel().setMessageCount(73L);
		
		// Act
		ServiceBusClient client = new ServiceBusClient(contract);
		Queue helloQueue = client.getQueue("Hello");
		
		// Assert
		assertEquals(73, helloQueue.getMessageCount().longValue());
	}
	
	@Test
	public void queueCreateSendsCreateQueueDescriptionMessage() throws DatatypeConfigurationException {
		// Arrange
		ServiceBusContract contract = mock(ServiceBusContract.class);

		// Act
		ServiceBusClient client = new ServiceBusClient(contract);
		Queue helloQueue = new Queue(client, "MyNewQueue");
		helloQueue.setLockDuration(DatatypeFactory.newInstance().newDuration(60 * 1000L));
		helloQueue.setMaxSizeInMegabytes(42L);
		helloQueue.commit();
		
		// Assert
		ArgumentCaptor<EntryModel> createArg = ArgumentCaptor.forClass(EntryModel.class);
		verify(contract).createQueue(createArg.capture());
		Entry entry = createArg.getValue().getEntry();
		QueueDescription model = (QueueDescription) createArg.getValue().getModel();
		
		assertEquals("MyNewQueue", entry.getTitle());
		assertEquals(DatatypeFactory.newInstance().newDuration(60 * 1000L), model.getLockDuration());
		assertEquals(42, model.getMaxSizeInMegabytes().longValue());
	}
}
