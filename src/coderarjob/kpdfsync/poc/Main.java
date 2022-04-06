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
    logBasicInformation();

    JFrame.setDefaultLookAndFeelDecorated (true);
    JDialog.setDefaultLookAndFeelDecorated (true);
    setUIFont (new FontUIResource ("Dialog", FontUIResource.PLAIN, 11));

    MainFrame mainFrame = new MainFrame();
    mainFrame.setVisible (true);
  }

  private static void logBasicInformation()
  {
    Log.getInstance().log (LogType.INFORMATION, "kpdfsync started");

    String workingDir = System.getProperty ("user.dir");
    Log.getInstance().log (LogType.INFORMATION, "Current working directory: " + workingDir);

    String osName = System.getProperty ("os.name");
    String osArch = System.getProperty ("os.arch");
    Log.getInstance().log (LogType.INFORMATION, "OS: %s (%s)", osName, osArch);

    String guiVersion = Config.getInstance().readSetting ("app.version");
    String libVersion = coderarjob.kpdfsync.lib.Config.getInstance().readSetting ("app.version");
    String ajlVersion = coderarjob.ajl.Config.getInstance().readSetting ("app.version");

    String logMessage =  "Kpdfsync - version: %s%n";
           logMessage += "libkpdfsync - version %s%n";
           logMessage += "ajl - version %s%n";

    Log.getInstance().log (LogType.INFORMATION, logMessage, guiVersion, libVersion, ajlVersion);
  }
}
