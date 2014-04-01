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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4chee.arr.cdi.Impl;

/**
 * The Class RemoteSource.
 * Passed as a source with the audit log used event to provide identification for the remote user accessing the system.
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class RemoteSource implements Participant {
    
	private String remoteHost;
	private String remoteUserName;
	private String URI;
	
	/**
     * Instantiates a new remote source.
     * 
     * @param host
     *            the host
     * @param userName
     *            the user name
     * @param URI
     *            the uri
     */
	public RemoteSource(String host, String userName, String URI)
	{
		this.remoteHost=host;
		this.remoteUserName=userName;
		this.URI=URI;
	}
    
    /**
     * Gets the uri.
     * 
     * @return the uri
     */
    public String getURI() {
		return URI;
	}

	/**
     * Sets the uri.
     * 
     * @param uRI
     *            the new uri
     */
	public void setURI(String uRI) {
		URI = uRI;
	}

	/**
     * Gets the remote user name.
     * 
     * @return the remote user name
     */
	public String getRemoteUserName() {
		return remoteUserName;
	}

	/**
     * Sets the remote user name.
     * 
     * @param remoteUserName
     *            the new remote user name
     */
	public void setRemoteUserName(String remoteUserName) {
		this.remoteUserName = remoteUserName;
	}

	/**
     * Sets the remote host.
     * 
     * @param remoteHost
     *            the new remote host
     */
	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	/**
     * Gets the remote identity.
     * 
     * @return the remote identity
     */
	public String getRemoteIdentity() {
        return unknownIfNull(remoteUserName);
    }

    /**
     * Gets the remote host.
     * 
     * @return the remote host
     */
    public String getRemoteHost() {
        return unknownIfNull(remoteHost);
    }

    /**
     * Unknown if null.
     * 
     * @param value
     *            the value
     * @return the string
     */
    private static String unknownIfNull(String value) {
        return value != null ? value : Participant.UNKNOWN;
    }

	/* (non-Javadoc)
	 * @see org.dcm4chee.arr.cdi.Impl.Participant#getHost()
	 */
	@Override
	public String getHost() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dcm4chee.arr.cdi.Impl.Participant#getIdentity()
	 */
	@Override
	public String getIdentity() {
		// TODO Auto-generated method stub
		return null;
	}
}
