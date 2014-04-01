package org.dcm4chee.arr.listeners.mdb;

import javax.ejb.Local;

@Local
public interface ReceiverHelperBeanLocal {
     byte[] cache=null;
     byte[] getCache() ;

     void setCache(byte[] cache) ;
}
