package coderarjob.kpdfsync.lib.pm;

import java.util.ArrayList;

class Line
{
  private int mLineNumber;
  private int mIndex;

  public Line (int lineNumber, int index)
  {
    this.mLineNumber = lineNumber;
    this.mIndex = index;
  }

  public int lineNumber () { return this.mLineNumber; }
  public int index () { return this.mIndex; }
}

abstract class AbstractMatcher
{
  protected abstract Match matchPattern(String line, int lineOffset, String pattern);

  public ArrayList<Match> match (String text, String pattern)
  {
    String shortlines = text.replaceAll("[  |\n|\r]", "");
    String shortpattern = pattern.replaceAll("[  |\n|\r]", "");

    int length = shortlines.length() - shortpattern.length() + 1;

    ArrayList<Match> matches = new ArrayList<>();

    for (int j = 0; j < length; j++)
    {
      Match res = matchPattern (shortlines, j, shortpattern);
      if (res.matchCount() == 0) continue;

      Line firstLine = remapLine (res.beginFrom().index(), text);
      Line endLine = remapLine (res.endAt().index(), text);

      matches.add(new Match(res.matchCount(), res.totalCount(), firstLine, endLine));

    }

    return matches;
  }

  private Line remapLine(int index, String line)
  {
    int numc = index + 1;      /* Number of characters left to be read. */

    if (numc > line.length())  /* It is not possible to read more than the length of the line. */
      return null;

    int lineno = 1;     /* Number of new lines encounterd + 1. */
    int relIndex = 0;   /* Number of characters after the last new line.*/

    int i = 0;
    for (; numc > 0; i++, relIndex++)
    {
      if (line.charAt(i) == ' ') 
        continue;

      if (line.charAt(i) == '\n') 
      {
        lineno++;
        relIndex = -1;
        continue;
      }

      numc--;
    }

    return new Line(lineno, --relIndex);
  }
}

