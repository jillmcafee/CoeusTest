 /*
 * @(#)CoeusAppletMDIForm.java 1.0 07/25/02
 *
 * Copyright (c) Massachusetts Institute of Technology
 * 77 Massachusetts Avenue, Cambridge, MA 02139-4307
 * All rights reserved.
 */

/* PMD check performed, and commented unused imports and variables on 19-Aug-2010
 * by George J Nirappeal
 */
package edu.mit.coeus.gui;

import edu.ucsd.coeus.personalization.controller.AbstractController;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.*;
import java.applet.AppletContext;
// Added by Shivakumar for Locking enhancement
import java.util.Timer;

import edu.mit.coeus.brokers.*;
import edu.mit.coeus.bean.*;
import edu.mit.coeus.utils.query.AuthorizationOperator;
import edu.mit.coeus.utils.AppletServletCommunicator;
import edu.mit.coeus.gui.toolbar.CoeusToolBarFactory;
import edu.mit.coeus.gui.menu.*;
import edu.mit.coeus.utils.CoeusGuiConstants;
import edu.mit.coeus.utils.CoeusOptionPane;
import edu.mit.coeus.bean.UserInfoBean;
import edu.mit.coeus.exception.CoeusException;
import edu.mit.coeus.propdev.bean.MessageBean;
import edu.mit.coeus.user.bean.UserPreferencesBean;
import edu.mit.coeus.utils.ScreenFocusTraversalPolicy;
import edu.mit.coeus.utils.TypeConstants;
//import edu.mit.coeus.utils.PollingMonitor;
import edu.mit.coeus.utils.locking.RemindTask;

/* JM 2-12-2016 new Vandy toolbar */
import edu.vanderbilt.coeus.gui.toolbar.ToolBar;
/* JM END */

/** <CODE>CoeusAppletMDIForm</CODE> is a main window to show the menu,toolbar and its
 * associated internal frames. This window will appear on the screen if the user
 * authentication is successful.
 *
 * On click of menu item the user will receive an internal frame which itself holds
 * menus and toolbars to be attached with this frame. The menus and toolbars can be
 * added to the internal frame using <CODE>CoeusMenuFactory</CODE> and <CODE>CoeusToolbarFactory</CODE> respectively.
 * All the menus and toolbars of the internal frame are created and stacked with <CODE>CoeusWindowBean</CODE>.
 * On click of menuitem, this application fetches the internal frame from the <CODE>CoeusWindowBean</CODE>
 * and displays accordingly.
 *
 * Each and every menus will hold their own listeners. For example, the
 * CoeusAwardListener is added with the 'awards' menu. All other menus are
 * associated with the CoeusActionListener.
 *
 *
 * @version :1.0 July 23, 2002, 10:15 AM
 * @author Guptha K
 */
public class CoeusAppletMDIForm extends JFrame {
    /*
     * destktop pane to place components
     */
    JDesktopPane desktop;
    /*
     * applet code base
     */
    URL codebase;
    /*
     * user autherization id
     */
    int roleid;
    /*
     * content pane of this object
     */
    Container contentPane = getContentPane();
    /*
     * reference of CoeusToolBarFactory
     */
    CoeusToolBarFactory coeusToolBarFactory;
    /*
     * reference of CoeusMenuBar
     */
    CoeusMenuBar coeusMenuBar;
    /*
     * panel to attach the toolbar
     */
    JPanel toolBarPanel = new JPanel();
    
    /** This is used to for polling the DB
     */
    
    private byte[] authSignature;
    /**
     * holds the current of Applect context which initiated this MDI form.
     */
    private AppletContext coeusAppletContext = null ;


    private JMenu rolodexEditMenu,sponsorEditMenu,fileMenu,maintainMenu,
            departmentalMenu,adminMenu,
            centralAdminMenu,windowMenu,helpMenu,rolodexToolsMenu,sponsorToolsMenu, reportMenu;

    private CoeusWindowMenu coeusWindowMenu = null;
    private CoeusFileMenu coeusFileMenu = null;
    //holds CoeusMessageResources instance used for reading message Properties.
    private CoeusMessageResources coeusMessageResources;

    Icon coeusIcon;
    /* JM 3-31-2015 16x16 icon */
    Icon coeus16Icon;
    /* JM END */
    
    // keep the default menus in vector for refreshing
    Vector defaultMenus;
    JToolBar defaultToolBar,sponsorToolBar,rolodexToolBar,unitHierarchyToolBar;

    static Hashtable activeFrames;

    private String userName;
    
    private CheckDuplicateInternalFrame duplicateFrames;
    private static final char COPY_MODE = 'E';
    
    //Added by sharath to store login user details (28/7/2003)
    private String userId, unitNumber, unitName;
    
    private String fullName;
    private String coeusGlobalImage;
    // Added by Shivakumar to store lockId and update-timestamp - BEGIN    
    private String lockIdKey, updateTimestamp;
    private Hashtable locking;
    Timer timer;    
//    PollingMonitor pollingMonitor; 
    // Added by Shivakumar to store lockId and update-timestamp - END    
    Frame appFrame ;
    
    /** begin: modified by ravi on 17-02-2003 for providing status bar
     * bug id: #147  */
    
   /* private JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private JLabel lblStatus = new JLabel();*/
    /*** end: bug id: #147 */
    
    //For Right Checking
    private static final String AUTH_SERVLET = "/AuthorizationServlet";
    private static final String UTILITY_SERVLET = "/UtilityServlet";
    private static final String LOGIN_SERVLET = "/loginServlet";
    
    private CoeusMaintainMenu coeusMaintainMenu;
    private CoeusCentralAdminMenu coeusCentralAdminMenu;
    private CoeusDepartmentalMenu coeusDepartmentalMenu;
    private CoeusAdminMenu coeusAdminMenu;
    private CoeusReportMenu coeusReportMenu;
    
    private CoeusToolBarFactory coeusToolBar;
    
    /* JM 2-12-2016 new Vandy toolbar */
    private ToolBar customToolBar;
    JToolBar defaultCustomToolBar;
    /* JM END */
    
    private RemindTask remindTask;    
    
    //Used For Award Templates
    private HashSet templateSet = new HashSet();
    
    private String instanceName;
    
    private String swingLoginMode;
    
    private static final String FUNCTION_TYPE_OSP = "OSP";
    
    /* JM 3-24-2015 latest build version number */
    private final String VERSION = "5.1.4";
    /* JM END */
    
    /** <I>Default Constructor</I> to create a MDI form.
     */
    public CoeusAppletMDIForm() {
        super("Coeus");
        duplicateFrames = new CheckDuplicateInternalFrame();
//      pollingMonitor = PollingMonitor.getInstance();
        //remindTask = RemindTask.getInstance();
    }

     public void setParentFrame (Frame appFrame) 
    {
        this.appFrame = appFrame ;
    }  
    public Frame getParentFrame () 
    {
        return this.appFrame  ;
    }

//    static {
//        // Modifies default time zone, disables Daylight Saving Time.
//        TimeZone defaultTimeZone = TimeZone.getDefault();
//        int rawOffset = defaultTimeZone.getRawOffset();
//        String timeZoneId = defaultTimeZone.getID();
//        SimpleTimeZone simpleTimeZone = new SimpleTimeZone(rawOffset,
//        timeZoneId,
//        0, //start month
//        0, //start day
//        0, //start day of week
//        0, //start time 
//        0, //end month
//        0, //end day
//        0, //end day of week
//        0); // end time
//        TimeZone.setDefault(simpleTimeZone);
//    }
    static{
        TimeZone est = TimeZone .getTimeZone ( "UTC" ) ;
        TimeZone.setDefault(est);
    }
   
    /**
     * Displays the form with menu and toolbar. This is the parent window of all
     * internal frames.
     */
    public void showForm() {
        activeFrames = new Hashtable();
        setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        addWindowListener(new WindowAdapter(){
            public void windowClosing( WindowEvent winEvent ){
                coeusMessageResources = CoeusMessageResources.getInstance();
                String message = coeusMessageResources.parseMessageKey(
                                        "toolBarFactory_exitConfirmCode.1149");
                int answer = CoeusOptionPane.showQuestionDialog(message,
                            CoeusOptionPane.OPTION_YES_NO, CoeusOptionPane.DEFAULT_NO);
                if (answer == JOptionPane.YES_OPTION) {
                    if( closeInternalFrames() ) {
                        /*
                         *Commented by Geo
                         *Exit from the JVM when mdi frame gets closed
                         */
//                        if (appFrame != null){
//                            try{
//                                System.exit(0) ;
//                            }catch(Exception exp){
//                                exp.printStackTrace() ;
//                            }
//                        }else{
//                            System.out.println("Exit application (Frame is null)...") ;
//                            System.exit(0);
//                        }
                        //JIRA COEUSQA 2527 - START
                        logout();
                        //JIRA COEUSQA 2527 - END
                        System.exit(0);
                    }
                }
            }
        });
        addComponentListener(new ComponentAdapter(){
           public void componentResized(ComponentEvent ce) {
                Dimension size = getContentPane().getSize();
                int width, height;
                width = (int)size.getWidth();
                JToolBar defaultToolBar = getDefaultToolBar();
                if( defaultToolBar != null ){
                    int defaultToolbarHeight = defaultToolBar.getHeight();
                    height = ((int)size.getHeight() - (2*defaultToolbarHeight));
                }else{
                    height = (int)size.getHeight() - getToolBarPanel().getHeight();
                }
                if(desktop!=null){
                    JInternalFrame[] activeInternalFrames = desktop.getAllFrames();
                    if( activeInternalFrames != null ){
                        int totalNumberOfFrames = activeInternalFrames.length;
                        for( int indx = 0; indx < totalNumberOfFrames; indx++ ){
                            if( !activeInternalFrames[indx].isIcon()){
                                activeInternalFrames[indx].setSize(width, height);
                            }
                        }
                    }
                }
               
           }
        });
        int inset = 0;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset*2, screenSize.height-50-inset*2);
        contentPane.setLayout(new BorderLayout());
        toolBarPanel.setLayout(new BoxLayout(toolBarPanel,BoxLayout.Y_AXIS));
       
