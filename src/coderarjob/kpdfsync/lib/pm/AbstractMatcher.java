package coderarjob.kpdfsync.lib.pm;

import java.util.ArrayList;

public abstract class AbstractMatcher
{
  /* Abstract methods */
  protected abstract Match matchPattern(String line, int lineOffset, String pattern);

  /* Private fields */
  private PatternMatcherEvents mMatcherEventsHandler;

  /* Getter and Setters */
  public void setPatternMatcherEventsHandler (PatternMatcherEvents matcherEventsHandler)
  {
	this.mMatcherEventsHandler = matcherEventsHandler;
  }

  /* Class method */
  public ArrayList<Match> match (String text, String pattern)
  {
    String shortlines = text.replaceAll("[  |\t|\n|\r]", "");
    String shortpattern = pattern.replaceAll("[  |\t|\n|\r]", "");

    onMatchStart(text, pattern);

    int length = shortlines.length() - shortpattern.length() + 1;
    ArrayList<Match> matches = new ArrayList<>();

    for (int j = 0; j < length; j++)
    {
      Match res = matchPattern (shortlines, j, shortpattern);
      if (res.matchCount() == 0) continue;

      Line firstLine = remapLine (res.beginFrom().index(), text);
      Line endLine = remapLine (res.endAt().index(), text);

      Match thisMatch = new Match(res.matchCount(), res.totalCount(), firstLine, endLine, pattern);
      onMatchEnd (thisMatch);

      matches.add(thisMatch);
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

      if (line.charAt(i) == '\t')
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

  /* Hook methods */
  protected void onMatchEnd (Match match)
  {
	if (this.mMatcherEventsHandler != null)
	  this.mMatcherEventsHandler.onMatchEnd(match);
  }

  protected void onMatchStart (String text, String pattern)
  {
	if (this.mMatcherEventsHandler != null)
	  this.mMatcherEventsHandler.onMatchStart(text, pattern);
  }
}

