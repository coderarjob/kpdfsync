package coderarjob.kpdfsync.poc;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

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
    JFrame.setDefaultLookAndFeelDecorated (true);
    JDialog.setDefaultLookAndFeelDecorated (true);
    setUIFont (new FontUIResource ("Dialog", FontUIResource.PLAIN, 11));

    MainFrame mainFrame = new MainFrame();
    mainFrame.setVisible (true);


  }
}
