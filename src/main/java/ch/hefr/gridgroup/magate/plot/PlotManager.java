package ch.hefr.gridgroup.magate.plot;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.RefineryUtilities;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.env.MaGateToolkit;
import ch.hefr.gridgroup.magate.storage.MaGateDB;

public class PlotManager {

	protected static final Log logger = LogFactory.getLog(PlotManager.class);
	
	private PlotAnimator animator;
	
//	private ChartPlotter communityStatus_ScreenChart;
//	private ChartPlotter scenarioStatistic_PrintChart;
	
	private ConcurrentHashMap<String, ChartPlotter> screenPlotter_perIteration   = new ConcurrentHashMap<String, ChartPlotter>();
	private ConcurrentHashMap<String, ChartPlotter> screenPlotter_perScenario    = new ConcurrentHashMap<String, ChartPlotter>();
	private ConcurrentHashMap<String, ChartPlotter> screenPlotter_perExperiment  = new ConcurrentHashMap<String, ChartPlotter>();
	
	private ConcurrentHashMap<String, ChartPlotter> printPlotter_perIteration    = new ConcurrentHashMap<String, ChartPlotter>();
	private ConcurrentHashMap<String, ChartPlotter> printPlotter_perScenario     = new ConcurrentHashMap<String, ChartPlotter>();
	
	public PlotManager() {
	}
	
	public void animatorSetup() {
		
		this.animator = new PlotAnimator(this);
		this.animator.start();
		
	}

	public void animatorShutdown() {
		
		this.animator.stop();
		this.animator = null;
		
	}
	
	public void screenPlotterSetup_perIteration() {
		
		if(MaGateParam.screenPlotActivated) {
			
			ChartPlotter dynamicCommunity = new DynamicCommunityPlotter("Community Live Statistic");		
	        screenPlotter_perIteration.put(dynamicCommunity.getPlotterId(), dynamicCommunity);
			
	        // Start screen plotter
			this.plotScreenChart(screenPlotter_perIteration);
		}
	}
	
	public void screenPlotterShutdown_perIteration() {
		
		if(!this.screenPlotter_perIteration.isEmpty()) {
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Collection<ChartPlotter> plotterCollection = this.screenPlotter_perIteration.values();
			
			for(ChartPlotter plotter : plotterCollection) {
				
				plotter.setVisible(false);
				plotter.dispose();
				plotter.chartShutdown();
				plotter = null;
			}
			
			this.screenPlotter_perIteration.clear();
		}
		
	}

	/**
	 * To be completed
	 */
	public void screenPlotterSetup_perScenario() {}
	
	/**
	 * To be completed
	 */
	public void screenPlotterShutdown_perScenario() {}
	
	/**
	 * To be completed
	 */
	public void screenPlotterSetup_perExperiment() {}
	
	/**
	 * To be completed
	 */
	public void screenPlotterShutdown_perExperiment() {}
	
	
	/**
	 * To be completed, blank plot so far
	 */
	public void printPlotter_perIteration() {
		
		// 1st: append plot to print per iteration
		System.out.println("Printing per-iteraion plot...");
		ChartPlotter dynamicCommunity = new DynamicCommunityPlotter("Community Live Statistic");
        this.printPlotter_perIteration.put(dynamicCommunity.getPlotterId(), dynamicCommunity);
        
		
		// 2nd: print plot per iteration
//		this.plotPrintChart(this.printPlotter_perIteration, "");
		this.plotPrintChart(this.printPlotter_perIteration, MaGateParam.currentIterationId);
		System.out.println("Per-iteration plot created!");
		
	}
	
	/**
	 * To be completed, blank plot so far
	 */
	public void printPlotter_perScenario() {
		
		// 1st: append plot to print per scenario
		System.out.println("Printing per-scenario plot...");
		ChartPlotter scenarioCommunity = new ScenarioCommunityPlotter("Community Per-Scenario Statistic");
        this.printPlotter_perScenario.put(scenarioCommunity.getPlotterId(), scenarioCommunity);
        
		// 2nd: print plot per scenario
//		this.plotPrintChart(this.printPlotter_perScenario, "PerScenario-Community-Statistic");
		this.plotPrintChart(this.printPlotter_perScenario, MaGateParam.currentScenarioId);
		System.out.println("Per-scenario plot created!");
	}
	
	/**
	 * Plot screen charts
	 * 
	 * @param screenPlotters
	 */
	private void plotScreenChart(ConcurrentHashMap<String, ChartPlotter> screenPlotters) {
		
		if(!screenPlotters.isEmpty()) {
			
			Collection<ChartPlotter> plotterCollection = screenPlotters.values();
			
			for(ChartPlotter plotter : plotterCollection) {
				
				plotter.refreshDataset();
				
				plotter.pack();
				RefineryUtilities.centerFrameOnScreen(plotter);
				plotter.setVisible(true);
			}
		}
		
	}
	
	/**
	 * Plot print charts
	 * 
	 * @param printPlotters
	 */
	private void plotPrintChart(ConcurrentHashMap<String, ChartPlotter> printPlotters, String title) {
		
		if(!printPlotters.isEmpty()) {
			
			Collection<ChartPlotter> plotterCollection = printPlotters.values();
			
			for(ChartPlotter plotter : plotterCollection) {
				
				// update dataset and chart handler
				plotter.plotPrint();
				
				// generate PDF plot
				if(title == null || title.equals("")) {
			        String pdfPlotName = plotter.getTitle();
			        PDFGenerator.generatePDFChart(plotter.getPrintChart(), pdfPlotName, 900, 600);
				} else {
			        PDFGenerator.generatePDFChart(plotter.getPrintChart(), title, 900, 600);
				}
				
		        plotter.chartShutdown();
		        plotter = null;
			}
		}
	}
	
	/**
	 * Invoke all owned screenPlotter to refresh their dataset
	 */
	public void refresh() {
		
		try {
			
			if (!this.screenPlotter_perIteration.isEmpty()) {
				
				Collection<ChartPlotter> collection = this.screenPlotter_perIteration.values();
				for (ChartPlotter plotter : collection) {
					plotter.refreshDataset();
				}
				
			}
			
		} catch (Exception e) {
			logger.error("===== ERROR: MaGateGUI refresh failed. =====");
		}
		
	}
    
}


