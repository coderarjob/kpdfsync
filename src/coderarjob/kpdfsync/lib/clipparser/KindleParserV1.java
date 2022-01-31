package coderarjob.kpdfsync.lib.clipparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class KindleParserV1 extends AbstractParser
{
  protected enum ParsingStates { TITLE, ANNOTATION_LINE, TEXT, END, ERROR }
  protected ParsingStates mState;    /* Invariance: Next readline will read line for this state.*/

  public KindleParserV1 (String fileName) throws FileNotFoundException, IOException
  {
    super (fileName);
    mState = ParsingStates.TITLE;
  }

  public String getParserVersion ()
  {
    return "1.0";
  }

  public String[] getSupportedKindleVersions ()
  {
    return new String[] {"1.2.4", "1.2.5", "1.2.6"};
  }

  public String[] getSupportedFields ()
  {
    return new String[] {
      "Title", 
        "FileOffset",
        "AnnotationType", 
        "PageNumberOrLocationNumber",
        "PageNumberType",
        "Text"
    };
  }

  /* 
   * Moves to the Title of the next block from the middle of the current block. If already at
   * Title, this does nothing and simply returns. This methods, does not actually parse the lines,
   * just looks for the end of block.
   * Returns True, of next block was found, otherwise False.
   */
  public boolean moveToNextEntry () throws Exception
  {
    String linestr = null;

    while (mState != ParsingStates.TITLE)
    {
      linestr = readLineWithProperEncoding ();
      if (linestr == null)
        return false;

      if (linestr.equals("==========")) 
        mState = ParsingStates.TITLE;
    }

    return true;
  }

  /* 
   * Moves the file pointer and assumes the next line read to be Title.
   */
  public void moveToEntryAtOffset (long offset) throws Exception
  {
    mFile.seek(offset);
    mState = ParsingStates.TITLE;
  }

  /*
   * Parses each line of the current block. 
   * Returns a ParserResult object with the parsed result. 
   * Null is returned is EOF was reached, before end of block.
   */
  public ParserResult parse () throws Exception
  {
    if (mState != ParsingStates.TITLE)
      throw new ParserException ("Invalid state. Not at Title");

    boolean isValid = true;
    String linestr = null;
    ParserResult result = new ParserResult (this);

    while ((linestr = readLineWithProperEncoding()) != null)
    {
      ParsingStates pState = mState;         /* Save the current state for displaying on error*/
      mState = parse (mState, linestr, result);

      isValid = (mState != ParsingStates.ERROR);
      if (isValid == false)
        throw new ParserException (String.format ("Parsing error in line '%s' for state '%s'.",
              linestr, 
              pState));

      /* Start of next block reached. So thats the end.*/
      if (mState == ParsingStates.TITLE)
        break;
    }

    /* End of file was reached before end of block.*/
    if (linestr == null)
      result = null;

    return result;
  } 

  /*
   * Validates, adds to ParserResult and returns the next state.
   * If validation fails, ERROR state is returned.
   * 
   * TODO: Instead of this method, create separate methods for each of the states. These
   * methods will be called one after other in the order they occur - there is no need for 
   * a state machine.
   */
  protected ParsingStates parse (ParsingStates currentState, String linestr, ParserResult result)
      throws Exception
  {
      ParsingStates nextState = ParsingStates.ERROR;
      String value = "";
      boolean isValid = true;

      String annotationType = "";

switchblock:
      switch (currentState)
      {
        case TITLE:
          isValid = (linestr.length () > 0);
          if (isValid == false)
            break;

          result.setFieldValue ("Title", linestr);
          result.setFieldValue ("FileOffset", String.valueOf(this.lastFilePointer()));
          nextState = ParsingStates.ANNOTATION_LINE;
          break;

        case ANNOTATION_LINE:

          /* Annotation Type */
          value = trySplitString (linestr, " ", 2);
          isValid = (value != null) 
            && (value.toLowerCase().equals ("highlight") 
                || value.toLowerCase().equals ("note")
                || value.toLowerCase().equals ("bookmark"));

          if (isValid == false)
            break;

          result.setFieldValue ("AnnotationType", value);
          annotationType = value;

          /* Page Number Type */
          value = trySplitString (linestr, " ", 4);
          isValid = (value != null)
            && (value.toLowerCase().equals("page") 
                || value.toLowerCase().equals("location"));

          if (isValid == false)
            break;

          result.setFieldValue ("PageNumberType", value);

          /* Page or Location Number */
          value = trySplitString (linestr, " ", 5);
          isValid = (value != null);
          if (isValid == false)
            break;

          if (annotationType.toLowerCase().equals("bookmark") == false) 
          {
            value = trySplitString (value, "-", 0);
            isValid = (value != null);
            if (isValid == false)
              break;
          }

          isValid = tryParseUnsigendInt (value);
          if (isValid == false)
            break;

          result.setFieldValue ("PageNumberOrLocationNumber", value);
          nextState = ParsingStates.TEXT;
          break;

        case TEXT:
          annotationType = result.getFieldValue("AnnotationType").toLowerCase();

          /* There should be a blank line */
          isValid = (linestr.length() == 0);
          if (isValid == false)
            break;

          /* Read the actual text, in the following lines */
          StringBuilder sb = new StringBuilder();
          while ((linestr = readLineWithProperEncoding()).equals("==========") == false)
          {
            sb.append(linestr + "\n");

            /* NOTE: This line can also be blank, in case of a bookmark annotation type.*/
            if (annotationType.equals("bookmark") == false)
            {
              isValid = (linestr.length() > 0);
              if (isValid == false)
                break switchblock;
            }
          } 
          
          /* We have read one line too far. Backup */
          mFile.seek(this.lastFilePointer());

          /* Remove the extra new line at the end*/
          sb.deleteCharAt(sb.length() - 1);

          result.setFieldValue ("Text", sb.toString());
          nextState = ParsingStates.END;
          break;

        case END:
          /* Check for termination line. */
          isValid = (linestr.equals("=========="));

          nextState = ParsingStates.TITLE;
          break;
        default:
          assert false : "Invalid state";
      } // switch

      if (isValid == false)
        nextState = ParsingStates.ERROR;

      return nextState;
  }

  protected String trySplitString (String s, String p, int index)
  {
    try {
      return s.split (p)[index];
    } catch (Exception ex) {
      return null;
    }
  }

  protected boolean tryParseUnsigendInt (String s)
  {
    try {
      Integer.parseUnsignedInt (s);
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
  } 

} 
