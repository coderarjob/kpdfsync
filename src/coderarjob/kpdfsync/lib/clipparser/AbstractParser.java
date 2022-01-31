package coderarjob.kpdfsync.lib.clipparser;

import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;

import coderarjob.ajl.file.ByteOrderMarkTypes;
import coderarjob.ajl.file.ByteOrderMark;

public abstract class AbstractParser
{
  public abstract boolean moveToNextEntry () throws Exception;
  public abstract void moveToEntryAtOffset (long offset) throws Exception;
  public abstract ParserResult parse () throws Exception;
  public abstract String getParserVersion ();
  public abstract String[] getSupportedKindleVersions ();
  public abstract String[] getSupportedFields ();

  protected final String mFileName;
  protected final RandomAccessFile mFile;
  protected final Charset mCharset;

  private long mLastFilePointer;
  protected long lastFilePointer() { return mLastFilePointer; }

  public Charset getCharset () { return mCharset; }
  public String getFileName () { return mFileName; }

  public AbstractParser (String fileName) throws FileNotFoundException, IOException
  {
    mFileName = fileName;

    /* Every line read from the file, will be converted to the Charset as determined by the BOM. By
     * default, UTF-8 BOM exists in the Clippings file.
     */
    ByteOrderMarkTypes mBom = ByteOrderMark.determineBom (fileName);
    mCharset = getCharsetFromByteOrderMarkType (mBom);

    /* Open the Clippings.txt file for reading */
    mFile = new RandomAccessFile (fileName, "r");
  }

  protected Charset getCharsetFromByteOrderMarkType (ByteOrderMarkTypes type)
  {
    switch (type)
    {
      case UTF8:
        return StandardCharsets.UTF_8;
      case UTF16_LE:
        return StandardCharsets.UTF_16LE;
      case UTF16_BE:
        return StandardCharsets.UTF_16BE;
      default:
        return Charset.defaultCharset();
    }
  }

  /*
   * Reads a line from the file and converts it as per the BOM in the file.
   */
  protected String readLineWithProperEncoding () throws IOException
  {
    mLastFilePointer = mFile.getFilePointer();
    String line = mFile.readLine();

    if (line == null)
      return null;

    String encodedline = new String (line.getBytes("ISO-8859-1"), mCharset);
    return encodedline;
  }

}
