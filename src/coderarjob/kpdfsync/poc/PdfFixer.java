package coderarjob.kpdfsync.poc;
import java.nio.file.*;
import java.io.*;
import coderarjob.kpdfsync.poc.Log.LogType;
import coderarjob.kpdfsync.poc.pdffixes.*;
import java.util.*;

public class PdfFixer
{
  private List<AbstractFix> mFixers;

  private static PdfFixer mInstance = null;
  public static PdfFixer getInstance()
  {
    if (mInstance == null)
      mInstance = new PdfFixer();

    return mInstance;
  }

  private PdfFixer()
  {
    mFixers = new ArrayList<>();
    mFixers.add (new pdftocairoFix());
  }

  public String getOSArchitecture() throws Exception
  {
    String osArch = System.getProperty ("os.arch");
    if (osArch == null)
      throw new Exception ("OS name is not determined.");
    return osArch;

  }

  public String getOSName() throws Exception
  {
    String osName = System.getProperty ("os.name");
    if (osName == null)
      throw new Exception ("OS name is not determined.");
    return osName;
  }

  public boolean isWindows() throws Exception
  { return getOSName().toUpperCase().startsWith ("WINDOWS"); }

  public void apply (Path sourcePdf, Path destinationPdf) throws Exception
  {
    for (AbstractFix fixer : mFixers)
      fixer.apply (isWindows(), sourcePdf, destinationPdf);
  }

  public Path createBackup (Path sourcePdf) throws IOException
  {
    // Determine duplicate file name
    Path duplicatePdf = generateBackupFilePath (sourcePdf);

    // Back up the original file before fixing it.
    Log.getInstance().log (LogType.INFORMATION, "Backing up '%s' -> '%s'"
                                              , sourcePdf.toString()
                                              , duplicatePdf.toString());

    Files.copy (sourcePdf, duplicatePdf);
    return duplicatePdf;
  }

  private Path generateBackupFilePath (Path sourcePdfFile)
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

  private String removeFileExtension (String fileName)
  {
    int dotIndex = fileName.lastIndexOf (".");
    return (dotIndex < 0) ? fileName : fileName.substring (0, dotIndex);
  }
}
