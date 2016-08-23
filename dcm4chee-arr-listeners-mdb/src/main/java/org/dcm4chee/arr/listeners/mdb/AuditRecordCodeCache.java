//
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

package org.dcm4chee.arr.listeners.mdb;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceReloaded;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceStarted;
import org.dcm4chee.arr.cdi.Impl.ReloadEvent;
import org.dcm4chee.arr.cdi.Impl.StartStopEvent;
import org.dcm4chee.arr.entities.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton cache for ARR codes.
 * </p>
 * 
 * Invariants of ARR codes:
 * <ol>
 *   <li>Codes are immutable. Once created and persisted to the DB, they never change</li>
 *   <li>Codes are never removed from the DB</li>
 * </ol>
 * 
 * Because of the variants the cache can never be in a dirty state (out-of-date with the DB).
 * 
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
@ApplicationScoped
public class AuditRecordCodeCache {
    private static final Logger LOG = LoggerFactory.getLogger(AuditRecordCodeCache.class);
    
    /*
     * Even if the cache is used by concurrent clients using a HashMap is thread-safe:
     * 1) The same entry can be added multiple times (concurrently) without problems
     * 2) There is no need for a visibility guarantee (that readers see writes immediately)
     */
    private final Map<CacheKey,Code> cache = new HashMap<>();
    
    private boolean enabled;
    
    public AuditRecordCodeCache() {
        // empty
    }
    
    private void init(Device device) {
        clear();
        try {
            AuditRecordRepository arrExtension = device.getDeviceExtension(AuditRecordRepository.class);
            enabled = arrExtension.isAuditRecordCodeCachingEnabled();
        } catch(Exception e) {
            LOG.error("Error while initializing ARR Code caching", e);
        }
        LOG.info("Initialized ARR code caching, enabled: " + enabled);
    }
    
    public void clear() {
        LOG.info("Cleared ARR code cache");
        cache.clear();
    }
    
    public void onAuditRecordRepositoryReload(@Observes @AuditRecordRepositoryServiceReloaded ReloadEvent reloadEvent) {
        init(reloadEvent.getDevice());
    }
    
    public void onAuditRecordRepositoryStart(@Observes @AuditRecordRepositoryServiceStarted StartStopEvent startEvent) {
        init(startEvent.getDevice());
    }
    
    public Code getCode(String codeValue, String codeDesignator) {
        return enabled ? cache.get(new CacheKey(codeValue, codeDesignator)) : null;
    }
    
    public void cacheCode(Code code) {
        if(enabled) {
            cache.put(new CacheKey(code.getValue(), code.getDesignator()), code);
        }
    }
    
    private static class CacheKey {
        private final String codeValue;
        private final String codeDesignator;
        
        private CacheKey(String codeValue, String codeDesignator) {
            this.codeValue = codeValue;
            this.codeDesignator = codeDesignator;
        }
        
        @Override
        public boolean equals(Object other) {
            if(this == other) {
                return true;
            }
            
            if(other == null || !(other instanceof CacheKey)) {
                return false;
            }
            
            CacheKey that = (CacheKey)other;
            return codeValue.equals(that.codeValue) && codeDesignator.equals(that.codeDesignator);
        }
        
        @Override
        public int hashCode() {
            return 41 * codeValue.hashCode() + 101 * codeDesignator.hashCode();
        }
        
    }
    
}
