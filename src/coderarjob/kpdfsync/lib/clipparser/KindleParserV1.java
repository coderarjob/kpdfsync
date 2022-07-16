/*
 * KindleParserV1 Class
 *
 * Contains an implementation of the AbstractParser. Current implementation, parsers the following
 * fields from the kindle clippings file.
 * {TITLE, ANNOTATION_TYPE, PAGE_OR_LOCATION_NUMBER, PAGE_NUMBER_TYPE, TEXT}
 * additionally saves the {FILE_OFFSET} in the ParserResult returned.
 *
 * Dated: 5 Feb 2022
 * Author: arjobmukherjee@gmail.com
 */
package coderarjob.kpdfsync.lib.clipparser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

import coderarjob.kpdfsync.lib.clipparser.ParserResult.SupportedFields;
import coderarjob.kpdfsync.lib.clipparser.ParserResult.AnnotationType;
import coderarjob.kpdfsync.lib.clipparser.ParserResult.PageNumberType;

public class KindleParserV1 extends AbstractParser
{
    private final int ANNOTATION_TYPE_WORD_POS = 2;
    private final int PAGE_NUMBER_TYPE_WORD_POS = 4;
    private final int PAGE_NUMBER_OR_LOCATION_WORD_POS = 5;
    private final String TERMINATION_LINE_PATTERN = "==========";

    public KindleParserV1 (String fileName) throws FileNotFoundException, IOException
    {
        /* Clippings file is opened and onClippingsFileOpen hook method is called. */
        super (fileName);
    }

    public static String getParserName()
    {
      return "Kindle Clippings - Newer kindles";
    }

    public String toString()
    {
      return KindleParserV1.getParserName();
    }

    /* Implementing abstract methods from AbstractParser*/
    protected boolean isTerminationLine (String linestr)
    {
      return linestr.toLowerCase().equals(TERMINATION_LINE_PATTERN);
    }

    /*protected AbstractKindleParserConstants getKindleParserConstants ()
    {
      AbstractKindleParserConstants constants = new AbstractKindleParserConstants () {
        public ParserResultFieldsFilter<AnnotationType> getAnnotationTypeFilter(ParserResult res)
        {
          Hashtable<String, AnnotationType> ht = new Hashtable<>();
          ht.put("highlight", AnnotationType.HIGHLIGHT);
          ht.put("note", AnnotationType.NOTE);
          ht.put("bookmark", AnnotationType.BOOKMARK);

          return new ParserResultFieldsFilter<> (2, ht);
        }

        public ParserResultFieldsFilter<PageNumberType> getPageNumberTypeFilter(ParserResult res)
        {
          Hashtable<String, PageNumberType> ht = new Hashtable<>();
          ht.put("page", PageNumberType.PAGE_NUMBER);
          ht.put("location", PageNumberType.LOCATION_NUMBER);

          return new ParserResultFieldsFilter<> (4, ht);
        }

        public ParserResultFieldsFilter<Object> getPageOrLocationNumberFilter(ParserResult res)
        {
          return new ParserResultFieldsFilter<> (5, null);
        }

        public ParserResultFieldsFilter<Boolean> getTerminationLineFilter()
        {
          Hashtable<String, Boolean> ht = new Hashtable<>();
          ht.put ("==========", true);
          return new ParserResultFieldsFilter<> (0, ht);
        }
      };

      return constants;
    }*/

    public String getParserVersion ()
    {
        return "1.0";
    }

    public String[] getSupportedKindleVersions ()
    {
        return new String[] {"5.12.*", "newer"};
    }

    /**
     * Parses each line of the current block.
     * Returns a ParserResult object with the parsed result.
     * Returns True, if there reached end of the block. False otherwise.
     */
    protected boolean parseLine(int lineIndex, ParserResult result)
        throws IOException, ParserException
    {
      switch (lineIndex)
      {
        case 0 :
          this.readLineWithProperEncoding();
          parseTitleLine (result);
          break;
        case 1 :
          this.readLineWithProperEncoding();
          parseAnnotationType(result);
          parsePageNumberType(result);
          parsePageOrLocationNumber(result);
          break;
        case 2 :
          this.readLineWithProperEncoding();
          parseTextLine (result);
          break;
        case 3 :
          this.readLineWithProperEncoding();
          parseTerminationLine (result);
          return true;
        default:
          String desc = String.format ("At an invalid line. Line #%d", lineIndex);
          throw new ParserException (desc);
      }
      return false;
    }

    /* Class methods */

    /**
     * Validates Book Title line and adds to ParserResult.
     */
    protected void parseTitleLine (ParserResult result) throws IOException, ParserException
    {
        /* Read current line. Cannot be EOF.*/
        String linestr = this.lastLineRead();
        if (linestr == null)
          throw genParserException (SupportedFields.TITLE.getName());

        boolean isValid = (linestr.length () > 0);
        if (isValid == false)
          throw genParserException (SupportedFields.TITLE.getName());

        result.setFieldValue (SupportedFields.TITLE, linestr.trim());
        result.setFieldValue (SupportedFields.FILE_OFFSET, String.valueOf(this.lastFilePointer()));
    }

