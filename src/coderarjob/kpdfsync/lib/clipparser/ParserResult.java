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
    UNKNOWN("Unknown"),
    PAGE_NUMBER("Page Number"),
    LOCATION_NUMBER("Location Number");

    private final String _name;
    public String getName() { return _name; }
    private PageNumberType (String name) { _name = name; }
    public static PageNumberType fromString (String type)
    {
      for (PageNumberType t : PageNumberType.values())
        if (t.getName().toLowerCase().equalsIgnoreCase(type))
          return t;
      return UNKNOWN;
    }
  }

  public enum AnnotationType
  {
    UNKNOWN("Unknown"),
    HIGHLIGHT("Highlight"),
    NOTE("Note"),
    BOOKMARK("Bookmark");

    private final String _name;
    public String getName() { return _name; }
    private AnnotationType (String name) { _name = name; }
    public static AnnotationType fromString (String type)
    {
      for (AnnotationType t : AnnotationType.values())
        if (t.getName().toLowerCase().equalsIgnoreCase(type))
          return t;
      return UNKNOWN;
    }
  }

  enum SupportedFields
  {
    TITLE("Title"),
    FILE_OFFSET("File Offset"),
    ANNOTATION_TYPE("Annotation Type"),
    PAGE_OR_LOCATION_NUMBER("Page or Location Number"),
    PAGE_NUMBER_TYPE("Page Number Type"),
    TEXT("Text");

    private final String _name;
    public String getName() { return _name; }
    private SupportedFields (String name) { _name = name; }
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
