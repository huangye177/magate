package ch.hefr.gridgroup.magate.model;

import java.util.Comparator;

/**
 * Comparator of InnerResult object according to clock time
 * @author ye Huang
 */
public class ResultComparator implements Comparator<Object> {

	/**
     * Represents a Comparator
     */
    public int compare(Object o1, Object o2) {
    	InnerResult ir1 = (InnerResult) o1;
    	InnerResult ir2 = (InnerResult) o2;
    	
        double clock1 = (Double) ir1.record[1];
        double clock2 = (Double) ir2.record[1];
        
        if(clock1 < clock2) return -1;
        if(clock1 == clock2) return 0;
        if(clock1 > clock2) return 1;
        return 0;
    }

}
