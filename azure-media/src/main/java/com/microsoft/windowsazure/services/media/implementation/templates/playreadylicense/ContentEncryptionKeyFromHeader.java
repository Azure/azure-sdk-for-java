package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Configures the license server to embed the content key identified in the content header sent with the license
 * request in the returned license. This is the typical content key configuration.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContentEncryptionKeyFromHeader")
public class ContentEncryptionKeyFromHeader extends PlayReadyContentKey {

}