        /**Get the DB instance name like Test DB, DevDB and Production DB
         */
        
        String name = "  -  "+getInstanceName();
        String title = "";
//        if(getInstanceName()!= null ){
//            title = "Coeus "+ " -  "+ getUserId() + name;
//        }else{
//            title = "Coeus "+ " -  "+ getUserId();
//        } 
          if(getInstanceName()!= null ){
            title = "Coeus "+ " -  "+ this.fullName + name;
        }else{
            title = "Coeus "+ " -  "+ this.fullName;
        }
        
        System.out.println("The title before showing :"+title);
        this.setTitle(title);
        System.out.println("The title After showing :"+title);
        contentPane.add(BorderLayout.NORTH, toolBarPanel);

        desktop = new JDesktopPane(); //a specialized layered pane
        desktop.putClientProperty("JDesktopPane.dragMode", "outline");
        desktop.setBackground(Color.gray);

        desktop.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_MASK),
                "CtrlTab");
        desktop.getActionMap().put( "CtrlTab" ,
            new AbstractAction( "CtrlTab" ){
                public void actionPerformed(ActionEvent evt) {

                }
            }
        );
        contentPane.add(desktop,BorderLayout.CENTER);

        /** begin: modified by ravi on 17-02-2003 for providing status bar
         * bug id: #147  */
       /* lblStatus.setFont(CoeusFontFactory.getNormalFont()); 
        lblStatus.setHorizontalTextPosition(JLabel.LEFT);
        lblStatus.setText(userId);
        statusPanel.add(lblStatus);
        //statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        contentPane.add(statusPanel,BorderLayout.SOUTH);
        /** end: bug id: #147  */
        
        coeusMenuBar = new CoeusMenuBar();
        coeusFileMenu = new CoeusFileMenu(this);
        fileMenu = coeusFileMenu.getMenu();
        
        //For right checking 
        coeusMaintainMenu = new CoeusMaintainMenu(this);
        coeusDepartmentalMenu = new CoeusDepartmentalMenu(this);
        coeusAdminMenu = new CoeusAdminMenu(this);
        coeusCentralAdminMenu = new CoeusCentralAdminMenu(this);
        
        maintainMenu = coeusMaintainMenu.getMenu();
        departmentalMenu = coeusDepartmentalMenu.getMenu();
        adminMenu = coeusAdminMenu.getMenu();
        centralAdminMenu = coeusCentralAdminMenu.getMenu();
        coeusWindowMenu = new CoeusWindowMenu(this);

        coeusReportMenu = new CoeusReportMenu();
        reportMenu = coeusReportMenu.getMenu();

         // create default tool bar
        coeusToolBar = new CoeusToolBarFactory(this);
        defaultToolBar =  coeusToolBar.getToolBar();
        defaultToolBar.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        toolBarPanel.add(defaultToolBar,BorderLayout.LINE_START); //JM was NORTH

        /* JM 2-12-2016 create custom tool bar */
        customToolBar = new ToolBar(this);
       	defaultCustomToolBar =  customToolBar.getToolBar();
       	defaultCustomToolBar.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
       	toolBarPanel.add(defaultCustomToolBar,BorderLayout.LINE_END);
       	toolBarPanel.setSize(screenSize.width - 100, toolBarPanel.getHeight());
       	/* JM END */
 
       	CoeusGuiConstants.setMDIForm(this);
       	CoeusLookAndFeelChooser.setCoeusLookAndFeel(
                   CoeusLookAndFeelChooser.getSystemDependentLookAndFeel() );
        
       	//To enable/disable/make visible menu items depending on the Rights
        //of the logged in user
        checkForRights();
        
        windowMenu = coeusWindowMenu.getMenu();
        helpMenu = (new CoeusHelpMenu(this)).getMenu();
        coeusMenuBar.add(fileMenu);
        coeusMenuBar.add(maintainMenu);
        coeusMenuBar.add(departmentalMenu);
        coeusMenuBar.add(adminMenu);
        coeusMenuBar.add(centralAdminMenu);
        
        coeusMenuBar.add(reportMenu);
        coeusMenuBar.add(windowMenu);
        coeusMenuBar.add(helpMenu);
        
        //add listeners to menu items
        setJMenuBar(coeusMenuBar);

        // set coeus icon
        setCoeusIcon(getCoeusImageIcon());
        // set title icon
        setIconImage(getCoeusImageIcon().getImage());
      
        /* JM 3-31-2015 pretty icons */
        try {
	        java.util.List<Image> icons = new ArrayList<Image>();
	        
        	icons.add(new ImageIcon(getClass().getClassLoader().getResource(CoeusGuiConstants.ICON128)).getImage());
        	icons.add(new ImageIcon(getClass().getClassLoader().getResource(CoeusGuiConstants.ICON96)).getImage());
        	icons.add(new ImageIcon(getClass().getClassLoader().getResource(CoeusGuiConstants.ICON64)).getImage());
        	icons.add(new ImageIcon(getClass().getClassLoader().getResource(CoeusGuiConstants.ICON48)).getImage());
        	icons.add(new ImageIcon(getClass().getClassLoader().getResource(CoeusGuiConstants.ICON32)).getImage());
        	icons.add(new ImageIcon(getClass().getClassLoader().getResource(CoeusGuiConstants.ICON24)).getImage());
        	icons.add(new ImageIcon(getClass().getClassLoader().getResource(CoeusGuiConstants.ICON16)).getImage());
        	
            //icons.add(getCoeusImageIcon().getImage());
        	setIconImages(icons);	        
        }
        catch (Exception e) {
            setIconImage(getCoeusImageIcon().getImage());
        }
        /* JM END */ 

        /* commented because the FocusManager class is obsolete from jdk 1.4
         if (System.getProperty("java.version").startsWith("1.3")) {
            FocusManager.setCurrentManager(new CoeusFocusManager(this));
        }*/
        CoeusFocusManager focusManager = new CoeusFocusManager(this);
        KeyboardFocusManager.setCurrentKeyboardFocusManager( focusManager );
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher( focusManager );
        UIManager.getDefaults().put( "ComboBox.disabledForeground", Color.black );
        setVisible(true);
        if( coeusGlobalImage != null && coeusGlobalImage.trim().length() > 0){
            ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(
                    CoeusGuiConstants.getPath()+coeusGlobalImage ));
            JLabel imageLabel = new JLabel( icon );
            int desktopwidth = screenSize.width - inset*2;
            int desktopheight = screenSize.height-120-inset*2;
            icon.setImage( icon.getImage().getScaledInstance(desktopwidth,desktopheight,Image.SCALE_DEFAULT));
            int iconwidth = icon.getIconWidth();
            int iconheight = icon.getIconHeight();
            imageLabel.setBounds(0,0,iconwidth,iconheight);
//            desktop.setLayout(new FlowLayout());
            desktop.add(imageLabel, JLayeredPane.FRAME_CONTENT_LAYER);
            imageLabel.setLocation((desktopwidth - iconwidth)/2,(desktopheight - iconheight)/2);
        }
        //commented on 12th March 04, due to clients requirement.
       // setExtendedState(Frame.MAXIMIZED_BOTH);
        
