package coderarjob.kpdfsync.lib.annotator;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import java.awt.geom.Rectangle2D;

import org.pdfclown.documents.Document;
import org.pdfclown.documents.Page;
import org.pdfclown.documents.contents.ITextString;
import org.pdfclown.files.File;
import org.pdfclown.tools.TextExtractor;
import org.pdfclown.documents.interaction.annotations.TextMarkup;
import org.pdfclown.documents.interaction.annotations.TextMarkup.MarkupTypeEnum;
import org.pdfclown.util.math.geom.Quad;
import org.pdfclown.files.SerializationModeEnum;
import org.pdfclown.documents.contents.TextChar;

import coderarjob.kpdfsync.lib.pm.AbstractMatcher;
import coderarjob.kpdfsync.lib.pm.Match;

public class PdfAnnotatorV1 extends AbstractAnnotator
{
  private final String mSourcePdfFileName;
  private final int mSkipNumberOfPages;

  private boolean mIsOpen;

  private File mFile;
  private Document mDoc;

  public PdfAnnotatorV1 (AbstractMatcher matcher, String sourcePdfFileName, int skipNumberOfPages)
  {
	super (matcher);
    this.mSourcePdfFileName = sourcePdfFileName;
    this.mSkipNumberOfPages = skipNumberOfPages;
    this.mIsOpen = false;
  }

  /* AbstractAnnotator methods */
  public String getAnnotatorVersion ()
  {
    return "1.0";
  }

  public void open () throws Exception
  {
    if (this.mIsOpen == true)
      throw new Exception ("Cannot open, till old file is closed");

    this.mFile = new File(this.mSourcePdfFileName);
    this.mDoc = this.mFile.getDocument();
    this.mIsOpen = true;
  }

  public boolean highlight (int pageOrLocationNumber, String toHighlight, String note) throws Exception
  {
    Page page = this.mDoc.getPages().get(pageOrLocationNumber + this.mSkipNumberOfPages - 1);

    TextExtractor tex = new TextExtractor();
    Map<Rectangle2D,List<ITextString>> map = tex.extract(page);
    List<ITextString> list = map.get(null);

    boolean success = doHighlight (page, toHighlight, list, note);
    return success;
  }

  public void save (String fileName) throws Exception
  {
    if (this.mIsOpen == false)
      throw new Exception ("Cannot save. No file is open");

    this.mFile.save(fileName, SerializationModeEnum.Standard);
  }

  public void close (String fileName) throws Exception
  {
    this.mIsOpen = false;
    this.mFile.close();
  }

  /* Private methods */
  private boolean doHighlight (Page page, String pattern, List<ITextString> linelist, String note)
      throws CloneNotSupportedException
  {
    /* 1. Concatinate all the lines to one place. */
    StringBuilder pagetext = new StringBuilder();
    for(ITextString ts: linelist) {
      /* Each line from page will have a newline in the end. */
      pagetext.append(ts.getText());
      pagetext.append("\n");
    }

    /* Removes the extra newline at the end. */
    pagetext.deleteCharAt(pagetext.length() - 1);

    ArrayList<Match> matches = this.mAbstractMatcher.match (pagetext.toString(), pattern);

    boolean matchfound = false;
    List<Quad> highlights = new ArrayList<>();

    for (Match m : matches)
    {
      if (m.matchPercent() < 70.0) continue;

      Rectangle2D highlightrect;

      if (m.beginFrom().lineNumber() == m.endAt().lineNumber())
      {
        /* First and last lines are the same.
         * Selection from <begin index, end index>*/
        highlightrect = getRectangle2D_Line (linelist, m.beginFrom().lineNumber(),
            m.beginFrom().index(), m.endAt().index());
        highlights.add(Quad.get(highlightrect));
      }
      else
      {
        /* First line: <index, till end> */
        highlightrect = getRectangle2D_Line (linelist, m.beginFrom().lineNumber(),
            m.beginFrom().index(), -1);
        highlights.add(Quad.get(highlightrect));

        /* Between lines: <from start, till end> */
        for (int lineNumber = m.beginFrom().lineNumber() + 1
            ; lineNumber < m.endAt().lineNumber()
            ; lineNumber++)
        {
          highlightrect = getRectangle2D_Line (linelist, lineNumber, -1, -1);
          highlights.add(Quad.get(highlightrect));
        }

        /* Last line: <from start, index> */
        highlightrect = getRectangle2D_Line (linelist, m.endAt().lineNumber(),
            -1, m.endAt().index());
        highlights.add(Quad.get(highlightrect));
      }

      matchfound = true;
      new TextMarkup(page, note, MarkupTypeEnum.Highlight, highlights);
    }

    return matchfound;
  }

  private Rectangle2D getRectangle2D_Line (List<ITextString> lineList, int lineNumber,
                                           int lineMatchStart, int lineMatchEnd)
  {
    List<TextChar> charrects = lineList.get(lineNumber - 1).getTextChars();

    lineMatchStart = (lineMatchStart < 0) ? 0 : lineMatchStart;
    lineMatchEnd = (lineMatchEnd < 0) ? charrects.size() - 1 : lineMatchEnd;

    Rectangle2D highlightrect;
    highlightrect = charrects.get(lineMatchStart).getBox();
    highlightrect.add(charrects.get(lineMatchEnd).getBox());

    /* TODO: Is this clone required here! At present it is there out of fear.*/
    return (Rectangle2D)highlightrect.clone();

  }
}
