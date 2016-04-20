package il.ac.shenkar.finalproject;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.table.JTableHeader;

import org.apache.log4j.Logger;


/**
 * GUI class. Includes all of the GUI components.
 *
 */
public class ProjectGUI
{
	static Logger logger = Logger.getLogger("");			// log4j component.
	private JFrame frame;
	private JPanel north; 
	private JPanel center;
	private JPanel south;
	private JPanel southLeft;
	private JPanel southRight;
	private JLabel label, lastUpdate;
	private JTextField amount, result;
	@SuppressWarnings("rawtypes")
	private JComboBox currencySelect, currencySelect2;
	private String[] currencies = {"Select Currency", "NIS", "USD", "GBP", "JPY", "EMU", "AUD", "CAD",
			"DKK", "NOK", "ZAR", "SEK", "CHF", "JOD", "LBP", "EGP"};
	private double[] rates;
	private JScrollPane scroll;
	private DecimalFormat numFormat; 
	private Double finalVal;
	public XMLParse xml;
	public JTable table;
	public JTextField lastUpdateText;
	public double amountVal;
	public double resultVal;
	public Date rightNow;
	public DateFormat time;
	public String timeOutput;

	
	/*
	 * GUI class constructor.
	 * Initialize all the components from the "ProjectGUI" class.
	 */
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ProjectGUI()
	{			
		logger.info("Program starting");
		xml = new XMLParse();
		xml.update(ProjectGUI.logger);		
		rightNow = new Date();
		time = DateFormat.getTimeInstance(DateFormat.DEFAULT);
		timeOutput = time.format(rightNow);							//gets the exact time.
		frame = new JFrame("Currency Converter");
		north = new JPanel();
		center = new JPanel();
		south = new JPanel();
		southLeft = new JPanel();
		southRight = new JPanel();
		label = new JLabel("CURRENCIES");
		table = new JTable(xml.getRows(), xml.getColumns());
		table.setPreferredScrollableViewportSize(new Dimension(450,224));
		table.setFillsViewportHeight(true);
		scroll = new JScrollPane(table);
		amount = new JTextField("0", 15);
		result = new JTextField("0", 15);
		lastUpdate = new JLabel("Last Update: ");
		lastUpdateText = new JTextField(xml.getLastupdate() + "  |  " + timeOutput);
		currencySelect = new JComboBox(currencies);
		currencySelect2 = new JComboBox(currencies);
		rates = new double[14];	
		numFormat = new DecimalFormat();
		numFormat.setMaximumFractionDigits(4);						//formats the number to 4 digits after the decimal point.
		JTableHeader header = table.getTableHeader();
		header.setFont(new Font("Ariel", Font.ITALIC, 12));
	    header.setBackground(Color.BLACK);
	    header.setForeground(Color.GREEN);
	    
		for(int x=0; x<14;x++)
		{
			rates[x] = Double.parseDouble((String) table.getModel().getValueAt(x, 4));		// saves all the rates from the xml in an array.
		}
		
		
		/*
		 * Left JComboBox listener + calculation of selected currency.
		 * 
		 */
		currencySelect.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) 
			{
				currencySelect2.setSelectedIndex(0);
				result.setText("0");
				amountVal = Double.parseDouble(amount.getText());
				String curSelected = (String)currencySelect.getSelectedItem();
				logger.info("From: " + curSelected + "  " + "Amount: " + amountVal);
				for(int val=1; val<15; val++)
				{
					if(curSelected == currencies[val])
					{ 
						if(curSelected == "NIS")				//if from "NIS" leave the amount as is.
						{
							resultVal = amountVal;
							break;
						}
						else
						{
							resultVal = amountVal * rates[val-2];		//else calculate the amount with the selected rate.
						}
						
					}	
				}		
			}
	    });
		
		/*
		 * Right JComboBox listener + calculation of selected currency
		 */
		currencySelect2.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) 
			{		
				String curSelected2 = (String)currencySelect2.getSelectedItem();
				for(int val=1; val<15; val++)
				{
					if(curSelected2 == currencies[val])
					{
						if(curSelected2 == "NIS")
						{
							result.setText(numFormat.format(resultVal));		//if to "NIS", show the amount without further calculation.
							finalVal = resultVal;
						}
						
						else
						{
							finalVal = resultVal / rates[val -2];			//else, calculate the amount by multiplying the the amount with the selected rate.
							if(curSelected2 == "JPY"){
								finalVal *= 100;
							}
							if(curSelected2 == "LBP"){
								finalVal *= 10;
							}
							result.setText(numFormat.format(finalVal));
						}
						
					}
					
				}	
				logger.info("To: " + curSelected2 + "  " + "Result: " + numFormat.format(finalVal));
			}
	    });
	}
	
	/*
	 * The function that starts the program...
	 * Adds all the components that were initialized in the constructor to the frame
	 * and make them appear on the screen.
	 */
	public void go()
	{	
		frame.setLayout(new BorderLayout());
		north.add(label);
		frame.add(north, BorderLayout.NORTH);
		center.setLayout(new FlowLayout());
		center.add(scroll);
		center.add(lastUpdate);
		center.add(lastUpdateText);
		frame.add(center, BorderLayout.CENTER);
		southLeft.setLayout(new BorderLayout(4, 1));
		southLeft.add(amount, BorderLayout.NORTH);
		southLeft.add(currencySelect, BorderLayout.CENTER);
		southLeft.setBorder(BorderFactory.createTitledBorder("FROM:"));
		southLeft.setSize(200, 100);
		southRight.setLayout(new BorderLayout(4, 1));
		southRight.add(result, BorderLayout.NORTH);
		southRight.add(currencySelect2, BorderLayout.CENTER);
		southRight.setBorder(BorderFactory.createTitledBorder("TO:"));
		southRight.setSize(200, 100);
		south.add(southLeft);
		south.add(southRight);
		frame.add(south, BorderLayout.SOUTH);
		frame.setSize(500, 450);
		frame.setVisible(true);	
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		
		logger.info("Successfully built GUI");
		
		UpdateThread up = new UpdateThread();								// the separate thread that updates the table every 10 minutes.
  		up.run(table, lastUpdateText, ProjectGUI.logger);
	}
	
	/*
	 * main function.
	 * Creates a new instance of ProjectGUi class and starts the go() function.
	 */
	public static void main(String[] args)
	{
		ProjectGUI prog = new ProjectGUI();
		prog.go();
	}
}
