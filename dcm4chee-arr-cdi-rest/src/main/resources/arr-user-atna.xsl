<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns:java="http://xml.apache.org/xalan/java" exclude-result-prefixes="java">
  <xsl:template match="AuditMessage">
    <xsl:apply-templates select="ActiveParticipant"/>
  </xsl:template>
  <!-- =========================================== -->
  <!-- Active Participant                          -->
  <!-- =========================================== -->
  <xsl:template match="ActiveParticipant">
    <strong>
      <xsl:choose>
        <xsl:when test="RoleIDCode">
           <xsl:apply-templates select="RoleIDCode"/>
        </xsl:when>
        <xsl:otherwise>User</xsl:otherwise>
      </xsl:choose>
      <xsl:if test="not(@UserIsRequestor='false')">
        <xsl:text>(R)</xsl:text>
      </xsl:if>
      <xsl:text>: </xsl:text>
    </strong>
    <span title="User ID">
      <xsl:value-of select="@UserID"/>
    </span>
    <xsl:if test="@AlternativeUserID">
      <xsl:text>, </xsl:text>
      <xsl:choose>
        <xsl:when test="starts-with(@AlternativeUserID,'AETITLES=')">
          <span title="AE Title(s)">
            <xsl:value-of select="substring(@AlternativeUserID,10)"/>
          </span>
        </xsl:when>
        <xsl:otherwise>
          <span title="Alt. User ID">
            <xsl:value-of select="@AlternativeUserID"/>
          </span>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <xsl:if test="@UserName">
      <xsl:text>, </xsl:text>
      <span title="User Name">
        <xsl:value-of select="@UserName"/>
      </span>
    </xsl:if>
    <xsl:if test="@NetworkAccessPointID">
      <xsl:text>, </xsl:text>
      <span>
        <xsl:attribute name="title">
          <xsl:choose>
            <xsl:when test="@NetworkAccessPointTypeCode=1">Machine Name</xsl:when>
            <xsl:when test="@NetworkAccessPointTypeCode=2">IP Address</xsl:when>
            <xsl:when test="@NetworkAccessPointTypeCode=3">Telephone Number</xsl:when>
          </xsl:choose>
        </xsl:attribute>
        <xsl:value-of select="@NetworkAccessPointID"/>
      </span>
    </xsl:if>
    <br/>
  </xsl:template>
  <xsl:template match="RoleIDCode">
    <xsl:if test="position()!=1">, </xsl:if>
    <xsl:value-of select="@displayName"/>
  </xsl:template>
</xsl:stylesheet>
