package coderarjob.kpdfsync.lib.pm;

public class BasicMatcher extends AbstractMatcher
{
  public Match matchPattern(String line, int lineOffset, String pattern)
  {
    int mc = 0;
    int i = 0;

    line = line.substring(lineOffset);

    for (; i < pattern.length() && i < line.length(); i++ )
    {
      if (line.charAt(i) == pattern.charAt(i))
        mc++;
    }

    Line begin = new Line(1, lineOffset);
    Line end = new Line(1, lineOffset + i - 1);
    return new Match(mc, i, begin, end, pattern);
  }
}
