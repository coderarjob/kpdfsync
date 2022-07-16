/*
 * AbstractKindleParserConstants Class
 *
 * Parsing of a Clippings file depends on a few constancts. These contants is determines a
 * particular format of the clippings file.
 *
 * Dated: 11 July 2022
 * Author: arjobmukherjee@gmail.com
 */

package coderarjob.kpdfsync.lib.clipparser;

import coderarjob.kpdfsync.lib.clipparser.ParserResult.AnnotationType;
import coderarjob.kpdfsync.lib.clipparser.ParserResult.PageNumberType;
import java.util.List;

abstract class AbstractKindleParserConstants
{
  /** Annotation type position and patters to identify annotation types.
   */
  public abstract
    List<ParserResultFieldsFilter<AnnotationType>> getAnnotationTypeFilter(ParserResult res);

  /** Page number type position and patters to identify annotation types.
   */
  public abstract
    ParserResultFieldsFilter<PageNumberType> getPageNumberTypeFilter(ParserResult res);

  /** Page number word position.
   * There is no pattern to match here.
   */
  public abstract
    ParserResultFieldsFilter<Object> getPageOrLocationNumberFilter(ParserResult res);

  /** Pattern by which the end of a block (i.e Termination line) is recognized. */
  public abstract ParserResultFieldsFilter<Boolean> getTerminationLineFilter();
}
