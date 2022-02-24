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
  protected enum ParsingErrors
  {
    NO_ERROR, END_OF_BLOCK_REACHED, PARSING_ERROR;

    private String mTag;
    public void setTag (String name) { this.mTag = name; }
    public String getTag()           { return this.mTag; }

  }

  /* Abstract public methods */
  public abstract boolean       moveToNextEntry()                            throws Exception;
  public abstract void          moveToEntryAtOffset (long offset)            throws Exception;
  public abstract ParsingErrors parseLine(int linei, ParserResult result)    throws Exception;
  public abstract String        getParserVersion();
  public abstract String[]      getSupportedKindleVersions();

  /* Protected fields */
  protected String           mFileName;
  protected RandomAccessFile mFile;
  protected Charset          mCharset;
  protected ParserEvents     mParserEvents;

  /* Private fields */
  private long   mLastFilePointer;
  private String mLastLineRead;

  /* Getters and setters */
  protected long         lastFilePointer()                    { return mLastFilePointer;    }
  protected String       lastLineRead()                       { return mLastLineRead;       }
  public    Charset      getCharset()                         { return mCharset;            }
  public    String       getFileName()                        { return mFileName;           }
  public    ParserEvents getParserEvents()                    { return this.mParserEvents;  }
  public    void         setParserEvents (ParserEvents value) { this.mParserEvents = value; }

  /* Hook methods */
  protected void onParsingStart() throws Exception { }

  protected void onParsingSuccess (ParserResult result) throws Exception
  {
    ParserEvents e = this.mParserEvents;
    if (e == null) return;

    e.onSuccess (this.mFileName, this.mFile.getFilePointer(), result);
  }
  protected void onParsingError(String error, ParserResult result) throws Exception
  {
    ParserEvents e = this.mParserEvents;
    if (e == null) return;

    e.onError (this.mFileName, this.mFile.getFilePointer(), error, result);
  }

  /* Contrtructor and public methods */
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

    /* Default values*/
    this.mLastLineRead = null;
    this.mLastFilePointer = -1;
    this.mParserEvents = null;
  }

  protected Charset getCharsetFromByteOrderMarkType (ByteOrderMarkTypes type)
  {
    switch (type)
    {
      case UTF8    : return StandardCharsets.UTF_8;
      case UTF16_LE: return StandardCharsets.UTF_16LE;
      case UTF16_BE: return StandardCharsets.UTF_16BE;
      default      : return Charset.defaultCharset();
    }
  }

  /**
   * Parses each line of the current block.
   * Returns a ParserResult object with the parsed result.
   * Null is returned is EOF was reached.
   */
  public ParserResult parse() throws Exception
  {
    ParsingErrors parseError = ParsingErrors.NO_ERROR;
    ParserResult result = new ParserResult();

    /* End of file was reached before */
    if (isEOF() == true)
    {
      onParsingError("EOF was reached", null);
      return null;
    }

    try {
      onParsingStart();

      for (int i = 0; parseError == ParsingErrors.NO_ERROR; i++)
        parseError = parseLine(i, result);

    } catch (Exception ex) {

      onParsingError(ex.getMessage(), result);
      throw ex;
    }

    /* Parsing failed at some point*/
    if (parseError == ParsingErrors.PARSING_ERROR)
    {
      String errDes = String.format ("Parsing error: '%s' is not '%s'.",
                                      (isEOF() == true) ? "<EOF>" : this.lastLineRead(),
                                      parseError.getTag());
      onParsingError(errDes, result);
      throw new ParserException (errDes);
    }

    onParsingSuccess (result);
    return result;
  }

  /**
   * Checks if End of File has been reached.
   */
  protected boolean isEOF() throws IOException
  {
    return mFile.getFilePointer() >= mFile.length() - 1;
  }

  /**
   * Reads a line from the file and converts it as per the BOM in the file.
   */
  protected String readLineWithProperEncoding () throws IOException
  {
    mLastFilePointer = mFile.getFilePointer();
    String line = mFile.readLine();

    if (line == null)
      return null;

    this.mLastLineRead = new String (line.getBytes("ISO-8859-1"), mCharset);
    return this.mLastLineRead;
  }
}
