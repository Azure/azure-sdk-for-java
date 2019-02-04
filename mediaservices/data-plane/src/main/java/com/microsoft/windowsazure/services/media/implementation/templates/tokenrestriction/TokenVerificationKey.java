package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlSeeAlso({ SymmetricVerificationKey.class, X509CertTokenVerificationKey.class })
public abstract class TokenVerificationKey {

}
