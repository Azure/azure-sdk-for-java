package com.microsoft.windowsazure.services.media.implementation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

public class BatchMimeMultipart extends MimeMultipart {
	
	public BatchMimeMultipart(SetBoundaryMultipartDataSource setBoundaryMultipartDataSource) throws MessagingException {
		super(setBoundaryMultipartDataSource);
	}

	@Override
	public synchronized void writeTo(OutputStream os)
			throws IOException, MessagingException {
		
		resetInputStreams();
		super.writeTo(os);
	}
	
	// reset all input streams to allow redirect filter to write the output twice
	private void resetInputStreams() throws IOException, MessagingException {
		for (int ix = 0; ix < this.getCount(); ix++) {
			BodyPart part = this.getBodyPart(ix);
			if (part.getContent() instanceof MimeMultipart) {
				MimeMultipart subContent = (MimeMultipart) part.getContent();
				for (int jx = 0; jx < subContent.getCount(); jx++) {
					BodyPart subPart = subContent.getBodyPart(jx);
					if (subPart.getContent() instanceof ByteArrayInputStream) {
						((ByteArrayInputStream) subPart.getContent()).reset();
					}
				}
			}
		}
	}
}
