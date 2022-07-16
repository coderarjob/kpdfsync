/*
 * KindleParserV2 Class
 *
 * Based on KindleParserV1, but minor change in Annotation line parsing.
 *
 * Dated: 11 July 2022
 * Author: arjobmukherjee@gmail.com
 */

package coderarjob.kpdfsync.lib.clipparser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

import coderarjob.kpdfsync.lib.clipparser.ParserResult.SupportedFields;
import coderarjob.kpdfsync.lib.clipparser.ParserResult.AnnotationType;
import coderarjob.kpdfsync.lib.clipparser.ParserResult.PageNumberType;

public class KindleParserV2 extends KindleParserV1
{
    private final int ANNOTATION_TYPE_WORD_POS = 1;
    private final int PAGE_NUMBER_TYPE_PAGE_WORD_POS = 3;
    private final int PAGE_NUMBER_TYPE_LOCATION_WORD_POS = 2;
    private final int PAGE_NUMBER_WORD_POS = 4;
    private final int LOCATION_NUMBER_WORD_POS = 3;

    public KindleParserV2 (String fileName) throws FileNotFoundException, IOException
    {
        super (fileName);
    }

    public static String getParserName()
    {
      return "Kindle Clippings - Older kindles";
    }

    public String toString()
    {
      return KindleParserV2.getParserName();
    }

    /* Implementing abstract methods from AbstractParser*/
    public String getParserVersion ()
    {
        return "2.0";
    }

    public String[] getSupportedKindleVersions ()
    {
        return new String[] {"3.4.3", "older"};
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

          return new ParserResultFieldsFilter<> (1, ht);
        }

        public ParserResultFieldsFilter<PageNumberType> getPageNumberTypeFilter(ParserResult res)
        {
          Hashtable<String, PageNumberType> ht = new Hashtable<>();
          ht.put("page", PageNumberType.PAGE_NUMBER);
          ht.put("location", PageNumberType.LOCATION_NUMBER);
          ht.put("loc.", PageNumberType.LOCATION_NUMBER);

          return new ParserResultFieldsFilter<> (3, ht);
        }

        public ParserResultFieldsFilter<Object> getPageOrLocationNumberFilter(ParserResult res)
        {
          return new ParserResultFieldsFilter<> (4, null);
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
        ht.put("loc.", PageNumberType.LOCATION_NUMBER);

        // Is Page Number Type = Page?
        String value = trySplitString (linestr, " ", PAGE_NUMBER_TYPE_PAGE_WORD_POS).toLowerCase();
        PageNumberType pageNumberType = ht.getOrDefault(value,PageNumberType.UNKNOWN);
        if (pageNumberType == PageNumberType.UNKNOWN)
        {
          // Not 'Page or Location', Try another location.
          value = trySplitString (linestr, " ", PAGE_NUMBER_TYPE_LOCATION_WORD_POS).toLowerCase();
          pageNumberType = ht.getOrDefault(value,PageNumberType.UNKNOWN);
          if (pageNumberType == PageNumberType.UNKNOWN)
            throw genParserException (SupportedFields.PAGE_NUMBER_TYPE.getName());
        }

        result.setFieldValue (SupportedFields.PAGE_NUMBER_TYPE, pageNumberType.getName());
    }

    protected void parsePageOrLocationNumber (ParserResult result)
        throws IOException, ParserException
    {
        boolean isValid = false;
        String value = "";

        String linestr = this.lastLineRead();
        AnnotationType annotationType = result.annotationType();
        PageNumberType pageNumberType = result.pageNumberType();

        /* Page or Location Number */
        if (pageNumberType == PageNumberType.PAGE_NUMBER)
          value = trySplitString (linestr, " ", PAGE_NUMBER_WORD_POS);
        else if (pageNumberType == PageNumberType.LOCATION_NUMBER)
          value = trySplitString (linestr, " ", LOCATION_NUMBER_WORD_POS);
        else
          throw genParserException (SupportedFields.PAGE_OR_LOCATION_NUMBER.getName());

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
}
