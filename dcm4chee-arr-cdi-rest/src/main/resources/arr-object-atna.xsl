<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns:java="http://xml.apache.org/xalan/java" exclude-result-prefixes="java">
  <xsl:template match="AuditMessage">
    <xsl:apply-templates select="ParticipantObjectIdentification"/>
  </xsl:template>
  <!-- =========================================== -->
  <!-- ParticipantObjectIdentification             -->
  <!-- =========================================== -->
  <xsl:template match="ParticipantObjectIdentification">
    <xsl:variable name="title">
      <xsl:choose>
        <xsl:when test="@ParticipantObjectTypeCode=1">Person</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCode=2">System object</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCode=3">Organization</xsl:when>
        <xsl:otherwise>Object</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <span title="{$title}">
      <strong>
        <xsl:choose>
          <xsl:when test="@ParticipantObjectTypeCodeRole=1">Patient: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=2">Location: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=3">Report: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=4">Resource: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=5">Master file: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=6">User: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=7">List: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=8">Doctor: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=9">Subscriber: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=10">Guarantor: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=11"
            >Security User Entity: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=12"
            >Security User Group: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=13"
            >Security Resource: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=14"
            >Security Granualarity Definition: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=15">Provider: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=16"
            >Report Destination: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=17"
            >Report Library: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=18">Schedule: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=19">Customer: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=20">Job: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=21">Job Stream: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=22">Table: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=23"
            >Routing Criteria: </xsl:when>
          <xsl:when test="@ParticipantObjectTypeCodeRole=24">Query: </xsl:when>
          <xsl:otherwise><xsl:value-of select="$title"/>: </xsl:otherwise>
        </xsl:choose>
      </strong>
    </span>
    <xsl:if test="@ParticipantObjectDataLifeCycle">
      <span title="Data Life Cycle">
        <xsl:choose>
          <xsl:when test="@ParticipantObjectDataLifeCycle=1">(Creation)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=2">(Import)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=3">(Amendment)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=4">(Verification)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=5">(Translation)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=6">(Access/Use)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=7">(De-identification)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=8">(Aggregation)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=9">(Report)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=10">(Export)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=11">(Disclosure)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=12">(Receipt of disclosure)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=13">(Archiving)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=14">(Logical deletion)</xsl:when>
          <xsl:when test="@ParticipantObjectDataLifeCycle=15">(Permanent erasure)</xsl:when>
        </xsl:choose>
      </span>
    </xsl:if>   
    <span>
      <xsl:attribute name="title">
        <xsl:apply-templates select="ParticipantObjectIDTypeCode"/>
      </xsl:attribute>
      <xsl:value-of select="@ParticipantObjectID"/>
    </span>
    <xsl:if test="ParticipantObjectName">
      <xsl:text>, </xsl:text>
      <span title="Name">
        <xsl:value-of select="ParticipantObjectName"/>
      </span>
    </xsl:if>
    <xsl:apply-templates select="ParticipantObjectDetail"/>
    <br/>
  </xsl:template>
  <xsl:template match="ParticipantObjectIDTypeCode[position()=1]">
    <xsl:choose>
      <xsl:when test="@displayName">
        <xsl:value-of select="@displayName"/>
      </xsl:when>
      <xsl:when test="@code=1">Medical Record Number</xsl:when>
      <xsl:when test="@code=2">Patient Number</xsl:when>
      <xsl:when test="@code=3">Encounter Number</xsl:when>
      <xsl:when test="@code=4">Enrollee Number</xsl:when>
      <xsl:when test="@code=5">Social Security Number</xsl:when>
      <xsl:when test="@code=6">Account Number</xsl:when>
      <xsl:when test="@code=7">Guarantor Number</xsl:when>
      <xsl:when test="@code=8">Report Name</xsl:when>
      <xsl:when test="@code=9">Report Number</xsl:when>
      <xsl:when test="@code=10">Search Criteria</xsl:when>
      <xsl:when test="@code=11">User Identifier</xsl:when>
      <xsl:when test="@code=12">URI</xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="ParticipantObjectDetail[@type='AlertDescription']">
    <xsl:text>, </xsl:text>
    <span title="Alert Description">
      <xsl:value-of select="java:org.dcm4chee.arr.seam.ejb.Base64Decoder.decodeToUTF8(@value)"/>
    </span>
  </xsl:template>
  <xsl:template match="ParticipantObjectDetail[@type='Description']">
    <xsl:text>, </xsl:text>
    <span title="Description">
      <xsl:value-of select="java:org.dcm4chee.arr.seam.ejb.Base64Decoder.decodeToUTF8(@value)"/>
    </span>
  </xsl:template>
</xsl:stylesheet>
