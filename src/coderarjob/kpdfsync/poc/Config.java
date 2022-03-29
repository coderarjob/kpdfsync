package coderarjob.kpdfsync.poc;
import coderarjob.ajl.AbstractConfig;
import java.io.InputStream;

public class Config extends AbstractConfig
{
  private final String SETTINGS_FILE = "/coderarjob/kpdfsync/poc/res/app.settings";
  private static Config mInstance;
  private Config()
  {
    super();
  }

  public static Config getInstance()
  {
    if (mInstance == null)
      mInstance = new Config();
    return mInstance;
  }

  public String readSetting (String setting)
  {
    InputStream stream = this.getClass().getResourceAsStream(SETTINGS_FILE);
    try {
      return read (stream, setting);
    } catch (Exception ex) {
      throw new RuntimeException (ex);
    }
  }
}
