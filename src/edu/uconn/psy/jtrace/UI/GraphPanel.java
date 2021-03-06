/*
 * GraphPanel.java
 *
 * Created on May 9, 2005, 6:46 PM
 */

package edu.uconn.psy.jtrace.UI;

import edu.uconn.psy.jtrace.Model.*;
import edu.uconn.psy.jtrace.UI.GraphParameters;
import org.jfree.chart.*;
import org.jfree.data.*;
import org.jfree.data.xy.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.title.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.annotations.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author  harlan
 */
public class GraphPanel extends javax.swing.JPanel {
    // data object
    TraceSim sim;
    
    // our objects
    TraceSimAnalysis analysis;
    GraphParameters graphParams;
    XYSeriesCollection dataset;

    // graph object
    JFreeChart graph;
    
    // fonts
    static Font fTitle;
    static Font fAxisLabel;
    static Font fTickLabel;
    static Font fAnnotation;
    static Font fLegend;
        
    /**
     * Constructor with specified analyis and parameters.
     */
    public GraphPanel(TraceSim _sim,TraceSimAnalysis anal,GraphParameters gp) 
    {
        sim = _sim;
        analysis=anal;
        graphParams=gp;
        
        // set up fonts
        setupFonts();
        
        // init the chart's data
        dataset = analysis.doAnalysis(sim);
        
        // create the graph itself
        graph = createJTRACEChart(dataset, graphParams);        
        
        // and annotate it
        graph = annotateJTRACEChart(graph, graphParams, sim.getParameters());
        
        // this has to come last so that the chart's arguments are ready
        initComponents();
        
        // and set those components correctly
        guiFromGraphParams();
        // also, load the unselected list with the right stuff
        
        initHints();
    }
    /** Constructor with default analysis and parameters. */
    public GraphPanel(TraceSim _sim) 
    {
        this(_sim, new TraceSimAnalysis(), new GraphParameters());
        
    }
    
    public void setupFonts()
    {
        fAxisLabel = new Font("SansSerif", Font.BOLD, 14);
        fTickLabel = new Font("SansSerif", Font.BOLD, 10);
        fTitle = new Font("SansSerif", Font.BOLD, 18);
        fAnnotation = new Font("SansSerif", Font.BOLD, 16);
        fLegend = new Font("SansSerif", Font.BOLD, 16);
    }
    
    /**
     * Add the inputs to the specified chart. If position is pegged high or low,
     * don't display anything.
     *
     * @param chart     chart to annotate
     * @param gps       GraphParameters
     * @param param     TraceParam
     */
    public static JFreeChart annotateJTRACEChart(JFreeChart chart, 
            GraphParameters gps, TraceParam param)
    {
        XYPlot plot = (XYPlot)chart.getPlot();
        
        // reset annotations
        plot.clearAnnotations();
        
        if (gps.getInputPosition() == 0 || gps.getInputPosition() == 100)
            return chart;
        
        // get phones of input
        //char [] inputChars = param.getModelInput().toCharArray();
        
        // figure out initial X position and interval of characters
        int initX = param.getMaxSpread(); 
        int intX = param.getDeltaInput();
        
        // figure out where on Y axis to display labels
        Range yRange = plot.getRangeAxis().getRange();
        double yRelPos = gps.getInputPosition() / 100f;
        double yPos = (yRange.getUpperBound() - yRange.getLowerBound()) * yRelPos +
                yRange.getLowerBound();
        
        // figure out if a vertical rule is plotted
        //chart.getPlot().
        
        // and add the character
        for (int c = 0, c_incr = 0; c < param.inputLength(); c++)
        {
            XYTextAnnotation annote;
            if(param.getModelInput().charAt(c + c_incr)=='{'){
                annote = new XYTextAnnotation(param.getModelInput().substring(c + c_incr, c + c_incr + 5), (double)(initX + (intX * c)), yPos);
                c_incr += 4;
            }
            else{
                annote = new XYTextAnnotation(param.getModelInput().substring(c + c_incr, c + c_incr + 1), (double)(initX + (intX * c)), yPos);
            }
                    
            if (fAnnotation != null)    // static method, so we can't assume it's been initialized
                annote.setFont(fAnnotation);
            if(gps.getIncludeInputAnnotation())
                plot.addAnnotation(annote);
        }
        return chart;
    }
    
    /**
     * A static method (call from scripting, for example) for creating a 
     * JFreeChart from the results of an analysis and a parameters object. Builds
     * the chart and sets some visual parameters.
     *
     * @param data      data to plot
     * @param gp        parameters to the chart
     */
    public static JFreeChart createJTRACEChart(XYSeriesCollection data,
            GraphParameters gp)
    {
        JFreeChart chart;
        
        chart = ChartFactory.createXYLineChart(gp.getGraphTitle(),
            gp.getXLabel(), gp.getYLabel(),
            data, PlotOrientation.VERTICAL,
            true,       // legend
            true,       // tooltips
            false);     // URLs
        
        // set basic color issues
        chart.setBackgroundPaint(Color.white);
        
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.lightGray);
        
        // make lines wider
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
        renderer.setStroke(new BasicStroke(2f));    
        