    /**
     * Validates Book Annotation type line and adds to ParserResult.
     */
    protected void parseAnnotationType (ParserResult result) throws IOException, ParserException
    {
        /* Read current line. Cannot be EOF.*/
        String linestr = this.lastLineRead();
        if (linestr == null)
          throw genParserException (SupportedFields.ANNOTATION_TYPE.getName());

        /* Annotation Type */
        Hashtable<String, AnnotationType> ht = new Hashtable<>();
        ht.put("highlight", AnnotationType.HIGHLIGHT);
        ht.put("note", AnnotationType.NOTE);
        ht.put("bookmark", AnnotationType.BOOKMARK);

        String value = trySplitString (linestr, " ", ANNOTATION_TYPE_WORD_POS).toLowerCase();

        AnnotationType annotationType = ht.getOrDefault(value,AnnotationType.UNKNOWN);
        if (annotationType == AnnotationType.UNKNOWN)
          throw genParserException (SupportedFields.ANNOTATION_TYPE.getName());

        result.setFieldValue (SupportedFields.ANNOTATION_TYPE, annotationType.getName());
    }

    protected void parsePageNumberType (ParserResult result) throws IOException, ParserException
    {
        String linestr = this.lastLineRead();

        /* Page Number Type */
        Hashtable<String, PageNumberType> ht = new Hashtable<>();
        ht.put("page", PageNumberType.PAGE_NUMBER);
        ht.put("location", PageNumberType.LOCATION_NUMBER);

        String value = trySplitString (linestr, " ", PAGE_NUMBER_TYPE_WORD_POS).toLowerCase();

        PageNumberType pageNumberType = ht.getOrDefault(value,PageNumberType.UNKNOWN);
        if (pageNumberType == PageNumberType.UNKNOWN)
          throw genParserException (SupportedFields.PAGE_NUMBER_TYPE.getName());

        result.setFieldValue (SupportedFields.PAGE_NUMBER_TYPE, pageNumberType.getName());
    }

    protected void parsePageOrLocationNumber (ParserResult result)
        throws IOException, ParserException
    {
        boolean isValid = false;
        String value = "";

        String linestr = this.lastLineRead();
        AnnotationType annotationType = result.annotationType();

        /* Page or Location Number */
        value = trySplitString (linestr, " ", PAGE_NUMBER_OR_LOCATION_WORD_POS);
        isValid = (value != null);
        if (isValid == false)
          throw genParserException (SupportedFields.PAGE_OR_LOCATION_NUMBER.getName());

        if (annotationType == AnnotationType.NOTE || annotationType == AnnotationType.HIGHLIGHT)
        {
            value = trySplitString (value, "-", 0);
            isValid = (value != null);
            if (isValid == false)
              throw genParserException (SupportedFields.PAGE_OR_LOCATION_NUMBER.getName());
        }

        isValid = tryParseUnsigendInt (value);
        if (isValid == false)
          throw genParserException (SupportedFields.PAGE_OR_LOCATION_NUMBER.getName());

        result.setFieldValue (SupportedFields.PAGE_OR_LOCATION_NUMBER, value);
    }

    /**
     * Validates Book highlight/note text line and adds to ParserResult.
     */
    protected void parseTextLine (ParserResult result) throws IOException, ParserException
    {
        /* Read current line. Cannot be EOF.*/
        String linestr = this.lastLineRead();
        if (linestr == null)
          throw genParserException (SupportedFields.TEXT.getName());

        boolean isValid = false;
        AnnotationType annotationType = result.annotationType();

        /* There should be a blank line */
        isValid = (linestr.length() == 0);
        if (isValid == false)
          throw genParserException (SupportedFields.TEXT.getName());

        /* Read the actual text, in the following lines */
        StringBuilder sb = new StringBuilder();
        while (isTerminationLine(linestr = readLineWithProperEncoding()) == false)
        {
            sb.append(linestr + "\n");

            /* NOTE: This line can also be blank, in case of a bookmark annotation type.*/
            if (annotationType == AnnotationType.NOTE || annotationType == AnnotationType.HIGHLIGHT)
            {
                isValid = (linestr.length() > 0);
                if (isValid == false)
                  throw genParserException (SupportedFields.TEXT.getName());
            }
        }

        /* We have read one line too far. Backup */
        mFile.seek(this.lastFilePointer());

        /* Remove the extra new line at the end*/
        sb.deleteCharAt(sb.length() - 1);

        result.setFieldValue (SupportedFields.TEXT, sb.toString());
    }

    /**
     * Validates entry end/termination line.
     */
    protected void parseTerminationLine (ParserResult result) throws IOException, ParserException
    {
        /* Read current line. Cannot be EOF.*/
        String linestr = this.lastLineRead();
        if (linestr == null)
          throw genParserException ("Termination Line");

        /* Check for termination line. */
        boolean isValid = linestr.equals(TERMINATION_LINE_PATTERN);
        if (isValid == false)
          throw genParserException ("Termination Line");
    }

    protected String trySplitString (String s, String p, int index)
    {
        assert (s != null);
        assert (p != null);

        try {
            return s.split (p)[index];
        } catch (Exception ex) {
            return null;
        }
    }

    protected boolean tryParseUnsigendInt (String s)
    {
        assert (s != null);

        try {
            Integer.parseUnsignedInt (s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