//        Added to show the Public Message
        PublicMessage pm = new PublicMessage(this);
        pm.display();
        
        // rdias UCSD - Coeus personalization impl
	    AbstractController persnref = AbstractController.getPersonalizationControllerRef();
	    persnref.customize_mainFrame(getCoeusMenuBar(),
	    		coeusToolBar.getToolBar(), this, "MAINFRAME");
	    //rdias UCSD
    }
    
    /* JM 3-31-2015 16x16 icon */
    public void setCoeus16Icon(Icon coeusIcon){
        this.coeus16Icon = coeusIcon;
    }
    
    public Icon getCoeus16Icon(){
        return coeus16Icon;
    }
    /* JM END */    
    
    /**
     * This method is used to set the custom icon to the application.
     * @param coeusIcon Icon to be used for the application.
     */
    public void setCoeusIcon(Icon coeusIcon){
        this.coeusIcon = coeusIcon;
    }
    
    /**
     * This method is used to get the Icon used for the application.
     *
     * @return coeusIcon Icon used for the application.
     */
    public Icon getCoeusIcon(){
        return coeusIcon;
    }

    /**
     * Set the applet code base.
     *
     * @param codebase URL applet code base.
     */

    public void setAppletCodeBase(URL codebase){
        this.codebase = codebase;
    }

    /**
     * Set the user role id
     *
     * @param roleid integer representing users role.
     */
    public void setRoleID(int roleid){
        this.roleid=roleid;
    }

    /** This method adds a toolbar to the application.
     *
     * @param toolBar JToolBar the toolbar to be added
     */
    public void addToolBar(JToolBar toolBar){
        toolBarPanel.add(toolBar);
        System.out.println("addToolBar called");
        toolBar.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        toolBarPanel.revalidate();
    }
    
    /**
     * Remove toolbar from the application.
     *
     * @param toolBar JToolBar the toolbar to be removed
     */
    public void removeToolBar(JToolBar toolBar){
        toolBarPanel.remove(toolBar);

        contentPane.validate();
    }

    /** This method adds a menu to the application
     *
     * @param coeusMenu CoeusMenu which contains all the sub menu items
     * @param position integer which represents the position of this menu item
     * in the menu bar.
     */
    public void addMenu(CoeusMenu coeusMenu, int position){
        getJMenuBar().add(coeusMenu,position);
    }

    /** This method removes a menu from the application.
     *
     * @param menu JMenu to be removed.
     */
    public void removeMenu(JMenu menu){
        if(menu!=null){
            getJMenuBar().remove(menu);
            getJMenuBar().validate();
        }
    }
    
    /**
     * This method is used to get the CoeusToolBarFactory.
     * @return coeusToolBarFactory CoeusToolBarFactory which consists of all the
     * methods required for adding a toolbar.
     */
    public CoeusToolBarFactory getCoeusToolBarFactory(){
        return coeusToolBarFactory;
    }
    
    /**
     * This method is used to get the CoeusMenuBar.
     *
     * @return coeusMenuBar CoeusMenuBar which consists of all the menus in the
     * application.
     */
    public CoeusMenuBar getCoeusMenuBar(){
        return coeusMenuBar;
    }
    
    /**
     * This method is used to get the desktopPane used in the application.
     * @return desktop reference to the JDesktopPane used in the application.
     */
    public JDesktopPane getDeskTopPane(){
        return desktop;
    }
    
    /**
     * This method is used to get the selected internal frame.
     *
     * @return reference to the selected JInternalFrame object.
     */
    public JInternalFrame getSelectedFrame(){
        return desktop.getSelectedFrame();
    }
    
    /**
     * This method is used to get the default menus items in a collection object.
     *
     * @return defaultMenus Vector which consists of default menus used in the 
     * application.
     */
    public Vector getDefaultMenus(){
        //add the default menus in vector
        defaultMenus = new Vector();
        defaultMenus.addElement(fileMenu);
        defaultMenus.addElement(maintainMenu);
        defaultMenus.addElement(departmentalMenu);
        defaultMenus.addElement(adminMenu);
        defaultMenus.addElement(centralAdminMenu);
        
        defaultMenus.addElement(reportMenu);
        defaultMenus.addElement(windowMenu);
        defaultMenus.addElement(helpMenu);
        return defaultMenus;
    }

    /** This Method is used to get the Window Menu (Coeus Menu ) which contain
     * mandatory menu items like Cascade, Title Horizontal, Title Vertical etc
     * and all the open window items of Coeus module
     * @return <CODE>CoeusWindowMenu</CODE> Window Menu of the Coeus Application.
     */
    public CoeusWindowMenu getWindowMenu(){
        if( coeusWindowMenu != null ){
            return coeusWindowMenu;
        }else{
            return null;
        }
    }

    /** This Method is used to get the File Menu (Coeus Menu ) 
     * @return <CODE>CoeusFileMenu</CODE> File Menu of the Coeus Application.
     */
    public CoeusFileMenu getFileMenu(){
        if( coeusFileMenu != null ){
            return coeusFileMenu;
        }else{
            return null;
        }
    }

    /** This method is used to refresh the Window menu with New open module
     * window or close module window updates
     * @param newWinMenu instance of CoeusWindowMenu with module window updates.
     */
    public void refreshWindowMenu( CoeusWindowMenu newWinMenu ){
        windowMenu = newWinMenu.getMenu();
        windowMenu.revalidate();
    }

    public void refreshReportMenu(){
        reportMenu.revalidate();
    }

    /**
     * This method is used to get the default toolbar used in the application
     * @return defaultToolBar reference to the JToolBar which consists of all
     * the default tools used in the application.
     */
    public JToolBar getDefaultToolBar(){
        return defaultToolBar;
    }
    
    /**
     * This method is used to get the panel used to show the toolbar used in the
     * application.
     *
     * @return toolBarPanel reference to the JPanel which holds the toolbar used
     * in the application.
     */
    public JPanel getToolBarPanel(){
        return toolBarPanel;
    }

    /**
     * Refresh the application menu items.
     *
     * @param menus vector which contains the menus to be included in the menu bar.
     */
    public void refreshMenu(Vector menus){
        getJMenuBar().removeAll();
        getJMenuBar().repaint();
        Object compObject = null;
        for(int i=0;i<menus.size();i++){
            compObject = menus.elementAt(i);
            if ( compObject!=null ){
                getJMenuBar().add( (CoeusMenu) compObject );
            }
        }
        getJMenuBar().validate();
    }

    /** This method refresh the toolbars sent as parameter.
     *
     * @param toolBars Vector which contains the <CODE>JToolBar</CODE>s to be included in the panel
     */
    public void refreshToolBars(Vector toolBars){
        getToolBarPanel().removeAll();
        getToolBarPanel().repaint();
        for(int i=0;i<toolBars.size();i++){
            addToolBar((JToolBar) toolBars.elementAt(i));
        }
    }

    /**
     * This method is used to put the opened CoeusInternalFrame in a hashtable.
     *
     * @param frameName String representing the opened CoeusInternalFrame.
     * @param frame reference to the opened CoeusInternalFrame. 
     */
    public void putFrame(String frameName,CoeusInternalFrame frame){
        frame.setName( frameName );
        //activeFrames.put(frameName,frame);
        duplicateFrames.putFrame(frameName,frame);
    }
    
    /**
     * This method is used to get the internal frame reference for the given
     * frame name.
     *
     * @param frameName String representing the CoeusInternalFrame.
     * @return reference to the CoeusInternalFrame for the given frame name.
     */
    public CoeusInternalFrame getFrame(String frameName){
        //return (CoeusInternalFrame) activeFrames.get(frameName);
        return duplicateFrames.getFrame(frameName);
    }
    
    /**
     * This method is used to remove the internal frame reference from the list
     * of active frames for the given frame name.
     *
     * @param frameName String representing the CoeusInternalFrame to be removed.
     */
    public void removeFrame(String frameName){
        //activeFrames.remove(frameName);
        duplicateFrames.removeFrame(frameName);
    }

    /**
     * This method is used to put the opened InternalFrame in a hashtable.
     *
     * @param frameName String representing the opened InternalFrame.
     * @param frame reference to the opened InternalFrame. 
     */
    public void putFrame(String frameName,JInternalFrame frame){
        frame.setName( frameName );
        activeFrames.put(frameName,frame);
    }

    /**
     * This method is used to get the internal frame reference for the given
     * frame name.
     *
     * @param frameName String representing the InternalFrame.
     * @return reference to the JInternalFrame for the given frame name.
     */
    public JInternalFrame getInternalFrame(String frameName){
        return (JInternalFrame) activeFrames.get(frameName);
    }

    /**
     * This method is used to remove the internal frame reference from the list
     * of active frames for the given frame name.
     *
     * @param frameName String representing the InternalFrame to be removed.
     */
    public void removeInternalFrame(String frameName){
        activeFrames.remove(frameName);
    }

     /**
      * This method is used to set the AppletContext for the application.
      * @param cAppCntxt reference to the AppletContext Object.
      */   
     public void setCoeusAppletContext( AppletContext cAppCntxt ) {

        this.coeusAppletContext = cAppCntxt ;

    }

    /**
     * This method is used to get the AppletContext for the application.
     * @return reference to the AppletContext object.
     */
    public AppletContext getCoeusAppletContext() {

        return coeusAppletContext;

    }

    /**
     * This method is used to show the alert message.
     *
     * @param mesg the message to be displayed
     */
    private void log(String mesg) {
        CoeusOptionPane.showErrorDialog(mesg);
    }

    /**
     * Method to load icon from the applet codebase.
     *
     * @return ImageIcon object which is used as icon throughout the application.
     */
    public ImageIcon getCoeusImageIcon() {   
        return new ImageIcon(getClass().getClassLoader().getResource(CoeusGuiConstants.COEUS_ICON));
        
//        URL url = null; //local variable
//        try {
//            url = new URL(codebase, CoeusGuiConstants.COEUS_ICON);
//        } catch (java.net.MalformedURLException e) {
//            return null;
//        }
//        return (new ImageIcon(url));
    }

    /** Set the login username
     *
     * @param userName String representing the name of the login user.
     */
    public void setUserName(String userName){
        this.userName= (userName == null ? "" : userName.toUpperCase() );
    }
    /** This method is used to get the login userName.
     * @return userName which represents the login user.
     */
    public String getUserName(){
        return userName;
    }
    
    /** This method is used to get the hashtable.
     * @return activeFrames HashMap reference which is used to hold the active
     * frames.
     */