        // set axis settings
        NumberAxis domain = (NumberAxis)plot.getDomainAxis();
        domain.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        domain.setAutoRange(true);
        if (fAxisLabel != null)
        {
            domain.setLabelFont(fAxisLabel);
            domain.setTickLabelFont(fTickLabel);
        }
        
        NumberAxis range = (NumberAxis)plot.getRangeAxis();
        //range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        range.setAutoRange(true);
        if (fAxisLabel != null)
        {
            range.setLabelFont(fAxisLabel);
            range.setTickLabelFont(fTickLabel);
        }
     
        // set legend settings
        // NB: legends in jfreechart-1.0pre2 is broken and will change in the
        // release version! This workaround will be replaceable with just a
        // getLegend() call.
        ArrayList subtitles = (ArrayList) chart.getSubtitles();
        for (Iterator iter = subtitles.iterator();iter.hasNext();)
        {
            Title t = (Title) iter.next();
            if (t instanceof LegendTitle)
            {
                LegendTitle legend = (LegendTitle) t;
                legend.setPosition(org.jfree.ui.RectangleEdge.RIGHT); //Set position
                // this doesn't work in pre2! will be fixed in release.
//                if (fLegend != null)
//                    legend.setItemFont(fLegend);
                
                break;
            }
        }
                
