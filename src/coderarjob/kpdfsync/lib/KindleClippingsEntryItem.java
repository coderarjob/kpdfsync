package coderarjob.kpdfsync.lib;

import coderarjob.kpdfsync.lib.clipparser.ParserResult;

public class KindleClippingsEntryItem 
{
  public enum PageNumberType
  {
    UNKNOWN,
    PAGE_NUMBER,
    LOCATION_NUMBER;

    public static PageNumberType fromString (String type)
    {
      if (type.toLowerCase().equals("page"))
        return PAGE_NUMBER;
      else if (type.toLowerCase().equals("location"))
        return LOCATION_NUMBER;
      else
        return UNKNOWN;
    }
  }

  public enum AnnotationType
  {
    UNKNOWN,
    HIGHLIGHT,
    NOTE,
    BOOKMARK;

    public static AnnotationType fromString (String type)
    {
      if (type.toLowerCase().equals("highlight"))
        return HIGHLIGHT;
      else if (type.toLowerCase().equals("note"))
        return NOTE;
      else if (type.toLowerCase().equals("bookmark"))
        return BOOKMARK;
      else
        return UNKNOWN;
    }
  }

  private ParserResult mParserResult;

  public String title() throws NullPointerException, Exception 
  { return mParserResult.getFieldValue ("Title"); }

  public long fileOffset() throws NumberFormatException, NullPointerException, Exception 
  { return Long.parseUnsignedLong (mParserResult.getFieldValue ("FileOffset")); }

  public AnnotationType annotationType() throws NullPointerException, Exception 
  { return AnnotationType.fromString (mParserResult.getFieldValue ("AnnotationType")); }

  public PageNumberType pageNumberType() throws NullPointerException, Exception 
  { return PageNumberType.fromString (mParserResult.getFieldValue ("PageNumberType")); }

  public int pageNumberOrLocationNumber() throws NumberFormatException, NullPointerException, Exception 
  { return Integer.parseUnsignedInt (mParserResult.getFieldValue ("PageNumberOrLocationNumber")); }

  public String text() throws NullPointerException, Exception 
  { return mParserResult.getFieldValue ("Text");}

  public static KindleClippingsEntryItem fromParserResult (ParserResult result)
  {
    KindleClippingsEntryItem ret = new KindleClippingsEntryItem();
    ret.mParserResult = result;
    return ret;
  }
}
