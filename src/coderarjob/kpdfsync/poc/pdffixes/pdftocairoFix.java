package coderarjob.kpdfsync.poc.pdffixes;

import java.nio.file.*;

public class pdftocairoFix extends AbstractFix
{
  public static String WINDOWS_SCRIPT_FILE = ".\\tools\\poppler-utils\\win_x86\\pdftocairo.bat";
  public static String UNIX_SCRIPT_FILE = "./tools/poppler-utils/unix/pdftocairo.sh";

  public String name()
  {
    return "pdftocairo (poppler-utils)";
  }

  public int apply(boolean isWindows, Path sourcePdf, Path destinationPdf) throws Exception
  {
    String scriptPath = (isWindows) ? WINDOWS_SCRIPT_FILE : UNIX_SCRIPT_FILE;
    String[] command_args = {scriptPath, sourcePdf.toString(), destinationPdf.toString()};

    int exitCode = execute (command_args);
    return exitCode;
  }
}
