/*
 *  ***** BEGIN LICENSE BLOCK ***** Version: MPL 1.1/GPL 2.0/LGPL 2.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights
 * and limitations under the License.
 * 
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in Java(TM), hosted at
 * https://github.com/gunterze/dcm4che.
 * 
 * The Initial Developer of the Original Code is Agfa Healthcare. Portions created by the Initial
 * Developer are Copyright (C) 2011 the Initial Developer. All Rights Reserved.
 * 
 * Contributor(s): See @authors listed below
 * 
 * Alternatively, the contents of this file may be used under the terms of either the GNU General
 * Public License Version 2 or later (the "GPL"), or the GNU Lesser General Public License Version
 * 2.1 or later (the "LGPL"), in which case the provisions of the GPL or the LGPL are applicable
 * instead of those above. If you wish to allow use of your version of this file only under the
 * terms of either the GPL or the LGPL, and not to allow others to use your version of this file
 * under the terms of the MPL, indicate your decision by deleting the provisions above and replace
 * them with the notice and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under the terms of any one of
 * the MPL, the GPL or the LGPL.
 * 
 * ***** END LICENSE BLOCK *****
 */

package org.dcm4chee.arr.cdi.cleanup.ejb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.dcm4chee.arr.cdi.conf.CleanUpConfigurationExtension;
import org.dcm4chee.arr.entities.AuditRecord;

/**
 * The Class AuditRecordDeleteBean.
 * used for deletion by the cleanup service
 * @author Hesham Elbadawi bsdreko@gmail.com
 */
@Stateless
public class AuditRecordDeleteBean {

  private static final Logger log=Logger.getLogger(AuditRecordDeleteBean.class);
  
  @PersistenceContext(unitName = "dcm4chee-arr")
  private EntityManager em;

  public EntityManager getEm() {
    return em;
}

/**
     * Delete record.
 * @param cleanUpConfig 
     * 
     * @param pk
     *            the pk
     */
  public void deleteRecord(CleanUpConfigurationExtension cleanUpConfig, long pk) {
      AuditRecord recordToDelete = em.find(AuditRecord.class, pk);
      if(cleanUpConfig.isArrSafeClean())
          backup(recordToDelete, cleanUpConfig.getArrBackUpDir());
    em.remove(recordToDelete);
  }

  private void backup(AuditRecord recordToDelete, String backUpDir) {
      String uniqueHash = ""+new String(""+System.currentTimeMillis()).hashCode();
    String backUpFileName = "Audit"+"-"+uniqueHash+"-from-"+recordToDelete.getSourceID()+"-DB-"+recordToDelete.getPk()+".tmp";
    log.info("Backing up file "+backUpFileName+" using backup dir "+backUpDir);
    File backUpFile = new File(backUpDir,backUpFileName);
    FileOutputStream fout = null;
    try {
        fout = new FileOutputStream(backUpFile);
    } catch (FileNotFoundException e) {
        log.error("Error creating temporary backup file backup dir is configure wrong ",e);
    }
    byte[] blob= recordToDelete.getXmldata();
    try {
        fout.write(blob);
    } catch (IOException e) {
        log.error("Error writing backup file ",e);
    }
    finally{
        try {
            fout.close();
        } catch (IOException e) {
            log.error("Error closing backup file output stream ",e);
        }
    }
    File finalTarget = new File(backUpFile.getPath().substring(0,backUpFile.getPath().length()-4));
    log.info("renaming from "+backUpFile.getPath() + " to "+finalTarget.getPath());
    if(backUpFile.renameTo(finalTarget))
    {
        log.info("Wrote backup file "+backUpFileName);
    }
    else
    {
        log.error("Error renaming file, non final backup data present and will not be archived");
    }
    
}

/**
     * Gets the pks by retention time.
     * 
     * @param retention
     *            the retention
     * @param unit
     *            the unit
     * @param deletePerTransaction
     *            the delete per transaction
     * @return the p ks by retention
     */
  public List<Long> getPKsByRetention(int retention, String unit, int deletePerTransaction) {
    String queryStr =
        "SELECT r.pk FROM org.dcm4chee.arr.entities.AuditRecord r where r.eventDateTime < :retentionUnitsAgo";
    Date now = new Date();
    Timestamp retentionUnitsAgo;
    switch (TimeUnit.valueOf(unit)) {
      case DAYS:
        retentionUnitsAgo = new Timestamp(now.getTime() - 86400000 * retention);
        break;
      case HOURS:
        retentionUnitsAgo = new Timestamp(now.getTime() - 3600000 * retention);
        break;
      case MINUTES:
        retentionUnitsAgo = new Timestamp(now.getTime() - 60000 * retention);
        break;
      default:
        retentionUnitsAgo = new Timestamp(now.getTime() - 86400000 * retention);
        break;
    }
    List<Long> l =
        em.createQuery(queryStr).setParameter("retentionUnitsAgo", retentionUnitsAgo)
            .setMaxResults(deletePerTransaction).getResultList();
    log.debug("Executed the following JPQL statement: \n"+queryStr);
    return l;
  }

