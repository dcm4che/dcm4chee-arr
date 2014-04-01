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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.arr.cdi.ejb;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 2, 2006
 */
public class XSLTUtils {

    public static final String USER = "arr-user.xsl";
    public static final String OBJECT = "arr-object.xsl";
    public static final String DETAILS = "arr-details.xsl";
    
    private static HashMap<String,Templates> templates =
            new HashMap<String,Templates>(4);
    private static SAXTransformerFactory tf;

    public static String render(String name, byte[] xmldata) {
        StringWriter writer = new StringWriter(512);
        render(name, xmldata, writer);
        return writer.toString();        
    }

    public static void render(String name, byte[] xmldata, Writer out) {
        try {
            render(getTemplates(name), xmldata, out);
        } catch (Exception e) {
            renderErrorMessage(e, out);
        }
    }
    
    private static void renderErrorMessage(Exception e, Writer out) {
        PrintWriter pw = (out instanceof PrintWriter) ?
                (PrintWriter) out : new PrintWriter(out);
        pw.print("<u>");
        pw.print(e.getMessage());
        pw.print("</u> <pre>");
        e.printStackTrace(pw);
        pw.print("</pre>");
    }
    
    private static void render(Templates tpl, byte[] xmldata, Writer out)
            throws TransformerException {
        tpl.newTransformer().transform(
                new StreamSource(new ByteArrayInputStream(xmldata)),
                new StreamResult(out));
    }
   
    private static Templates getTemplates(String name)
            throws TransformerException {
        Templates tpl = (Templates) templates.get(name);
        if (tpl == null) {
            tpl = loadTemplates(name);
            templates.put(name, tpl);
        }
        return tpl;
    }
    
    private static Templates loadTemplates(String name)
            throws TransformerException  {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return transfomerFactory().newTemplates(
                new StreamSource(cl.getResource(name).toString()));
    }
    
    private static SAXTransformerFactory transfomerFactory() {
	if (tf == null) {
	    tf = (SAXTransformerFactory) TransformerFactory.newInstance();
	}
	return tf;
    }
    
}
