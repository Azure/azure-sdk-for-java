package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public final class MediaServicesLicenseTemplateSerializer {

    private MediaServicesLicenseTemplateSerializer() {

    }

    public static String serialize(PlayReadyLicenseResponseTemplate template) throws JAXBException {

        validateLicenseResponseTemplate(template);

        StringWriter writer = new StringWriter();
        JAXBContext context = JAXBContext.newInstance(PlayReadyLicenseResponseTemplate.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
            @Override
            public String[] getPreDeclaredNamespaceUris() {
                return new String[] { 
                        XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI 
                        };
            }

            @Override
            public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                if (namespaceUri.equals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI)) {
                    return "i";
                }
                return suggestion;
            }
        });
        m.marshal(template, writer);
        return writer.toString();
    }

    public static PlayReadyLicenseResponseTemplate deserialize(String xml) throws JAXBException {
        try {
            return deserialize(xml, null);
        } catch (SAXException e) {
            // never reached.
            return null;
        }
    }

    public static PlayReadyLicenseResponseTemplate deserialize(String xml, String validationSchemaFileName)
            throws JAXBException, SAXException {
        JAXBContext context = JAXBContext.newInstance(PlayReadyLicenseResponseTemplate.class);
        Unmarshaller u = context.createUnmarshaller();
        if (validationSchemaFileName != null) {
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = factory.newSchema(new File(validationSchemaFileName));
            u.setSchema(schema);
        }
        PlayReadyLicenseResponseTemplate template = (PlayReadyLicenseResponseTemplate) u
                .unmarshal(new StringReader(xml));

        validateLicenseResponseTemplate(template);

        return template;
    }

    private static void validateLicenseResponseTemplate(PlayReadyLicenseResponseTemplate templateToValidate) {
        // Validate the PlayReadyLicenseResponseTemplate has at least one
        // license
        if (templateToValidate.getLicenseTemplates().size() <= 0) {
            throw new IllegalArgumentException(ErrorMessages.AT_LEAST_ONE_LICENSE_TEMPLATE_REQUIRED);
        }

        for (PlayReadyLicenseTemplate template : templateToValidate.getLicenseTemplates()) {
            // This is actually enforced in the DataContract with the IsRequired
            // attribute
            // so this check should never fail.
            if (template.getContentKey() == null) {
                throw new IllegalArgumentException(ErrorMessages.PLAY_READY_CONTENT_KEY_REQUIRED);
            }

            // A PlayReady license must have at least one Right in it. Today we
            // only
            // support the PlayRight so it is required. In the future we might
            // support
            // other types of rights (CopyRight, perhaps an extensible Right,
            // whatever)
            // so we enforce this in code and not in the DataContract itself.
            if (template.getPlayRight() == null) {
                throw new IllegalArgumentException(ErrorMessages.PLAY_READY_PLAY_RIGHT_REQUIRED);
            }

            //
            // Per the PlayReady Compliance rules (section 3.8 - Output Control
            // for Unknown Outputs), passing content to
            // unknown output is prohibited if the
            // DigitalVideoOnlyContentRestriction is enabled.
            //
            if (template.getPlayRight().isDigitalVideoOnlyContentRestriction()) {
                if ((template.getPlayRight()
                        .getAllowPassingVideoContentToUnknownOutput() == UnknownOutputPassingOption.Allowed)
                        || (template.getPlayRight()
                                .getAllowPassingVideoContentToUnknownOutput() == UnknownOutputPassingOption.AllowedWithVideoConstriction)) {
                    throw new IllegalArgumentException(
                            ErrorMessages.DIGITAL_VIDEO_ONLY_MUTUALLY_EXCLUSIVE_WITH_PASSING_TO_UNKNOWN_OUTPUT_ERROR);
                }
            }

            if (template.getLicenseType() == PlayReadyLicenseType.Nonpersistent) {
                //
                // The PlayReady Rights Manager SDK will return an error if you
                // try to specify a license
                // that is non-persistent and has a first play expiration set.
                // The event log message related
                // to the error will say "LicenseGenerationFailure:
                // FirstPlayExpiration can not be set on Non
                // Persistent license PlayRight."
                //
                if (template.getPlayRight().getFirstPlayExpiration() != null) {
                    throw new IllegalArgumentException(
                            ErrorMessages.FIRST_PLAY_EXPIRATION_CANNOT_BE_SET_ON_NON_PERSISTENT_LICENSE);
                }

                //
                // The PlayReady Rights Manager SDK will return an error if you
                // try to specify a license
                // that is non-persistent and has a GracePeriod set.
                //
                if (template.getGracePeriod() != null) {
                    throw new IllegalArgumentException(
                            ErrorMessages.GRACE_PERIOD_CANNOT_BE_SET_ON_NON_PERSISTENT_LICENSE);
                }

                //
                // The PlayReady Rights Manager SDK will return an error if you
                // try to specify a license
                // that is non-persistent and has a GracePeriod set. The event
                // log message related
                // to the error will say "LicenseGenerationFailure: BeginDate or
                // ExpirationDate should not be set
                // on Non Persistent licenses"
                //
                if (template.getBeginDate() != null) {
                    throw new IllegalArgumentException(
                            ErrorMessages.BEGIN_DATE_CANNOT_BE_SET_ON_NON_PERSISTENT_LICENSE);
                }

                //
                // The PlayReady Rights Manager SDK will return an error if you
                // try to specify a license
                // that is non-persistent and has a GracePeriod set. The event
                // log message related
                // to the error will say "LicenseGenerationFailure: BeginDate or
                // ExpirationDate should not be set
                // on Non Persistent licenses"
                //
                if (template.getExpirationDate() != null) {
                    throw new IllegalArgumentException(
                            ErrorMessages.EXPIRATION_CANNOT_BE_SET_ON_NON_PERSISTENT_LICENSE);
                }
            }
        }

    }
}
