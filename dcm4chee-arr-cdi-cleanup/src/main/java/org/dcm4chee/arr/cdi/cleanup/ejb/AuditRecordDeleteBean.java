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

import org.dcm4chee.arr.cdi.conf.CleanUpConfigurationExtension;
import org.dcm4chee.arr.entities.AuditRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The Class AuditRecordDeleteBean.
 * used for deletion by the cleanup service
 * @author Hesham Elbadawi bsdreko@gmail.com
 */
@Stateless
public class AuditRecordDeleteBean {

  private static final Logger log=LoggerFactory.getLogger(AuditRecordDeleteBean.class);
  
  @PersistenceContext(unitName = "dcm4chee-arr")
  private EntityManager em;

/**
     * Delete record.
 * @param cleanUpConfig 
     * 
     * @param pk
     *            the pk
     */
  public void deleteRecord(CleanUpConfigurationExtension cleanUpConfig, long pk) {
      log.debug("DeleteRecord safe? " + cleanUpConfig.isArrSafeClean());
      AuditRecord recordToDelete = em.find(AuditRecord.class, pk);
      if (recordToDelete!=null) {
          if (recordToDelete.isDueDelete())
              em.remove(recordToDelete);
          if (!cleanUpConfig.isArrSafeClean())
              em.remove(recordToDelete);
          else
              recordToDelete.setDueDelete(true);
      }
      else {
          log.debug("AuditRecordDeleteBean.deleteRecord failed, audit doesn't exists. [pk:"+pk+"]");
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
            "SELECT r.pk FROM org.dcm4chee.arr.entities.AuditRecord r " +
                    "where r.eventDateTime < :retentionUnitsAgo and r.isDueDelete = false";
    Date now = new Date();
    Calendar cal = Calendar.getInstance();
    Timestamp retentionUnitsAgo;
    switch (TimeUnit.valueOf(unit)) {
        case HOURS:
            cal.add(Calendar.HOUR, -retention);
            break;
        case MINUTES:
            cal.add(Calendar.MINUTE, -retention);
            break;
        case SECONDS:
            cal.add(Calendar.SECOND, -retention);
            break;
        case DAYS:
        default:
            cal.add(Calendar.DATE, -retention);
            break;
    }

    List<Long> l =
            em.createQuery(queryStr)
            	.setParameter("retentionUnitsAgo", new Timestamp( cal.getTimeInMillis() ) )
                .setMaxResults(deletePerTransaction)
                .getResultList();
    
    log.debug("Executed the following JPQL statement: \n" + queryStr);
    
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
      String queryStr = "SELECT count(*) FROM org.dcm4chee.arr.entities.AuditRecord r where r.isDueDelete = false ";
      long recCount = em.createQuery(queryStr, Long.class).getSingleResult();
      if (recCount > maxRecordsAllowed) {
          int diffCount = (int) recCount - maxRecordsAllowed;
          queryStr =
                  "SELECT r FROM org.dcm4chee.arr.entities.AuditRecord r " +
                          "where r.isDueDelete = false " +
                          "ORDER BY r.eventDateTime ASC";
          if (diffCount > deletePerTransaction) {
              List<AuditRecord> recObjects =
                      em.createQuery(queryStr).setMaxResults(deletePerTransaction).getResultList();
              log.debug("Executed the following JPQL statement: \n" + queryStr);
              List<Long> l = new ArrayList<Long>();
              for (AuditRecord rec : recObjects) {
                  l.add(rec.getPk());
              }
              return l;
          } else {
              List<AuditRecord> recObjects =
                      em.createQuery(queryStr).setMaxResults(diffCount).getResultList();
              log.debug("Executed the following JPQL statement: \n" + queryStr);
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
                "SELECT r.pk FROM org.dcm4chee.arr.entities.AuditRecord r " +
                        "where r.eventID.value =:codeVal AND r.eventID.designator =:designatorVal " +
                        "AND r.eventDateTime < :retentionUnitsAgo AND r.isDueDelete = false";
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
          case SECONDS:
              retentionUnitsAgo = new Timestamp(now.getTime() - 1000 * retention);
              break;
          default:
              retentionUnitsAgo = new Timestamp(now.getTime() - 86400000 * retention);
              break;
      }
      @SuppressWarnings("unchecked")
    List<Long> l =
          em.createQuery(queryStr).setParameter("retentionUnitsAgo",
                  retentionUnitsAgo).setParameter("codeVal", codeVal)
                  .setParameter("designatorVal", designatorVal)
              .setMaxResults(deletePerTransaction).getResultList();
      
      log.debug("Executed the following JPQL statement: \n"+queryStr);
      return l;
    }

    public List<AuditRecord> getRecordsDueToDelete() {
        String queryStr = "SELECT r FROM org.dcm4chee.arr.entities.AuditRecord"
                + " r where r.isDueDelete = true";
        @SuppressWarnings("unchecked")
        List<AuditRecord> records = em.createQuery(queryStr).getResultList();
        log.debug("Executed the following JPQL statement: \n" + queryStr);
        log.debug("Found " + records.size() + " records");
        return records;
    }
}
