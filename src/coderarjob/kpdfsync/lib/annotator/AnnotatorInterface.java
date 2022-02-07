package coderarjob.kpdfsync.lib.annotator;

public interface AnnotatorInterface
{
  public String getAnnotatorVersion ();
  public void open () throws Exception;
  public void close (String fileName) throws Exception;
  public boolean highlight (int pageOrLocationNumber, String toHighlight, String note) throws Exception;
  public void save (String fileName) throws Exception;
}
