/*
 * LengthComparator.java
 *
 * Created on 19. øíjen 2007, 14:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ch.hefr.gridgroup.magate.ext;
import java.util.Comparator;

import ch.hefr.gridgroup.magate.model.JobInfo;

/**
 *
 * This class compares two jobs according to their estimated execution time
 * @author Dalibor Klusacek
 * @author Ye HUANG (modification)
 */
public class LengthComparator  implements Comparator {
    
    /**
     * Represents a Comparator
     */
    public int compare(Object o1, Object o2) {
    	JobInfo g1 = (JobInfo) o1;
    	JobInfo g2 = (JobInfo) o2;
        double length1 = (Double) g1.getEstimatedComputationTime();  
        double length2 = (Double) g2.getEstimatedComputationTime();  
        if(length1 < length2) return 1;
        if(length1 == length2) return 0;
        if(length1 > length2) return -1;
        return 0;
    }
    
}
