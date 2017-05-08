<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:param name="code_attr_name">csd-code</xsl:param>
    <xsl:param name="displayname_attr_name">originalText</xsl:param>
    <xsl:include href="iheyr4.xsl"/>
    <xsl:template match="/AuditMessages">
        <AuditMessages>
            <xsl:apply-templates select="@*|node()"/>
        </AuditMessages>
    </xsl:template>
    <xsl:template match="AuditMessage">
        <AuditMessage>
            <xsl:apply-templates select="@*|node()"/>
        </AuditMessage>
    </xsl:template>
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*/*/@code">
        <xsl:attribute name="csd-code">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>
    <xsl:template match="*/*/@displayName">
        <xsl:attribute name="originalText">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>
    <xsl:template match="AuditSourceIdentification[position() &lt; 2 and @code]">
        <AuditSourceIdentification>
            <xsl:attribute name="code"><xsl:value-of select="@code"/></xsl:attribute>
            <xsl:apply-templates select="@*[name() != 'code']|node()"/>
        </AuditSourceIdentification>
    </xsl:template>
    <xsl:template match="AuditSourceIdentification[position() &lt; 2 and not(@code)]">
        <AuditSourceIdentification>
            <xsl:apply-templates select="@*"/>
            <xsl:attribute name="code">
                <xsl:value-of select="./AuditSourceTypeCode[1]/@code"/>
            </xsl:attribute>
            <xsl:if test="./AuditSourceTypeCode[1]/@codeSystemName">
                <xsl:attribute name="codeSystemName">
                    <xsl:value-of select="./AuditSourceTypeCode[1]/@codeSystemName"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="./AuditSourceTypeCode[1]/@originalText">
                <xsl:attribute name="displayName">
                    <xsl:value-of select="./AuditSourceTypeCode[1]/@originalText"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="AuditSourceTypeCode[position() &gt; 1]"/>
            <xsl:apply-templates select="../AuditSourceIdentification[position() &gt; 1]" mode="append"/>
        </AuditSourceIdentification>
    </xsl:template>
    
    <xsl:template match="AuditSourceIdentification[position() &gt; 1]" mode="append">
        <xsl:apply-templates select="AuditSourceTypeCode"/>
    </xsl:template>
    <xsl:template match="AuditSourceIdentification[position() &gt; 1]"></xsl:template>
    
    <xsl:template match="AuditSourceTypeCode[not(@code)]">
        <AuditSourceTypeCode><xsl:value-of select="."/></AuditSourceTypeCode>
    </xsl:template>
    <xsl:template match="AuditSourceTypeCode[@code]">
        <AuditSourceTypeCode>
            <xsl:value-of select="@code"/>
            <xsl:if test="@codeSystemName">
                <xsl:text>^</xsl:text><xsl:value-of select="@codeSystemName"/>
            </xsl:if>
            <xsl:if test="@displayName">
                <xsl:text>^</xsl:text><xsl:value-of select="@displayName"/>
            </xsl:if>
        </AuditSourceTypeCode>
    </xsl:template>
    
    <xsl:template match="ParticipantObjectIdentification">
        <ParticipantObjectIdentification>
            <xsl:apply-templates select="@*|node()"/>
        </ParticipantObjectIdentification>
    </xsl:template>
    <xsl:template match="ParticipantObjectDescription">
        <xsl:apply-templates select="node()"></xsl:apply-templates>
    </xsl:template>

</xsl:stylesheet>
