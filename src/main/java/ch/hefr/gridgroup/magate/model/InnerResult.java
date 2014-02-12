package ch.hefr.gridgroup.magate.model;

/**
 * Inner class for process PAIR: record[] - counter
 * @author ye Huang
 */
public class InnerResult {
	
	public double[] record;
	public String name;
	public int counter;
	
	public InnerResult(int arraySize) {
		this.record  = new double[arraySize];
		this.counter = 0;
		this.name = "";
	}
}
