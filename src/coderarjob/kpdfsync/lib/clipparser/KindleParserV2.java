package coderarjob.kpdfsync.lib.clipparser;

import java.io.FileNotFoundException;
import java.io.IOException;

public class KindleParserV2 extends KindleParserV1
{
  public KindleParserV2 (String fileName) throws FileNotFoundException, IOException
  {
    super (fileName);
  }

  public String[] getSupportedKindleVersions ()
  {
    return new String[] {"1.2.7"};
  }

  public String[] getSupportedFields ()
  {
    return new String[] {
                         "Title", 
                         "AnnotationType", 
                         "PageNumberOrLocationNumber",
                         "PageNumberType",
                         "Text",
                         "CreationDate"
                        };
  }

  protected ParsingStates parse (ParsingStates currentState, String linestr, ParserResult result)
    throws Exception
  {
    if (currentState != ParsingStates.ANNOTATION_LINE)
      return super.parse (currentState, linestr, result);

    boolean isValid = true;

    ParsingStates nState = super.parse (currentState, linestr, result);
    if (nState != ParsingStates.ERROR)
    {
      /* Created date is in the Annotation line, beginning with '| Added on' string. */
      String value = trySplitString (linestr, "\\| Added on", 1);

      /* Valid creation date/time string must be greater than zero. */
      isValid = (value != null) && (value.length() > 0);
      if (isValid == true)
        result.setFieldValue ("CreationDate", value);
    }

    if (isValid == false)
      nState = ParsingStates.ERROR;

    return nState;
  }

}
