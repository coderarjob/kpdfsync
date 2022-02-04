package coderarjob.kpdfsync.lib.pm;

public class Line
{
  private int mLineNumber;
  private int mIndex;

  public Line (int lineNumber, int index)
  {
    this.mLineNumber = lineNumber;
    this.mIndex = index;
  }

  public int lineNumber() { return this.mLineNumber; }
  public int index()      { return this.mIndex; }
}
