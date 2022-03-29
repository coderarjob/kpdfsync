package coderarjob.kpdfsync.poc;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

import coderarjob.kpdfsync.poc.Log.LogType;

public class Main
{
  public static void setUIFont (FontUIResource f){
    java.util.Enumeration keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      Object value = UIManager.get (key);
      if (value instanceof FontUIResource)
        UIManager.put (key, f);
    }
  }

  /* Class methods */
  public static void main(String[] args) throws Exception
  {

    String logMessage =  "Kpdfsync started%n";
           logMessage +=  "Kpdfsync - version: %s%n";
           logMessage += "libkpdfsync - version %s%n";
           logMessage += "ajl - version %s%n%n";

    String guiVersion = Config.getInstance().readSetting ("app.version");
    String libVersion = coderarjob.kpdfsync.lib.Config.getInstance().readSetting ("app.version");
    String ajlVersion = coderarjob.ajl.Config.getInstance().readSetting ("app.version");

    Log.getInstance().log (LogType.INFORMATION, logMessage, guiVersion, libVersion, ajlVersion);

    JFrame.setDefaultLookAndFeelDecorated (true);
    JDialog.setDefaultLookAndFeelDecorated (true);
    setUIFont (new FontUIResource ("Dialog", FontUIResource.PLAIN, 11));

    MainFrame mainFrame = new MainFrame();
    mainFrame.setVisible (true);
  }
}
