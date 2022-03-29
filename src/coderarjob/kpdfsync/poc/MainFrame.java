package coderarjob.kpdfsync.poc;

import javax.swing.*;
import java.awt.event.*;

import java.io.*;
import java.nio.file.*;

import java.util.ArrayList;
import java.util.Collections;

import coderarjob.kpdfsync.lib.clipparser.*;
import coderarjob.kpdfsync.lib.clipparser.ParserResult.AnnotationType;
import coderarjob.kpdfsync.lib.*;
import coderarjob.kpdfsync.lib.pm.*;
import coderarjob.kpdfsync.poc.Log.LogType;
import coderarjob.kpdfsync.lib.annotator.*;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainFrame extends javax.swing.JFrame
{
  private enum ApplicationStatus
  {
    NOT_STARTED,
    CLIPPINGS_FILE_PARSE_STARTED, CLIPPINGS_FILE_PARSE_COMPLETED, CLIPPINGS_FILE_PARSE_FAILED,
    BOOK_TITLE_SELECTED,
    PDF_SELECTED,
    HIGHLIGHT_STARTED, HIGHLIGHT_COMPLETED, HIGHLIGHT_FAILED,
    FIX_STARTED, FIX_COMPLETED, FIX_FAILED
  }

  private enum StatusTypes
  {
    ERROR, INFORMATION, WARNING
  }

  /* Private fields */
  private AbstractParser mParser;
  private KindleClippingsFile mClippingsFile;
  private AbstractAnnotator mAnnotator;
  private AbstractMatcher mMatcher;

  private Thread highlightThread, parseClippingsThread, fixerThread;

  // Other Swing variable declarations
  private DefaultListModel<String> statusListModel;
  private DefaultListModel<HighlightNotePair> highlightsListModel;
  private DefaultListModel<PageResource> pageNumbersListModel;

  private HighlightNotePairManager mPairManager;
  private NoteAssociationDialog mNoteForm;

  private FileFilter clippingsFileFilter;
  private FileFilter pdfFileFilter;

  /* Constructor and other methods*/
  public MainFrame()
  {
    statusListModel = new DefaultListModel<> ();
    pageNumbersListModel = new DefaultListModel<> ();
    highlightsListModel = new DefaultListModel<> ();

    mNoteForm = new NoteAssociationDialog (this);

    clippingsFileFilter = new FileNameExtensionFilter ("Kindle Clippings file", "txt");
    pdfFileFilter = new FileNameExtensionFilter ("PDF file", "pdf");

	initComponents();
    setStatus (ApplicationStatus.NOT_STARTED);
  }

  /* Kindle Parser event handler*/
  private void parserErrorHander (String fileName, long offset, String error, ParserResult result)
  {
    addStatusLine (StatusTypes.WARNING, "'%s' at %d", error, offset);
  }

  /* Matcher event handler */
  private void matchCompletedEventHandler (Match match)
  {
    if (match.matchPercent() > mAnnotator.getMatchThreshold())
    {
      String fmt = "%.30s... %.0f%% matched on line %d.";
      StatusTypes type = (match.matchPercent() == 100) ? StatusTypes.INFORMATION
                                                       : StatusTypes.WARNING;

      addStatusLine (type, fmt, match.pattern(), match.matchPercent()
                                               , match.beginFrom().lineNumber());
    }
  }

  /* Button and other UI event handlers*/
  private void browseClippingsFileButtonActionPerformed(ActionEvent evt)
  {
    fileChooser.removeChoosableFileFilter (pdfFileFilter);
    fileChooser.addChoosableFileFilter (clippingsFileFilter);
    if (fileChooser.showOpenDialog (this) != JFileChooser.APPROVE_OPTION)
      return;

    setStatus (ApplicationStatus.CLIPPINGS_FILE_PARSE_STARTED);

    File file= fileChooser.getSelectedFile ();
    clippingsFileTextBox.setText (file.getAbsolutePath());

    parseClippingsThread = new Thread (new Runnable () {
      public void run () {
        try {
          addStatusLine (StatusTypes.INFORMATION, "Parsing %s ...", file.getName());
          createNewParser (file.getAbsolutePath());
          mClippingsFile = new KindleClippingsFile(mParser);

          ArrayList<String> titles = Collections.list(mClippingsFile.getBookTitles());

          for (String title : titles)
            updateUIBooksComboBox (title);

          if (titles.size() == 0)
            throw new Exception ("Empty or invalid clippings file.");

          addStatusLine (StatusTypes.INFORMATION, "Parsing complete");
          setStatus (ApplicationStatus.CLIPPINGS_FILE_PARSE_COMPLETED);

        } catch (InterruptedException ex) {
          addStatusLine (StatusTypes.WARNING, "Parsing canceled");
          setStatus (ApplicationStatus.CLIPPINGS_FILE_PARSE_FAILED);
        }
        catch (Exception ex)
        {
          addStatusLine (StatusTypes.ERROR, "Parsing failed: " + ex.getMessage());
          setStatus (ApplicationStatus.CLIPPINGS_FILE_PARSE_FAILED);
          reportException (ex);
        }
      }
    });
    parseClippingsThread.start();
  }

  private void browsePdfFileButtonActionPerformed(ActionEvent evt)
  {
    fileChooser.removeChoosableFileFilter (clippingsFileFilter);
    fileChooser.addChoosableFileFilter (pdfFileFilter);
    if (fileChooser.showOpenDialog (this) == JFileChooser.APPROVE_OPTION)
    {
      File file= fileChooser.getSelectedFile ();
      selectPdfFileTextBox.setText (file.getAbsolutePath());
      setStatus (ApplicationStatus.PDF_SELECTED);
    }
  }

  private void proceedButtonActionPerformed(ActionEvent evt)
  {
    String pdfFileName = selectPdfFileTextBox.getText();
    int numberOfPagesToSkip = (Integer)pdfSkipPagesSpinner.getValue();
    int matchThreshold = (Integer)matchThressholdSpinner.getValue();

    fileChooser.removeChoosableFileFilter (clippingsFileFilter);
    fileChooser.addChoosableFileFilter (pdfFileFilter);
    if (fileChooser.showSaveDialog (this) != JFileChooser.APPROVE_OPTION)
      return;

    String destinationPdfFileName = fileChooser.getSelectedFile().getAbsolutePath();

    highlightThread = new Thread (new Runnable () {
      public void run() {
        try {
          setStatus (ApplicationStatus.HIGHLIGHT_STARTED);
          mAnnotator = new PdfAnnotatorV1 (mMatcher, pdfFileName, numberOfPagesToSkip);
          mAnnotator.setMatchThreshold (matchThreshold);
          mAnnotator.open ();

          if (mPairManager.isComplete() == false)
          {
            int option =
            JOptionPane.showOptionDialog (null,
                                          "Not all notes have been paired. Continue?",
                                          "Continue?",
                                          JOptionPane.YES_NO_OPTION,
                                          JOptionPane.QUESTION_MESSAGE,
                                          null, null, null);
            if (option == JOptionPane.NO_OPTION)
                throw new Exception ("Cannot continue. Please ensure all the notes are mapped.");
          }

          int totalHighlightCount = mPairManager.count();
          int highlightIndex = 0;
          for (Integer pageNum : mPairManager.getPageNumbers())
          {
            PageResource res = mPairManager.getPageResourceByPageNumber (pageNum);
            for (HighlightNotePair pair : res.getPairs())
            {
              boolean okay;

              Log.getInstance().log (LogType.INFORMATION
                                     , "%n\t[Highlighting] '%s' with note '%s' @ page %d"
                                     , pair.getHighlightText()
                                     , pair.getNoteText()
                                     , pageNum);

              okay = mAnnotator.highlight (pageNum, pair.getHighlightText(), pair.getNoteText());
              if (!okay)
                addStatusLine (StatusTypes.ERROR, "%.30s... not found in page %d."
                                                , pair.getHighlightText(), pair.getPageNumber());

              float progress = (float) highlightIndex/(totalHighlightCount - 1) * 100;
              updateUIProgressBar ((int)progress);
              highlightIndex++;
            }
          }

          // Save pdf to a new file.
          addStatusLine (StatusTypes.INFORMATION, "Saving to file " + destinationPdfFileName);
          mAnnotator.save (destinationPdfFileName);

          addStatusLine (StatusTypes.INFORMATION, "Highlighting complete.");
          setStatus (ApplicationStatus.HIGHLIGHT_COMPLETED);
        } catch (InterruptedException ex) {
          addStatusLine (StatusTypes.WARNING, "Highlight canceled.");
          setStatus (ApplicationStatus.HIGHLIGHT_FAILED);
        } catch (Exception ex)
        {
          addStatusLine (StatusTypes.ERROR, "Highlighting failed.");
          setStatus (ApplicationStatus.HIGHLIGHT_FAILED);
          reportException (ex);
        }
      }
    });
    highlightThread.start();
  }

  private void pageNumbersListValueChanged(javax.swing.event.ListSelectionEvent evt)
  {
    if (evt.getValueIsAdjusting() == true)
      return;

    if (pageNumbersList.getSelectedValue() == null)
      return;

    PageResource res = pageNumbersList.getSelectedValue();
    int pageNumber = res.getPageNumber();

    Log.getInstance().log (LogType.INFORMATION, "[PageNumberList] selected page: " + pageNumber);

    // Clear highlights and notes
    highlightsListModel.clear();

    // Add highlights and notes
    try {
      for (HighlightNotePair pair : res.getPairs()) {
        highlightsListModel.addElement (pair);
      }
    } catch (Exception ex) {
      addStatusLine (StatusTypes.ERROR, ex.getMessage ());
      reportException (ex);
    }
  }

  private void highlightsListValueChanged(javax.swing.event.ListSelectionEvent evt)
  {
  }

  private void optionsButtonActionPerformed(ActionEvent evt)
  {
  }

  private void exitButtonActionPerformed(ActionEvent evt)
  {
    String buttonText = exitButton.getText().toUpperCase ();
    boolean isExit = buttonText.equals ("EXIT");

    Log.getInstance().log (LogType.INFORMATION, (isExit) ? "Exit button pressed."
                                                         : "Cancelling Operation");

    if (isExit)
    {
      WindowEvent windowClosing = new WindowEvent (this, WindowEvent.WINDOW_CLOSING);
      this.dispatchEvent (windowClosing);
    }
    else
    {
      if (highlightThread != null && highlightThread.isAlive ())
        highlightThread.interrupt();

      if (parseClippingsThread != null && parseClippingsThread.isAlive ())
        parseClippingsThread.interrupt();

      if (fixerThread != null && fixerThread.isAlive ())
        fixerThread.interrupt();
    }
  }

  private void highlightsListMouseClicked(java.awt.event.MouseEvent evt)
  {
    JList<HighlightNotePair> list = (JList<HighlightNotePair>)evt.getSource();
    HighlightNotePair pair = list.getSelectedValue();

    if (pair == null) return;               // Nothing is selected.
    if (evt.getClickCount() != 2) return;   // Not double click.

    PageResource res = mPairManager.getPageResourceByPageNumber (pair.getPageNumber());

    try {
      switch (mNoteForm.showDialog (this, res))
      {
        case CANCEL:
          // Nothing to be done. Just break.
          break;
        case CREATE:
          String noteText = mNoteForm.getSelectedNoteText();

          Log.getInstance().log (LogType.INFORMATION
                                , "[pair create] Highlight: '%s'%n\tNote: '%s'"
                                , pair.getHighlightText()
                                , noteText);

          if (noteText != null) {
            Log.getInstance().log (LogType.INFORMATION, "[pair create] Created.");
            res.pair (pair.getHighlightText(), noteText);
          } else {
            Log.getInstance().log (LogType.WARNING, "[pair create] Not created.");
          }

          break;
        case DELETE:
          Log.getInstance().log (LogType.INFORMATION
                                , "[pair delete] Highlight: '%s'%n\tNote: '%s'"
                                , pair.getHighlightText()
                                , pair.getNoteText());

          res.unpair (pair.getHighlightText());
          break;
      }

      populatePageNumbersList();
    } catch (Exception ex) {
      addStatusLine (StatusTypes.ERROR, ex.getMessage ());
      reportException (ex);
    }
  }

  private void selectBookNameComboBoxActionPerformed(java.awt.event.ActionEvent evt)
  {
      String bookTitle = (String)selectBookNameComboBox.getSelectedItem();
      if (bookTitle != null)
      {
        createPageResourceObjects (bookTitle);
        populatePageNumbersList();
        setStatus (ApplicationStatus.BOOK_TITLE_SELECTED);
      }
  }

  private void fixPDFButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    Path sourcePdfFile = Paths.get (selectPdfFileTextBox.getText());
    PdfFixer fixer = PdfFixer.getInstance();
    JFrame thisFrame = (JFrame)this;

    fixerThread = new Thread (new Runnable() {
      public void run()
      {
        try {
          setStatus (ApplicationStatus.FIX_STARTED);
          addStatusLine (StatusTypes.INFORMATION, "Fixing pdf file: %s", sourcePdfFile.toString());
          Log.getInstance().log (LogType.INFORMATION, "Fixing PDF for OS: %s (%s)"
                                                    , fixer.getOSName()
                                                    , fixer.getOSArchitecture());

          // Create back of the source file.
          Path duplicatePdfFile = fixer.createBackup (sourcePdfFile);
          JOptionPane.showMessageDialog (thisFrame,
                                         String.format ("Original file is backed up to %s",
                                                         duplicatePdfFile.toString()));
          // Start the process
          fixer.apply (duplicatePdfFile, sourcePdfFile);
          setStatus (ApplicationStatus.FIX_COMPLETED);
          addStatusLine (StatusTypes.INFORMATION, "Fixing has completed.");

        } catch (InterruptedException ex) {
          setStatus (ApplicationStatus.FIX_FAILED);
          addStatusLine (StatusTypes.WARNING, "Fixing was cancelled");
        } catch (Exception ex) {
          addStatusLine (StatusTypes.ERROR, ex.getMessage ());
          reportException (ex);
          setStatus (ApplicationStatus.FIX_FAILED);
        }
      }
    });

    fixerThread.start();
  }

  private void formWindowClosing(java.awt.event.WindowEvent evt)
  {
    // TODO: Possibly add confirmation before closing.

    // Flush log file.
    Log.getInstance().log (LogType.INFORMATION, "Exiting");
    Log.getInstance().flush();
  }

  private void versionButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    String htmlVersionText =  "<html><body>";
           htmlVersionText += "GUI version: <b>%s</b><br/>";
           htmlVersionText += "kpdfsync lib version: <b>%s</b><br/>";
           htmlVersionText += "ajl lib version: <b>%s</b><br/>";
           htmlVersionText += "</body></html>";

    String guiVersion = Config.getInstance().readSetting ("app.version");
    String libVersion = coderarjob.kpdfsync.lib.Config.getInstance().readSetting ("app.version");
    String ajlVersion = coderarjob.ajl.Config.getInstance().readSetting ("app.version");

    htmlVersionText = String.format (htmlVersionText, guiVersion, libVersion, ajlVersion);
    JOptionPane.showMessageDialog (this,
                                   htmlVersionText,
                                   "Versions",
                                   JOptionPane.INFORMATION_MESSAGE);
  }
  /* Other private class methods*/

  private void createPageResourceObjects (String bookTitle)
  {
    try {
      ArrayList<ParserResult> entries = Collections.list (mClippingsFile.getBookAnnotations (bookTitle));
      mPairManager = new HighlightNotePairManager();
      for (ParserResult entry : entries)
      {
        if (entry.annotationType() != AnnotationType.HIGHLIGHT &&
            entry.annotationType() != AnnotationType.NOTE)
          continue;

        if (entry.annotationType () == AnnotationType.HIGHLIGHT)
          mPairManager.addHighlight (entry.pageOrLocationNumber(), entry.text());

        if (entry.annotationType () == AnnotationType.NOTE)
          mPairManager.addNote (entry.pageOrLocationNumber(), entry.text());
      }

      // Automatic pair for pages with only one highlight and note
      mPairManager.pairAutomatic();
    } catch (Exception ex) {
      addStatusLine (StatusTypes.ERROR, ex.getMessage ());
      reportException (ex);
    }
  }

  private void populatePageNumbersList ()
  {
    int selectedIndex = pageNumbersList.getSelectedIndex();

    pageNumbersListModel.clear();
    highlightsListModel.clear();

    try {
      for (int pageNumber : mPairManager.getPageNumbers())
      {
        PageResource r = mPairManager.getPageResourceByPageNumber (pageNumber);
        pageNumbersListModel.addElement (r);
      }

      if (selectedIndex >= 0)
        pageNumbersList.setSelectedIndex (selectedIndex);

    } catch (Exception ex) {
      addStatusLine (StatusTypes.ERROR, ex.getMessage ());
      reportException (ex);
    }
  }

  private void createNewParser (String fileName) throws FileNotFoundException, IOException
  {
    mParser = new KindleParserV1 (fileName);
    mParser.setParserEvents (new ParserEvents ()
        {
          public void onError (String fileName, long offset, String error, ParserResult result)
          {
            parserErrorHander (fileName, offset, error, result);
          }
          public void onSuccess (String fileName, long offset, ParserResult result)
          { }
        });

    mMatcher = new BasicMatcher();
    mMatcher.setPatternMatcherEventsHandler(new PatternMatcherEvents ()
        {
          public void onMatchStart (String text, String pattern)
          { }
          public void onMatchEnd (Match result)
          {
            matchCompletedEventHandler (result);
          }
        });
  }

  private void reportException (Exception ex)
  {
    String exceptionMessage = ex.getMessage() == null ? ex.toString() : ex.getMessage();

    JOptionPane.showMessageDialog (this, exceptionMessage, "Error!", JOptionPane.ERROR_MESSAGE);
    System.err.println (exceptionMessage);
    ex.printStackTrace();

    Log.getInstance().log (ex);
    Log.getInstance().flush();

    Throwable cause = ex.getCause();
    for (int i = 1; cause != null; i++)
    {
      System.err.println (":: Cause #" + i);
      cause.printStackTrace();
      cause = cause.getCause();
    }
  }

  private void updateUIBooksComboBox (final String item)
  {
    Runnable r = new Runnable () {
      public void run () {
        selectBookNameComboBox.addItem (item);
      }
    };
    SwingUtilities.invokeLater (r);
  }

  int pvalue = 0;
  private void updateUIProgressBar (final int value)
  {
    if (pvalue == value)
      return;

    pvalue = value;

    Runnable r = new Runnable () {
      public void run () {
        jProgressBar1.setValue (value);
      }
    };
    SwingUtilities.invokeLater (r);
  }

  private void addStatusLine (final StatusTypes type, final String fmt, final Object... values)
  {
    String statusText;

    switch (type)
    {
      case WARNING:
        statusText = String.format ("[Warning] " + fmt, values);
        Log.getInstance().log (LogType.WARNING, fmt, values);
        break;
      case ERROR:
        statusText = String.format ("[Error] " + fmt, values);
        Log.getInstance().log (LogType.ERROR, fmt, values);
        break;
      case INFORMATION:
        statusText = String.format (fmt, values);
        Log.getInstance().log (LogType.INFORMATION, fmt, values);
        break;
      default:
        statusText = "";
        break;
    }

    Runnable r = new Runnable () {
      public void run () {
        statusLabel.setText (statusText);

        // Errors and Warnings are only put to the list.
        if (type != StatusTypes.INFORMATION)
          statusListModel.addElement (statusText);
      }
    };
    SwingUtilities.invokeLater (r);
  }

  private void logApplicationStatus (final ApplicationStatus status)
  {
    String fmt  = "At %s%n";
           fmt += "\tClip file: %s%n";
           fmt += "\tBook: %s%n";
           fmt += "\tSkip pages: %d%n";
           fmt += "\tThreshold :%d%n";
           fmt += "\tPDF souce :%s";


    String clipFileName = clippingsFileTextBox.getText();
    String bookTitle = (selectBookNameComboBox.getSelectedIndex() >= 0)
                                            ? (String)selectBookNameComboBox.getSelectedItem()
                                            : "<nothing selected>";
    Integer skipPage = (Integer)pdfSkipPagesSpinner.getValue();
    Integer threshold = (Integer)matchThressholdSpinner.getValue();
    String sourcePDF = selectPdfFileTextBox.getText();

    Log.getInstance().log (LogType.INFORMATION, fmt
                                              , status.toString()
                                              , clipFileName
                                              , bookTitle
                                              , skipPage
                                              , threshold
                                              , sourcePDF);
  }

  private void setStatus (final ApplicationStatus status)
  {
    if (!SwingUtilities.isEventDispatchThread())
    {
      Runnable r = new Runnable () {
        public void run() {
          setStatus (status);
        }
      };
      SwingUtilities.invokeLater (r);
      return;
    }

    // Log Application Status
    logApplicationStatus (status);

    // Always keep the text boxes disabled.
    selectPdfFileTextBox.setEnabled (false);
    clippingsFileTextBox.setEnabled (false);

    // Disable all controls first, then enable each based on status.
    browseClippingsFileButton.setEnabled (false);
    selectBookNameComboBox.setEnabled (false);
    browsePdfFileButton.setEnabled (false);
    proceedButton.setEnabled (false);
    pdfSkipPagesSpinner.setEnabled (false);
    matchThressholdSpinner.setEnabled (false);
    fixPDFButton.setEnabled (false);
    exitButton.setText ("Exit");
    exitButton.setEnabled (true);

    switch (status)
    {
      case NOT_STARTED:
        statusLabel.setText ("Ready. Provide a Kindle Clippings file.");
        break;
      case CLIPPINGS_FILE_PARSE_COMPLETED:
        statusLabel.setText ("Parsing complete. Select the book title you want to highlight.");
        break;
      case CLIPPINGS_FILE_PARSE_FAILED:
        statusLabel.setText ( "Parsing failed. See the errors list or " + Log.LOG_FILE + " file");
        break;
      case BOOK_TITLE_SELECTED:
        statusLabel.setText ("Associate highlights with corresponding notes (if needed)");
        break;
      case HIGHLIGHT_COMPLETED:
        statusLabel.setText ("Highlighting completed");
        break;
      case HIGHLIGHT_FAILED:
        statusLabel.setText (
            "Highlighting failed. See the errors list or " + Log.LOG_FILE + " file"
            );
        break;
      case PDF_SELECTED:
        statusLabel.setText (
            "PDF file selected. Now enter the 'threshold' and 'number of pages before page 1'."
            );
        break;
      default:
        break;
    }

    switch (status)
    {
      case NOT_STARTED:
        statusListModel.clear ();
        selectBookNameComboBox.removeAllItems ();
        browseClippingsFileButton.setEnabled (true);
        break;
      case CLIPPINGS_FILE_PARSE_COMPLETED:
        selectBookNameComboBox.setEnabled (true);
        browseClippingsFileButton.setEnabled (true);
        break;
      case CLIPPINGS_FILE_PARSE_FAILED:
        statusLabel.setText ("Parsing failed");
        browseClippingsFileButton.setEnabled (true);
        break;
      case BOOK_TITLE_SELECTED:
        statusListModel.clear ();
        selectBookNameComboBox.setEnabled (true);
        browseClippingsFileButton.setEnabled (true);
        browsePdfFileButton.setEnabled (true);
        break;
      case HIGHLIGHT_COMPLETED:         // intentional falling
      case HIGHLIGHT_FAILED:            // intentional falling
      case FIX_COMPLETED:               // intentional falling
      case FIX_FAILED:                  // intentional falling
      case PDF_SELECTED:
        selectBookNameComboBox.setEnabled (true);
        browseClippingsFileButton.setEnabled (true);
        browsePdfFileButton.setEnabled (true);
        proceedButton.setEnabled (true);
        pdfSkipPagesSpinner.setEnabled (true);
        matchThressholdSpinner.setEnabled (true);
        fixPDFButton.setEnabled (true);
        break;
      case CLIPPINGS_FILE_PARSE_STARTED: // intentional falling
        statusListModel.clear ();
        selectBookNameComboBox.removeAllItems ();
        browseClippingsFileButton.setEnabled (true);
      case HIGHLIGHT_STARTED:
        jProgressBar1.setValue (0);
        exitButton.setText ("Cancel");
        break;
      case FIX_STARTED:
        exitButton.setEnabled (false);  // Cannot be cancelled properly. File need to be restored.
        jProgressBar1.setValue (0);
        exitButton.setText ("Wait..");
        break;
    }
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    fileChooser = new javax.swing.JFileChooser();
    headerPanel = new javax.swing.JPanel();
    logoLabel = new javax.swing.JLabel();
    exitButton = new javax.swing.JButton();
    optionsButton = new javax.swing.JButton();
    versionButton = new javax.swing.JButton();
    clippingsFileLabel = new javax.swing.JLabel();
    clippingsFileTextBox = new javax.swing.JTextField();
    browseClippingsFileButton = new javax.swing.JButton();
    selectBookNameComboBox = new javax.swing.JComboBox<>();
    selectBookNameLabel = new javax.swing.JLabel();
    selectPdfFileLabel = new javax.swing.JLabel();
    selectPdfFileTextBox = new javax.swing.JTextField();
    browsePdfFileButton = new javax.swing.JButton();
    pageNumbersScrollPane = new javax.swing.JScrollPane();
    pageNumbersList = new javax.swing.JList<>();
    pageNumbersLabel = new javax.swing.JLabel();
    selectHighlightLabel = new javax.swing.JLabel();
    highlightsScrollPane = new javax.swing.JScrollPane();
    highlightsList = new javax.swing.JList<>();
    proceedPanel = new javax.swing.JPanel();
    statusLabel = new javax.swing.JLabel();
    jProgressBar1 = new javax.swing.JProgressBar();
    statusScrollPane = new javax.swing.JScrollPane();
    statusList = new javax.swing.JList<>();
    pdfSkipPagesLabel = new javax.swing.JLabel();
    pdfSkipPagesSpinner = new javax.swing.JSpinner();
    matchThressholdLabel = new javax.swing.JLabel();
    matchThressholdSpinner = new javax.swing.JSpinner();
    percentLabel = new javax.swing.JLabel();
    proceedButton = new javax.swing.JButton();
    fixPDFButton = new javax.swing.JButton();

    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setSelectedFile(new java.io.File("/home/coder/  "));

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("kpdfsync");
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    headerPanel.setBackground(new java.awt.Color(25, 66, 97));

    logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/coderarjob/kpdfsync/poc/res/Logo.png"))); // NOI18N
    logoLabel.setToolTipText("");

    exitButton.setText("Exit");
    exitButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exitButtonActionPerformed(evt);
      }
    });

    optionsButton.setText("Options");
    optionsButton.setEnabled(false);
    optionsButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        optionsButtonActionPerformed(evt);
      }
    });

    versionButton.setText("Version");
    versionButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        versionButtonActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
    headerPanel.setLayout(headerPanelLayout);
    headerPanelLayout.setHorizontalGroup(
      headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(headerPanelLayout.createSequentialGroup()
        .addComponent(logoLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(optionsButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(versionButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(exitButton)
        .addContainerGap())
    );
    headerPanelLayout.setVerticalGroup(
      headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
      .addComponent(logoLabel)
      .addGroup(headerPanelLayout.createSequentialGroup()
        .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(exitButton)
          .addComponent(optionsButton)
          .addComponent(versionButton))
        .addGap(24, 24, 24))
    );

    clippingsFileLabel.setLabelFor(clippingsFileTextBox);
    clippingsFileLabel.setText("Clippings file :");

    browseClippingsFileButton.setText("...");
    browseClippingsFileButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        browseClippingsFileButtonActionPerformed(evt);
      }
    });

    selectBookNameComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        selectBookNameComboBoxActionPerformed(evt);
      }
    });

    selectBookNameLabel.setLabelFor(selectBookNameComboBox);
    selectBookNameLabel.setText("Select book name :");

    selectPdfFileLabel.setLabelFor(selectPdfFileTextBox);
    selectPdfFileLabel.setText("Select book PDF file :");

    browsePdfFileButton.setText("...");
    browsePdfFileButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        browsePdfFileButtonActionPerformed(evt);
      }
    });

    pageNumbersList.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    pageNumbersList.setModel(pageNumbersListModel);
    pageNumbersList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    pageNumbersList.setCellRenderer(new PageNumberListRenderer());
    pageNumbersList.setLayoutOrientation(javax.swing.JList.VERTICAL_WRAP);
    pageNumbersList.setVisibleRowCount(3);
    pageNumbersList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
        pageNumbersListValueChanged(evt);
      }
    });
    pageNumbersScrollPane.setViewportView(pageNumbersList);

    pageNumbersLabel.setText("Pages with highlight/notes:");

    selectHighlightLabel.setText("Highlight and notes association. (Double-click to change)");

    highlightsList.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    highlightsList.setModel(highlightsListModel);
    highlightsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    highlightsList.setCellRenderer(new HighlightNotePairListRenderer());
    highlightsList.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        highlightsListMouseClicked(evt);
      }
    });
    highlightsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
        highlightsListValueChanged(evt);
      }
    });
    highlightsScrollPane.setViewportView(highlightsList);

    proceedPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

    statusLabel.setText("Status:");
    statusLabel.setFocusable(false);
    statusLabel.setMaximumSize(new java.awt.Dimension(900, 14));
    statusLabel.setMinimumSize(new java.awt.Dimension(900, 14));
    statusLabel.setPreferredSize(new java.awt.Dimension(900, 14));

    jProgressBar1.setForeground(new java.awt.Color(0, 204, 204));
    jProgressBar1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    statusList.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    statusList.setModel(statusListModel);
    statusList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    statusScrollPane.setViewportView(statusList);

    javax.swing.GroupLayout proceedPanelLayout = new javax.swing.GroupLayout(proceedPanel);
    proceedPanel.setLayout(proceedPanelLayout);
    proceedPanelLayout.setHorizontalGroup(
      proceedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jProgressBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addComponent(statusScrollPane)
      .addGroup(proceedPanelLayout.createSequentialGroup()
        .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );
    proceedPanelLayout.setVerticalGroup(
      proceedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(proceedPanelLayout.createSequentialGroup()
        .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statusScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );

    pdfSkipPagesLabel.setLabelFor(pdfSkipPagesSpinner);
    pdfSkipPagesLabel.setText("Number of pages before page 1:");

    pdfSkipPagesSpinner.setModel(new javax.swing.SpinnerNumberModel());

    matchThressholdLabel.setLabelFor(matchThressholdSpinner);
    matchThressholdLabel.setText("Match acceptance thresshold:");

    matchThressholdSpinner.setModel(new javax.swing.SpinnerNumberModel());
    matchThressholdSpinner.setValue(90);

    percentLabel.setLabelFor(pdfSkipPagesSpinner);
    percentLabel.setText("%");

    proceedButton.setText("Proceed");
    proceedButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        proceedButtonActionPerformed(evt);
      }
    });

    fixPDFButton.setText("Fix");
    fixPDFButton.setMaximumSize(new java.awt.Dimension(80, 24));
    fixPDFButton.setMinimumSize(new java.awt.Dimension(80, 24));
    fixPDFButton.setPreferredSize(new java.awt.Dimension(80, 24));
    fixPDFButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        fixPDFButtonActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(highlightsScrollPane)
            .addContainerGap())
          .addGroup(layout.createSequentialGroup()
            .addComponent(pageNumbersScrollPane)
            .addContainerGap())
          .addComponent(selectHighlightLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addGroup(layout.createSequentialGroup()
                    .addComponent(pdfSkipPagesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(pdfSkipPagesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(matchThressholdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(matchThressholdSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(percentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addComponent(pageNumbersLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 356, Short.MAX_VALUE))
              .addGroup(layout.createSequentialGroup()
                .addComponent(selectPdfFileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectPdfFileTextBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                  .addComponent(proceedButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addGroup(layout.createSequentialGroup()
                    .addComponent(browsePdfFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(fixPDFButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))))
            .addContainerGap())
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(selectBookNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
              .addComponent(clippingsFileLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(37, 37, 37)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(selectBookNameComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(clippingsFileTextBox))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(browseClippingsFileButton)
            .addGap(11, 11, 11))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(proceedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(clippingsFileLabel)
          .addComponent(clippingsFileTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(browseClippingsFileButton))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(selectBookNameLabel)
          .addComponent(selectBookNameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(pageNumbersLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(pageNumbersScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(selectHighlightLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(highlightsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(selectPdfFileLabel)
          .addComponent(selectPdfFileTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(browsePdfFileButton)
          .addComponent(fixPDFButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(pdfSkipPagesLabel)
          .addComponent(pdfSkipPagesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(matchThressholdLabel)
          .addComponent(matchThressholdSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(percentLabel)
          .addComponent(proceedButton))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(proceedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton browseClippingsFileButton;
  private javax.swing.JButton browsePdfFileButton;
  private javax.swing.JLabel clippingsFileLabel;
  private javax.swing.JTextField clippingsFileTextBox;
  private javax.swing.JButton exitButton;
  private javax.swing.JFileChooser fileChooser;
  private javax.swing.JButton fixPDFButton;
  private javax.swing.JPanel headerPanel;
  private javax.swing.JList<HighlightNotePair> highlightsList;
  private javax.swing.JScrollPane highlightsScrollPane;
  private javax.swing.JProgressBar jProgressBar1;
  private javax.swing.JLabel logoLabel;
  private javax.swing.JLabel matchThressholdLabel;
  private javax.swing.JSpinner matchThressholdSpinner;
  private javax.swing.JButton optionsButton;
  private javax.swing.JLabel pageNumbersLabel;
  private javax.swing.JList<PageResource> pageNumbersList;
  private javax.swing.JScrollPane pageNumbersScrollPane;
  private javax.swing.JLabel pdfSkipPagesLabel;
  private javax.swing.JSpinner pdfSkipPagesSpinner;
  private javax.swing.JLabel percentLabel;
  private javax.swing.JButton proceedButton;
  private javax.swing.JPanel proceedPanel;
  private javax.swing.JComboBox<String> selectBookNameComboBox;
  private javax.swing.JLabel selectBookNameLabel;
  private javax.swing.JLabel selectHighlightLabel;
  private javax.swing.JLabel selectPdfFileLabel;
  private javax.swing.JTextField selectPdfFileTextBox;
  private javax.swing.JLabel statusLabel;
  private javax.swing.JList<String> statusList;
  private javax.swing.JScrollPane statusScrollPane;
  private javax.swing.JButton versionButton;
  // End of variables declaration//GEN-END:variables

}
