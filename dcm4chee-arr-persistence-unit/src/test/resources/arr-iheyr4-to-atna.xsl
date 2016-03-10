<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" encoding="UTF-8"/>
  <xsl:template match="/IHEYr4">
    <AuditMessage>
      <EventIdentification>
        <xsl:attribute name="EventDateTime">
          <xsl:value-of select="TimeStamp"/>
        </xsl:attribute>
        <xsl:apply-templates mode="EventIdentification" select="*"/>
      </EventIdentification>
      <xsl:apply-templates mode="ActiveParticipant" select="*"/>
      <xsl:apply-templates mode="AuditSourceIdentification" select="*"/>
      <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
    </AuditMessage>
  </xsl:template>
  <!-- =========================================== -->
  <!-- EventIdentification                         -->
  <!-- =========================================== -->
  <xsl:template mode="EventIdentification" match="ActorConfig">
    <xsl:attribute name="EventActionCode">E</xsl:attribute>
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <EventID code="110100" codeSystemName="DCM"
      displayName="Application Activity"/>
    <xsl:apply-templates mode="EventTypeCode" select="*"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="ActorStartStop">
    <xsl:attribute name="EventActionCode">E</xsl:attribute>
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <EventID code="110100" codeSystemName="DCM"
      displayName="Application Activity"/>
    <xsl:apply-templates mode="EventTypeCode" select="*"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="AuditLogUsed">
    <xsl:attribute name="EventActionCode">R</xsl:attribute>
    <xsl:attribute name="EventOutcomeIndicator">
      <xsl:value-of select="0"/>
    </xsl:attribute>
    <EventID code="110101" codeSystemName="DCM" displayName="Audit Log Used"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="BeginStoringInstances">
    <xsl:attribute name="EventActionCode">E</xsl:attribute>
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <EventID code="110102" codeSystemName="DCM"
      displayName="Begin Transferring DICOM Instances"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="DICOMInstancesDeleted">
    <xsl:attribute name="EventActionCode">D</xsl:attribute>
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <EventID code="110103" codeSystemName="DCM"
      displayName="DICOM Instances Accessed"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="DICOMInstancesUsed">
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <xsl:apply-templates mode="EventIdentification" select="ObjectAction"/>
    <EventID code="110103" codeSystemName="DCM"
      displayName="DICOM Instances Accessed"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="DicomQuery">
    <xsl:attribute name="EventActionCode">E</xsl:attribute>
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <EventID code="110112" codeSystemName="DCM" displayName="Query"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="Export">
    <xsl:attribute name="EventActionCode">R</xsl:attribute>
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <EventID code="110106" codeSystemName="DCM" displayName="Export"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="Import">
    <xsl:attribute name="EventActionCode">C</xsl:attribute>
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <EventID code="110107" codeSystemName="DCM" displayName="Import"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="InstancesSent">
    <xsl:attribute name="EventActionCode">E</xsl:attribute>
    <xsl:attribute name="EventOutcomeIndicator">
      <xsl:value-of select="0"/>
    </xsl:attribute>
    <EventID code="110104" codeSystemName="DCM"
      displayName="DICOM Instances Transferred"/>
    <xsl:apply-templates mode="EventTypeCode" select="*"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="InstancesStored">
    <xsl:attribute name="EventActionCode">C</xsl:attribute>
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <EventID code="110104" codeSystemName="DCM"
      displayName="DICOM Instances Transferred"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="NetworkEntry">
    <xsl:attribute name="EventActionCode">E</xsl:attribute>
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <EventID code="110108" codeSystemName="DCM" displayName="NetworkEntry"/>
    <xsl:apply-templates mode="EventTypeCode" select="*"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="OrderRecord">
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <xsl:apply-templates mode="EventIdentification" select="ObjectAction"/>
    <EventID code="110109" codeSystemName="DCM" displayName="Order Record"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="PatientRecord">
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <xsl:apply-templates mode="EventIdentification" select="ObjectAction"/>
    <EventID code="110110" codeSystemName="DCM" displayName="Patient Record"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="ProcedureRecord">
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <xsl:apply-templates mode="EventIdentification" select="ObjectAction"/>
    <EventID code="110111" codeSystemName="DCM" displayName="Procedure Record"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="SecurityAlert">
    <xsl:attribute name="EventActionCode">E</xsl:attribute>
    <xsl:attribute name="EventOutcomeIndicator">4</xsl:attribute>
    <EventID code="110113" codeSystemName="DCM" displayName="Security Alert"/>
    <xsl:apply-templates mode="EventTypeCode" select="*"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="StudyDeleted">
    <xsl:attribute name="EventActionCode">D</xsl:attribute>
    <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
    <EventID code="110115" codeSystemName="DCM"
      displayName="DICOM Study Deleted"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="UserAuthenticated">
    <xsl:attribute name="EventActionCode">E</xsl:attribute>
    <xsl:apply-templates mode="EventTypeCode" select="*"/>
    <EventID code="110114" codeSystemName="DCM"
      displayName="User Authentication"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="InstanceActionDescription">
    <xsl:apply-templates mode="EventIdentification" select="ObjectAction"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="ObjectAction">
    <xsl:attribute name="EventActionCode">
      <xsl:choose>
        <xsl:when test=". = 'Create'">C</xsl:when>
        <xsl:when test=". = 'Access'">R</xsl:when>
        <xsl:when test=". = 'Modify'">U</xsl:when>
        <xsl:when test=". = 'Delete'">D</xsl:when>
        <xsl:otherwise>E</xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="*"/>
  <!-- =========================================== -->
  <!-- EventIdentification / EventTypeCode         -->
  <!-- =========================================== -->
  <xsl:template mode="EventTypeCode" match="Action">
    <xsl:choose>
      <xsl:when test=". = 'Login'">
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <EventTypeCode code="110122" codeSystemName="DCM" displayName="Login"/>
      </xsl:when>
      <xsl:when test=". = 'Logout'">
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <EventTypeCode code="110123" codeSystemName="DCM" displayName="Logout"/>
      </xsl:when>
      <xsl:when test=". = 'Failure'">
        <xsl:attribute name="EventOutcomeIndicator">4</xsl:attribute>
        <EventTypeCode code="110122" codeSystemName="DCM" displayName="Login"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="AlertType">
    <xsl:choose>
      <xsl:when test=". = 'NodeAuthenticationFailure'">
        <EventTypeCode code="110126" codeSystemName="DCM"
          displayName="Node Authentication"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="ApplicationAction">
    <xsl:choose>
      <xsl:when test=". = 'Start'">
        <EventTypeCode code="110120" codeSystemName="DCM"
          displayName="Application Start"/>
      </xsl:when>
      <xsl:when test=". = 'Stop'">
        <EventTypeCode code="110121" codeSystemName="DCM"
          displayName="Application Stop"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="ConfigType">
    <xsl:choose>
      <xsl:when test=". = 'Networking'">
        <EventTypeCode code="110128" codeSystemName="DCM"
          displayName="Network Configuration"/>
      </xsl:when>
      <xsl:when test=". = 'Security'">
        <EventTypeCode code="110129" codeSystemName="DCM"
          displayName="Security Configuration"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="MachineAction">
    <xsl:choose>
      <xsl:when test=". = 'Attach'">
        <EventTypeCode code="110124" codeSystemName="DCM" displayName="Attach"/>
      </xsl:when>
      <xsl:when test=". = 'Detach'">
        <EventTypeCode code="110125" codeSystemName="DCM" displayName="Detach"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="*"/>
  <!-- =========================================== -->
  <!-- Active Participant                          -->
  <!-- =========================================== -->
  <xsl:template mode="ActiveParticipant" match="ActorConfig">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="ActorName">
    <ActiveParticipant>
      <xsl:attribute name="UserID">
        <xsl:value-of select="."/>
      </xsl:attribute>
      <xsl:attribute name="UserIsRequestor">false</xsl:attribute>
    </ActiveParticipant>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="ActorStartStop">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="AET">
    <xsl:attribute name="AlternativeUserID">AETITLES=<xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="AuditLogUsed">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="BeginStoringInstances">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Destination">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="DICOMInstancesDeleted">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="DICOMInstancesUsed">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="DicomQuery">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Export">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Hname">
    <xsl:attribute name="UserName">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Import">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="InstanceActionDescription">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="InstancesSent">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="InstancesStored">
    <ActiveParticipant>
      <xsl:attribute name="UserIsRequestor">true</xsl:attribute>
      <xsl:apply-templates mode="ActiveParticipant" select="RemoteNode"/>
    </ActiveParticipant>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="IP">
    <xsl:attribute name="UserID">
      <xsl:value-of select="."/>
    </xsl:attribute>
    <xsl:attribute name="NetworkAccessPointID">
      <xsl:value-of select="."/>
    </xsl:attribute>
    <xsl:attribute name="NetworkAccessPointTypeCode">2</xsl:attribute>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="LocalPrinter">
    <ActiveParticipant>
      <xsl:attribute name="UserID">
        <xsl:value-of select="."/>
      </xsl:attribute>
      <xsl:attribute name="UserIsRequestor">false</xsl:attribute>
      <RoleIDCode code="110154" codeSystemName="DCM"
        displayName="Destination Media"/>
    </ActiveParticipant>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="LocalUser">
    <xsl:attribute name="UserID">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="LocalUsername">
    <ActiveParticipant>
      <xsl:attribute name="UserID">
        <xsl:value-of select="."/>
      </xsl:attribute>
      <xsl:attribute name="UserIsRequestor">true</xsl:attribute>
    </ActiveParticipant>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="MediaID">
    <ActiveParticipant>
      <xsl:attribute name="UserID">
        <xsl:value-of select="."/>
      </xsl:attribute>
      <xsl:attribute name="UserIsRequestor">false</xsl:attribute>
    </ActiveParticipant>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="NetworkEntry">
    <ActiveParticipant>
      <xsl:attribute name="UserID">
        <xsl:value-of select="../Host"/>
      </xsl:attribute>
      <xsl:attribute name="UserIsRequestor">false</xsl:attribute>
    </ActiveParticipant>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Node">
    <ActiveParticipant>
      <xsl:attribute name="UserIsRequestor">false</xsl:attribute>
      <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    </ActiveParticipant>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="OrderRecord">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="PatientRecord">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="ProcedureRecord">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="RemoteNode">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Requestor">
    <ActiveParticipant>
      <xsl:attribute name="UserIsRequestor">true</xsl:attribute>
      <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    </ActiveParticipant>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="RNode">
    <ActiveParticipant>
      <xsl:attribute name="UserIsRequestor">false</xsl:attribute>
      <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    </ActiveParticipant>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="SecurityAlert">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    <ActiveParticipant>
      <xsl:attribute name="UserID">
        <xsl:value-of select="../Host"/>
      </xsl:attribute>
      <xsl:attribute name="UserIsRequestor">false</xsl:attribute>
    </ActiveParticipant>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="StudyDeleted">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="User">
    <ActiveParticipant>
      <xsl:attribute name="UserIsRequestor">true</xsl:attribute>
      <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    </ActiveParticipant>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="UserAuthenticated">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="*"/>
  <!-- =========================================== -->
  <!-- Active Participant / RoleIDCode             -->
  <!-- =========================================== -->
  <!-- =========================================== -->
  <!-- AuditSourceIdentification                   -->
  <!-- =========================================== -->
  <xsl:template mode="AuditSourceIdentification" match="Host">
    <AuditSourceIdentification>
      <xsl:attribute name="AuditSourceID">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </AuditSourceIdentification>
  </xsl:template>
  <xsl:template mode="AuditSourceIdentification" match="*"/>
  <!-- =========================================== -->
  <!-- ParticipantObjectIdentification             -->
  <!-- =========================================== -->
  <xsl:template mode="ParticipantObjectIdentification"
    match="BeginStoringInstances">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification"
    match="DICOMInstancesDeleted">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification"
    match="DICOMInstancesUsed">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="DicomQuery">
    <ParticipantObjectIdentification>
      <xsl:attribute name="ParticipantObjectTypeCode">2</xsl:attribute>
      <xsl:attribute name="ParticipantObjectTypeCodeRole">3</xsl:attribute>
      <xsl:attribute name="ParticipantObjectID">
        <xsl:value-of select="CUID"/>
      </xsl:attribute>
      <ParticipantObjectIDTypeCode code="110181" codeSystemName="DCM"
        displayName="SOP Class UID"/>
    </ParticipantObjectIdentification>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="Export">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="Import">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification"
    match="InstanceActionDescription">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="InstancesSent">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="InstancesStored">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="OrderRecord">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="Patient">
    <ParticipantObjectIdentification>
      <xsl:attribute name="ParticipantObjectTypeCode">1</xsl:attribute>
      <xsl:attribute name="ParticipantObjectTypeCodeRole">1</xsl:attribute>
      <xsl:apply-templates mode="ParticipantObjectIdentification"
        select="PatientID"/>
      <ParticipantObjectIDTypeCode code="2"/>
      <xsl:apply-templates mode="ParticipantObjectIdentification"
        select="PatientName"/>
    </ParticipantObjectIdentification>
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="SUID"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="PatientID">
    <xsl:attribute name="ParticipantObjectID">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="PatientName">
    <ParticipantObjectName>
      <xsl:value-of select="."/>
    </ParticipantObjectName>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="PatientRecord">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="ProcedureRecord">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="SecurityAlert">
    <ParticipantObjectIdentification>
      <xsl:attribute name="ParticipantObjectID">
        <xsl:value-of select="../Host"/>
      </xsl:attribute>
      <xsl:attribute name="ParticipantObjectTypeCode">2</xsl:attribute>
      <xsl:attribute name="ParticipantObjectTypeCodeRole">13</xsl:attribute>
      <ParticipantObjectIDTypeCode code="12"/>
    </ParticipantObjectIdentification>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="StudyDeleted">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="SUID">
    <ParticipantObjectIdentification>
      <xsl:attribute name="ParticipantObjectID">
        <xsl:value-of select="."/>
      </xsl:attribute>
      <xsl:attribute name="ParticipantObjectTypeCode">
        <xsl:value-of select="2"/>
      </xsl:attribute>
      <xsl:attribute name="ParticipantObjectTypeCodeRole">
        <xsl:value-of select="3"/>
      </xsl:attribute>
      <ParticipantObjectIDTypeCode>
        <xsl:attribute name="code">
          <xsl:value-of select="'9'"/>
        </xsl:attribute>
      </ParticipantObjectIDTypeCode>
    </ParticipantObjectIdentification>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="*"/>
  <!-- =========================================== -->
  <!-- Discard everything else                     -->
  <!-- =========================================== -->
  <xsl:template match="*"/>
</xsl:stylesheet>
