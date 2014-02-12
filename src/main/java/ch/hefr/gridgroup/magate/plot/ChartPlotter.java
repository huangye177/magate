/**
 * 
 */
package ch.hefr.gridgroup.magate.plot;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.jdbc.JDBCCategoryDataset;
import org.jfree.ui.ApplicationFrame;

import ch.hefr.gridgroup.magate.storage.MaGateDB;

/**
 * @author yehuang
 *
 */
//public abstract class ChartPlotter extends ApplicationFrame {
public abstract class ChartPlotter extends JFrame {

	protected JFreeChart printChart;
//	protected ChartPanel chartPanel;
//	protected JPanel chartPanel;
	
	protected Connection chartDBConnection;
//	protected String sql;
	protected String title;

	protected String rangeText = "Total number of jobs submitted to the community";
	protected String domainText = "Scenarios";
	
	private String plotterId;

	/**
	 * 
	 */
	public ChartPlotter(String title) {
		
		super(title);
		this.plotterId = "ChartPlotter_" + UUID.randomUUID().toString();
		
		try {
			this.chartDBConnection = MaGateDB.getConnectionPool().getConnection();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	protected abstract  void readScreePlotDataset();
	
	protected abstract void refreshDataset();
	
	protected abstract void plotScreen();
	
	protected abstract void plotPrint();
	
	public void chartShutdown() {
		
		if(this.chartDBConnection != null) {
			
			try {
				
				this.chartDBConnection.close();
				this.chartDBConnection = null;
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
    
    // ---------- ---------- ---------- ---------- ---------- ---------- 
	//                        Getter/Setter methods
	// ---------- ---------- ---------- ---------- ---------- ---------- 
    
    public JFreeChart getPrintChart() {
		return printChart;
	}
    
    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getPlotterId() {
		return plotterId;
	}

}



