package ch.hefr.gridgroup.magate.plot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.jdbc.JDBCCategoryDataset;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import ch.hefr.gridgroup.magate.env.JobCenterManager;
import ch.hefr.gridgroup.magate.env.MaGateParam;

public class ScenarioCommunityPlotter extends ChartPlotter {

protected static final Log logger = LogFactory.getLog(ScenarioCommunityPlotter.class);

	private TimeSeriesCollection[] perScenarioTimeseriesCollection; 
	private TimeSeries[] perScenarioTimeseries;
	
	public ScenarioCommunityPlotter(String title) {
		
		super(title);
		
		/*******************************************************************************
		 * array[0]: CE [1] toPrint_chart
		 *******************************************************************************/
		this.perScenarioTimeseriesCollection = new TimeSeriesCollection[2];
		this.perScenarioTimeseries = new TimeSeries[9];
		
		this.generateSeries();
		for(int j = 0; j < this.perScenarioTimeseriesCollection.length; j++) {
			this.perScenarioTimeseriesCollection[j] = new TimeSeriesCollection();
		}
		
		// Creating dataset, as well as chart itself
		this.plotScreen();
        
	}
			
	@Override
	protected void plotScreen() {
		
		
        
	}
	
	protected void plotPrint() {
		
		/*************** recentJob_CE Line chart ***************/
		this.readPrintPlotDataset();
		
		/*************** CE for all Line charts ***************/
		this.perScenarioTimeseriesCollection[0].addSeries(perScenarioTimeseries[6]);  // ce
		
		// Set plot
		this.perScenarioTimeseriesCollection[1].addSeries(perScenarioTimeseries[0]);  // arrived_job
		this.perScenarioTimeseriesCollection[1].addSeries(perScenarioTimeseries[1]);  // scheduling_job
		this.perScenarioTimeseriesCollection[1].addSeries(perScenarioTimeseries[2]);  // processing_job
		this.perScenarioTimeseriesCollection[1].addSeries(perScenarioTimeseries[3]);  // executed_job
		this.perScenarioTimeseriesCollection[1].addSeries(perScenarioTimeseries[4]);  // suspended_job
		
		TimeSeries meanExecutedJob = MovingAverage.createMovingAverage(perScenarioTimeseries[3], "mean of executed jobs", 10, 0);
		TimeSeries meanSuspendedJob = MovingAverage.createMovingAverage(perScenarioTimeseries[4], "mean of suspended jobs", 10, 0);
		
		this.perScenarioTimeseriesCollection[1].addSeries(meanExecutedJob);
		this.perScenarioTimeseriesCollection[1].addSeries(meanSuspendedJob);
		
		this.printChart = ChartFactory.createTimeSeriesChart(
			"Community Status Averaged Overview", // chart title
            "Date/Time",
            "Num. of jobs",
            this.perScenarioTimeseriesCollection[1],                     
            true,                       // include legend
            true,
            false
        );
		
//		this.printChart.addSubtitle(new TextTitle(MaGateParam.numberOfTotalNode + 
//				" nodes and " + MaGateParam.totalNumberOfJob + " jobs in total"));
		this.printChart.addSubtitle(new TextTitle(MaGateParam.currentScenarioId));
        
        XYPlot recentJobPlot = (XYPlot) this.printChart.getPlot();
        this.printChart.setBackgroundPaint(Color.white);
        recentJobPlot.setBackgroundPaint(Color.lightGray);
        recentJobPlot.setDomainGridlinePaint(Color.white);
        recentJobPlot.setRangeGridlinePaint(Color.white);
        
        // change the auto tick unit selection to integer units only...
        NumberAxis recentJobPlotRangeAxis = (NumberAxis) recentJobPlot.getRangeAxis();
        recentJobPlotRangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        XYLineAndShapeRenderer recentJobPlotlineRenderer = (XYLineAndShapeRenderer) recentJobPlot.getRenderer();
        recentJobPlotlineRenderer.setBaseShapesVisible(true);
        recentJobPlotlineRenderer.setBaseShapesFilled(true);
        
        DateAxis recentJobPlotlineDomainaxis = (DateAxis) recentJobPlot.getDomainAxis();
        recentJobPlotlineDomainaxis.setAutoRange(true);
//        recentJobPlotlineDomainaxis.setFixedAutoRange(1000.0 * 3600 * 24 * 1);  // 1 second * 3600 * 24 * 1 = 1 day
        recentJobPlotlineDomainaxis.setDateFormatOverride(new SimpleDateFormat("H,dd-MMM"));

        recentJobPlot.setDataset(1, this.perScenarioTimeseriesCollection[0]);
        NumberAxis recentJobPlotline_rangeAxis2 = new NumberAxis("Percentage");
        recentJobPlotline_rangeAxis2.setAutoRangeIncludesZero(false);
        recentJobPlot.setRenderer(1, new DefaultXYItemRenderer());
        recentJobPlot.setRangeAxis(1, recentJobPlotline_rangeAxis2);
        recentJobPlot.mapDatasetToRangeAxis(1, 1);
        
        recentJobPlot.getRenderer().setSeriesPaint(0, Color.BLACK); // arrival job
        recentJobPlot.getRenderer().setSeriesPaint(1, Color.BLUE); // scheduling job
        recentJobPlot.getRenderer().setSeriesPaint(2, Color.ORANGE); // processing job
        recentJobPlot.getRenderer().setSeriesPaint(3, Color.GREEN); // executed job
        recentJobPlot.getRenderer().setSeriesPaint(4, Color.GRAY); // suspended job
        recentJobPlot.getRenderer().setSeriesPaint(5, Color.RED); // CE
        recentJobPlot.getRenderer().setSeriesPaint(5, Color.MAGENTA); 
        
	}
	
	protected void initDatasetAndChart() {
        
	}

	@Override
	protected void refreshDataset() {
		
	}
	
	@Override
	protected void readScreePlotDataset() {
		
	}
	
	protected void readPrintPlotDataset() {
		
		/*************** RJC_CE Line chart ***************/
		JobCenterManager.plot_getPerScenarioCommunityStatus(perScenarioTimeseries);

	}
	
	private void generateSeries() {
		
		this.perScenarioTimeseries[0] = new TimeSeries("arrival job");
		this.perScenarioTimeseries[1] = new TimeSeries("scheduling job");
		this.perScenarioTimeseries[2] = new TimeSeries("processing job");
		this.perScenarioTimeseries[3] = new TimeSeries("executed job");
		this.perScenarioTimeseries[4] = new TimeSeries("suspended job");
		this.perScenarioTimeseries[5] = new TimeSeries("RJC");
		this.perScenarioTimeseries[6] = new TimeSeries("CE");
		this.perScenarioTimeseries[7] = new TimeSeries("network degree");
		this.perScenarioTimeseries[8] = new TimeSeries("CFC degree");
	}
	

}
