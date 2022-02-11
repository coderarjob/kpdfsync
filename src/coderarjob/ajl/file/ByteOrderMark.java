/**
 * This part of the Arjobs Java Library.
 * ByteOrderMark parses the BOM bytes (if any) and returns the BOM Type.
 **/

package coderarjob.ajl.file;

import java.io.FileInputStream;
import java.io.IOException;

public class ByteOrderMark
{
  public static ByteOrderMarkTypes determineBom (String fileName) throws IOException
  {
    try (FileInputStream fis = new FileInputStream (fileName))
    {
      /* Get the first 3 bytes; which is the max size of a BOM */
      byte[] fileBom = new byte[3];
      fis.read (fileBom, 0, fileBom.length);

      /* Compare BOM bytes of each type of BOM to determine the type we have */
      for (ByteOrderMarkTypes bom : ByteOrderMarkTypes.values())
      {
        if (isBomMatching (bom.getBomBytes(), fileBom))
          return bom;
      }

      /* Bytes from file, did not match any bom bytes */
      return ByteOrderMarkTypes.NOT_PRESENT;
    }
  }

  private static boolean isBomMatching (byte[] bomBytes, byte[] fileBom)
  {
    int i = 0;
    for (i = 0; i < bomBytes.length; i++) {
      if (bomBytes[i] != fileBom[i])
        return false;
    }

    return true;
  }
}
