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
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.jdbc.JDBCCategoryDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

import ch.hefr.gridgroup.magate.env.JobCenterManager;
import ch.hefr.gridgroup.magate.env.MaGateParam;

public class DynamicCommunityPlotter extends ChartPlotter {

	protected static final Log logger = LogFactory.getLog(DynamicCommunityPlotter.class);
	
	protected JFreeChart rjcCE_lineChart;
	protected JFreeChart schedulingJobCE_lineChart;
	protected JFreeChart recentJobCE_lineChart;
	
	private TimeSeriesCollection[] perIterationTimeseriesCollection; 
	private TimeSeries[] perIterationTimeseries;
	private TimeSeriesCollection[] perScenarioTimeseriesCollection; 
	private TimeSeries[] perScenarioTimeseries;
	
	protected JFreeChart averageNode_histogramChart;
	private JDBCCategoryDataset histogramDataset;
	protected String histogramSql;
	
	protected JPanel layoutPanel;
	
	public DynamicCommunityPlotter(String title) {
		
		super(title);
		
		/*******************************************************************************
		 * array[0]: CE, [1] rjcCE_line, [2] schedulingJobCE_line, [3] recentJobCE_line
		 *******************************************************************************/
		this.perIterationTimeseriesCollection = new TimeSeriesCollection[4];
		this.perIterationTimeseries = new TimeSeries[9];
		/*******************************************************************************
		 * array[0]: CE [1] toPrint_chart
		 *******************************************************************************/
		this.perScenarioTimeseriesCollection = new TimeSeriesCollection[2];
		this.perScenarioTimeseries = new TimeSeries[9];
		
		this.generateSeries();
		for(int i = 0; i < this.perIterationTimeseriesCollection.length; i++) {
			this.perIterationTimeseriesCollection[i] = new TimeSeriesCollection();
		}
		for(int j = 0; j < this.perScenarioTimeseriesCollection.length; j++) {
			this.perScenarioTimeseriesCollection[j] = new TimeSeriesCollection();
		}
		
		this.histogramDataset = new JDBCCategoryDataset(this.chartDBConnection);
		this.refreshSQLStatement();
		
		this.layoutPanel = new JPanel(new GridLayout(2, 2));
		
		// Creating dataset, as well as chart itself
		this.plotScreen();
        
	}
			
