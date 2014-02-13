/*
 * JobComparator.java
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ch.hefr.gridgroup.magate.ext;

import java.util.Comparator;

import ch.hefr.gridgroup.magate.model.JobInfo;

/**
 * This class compares two jobs according to their priority
 * @author Dalibor Klusacek
 * @author Ye HUANG (modification)
 */
public class JobComparator implements Comparator {
    
    /**
     * Represents a Comparator
     */
    public int compare(Object o1, Object o2) {
    	JobInfo g1 = (JobInfo) o1;
    	JobInfo g2 = (JobInfo) o2;
        double priority1 = (Double) g1.getJobPriority();  
        double priority2 = (Double) g2.getJobPriority();  
        if(priority1 < priority2) return 1;
        if(priority1 == priority2) return 0;
        if(priority1 > priority2) return -1;
        return 0;
    }
    
}
