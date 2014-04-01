<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="IHEYr4">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="ActorConfig">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="ActorName">
    <strong>User: </strong>
    <span title="User ID">
      <xsl:value-of select="."/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="ActorStartStop">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="AET">
    <xsl:text>, </xsl:text>
    <span title="AE Title(s)">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>
  <xsl:template match="AuditLogUsed">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="BeginStoringInstances">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="Destination">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="DICOMInstancesDeleted">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="DICOMInstancesUsed">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="DicomQuery">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="Export">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="Hname">
    <xsl:text>, </xsl:text>
    <span title="User Name">
      <xsl:value-of select="."/>
    </span>
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
    <strong>User(R): </strong>
    <xsl:apply-templates select="RemoteNode"/>
    <br/>
  </xsl:template>
  <xsl:template match="IP">
    <span title="IP Address">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>
  <xsl:template match="LocalPrinter">
    <strong>Destination Media: </strong>
    <span title="User ID">
      <xsl:value-of select="."/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="LocalUser">
    <span title="User ID">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>
  <xsl:template match="LocalUsername">
    <strong>User(R): </strong>
    <span title="User ID">
      <xsl:value-of select="."/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="MediaID">
    <strong>User: </strong>
    <span title="User ID">
      <xsl:value-of select="."/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="NetworkEntry">
    <strong>User: </strong>
    <span title="User ID">
      <xsl:value-of select="../Host"/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="Node">
    <strong>User: </strong>
    <xsl:apply-templates select="*"/>
    <br/>
  </xsl:template>
  <xsl:template match="OrderRecord">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="PatientRecord">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="ProcedureRecord">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="RemoteNode">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="Requestor">
    <strong>User(R): </strong>
    <xsl:apply-templates select="*"/>
    <br/>
  </xsl:template>
  <xsl:template match="RNode">
    <strong>User: </strong>
    <xsl:apply-templates select="*"/>
    <br/>
  </xsl:template>
  <xsl:template match="SecurityAlert">
    <xsl:apply-templates select="*"/>
    <strong>User: </strong>
    <span title="User ID">
      <xsl:value-of select="../Host"/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="StudyDeleted">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="User">
    <strong>User(R): </strong>
    <xsl:apply-templates select="*"/>
    <br/>
  </xsl:template>
  <xsl:template match="UserAuthenticated">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="*"/>
</xsl:stylesheet>
