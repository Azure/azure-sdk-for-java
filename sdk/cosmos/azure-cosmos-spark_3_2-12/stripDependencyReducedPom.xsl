<?xml version="1.0"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  version="1.0"
  xmlns:pom="http://maven.apache.org/POM/4.0.0">

    <xsl:output omit-xml-declaration="no" indent="yes"/>

    <!-- TEMPLATE to copy all nodes as the default -->
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- TEMPLATE overriding the default to skip certain nodes (effectively deleting it) -->
    <xsl:template match="pom:parent" />
    <xsl:template match="pom:build" />
    <xsl:template match="pom:profiles" />
    <xsl:template match="pom:properties" />
</xsl:stylesheet>