/*    public HashMap getActiveFrames(){
        return activeFrames;
    }
*/
    /**
     * This method is used to close all the opened internal frames.
     * @return boolean true if it is able to close all the internal frames else
     * false.
     */
    public boolean closeInternalFrames(){
        boolean performClose = true;
        try {
            if( !isProposalChildWindowsOpen() ){
                JInternalFrame[] activeInternalFrames = desktop.getAllFrames();
                int totalNumberOfFrames = activeInternalFrames.length;
                for( int indx = 0; indx < totalNumberOfFrames; indx++ ){
                    activeInternalFrames[ indx ].doDefaultCloseAction();
                }
                //check for all the frame have been disposed
                activeInternalFrames = desktop.getAllFrames();
                if( activeInternalFrames.length == 0 ){
                    return true;
                } else {
                    return false;
                }
            }
        }catch(Exception e) {
            performClose = false;
            CoeusOptionPane.showInfoDialog( e.getMessage() );
        }
        return performClose;
        
    }
    
    /** Wrapper method which calls the <CODE>isDuplicate()</CODE> method in
     * <CODE>CheckDuplicateInternalFrame</CODE> class.
     * @see  CheckDuplicateInternalFrame#isDuplicate
     */
    public boolean checkDuplicate(String moduleId, String refId, char mode) 
    throws Exception{
        return duplicateFrames.isDuplicate(moduleId, refId,mode);
    } 
    
    /** To get number of frames open for the given moduleId and mode
     * @return the count of open internal frames in the given mode
     */
    public int getFrameCount(String moduleId, char mode){
        return duplicateFrames.getFrameCount(moduleId, mode);
    }
    
    /** Wrapper method which calls 
     * <CODE>putFrame(String, String, char, CoeusInternalFrame)</CODE> method in
     * <CODE>CheckDuplicateInternalFrame</CODE> class.
     * @see  CheckDuplicateInternalFrame#putFrame
     */
    public void putFrame(String moduleId, String refId, char mode, CoeusInternalFrame frame){
        frame.setName(moduleId + ( (refId != null && refId.length() > 0 
            && mode != COPY_MODE) ? " - " + refId :"" ));
        duplicateFrames.putFrame(moduleId, refId, mode, frame );
    }
    
    /** Wrapper method which calls <CODE>getFrame(String, String)</CODE>
     *  method in <CODE>CheckDuplicateInternalFrame</CODE> class.
     * @see  CheckDuplicateInternalFrame#getFrame
     */
    public CoeusInternalFrame getFrame(String moduleId, String refId){
        return duplicateFrames.getFrame(moduleId, refId);
    }

    
    /** Wrapper method which calls <CODE>getEditingFrame(String)</CODE>
     *  method in <CODE>CheckDuplicateInternalFrame</CODE> class.
     * @see  CheckDuplicateInternalFrame#getEditingFrame
     */
    public CoeusInternalFrame getEditingFrame ( String moduleId ) {
        return duplicateFrames.getEditingFrame ( moduleId );
    }
    
    /** Wrapper method which calls <CODE>removeFrame(String, String)</CODE>
     *  method in <CODE>CheckDuplicateInternalFrame</CODE> class.
     * @see  CheckDuplicateInternalFrame#removeFrame
     */
    public void removeFrame(String moduleId, String refId){
        duplicateFrames.removeFrame(moduleId, refId);
    }
    
    /**
     * Wrapper method which calls <CODE>isProposalChildWindowsOpen</CODE> method
     * in <CODE>CheckDuplicateInternalFrame</CODE> class.
     * @see CheckDuplicateInternalFrame#isProposalChildWindowsOpen
     */
    public boolean isProposalChildWindowsOpen() throws Exception {
        return duplicateFrames.isProposalChildWindowsOpen();
    }
    
    /**
     *  sets UserInfo Details for future retrieval.
     */
    public void setUserInfo(UserInfoBean userInfoBean) {
        userId = userInfoBean.getUserId();
        unitNumber = userInfoBean.getUnitNumber();
        unitName = userInfoBean.getUnitName();
        //userName = userInfoBean.getUserName();
        fullName = userInfoBean.getUserName();
        //setUserName(userName);
    }
    
    public String getUserId()
    {
        return userId;
    }
    
    public String getUnitNumber()
    {
        return unitNumber;
    }
    
    public String getUnitName()
    {
        return unitName;
    }
    
    /** Getter for property fullName.
     * @return Value of property fullName.
     */
    public java.lang.String getFullName() {
        return fullName;
    }
    
    /** Setter for property fullName.
     * @param fullName New value of property fullName.
     */
    public void setFullName(java.lang.String fullName) {
        this.fullName = fullName;
    }
    
   
    public void setCoeusGlobalImageName(java.lang.String coeusGlobalImage) {
        this.coeusGlobalImage = coeusGlobalImage;
    }
    
    /** Getter for property lockIdKey.
     * @return Value of property lockIdKey.
     *
     */
    public java.lang.String getLockIdKey() {
        return lockIdKey;
    }
    
    /** Setter for property lockIdKey.
     * @param lockIdKey New value of property lockIdKey.
     *
     */
    public void setLockIdKey(java.lang.String lockIdKey) {
        this.lockIdKey = lockIdKey;
    }
    
    /** Getter for property updateTimestamp.
     * @return Value of property updateTimestamp.
     *
     */
    public java.lang.String getUpdateTimestamp() {
        return updateTimestamp;
    }
    
    /** Setter for property updateTimestamp.
     * @param updateTimestamp New value of property updateTimestamp.
     *
     */
    public void setUpdateTimestamp(java.lang.String updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }
    
    /** Getter for property locking.
     * @return Value of property locking.
     *
     */
    public java.util.Hashtable getLocking() {
        return locking;
    }
    
    /** Setter for property locking.
     * @param locking New value of property locking.
     *
     */
    public void setLocking(java.util.Hashtable locking) {
        this.locking = locking;       
        /*
            if((locking  != null) && (locking.size() > 0)){
                reminder(locking);
            }*/
    }
    
    /** begin: modified by ravi on 17-02-2003 for providing status bar
     * bug id: #147  */

    /**
     * This method is used to set the message in the status bar.
     *
     * @param message String representing the message to be displayed in status bar.
     */
    
  /*  public void setStatusMessage(String message){
        if(message == null){
            message = "";
        }
        lblStatus.setText(message);
    }
    
    /** end: bug id: #147  */
   
    
    //Added by Ajay to remove lockID from HashTable 
    //in client's side 06-09-2004 Start 1
    
    public Hashtable getLockingData() {
        return getRemindTask().getHtLockIds();
    }
    
    public void setLockingData(Hashtable lockingData) {
        getRemindTask().setHtLockIds(lockingData);
    }    

    public void logout(){
        try {
            RequesterBean requesterBean = new RequesterBean();
            requesterBean.setId("LOGOUT");
            requesterBean.setDataObject(getUserId());
            AppletServletCommunicator appletServletCommunicator = new AppletServletCommunicator();
            appletServletCommunicator.setConnectTo(CoeusGuiConstants.CONNECTION_URL+LOGIN_SERVLET);
            appletServletCommunicator.setRequest(requesterBean);
            appletServletCommunicator.send();
            ResponderBean responderBean = appletServletCommunicator.getResponse();
            if(responderBean!= null){
                if(!responderBean.isSuccessfulResponse()){
                    throw new CoeusException(responderBean.getMessage(), 1);
                }else{
//                    CoeusOptionPane.showInfoDialog("User "+getFullName()+" logged out");
                    return;
                }
            }
        } catch (CoeusException exception) {
            CoeusOptionPane.showErrorDialog(exception.getMessage());
        }
    }
    
    //To enable/disable/make visible menu items depending on the Rights
    //of the logged in user
    public void checkForRights(){
        RequesterBean requester;
        ResponderBean responder;
        
        requester = new RequesterBean();
        Hashtable authorizations = new Hashtable();
        
        AuthorizationBean authorizationBean;
        AuthorizationOperator authorizationOperator;
        
        String OSP_HAS_ANY_RIGHT,GENERATE_SAP_FEED,PROCESS_EOM,SYSTEM_MAINTENANCE;
        
        OSP_HAS_ANY_RIGHT = "OSP_HAS_ANY_RIGHT";
        GENERATE_SAP_FEED = "GENERATE_SAP_FEED";
        PROCESS_EOM = "PROCESS_EOM";
        SYSTEM_MAINTENANCE = "SYSTEM_MAINTENANCE";
        
        // Determine whether user has right 
        
        //Check weather user has any OSP rights if not disable 
        authorizationBean = new AuthorizationBean();
        authorizationBean.setFunctionType(FUNCTION_TYPE_OSP);
        authorizationBean.setPerson(getUserId());
        authorizationOperator = new AuthorizationOperator(authorizationBean);
        authorizations.put(OSP_HAS_ANY_RIGHT, authorizationOperator);
        
        //Check whether the Logged in user has sap feed rights 
        authorizationBean = new AuthorizationBean();
        authorizationBean.setFunction(GENERATE_SAP_FEED);
        authorizationBean.setFunctionType(FUNCTION_TYPE_OSP);
        authorizationBean.setPerson(getUserId());
        authorizationOperator = new AuthorizationOperator(authorizationBean);
        authorizations.put(GENERATE_SAP_FEED, authorizationOperator);
        
        //Check whether the logged in user has EOM rights
        authorizationBean = new AuthorizationBean();
        authorizationBean.setFunction(PROCESS_EOM);
        authorizationBean.setFunctionType(FUNCTION_TYPE_OSP);
        authorizationBean.setPerson(getUserId());
        authorizationOperator = new AuthorizationOperator(authorizationBean);
        authorizations.put(PROCESS_EOM, authorizationOperator);
        
        //Check whether the logged in user has System Maintanance rights
        authorizationBean = new AuthorizationBean();
        authorizationBean.setFunction(SYSTEM_MAINTENANCE);
        authorizationBean.setFunctionType(FUNCTION_TYPE_OSP);
        authorizationBean.setPerson(getUserId());
        authorizationOperator = new AuthorizationOperator(authorizationBean);
        authorizations.put(SYSTEM_MAINTENANCE, authorizationOperator);
        
        String CREATE_PROPOSAL_LOG = "CREATE_PROPOSAL_LOG";
        String MODIFY_PROPOSAL_LOG = "MODIFY_PROPOSAL_LOG";
        String VIEW_PROPOSAL_LOG = "VIEW_PROPOSAL_LOG";
        String CREATE_AWARD = "CREATE_AWARD";
        String MODIFY_AWARD = "MODIFY_AWARD";
        String VIEW_AWARD = "VIEW_AWARD";
        String CREATE_INST_PROPOSAL = "CREATE_INST_PROPOSAL";
        String MODIFY_INST_PROPOSAL = "MODIFY_INST_PROPOSAL";
        String VIEW_INST_PROPOSAL = "VIEW_INST_PROPOSAL";
        String CREATE_AWARD_TEMPLATE = "CREATE_AWARD_TEMPLATE";
        String MODIFY_AWARD_TEMPLATE = "MODIFY_AWARD_TEMPLATE";
        String VIEW_AWARD_TEMPLATE = "VIEW_AWARD_TEMPLATE";
        String CREATE_SUBCONTRACT = "CREATE_SUBCONTRACT";
        String MODIFY_SUBCONTRACT = "MODIFY_SUBCONTRACT";
        String VIEW_SUBCONTRACT = "VIEW_SUBCONTRACT";
        String RELEASE_LOCKS = "RELEASE_LOCKS";
        
        String CREATE_PROTOCOL = "CREATE_PROTOCOL";
        String MODIFY_PROTOCOL = "MODIFY_PROTOCOL";
        
        //New Rights Added for protocol - START - 1
        String MODIFY_ANY_PROTOCOL = "MODIFY_ANY_PROTOCOL";
        String PERFORM_IRB_ACTIONS_ON_PROTO = "PERFORM_IRB_ACTIONS_ON_PROTO";
        String VIEW_ANY_PROTOCOL = "VIEW_ANY_PROTOCOL";
        //New Rights Added for protocol - END - 1
        
        String ADD_COMMITTEE = "ADD_COMMITTEE";
        String MODIFY_COMMITTEE = "MODIFY_COMMITTEE";
        String ADD_SCHEDULE = "ADD_SCHEDULE";
        String MODIFY_SCHEDULE = "MODIFY_SCHEDULE";
        String MAINTAIN_IRB_CORRESP_TEMPLATES = "MAINTAIN_IRB_CORRESP_TEMPLATES";
        String MAINTAIN_PROTOCOL_SUBMISSIONS = "MAINTAIN_PROTOCOL_SUBMISSIONS";
        //Added for COEUSDEV-222 : Some IRB-related rights not working correctly. - Start
        String VIEW_COMMITTEE = "VIEW_COMMITTEE";
        String VIEW_SCHEDULE = "VIEW_SCHEDULE";
        //COEUSDEV-222 : End
        //Added For IACUC Protocol Right MENUS-Start
         String CREATE_IACUC_PROTOCOL = "CREATE_IACUC_PROTOCOL";
         String VIEW_ANY_IACUC_PROTOCOL = "VIEW_ANY_IACUC_PROTOCOL";
         String MODIFY_ANY_IACUC_PROTOCOL = "MODIFY_ANY_IACUC_PROTOCOL";
         String MAINTAIN_IACUC_PROTO_SUBMISION = "MAINTAIN_IACUC_PROTO_SUBMISION";
         String PERFORM_IACUC_ACTIONS_ON_PROTO = "PERFORM_IACUC_ACTIONS_ON_PROTO";
         String MAINTAIN_IACUC_CORR_TEMPLATES = "MAINTAIN_IACUC_CORR_TEMPLATES";
         String MODIFY_IACUC_PROTOCOL = "MODIFY_IACUC_PROTOCOL";
        //Added For IACUC Protocol Right MENUS-End
         // Added for COEUSQA-1692_User Access - Maintenance_start
         String MAINTAIN_USER_ROLES = "MAINTAIN_USER_ROLES";
         // Added for COEUSQA-1692_User Access - Maintenance_end
         // Modified and Added for COEUSQA-1692_User Access - Maintenance_start
        String args[] = {CREATE_PROPOSAL_LOG , MODIFY_PROPOSAL_LOG , VIEW_PROPOSAL_LOG ,
                          CREATE_AWARD , MODIFY_AWARD , VIEW_AWARD , 
                          CREATE_INST_PROPOSAL , MODIFY_INST_PROPOSAL , VIEW_INST_PROPOSAL ,
                          CREATE_AWARD_TEMPLATE , MODIFY_AWARD_TEMPLATE , VIEW_AWARD_TEMPLATE ,
                          CREATE_SUBCONTRACT , MODIFY_SUBCONTRACT , VIEW_SUBCONTRACT,RELEASE_LOCKS ,
                          CREATE_PROTOCOL , MODIFY_PROTOCOL ,                  
                          //New Rights Added for protocol - START - 2
                          MODIFY_ANY_PROTOCOL, PERFORM_IRB_ACTIONS_ON_PROTO, VIEW_ANY_PROTOCOL, 
                          //New Rights Added for protocol - START - 2
                          VIEW_COMMITTEE,ADD_COMMITTEE , MODIFY_COMMITTEE , 
                          VIEW_SCHEDULE,ADD_SCHEDULE , MODIFY_SCHEDULE  ,
                          MAINTAIN_IRB_CORRESP_TEMPLATES,  
                          MAINTAIN_PROTOCOL_SUBMISSIONS,
                         //Added For IACUC Protocol Right MENUS-Start
                         CREATE_IACUC_PROTOCOL,VIEW_ANY_IACUC_PROTOCOL,MODIFY_ANY_IACUC_PROTOCOL,
                         MAINTAIN_IACUC_PROTO_SUBMISION,PERFORM_IACUC_ACTIONS_ON_PROTO,
                         MAINTAIN_IACUC_CORR_TEMPLATES,MODIFY_IACUC_PROTOCOL,
                         //Added For IACUC Protocol Right MENUS-End                         
                         MAINTAIN_USER_ROLES};
        // Modified and Added for COEUSQA-1692_User Access - Maintenance_end
        
        for(int index = 0; index < args.length ; index++){
            // Determine whether user has right to modify 
            authorizationBean = new AuthorizationBean();
            authorizationBean.setFunction(args[index]);
            authorizationBean.setFunctionType(FUNCTION_TYPE_OSP);
            authorizationBean.setPerson(getUserId());
            authorizationOperator = new AuthorizationOperator(authorizationBean);
            authorizations.put(args[index], authorizationOperator);
            
            // Determine whether user has right to modify 
            authorizationBean = new AuthorizationBean();
            authorizationBean.setFunction(args[index]);
            authorizationBean.setFunctionType(FUNCTION_TYPE_OSP);
            authorizationBean.setPerson(getUserId());
            authorizationOperator = new AuthorizationOperator(authorizationBean);
            authorizations.put(args[index], authorizationOperator);
            
            // Determine whether user has right to display
            authorizationBean = new AuthorizationBean();
            authorizationBean.setFunction(args[index]);
            authorizationBean.setFunctionType("RIGHT_ID");
            authorizationBean.setPerson(getUserId());
            authorizationOperator = new AuthorizationOperator(authorizationBean);
            authorizations.put(args[index], authorizationOperator);
        }//End for
        
        //Check if the user have any rights to financial interest disclosure 
        String MAINTAIN_CONFLICT_OF_INTEREST,VIEW_CONFLICT_OF_INTEREST;
        
        MAINTAIN_CONFLICT_OF_INTEREST = "MAINTAIN_CONFLICT_OF_INTEREST";
        VIEW_CONFLICT_OF_INTEREST = "VIEW_CONFLICT_OF_INTEREST";
        
        // Determine whether user has right to MAINTAIN_CONFLICT_OF_INTEREST
        authorizationBean = new AuthorizationBean();
        authorizationBean.setFunction(MAINTAIN_CONFLICT_OF_INTEREST);
        authorizationBean.setFunctionType(FUNCTION_TYPE_OSP);
        authorizationBean.setPerson(getUserId());
        authorizationOperator = new AuthorizationOperator(authorizationBean);
        authorizations.put(MAINTAIN_CONFLICT_OF_INTEREST, authorizationOperator);
        
        // Determine whether user has right to VIEW_CONFLICT_OF_INTEREST
        authorizationBean = new AuthorizationBean();
        authorizationBean.setFunction(VIEW_CONFLICT_OF_INTEREST);
        authorizationBean.setFunctionType(FUNCTION_TYPE_OSP);
        authorizationBean.setPerson(getUserId());
        authorizationOperator = new AuthorizationOperator(authorizationBean);
        authorizations.put(VIEW_CONFLICT_OF_INTEREST, authorizationOperator);
        
        
        //Check if the user has MAINTAIN_REPORTING right 
        String MAINTAIN_REPORTING;
        MAINTAIN_REPORTING = "MAINTAIN_REPORTING";
        
        authorizationBean = new AuthorizationBean();
        authorizationBean.setFunction(MAINTAIN_REPORTING);
        // 3587: Multi Campus enhancements - Start
//        authorizationBean.setFunctionType(FUNCTION_TYPE_OSP);
        authorizationBean.setFunctionType("RIGHT_ID");
        // 3587: Multi Campus enhancements - End
        authorizationBean.setPerson(getUserId());
        authorizationOperator = new AuthorizationOperator(authorizationBean);
        authorizations.put(MAINTAIN_REPORTING, authorizationOperator);
        
        // Check if the user has role id
        String ROLE_ID = "1";
        authorizationBean = new AuthorizationBean();
        authorizationBean.setFunction(ROLE_ID);
        authorizationBean.setFunctionType("ROLE");
        authorizationBean.setPerson(getUserId());
        authorizationOperator = new AuthorizationOperator(authorizationBean);
        authorizations.put(ROLE_ID, authorizationOperator);
        
        // Check if the user has unit delegation right
        String MODIFY_DELEGATIONS;
        MODIFY_DELEGATIONS = "MODIFY_DELEGATIONS";
        
        authorizationBean = new AuthorizationBean();
        authorizationBean.setFunction(MODIFY_DELEGATIONS);
        authorizationBean.setFunctionType("RIGHT");
        authorizationBean.setPerson(getUserId());
        authorizationBean.setQualifier(getUnitNumber());
        authorizationBean.setQualifierType("UNIT");
        authorizationOperator = new AuthorizationOperator(authorizationBean);
        authorizations.put(MODIFY_DELEGATIONS, authorizationOperator);
        
        //Check if the user have any rights for Populate Subcontract Expense Data
        String POPULATE_SUBCONTRACT_EXP;
        POPULATE_SUBCONTRACT_EXP = "POPULATE_SUBCONTRACT_EXP";
        
        authorizationBean = new AuthorizationBean();
        authorizationBean.setFunction(POPULATE_SUBCONTRACT_EXP);
        authorizationBean.setFunctionType(FUNCTION_TYPE_OSP);
        authorizationBean.setPerson(getUserId());
        authorizationOperator = new AuthorizationOperator(authorizationBean);
        authorizations.put(POPULATE_SUBCONTRACT_EXP, authorizationOperator);
        
        
        requester.setAuthorizationOperators(authorizations);
        requester.setIsAuthorizationRequired(true);
        
        AppletServletCommunicator comm = new AppletServletCommunicator(CoeusGuiConstants.CONNECTION_URL + AUTH_SERVLET, requester);
        
        comm.send();
        responder = comm.getResponse();
        if(responder.isSuccessfulResponse()){
            authorizations = responder.getAuthorizationOperators();
        }else{
            CoeusOptionPane.showInfoDialog(responder.getMessage());
        }
        
        /* JM 3-23-2015 update last login table */
        boolean loginUpdated = false;
        RequesterBean vuRequester = new RequesterBean();
        ResponderBean vuResponder;
        final char UPDATE_LAST_LOGIN = 'L';
        final String VU_SERVLET = "/CustomFunctionsServlet";
        
        vuRequester.setFunctionType(UPDATE_LAST_LOGIN);
        vuRequester.setId(userId);
        vuRequester.setDataObject(VERSION);
        AppletServletCommunicator vuComm = new AppletServletCommunicator(CoeusGuiConstants.CONNECTION_URL + VU_SERVLET, vuRequester);
        
        vuComm.send();
        vuResponder = vuComm.getResponse();
        if (vuResponder.isSuccessfulResponse()){
        	loginUpdated = (Boolean) vuResponder.getDataObject();
        	if (loginUpdated) {
        		// do nothing - all is good
        	}
        	else {
                System.out.println("Unable to update last login information");        		
        	}
        }
        else {
            System.out.println("No database response for last login information");
        }
        /* JM END */
        
        boolean ospHasAnyRight = ((Boolean)authorizations.get(OSP_HAS_ANY_RIGHT)).booleanValue();
        boolean generateSAPFeed = ((Boolean)authorizations.get(GENERATE_SAP_FEED)).booleanValue();
        boolean processEOM = ((Boolean)authorizations.get(PROCESS_EOM)).booleanValue();
        boolean systemMaintenance = ((Boolean)authorizations.get(SYSTEM_MAINTENANCE)).booleanValue();
        
        boolean createProposalLog = ((Boolean)authorizations.get(CREATE_PROPOSAL_LOG)).booleanValue();
        boolean modifyProposalLog = ((Boolean)authorizations.get(MODIFY_PROPOSAL_LOG)).booleanValue();
        boolean viewProposalLog = ((Boolean)authorizations.get(VIEW_PROPOSAL_LOG)).booleanValue();
        
        boolean createAward = ((Boolean)authorizations.get(CREATE_AWARD)).booleanValue();
        boolean modifyAward = ((Boolean)authorizations.get(MODIFY_AWARD)).booleanValue();
        boolean viewAward = ((Boolean)authorizations.get(VIEW_AWARD)).booleanValue();
        
        boolean createInstProp = ((Boolean)authorizations.get(CREATE_INST_PROPOSAL)).booleanValue();
        boolean modifyInstProp = ((Boolean)authorizations.get(MODIFY_INST_PROPOSAL)).booleanValue();
        boolean viewInstProp = ((Boolean)authorizations.get(VIEW_INST_PROPOSAL)).booleanValue();
        
        boolean createAwardTemplate = ((Boolean)authorizations.get(CREATE_AWARD_TEMPLATE)).booleanValue();
        boolean modifyAwardTemplate = ((Boolean)authorizations.get(MODIFY_AWARD_TEMPLATE)).booleanValue();
        boolean viewAwardTemplate = ((Boolean)authorizations.get(VIEW_AWARD_TEMPLATE)).booleanValue();
        
        boolean createSubContract = ((Boolean)authorizations.get(CREATE_SUBCONTRACT)).booleanValue();
        boolean modifySubContract = ((Boolean)authorizations.get(MODIFY_SUBCONTRACT)).booleanValue();
        boolean viewSubContract = ((Boolean)authorizations.get(VIEW_SUBCONTRACT)).booleanValue();
        boolean viewLocks = ((Boolean)authorizations.get(RELEASE_LOCKS)).booleanValue();
        
        boolean maintainConfilctOfIntrest = ((Boolean)authorizations.get(MAINTAIN_CONFLICT_OF_INTEREST)).booleanValue();
        boolean viewConfilctOfIntrest = ((Boolean)authorizations.get(VIEW_CONFLICT_OF_INTEREST)).booleanValue();

        boolean maintainReporting = ((Boolean)authorizations.get(MAINTAIN_REPORTING)).booleanValue() || viewAward;
        
        boolean roleId = ((Boolean)authorizations.get(ROLE_ID)).booleanValue();
        
        boolean modifyDelegations = ((Boolean)authorizations.get(MODIFY_DELEGATIONS)).booleanValue();
        
        boolean populateSubcontract = ((Boolean)authorizations.get(POPULATE_SUBCONTRACT_EXP)).booleanValue();
        
        // added for enhancement 
        boolean createProtocol = ((Boolean)authorizations.get(CREATE_PROTOCOL)).booleanValue();
        boolean modifyProtocol = ((Boolean)authorizations.get(MODIFY_PROTOCOL)).booleanValue();
        //New Rights Added for protocol - START - 3
        boolean modifyAnyProtocol = ((Boolean)authorizations.get(MODIFY_ANY_PROTOCOL)).booleanValue();
        boolean performIrbActionsOnProto = ((Boolean)authorizations.get(PERFORM_IRB_ACTIONS_ON_PROTO)).booleanValue();
        boolean viewAnyProtocol = ((Boolean)authorizations.get(VIEW_ANY_PROTOCOL)).booleanValue();
        //New Rights Added for protocol - START - 3
        
        boolean addCommittee = ((Boolean)authorizations.get(ADD_COMMITTEE)).booleanValue();
        boolean modifyCommittee = ((Boolean)authorizations.get(MODIFY_COMMITTEE)).booleanValue();
        //Added COEUSDEV-222 : Some IRB-related rights not working correctly.
        //Enable committee toolbar button and menu item,if user has VIEW_COMMITEE right
        boolean viewCommittee = false;
        if(!modifyCommittee){
            viewCommittee = ((Boolean)authorizations.get(VIEW_COMMITTEE)).booleanValue();
        }
        //COEUSDEV-222:End
        boolean addSchedule = ((Boolean)authorizations.get(ADD_SCHEDULE)).booleanValue();
        boolean modifySchedule = ((Boolean)authorizations.get(MODIFY_SCHEDULE)).booleanValue();
        //Added COEUSDEV-222 : Some IRB-related rights not working correctly.
        //Enable schedule toolbar button and menu item,if user has VIEW_SCHEDULE right
        boolean viewSchedule = false;
        if(!modifySchedule){
            viewSchedule = ((Boolean)authorizations.get(VIEW_SCHEDULE)).booleanValue();
        }
        //COEUSDEV-222:End
        boolean maintainCorrespondence = ((Boolean)authorizations.get(MAINTAIN_IRB_CORRESP_TEMPLATES)).booleanValue();
        boolean maintainProtocolSubmissions = ((Boolean)authorizations.get(MAINTAIN_PROTOCOL_SUBMISSIONS)).booleanValue();
         
//        System.out.println("CREATE_AWARD" + createAward);
//        System.out.println("MODIFY_AWARD" + modifyAward);
//        System.out.println("VIEW_AWARD" + viewAward);
//        System.out.println("MAINTAIN_REPORTING" + maintainReporting);
        //Added For IACUC Protocol Right MENUS-Start
         boolean createIacucProtocol = ((Boolean)authorizations.get(CREATE_IACUC_PROTOCOL)).booleanValue();
         boolean viewAnyIacucProtocol = ((Boolean)authorizations.get(VIEW_ANY_IACUC_PROTOCOL)).booleanValue();
         boolean modifyAnyIacucProtocol = ((Boolean)authorizations.get(MODIFY_ANY_IACUC_PROTOCOL)).booleanValue();
         boolean maintainIacucProtocolSubmissions = ((Boolean)authorizations.get(MAINTAIN_IACUC_PROTO_SUBMISION)).booleanValue();
         boolean performIacucActionsOnProto = ((Boolean)authorizations.get(PERFORM_IACUC_ACTIONS_ON_PROTO)).booleanValue();
         boolean maintainIacucCorrespondence = ((Boolean)authorizations.get(MAINTAIN_IACUC_CORR_TEMPLATES)).booleanValue();
         boolean modifyIacucProtocol = ((Boolean)authorizations.get(MODIFY_IACUC_PROTOCOL)).booleanValue();
        //Added For IACUC Protocol Right MENUS-End
        // Added for COEUSQA-1692_User Access - Maintenance_start
         boolean maintainUserRoles = ((Boolean)authorizations.get(MAINTAIN_USER_ROLES)).booleanValue();
         if(!maintainUserRoles){
             coeusAdminMenu.userRolesMaintenance.setEnabled(false);
         }
         // Added for COEUSQA-1692_User Access - Maintenance_end
        //Disable central admin menu
        if(!ospHasAnyRight){
            centralAdminMenu.setVisible(false);
        }
        
         //Disable Generate Master Data Feed, 
        //Generate Sponsor Feed, Feed Maintenance, Generate Rolodex Feed menu items.
        coeusCentralAdminMenu.generateMasterDataFeed.setEnabled(generateSAPFeed);
        coeusCentralAdminMenu.generateSponsorFeed.setEnabled(generateSAPFeed);    
        coeusCentralAdminMenu.GenerateRolodexFeed.setEnabled(generateSAPFeed);
        coeusCentralAdminMenu.feedMaintenance.setEnabled(generateSAPFeed);
        
        //Disable End Of Month Process
        coeusCentralAdminMenu.endOfMonthProcess.setEnabled(processEOM);
        
        //Disable Protocol
        //Modified for COEUSQA-2314 : IRB Admin should have ability to assign committee based on lead unit of the protocol - Start
//        if(!createProtocol && !modifyProtocol && !modifyAnyProtocol && !performIrbActionsOnProto && !viewAnyProtocol ){
        /* JM 05-02-2013
        if(!createProtocol && !modifyProtocol && !modifyAnyProtocol && !performIrbActionsOnProto && !viewAnyProtocol 
                && !maintainProtocolSubmissions){//COEUSQA-2314 : End
            coeusMaintainMenu.protocol.setEnabled(false);
            coeusToolBar.irbProtocol.setEnabled(false);
        }
        */
         //Disable Commitee
        //Modified for COEUSDEV-222 : Some IRB-related rights not working correctly. - Start
        //Committee toolbar button and menuItem will disabled only if user doesn't have ADD_COMMITTEE,MODIFY_COMMITTEE and VIEW_COMMITTEE right
//        if(!addCommittee && !modifyCommittee){
        /* JM 05-02-2013
        if(!addCommittee && !modifyCommittee && !viewCommittee){//COEUSDEV-222 : End
            coeusMaintainMenu.committee.setEnabled(false);
            coeusToolBar.committee.setEnabled(false);
        } */
        
         //Disable Schedule 
        //Modified for COEUSDEV-222 : Some IRB-related rights not working correctly.
        //Schedule toolbar button and menuItem will disabled only if user doesn't have ADD_SCHEDULE,MODIFY_SCHEDULE and VIEW_SCHEDULE right
//         if(!addSchedule && !modifySchedule)
        /* JM 05-02-2013
        if(!addSchedule && !modifySchedule  && !viewSchedule){//COEUSDEV-222: End
            coeusMaintainMenu.schedule.setEnabled(false);
            coeusToolBar.schedule.setEnabled(false);  
        } */
        
        //Disable Correspondence
        /* JM 05-02-2013
        if(!maintainCorrespondence){
            coeusMaintainMenu.irbCorrespondence.setEnabled(false);          
        }
        //Disable ProtocolSubmissions
        if(!maintainProtocolSubmissions){
            coeusMaintainMenu.irbProtocolSubmission.setEnabled(false);
            coeusToolBar.irbProtocolSubmission.setEnabled(false);  
        }*/
        
        
        //Disable Parameter Maintenance.
        coeusCentralAdminMenu.parameter.setEnabled(roleId);
        
        // Disable Investigator Mass Change,
        //Rtf Forms, New Public Message
        coeusCentralAdminMenu.investigatorMaskChange.setEnabled(systemMaintenance);
        coeusCentralAdminMenu.subcontractForms.setEnabled(systemMaintenance);
        coeusCentralAdminMenu.newPublicMessage.setEnabled(systemMaintenance);
        
        //Disable Proposal Log 
        if(!createProposalLog && !modifyProposalLog && !viewProposalLog){
            coeusMaintainMenu.proposalsLog.setEnabled(false);
        }
       //Code commented for Case#3388 - Implementing authorization check at department level        
        //Disable Award
//        if(!createAward && !modifyAward && !viewAward){
//            coeusMaintainMenu.awards.setEnabled(false);
//            coeusToolBar.awards.setEnabled(false);
//        }
        
        //Disable Award Reporting
        coeusMaintainMenu.awardReportingRequirements.setEnabled(maintainReporting);
          //Code commented for Case#3388 - Implementing authorization check at department level
         //Disable Institute Proposal(Proposal)
//        if(!createInstProp && !modifyInstProp && !viewInstProp){
//            coeusMaintainMenu.proposals.setEnabled(false);
//            coeusToolBar.proposal.setEnabled(false);
//        }
        
        //Disable Award Template
        if(!createAwardTemplate && !modifyAwardTemplate && !viewAwardTemplate){
            coeusAdminMenu.awardTemplate.setEnabled(false);
        }
        
        //Disable Sub Contract
        if(!createSubContract && !modifySubContract && !viewSubContract){
            coeusMaintainMenu.subcontract.setEnabled(false);
            coeusToolBar.subContract.setEnabled(false);
        }
        
        if(!viewLocks){
            coeusCentralAdminMenu.currentLock.setVisible(false);
        }
        
        //Disable Financial Intrest Disclosure
        if(!maintainConfilctOfIntrest && !viewConfilctOfIntrest){
            coeusMaintainMenu.financialInterestDisclosure.setVisible(false);
        }
        
        //Disable User Delegations
        coeusDepartmentalMenu.userDeligation.setEnabled(modifyDelegations);    
        
        boolean parameterValue = getParameterValueFromServer();
        
        //Disable/Visble Populate Subcontract Expense Data
        if(!parameterValue){
            coeusCentralAdminMenu.populateSubcontractExpenseData.setEnabled(false);
            coeusCentralAdminMenu.populateSubcontractExpenseData.setVisible(false);
        }else{
            coeusCentralAdminMenu.populateSubcontractExpenseData.setEnabled(populateSubcontract);
        }
         //Added For IACUC Protocol Right MENUS-Start
        /* JM 05-02-2013
        if(!createIacucProtocol && !modifyIacucProtocol && !modifyAnyIacucProtocol && !performIacucActionsOnProto && !viewAnyIacucProtocol 
                && !maintainIacucProtocolSubmissions){//COEUSQA-2314 : End
            coeusMaintainMenu.iacucProtocol.setEnabled(false);
           //Added for case id COEUSQA-2717 icons for IACUC to Coeus Premium start
            coeusToolBar.iacucProtocol.setEnabled(false);
           //Added for case id COEUSQA-2717 icons for IACUC to Coeus Premium end
        }
        
        if(!maintainIacucProtocolSubmissions){
            coeusMaintainMenu.iacucProtocolSubmissions.setEnabled(false);
            //Added for case id COEUSQA-2717 icons for IACUC to Coeus Premium start
            coeusToolBar.iacucProtocolSubmission.setEnabled(false);
            //Added for case id COEUSQA-2717 icons for IACUC to Coeus Premium end
        }
        if(!maintainIacucCorrespondence){
            coeusMaintainMenu.iacucCorrespondence.setEnabled(false);
        }
        */
         //Added For IACUC Protocol Right MENUS-Start
    }//End checkForRights
    
    private boolean getParameterValueFromServer(){
        RequesterBean requester;
        ResponderBean responder;
        
        requester = new RequesterBean();
        requester.setFunctionType('A');
        requester.setDataObject("SUBCONTRACT_EXP_FEED_ENABLED");
        
        AppletServletCommunicator comm = new AppletServletCommunicator(CoeusGuiConstants.CONNECTION_URL + UTILITY_SERVLET, requester);
        comm.send();
        responder = comm.getResponse();
        if(responder.isSuccessfulResponse()){
            String value = (String)responder.getDataObject();
            if(value.equals("1")){
                return true;
            }else{
                return false;
            }
        }else{
            CoeusOptionPane.showInfoDialog(responder.getMessage());
        }
        return false;
    }
    
    /** Getter for property remindTask.
     * @return Value of property remindTask.
     *
     */
    public edu.mit.coeus.utils.locking.RemindTask getRemindTask() {
        remindTask = RemindTask.getInstance();
        return remindTask;
    }
    
    /** Setter for property remindTask.
     * @param remindTask New value of property remindTask.
     *
     */
    public void setRemindTask(edu.mit.coeus.utils.locking.RemindTask remindTask) {
        this.remindTask = remindTask;
    }
    
    /** This is a class to implement the Public Message.
     * This will show the public message for the application,unless the user checks
     * the option to disable this feature.
     */
    public class PublicMessage implements ActionListener {

        private CoeusMessageForm coeusMessageForm;
        public CoeusDlgWindow dlgMessage;
        private final String PUBLIC_MESSAGE = "Public Message";
        private final String TITLE = "Coeus Message";
        private final String SERVLET = "/CentralAdminMaintenanceServlet";
        private final String connectTo = CoeusGuiConstants.CONNECTION_URL+ SERVLET;
        private static final int WIDTH = 470;
        private static final int HEIGHT = 350;
        private String strMessageID = "";
        private HashMap data = null;
        private UserPreferencesBean userPreferencesBean = null;

        
        public PublicMessage(CoeusAppletMDIForm mdiForm){
            coeusMessageForm = new CoeusMessageForm();
            dlgMessage = new CoeusDlgWindow(mdiForm);
            postInitComponents();
            
            coeusMessageForm.btnOK.addActionListener(this);
            java.awt.Component[] components = {coeusMessageForm.btnOK,coeusMessageForm.chkMessage};
            ScreenFocusTraversalPolicy traversalPolicy = new ScreenFocusTraversalPolicy(components);
            coeusMessageForm.setFocusTraversalPolicy(traversalPolicy);
            coeusMessageForm.setFocusCycleRoot(true);
            
        }
        private void postInitComponents() {
            dlgMessage.getContentPane().add(coeusMessageForm);
            dlgMessage.setResizable(false);
            dlgMessage.setModal(true);
            dlgMessage.setFont(CoeusFontFactory.getLabelFont());
            dlgMessage.setSize(WIDTH, HEIGHT);
            Dimension dlgLocation = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension dlgMessageSize = dlgMessage.getSize();
            dlgMessage.setLocation((dlgLocation.width/2 - dlgMessageSize.width/2), (dlgLocation.height/2 - dlgMessageSize.height/2));
            dlgMessage.setTitle(TITLE);

            dlgMessage.addEscapeKeyListener(
            new AbstractAction("escPressed"){
                public void actionPerformed(ActionEvent ae) {
                    performCancelAction();
                    return;
                }
            });
            dlgMessage.setDefaultCloseOperation(CoeusDlgWindow.DO_NOTHING_ON_CLOSE);
            dlgMessage.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    performCancelAction();
                    return;
                }
            });
            dlgMessage.addComponentListener(
            new ComponentAdapter(){
                public void componentShown(ComponentEvent e){
                    requestDefaultFocus();
                }
            });

        }
        /**
         * setting up the default focus
         * @return void
         */
        public void requestDefaultFocus(){
            coeusMessageForm.txtArMessage.setCaretPosition(0);
            coeusMessageForm.btnOK.requestFocus();
        }
        public void display(){
            getData();
            if(checkStatus()){
                setData();
                dlgMessage.setVisible(true);
            }
        }
        private void performCancelAction(){
            dlgMessage.setVisible(false);
        }
        private void performOKOperation(){
//            To check whether the checkbox is selected for not displaying the  
//            message for the next time login . If so update the table.
            if(coeusMessageForm.chkMessage.isSelected()){
                try {
                    if(userPreferencesBean == null){
                        userPreferencesBean = new UserPreferencesBean();
                        userPreferencesBean.setAcType(TypeConstants.INSERT_RECORD);
                        userPreferencesBean.setUserId(getUserId());
                        userPreferencesBean.setVariableName(PUBLIC_MESSAGE);
                        userPreferencesBean.setVarValue(strMessageID);
                    }else{
                        userPreferencesBean.setAcType(TypeConstants.UPDATE_RECORD);
                        userPreferencesBean.setVarValue(strMessageID);
                    }
                    java.util.Vector userPreferences = new java.util.Vector();
                    userPreferences.add(userPreferencesBean);
                    
                    RequesterBean requesterBean = new RequesterBean();
                    requesterBean.setFunctionType('Y');
                    requesterBean.setDataObject(userPreferences);
                    AppletServletCommunicator appletServletCommunicator = new AppletServletCommunicator();
                    appletServletCommunicator.setConnectTo(connectTo);
                    appletServletCommunicator.setRequest(requesterBean);
                    appletServletCommunicator.send();
                    ResponderBean responderBean = appletServletCommunicator.getResponse();
                    if(responderBean!= null){
                        if(!responderBean.isSuccessfulResponse()){
                            throw new CoeusException(responderBean.getMessage(), 1);
                        }
                    }
                } catch (CoeusException exception) {
                    CoeusOptionPane.showErrorDialog(exception.getMessage());
                }
            }
            performCancelAction();
        }
        public void actionPerformed(ActionEvent actionEvent) {
            try{
                if (actionEvent.getSource().equals(coeusMessageForm.btnOK)) {
                    dlgMessage.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                    performOKOperation();
                }
            }catch(Exception exception){
                exception.printStackTrace();
            }finally{
                dlgMessage.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
        
        private boolean checkStatus(){
            if(data != null){
                strMessageID = (String)data.get("MESSAGE_ID");
            }
            if(strMessageID != null && !strMessageID.trim().equals("0")){
//          check for "Public Message" property in UserPreference table
                java.util.Vector userPreferenceVector = (java.util.Vector)data.get(UserPreferencesBean.class);
                if(userPreferenceVector != null){
                    for(int i=0;i<userPreferenceVector.size();i++){
                        UserPreferencesBean bean = (UserPreferencesBean)userPreferenceVector.elementAt(i);
                        if(bean != null && bean.getVariableName().equals(PUBLIC_MESSAGE)){
                            userPreferencesBean = bean;
                            break;
                        }
                    }
                }
                if(userPreferencesBean != null){
                    String seenMessageID = userPreferencesBean.getVarValue();
                    if(seenMessageID == null || seenMessageID.trim().equals("0")){
                        return true;
                    }else if(strMessageID.trim().equals(seenMessageID.trim())){
                        return false;
                    }
                }
                return true;
            }else{
                return false;
            }
        }
        private void getData(){
            try {
                RequesterBean requesterBean = new RequesterBean();
                requesterBean.setFunctionType('X');
                requesterBean.setDataObject(getUserId());
                AppletServletCommunicator appletServletCommunicator = new AppletServletCommunicator();
                appletServletCommunicator.setConnectTo(connectTo);
                appletServletCommunicator.setRequest(requesterBean);
                appletServletCommunicator.send();
                ResponderBean responderBean = appletServletCommunicator.getResponse();
                if(responderBean!= null){
                    if(!responderBean.isSuccessfulResponse()){
                        throw new CoeusException(responderBean.getMessage(), 1);
                    }else{
                        data = (HashMap)responderBean.getDataObject();
                    }
                }
            } catch (CoeusException exception) {
                CoeusOptionPane.showErrorDialog(exception.getMessage());
            }
        }
        private void setData(){
            if(data != null){
                MessageBean messageBean = (MessageBean)data.get(MessageBean.class);
                if(messageBean != null){
                    coeusMessageForm.txtArMessage.setText(messageBean.getMessage());
                }
            }
        }
    }
    
    //Method for adding the opened Award Templates to the HashSet
    public void addToHashSet(Integer templateCode){
        if(templateSet == null){
            templateSet = new HashSet();
        }
        templateSet.add(templateCode);
    }
    
    //Method for deleting the opened Award Templates in the HashSet
    public void deleteHashSetVal(Integer templateCode){
        if(templateSet.contains(templateCode)){
            templateSet.remove(templateCode);
        }
    }
    
    public HashSet getHashSet(){
        return templateSet;
    }
    
    public void removeHashSet(){
        templateSet = null;
    }
    
    /**
     * Getter for property instanceName.
     * @return Value of property instanceName.
     */
    public java.lang.String getInstanceName() {
        return instanceName;
    }
    
    /**
     * Setter for property instanceName.
     * @param instanceName New value of property instanceName.
     */
    public void setInstanceName(java.lang.String instanceName) {
        this.instanceName = instanceName;
    }
    
    /**
     * Getter for property swingLoginMode.
     * @return Value of property swingLoginMode.
     */
    public java.lang.String getSwingLoginMode() {
        return swingLoginMode;
    }
    
    /**
     * Setter for property swingLoginMode.
     * @param swingLoginMode New value of property swingLoginMode.
     */
    public void setSwingLoginMode(java.lang.String swingLoginMode) {
        this.swingLoginMode = swingLoginMode;
    }

    public byte[] getAuthSignature() {
        return authSignature;
    }

    public void setAuthSignature(byte[] authSignature) {
        this.authSignature = authSignature;
    }

    public void putController(edu.mit.coeus.gui.event.Controller controller) {
        duplicateFrames.putController(controller);
    }
    
    public edu.mit.coeus.gui.event.Controller getController(Object uiClass) {
        return duplicateFrames.getController(uiClass);
    }
}//End Class

