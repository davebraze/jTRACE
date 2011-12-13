/*
 * HelpFrame.java
 *
 * Created on May 12, 2005, 3:50 PM
 */

package edu.uconn.psy.jtrace.UI;

import edu.uconn.psy.jtrace.UI.traceProperties;
import java.awt.GridBagConstraints;
import java.io.*;
import javax.swing.event.*;


/**
 *
 * @author  harlan
 */
public class HelpFrame extends javax.swing.JFrame implements HyperlinkListener {
    
    traceProperties properties;
    
    /** Creates new form HelpFrame */
    public HelpFrame(traceProperties _p) 
    {
        properties = _p;
        
        initComponents();
        manualScrollPane.setPreferredSize(new java.awt.Dimension(750, 600));
        
        manualPane.setContentType("text/html");
        manualPane.setEditable(false);
        manualPane.addHyperlinkListener(this);
            
        // create path for manual.html
        boolean goodManualPane = true;
        String manualPath = jTRACEMDI.properties.getProperty("WorkingDirectory") + System.getProperty("file.separator") +
                "docs" + System.getProperty("file.separator") + "manual.html";
        
        // try to open it -- if not, display error message
        
        try
        {
            manualPane.setPage("file://" + manualPath);
        }
        catch (IOException ex)
        {
            // file not found -- create error message in the pane
            manualPane.setText("<em>ERROR: jTRACE manual could not be found!</em>" +
                  "<br>" + manualPath);
        }
        
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        okButton = new javax.swing.JButton();
        manualScrollPane = new javax.swing.JScrollPane();
        manualPane = new javax.swing.JEditorPane();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("jTRACE Help");
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        getContentPane().add(okButton, gridBagConstraints);

        manualScrollPane.setPreferredSize(new java.awt.Dimension(600, 600));
        manualScrollPane.setViewportView(manualPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(manualScrollPane, gridBagConstraints);

        pack();
    }//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed
    
    public void scrollToLink(String link){
        manualPane.scrollToReference(link);
    }
    
    public void hyperlinkUpdate(HyperlinkEvent event) 
    {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
        {
            try {
                manualPane.setPage(event.getURL());
            } catch(IOException ioe) {
                System.out.println("Can't follow link to " 
                    + event.getURL().toExternalForm() + ": " + ioe);
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // set or create properties object
//                traceProperties properties=new edu.uconn.psy.jtrace.UI.traceProperties();    
//                properties.situate(this);
//        
//                new HelpFrame(properties).setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane manualPane;
    private javax.swing.JScrollPane manualScrollPane;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
    
}