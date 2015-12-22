<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:param name="code_attr_name">code</xsl:param>
    <xsl:param name="displayname_attr_name">displayName</xsl:param>
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
    <xsl:template match="*/*/@csd-code">
        <xsl:attribute name="code">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>
    <xsl:template match="*/*/@originalText">
        <xsl:if test="string-length() &gt; 0">
            <xsl:attribute name="displayName">
                <xsl:value-of select="."/>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*/*/@codeSystemName">
        <xsl:if test="string-length() &gt; 0">
            <xsl:attribute name="codeSystemName">
                <xsl:value-of select="."/>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>
    <xsl:template match="AuditSourceIdentification">
        <AuditSourceIdentification>
            <xsl:attribute name="AuditSourceID">
                <xsl:value-of select="@AuditSourceID"/>
            </xsl:attribute>
            <xsl:if test="@AuditEnterpriseSiteID">
                <xsl:attribute name="AuditEnterpriseSiteID">
                    <xsl:value-of select="@AuditEnterpriseSiteID"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@code">
                <AuditSourceTypeCode>
                    <xsl:attribute name="code">
                        <xsl:value-of select="@code"/>
                    </xsl:attribute>
                    <xsl:if test="@codeSystemName">
                        <xsl:attribute name="codeSystemName">
                            <xsl:value-of select="@codeSystemName"/>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="@originalText">
                        <xsl:attribute name="displayName">
                            <xsl:value-of select="@originalText"/>
                        </xsl:attribute>
                    </xsl:if>
                </AuditSourceTypeCode>
            </xsl:if>
            <xsl:apply-templates select="AuditSourceTypeCode"/>
        </AuditSourceIdentification>
    </xsl:template>
    <xsl:template match="AuditSourceTypeCode[not(@code)]">
        <AuditSourceTypeCode>
            <xsl:choose>
                <xsl:when test="contains(., '^')">
                    <xsl:attribute name="code">
                        <xsl:value-of select="substring-before(.,'^')"/>
                    </xsl:attribute>
                    <xsl:variable name="after">
                        <xsl:value-of select="substring-after(.,'^')"/>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="contains($after, '^')">
                            <xsl:attribute name="codeSystemName">
                                <xsl:value-of select="substring-before($after, '^')"/>
                            </xsl:attribute>
                            <xsl:attribute name="displayName">
                                <xsl:value-of select="substring-after($after, '^')"/>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="codeSystemName">
                                <xsl:value-of select="$after"/>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="code">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
        </AuditSourceTypeCode>
    </xsl:template>
    <xsl:template match="ParticipantObjectIdentification">
        <ParticipantObjectIdentification>
            <xsl:apply-templates select="@*|ParticipantObjectIDTypeCode|ParticipantObjectName|ParticipantObjectQuery|ParticipantObjectDetail"></xsl:apply-templates>
            <xsl:choose>
                <xsl:when test="count(ParticipantObjectDescription/node()) &gt; 0">
                    <xsl:apply-templates select="ParticipantObjectDescription"></xsl:apply-templates>
                </xsl:when>
                <xsl:when test="count(MPPS|Accession|SOPClass|ParticipantObjectContainsStudy) &gt; 0">
                    <ParticipantObjectDescription>
                        <xsl:apply-templates select="MPPS|Accession|SOPClass|ParticipantObjectContainsStudy"></xsl:apply-templates>
                    </ParticipantObjectDescription>
                </xsl:when>
            </xsl:choose>
        </ParticipantObjectIdentification>
    </xsl:template>
</xsl:stylesheet>