        return chart;
    }
    
    
    /**
     * Puts all items in the lexicon or phoneme set into the left (unselected) list.
     * Resets the selected list too.
     */
    public void loadSelectionListsFromParams()
    {
        String [] items;
        
        // reset unselected list
        
        if (wordsRadioButton.isSelected())
        {
            // add all words
            items = sim.getParameters().getLexicon().toStringArray();                        
        }
        else
        {
            // add all phonemes
            items = sim.getParameters().getPhonology().getLabels();
        }
        
        DefaultListModel listModel = (DefaultListModel)unselectedList.getModel();

        listModel.clear();
        for (int i = 0; i < items.length; i++)
            listModel.addElement(items[i]);
        
        // reset selected list
        ((DefaultListModel)selectedList.getModel()).clear();
    }
    
    /**
     * Call to re-do the analysis, and re-create the graph with the new series'.
     * 
     */
    public void updateGraph() 
    {                        
        // update the graph params
        graphParamsFromGui();
        
        // ...
        Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);        
        javax.swing.RootPaneContainer root =((javax.swing.RootPaneContainer) this.getTopLevelAncestor());
        if(root!=null&&root.getGlassPane()!=null){
            root.getGlassPane().setCursor(WAIT_CURSOR);
            root.getGlassPane().setVisible(true);
        }
        
        // update the dataset
        XYPlot plot = (XYPlot)graph.getPlot();        
        dataset = analysis.doAnalysis(sim);        // keep analysis, so convenient to export
        plot.setDataset(dataset);
        
        // update the chrome
        graph.setTitle(new TextTitle(graphParams.getGraphTitle(), fTitle));
        NumberAxis domain = (NumberAxis)plot.getDomainAxis();
        NumberAxis range = (NumberAxis)plot.getRangeAxis();
        domain.setLabel(graphParams.getXLabel());
        if(this.analysisContentComboBox.getSelectedIndex()==0)
            range.setLabel("Activation strength");
        else if(this.analysisContentComboBox.getSelectedIndex()==1)
            range.setLabel("Response probability");
        else 
            range.setLabel("Competition index");
        // update the annotations
        annotateJTRACEChart(graph, graphParams, sim.getParameters());
        
        if(root!=null&&root.getGlassPane()!=null){
            root.getGlassPane().setCursor(DEFAULT_CURSOR);
            root.getGlassPane().setVisible(false);                
        }        
    }
    public void updateGraph(TraceSimAnalysis anal) 
    {
        analysis=anal;
        
        updateGraph();
        
    }
    
    /**
     * Get stuff from the gui and put it into our GraphParameters object.
     */
    public void graphParamsFromGui() 
    {
        if (xLabelTextField != null)    // make sure we're initialized
        {
            // display tab:
            // get strings
            graphParams.setXLabel(xLabelTextField.getText());
            graphParams.setYLabel(yLabelTextField.getText());
            graphParams.setGraphTitle(titleTextField.getText());
            // get slider
            graphParams.setInputPosition(inputPositionSlider.getValue());
            
            // analysis tab:
            // analyzeRadioButtons            
            analysis.setDomain(wordsRadioButton.isSelected() ? TraceSimAnalysis.WORDS : TraceSimAnalysis.PHONEMES);
            // contentRadioButtons
            if (analysisContentComboBox.getSelectedIndex()==0)
                analysis.setContentType(TraceSimAnalysis.ACTIVATIONS);
            else if (analysisContentComboBox.getSelectedIndex()==1)
                analysis.setContentType(TraceSimAnalysis.RESPONSE_PROBABILITIES);
            else if (analysisContentComboBox.getSelectedIndex()==2)
                analysis.setContentType(TraceSimAnalysis.COMPETITION_INDEX);
                        
            // items box
            analysis.setWatchType(topNRadioButton.isSelected() ? TraceSimAnalysis.WATCHTOPN : TraceSimAnalysis.WATCHSPECIFIED);
            DefaultListModel listModel = (DefaultListModel)selectedList.getModel();
            Object [] listArray = listModel.toArray();
            analysis.setItemsToWatch(new String[0]);
            for (int i = 0; i < listArray.length; i++)
                analysis.addItemToWatchList((String)listArray[i]);
            analysis.setTopN(Integer.parseInt(topNTextField.getText()));
            // alignmentRadioButtons
            if (averageRadioButton.isSelected())
                analysis.setCalculationType(TraceSimAnalysis.AVERAGE);
            else if (maxAdhocRadioButton.isSelected())
                analysis.setCalculationType(TraceSimAnalysis.MAX_ADHOC);
            else if (maxAdhoc2RadioButton.isSelected())
                analysis.setCalculationType(TraceSimAnalysis.MAX_ADHOC_2);
            else if (maxPosthocRadioButton.isSelected())
                analysis.setCalculationType(TraceSimAnalysis.MAX_POSTHOC);
            else if (specifiedRadioButton.isSelected())
                analysis.setCalculationType(TraceSimAnalysis.STATIC);                            
            else if (fraunfelderRadioButton.isSelected())
                analysis.setCalculationType(TraceSimAnalysis.FRAUENFELDER);
            try
            {
                analysis.setAlignment(Integer.parseInt(specifiedTextField.getText()));
            }
            catch (java.lang.NumberFormatException ex) {}
            // LCR box
            analysis.setChoice(allItemsRadioButton.isSelected() ? TraceSimAnalysis.NORMAL : TraceSimAnalysis.FORCED);
            analysis.setKValue(((Number)kValueSpinner.getValue()).intValue());
            
            analysis.setCompetIndexType(competitionIndexComboBox.getSelectedIndex());
            analysis.setCompetIndexSlope(((Number)this.slopeValueSpinner.getValue()).intValue());
        }
    }
    
    /**
     * Set the values of GUI objects with the specified parameters object.
     */
    public void guiFromGraphParams()
    {
        // display tab:
        // text stuff
        titleTextField.setText(graphParams.getGraphTitle());
        xLabelTextField.setText(graphParams.getXLabel());
        yLabelTextField.setText(graphParams.getYLabel());
        // slider
        inputPositionSlider.setValue(graphParams.getInputPosition());
        
        // analysis tab:
        // analyzeRadioButtons
        if (analysis.getDomain() == TraceSimAnalysis.WORDS)
            wordsRadioButton.setSelected(true);
        else
            phonemesRadioButton.setSelected(true);
        // contentRadioButtons
        if (analysis.getContentType() == TraceSimAnalysis.ACTIVATIONS)
            analysisContentComboBox.setSelectedIndex(0);            
        else if (analysis.getContentType() == TraceSimAnalysis.RESPONSE_PROBABILITIES)
            analysisContentComboBox.setSelectedIndex(1);            
        else
            analysisContentComboBox.setSelectedIndex(2);            
        
        if (analysis.getWatchType() == TraceSimAnalysis.WATCHTOPN)
            topNRadioButton.setSelected(true);
        else
            specifiedItemsRadioButton.setSelected(true);
        topNTextField.setText(Integer.toString(analysis.getTopN()));
        
        // items box
        loadSelectionListsFromParams();
        DefaultListModel listModel = (DefaultListModel)selectedList.getModel();        
        DefaultListModel unselectlistModel = (DefaultListModel)unselectedList.getModel();
        
        listModel.clear();
        Vector watchItems = analysis.getItemsToWatch();
        if (watchItems.size() > 0)
        {
            Object [] itemsList = (Object [])watchItems.toArray();
            boolean exists = true;
            for (int i = 0; i < itemsList.length; i++){                
                //exists = unselectlistModel.removeElement((String)itemsList[i]); //@@@ uncomment again when you resolve ^ vs. x !
                if(exists)
                    listModel.addElement((String)itemsList[i]);                                
            }
        }        
        selectedList.setModel(listModel);
        unselectedList.setModel(unselectlistModel);        
        
        // alignmentRadioButtons
        switch (analysis.getCalculationType())
        {
            case TraceSimAnalysis.AVERAGE:
                averageRadioButton.setSelected(true);
                break;
            case TraceSimAnalysis.MAX_ADHOC:
                maxAdhocRadioButton.setSelected(true);
                break;
            case TraceSimAnalysis.MAX_ADHOC_2:
                maxAdhoc2RadioButton.setSelected(true);
                break;
            case TraceSimAnalysis.MAX_POSTHOC:
                maxPosthocRadioButton.setSelected(true);
                break;
            case TraceSimAnalysis.STATIC:
                specifiedRadioButton.setSelected(true);
                break;
            case TraceSimAnalysis.FRAUENFELDER:
                fraunfelderRadioButton.setSelected(true);
                break;
        }
        specifiedTextField.setText(Integer.toString(analysis.getAlignment()));
        // LCR box
        if (analysis.getChoice() == TraceSimAnalysis.NORMAL)
            allItemsRadioButton.setSelected(true);
        else
            forcedChoiceRadioButton.setSelected(true);
        kValueSpinner.setValue(new Integer(analysis.getKValue()));
        
    }
    
    public void initHints() 
    {
        MouseOverHintManager hintManager = jTRACEMDI.hintManager;
        
        // buttons on the bottom of the screen
        hintManager.addHintFor(updateGraphButton, "Update graph with current data and settings");
        hintManager.addHintFor(exportGraphButton, "Export graph data to text file");
        hintManager.addHintFor(saveImageButton, "Save image to PNG file");
       
        // display tab objects
        hintManager.addHintFor(inputPositionLabel, "Vertical position of model input on graph");
        hintManager.addHintFor(inputPositionSlider, "Vertical position of model input on graph");
        hintManager.addHintFor(xLabelLabel, "X-axis label");
        hintManager.addHintFor(xLabelTextField, "X-axis label");
        hintManager.addHintFor(yLabelLabel, "Y-axis label");
        hintManager.addHintFor(yLabelTextField, "Y-axis label");
        hintManager.addHintFor(titleLabel, "Graph title");
        hintManager.addHintFor(titleTextField, "Graph title");
       
        // analysis tab objects
        // analyze
        hintManager.addHintFor(wordsRadioButton, "Analyze word-level activation");
        hintManager.addHintFor(phonemesRadioButton, "Analyze phoneme-level activation");
        // content
        // items
        hintManager.addHintFor(unselectedList, "Available items to display");
        hintManager.addHintFor(unselectedScrollPane, "Available items to display");
        hintManager.addHintFor(selectedList, "Items selected to display");
        hintManager.addHintFor(selectedScrollPane, "Items selected to display");
        hintManager.addHintFor(selectButton, "Move selected items in left list to right list");
        hintManager.addHintFor(unselectButton, "Move selected items in right list to left list");
        hintManager.addHintFor(allButton, "Move all items in left list to right list");
        hintManager.addHintFor(topNRadioButton, "Automatically display top N most active items");
        hintManager.addHintFor(topNTextField, "Number of items to display");
        hintManager.addHintFor(specifiedItemsRadioButton, "Display specified items");
        hintManager.addHintFor(resetButton, "Unselect all items");
        // alignment
        hintManager.addHintFor(averageRadioButton, "Use each item's average activation over entire simulation");
        hintManager.addHintFor(maxAdhocRadioButton, "Use maximum alignment, for each step of simulation");
        hintManager.addHintFor(maxAdhoc2RadioButton, "Use maximum alignment, for each step of simulation");
        hintManager.addHintFor(maxPosthocRadioButton, "Use maximum alignment, computed globally for the whole simulation");
        hintManager.addHintFor(specifiedRadioButton, "Use each item's activation at the specified time slice");
        hintManager.addHintFor(fraunfelderRadioButton, "Use max of each item's activation at the specified and subsequent time slices");
        hintManager.addHintFor(specifiedTextField, "Time slice for alignment");
        // luce choice
        hintManager.addHintFor(allItemsRadioButton, "Use all items as possible responses for response probabilities");
        hintManager.addHintFor(forcedChoiceRadioButton, "Use only specified items as possible responses for response probabilities");
        hintManager.addHintFor(kValueSpinner, "Exponent in Luce-choice rule");
        hintManager.addHintFor(kValueLabel, "Exponent in Luce-choice rule");
        
        hintManager.enableHints(this);   
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        wordsPhonemesButtonGroup = new javax.swing.ButtonGroup();
        alignmentButtonGroup = new javax.swing.ButtonGroup();
        choiceButtonGroup = new javax.swing.ButtonGroup();
        contentButtonGroup = new javax.swing.ButtonGroup();
        itemsButtonGroup = new javax.swing.ButtonGroup();
        buttonPanel = new javax.swing.JPanel();
        updateGraphButton = new javax.swing.JButton();
        saveImageButton = new javax.swing.JButton();
        exportGraphButton = new javax.swing.JButton();
        graphSplitPane = new javax.swing.JSplitPane();
        graphTabbedPane = new javax.swing.JTabbedPane();
        displayTabPanel = new javax.swing.JPanel();
        xLabelTextField = new javax.swing.JTextField();
        xLabelLabel = new javax.swing.JLabel();
        yLabelLabel = new javax.swing.JLabel();
        yLabelTextField = new javax.swing.JTextField();
        titleLabel = new javax.swing.JLabel();
        titleTextField = new javax.swing.JTextField();
        inputPositionSlider = new javax.swing.JSlider();
        inputPositionLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        analysisTabPanel = new javax.swing.JPanel();
        analysisTabScrollPane = new javax.swing.JScrollPane();
        analysisTabScrollPanel = new javax.swing.JPanel();
        analyzePanel = new javax.swing.JPanel();
        wordsRadioButton = new javax.swing.JRadioButton();
        phonemesRadioButton = new javax.swing.JRadioButton();
        itemsPanel = new javax.swing.JPanel();
        selectionPanel = new javax.swing.JPanel();
        allButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        unselectedScrollPane = new javax.swing.JScrollPane();
        unselectedList = new javax.swing.JList();
        selectedScrollPane = new javax.swing.JScrollPane();
        selectedList = new javax.swing.JList();
        selectButton = new javax.swing.JButton();
        unselectButton = new javax.swing.JButton();
        itemsRadioButtonPanel = new javax.swing.JPanel();
        topNRadioButton = new javax.swing.JRadioButton();
        topNTextField = new javax.swing.JTextField();
        specifiedItemsRadioButton = new javax.swing.JRadioButton();
        alignmentPanel = new javax.swing.JPanel();
        averageRadioButton = new javax.swing.JRadioButton();
        maxAdhocRadioButton = new javax.swing.JRadioButton();
        maxAdhoc2RadioButton = new javax.swing.JRadioButton();
        specifiedRadioButton = new javax.swing.JRadioButton();
        specifiedTextField = new javax.swing.JTextField();
        fraunfelderRadioButton = new javax.swing.JRadioButton();
        maxPosthocRadioButton = new javax.swing.JRadioButton();
        lcrPanel = new javax.swing.JPanel();
        allItemsRadioButton = new javax.swing.JRadioButton();
        forcedChoiceRadioButton = new javax.swing.JRadioButton();
        kValueSpinner = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(1,1,50,1));
        kValueLabel = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        contentPanel = new javax.swing.JPanel();
        analysisContentComboBox = new javax.swing.JComboBox();
        competPanel = new javax.swing.JPanel();
        slopeValueSpinner = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(1,1,50,1));
        sampleValueLabel = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        competitionIndexComboBox = new javax.swing.JComboBox();
        graphDisplayPanel = new ChartPanel(graph);

        wordsPhonemesButtonGroup.add(wordsRadioButton);
        wordsPhonemesButtonGroup.add(phonemesRadioButton);

        choiceButtonGroup.add(allItemsRadioButton);
        choiceButtonGroup.add(forcedChoiceRadioButton);

        setLayout(new java.awt.GridBagLayout());

        buttonPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        updateGraphButton.setText("Update Graph");
        updateGraphButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateGraphButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(updateGraphButton);

        saveImageButton.setText("Save Image...");
        saveImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveImageButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(saveImageButton);

        exportGraphButton.setText("Export Graph Data...");
        exportGraphButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportGraphButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(exportGraphButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(buttonPanel, gridBagConstraints);

        graphSplitPane.setOneTouchExpandable(true);
        graphTabbedPane.setMinimumSize(new java.awt.Dimension(245, 358));
        graphTabbedPane.setPreferredSize(new java.awt.Dimension(250, 467));
        displayTabPanel.setLayout(new java.awt.GridBagLayout());

        displayTabPanel.setAlignmentY(0.0F);
        displayTabPanel.setMinimumSize(new java.awt.Dimension(100, 101));
        displayTabPanel.setPreferredSize(new java.awt.Dimension(100, 265));
        xLabelTextField.setColumns(15);
        xLabelTextField.setText("Time Steps");
        xLabelTextField.setPreferredSize(new java.awt.Dimension(188, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 3, 0, 10);
        displayTabPanel.add(xLabelTextField, gridBagConstraints);

        xLabelLabel.setText("X-Axis Label:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        displayTabPanel.add(xLabelLabel, gridBagConstraints);

        yLabelLabel.setText("Y-Axis Label");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        displayTabPanel.add(yLabelLabel, gridBagConstraints);

        yLabelTextField.setColumns(15);
        yLabelTextField.setText("Activation");
        yLabelTextField.setPreferredSize(new java.awt.Dimension(188, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 10);
        displayTabPanel.add(yLabelTextField, gridBagConstraints);

        titleLabel.setText("Title:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        displayTabPanel.add(titleLabel, gridBagConstraints);

        titleTextField.setColumns(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 3, 0, 10);
        displayTabPanel.add(titleTextField, gridBagConstraints);

        inputPositionSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        inputPositionSlider.setValue(90);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 0);
        displayTabPanel.add(inputPositionSlider, gridBagConstraints);

        inputPositionLabel.setLabelFor(inputPositionSlider);
        inputPositionLabel.setText("Input Label Position:");
        inputPositionLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 20, 0);
        displayTabPanel.add(inputPositionLabel, gridBagConstraints);

        jSeparator2.setEnabled(false);
        jSeparator2.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        displayTabPanel.add(jSeparator2, gridBagConstraints);

        graphTabbedPane.addTab("Display", displayTabPanel);

        analysisTabPanel.setLayout(new java.awt.GridLayout(1, 0));

        analysisTabPanel.setMinimumSize(new java.awt.Dimension(230, 550));
        analysisTabPanel.setPreferredSize(new java.awt.Dimension(240, 550));
        analysisTabScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        analysisTabScrollPane.setMinimumSize(new java.awt.Dimension(220, 200));
        analysisTabScrollPane.setPreferredSize(new java.awt.Dimension(233, 583));
        analysisTabScrollPanel.setLayout(new java.awt.GridBagLayout());

        analysisTabScrollPanel.setMinimumSize(new java.awt.Dimension(200, 550));
        analysisTabScrollPanel.setPreferredSize(new java.awt.Dimension(210, 640));
        analyzePanel.setLayout(new java.awt.GridBagLayout());

        analyzePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Analyze"));
        analyzePanel.setMinimumSize(new java.awt.Dimension(190, 71));
        analyzePanel.setPreferredSize(new java.awt.Dimension(190, 71));
        wordsRadioButton.setSelected(true);
        wordsRadioButton.setText("Words");
        wordsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wordsRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        analyzePanel.add(wordsRadioButton, gridBagConstraints);

        phonemesRadioButton.setText("Phonemes");
        phonemesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phonemesRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        analyzePanel.add(phonemesRadioButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        analysisTabScrollPanel.add(analyzePanel, gridBagConstraints);

        itemsPanel.setLayout(new java.awt.GridBagLayout());

        itemsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Items"));
        itemsPanel.setMinimumSize(new java.awt.Dimension(154, 180));
        itemsPanel.setPreferredSize(new java.awt.Dimension(150, 180));
        selectionPanel.setLayout(new java.awt.GridBagLayout());

        allButton.setText("All");
        allButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        allButton.setMinimumSize(new java.awt.Dimension(40, 25));
        allButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        selectionPanel.add(allButton, gridBagConstraints);

        resetButton.setText("Reset");
        resetButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        resetButton.setMinimumSize(new java.awt.Dimension(40, 25));
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        selectionPanel.add(resetButton, gridBagConstraints);

        jSeparator1.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        selectionPanel.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        itemsPanel.add(selectionPanel, gridBagConstraints);

        unselectedScrollPane.setMinimumSize(new java.awt.Dimension(23, 50));
        unselectedList.setModel(new DefaultListModel());
        unselectedList.setMinimumSize(new java.awt.Dimension(0, 25));
        unselectedScrollPane.setViewportView(unselectedList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        itemsPanel.add(unselectedScrollPane, gridBagConstraints);

        selectedList.setModel(new DefaultListModel());
        selectedList.setMinimumSize(new java.awt.Dimension(0, 25));
        selectedScrollPane.setViewportView(selectedList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        itemsPanel.add(selectedScrollPane, gridBagConstraints);

        selectButton.setText("->");
        selectButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        itemsPanel.add(selectButton, gridBagConstraints);

        unselectButton.setText("<-");
        unselectButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        unselectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unselectButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        itemsPanel.add(unselectButton, gridBagConstraints);

        itemsRadioButtonPanel.setLayout(new java.awt.GridBagLayout());

        itemsButtonGroup.add(topNRadioButton);
        topNRadioButton.setText("Top N Items:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        itemsRadioButtonPanel.add(topNRadioButton, gridBagConstraints);

        topNTextField.setText("10");
        topNTextField.setMinimumSize(new java.awt.Dimension(20, 19));
        topNTextField.setPreferredSize(new java.awt.Dimension(30, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        itemsRadioButtonPanel.add(topNTextField, gridBagConstraints);

        itemsButtonGroup.add(specifiedItemsRadioButton);
        specifiedItemsRadioButton.setSelected(true);
        specifiedItemsRadioButton.setText("Specified Items");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        itemsRadioButtonPanel.add(specifiedItemsRadioButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        itemsPanel.add(itemsRadioButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        analysisTabScrollPanel.add(itemsPanel, gridBagConstraints);

        alignmentPanel.setLayout(new java.awt.GridBagLayout());

        alignmentPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Alignment"));
        alignmentButtonGroup.add(averageRadioButton);
        averageRadioButton.setText("Average");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        alignmentPanel.add(averageRadioButton, gridBagConstraints);

        alignmentButtonGroup.add(maxAdhocRadioButton);
        maxAdhocRadioButton.setText("Max (Ad-Hoc)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        alignmentPanel.add(maxAdhocRadioButton, gridBagConstraints);

        alignmentButtonGroup.add(maxAdhoc2RadioButton);
        maxAdhoc2RadioButton.setText("Max (Ad-Hoc-2)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        alignmentPanel.add(maxAdhoc2RadioButton, gridBagConstraints);

        alignmentButtonGroup.add(specifiedRadioButton);
        specifiedRadioButton.setSelected(true);
        specifiedRadioButton.setText("Specified:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        alignmentPanel.add(specifiedRadioButton, gridBagConstraints);

        specifiedTextField.setColumns(2);
        specifiedTextField.setText("4");
        specifiedTextField.setMargin(new java.awt.Insets(0, 4, 0, 4));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        alignmentPanel.add(specifiedTextField, gridBagConstraints);

        alignmentButtonGroup.add(fraunfelderRadioButton);
        fraunfelderRadioButton.setText("Fraunfelder (x, x+1):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        alignmentPanel.add(fraunfelderRadioButton, gridBagConstraints);

        alignmentButtonGroup.add(maxPosthocRadioButton);
        maxPosthocRadioButton.setText("Max (Post-Hoc)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        alignmentPanel.add(maxPosthocRadioButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        analysisTabScrollPanel.add(alignmentPanel, gridBagConstraints);

        lcrPanel.setLayout(new java.awt.GridBagLayout());

        lcrPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Luce Choice"));
        lcrPanel.setMinimumSize(new java.awt.Dimension(190, 71));
        lcrPanel.setPreferredSize(new java.awt.Dimension(190, 71));
        allItemsRadioButton.setText("All Items");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        lcrPanel.add(allItemsRadioButton, gridBagConstraints);

        forcedChoiceRadioButton.setSelected(true);
        forcedChoiceRadioButton.setText("Forced Choice");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        lcrPanel.add(forcedChoiceRadioButton, gridBagConstraints);

        kValueSpinner.setPreferredSize(new java.awt.Dimension(45, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        lcrPanel.add(kValueSpinner, gridBagConstraints);

        kValueLabel.setText("k Value:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        lcrPanel.add(kValueLabel, gridBagConstraints);

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator3.setPreferredSize(new java.awt.Dimension(2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        lcrPanel.add(jSeparator3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        analysisTabScrollPanel.add(lcrPanel, gridBagConstraints);

        contentPanel.setLayout(new java.awt.GridBagLayout());

        contentPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Content"));
        contentPanel.setMinimumSize(new java.awt.Dimension(150, 71));
        contentPanel.setPreferredSize(new java.awt.Dimension(150, 71));
        analysisContentComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Activations", "Response Probabilities", "Competition Index" }));
        analysisContentComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analysisContentComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        contentPanel.add(analysisContentComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        analysisTabScrollPanel.add(contentPanel, gridBagConstraints);

        competPanel.setLayout(new java.awt.GridBagLayout());

        competPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Global Competition Index"));
        competPanel.setMinimumSize(new java.awt.Dimension(190, 71));
        competPanel.setName("Global Competition");
        competPanel.setPreferredSize(new java.awt.Dimension(190, 71));
        slopeValueSpinner.setPreferredSize(new java.awt.Dimension(45, 20));
        slopeValueSpinner.setValue(4);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        competPanel.add(slopeValueSpinner, gridBagConstraints);

        sampleValueLabel.setText("Sampling width:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        competPanel.add(sampleValueLabel, gridBagConstraints);

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator4.setPreferredSize(new java.awt.Dimension(2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        competPanel.add(jSeparator4, gridBagConstraints);

        competitionIndexComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2nd Derivative", "1st Derivative", "Raw competition" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        competPanel.add(competitionIndexComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        analysisTabScrollPanel.add(competPanel, gridBagConstraints);
        competPanel.getAccessibleContext().setAccessibleName("Global Competition ");
        competPanel.getAccessibleContext().setAccessibleDescription("Global Competition");

        analysisTabScrollPane.setViewportView(analysisTabScrollPanel);

        analysisTabPanel.add(analysisTabScrollPane);

        graphTabbedPane.addTab("Analysis", analysisTabPanel);

        graphTabbedPane.setSelectedIndex(1);

        graphSplitPane.setLeftComponent(graphTabbedPane);

        graphDisplayPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        graphSplitPane.setRightComponent(graphDisplayPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(graphSplitPane, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void analysisContentComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analysisContentComboBoxActionPerformed
        // update what is enabled and disabled.
        if(analysisContentComboBox.getSelectedIndex()==0){
            lcrPanel.setEnabled(false);
            itemsPanel.setEnabled(true);
            alignmentPanel.setEnabled(true);
            competPanel.setEnabled(false);
        }
        else if(analysisContentComboBox.getSelectedIndex()==1){
            lcrPanel.setEnabled(true);
            itemsPanel.setEnabled(true);
            alignmentPanel.setEnabled(true);
            competPanel.setEnabled(false);
        }
        else if(analysisContentComboBox.getSelectedIndex()==2){
            lcrPanel.setEnabled(false);
            itemsPanel.setEnabled(false);
            alignmentPanel.setEnabled(false);
            competPanel.setEnabled(true);
        }
        // send GUI values to objects
    }//GEN-LAST:event_analysisContentComboBoxActionPerformed

    private void phonemesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phonemesRadioButtonActionPerformed
        loadSelectionListsFromParams();
    }//GEN-LAST:event_phonemesRadioButtonActionPerformed

    private void unselectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unselectButtonActionPerformed
        // foreach selected item in the selected list, move it to the unselected
        // list
        
        int [] indices = selectedList.getSelectedIndices();
        for (int i = indices.length-1; i >= 0; i--) // go backwards
        {
            // copy to the right
            ((DefaultListModel)unselectedList.getModel()).addElement(((DefaultListModel)selectedList.getModel()).getElementAt(indices[i]));
            
            // and delete
            ((DefaultListModel)selectedList.getModel()).removeElementAt(indices[i]);
        }
    }//GEN-LAST:event_unselectButtonActionPerformed

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        // foreach selected item in the unselected list, move it to the selected
        // list
        
        int [] indices = unselectedList.getSelectedIndices();
        for (int i = indices.length-1; i >= 0; i--) // go backwards
        {
            // copy to the right
            ((DefaultListModel)selectedList.getModel()).addElement(((DefaultListModel)unselectedList.getModel()).getElementAt(indices[i]));
            
            // and delete
            ((DefaultListModel)unselectedList.getModel()).removeElementAt(indices[i]);
        }
    }//GEN-LAST:event_selectButtonActionPerformed

    private void wordsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wordsRadioButtonActionPerformed
        loadSelectionListsFromParams();
    }//GEN-LAST:event_wordsRadioButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        loadSelectionListsFromParams();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void exportGraphButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportGraphButtonActionPerformed
        // pop up a file-save box (*.dat)
        JFileChooser fileChooser = new JFileChooser(traceProperties.rootPath.getAbsolutePath());
        
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setDialogTitle("Save graph data to file"); 
        fileChooser.setCurrentDirectory(traceProperties.workingPath);
        
        int returnVal = fileChooser.showSaveDialog(this);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            java.io.File exportFile = fileChooser.getSelectedFile();
            traceProperties.workingPath = exportFile.getParentFile(); 
        
            // set cursor
            setCursor(new Cursor(Cursor.WAIT_CURSOR));

            // and save to file
            if (!analysis.exportAnalysis(dataset, exportFile, false))
            {
                // failed!
                javax.swing.JOptionPane.showMessageDialog(null,
                    "Export failed.",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            
            // restore cursor
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_exportGraphButtonActionPerformed

    private void saveImageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveImageButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser(traceProperties.rootPath.getAbsolutePath());
        
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setDialogTitle("Save graph image to file"); 
        fileChooser.setFileFilter(new edu.uconn.psy.jtrace.IO.PNGFileFilter());
        fileChooser.setCurrentDirectory(traceProperties.workingPath);            
        
        int returnVal = fileChooser.showSaveDialog(this);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            java.io.File saveFile = fileChooser.getSelectedFile();
            traceProperties.workingPath = saveFile.getParentFile(); 
        
            // set cursor
            setCursor(new Cursor(Cursor.WAIT_CURSOR));

            // and save to file
            try
            {
                ChartUtilities.saveChartAsPNG(saveFile, graph, 1024, 768);
            }
            catch (java.io.IOException ex)
            {
                // failed!
                javax.swing.JOptionPane.showMessageDialog(null,
                    "Save failed.",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            
            // restore cursor
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        
    }//GEN-LAST:event_saveImageButtonActionPerformed

    private void allButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allButtonActionPerformed
        
        int numUnselected = ((DefaultListModel)unselectedList.getModel()).getSize();
        for (int i = 0; i < numUnselected; i++) 
        {
            // copy to the right
            ((DefaultListModel)selectedList.getModel()).addElement(((DefaultListModel)unselectedList.getModel()).getElementAt(i));
        }
           
        ((DefaultListModel)unselectedList.getModel()).clear();
    }//GEN-LAST:event_allButtonActionPerformed

    public void setSplitPane(double splitPosition) {
        graphSplitPane.setDividerLocation(splitPosition);
    }
    
    private void updateGraphButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateGraphButtonActionPerformed
        updateGraph();
    }//GEN-LAST:event_updateGraphButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup alignmentButtonGroup;
    private javax.swing.JPanel alignmentPanel;
    private javax.swing.JButton allButton;
    private javax.swing.JRadioButton allItemsRadioButton;
    private javax.swing.JComboBox analysisContentComboBox;
    private javax.swing.JPanel analysisTabPanel;
    private javax.swing.JScrollPane analysisTabScrollPane;
    private javax.swing.JPanel analysisTabScrollPanel;
    private javax.swing.JPanel analyzePanel;
    private javax.swing.JRadioButton averageRadioButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.ButtonGroup choiceButtonGroup;
    private javax.swing.JPanel competPanel;
    private javax.swing.JComboBox competitionIndexComboBox;
    private javax.swing.ButtonGroup contentButtonGroup;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JPanel displayTabPanel;
    private javax.swing.JButton exportGraphButton;
    private javax.swing.JRadioButton forcedChoiceRadioButton;
    private javax.swing.JRadioButton fraunfelderRadioButton;
    private javax.swing.JPanel graphDisplayPanel;
    private javax.swing.JSplitPane graphSplitPane;
    private javax.swing.JTabbedPane graphTabbedPane;
    private javax.swing.JLabel inputPositionLabel;
    private javax.swing.JSlider inputPositionSlider;
    private javax.swing.ButtonGroup itemsButtonGroup;
    private javax.swing.JPanel itemsPanel;
    private javax.swing.JPanel itemsRadioButtonPanel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel kValueLabel;
    private javax.swing.JSpinner kValueSpinner;
    private javax.swing.JPanel lcrPanel;
    private javax.swing.JRadioButton maxAdhoc2RadioButton;
    private javax.swing.JRadioButton maxAdhocRadioButton;
    private javax.swing.JRadioButton maxPosthocRadioButton;
    private javax.swing.JRadioButton phonemesRadioButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel sampleValueLabel;
    private javax.swing.JButton saveImageButton;
    private javax.swing.JButton selectButton;
    private javax.swing.JList selectedList;
    private javax.swing.JScrollPane selectedScrollPane;
    private javax.swing.JPanel selectionPanel;
    private javax.swing.JSpinner slopeValueSpinner;
    private javax.swing.JRadioButton specifiedItemsRadioButton;
    private javax.swing.JRadioButton specifiedRadioButton;
    private javax.swing.JTextField specifiedTextField;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTextField;
    private javax.swing.JRadioButton topNRadioButton;
    private javax.swing.JTextField topNTextField;
    private javax.swing.JButton unselectButton;
    private javax.swing.JList unselectedList;
    private javax.swing.JScrollPane unselectedScrollPane;
    private javax.swing.JButton updateGraphButton;
    private javax.swing.ButtonGroup wordsPhonemesButtonGroup;
    private javax.swing.JRadioButton wordsRadioButton;
    private javax.swing.JLabel xLabelLabel;
    private javax.swing.JTextField xLabelTextField;
    private javax.swing.JLabel yLabelLabel;
    private javax.swing.JTextField yLabelTextField;
    // End of variables declaration//GEN-END:variables
    
}
