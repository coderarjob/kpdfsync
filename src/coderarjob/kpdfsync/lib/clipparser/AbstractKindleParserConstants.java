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

public abstract class AbstractKindleParserConstants
{

  /** When annotation line split by spaces, this is the index at which annotation type (Note or
   * Highlight) can be found.
   */
  public abstract int getAnnotationLineTypePosition ();

  /** When annotation line split by spaces, this is the index at which Page number type (Page or
   * location) can be found.
   */
  public abstract int getAnnotationLinePageNumberTypePosition ();

  /** When annotation line split by spaces, this is the index at which Page number or location
   * number can be found.
   */
  public abstract int getAnnotationLinePageOrLocationNumberPosition ();

  /** Pattern by which the end of a block (i.e Termination line) is recognized. */
  public abstract String getTeminationLinePattern ();
}
