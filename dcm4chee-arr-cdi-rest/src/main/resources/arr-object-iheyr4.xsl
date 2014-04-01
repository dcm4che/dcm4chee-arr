<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="IHEYr4">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="BeginStoringInstances">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="DICOMInstancesDeleted">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="DICOMInstancesUsed">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="DicomQuery">
    <span title="System Object">
      <strong>Report: </strong>
    </span>
    <span title="SOP Class UID">
      <xsl:value-of select="CUID"/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="Export">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="Import">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="InstanceActionDescription">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="InstancesSent">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="InstancesStored">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="OrderRecord">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="Patient">
    <span title="Person">
      <strong>Patient: </strong>
    </span>
    <span title="Patient Number">
      <xsl:value-of select="PatientID"/>
    </span>
    <xsl:apply-templates select="PatientName"/>
    <br/>
    <xsl:apply-templates select="SUID"/>
  </xsl:template>
  <xsl:template match="PatientName">
    <span title="Patient Name">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>
  <xsl:template match="PatientRecord">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="ProcedureRecord">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="SecurityAlert">
    <span title="System Object">
      <strong>Security Resource: </strong>
    </span>
    <span title="URI">
      <xsl:value-of select="../Host"/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="StudyDeleted">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="SUID">
    <span title="System Object">
      <strong>Report: </strong>
    </span>
    <span title="Study Instance UID">
      <xsl:value-of select="."/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="*"/>
</xsl:stylesheet>
