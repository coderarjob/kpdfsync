package coderarjob.kpdfsync.poc;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.LocalTime;
import java.time.LocalDate;

public class Log
{
  public enum LogType
  {
    UNSET, INFORMATION, WARNING, ERROR;
  }

  private static Log mInstance;
  public static final String LOG_FILE;

  private BufferedWriter mWriter;
  private PrintWriter mPrinter;
  private DateTimeFormatter mLogDateTimeFormatter;
  private LocalTime mPrev;      // Used to determine passage of time from last log.
  private LogType mPrevLogType; // Used with mPrev to determine if discontinuation is needed.

  static
  {
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern ("Hms");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern ("YMd");
    LOG_FILE = String.format ("log_%s_%s.txt", LocalDate.now().format (dateFormatter)
                                             , LocalTime.now().format (timeFormatter));
  }

  private Log (String fileName) throws IOException
  {
    mWriter = new BufferedWriter (new FileWriter (fileName, false));
    mPrinter = new PrintWriter (mWriter);
    mLogDateTimeFormatter = DateTimeFormatter.ofPattern ("HH:mm:ss");
    mPrev = LocalTime.now();
    mPrevLogType = LogType.UNSET;
  }

  public static Log getInstance()
  {
    if (mInstance == null)
    {
      try {
        mInstance = new Log(Log.LOG_FILE);
      } catch (IOException ex) {
        new RuntimeException (ex);
      }
    }

    return mInstance;
  }

  public void log (Exception ex)
  {
    String exceptionMessage = ex.getMessage() == null ? ex.toString() : ex.getMessage();

    log (LogType.ERROR, "Exception :" + exceptionMessage);
    ex.printStackTrace (mPrinter);

    Throwable cause = ex.getCause();
    while (cause != null)
    {
      cause.printStackTrace(mPrinter);
      cause = cause.getCause();
    }
  }

  public void log (LogType type, String fmt, Object... values)
  {
    String logText = "";

    String datetimeText = LocalTime.now().format (mLogDateTimeFormatter);
    boolean continueWithPrevious = ChronoUnit.SECONDS.between (mPrev, LocalTime.now()) < 1;
    continueWithPrevious = continueWithPrevious & type == mPrevLogType;

    mPrev = LocalTime.now();
    mPrevLogType = type;

    switch (type)
    {
      case INFORMATION:
        if (!continueWithPrevious)
          logText = "%n-----[" + datetimeText + "]-----%n";

        logText += fmt + "%n";
        logText = String.format (logText, values);  // call to format is required to parse the %n.

        break;
      case ERROR:
        if (!continueWithPrevious)
          logText = "%n-----[ERROR] [" + datetimeText + "]-----%n";

        logText += fmt + "%n";
        logText = String.format (logText, values);
        break;
      case WARNING:
        if (!continueWithPrevious)
          logText = "%n-----[WARN] [" + datetimeText + "]-----%n";

        logText += fmt + "%n";
        logText = String.format (logText, values);
        break;
    }

    try {
      mWriter.write (logText);
    } catch (IOException ex) {
      new RuntimeException (ex);
    }
  }

  public void flush()
  {
    try {
      mWriter.flush();
    } catch (IOException ex) {
      new RuntimeException (ex);
    }
  }
}
