/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2013
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.arr.cdi.conf.ldap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.conf.ldap.audit.LdapAuditLoggerConfiguration;
import org.dcm4che3.conf.ldap.audit.LdapAuditRecordRepositoryConfiguration;
import org.dcm4che3.conf.ldap.generic.LdapGenericConfigExtension;
import org.dcm4che3.util.StreamUtils;
import org.dcm4chee.arr.cdi.cleanup.CleanUpConfigurationExtension;

import java.io.*;
import java.util.Properties;

/**
 * A factory for creating LdapConfiguration.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@Default
public class LdapConfigurationFactory {
	private LdapDicomConfiguration AuditRecordRepositoryDicomConfiguration; 
	private LdapAuditRecordRepositoryConfiguration AuditRecordRepositoryDicomConfigurationExtension;
	private LdapAuditLoggerConfiguration auditLogger; 
	private LdapArrEventFilterConfiguration arrEventFilter;
	private final String LDAP_PROPERTIES_PROPERTY = "org.dcm4chee.arr.ldap";
	
	/**
     * Dicom configuration.
     * becomes the only producer for the configuration injection point
     * @return the dicom configuration
     * @throws ConfigurationException
     *             the configuration exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
	@Produces
	@ApplicationScoped
	public DicomConfiguration dicomConfiguration()
			throws ConfigurationException,IOException {
	
		AuditRecordRepositoryDicomConfiguration = new LdapDicomConfiguration(ldapEnv());
		
		AuditRecordRepositoryDicomConfigurationExtension = new LdapAuditRecordRepositoryConfiguration();
		AuditRecordRepositoryDicomConfiguration.addDicomConfigurationExtension(AuditRecordRepositoryDicomConfigurationExtension);
		LdapGenericConfigExtension<CleanUpConfigurationExtension> ext = new LdapGenericConfigExtension<CleanUpConfigurationExtension>(CleanUpConfigurationExtension.class);
		auditLogger = new LdapAuditLoggerConfiguration();
		arrEventFilter = new LdapArrEventFilterConfiguration();
		AuditRecordRepositoryDicomConfiguration.addDicomConfigurationExtension(ext);
	    AuditRecordRepositoryDicomConfiguration.addDicomConfigurationExtension(auditLogger);
	    AuditRecordRepositoryDicomConfiguration.addDicomConfigurationExtension(arrEventFilter);
		System.out.println("Adding Extension for cleanup/auditlogger/EventFilter");
		return AuditRecordRepositoryDicomConfiguration;
	}

	/**
     * Dispose.
     * 
     * @param conf
     *            the conf
     */
	public void dispose(@Disposes DicomConfiguration conf) {
		conf.close();
	}
    
    /**
     * Ldap env.
     * 
     * @return the properties
     * @throws ConfigurationException
     *             the configuration exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private Properties ldapEnv() throws ConfigurationException, IOException {
        String url = System.getProperty(LDAP_PROPERTIES_PROPERTY);

        Properties p = new Properties();
        	 InputStream in = null;
			try {
				in = StreamUtils.openFileOrURL(url);
				p.load(in);
			} catch (IOException e) {
				throw new ConfigurationException(e);
			}
			finally
			{
				in.close();
			}
        
        return p;
    }
    
}