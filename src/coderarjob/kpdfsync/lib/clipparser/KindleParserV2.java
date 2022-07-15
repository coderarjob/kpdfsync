/*
 * KindleParserV2 Class
 *
 * Based on KindleParserV1, but minor change in Annotation line parsing.
 *
 * Dated: 11 July 2022
 * Author: arjobmukherjee@gmail.com
 */

package coderarjob.kpdfsync.lib.clipparser;

import java.io.FileNotFoundException;
import java.io.IOException;

public class KindleParserV2 extends KindleParserV1
{
    public KindleParserV2 (String fileName) throws FileNotFoundException, IOException
    {
        super (fileName);
    }

    public static String getParserName()
    {
      return "Kindle Clippings - Older kindles";
    }

    public String toString()
    {
      return KindleParserV2.getParserName();
    }

    /* Implementing abstract methods from AbstractParser*/
    public String getParserVersion ()
    {
        return "2.0";
    }

    public String[] getSupportedKindleVersions ()
    {
        return new String[] {"3.4.3", "older"};
    }

    protected AbstractKindleParserConstants getKindleParserConstants ()
    {
      AbstractKindleParserConstants constants = new AbstractKindleParserConstants () {
        public int getAnnotationLineTypePosition() { return 1; }
        public int getAnnotationLinePageNumberTypePosition() { return 3; }
        public int getAnnotationLinePageOrLocationNumberPosition() { return 4; }
        public String getTeminationLinePattern () { return "=========="; }
      };

      return constants;
    }
}
