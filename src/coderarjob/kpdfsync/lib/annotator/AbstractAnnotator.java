package coderarjob.kpdfsync.lib.annotator;

import coderarjob.kpdfsync.lib.pm.AbstractMatcher;

public abstract class AbstractAnnotator
{
  /* Abstract methods */
  public abstract String getAnnotatorVersion ();
  public abstract void open () throws Exception;
  public abstract void close () throws Exception;
  public abstract boolean highlight (int pageOrLocationNumber, String toHighlight, String note) throws Exception;
  public abstract void save (String fileName) throws Exception;

  /* Protected field */
  protected final AbstractMatcher mAbstractMatcher;
  protected float mMatchThreshold;

  /* Constructor */
  public AbstractAnnotator (AbstractMatcher matcher, float defaultMatchThreshold)
  {
	this.mAbstractMatcher = matcher;
    this.mMatchThreshold = defaultMatchThreshold;
  }

  /* Instance methods */
  public float getMatchThreshold ()
  {
    return this.mMatchThreshold;
  }

  public void setMatchThreshold (float v)
  {
    this.mMatchThreshold = v;
  }

}
