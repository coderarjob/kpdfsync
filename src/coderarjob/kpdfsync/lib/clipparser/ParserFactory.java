/*
 * ParserFactory
 *
 * Dated: 16 July 2022
 * Author: arjobmukherjee@gmail.com
 */

package coderarjob.kpdfsync.lib.clipparser;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ParserFactory
{
  public static AbstractParser getParser (String name, String fileName)
      throws FileNotFoundException, IOException, ParserException
  {
    if (name.toLowerCase().equals(KindleParserV1.getParserName().toLowerCase()))
        return new KindleParserV1 (fileName);
    else if (name.toLowerCase().equals(KindleParserV2.getParserName().toLowerCase()))
        return new KindleParserV2 (fileName);
    else
      throw new ParserException ("Cannot create Parser. Invalid name: " + name);
  }
}
