package org.dcm4chee.arr.cdi.conf.prefs;


import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che3.conf.prefs.PreferencesDicomConfigurationExtension;
import org.dcm4che3.conf.prefs.PreferencesUtils;
import org.dcm4che3.net.Device;
import org.dcm4chee.arr.cdi.cleanup.EventTypeFilter;
import org.dcm4chee.arr.cdi.cleanup.EventTypeFilterExtension;
import org.dcm4chee.arr.cdi.cleanup.EventTypeObject;

public class PreferencesArrEventFilterConfiguration extends PreferencesDicomConfigurationExtension{

    @Override
    protected void storeChilds(Device device, Preferences deviceNode) {
        EventTypeFilterExtension ext =
                device.getDeviceExtension(EventTypeFilterExtension.class);
        if (ext != null)
            storeTo(ext.getEventTypeFilter(),
                    deviceNode.node("arrEventTypeFilter"));
    }

    private void storeTo(EventTypeFilter filter, Preferences prefs) {
        for (Entry<String, EventTypeObject> entry : filter.getEntries())
            storeTo(entry.getValue(), prefs.node(entry.getKey()));
    }

    private void storeTo(EventTypeObject Obj, Preferences prefs) {
        prefs.put("eventIDTypeCode", Obj.getCodeID());
        PreferencesUtils.storeNotNull(prefs, "eventTypeRetention", Obj.getRetentionTime());
        PreferencesUtils.storeNotNull(prefs, "eventTypeRetentionUnit", Obj.getRetentionTimeUnit());
    }

    @Override
    protected void loadChilds(Device device, Preferences deviceNode)
            throws BackingStoreException {
        if (!deviceNode.nodeExists("arrEventTypeFilter"))
            return;
        
        Preferences prefs = deviceNode.node("arrEventTypeFilter");
        EventTypeFilter filter = new EventTypeFilter();
        for (String code : prefs.childrenNames())
            filter.addEventType(code, load(prefs.node(code)));

        device.addDeviceExtension(new EventTypeFilterExtension(filter));
    }

    private EventTypeObject load(Preferences prefs) {
      EventTypeObject obj= new EventTypeObject(
                prefs.get("eventIDTypeCode", null),
                Long.parseLong(prefs.get("eventTypeRetention", null)),
                prefs.get("eventTypeRetentionUnit", null));
      return obj;
    }

    @Override
    protected void mergeChilds(Device prev, Device device, Preferences deviceNode)
            throws BackingStoreException {
        EventTypeFilterExtension prevExt =
                prev.getDeviceExtension(EventTypeFilterExtension.class);
        EventTypeFilterExtension ext =
                device.getDeviceExtension(EventTypeFilterExtension.class);
        if (ext == null && prevExt == null)
            return;
        
        Preferences factoryNode = deviceNode.node("arrEventTypeFilter");
        if (ext == null)
            factoryNode.removeNode();
        else if (prevExt == null)
            storeTo(ext.getEventTypeFilter(), factoryNode);
        else
            storeDiffs(factoryNode, prevExt.getEventTypeFilter(),
                    ext.getEventTypeFilter());
    }

    private void storeDiffs(Preferences prefs, EventTypeFilter prevFilter ,
            EventTypeFilter filter) throws BackingStoreException {
        for (Entry<String, EventTypeObject> entry : prevFilter.getEntries()) {
            String code = entry.getKey();
            if (filter.getEventType(code) == null) {
                Preferences node = prefs.node(code);
                node.removeNode();
                node.flush();
            }
        }
        for (Entry<String, EventTypeObject> entry : filter.getEntries()) {
            String code = entry.getKey();
            storeDiffs(prefs.node(code), prevFilter.getEventType(code), entry.getValue());
        }
    }

    private void storeDiffs(Preferences prefs,
            EventTypeObject prev, EventTypeObject Obj) {
        if (prev != null) {
            PreferencesUtils.storeDiff(prefs, "eventIDTypeCode",
                    prev.getCodeID(), Obj.getCodeID());
            PreferencesUtils.storeDiff(prefs, "eventTypeRetention",
                    prev.getRetentionTime(), Obj.getRetentionTime());
            PreferencesUtils.storeDiff(prefs, "eventTypeRetentionUnit",
                    prev.getRetentionTimeUnit(), Obj.getRetentionTimeUnit());
        } else
            storeTo(Obj, prefs);
    }
}
