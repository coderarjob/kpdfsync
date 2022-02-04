package coderarjob.kpdfsync.lib.clipparser;

import java.util.Hashtable;
import coderarjob.kpdfsync.lib.clipparser.ParserException;

public class ParserResult
{
  Hashtable<String, String> mFieldDict;

  public ParserResult (AbstractParser parser)
  {
    mFieldDict = new Hashtable<>();

    /* Initialize hash table with the supported parser fields */
    for (String field : parser.getSupportedFields ())
      mFieldDict.put (field, "");
  }

  public void setFieldValue (String field, String value) 
      throws NullPointerException, ParserException
  {
      if (mFieldDict.containsKey (field) == false)
        throw new ParserException ("Unsupported field : " + field);

      mFieldDict.put (field, value);
  }

  public String getFieldValue (String field) 
      throws NullPointerException, ParserException
  {
      if (mFieldDict.containsKey (field) == false)
        throw new ParserException ("Unsupported field : " + field);

      return mFieldDict.get (field);
  }
}
