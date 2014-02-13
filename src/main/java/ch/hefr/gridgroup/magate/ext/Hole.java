/*
 * Hole.java
 
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ch.hefr.gridgroup.magate.ext;

import ch.hefr.gridgroup.magate.model.JobInfo;

/**
 * This class represents a hole or so called gap in the schedule.
 * @author Dalibor Klusacek
 * @author Ye HUANG (modification)
 */
public class Hole {
    private double start;
    private double end;
    private double length;
    private double mips;
    private int size;
    private JobInfo position;
    
    /** Creates a new instance of Hole */
    public Hole(double start, double end, double length, double mips, int size, JobInfo position) {
        this.setStart(start);        
        this.setEnd(end);
        this.setLength(length);
        this.setSize(size);
        this.setPosition(position);
        this.setMips(mips);
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public JobInfo getPosition() {
        return position;
    }

    public void setPosition(JobInfo position) {
        this.position = position;
    }

    public double getMips() {
        return mips;
    }

    public void setMips(double mips) {
        this.mips = mips;
    }
    
}
