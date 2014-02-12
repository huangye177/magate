package ch.hefr.gridgroup.magate.plot;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import org.jfree.chart.JFreeChart;

import ch.hefr.gridgroup.magate.env.MaGateProfile;
import ch.hefr.gridgroup.magate.env.MaGateToolkit;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class PDFGenerator {

	/**
	 * Plot PDF chart
	 * 
	 * @param chart
	 * @param fileName
	 * @param width
	 * @param height
	 */
	public static void generatePDFChart(JFreeChart chart, String fileName, int width, int height) {
		
		// write the chart to a PDF file... 
		
        File outputPlotResult = new File(new File(MaGateProfile.chartLocation()), fileName + "-" + UUID.randomUUID().toString() + ".pdf");
        
        try {
			saveChartAsPDF(outputPlotResult, chart, width, height, new DefaultFontMapper());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/**
     * Saves a chart to a PDF file. 
     *  
     * @param file  the file. 
     * @param chart  the chart.
     * @param width  the chart width.
     * @param height  the chart height.
     * @param mapper  the font mapper.
     * 
     * @throws IOException if there is an I/O problem.
     */
    public static void saveChartAsPDF(File file,
                                      JFreeChart chart,
                                      int width,
                                      int height,
                                      FontMapper mapper) throws IOException {
        
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        writeChartAsPDF(out, chart, width, height, mapper);
        out.close();
            
    } 
    
    /**
     * Writes a chart to an output stream in PDF format. 
     *  
     * @param out  the output stream. 
     * @param chart  the chart. 
     * @param width  the chart width.
     * @param height  the chart height.
     * @param mapper  the font mapper.
     * 
     * @throws IOException if there is an I/O problem.
     */
    public static void writeChartAsPDF(OutputStream out,
                                       JFreeChart chart,
                                       int width,
                                       int height,
                                       FontMapper mapper) throws IOException {
    	
        Rectangle pagesize = new Rectangle(width, height);
        Document document = new Document(pagesize, 50, 50, 50, 50);
        
        try {
        	System.out.println("\n Writing PDF based plots...");
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.addAuthor("ye.huang");
            document.addSubject("magate_scheduler_chart");
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(width, height);
            Graphics2D g2 = tp.createGraphics(width, height, mapper);
            Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
            chart.draw(g2, r2D);
            g2.dispose();
            cb.addTemplate(tp, 0, 0);
            System.out.println(" PDF based plots generated.\n");
        } 
        catch (DocumentException de) {
        	System.out.println("\n PDF based plots generation failed.");
            System.err.println(de.getMessage());
        }
        document.close();
    } 
    
}