  /**
     * Gets the pks by max records.
     * 
     * @param maxRecordsAllowed
     *            the max records allowed
     * @param deletePerTransaction
     *            the delete per transaction
     * @return the pks by max records
     */
  public List<Long> getPksByMaxRecords(int maxRecordsAllowed, int deletePerTransaction) {
    String queryStr = "SELECT count(*) FROM org.dcm4chee.arr.entities.AuditRecord r";
    long recCount = em.createQuery(queryStr, Long.class).getSingleResult();
    if (recCount > maxRecordsAllowed) {
      int diffCount = (int) recCount - maxRecordsAllowed;
      queryStr =
          "SELECT r FROM org.dcm4chee.arr.entities.AuditRecord r ORDER BY r.eventDateTime ASC";
      if (diffCount > deletePerTransaction) {
        List<AuditRecord> recObjects =
            em.createQuery(queryStr).setMaxResults(deletePerTransaction).getResultList();
        log.debug("Executed the following JPQL statement: \n"+queryStr);
        List<Long> l = new ArrayList<Long>();
        for (AuditRecord rec : recObjects) {
          l.add(rec.getPk());
        }
        return l;
      } else {
        List<AuditRecord> recObjects =
            em.createQuery(queryStr).setMaxResults(diffCount).getResultList();
        log.debug("Executed the following JPQL statement: \n"+queryStr);
        List<Long> l = new ArrayList<Long>();
        for (AuditRecord rec : recObjects) {
          l.add(rec.getPk());
        }
        return l;
      }
    } else {
      return null;
    }
  }
  
  /**
     * Gets the pks by event id type code.
     * 
     * @param code
     *            the code
     * @param retention
     *            the retention
     * @param unit
     *            the unit
     * @param deletePerTransaction
     *            the delete per transaction
     * @return the p ks by event id type code
     */
  public List<Long> getPKsByEventIDTypeCode(String code,int retention, String unit, int deletePerTransaction) {
      String queryStr =
"SELECT r.pk FROM org.dcm4chee.arr.entities.AuditRecord r where r.eventID.value =:codeVal AND r.eventID.designator =:designatorVal AND r.eventDateTime < :retentionUnitsAgo";
      Date now = new Date();
      String codeVal = code.split("\\^")[0];
      String designatorVal = code.split("\\^")[1];
      log.debug("Processing the following code " + codeVal + " with designator "+ designatorVal);
      Timestamp retentionUnitsAgo;
      switch (TimeUnit.valueOf(unit)) {
        case DAYS:
          retentionUnitsAgo = new Timestamp(now.getTime() - 86400000 * retention);
          break;
        case HOURS:
          retentionUnitsAgo = new Timestamp(now.getTime() - 3600000 * retention);
          break;
        case MINUTES:
          retentionUnitsAgo = new Timestamp(now.getTime() - 60000 * retention);
          break;
        default:
          retentionUnitsAgo = new Timestamp(now.getTime() - 86400000 * retention);
          break;
      }
      List<Long> l =
          em.createQuery(queryStr).setParameter("retentionUnitsAgo", retentionUnitsAgo).setParameter("codeVal", codeVal).setParameter("designatorVal", designatorVal)
              .setMaxResults(deletePerTransaction).getResultList();
      log.debug("Executed the following JPQL statement: \n"+queryStr);
      return l;
    }
}
