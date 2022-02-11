package coderarjob.kpdfsync.lib;

import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import coderarjob.kpdfsync.lib.clipparser.AbstractParser;
import coderarjob.kpdfsync.lib.clipparser.ParserResult;

public class KindleClippingsEntryEnumeration implements Enumeration<ParserResult>
{
  private final List<Long> mFileOffsets;
  private final AbstractParser mParser;

  private ParserResult mLastResult;
  private int mListIndex;

  public KindleClippingsEntryEnumeration (AbstractParser parser, List<Long> fileOffsets)
    throws Exception
  {
    this.mFileOffsets = fileOffsets;
    this.mParser = parser;
    this.mListIndex = 0;
  }

  public boolean hasMoreElements()
  {
    try
    {
      mLastResult = null;

      if (mListIndex < mFileOffsets.size())
      {
        long offset = mFileOffsets.get (mListIndex++);
        mParser.moveToEntryAtOffset (offset);
        mLastResult = mParser.parse();
      }
    } catch (Exception ex) { }

    return mLastResult != null;
  }

  public ParserResult nextElement() throws NoSuchElementException
  {
    if (mLastResult == null)
      throw new NoSuchElementException();

    return mLastResult;
  }

}
