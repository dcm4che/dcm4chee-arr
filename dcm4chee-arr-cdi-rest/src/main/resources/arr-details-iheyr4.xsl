<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="IHEYr4">
    <table width="100%" cellpadding="0" cellspacing="0" border="0">
      <tr bgcolor="#eeeeee">
        <td>Host</td>
        <td width="10"/>
        <td>Time</td>
        <td width="10"/>
        <td>Parameters</td>
      </tr>
      <tr>
        <td height="15"/>
      </tr>
      <tr>
        <tr>
          <td/>
        </tr>
        <td valign="top">
          <xsl:apply-templates select="Host"/>
        </td>
        <td width="10"/>
        <td valign="top">
          <xsl:apply-templates select="TimeStamp"/>
        </td>
        <td width="10"/>
        <td>
          <div class="level">
            <xsl:apply-templates
              select="Import|InstancesStored|ProcedureRecord|ActorStartStop|ActorConfig|Export|DICOMInstancesDeleted|PatientRecord|OrderRecord|BeginStoringInstances|InstancesSent|DICOMInstancesUsed|StudyDeleted|DicomQuery|SecurityAlert|UserAuthenticated|AuditLogUsed|NetworkEntry"
            />
          </div>
        </td>
      </tr>
    </table>
  </xsl:template>
  <!-- events -->
  <!--(Import | InstancesStored | ProcedureRecord | ActorStartStop | ActorConfig | Export | DICOMInstancesDeleted | PatientRecord |
	OrderRecord | BeginStoringInstances | InstancesSent | DICOMInstancesUsed | StudyDeleted | DicomQuery | SecurityAlert |
	UserAuthenticated | AuditLogUsed | NetworkEntry)-->
  <!-- Import -->
  <xsl:template match="Import">
    <span class="event">Import</span>
    <br/>
    <br/>
    <xsl:apply-templates select="MediaDescriptionType"/>
    <xsl:apply-templates select="User"/>
  </xsl:template>
  <xsl:template match="MediaDescriptionType">
    <b>MediaDescriptionType:</b>
    <br/>
    <div class="level">
      <xsl:call-template name="MediaDescriptionType"/>
    </div>
  </xsl:template>
  <xsl:template match="User">
    <b>User:</b>
    <br/>
    <div class="level">
      <xsl:call-template name="UserType"/>
    </div>
  </xsl:template>
  <!-- InstancesStored -->
  <xsl:template match="InstancesStored">
    <span class="event">InstancesStored</span>
    <br/>
    <br/>
    <xsl:apply-templates select="RemoteNode"/>
    <xsl:apply-templates select="InstanceActionDescription"/>
  </xsl:template>
  <xsl:template match="RemoteNode">
    <b>RemoteNode:</b>
    <br/>
    <div class="level">
      <xsl:call-template name="RemoteNodeType"/>
    </div>
  </xsl:template>
  <xsl:template match="InstanceActionDescription">
    <b>InstanceActionDescription:</b>
    <br/>
    <div class="level">
      <xsl:call-template name="InstancesActionType"/>
    </div>
  </xsl:template>
  <!-- ProcedureRecord -->
  <xsl:template match="ProcedureRecord">
    <span class="event">ProcedureRecord</span>
    <br/>
    <br/>
    <xsl:apply-templates select="ObjectAction"/>
    <xsl:if test="PlacerOrderNumber">
      <b>PlacerOrderNum: </b>
      <span class="num">
        <xsl:value-of select="PlacerOrderNumber"/>
      </span>
      <br/>
    </xsl:if>
    <xsl:if test="FillerOrderNumber">
      <b>FillerOrderNum: </b>
      <span class="num">
        <xsl:value-of select="FillerOrderNumber"/>
      </span>
      <br/>
    </xsl:if>
    <xsl:apply-templates select="SUID"/>
    <xsl:apply-templates select="AccessionNumber"/>
    <xsl:apply-templates select="Patient"/>
    <xsl:apply-templates select="Description"/>
    <xsl:apply-templates select="User"/>
  </xsl:template>
  <!-- ActorStartStop -->
  <xsl:template match="ActorStartStop">
    <span class="event">ActorStartStop</span>
    <br/>
    <br/>
    <xsl:if test="ActorName">
      <b>ActorName: </b>
      <xsl:value-of select="ActorName"/>
      <br/>
    </xsl:if>
    <xsl:apply-templates select="ApplicationAction"/>
    <xsl:apply-templates select="User"/>
  </xsl:template>
  <!-- ActorConfig -->
  <xsl:template match="ActorConfig">
    <span class="event">ActorConfig</span>
    <br/>
    <br/>
    <xsl:apply-templates select="Description"/>
    <xsl:apply-templates select="User"/>
    <xsl:if test="ConfigType">
      <b>ConfigType:</b>
      <br/>
      <xsl:value-of select="ConfigType"/>
      <br/>
    </xsl:if>
  </xsl:template>
  <!-- Export -->
  <xsl:template match="Export">
    <span class="event">Export</span>
    <br/>
    <br/>
    <xsl:call-template name="MediaDescriptionType"/>
    <xsl:apply-templates select="User"/>
  </xsl:template>
  <!-- DICOMInstancesDeleted -->
  <xsl:template match="DICOMInstancesDeleted">
    <span class="event-important">DICOMInstancesUsed</span>
    <br/>
    <br/>
    <xsl:call-template name="InstancesActionType"/>
  </xsl:template>
  <!-- PatientRecord -->
  <xsl:template match="PatientRecord">
    <span class="event">PatientRecord</span>
    <br/>
    <br/>
    <xsl:apply-templates select="ObjectAction"/>
    <xsl:apply-templates select="Patient"/>
    <xsl:apply-templates select="Description"/>
    <xsl:apply-templates select="User"/>
  </xsl:template>
  <xsl:template match="Patient">
    <b>Patient:</b>
    <br/>
    <div class="level">
      <xsl:call-template name="PatientType"/>
    </div>
  </xsl:template>
  <!-- OrderRecord -->
  <xsl:template match="OrderRecord">
    <span class="event">OrderRecord</span>
    <br/>
    <br/>
    <xsl:apply-templates select="ObjectAction"/>
    <xsl:apply-templates select="Patient"/>
    <xsl:apply-templates select="User"/>
  </xsl:template>
  <!-- BeginStoringInstances -->
  <xsl:template match="BeginStoringInstances">
    <span class="event">BeginStoringInstances</span>
    <br/>
    <br/>
    <xsl:call-template name="BeginStoringInstancesType"/>
    <xsl:apply-templates select="Rnode"/>
    <xsl:apply-templates select="InstanceActionDescription"/>
  </xsl:template>
  <xsl:template match="Rnode">
    <b>Rnode:</b>
    <br/>
    <div class="level">
      <xsl:call-template name="RemoteNodeType"/>
    </div>
  </xsl:template>
  <xsl:template match="RNode">
    <b>RNode:</b>
    <br/>
    <div class="level">
      <xsl:call-template name="RemoteNodeType"/>
    </div>
  </xsl:template>
  <xsl:template match="InstanceActionDescription">
    <b>InstanceActionDescription:</b>
    <br/>
    <div class="level">
      <xsl:call-template name="InstancesActionType"/>
    </div>
  </xsl:template>
  <!-- InstancesSent -->
  <xsl:template match="InstancesSent">
    <span class="event">InstancesSent</span>
    <br/>
    <br/>
    <xsl:call-template name="BeginStoringInstancesType"/>
    <xsl:apply-templates select="RNode"/>
    <xsl:apply-templates select="InstanceActionDescription"/>
  </xsl:template>
  <!-- DICOMInstancesUsed -->
  <xsl:template match="DICOMInstancesUsed">
    <span class="event">DICOMInstancesUsed</span>
    <br/>
    <br/>
    <xsl:call-template name="InstancesActionType"/>
  </xsl:template>
  <!-- StudyDeleted -->
  <xsl:template match="StudyDeleted">
    <span class="event-important">StudyDeleted</span>
    <br/>
    <br/>
    <xsl:call-template name="InstancesActionType"/>
    <xsl:apply-templates select="Description"/>
  </xsl:template>
  <!-- DICOMQuery -->
  <xsl:template match="DicomQuery">
    <span class="event">DicomQuery</span>
    <br/>
    <br/>
    <!--xsl:apply-templates select="Keys"/-->
    <xsl:apply-templates select="Requestor"/>
    <xsl:apply-templates select="CUID"/>
  </xsl:template>
  <xsl:template match="Requestor">
    <b>Requestor:</b>
    <br/>
    <div class="level">
      <xsl:call-template name="RemoteNodeType"/>
    </div>
  </xsl:template>
  <!-- SecurityAlert -->
  <xsl:template match="SecurityAlert">
    <span class="event-alert">SecurityAlert</span>
    <br/>
    <br/>
    <xsl:apply-templates select="AlertType"/>
    <xsl:apply-templates select="User"/>
    <xsl:apply-templates select="Description"/>
  </xsl:template>
  <!-- UserAuthenticated -->
  <xsl:template match="UserAuthenticated">
    <span class="event">UserAuthenticated</span>
    <br/>
    <br/>
    <xsl:if test="LocalUsername">
      <b>LocalUsername: </b>
      <xsl:value-of select="LocalUsername"/>
      <br/>
    </xsl:if>
    <xsl:apply-templates select="Action"/>
  </xsl:template>
  <!-- AuditLogUsed -->
  <xsl:template match="AuditLogUsed">
    <span class="event">AuditLogUsed</span>
    <br/>
    <br/>
    <xsl:apply-templates select="Usage"/>
    <xsl:apply-templates select="User"/>
  </xsl:template>
  <!-- NetworkEntry -->
  <xsl:template match="NetworkEntry">
    <span class="event">NetworkEntry</span>
    <br/>
    <br/>
    <xsl:apply-templates select="MachineAction"/>
  </xsl:template>
  <!-- Matching templates are used for special handling of basic elements -->
  <xsl:template match="Host">
    <span class="host">
      <xsl:value-of select="."/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="TimeStamp">
    <!-- make timestamp into a pretty link -->
    <xsl:variable name="timezone-found">
      <xsl:choose>
        <xsl:when
          test="contains(.,'z') or contains(.,'Z') or contains(substring-after(.,'T'),'-') or contains(.,'+')"
          >true</xsl:when>
        <xsl:otherwise>false</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test=". != ''">
        <!--<a href="arr-list.do?to={$time}&amp;from={$time}">-->
        <span class="time">
          <xsl:value-of select="."/>
        </span>
        <xsl:if test="$timezone-found = 'false'">
          <br/>
          <i>(no TZ, local assumed)</i>
        </xsl:if>
        <!--</a>-->
      </xsl:when>
      <xsl:otherwise>[empty]</xsl:otherwise>
    </xsl:choose>
    <br/>
  </xsl:template>
  <xsl:template match="IP">
    <b>IP: </b>
    <span class="ip">
      <xsl:value-of select="."/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="AET">
    <b>AET: </b>
    <xsl:value-of select="."/>
    <br/>
  </xsl:template>
  <xsl:template match="Hname">
    <b>Hname: </b>
    <xsl:value-of select="."/>
    <br/>
  </xsl:template>
  <xsl:template match="MachineAction">
    <b>MachineAction: </b>
    <xsl:value-of select="."/>
    <br/>
  </xsl:template>
  <xsl:template match="ObjectAction">
    <b>ObjectAction: </b>
    <xsl:choose>
      <xsl:when test=". = 'Create'">
        <span style="font-style: italic">
          <xsl:value-of select="."/>
        </span>
      </xsl:when>
      <xsl:when test=". = 'Access'">
        <span style="font-style: italic">
          <xsl:value-of select="."/>
        </span>
      </xsl:when>
      <xsl:when test=". = 'Modify'">
        <span style="font-style: italic">
          <xsl:value-of select="."/>
        </span>
      </xsl:when>
      <xsl:when test=". = 'Delete'">
        <span style="font-style: italic">
          <xsl:value-of select="."/>
        </span>
      </xsl:when>
      <xsl:otherwise>
        <span style=""> [Invalid or unspecified]</span>
      </xsl:otherwise>
    </xsl:choose>
    <br/>
  </xsl:template>
  <xsl:template match="CUID">
    <b>CUID: </b>
    <span class="id">
      <xsl:value-of select="."/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="SUID">
    <b>SUID: </b>
    <span class="id">
      <xsl:value-of select="."/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="MPPSUID">
    <b>MPPSUID: </b>
    <span class="id">
      <xsl:value-of select="."/>
    </span>
    <br/>
  </xsl:template>
  <!--xsl:template match="Keys">
    <form name="frmviewdcm" action="arr-viewdcm.do" method="post">
      <input type="hidden" name="base64" value="{.}"/>
      <b>Keys: </b>
      <input type="submit" name="submit" value="view..."/>
    </form>
  </xsl:template-->
  <xsl:template match="AccessionNumber">
    <b>AccessionNumber: </b>
    <span class="num">
      <xsl:value-of select="."/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="ApplicationAction">
    <b>ApplicationAction:</b>
    <xsl:choose>
      <xsl:when test=". = 'Start'">
        <span style="font-style: italic">
          <xsl:value-of select="."/>
        </span>
      </xsl:when>
      <xsl:when test=". = 'Stop'">
        <span style="font-style: italic">
          <xsl:value-of select="."/>
        </span>
      </xsl:when>
      <xsl:otherwise>
        <span style=""> [Invalid or unspecified]</span>
      </xsl:otherwise>
    </xsl:choose>
    <br/>
  </xsl:template>
  <xsl:template match="PatientID">
    <b>PatientID: </b>
    <span class="id">
      <xsl:value-of select="."/>
    </span>
    <br/>
  </xsl:template>
  <xsl:template match="PatientName">
    <b>PatientName: </b>
    <xsl:value-of select="."/>
    <br/>
  </xsl:template>
  <xsl:template match="AlertType">
    <b>AlertType: </b>
    <xsl:value-of select="."/>
    <br/>
  </xsl:template>
  <xsl:template match="Action">
    <b>Action:</b>
    <xsl:choose>
      <xsl:when test=". = 'Login'">
        <span style="color: green">
          <xsl:value-of select="."/>
        </span>
      </xsl:when>
      <xsl:when test=". = 'Logout'">
        <span style="color: black">
          <xsl:value-of select="."/>
        </span>
      </xsl:when>
      <xsl:when test=". = 'Failure'">
        <span style="color: red">
          <xsl:value-of select="."/>
        </span>
      </xsl:when>
      <xsl:otherwise>
        <span style=""> [Invalid or unspecified]</span>
      </xsl:otherwise>
    </xsl:choose>
    <br/>
  </xsl:template>
  <xsl:template match="Description">
    <b>Description: </b>
    <xsl:value-of select="."/>
    <br/>
  </xsl:template>
  <!-- The 5.4 xml schema provides no definition for the content for these -->
  <xsl:template match="Usage">
    <b>Usage: </b>
    <xsl:value-of select="."/>
    <br/>
  </xsl:template>
  <!-- used instead of declaring an empty UsageType named template -->
  <xsl:template match="LocalUser">
    <b>LocalUser: </b>
    <xsl:value-of select="."/>
    <br/>
  </xsl:template>
  <xsl:template match="LocalPrinter">
    <b>LocalPrinter: </b>
    <xsl:value-of select="."/>
    <br/>
  </xsl:template>
  <!-- Named templates are used for aggregate types -->
  <xsl:template name="MediaDescriptionType">
    <xsl:if test="MediaID">
      <b>MediaID: </b>
      <xsl:value-of select="MediaID"/>
      <br/>
    </xsl:if>
    <xsl:if test="MediaType">
      <b>MediaType: </b>
      <xsl:value-of select="MediaType"/>
      <br/>
    </xsl:if>
    <xsl:apply-templates select="SUID"/>
    <xsl:apply-templates select="Patient"/>
    <xsl:apply-templates select="Destination"/>
  </xsl:template>
  <xsl:template match="Destination">
    <xsl:call-template name="PrinterType"/>
  </xsl:template>
  <xsl:template name="UserType">
    <xsl:apply-templates select="LocalUser"/>
    <xsl:apply-templates select="RemoteNode"/>
  </xsl:template>
  <xsl:template name="PrinterType">
    <xsl:apply-templates select="LocalPrinter"/>
    <xsl:apply-templates select="Node"/>
  </xsl:template>
  <xsl:template match="Node">
    <xsl:call-template name="RemoteNodeType"/>
  </xsl:template>
  <xsl:template name="RemoteNodeType">
    <xsl:apply-templates select="IP"/>
    <xsl:apply-templates select="Hname"/>
    <xsl:apply-templates select="AET"/>
  </xsl:template>
  <xsl:template name="RetrieveInstancesType"/>
  <xsl:template name="BeginStoringInstancesType"/>
  <xsl:template name="InstancesActionType">
    <!-- renamed, called InstancesAction in xsd -->
    <xsl:apply-templates select="ObjectAction"/>
    <xsl:apply-templates select="AccessionNumber"/>
    <xsl:apply-templates select="SUID"/>
    <xsl:apply-templates select="Patient"/>
    <xsl:apply-templates select="User"/>
    <xsl:apply-templates select="CUID"/>
    <xsl:if test="NumberOfInstances">
      <b>NumberOfInstances: </b>
      <xsl:value-of select="NumberOfInstances"/>
      <br/>
    </xsl:if>
    <xsl:apply-templates select="MPPSUID"/>
  </xsl:template>
  <xsl:template name="PatientType">
    <xsl:apply-templates select="PatientID"/>
    <xsl:apply-templates select="PatientName"/>
  </xsl:template>
</xsl:stylesheet>