	@Override
	protected void plotScreen() {
		
		this.initDatasetAndChart();
		
		/*************** RJC_CE Line chart ***************/
		// Set plot
		this.rjcCE_lineChart.addSubtitle(new TextTitle("number of arrived/executed/suspended jobs, and community efficiency"));
        
        XYPlot xyplot = (XYPlot) this.rjcCE_lineChart.getPlot();
        this.rjcCE_lineChart.setBackgroundPaint(Color.white);
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setRangeGridlinePaint(Color.white);
        
        // change the auto tick unit selection to integer units only...
        NumberAxis xyplotRangeAxis = (NumberAxis) xyplot.getRangeAxis();
        xyplotRangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        XYLineAndShapeRenderer lineRenderer = (XYLineAndShapeRenderer) xyplot.getRenderer();
        lineRenderer.setBaseShapesVisible(true);
        lineRenderer.setBaseShapesFilled(true);
        
        DateAxis lineDomainaxis = (DateAxis) xyplot.getDomainAxis();
        lineDomainaxis.setAutoRange(true);
        lineDomainaxis.setDateFormatOverride(new SimpleDateFormat("H,dd-MMM"));

        xyplot.setDataset(1, this.perIterationTimeseriesCollection[0]);
        NumberAxis line_rangeAxis2 = new NumberAxis("Percentage");
        line_rangeAxis2.setAutoRangeIncludesZero(false);
        xyplot.setRenderer(1, new DefaultXYItemRenderer());
        xyplot.setRangeAxis(1, line_rangeAxis2);
        xyplot.mapDatasetToRangeAxis(1, 1);
        
        xyplot.getRenderer().setSeriesPaint(0, Color.BLACK); // arrival job
        xyplot.getRenderer().setSeriesPaint(1, Color.GREEN); // executed job
        xyplot.getRenderer().setSeriesPaint(2, Color.GRAY); // suspended job
        xyplot.getRenderer().setSeriesPaint(3, Color.RED); // CE
        
        /*************** schedulingJob_CE Line chart ***************/
		// Set plot
		this.schedulingJobCE_lineChart.addSubtitle(new TextTitle("number of scheduling/processing jobs, and community efficiency"));
        
        XYPlot schedulingJobPlot = (XYPlot) this.schedulingJobCE_lineChart.getPlot();
        this.schedulingJobCE_lineChart.setBackgroundPaint(Color.white);
        schedulingJobPlot.setBackgroundPaint(Color.lightGray);
        schedulingJobPlot.setDomainGridlinePaint(Color.white);
        schedulingJobPlot.setRangeGridlinePaint(Color.white);
        
        // change the auto tick unit selection to integer units only...
        NumberAxis schedulingJobPlotRangeAxis = (NumberAxis) schedulingJobPlot.getRangeAxis();
        schedulingJobPlotRangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        XYLineAndShapeRenderer schedulingJobPlotlineRenderer = (XYLineAndShapeRenderer) schedulingJobPlot.getRenderer();
        schedulingJobPlotlineRenderer.setBaseShapesVisible(true);
        schedulingJobPlotlineRenderer.setBaseShapesFilled(true);
        
        DateAxis schedulingJobPlotlineDomainaxis = (DateAxis) schedulingJobPlot.getDomainAxis();
        schedulingJobPlotlineDomainaxis.setAutoRange(true);
        schedulingJobPlotlineDomainaxis.setDateFormatOverride(new SimpleDateFormat("H,dd-MMM"));

        schedulingJobPlot.setDataset(1, this.perIterationTimeseriesCollection[0]);
        NumberAxis schedulingJobPlotline_rangeAxis2 = new NumberAxis("Percentage");
        schedulingJobPlotline_rangeAxis2.setAutoRangeIncludesZero(false);
        schedulingJobPlot.setRenderer(1, new DefaultXYItemRenderer());
        schedulingJobPlot.setRangeAxis(1, schedulingJobPlotline_rangeAxis2);
        schedulingJobPlot.mapDatasetToRangeAxis(1, 1);
        
        schedulingJobPlot.getRenderer().setSeriesPaint(0, Color.BLUE); // scheduling job
        schedulingJobPlot.getRenderer().setSeriesPaint(1, Color.ORANGE); // processing job
        schedulingJobPlot.getRenderer().setSeriesPaint(2, Color.RED); // CE
        
        
        /*************** recentJob_CE Line chart ***************/
		// Set plot
		this.recentJobCE_lineChart.addSubtitle(new TextTitle(MaGateParam.numberOfTotalNode + 
				" nodes and " + MaGateParam.totalNumberOfJob + " jobs in total; " + 
				"Iteration no." + (MaGateParam.currentExperimentIndex + 1)));
        
        XYPlot recentJobPlot = (XYPlot) this.recentJobCE_lineChart.getPlot();
        this.recentJobCE_lineChart.setBackgroundPaint(Color.white);
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
        recentJobPlotlineDomainaxis.setFixedAutoRange(1000.0 * 3600 * 12);  // 1 second * 3600 * 12 = 12 hours
        recentJobPlotlineDomainaxis.setDateFormatOverride(new SimpleDateFormat("H,dd-MMM"));

        recentJobPlot.setDataset(1, this.perIterationTimeseriesCollection[0]);
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
        
        /*************** Node-Job Histogram chart ***************/
        
//        int avgJobPerNode = (int) MaGateParam.totalNumberOfJob / MaGateParam.numberOfTotalNode;
        this.averageNode_histogramChart.addSubtitle(new TextTitle(MaGateParam.totalNumberOfJob + " jobs per node; " + 
				"Iteration no." + (MaGateParam.currentExperimentIndex + 1)));
        CategoryPlot categoryplot = this.averageNode_histogramChart.getCategoryPlot();
        
        this.averageNode_histogramChart.setBackgroundPaint(Color.white);
        categoryplot.setBackgroundPaint(Color.lightGray);
        categoryplot.setDomainGridlinePaint(Color.white);
        categoryplot.setRangeGridlinePaint(Color.white);
        
		BarRenderer histRenderer = (BarRenderer) categoryplot.getRenderer();
		histRenderer.setBaseItemLabelsVisible(true);
		histRenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        
        double tick = MaGateParam.totalNumberOfJob/10;
        NumberAxis hist_rangeAxis = (NumberAxis) categoryplot.getRangeAxis();
        hist_rangeAxis.setAutoRange(false);
        hist_rangeAxis.setRange(0, MaGateParam.totalNumberOfJob);
        hist_rangeAxis.setTickUnit(new NumberTickUnit(tick));
        
        NumberAxis hist_rangeAxis2 = new NumberAxis("Percentage");
        hist_rangeAxis2.setAutoRange(false);
        hist_rangeAxis2.setRange(0, 100);
        hist_rangeAxis2.setTickUnit(new NumberTickUnit(10));
        categoryplot.setRangeAxis(1, hist_rangeAxis2);
        
        categoryplot.getRenderer().setSeriesPaint(0, Color.BLACK); // arrival job
        categoryplot.getRenderer().setSeriesPaint(1, Color.BLUE); // scheduling job
        categoryplot.getRenderer().setSeriesPaint(2, Color.ORANGE); // processing job
        categoryplot.getRenderer().setSeriesPaint(3, Color.GREEN); // executed job
        categoryplot.getRenderer().setSeriesPaint(4, Color.GRAY); // suspended job
        
//        categoryplot.getRenderer().setSeriesPaint(0, Color.YELLOW); // arrived_job job
//        categoryplot.getRenderer().setSeriesPaint(1, Color.CYAN); // submitted job
//        categoryplot.getRenderer().setSeriesPaint(2, Color.BLUE); // scheduling job
//        categoryplot.getRenderer().setSeriesPaint(3, Color.ORANGE); // processing job
//        categoryplot.getRenderer().setSeriesPaint(4, Color.MAGENTA); // transferring job
//        categoryplot.getRenderer().setSeriesPaint(5, Color.GREEN); // executed job
//        categoryplot.getRenderer().setSeriesPaint(6, Color.GRAY); // suspended job
//        categoryplot.getRenderer().setSeriesPaint(7, Color.DARK_GRAY); // failed job
        
        /*************** Panel ***************/
        
        // Set Panel
        ChartPanel rjcCE_linePanel = new ChartPanel(this.rjcCE_lineChart);
        rjcCE_linePanel.setMouseZoomable(true, false);
        ChartPanel schedulingJobCE_linePanel = new ChartPanel(this.schedulingJobCE_lineChart);
        rjcCE_linePanel.setMouseZoomable(true, false);
        ChartPanel recentJobCE_linePanel = new ChartPanel(this.recentJobCE_lineChart);
        rjcCE_linePanel.setMouseZoomable(true, false);
        ChartPanel histogramPanel = new ChartPanel(this.averageNode_histogramChart);
        histogramPanel.setMouseZoomable(true, false);
        this.layoutPanel.add(rjcCE_linePanel);
        this.layoutPanel.add(schedulingJobCE_linePanel);
        this.layoutPanel.add(recentJobCE_linePanel);
        this.layoutPanel.add(histogramPanel);
        
//        this.setTitle("Dynamic Community Status [in total: " + MaGateParam.numberOfTotalNode + 
//    			" nodes, " + MaGateParam.totalNumberOfJob + " jobs; iteration no." + (MaGateParam.currentExperimentIndex + 1) + "]");
        this.setTitle("Community Status (" + MaGateParam.currentIterationId + ")");
        this.layoutPanel.setPreferredSize(new Dimension(1200, 800));
        
        setContentPane(this.layoutPanel);
        
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
		
		this.printChart = ChartFactory.createTimeSeriesChart(
			"Community Status Tracing Overview", // chart title
            "Date/Time",
            "Num. of jobs",
            this.perScenarioTimeseriesCollection[1],                     
            true,                       // include legend
            true,
            false
        );
		
//		this.printChart.addSubtitle(new TextTitle(MaGateParam.numberOfTotalNode + 
//				" nodes and " + MaGateParam.totalNumberOfJob + " jobs in total"));
		this.printChart.addSubtitle(new TextTitle(MaGateParam.currentIterationId));
        
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
        
	}
	
