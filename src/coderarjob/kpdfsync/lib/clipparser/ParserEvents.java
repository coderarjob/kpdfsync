package coderarjob.kpdfsync.lib.clipparser;

public interface ParserEvents
{
  public void onError (String fileName, long offset, String error, ParserResult result);
  public void onSuccess (String fileName, long offset, ParserResult result);
}
