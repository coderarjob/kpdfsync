package coderarjob.kpdfsync.lib.annotator;

import coderarjob.kpdfsync.lib.pm.AbstractMatcher;

public abstract class AbstractAnnotator
{
  /* Abstract methods */
  public abstract String getAnnotatorVersion ();
  public abstract void open () throws Exception;
  public abstract void close (String fileName) throws Exception;
  public abstract boolean highlight (int pageOrLocationNumber, String toHighlight, String note) throws Exception;
  public abstract void save (String fileName) throws Exception;

  /* Protected field */
  protected final AbstractMatcher mAbstractMatcher;

  /* Constructor */
  public AbstractAnnotator (AbstractMatcher matcher)
  {
	this.mAbstractMatcher = matcher;
  }
}