	protected void initDatasetAndChart() {
		
		// read the data from the database...
		this.readScreePlotDataset();
		
		/*************** CE for all Line charts ***************/
		this.perIterationTimeseriesCollection[0].addSeries(perIterationTimeseries[6]);  // ce
		
		/*************** RJC_CE Line chart ***************/
        
		this.perIterationTimeseriesCollection[1].addSeries(perIterationTimeseries[0]);  // arrived_job
		this.perIterationTimeseriesCollection[1].addSeries(perIterationTimeseries[3]);  // executed_job
		this.perIterationTimeseriesCollection[1].addSeries(perIterationTimeseries[4]);  // suspended_job
        
		this.rjcCE_lineChart = ChartFactory.createTimeSeriesChart(
			"Overview of Job Arrival and Processed Status", // chart title
            "Date/Time",
            "Num. of jobs",
            this.perIterationTimeseriesCollection[1],                     
            true,                       // include legend
            true,
            false
        );
		
		/*************** schedulingJob_CE Line chart ***************/
        
		this.perIterationTimeseriesCollection[2].addSeries(perIterationTimeseries[1]);  // scheduling_job
		this.perIterationTimeseriesCollection[2].addSeries(perIterationTimeseries[2]);  // processing_job
		
		this.schedulingJobCE_lineChart = ChartFactory.createTimeSeriesChart(
			"Overview of Job Scheduling and Processing Status", // chart title
            "Date/Time",
            "Num. of jobs",
            this.perIterationTimeseriesCollection[2],                     
            true,                       // include legend
            true,
            false
        );
		
		/*************** recentJob_CE Line chart ***************/
        
		this.perIterationTimeseriesCollection[3].addSeries(perIterationTimeseries[0]);  // arrived_job
		this.perIterationTimeseriesCollection[3].addSeries(perIterationTimeseries[1]);  // scheduling_job
		this.perIterationTimeseriesCollection[3].addSeries(perIterationTimeseries[2]);  // processing_job
		this.perIterationTimeseriesCollection[3].addSeries(perIterationTimeseries[3]);  // executed_job
		this.perIterationTimeseriesCollection[3].addSeries(perIterationTimeseries[4]);  // suspended_job
		
		this.recentJobCE_lineChart = ChartFactory.createTimeSeriesChart(
			"Community Status Overview in Recent 24 Hours", // chart title
            "Date/Time",
            "Num. of jobs",
            this.perIterationTimeseriesCollection[3],                     
            true,                       // include legend
            true,
            false
        );
		
		/*************** Average Node Histogram chart ***************/
		
		// create the chart...
		this.averageNode_histogramChart = ChartFactory.createBarChart(
            "Overview of Job Status Changes", // chart title
            "Scenarios",
            "Num. of jobs",
            this.histogramDataset,                       // data
            PlotOrientation.VERTICAL,
//            PlotOrientation.HORIZONTAL,
            true,                       // include legend
            true,
            false
        );
        
	}

