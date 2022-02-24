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
    /*
     * On paring error, this is set to true. False indicates, no error, or that file pointer has
     * moved to the next block after the previous parsing error.
     */
    protected boolean mIsInvalidState;

    public KindleParserV1 (String fileName) throws FileNotFoundException, IOException
    {
        /* Clippings file is opened and onClippingsFileOpen hook method is called. */
        super (fileName);

        /* Default state */
        mIsInvalidState = false;
    }

    /* Implementing abstract methods from AbstractParser*/
    public String getParserVersion ()
    {
        return "1.0";
    }

    public String[] getSupportedKindleVersions ()
    {
        return new String[] {"1.2.4", "1.2.5", "1.2.6"};
    }

    /**
     * Moves to the Title of the next block from anywhere in the current block.
     * Moves to the start of the next block. This methods, does not actually parse the lines, it
     * just looks for the next termination line.
     *
     * Returns True, of next block was found, otherwise False.
     */
    public boolean moveToNextEntry () throws IOException
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
    public void moveToEntryAtOffset (long offset) throws IOException
    {
        mFile.seek(offset);
        mIsInvalidState = false;
    }

    /**
     * Parses each line of the current block.
     * Returns a ParserResult object with the parsed result.
     * Null is returned is EOF was reached.
     */
    public ParsingErrors parseLine (int lineIndex, ParserResult result)
        throws IOException, ParserException
    {
      ParsingErrors err = ParsingErrors.NO_ERROR;
      switch (lineIndex)
      {
        case 0 :
          err = parseTitleLine (result);
          err.setTag ("Title line");
          break;
        case 1 :
          err = parseAnnotationLine (result);
          err.setTag ("Annotation line");
          break;
        case 2 :
          err = parseTextLine (result);
          err.setTag ("Text line");
          break;
        case 3 :
          err = parseTerminationLine (result);
          err.setTag ("Termination line");
          break;
        default:
          err =ParsingErrors.END_OF_BLOCK_REACHED;
          break;
      }

      return err;
    }

    protected void onParsingError(String error, ParserResult result) throws Exception
    {
        /* Until we move past the current block to the next block, parser remains in invalid
         * state. */
        mIsInvalidState = true;

        /* Call base class method */
        super.onParsingError (error, result);
    }

    protected void onParsingStart() throws Exception
    {
        if (this.mIsInvalidState)
            throw new ParserException ("Invalid parser state : On an invalid line.");

        /* Call base class method */
        super.onParsingStart();
    }

    /* Class methods */

    /**
     * Validates Book Title line and adds to ParserResult and returns true is valid.
     * If validation fails, false is returned.
     */
    protected ParsingErrors parseTitleLine (ParserResult result) throws IOException, ParserException
    {
        /* Read current line. Cannot be EOF.*/
        String linestr = readLineWithProperEncoding();
        if (linestr == null)
            return ParsingErrors.PARSING_ERROR;

        boolean isValid = (linestr.length () > 0);
        if (isValid == false)
            return ParsingErrors.PARSING_ERROR;

        result.setFieldValue (SupportedFields.TITLE, linestr.trim());
        result.setFieldValue (SupportedFields.FILE_OFFSET, String.valueOf(this.lastFilePointer()));
        return ParsingErrors.NO_ERROR;
    }

    /**
     * Validates Book Annotation type line and adds to ParserResult and returns true is valid.
     * If validation fails, false is returned.
     */
    protected ParsingErrors parseAnnotationLine (ParserResult result)
            throws IOException, ParserException
    {
        /* Read current line. Cannot be EOF.*/
        String linestr = readLineWithProperEncoding();
        if (linestr == null)
            return ParsingErrors.PARSING_ERROR;

        boolean isValid = false;
        String value = "";

        /* Annotation Type */
        value = trySplitString (linestr, " ", 2);
        isValid = (value != null)
                  && (value.toLowerCase().equals ("highlight")
                      || value.toLowerCase().equals ("note")
                      || value.toLowerCase().equals ("bookmark"));

        if (isValid == false)
            return ParsingErrors.PARSING_ERROR;

        result.setFieldValue (SupportedFields.ANNOTATION_TYPE, value);
        String annotationType = value;

        /* Page Number Type */
        value = trySplitString (linestr, " ", 4);
        isValid = (value != null)
                  && (value.toLowerCase().equals("page")
                      || value.toLowerCase().equals("location"));

        if (isValid == false)
            return ParsingErrors.PARSING_ERROR;

        result.setFieldValue (SupportedFields.PAGE_NUMBER_TYPE, value);

        /* Page or Location Number */
        value = trySplitString (linestr, " ", 5);
        isValid = (value != null);
        if (isValid == false)
            return ParsingErrors.PARSING_ERROR;

        if (annotationType.toLowerCase().equals("bookmark") == false)
        {
            value = trySplitString (value, "-", 0);
            isValid = (value != null);
            if (isValid == false)
                return ParsingErrors.PARSING_ERROR;
        }

        isValid = tryParseUnsigendInt (value);
        if (isValid == false)
            return ParsingErrors.PARSING_ERROR;

        result.setFieldValue (SupportedFields.PAGE_OR_LOCATION_NUMBER, value);
        return ParsingErrors.NO_ERROR;
    }

    /**
     * Validates Book highlight/note text line and adds to ParserResult and returns true is valid.
     * If validation fails, false is returned.
     */
    protected ParsingErrors parseTextLine (ParserResult result) throws IOException, ParserException
    {
        /* Read current line. Cannot be EOF.*/
        String linestr = readLineWithProperEncoding();
        if (linestr == null)
            return ParsingErrors.PARSING_ERROR;

        boolean isValid = false;
        String annotationType = result.getFieldValue(SupportedFields.ANNOTATION_TYPE).toLowerCase();

        /* There should be a blank line */
        isValid = (linestr.length() == 0);
        if (isValid == false)
            return ParsingErrors.PARSING_ERROR;

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
                    return ParsingErrors.PARSING_ERROR;
            }
        }

        /* We have read one line too far. Backup */
        mFile.seek(this.lastFilePointer());

        /* Remove the extra new line at the end*/
        sb.deleteCharAt(sb.length() - 1);

        result.setFieldValue (SupportedFields.TEXT, sb.toString());
        return ParsingErrors.NO_ERROR;
    }

    /**
     * Validates entry end/termination line and adds to ParserResult and returns true is valid.
     * If validation fails, false is returned.
     */
    protected ParsingErrors parseTerminationLine (ParserResult result)
        throws IOException, ParserException
    {
        /* Read current line. Cannot be EOF.*/
        String linestr = readLineWithProperEncoding();
        if (linestr == null)
            return ParsingErrors.PARSING_ERROR;

        /* Check for termination line. */
        boolean isValid = isTerminationLine (linestr);
        return (isValid == true) ? ParsingErrors.NO_ERROR : ParsingErrors.PARSING_ERROR;
    }

    protected boolean isTerminationLine (String linestr)
    {
        assert (linestr != null);
        return linestr.equals("==========");
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
