/*
 * ParserResult Class
 *
 * Holds the super set of all the fields supported by any kindle parser.
 * Why not subclass from ParserResult for each Parser? Well inheritence provides abstraction
 * (same function but different implementation), but in this case, to be userfull, there will be
 * new fields per parser; but not at all change in the base functions. So why use inheritence!
 *
 * Dated: 5 Feb 2022
 * Author: arjobmukherjee@gmail.com
 */

package coderarjob.kpdfsync.lib.clipparser;

import java.util.Hashtable;

public class ParserResult
{
  public enum PageNumberType
  {
    UNKNOWN, PAGE_NUMBER, LOCATION_NUMBER;

    public static PageNumberType fromString (String type)
    {
      if (type.toLowerCase().equals("page"))          return PAGE_NUMBER;
      else if (type.toLowerCase().equals("location")) return LOCATION_NUMBER;
      else                                            return UNKNOWN;
    }
  }

  public enum AnnotationType
  {
    UNKNOWN, HIGHLIGHT, NOTE, BOOKMARK;

    public static AnnotationType fromString (String type)
    {
      switch (type.toLowerCase())
      {
        case "highlight": return HIGHLIGHT;
        case "note"     : return NOTE;
        case "bookmark" : return BOOKMARK;
        default         : return UNKNOWN;
      }
    }
  }

  enum SupportedFields
  {
    TITLE, FILE_OFFSET, ANNOTATION_TYPE, PAGE_OR_LOCATION_NUMBER, PAGE_NUMBER_TYPE, TEXT
  }

  private Hashtable<SupportedFields, String> mFieldDict;

  public ParserResult ()
  {
    mFieldDict = new Hashtable<>();

    /* Add all the supported fields and blank them out.
     * When reading a field, not supported/filled by the current parser, the default value of emtry
     * string is returned.*/
    for (SupportedFields field : SupportedFields.values ())
      mFieldDict.put (field, "");
  }

  void setFieldValue (SupportedFields field, String value)
      throws NullPointerException, ParserException
  {
      if (mFieldDict.containsKey (field) == false)
        throw new ParserException ("Unsupported field : " + field);

      mFieldDict.put (field, value);
  }

  public String getFieldValue(SupportedFields field) throws NullPointerException, ParserException
  {
      if (mFieldDict.containsKey (field) == false)
        throw new ParserException ("Unsupported field : " + field);

      return mFieldDict.get (field);
  }

  public String title () throws NullPointerException, ParserException
  {
    return this.getFieldValue (SupportedFields.TITLE);
  }

  public long fileOffset () throws NumberFormatException, NullPointerException, ParserException
  {
    return Long.parseUnsignedLong (this.getFieldValue (SupportedFields.FILE_OFFSET));
  }

  public AnnotationType annotationType () throws NullPointerException, ParserException
  {
    return AnnotationType.fromString (this.getFieldValue (SupportedFields.ANNOTATION_TYPE));
  }

  public PageNumberType pageNumberType () throws NullPointerException, ParserException
  {
    return PageNumberType.fromString (this.getFieldValue (SupportedFields.PAGE_NUMBER_TYPE));
  }

  public int pageOrLocationNumber()
      throws NumberFormatException, NullPointerException, ParserException
  {
    return Integer.parseUnsignedInt (this.getFieldValue (SupportedFields.PAGE_OR_LOCATION_NUMBER));
  }

  public String text() throws NullPointerException, Exception
  {
    return this.getFieldValue (SupportedFields.TEXT);
  }
}
