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
  /* Abstract public methods */
  public abstract String        getParserVersion();
  public abstract String[]      getSupportedKindleVersions();

  /* Abstract protected methods */
  protected abstract boolean isTerminationLine (String linestr);
  protected abstract boolean parseLine(int linei, ParserResult res) throws Exception;

  /* Protected fields */
  protected String           mFileName;
  protected RandomAccessFile mFile;
  protected Charset          mCharset;
  protected ParserEvents     mParserEvents;
  protected boolean mIsInvalidState;         /* On paring error, this is set to true.
                                                False indicates, no error, or file pointer has
                                                moved to the next block after the previous parsing
                                                error.*/

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
  protected void onParsingStart() throws Exception
  {
    if (this.mIsInvalidState)
      throw new ParserException ("Invalid parser state : On an invalid line.");
  }

  protected void onParsingSuccess (ParserResult result) throws Exception
  {
    ParserEvents e = this.mParserEvents;
    if (e == null) return;

    e.onSuccess (this.mFileName, this.mFile.getFilePointer(), result);
  }
  protected void onParsingError(String error, ParserResult result) throws Exception
  {
    /* Until we move past the current block to the next block, parser remains in invalid
     * state. */
    mIsInvalidState = true;

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
    this.mIsInvalidState = false;
  }

  /**
   * Moves to the Title of the next block from anywhere in the current block.
   * Moves to the start of the next block. This methods, does not actually parse the lines, it
   * just looks for the next termination line.
   *
   * Returns True, of next block was found, otherwise False.
   */
  public boolean moveToNextEntry() throws Exception
  {
    String linestr = null;

    while (true)
    {
      linestr = readLineWithProperEncoding ();
      if (linestr == null)
        return false;

      if (isTerminationLine (linestr))
        break;
    }

    /* Move past any invalid block.*/
    mIsInvalidState = false;
    return true;
  }

  /**
   * Moves the file pointer and assumes the next line read to be Title.
   */
  public void moveToEntryAtOffset (long offset) throws Exception
  {
    mFile.seek(offset);
    mIsInvalidState = false;
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
    ParserResult result = new ParserResult();

    /* End of file was reached before */
    if (isEOF() == true)
    {
      onParsingError("EOF was reached", null);
      return null;
    }

    try
    {
      onParsingStart();

      boolean isTerminationLineReached = false;
      for (int i = 0; isTerminationLineReached  == false; i++)
      {
        if (Thread.interrupted() == true)
          throw new InterruptedException();

        isTerminationLineReached = parseLine(i, result);
      }

    } catch (InterruptedException ex) {
      throw ex;
    }
    catch (Exception ex) {
      onParsingError(ex.getMessage(), result);
      throw ex;
    }

    onParsingSuccess (result);
    return result;
  }

  /** Generates a Parser Exception object for subclasses to use.
   * This ensures a consistent Exception description.
   */
  protected ParserException genParserException (String field)
  {
      String errDes = String.format ("Invalid '%s' in line '%s'.",
                                      field, this.lastLineRead());
      return new ParserException (errDes);
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
