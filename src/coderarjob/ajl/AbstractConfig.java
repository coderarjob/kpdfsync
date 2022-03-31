/**
 * A singleton class that parses text files for settings.
 *
 * Settings are simple key value pairs separated by '=' char.Any line starting with a '#' char is
 * ignored and treated as comment.
 *
 * Different settings can reside in different files, but once a file is open, it is never closed.
 **/

package coderarjob.ajl;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Hashtable;

public abstract class AbstractConfig
{
  public static final String COMMENT_STARTS_WITH = "#";
  public static final String SEPARATEOR = "=";

  private Hashtable<String, BufferedReader> mReaders;
  private Hashtable<String, String> mSettings;

  protected AbstractConfig()
  {
    this.mReaders = new Hashtable<>();
    this.mSettings = new Hashtable<>();
  }

  public abstract String readSetting (String setting);

  protected String read (InputStream stream, String setting) throws IOException
  {
    String value = mSettings.get (setting);
    if (value != null)
      return value;

    if (stream == null)
      throw new IOException ("Settings file is not found");

    BufferedReader reader = getReader (stream, setting);
    value = searchForKey (reader, setting);
    if (value != null)
      mSettings.put (setting, value);

    return value;
  }

  public void clearCache() throws IOException
  {
    for (BufferedReader reader : mReaders.values())
      reader.close();

    mSettings.clear();
  }

  private String searchForKey (BufferedReader reader, String setting) throws IOException
  {
    setting = setting.trim().toUpperCase();

    String line;
    while ((line = reader.readLine()) != null)
    {
      line = line.trim();
      if (line.startsWith (COMMENT_STARTS_WITH))
        continue;

      String[] parts = line.split (SEPARATEOR);
      if (parts.length != 2)
        throw new IOException ("Invalid setting at: " + line);

      if (parts[0].trim().toUpperCase().equals(setting))
        return parts[1].trim();
    }

    return null;
  }

  private BufferedReader getReader (InputStream stream, String setting)
  {
    BufferedReader reader = mReaders.get (setting);
    if (reader == null)
      reader = new BufferedReader (new InputStreamReader (stream));
    return reader;
  }
}
