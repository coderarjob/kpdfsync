package coderarjob.kpdfsync.lib.pm;

public class Match
{
  private int mMatchCount;
  private int mTotalCount;
  private Line mBeginFrom;
  private Line mEndAt;

  public Match (int matchCount, int totalCount, Line beginFrom, Line endAt)
  {
    this.mMatchCount = matchCount;
    this.mTotalCount = totalCount;
    this.mBeginFrom = beginFrom;
    this.mEndAt = endAt;
  }

  public float matchPercent () { return (float)this.mMatchCount/this.mTotalCount * 100; }
  public int matchCount () { return this.mMatchCount; }
  public int totalCount () { return this.mTotalCount; }
  public Line beginFrom () { return this.mBeginFrom; }
  public Line endAt () { return this.mEndAt; }

}
