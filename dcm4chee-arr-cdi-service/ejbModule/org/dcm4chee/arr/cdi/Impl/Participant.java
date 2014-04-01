/*
 * 
 */
package org.dcm4chee.arr.cdi.Impl;

/**
 * The Interface Participant.
 * 
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 */
public interface Participant {
   
   public static String UNKNOWN = "UNKNOWN";
   
   /**
     * Gets the identity.
     * 
     * @return the identity
     */
   public String getIdentity();
           
   /**
     * Gets the host.
     * 
     * @return the host
     */
   public String getHost();
   
}