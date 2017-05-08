<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="IHEYr4">
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
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">Application Activity</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110100</xsl:attribute>
        </EventID>
        <xsl:apply-templates mode="EventTypeCode" select="*"/>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="ActorStartStop">
        <xsl:attribute name="EventActionCode">E</xsl:attribute>
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">Application Activity</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110100</xsl:attribute>
        </EventID>
        <xsl:apply-templates mode="EventTypeCode" select="*"/>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="AuditLogUsed">
        <xsl:attribute name="EventActionCode">R</xsl:attribute>
        <xsl:attribute name="EventOutcomeIndicator">
            <xsl:value-of select="0"/>
        </xsl:attribute>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">Audit Log Used</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110101</xsl:attribute>
        </EventID>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="BeginStoringInstances">
        <xsl:attribute name="EventActionCode">E</xsl:attribute>
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">Begin Transferring DICOM Instances</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110102</xsl:attribute>
        </EventID>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="DICOMInstancesDeleted">
        <xsl:attribute name="EventActionCode">D</xsl:attribute>
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <EventID  codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">DICOM Instances Accessed</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110103</xsl:attribute>
        </EventID>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="DICOMInstancesUsed">
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <xsl:apply-templates mode="EventIdentification" select="ObjectAction"/>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">DICOM Instances Accessed</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110103</xsl:attribute>
        </EventID>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="DicomQuery">
        <xsl:attribute name="EventActionCode">E</xsl:attribute>
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">Query</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110112</xsl:attribute>
        </EventID>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="Export">
        <xsl:attribute name="EventActionCode">R</xsl:attribute>
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">Export</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110106</xsl:attribute>
        </EventID>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="Import">
        <xsl:attribute name="EventActionCode">C</xsl:attribute>
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">Import</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110107</xsl:attribute>
        </EventID>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="InstancesSent">
        <xsl:attribute name="EventActionCode">E</xsl:attribute>
        <xsl:attribute name="EventOutcomeIndicator">
            <xsl:value-of select="0"/>
        </xsl:attribute>
        <EventID  codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">DICOM Instances Transferred</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110104</xsl:attribute>
        </EventID>
        <xsl:apply-templates mode="EventTypeCode" select="*"/>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="InstancesStored">
        <xsl:attribute name="EventActionCode">C</xsl:attribute>
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">DICOM Instances Transferred</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110104</xsl:attribute>
        </EventID>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="NetworkEntry">
        <xsl:attribute name="EventActionCode">E</xsl:attribute>
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <EventID codeSystemName="DCM" >
            <xsl:attribute name="{$displayname_attr_name}">NetworkEntry</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110108</xsl:attribute>
        </EventID>
        <xsl:apply-templates mode="EventTypeCode" select="*"/>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="OrderRecord">
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <xsl:apply-templates mode="EventIdentification" select="ObjectAction"/>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">Order Record</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110109</xsl:attribute>
        </EventID>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="PatientRecord">
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <xsl:apply-templates mode="EventIdentification" select="ObjectAction"/>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">Patient Record</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110110</xsl:attribute>
        </EventID>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="ProcedureRecord">
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <xsl:apply-templates mode="EventIdentification" select="ObjectAction"/>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">Procedur Record</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110111</xsl:attribute>
        </EventID>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="SecurityAlert">
        <xsl:attribute name="EventActionCode">E</xsl:attribute>
        <xsl:attribute name="EventOutcomeIndicator">4</xsl:attribute>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">Security Alert</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110113</xsl:attribute>
        </EventID>
        <xsl:apply-templates mode="EventTypeCode" select="*"/>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="StudyDeleted">
        <xsl:attribute name="EventActionCode">D</xsl:attribute>
        <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">DICOM Study Deleted</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110115</xsl:attribute>
        </EventID>
    </xsl:template>
    <xsl:template mode="EventIdentification" match="UserAuthenticated">
        <xsl:attribute name="EventActionCode">E</xsl:attribute>
        <xsl:apply-templates mode="EventTypeCode" select="*"/>
        <EventID codeSystemName="DCM">
            <xsl:attribute name="{$displayname_attr_name}">User Authentication</xsl:attribute>
            <xsl:attribute name="{$code_attr_name}">110114</xsl:attribute>
        </EventID>
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
                <EventTypeCode codeSystemName="DCM">
                    <xsl:attribute name="{$displayname_attr_name}">Login</xsl:attribute>
                    <xsl:attribute name="{$code_attr_name}">110122</xsl:attribute>
                </EventTypeCode>
            </xsl:when>
            <xsl:when test=". = 'Logout'">
                <xsl:attribute name="EventOutcomeIndicator">0</xsl:attribute>
                <EventTypeCode codeSystemName="DCM">
                    <xsl:attribute name="{$displayname_attr_name}">Logout</xsl:attribute>
                    <xsl:attribute name="{$code_attr_name}">110123</xsl:attribute>
                </EventTypeCode>
            </xsl:when>
            <xsl:when test=". = 'Failure'">
                <xsl:attribute name="EventOutcomeIndicator">4</xsl:attribute>
                <EventTypeCode codeSystemName="DCM">
                    <xsl:attribute name="{$displayname_attr_name}">Login</xsl:attribute>
                    <xsl:attribute name="{$code_attr_name}">110122</xsl:attribute>
                </EventTypeCode>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template mode="EventTypeCode" match="AlertType">
        <xsl:choose>
            <xsl:when test=". = 'NodeAuthenticationFailure'">
                <EventTypeCode codeSystemName="DCM">
                    <xsl:attribute name="{$displayname_attr_name}">Node Authentication</xsl:attribute>
                    <xsl:attribute name="{$code_attr_name}">110126</xsl:attribute>
                </EventTypeCode>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template mode="EventTypeCode" match="ApplicationAction">
        <xsl:choose>
            <xsl:when test=". = 'Start'">
                <EventTypeCode codeSystemName="DCM" >
                    <xsl:attribute name="{$displayname_attr_name}">Application Start</xsl:attribute>
                    <xsl:attribute name="{$code_attr_name}">110120</xsl:attribute>
                </EventTypeCode>
            </xsl:when>
            <xsl:when test=". = 'Stop'">
                <EventTypeCode codeSystemName="DCM">
                    <xsl:attribute name="{$displayname_attr_name}">Application Stop</xsl:attribute>
                    <xsl:attribute name="{$code_attr_name}">110121</xsl:attribute>
                </EventTypeCode>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template mode="EventTypeCode" match="ConfigType">
        <xsl:choose>
            <xsl:when test=". = 'Networking'">
                <EventTypeCode codeSystemName="DCM">
                    <xsl:attribute name="{$displayname_attr_name}">Network Configuration</xsl:attribute>
                    <xsl:attribute name="{$code_attr_name}">110128</xsl:attribute>
                </EventTypeCode>
            </xsl:when>
            <xsl:when test=". = 'Security'">
                <EventTypeCode codeSystemName="DCM">
                    <xsl:attribute name="{$displayname_attr_name}">Security Configuration</xsl:attribute>
                    <xsl:attribute name="{$code_attr_name}">110129</xsl:attribute>
                </EventTypeCode>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template mode="EventTypeCode" match="MachineAction">
        <xsl:choose>
            <xsl:when test=". = 'Attach'">
                <EventTypeCode codeSystemName="DCM">
                    <xsl:attribute name="{$displayname_attr_name}">Attach</xsl:attribute>
                    <xsl:attribute name="{$code_attr_name}">110124</xsl:attribute>
                </EventTypeCode>
            </xsl:when>
            <xsl:when test=". = 'Detach'">
                <EventTypeCode codeSystemName="DCM">
                    <xsl:attribute name="{$displayname_attr_name}">Detach</xsl:attribute>
                    <xsl:attribute name="{$code_attr_name}">110125</xsl:attribute>
                </EventTypeCode>
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
            <RoleIDCode codeSystemName="DCM">
                <xsl:attribute name="{$displayname_attr_name}">Destination Media</xsl:attribute>
                <xsl:attribute name="{$code_attr_name}">110154</xsl:attribute>
            </RoleIDCode>
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
            <ParticipantObjectIDTypeCode codeSystemName="DCM">
                <xsl:attribute name="{$displayname_attr_name}">SOP Class UID</xsl:attribute>
                <xsl:attribute name="{$code_attr_name}">110181</xsl:attribute>
            </ParticipantObjectIDTypeCode>
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
            <ParticipantObjectIDTypeCode codeSystemName="">
                <xsl:attribute name="{$displayname_attr_name}">2</xsl:attribute>
                <xsl:attribute name="{$code_attr_name}"></xsl:attribute>
            </ParticipantObjectIDTypeCode>
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
            <ParticipantObjectIDTypeCode codeSystemName="">
                <xsl:attribute name="{$displayname_attr_name}">12</xsl:attribute>
                <xsl:attribute name="{$code_attr_name}"></xsl:attribute>
            </ParticipantObjectIDTypeCode>
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
