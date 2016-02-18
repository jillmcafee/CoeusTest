/*
 * @(#)CoeusHelpMenu.java 1.0 10/18/02
 *
 * Copyright (c) Massachusetts Institute of Technology
 * 77 Massachusetts Avenue, Cambridge, MA 02139-4307
 * All rights reserved.
 */
package edu.mit.coeus.gui.menu;

import edu.mit.coeus.utils.CoeusGuiConstants;
import edu.mit.coeus.gui.CoeusAppletMDIForm;

import javax.swing.*;
import java.awt.event.*;
import java.util.Vector;
import edu.mit.coeus.gui.CoeusAboutForm;
import edu.mit.coeus.utils.CoeusOptionPane;
import edu.mit.coeus.gui.CoeusMessageResources;
import edu.mit.coeus.gui.URLOpener;


/**
 * This class creates Central Help menu for the coeus application.
 *
 * @version :1.0 October 18, 2002, 3:11 PM
 * @author Guptha
 */

public class CoeusHelpMenu extends JMenu implements ActionListener{

    /*
     * Help menu items
     */
    CoeusMenuItem helpTopics, about;
    /* JM 2-11-2016 new contact help option */
    CoeusMenuItem contactHelp;
    /* JM END */

    /*
     * to indicate horizondal seperator in menu items
     */
    private final String SEPERATOR="seperator";

    private CoeusMenu coeusMenu;

    private CoeusAppletMDIForm mdiForm;
    
    CoeusAboutForm coeusAboutForm = null;
       
    //holds CoeusMessageResources instance used for reading message Properties.
    private CoeusMessageResources coeusMessageResources;
    
    /** Default constructor which constructs the help menu for coeus application.
     * @param mdiForm  CoeusAppletMDIForm
     */
    public CoeusHelpMenu(CoeusAppletMDIForm mdiForm){
        super();
        this.mdiForm = mdiForm;
        coeusMessageResources = CoeusMessageResources.getInstance();
        createMenu();
    }

    /**
     * This method is used to get the help menu
     *
     * @return JMenu coeus help menu
     */
    public JMenu getMenu(){
        return coeusMenu;
    }

    /**
     * This method is used to create help menu for coeus application.
     */
    private void createMenu(){
        java.util.Vector fileChildren = new java.util.Vector();
        helpTopics = new CoeusMenuItem("Help Topics",null,true,true);
        helpTopics.setMnemonic('T');
        about = new CoeusMenuItem("About",null,true,true);
        about.setMnemonic('A');
        fileChildren.add(helpTopics);
        fileChildren.add(SEPERATOR);
        fileChildren.add(about);
        
        /* JM 2-11-2016 new contact help option */
        contactHelp = new CoeusMenuItem("Contact Coeus Help",null,true,true);
        fileChildren.add(contactHelp);
        /* JM END */
        
        coeusMenu = new CoeusMenu("Help",null,fileChildren,true,true);
        coeusMenu.setMnemonic('H');
        
        //add listener
        helpTopics.addActionListener(this);
        about.addActionListener(this);
        contactHelp.addActionListener(this); // JM 2-12-2016

    }

     /** This method is used to handle the action event for the help menu items.
     * @param ae  ActionEvent
     */
    public void actionPerformed(ActionEvent ae){
        Object source = ae.getSource();
        if (source.equals(about)){
                showAbout();
        }
        /* JM 2-11-2016 new contact help option */
        else if (source.equals(contactHelp)) {
        	System.out.println("Contact called from the menu");
        	edu.vanderbilt.coeus.gui.toolbar.ToolFunctions tools = new edu.vanderbilt.coeus.gui.toolbar.ToolFunctions();
        	tools.contactCoeusHelp();
        }
        /* JM END */
        else if(source.equals(helpTopics)){
            try{
                URLOpener.openUrl(CoeusGuiConstants.HELP_URL);
            }catch(Exception ex){
                CoeusOptionPane.showErrorDialog(ex.getMessage());
            }
        }else
            log(coeusMessageResources.parseMessageKey(
                                            "funcNotImpl_exceptionCode.1100"));
    }
    
    /**
     * when the about the screen is clicked the about dialog is shown
     */
     private void showAbout() {
        coeusAboutForm = new CoeusAboutForm(mdiForm, "About" ,true){
            protected JRootPane createRootPane() {
                ActionListener actionListener = new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        coeusAboutForm.dispose();
                    }
                };
                JRootPane rootPane = new JRootPane();
                KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
                0);
                rootPane.registerKeyboardAction(actionListener, stroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
                return rootPane;
            }
        };
        coeusAboutForm.setVisible(true);
    }
     
    /**
     * display alert message
     *
     * @param mesg the message to be displayed
     */
    private void log(String mesg) {
        CoeusOptionPane.showInfoDialog(mesg);
    }
}