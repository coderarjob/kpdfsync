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

import coderarjob.kpdfsync.lib.clipparser.ParserResult.SupportedFields;

public class KindleParserV1 extends AbstractParser
{
    public enum ParsingStages
    {
      TITLE("Title"),
      FILE_OFFSET("Annotation block offset in file"),
      ANNOTATION_TYPE("Annotation Type"),
      PAGE_OR_LOCATION_NUMBER("Page or location number"),
      PAGE_NUMBER_TYPE("Page number type"),
      TEXT("Text"),
      END_OF_BLOCK("End of Block");

      private final String _name;
      public String getName() { return _name; }
      private ParsingStages (String name) { _name = name; }
    }

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
    protected AbstractKindleParserConstants getKindleParserConstants ()
    {
      AbstractKindleParserConstants constants = new AbstractKindleParserConstants () {
        public int getAnnotationLineTypePosition() { return 2; }
        public int getAnnotationLinePageNumberTypePosition() { return 4; }
        public int getAnnotationLinePageOrLocationNumberPosition() { return 5; }
        public String getTeminationLinePattern () { return "=========="; }
      };

      return constants;
    }

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
          parseAnnotationLine (result);
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
          throw genParserException (ParsingStages.TITLE.getName());

        boolean isValid = (linestr.length () > 0);
        if (isValid == false)
          throw genParserException (ParsingStages.TITLE.getName());

        result.setFieldValue (SupportedFields.TITLE, linestr.trim());
        result.setFieldValue (SupportedFields.FILE_OFFSET, String.valueOf(this.lastFilePointer()));
    }

    /**
     * Validates Book Annotation type line and adds to ParserResult.
     */
    protected void parseAnnotationLine (ParserResult result) throws IOException, ParserException
    {
        /* Read current line. Cannot be EOF.*/
        String linestr = this.lastLineRead();
        if (linestr == null)
          throw genParserException (ParsingStages.ANNOTATION_TYPE.getName());

        boolean isValid = false;
        String value = "";

        /* Annotation Type */
        value = trySplitString (linestr, " ", mConstants.getAnnotationLineTypePosition());
        isValid = (value != null)
                  && (value.toLowerCase().equals ("highlight")
                      || value.toLowerCase().equals ("note")
                      || value.toLowerCase().equals ("bookmark"));

        if (isValid == false)
          throw genParserException (ParsingStages.ANNOTATION_TYPE.getName());

        result.setFieldValue (SupportedFields.ANNOTATION_TYPE, value);
        String annotationType = value;

        /* Page Number Type */
        value = trySplitString (linestr, " ", mConstants.getAnnotationLinePageNumberTypePosition());
        isValid = (value != null)
                  && (value.toLowerCase().equals("page")
                      || value.toLowerCase().equals("location"));

        if (isValid == false)
          throw genParserException (ParsingStages.PAGE_NUMBER_TYPE.getName());

        result.setFieldValue (SupportedFields.PAGE_NUMBER_TYPE, value);

        /* Page or Location Number */
        value = trySplitString (linestr, " ", mConstants.getAnnotationLinePageOrLocationNumberPosition());
        isValid = (value != null);
        if (isValid == false)
          throw genParserException (ParsingStages.PAGE_OR_LOCATION_NUMBER.getName());

        if (annotationType.toLowerCase().equals("bookmark") == false)
        {
            value = trySplitString (value, "-", 0);
            isValid = (value != null);
            if (isValid == false)
              throw genParserException (ParsingStages.PAGE_OR_LOCATION_NUMBER.getName());
        }

        isValid = tryParseUnsigendInt (value);
        if (isValid == false)
          throw genParserException (ParsingStages.PAGE_OR_LOCATION_NUMBER.getName());

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
          throw genParserException (ParsingStages.TEXT.getName());

        boolean isValid = false;
        String annotationType = result.getFieldValue(SupportedFields.ANNOTATION_TYPE).toLowerCase();

        /* There should be a blank line */
        isValid = (linestr.length() == 0);
        if (isValid == false)
          throw genParserException (ParsingStages.TEXT.getName());

        /* Read the actual text, in the following lines */
        StringBuilder sb = new StringBuilder();
        while (isTerminationLine(linestr = readLineWithProperEncoding()) == false)
        {
            sb.append(linestr + "\n");

            /* NOTE: This line can also be blank, in case of a bookmark annotation type.*/
            if (annotationType.equals("bookmark") == false)
            {
                isValid = (linestr.length() > 0);
                if (isValid == false)
                  throw genParserException (ParsingStages.TEXT.getName());
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
          throw genParserException (ParsingStages.END_OF_BLOCK.getName());

        /* Check for termination line. */
        boolean isValid = isTerminationLine (linestr);
        if (isValid == false)
          throw genParserException (ParsingStages.END_OF_BLOCK.getName());
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
