package ch.hefr.gridgroup.magate.plot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class PlotAnimator extends Timer implements ActionListener {

	protected static final Log logger = LogFactory.getLog(PlotAnimator.class);
	private PlotManager plotManager;
	
	public PlotAnimator(PlotManager magateGUI) {
		super(500, null);
		// TODO Auto-generated constructor stub
		
		this.plotManager = magateGUI;
		addActionListener(this);
		
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		try {
			this.plotManager.refresh();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
}
