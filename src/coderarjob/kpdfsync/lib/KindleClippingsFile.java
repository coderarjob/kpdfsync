package coderarjob.kpdfsync.lib;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

import coderarjob.kpdfsync.lib.clipparser.AbstractParser;
import coderarjob.kpdfsync.lib.clipparser.ParserResult;
import coderarjob.kpdfsync.lib.clipparser.KindleParserV1;
import coderarjob.kpdfsync.lib.clipparser.ParserException;

public class KindleClippingsFile 
{
  private String mClippingsFile;
  private AbstractParser mParser;
  private Hashtable<String, List<Long>> mTitleOffsetMap;

  public KindleClippingsFile (String clippingsFileName) 
      throws FileNotFoundException, IOException, Exception
  {
    this.mClippingsFile = clippingsFileName;
    mParser = new KindleParserV1 (clippingsFileName);

    mTitleOffsetMap = new Hashtable<>();
    readTitlesAndAddOffsets();
  }

  public Enumeration<String> getBookTitles()
  {
    return mTitleOffsetMap.keys();
  }

  public Enumeration<KindleClippingsEntryItem> getBookAnnotations (String bookTitle)
    throws Exception
  {
    List<Long> offsetlist = mTitleOffsetMap.getOrDefault(bookTitle, null);
    if (offsetlist == null) 
      throw new Exception ("Book title not found.");

    KindleClippingsEntryEnumeration new_enu = 
      new KindleClippingsEntryEnumeration (this.mParser, offsetlist);
    
    return new_enu;
  }

  private ParserResult parse() throws Exception
  {
    ParserResult result = null;
    boolean isSuccess = false;
    do
    {
      try
      {
        isSuccess = false;
        result = mParser.parse();
        isSuccess = true;
      }
      catch (ParserException ex)
      {
        mParser.moveToNextEntry();
      }
    } while (isSuccess == false);

    return result;
  }

  private void readTitlesAndAddOffsets() throws Exception
  {
    ParserResult result;
    while ((result = parse()) != null)
    {
      KindleClippingsEntryItem item = KindleClippingsEntryItem.fromParserResult (result);

      List<Long> offsetlist = mTitleOffsetMap.getOrDefault(item.title(), null);
      if (offsetlist == null) 
      {
        offsetlist = new ArrayList<Long>();
        mTitleOffsetMap.put (item.title(), offsetlist);
      }

      offsetlist.add(item.fileOffset());
    }
  }
}
