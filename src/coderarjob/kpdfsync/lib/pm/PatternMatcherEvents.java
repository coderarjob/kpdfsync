package coderarjob.kpdfsync.lib.pm;

public interface PatternMatcherEvents
{
  public void onMatchStart (String text, String pattern);
  public void onMatchEnd (Match result);
}