	@Override
	protected void refreshDataset() {
		// TODO Auto-generated method stub
		try {
			
			this.refreshSQLStatement();
			this.readScreePlotDataset();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	@Override
	protected void readScreePlotDataset() {
		
		/*************** RJC_CE Line chart ***************/
		JobCenterManager.plot_getPerIterationCommunityStatus(perIterationTimeseries);
		
		/*************** Average Node Histogram chart ***************/
		try {
            
            this.histogramDataset.executeQuery(histogramSql);
            
        } catch (SQLException e) {
            System.err.print("SQLException: ");
            System.err.println(e.getMessage());
            
        } catch (Exception e) {
            System.err.print("Exception: ");
            System.err.println(e.getMessage());
            
        }

	}
	
	protected void readPrintPlotDataset() {
		
		/*************** RJC_CE Line chart ***************/
		JobCenterManager.plot_getPerIterationCommunityStatus(perScenarioTimeseries);
//		JobCenterManager.plot_getPerScenarioCommunityStatus(perScenarioTimeseries);

	}
	
	private void generateSeries() {
		
		this.perIterationTimeseries[0] = new TimeSeries("arrival job");
		this.perIterationTimeseries[1] = new TimeSeries("scheduling job");
		this.perIterationTimeseries[2] = new TimeSeries("processing job");
		this.perIterationTimeseries[3] = new TimeSeries("executed job");
		this.perIterationTimeseries[4] = new TimeSeries("suspended job");
		this.perIterationTimeseries[5] = new TimeSeries("RJC");
		this.perIterationTimeseries[6] = new TimeSeries("CE");
		this.perIterationTimeseries[7] = new TimeSeries("network degree");
		this.perIterationTimeseries[8] = new TimeSeries("CFC degree");
		
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
	
	private void refreshSQLStatement() {
		
		this.histogramSql = "SELECT scenario_id, " +
			"generated_job, " +
			"scheduling_job, " +
			"processing_job, " +
			"executed_job, " +
			"suspended_job, " +
			"failed_job " +
			"FROM community_status " +
			" WHERE scenario_id = '" + MaGateParam.currentScenarioId + "' " + 
			" AND exp_iteration = '" + MaGateParam.currentExperimentIndex + "' " +
			" GROUP BY scenario_id; ";
		
//		this.histogramSql = "SELECT scenario_id, " +
//		"generated_job, " +
//		"submitted_job, " +
//		"scheduling_job, " +
//		"processing_job, " +
//		"transferred_job, " +
//		"executed_job, " +
//		"suspended_job, " +
//		"failed_job " +
//		"FROM community_status " +
//		" WHERE scenario_id = '" + MaGateParam.currentScenarioId + "' " + 
//		" AND exp_iteration = '" + MaGateParam.currentExperimentIndex + "' " +
//		" GROUP BY scenario_id; ";
		

	}

}
