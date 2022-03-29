package coderarjob.kpdfsync.poc.pdffixes;

import java.nio.file.*;
import java.io.*;
import coderarjob.kpdfsync.poc.Log.LogType;
import coderarjob.kpdfsync.poc.Log;

public abstract class AbstractFix
{
  public abstract String name();
  public abstract int apply(boolean isWindows, Path sourcePdf,
                                               Path destinationPdf) throws Exception;

  protected int execute (String[] commandArgs) throws Exception
  {
    Log.getInstance().log (LogType.INFORMATION, "Executing: " + this.name());
    Log.getInstance().log (LogType.INFORMATION, "Command: " + String.join(" ", commandArgs));

    Process p = Runtime.getRuntime().exec (commandArgs);

    // Wait for process to complete.
    while (!Thread.currentThread().isInterrupted() && p.isAlive())
      ;

    // Exception is thrown if fixing thread was interrupted.
    if (Thread.interrupted())
    {
      p.destroy();
      throw new InterruptedException ("Fixing was cancelled.");
    }

    int exitCode = p.exitValue();
    Log.getInstance().log (LogType.INFORMATION, "Process completed with exit code : %d", exitCode);

    // Process has exited successfully
    if (exitCode == 0)
      return exitCode;

    // Process has not exited successfully. Gather contents of standard error.
    String processStdErr = readAllTextFromStream (p.getErrorStream());
    Log.getInstance().log (LogType.ERROR, "Standard Error:%n%s", processStdErr);
    throw new Exception (String.format ("Exit code: %d. Error:\n %s", exitCode, processStdErr));
  }

  private String readAllTextFromStream (InputStream stream) throws IOException
  {
    BufferedReader reader = new BufferedReader (new InputStreamReader (stream));
    StringBuilder processStdErr = new StringBuilder ();
    String line;
    while ((line = reader.readLine()) != null)
      processStdErr.append (String.format ("%s%n", line));

    return processStdErr.toString();
  }
}
