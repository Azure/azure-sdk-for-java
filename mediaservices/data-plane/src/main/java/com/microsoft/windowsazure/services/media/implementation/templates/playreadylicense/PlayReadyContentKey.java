package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlSeeAlso({ ContentEncryptionKeyFromHeader.class, ContentEncryptionKeyFromKeyIdentifier.class })
public abstract class PlayReadyContentKey {

}
