package coderarjob.kpdfsync.poc;

import java.util.ArrayList;
import java.util.Collections;

import coderarjob.kpdfsync.lib.*;
import coderarjob.kpdfsync.lib.clipparser.ParserResult;
import coderarjob.kpdfsync.lib.clipparser.ParserResult.AnnotationType;
import coderarjob.kpdfsync.lib.clipparser.ParserResult.PageNumberType;

import coderarjob.kpdfsync.lib.annotator.AnnotatorInterface;
import coderarjob.kpdfsync.lib.annotator.PdfAnnotatorV1;

public class Main
{
  public static void main(String[] args) throws Exception 
  {
    KindleClippingsFile file = new KindleClippingsFile("test-files/My Clippings.txt");
    ArrayList<String> titles = Collections.list(file.getBookTitles());

    System.out.println ("Book titles");
    for (String title : titles)
      System.out.println ("\t" + title);

    System.out.println ("---------------------------------");


    AnnotatorInterface ann = new PdfAnnotatorV1 ("test-files/progit.pdf", 6);
    ann.open ();

    String bookTitle = titles.get(3);
    System.out.println ("Book: " + bookTitle);

    ArrayList<ParserResult> entries = Collections.list (file.getBookAnnotations (bookTitle));
    for (ParserResult entry : entries)
    {
      if (entry.annotationType() != AnnotationType.HIGHLIGHT)
        continue;
      /*if (entry.pageOrLocationNumber() != 53)
        continue;*/

      //displayParserResult (entry);
      System.out.print ("Highlighting page: " + entry.pageOrLocationNumber() + " ");
      ann.highlight (entry.pageOrLocationNumber(), entry.text(), "Test highlight");
    }

    ann.save ("output.pdf");

  }

  private static void displayClippingsForFile (KindleClippingsFile file, String bookTitle)
      throws Exception
  {
    ArrayList<ParserResult> entries = Collections.list (file.getBookAnnotations (bookTitle));
    for (ParserResult entry : entries)
      displayParserResult (entry);
  }

  private static void displayParserResult (ParserResult entry) throws Exception
  {
    System.out.println ("\t" + entry.title());
    System.out.println ("\t" + entry.annotationType());

    int pageOrLocationNumber = entry.pageOrLocationNumber();
    if (entry.pageNumberType() == PageNumberType.PAGE_NUMBER)
      System.out.println ("\tPage: " + String.valueOf(pageOrLocationNumber));
    else
      System.out.println ("\tLocation: " + String.valueOf(pageOrLocationNumber));

    if (entry.annotationType() != AnnotationType.BOOKMARK)
      System.out.println ("\t" + entry.text());
    System.out.println ("---------------------------------");
  }
}
