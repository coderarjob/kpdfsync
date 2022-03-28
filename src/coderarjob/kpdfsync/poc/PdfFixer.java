package coderarjob.kpdfsync.poc;
import java.nio.file.*;
import java.io.*;
import coderarjob.kpdfsync.poc.Log.LogType;

public class PdfFixer
{
  public static String WINDOWS_SCRIPT_FILE = ".\\tools\\poppler-utils\\win_x86\\pdftocairo.bat";
  public static String UNIX_SCRIPT_FILE = "./tools/poppler-utils/unix/pdftocairo.sh";

  /*private static String WINDOWS_KILL_COMMAND = "Taskkill /F /PID %s";
  private static String UNIX_KILL_COMMAND = "kill -9 %s";*/

  private static PdfFixer mInstance = null;
  public static PdfFixer getInstance()
  {
    if (mInstance == null)
      mInstance = new PdfFixer();

    return mInstance;
  }

  private PdfFixer()
  { }

  public String getOSArchitecture() { return System.getProperty ("os.arch"); }
  public String getOSName() { return System.getProperty ("os.name"); }
  public boolean isWindows() { return getOSName().toUpperCase().startsWith ("WINDOWS"); }

  public Path generateBackupFilePath (Path sourcePdfFile)
  {
    String sourceFileWithoutExt = removeFileExtension (sourcePdfFile.toString());
    Path parentDir = sourcePdfFile.getParent();

    Path duplicateFile = null;
    int i = 0;
    do{
      String duplicatePdfFileTitle = String.format("%s_original(%d).pdf", sourceFileWithoutExt, i++);
      duplicateFile = parentDir.resolve (duplicatePdfFileTitle);
    } while (Files.exists (duplicateFile, LinkOption.NOFOLLOW_LINKS));

    return duplicateFile;
  }

  public int fix (Path sourcePdf, Path destinationPdf) throws Exception
  {
    String scriptPath = (isWindows()) ? WINDOWS_SCRIPT_FILE
                                      : UNIX_SCRIPT_FILE;
    String[] command_args = {scriptPath, sourcePdf.toString(), destinationPdf.toString()};

    Log.getInstance().log (LogType.INFORMATION, "Executing: " + String.join(" ", command_args));
    Process p = Runtime.getRuntime().exec (command_args);

    // Wait for process to complete.
    while (!Thread.currentThread().isInterrupted() && p.isAlive())
      ;

    // Exception is thrown if fixing thread was interrupted.
    if (Thread.interrupted())
    {
      p.destroy();
      throw new InterruptedException ("Fixing process was cancelled.");
    }

    int exitCode = p.exitValue();
    Log.getInstance().log (LogType.INFORMATION, "Fixing completed with exit code : %d", exitCode);

    // Process has exited successfully
    if (exitCode == 0)
      return exitCode;

    // Process has not exited successfully. Gather contents of standard error.
    String processStdErr = readAllTextFromStream (p.getErrorStream());
    Log.getInstance().log (LogType.ERROR, "Standard Error:%n%s", processStdErr);
    throw new Exception (String.format ("Exit code: %d. Error:\n %s", exitCode, processStdErr));
  }

  /*private void killAll (Process p) throws IOException
  {
    String killCommand = (isWindows()) ? WINDOWS_KILL_COMMAND
                                       : UNIX_KILL_COMMAND;
    killCommand = String.format (killCommand, p.pid());
    Runtime.getRuntime().exec (killCommand);
  }*/

  private String readAllTextFromStream (InputStream stream) throws IOException
  {
    BufferedReader reader = new BufferedReader (new InputStreamReader (stream));
    StringBuilder processStdErr = new StringBuilder ();
    String line;
    while ((line = reader.readLine()) != null)
      processStdErr.append (String.format ("%s%n", line));

    return processStdErr.toString();
  }

  private String removeFileExtension (String fileName)
  {
    int dotIndex = fileName.lastIndexOf (".");
    return (dotIndex < 0) ? fileName : fileName.substring (0, dotIndex);
  }
}
