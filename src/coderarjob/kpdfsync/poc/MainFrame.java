package coderarjob.kpdfsync.poc;

import javax.swing.*;
import java.awt.event.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import coderarjob.kpdfsync.lib.clipparser.*;
import coderarjob.kpdfsync.lib.clipparser.ParserResult.AnnotationType;
import coderarjob.kpdfsync.lib.*;
import coderarjob.kpdfsync.lib.pm.*;
import coderarjob.kpdfsync.lib.annotator.*;

public class MainFrame extends javax.swing.JFrame
{
  private enum ApplicationStatus
  {
    NOT_STARTED,
    CLIPPINGS_FILE_PARSE_STARTED, CLIPPINGS_FILE_PARSE_COMPLETED, CLIPPINGS_FILE_PARSE_FAILED,
    BOOK_TITLE_SELECTED,
    PDF_SELECTED,
    HIGHLIGHT_STARTED, HIGHLIGHT_COMPLETED, HIGHLIGHT_FAILED
  }

  private enum StatusTypes
  {
    PARSER_ERROR, MATCH_FOUND, MATCH_NOT_FOUND, OTHER, ERROR, INFORMATION
  }

  /* Private fields */
  private AbstractParser mParser;
  private KindleClippingsFile mClippingsFile;
  private AbstractAnnotator mAnnotator;
  private AbstractMatcher mMatcher;

  private Thread highlightThread, parseClippingsThread;

  // Other Swing variable declarations
  private DefaultListModel<String> statusListModel;
  private DefaultListModel<HighlightNotePair> highlightsListModel;
  private DefaultListModel<PageResource> pageNumbersListModel;

  private HighlightNotePairManager mPairManager;
  private NoteAssociationDialog mNoteForm;

  /* Constructor and other methods*/
  public MainFrame()
  {
    statusListModel = new DefaultListModel<> ();
    pageNumbersListModel = new DefaultListModel<> ();
    highlightsListModel = new DefaultListModel<> ();

    mNoteForm = new NoteAssociationDialog (this);

	initComponents();
    setStatus (ApplicationStatus.NOT_STARTED);
  }

  /* Kindle Parser event handler*/
  private void parserErrorHander (String fileName, long offset, String error, ParserResult result)
  {
    addStatusLine (StatusTypes.PARSER_ERROR, "'%s' at %d", error, offset);
  }

  /* Matcher event handler */
  private void matchCompletedEventHandler (Match match)
  {
    if (match.matchPercent() > mAnnotator.getMatchThreshold())
      addStatusLine (StatusTypes.MATCH_FOUND, match);
  }

  /* Button and other UI event handlers*/
  private void browseClippingsFileButtonActionPerformed(ActionEvent evt)
  {
    if (fileChooser.showOpenDialog (this) != JFileChooser.APPROVE_OPTION)
      return;

    setStatus (ApplicationStatus.CLIPPINGS_FILE_PARSE_STARTED);

    File file= fileChooser.getSelectedFile ();
    clippingsFileTextBox.setText (file.getAbsolutePath());

    parseClippingsThread = new Thread (new Runnable () {
      public void run () {
        try {
          addStatusLine (StatusTypes.OTHER, "Parsing %s ...", file.getName());
          createNewParser (file.getAbsolutePath());
          mClippingsFile = new KindleClippingsFile(mParser);

          ArrayList<String> titles = Collections.list(mClippingsFile.getBookTitles());

          for (String title : titles)
            updateUIBooksComboBox (title);

          if (statusList.getModel().getSize() == 0)
            throw new Exception ("Empty or invalid clippings file.");

          addStatusLine (StatusTypes.OTHER, "Parsing complete");
          setStatus (ApplicationStatus.CLIPPINGS_FILE_PARSE_COMPLETED);

        } catch (InterruptedException ex) {
          addStatusLine (StatusTypes.OTHER, "Parsing canceled");
          setStatus (ApplicationStatus.CLIPPINGS_FILE_PARSE_FAILED);
        }
        catch (Exception ex)
        {
          addStatusLine (StatusTypes.OTHER, "Parsing failed: " + ex.getMessage());
          setStatus (ApplicationStatus.CLIPPINGS_FILE_PARSE_FAILED);
          reportException (ex);
        }
      }
    });
    parseClippingsThread.start();
  }

  private void browsePdfFileButtonActionPerformed(ActionEvent evt)
  {
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
              okay = mAnnotator.highlight (pageNum, pair.getHighlightText(), pair.getNoteText());
              if (!okay)
                addStatusLine (StatusTypes.MATCH_NOT_FOUND, pair.getHighlightText());

              float progress = (float) highlightIndex/(totalHighlightCount - 1) * 100;
              updateUIProgressBar ((int)progress);
              highlightIndex++;
            }
          }

          // Save pdf to a new file.
          addStatusLine (StatusTypes.OTHER, "Saving to file " + destinationPdfFileName);
          mAnnotator.save (destinationPdfFileName);

          addStatusLine (StatusTypes.OTHER, "Highlighting complete.");
          setStatus (ApplicationStatus.HIGHLIGHT_COMPLETED);
        } catch (InterruptedException ex) {
          addStatusLine (StatusTypes.OTHER, "Highlight canceled.");
          setStatus (ApplicationStatus.HIGHLIGHT_FAILED);
        } catch (Exception ex)
        {
          addStatusLine (StatusTypes.OTHER, "Highlighting failed.");
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
    System.out.println ("Page number selected is " + pageNumber);

    // Clear highlights and notes
    highlightsListModel.clear();

    // Add highlights and notes
    try {
      for (HighlightNotePair pair : res.getPairs()) {
        highlightsListModel.addElement (pair);
      }
    } catch (Exception ex) {
      addStatusLine (StatusTypes.ERROR, "", ex.getMessage ());
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
    if (buttonText.equals ("EXIT"))
    {
      // TODO: Possibly add confirmation before closing.
      System.exit (0);
    }
    else
    {
      if (highlightThread != null && highlightThread.isAlive ()) {
        System.out.println ("highlight thread interrupted");
        highlightThread.interrupt();
      }

      if (parseClippingsThread != null && parseClippingsThread.isAlive ()) {
        System.out.println ("parser thread interrupted");
        parseClippingsThread.interrupt();
      }
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
          System.out.println ("Selected: " + noteText);
          if (noteText != null)
            res.pair (pair.getHighlightText(), noteText);

          break;
        case DELETE:
          res.unpair (pair.getHighlightText());
          break;
      }

      populatePageNumbersList();
    } catch (Exception ex) {
      addStatusLine (StatusTypes.ERROR, "", ex.getMessage ());
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
      addStatusLine (StatusTypes.ERROR, "", ex.getMessage ());
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
      addStatusLine (StatusTypes.ERROR, "", ex.getMessage ());
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
    JOptionPane.showMessageDialog (this, ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
    System.err.println (ex.getMessage());
    ex.printStackTrace();

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

  private void addStatusLine (final StatusTypes type, final Object... values)
  {
    String newEntryString;
    String fmt;
    Object[] args;

    switch (type)
    {
      case MATCH_FOUND:
        fmt = "%.30s... %.0f%% matched on line %d.";
        Match match = (Match)values[0];
        newEntryString = String.format (fmt
            , match.pattern()
            , match.matchPercent()
            , match.beginFrom().lineNumber());
        break;
      case MATCH_NOT_FOUND:
        fmt = "%.30s... not found.";
        String pattern = (String)values[0];
        newEntryString = String.format (fmt, pattern);
        break;
      case PARSER_ERROR:
        fmt = (String)values[0];
        args = Arrays.copyOfRange (values, 1, values.length);
        newEntryString = String.format ("Parser Error : " + fmt, args);
        break;
      default:
        fmt = (String)values[0];
        args = Arrays.copyOfRange (values, 1, values.length);
        newEntryString = String.format (fmt, args);
    }

    Runnable r = new Runnable () {
      public void run () {
        statusListModel.addElement (newEntryString);
        //statusList.ensureIndexIsVisible (statusListModel.getSize() - 1);      // Slows down UI
      }
    };
    SwingUtilities.invokeLater (r);
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
    exitButton.setText ("Exit");

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
        browseClippingsFileButton.setEnabled (true);
        break;
      case BOOK_TITLE_SELECTED:
        selectBookNameComboBox.setEnabled (true);
        browseClippingsFileButton.setEnabled (true);
        browsePdfFileButton.setEnabled (true);
        break;
      case HIGHLIGHT_COMPLETED:         // intentional falling:
      case HIGHLIGHT_FAILED:            // intentional falling:
      case PDF_SELECTED:
        selectBookNameComboBox.setEnabled (true);
        browseClippingsFileButton.setEnabled (true);
        browsePdfFileButton.setEnabled (true);
        proceedButton.setEnabled (true);
        pdfSkipPagesSpinner.setEnabled (true);
        matchThressholdSpinner.setEnabled (true);
        break;
      case CLIPPINGS_FILE_PARSE_STARTED: // intentional falling
      case HIGHLIGHT_STARTED:
        jProgressBar1.setValue (0);
        exitButton.setText ("Cancel");
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
    proceedButton = new javax.swing.JButton();
    jProgressBar1 = new javax.swing.JProgressBar();
    statusScrollPane = new javax.swing.JScrollPane();
    statusList = new javax.swing.JList<>();
    statusLabel = new javax.swing.JLabel();
    pdfSkipPagesLabel = new javax.swing.JLabel();
    pdfSkipPagesSpinner = new javax.swing.JSpinner();
    matchThressholdLabel = new javax.swing.JLabel();
    matchThressholdSpinner = new javax.swing.JSpinner();
    percentLabel = new javax.swing.JLabel();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("mk-float-kpdfsync-gui");

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

    javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
    headerPanel.setLayout(headerPanelLayout);
    headerPanelLayout.setHorizontalGroup(
      headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(headerPanelLayout.createSequentialGroup()
        .addComponent(logoLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(optionsButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(exitButton)
        .addContainerGap())
    );
    headerPanelLayout.setVerticalGroup(
      headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(logoLabel, javax.swing.GroupLayout.Alignment.TRAILING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
        .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(exitButton)
          .addComponent(optionsButton))
        .addGap(24, 24, 24))
    );

    clippingsFileLabel.setForeground(new java.awt.Color(0, 0, 0));
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

    selectBookNameLabel.setForeground(new java.awt.Color(0, 0, 0));
    selectBookNameLabel.setLabelFor(selectBookNameComboBox);
    selectBookNameLabel.setText("Select book name :");

    selectPdfFileLabel.setForeground(new java.awt.Color(0, 0, 0));
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

    pageNumbersLabel.setText("Page numbers:");

    selectHighlightLabel.setText("Highlight as associated notes. (Double-click to change)");

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

    proceedButton.setText("Proceed");
    proceedButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        proceedButtonActionPerformed(evt);
      }
    });

    jProgressBar1.setForeground(new java.awt.Color(0, 204, 204));
    jProgressBar1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    jProgressBar1.setStringPainted(true);

    javax.swing.GroupLayout proceedPanelLayout = new javax.swing.GroupLayout(proceedPanel);
    proceedPanel.setLayout(proceedPanelLayout);
    proceedPanelLayout.setHorizontalGroup(
      proceedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, proceedPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(proceedButton)
        .addContainerGap())
    );
    proceedPanelLayout.setVerticalGroup(
      proceedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(proceedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addComponent(proceedButton))
    );

    statusList.setModel(this.statusListModel);
    statusScrollPane.setViewportView(statusList);

    statusLabel.setText("Status:");

    pdfSkipPagesLabel.setLabelFor(pdfSkipPagesSpinner);
    pdfSkipPagesLabel.setText("Number of pages to skip:");

    pdfSkipPagesSpinner.setModel(new javax.swing.SpinnerNumberModel());

    matchThressholdLabel.setLabelFor(matchThressholdSpinner);
    matchThressholdLabel.setText("Match acceptance thresshold:");

    matchThressholdSpinner.setModel(new javax.swing.SpinnerNumberModel());
    matchThressholdSpinner.setValue(90);

    percentLabel.setLabelFor(pdfSkipPagesSpinner);
    percentLabel.setText("%");

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
            .addComponent(statusScrollPane)
            .addContainerGap())
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(selectPdfFileLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
              .addComponent(pdfSkipPagesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addComponent(pdfSkipPagesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(matchThressholdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(matchThressholdSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(percentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(372, 372, 372))
              .addGroup(layout.createSequentialGroup()
                .addComponent(selectPdfFileTextBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browsePdfFileButton)))
            .addContainerGap())
          .addGroup(layout.createSequentialGroup()
            .addComponent(pageNumbersScrollPane)
            .addContainerGap())
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(selectBookNameLabel)
                  .addComponent(clippingsFileLabel)
                  .addComponent(statusLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(selectBookNameComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(clippingsFileTextBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseClippingsFileButton)
                .addGap(6, 6, 6))
              .addComponent(proceedPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(5, 5, 5))
          .addComponent(selectHighlightLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(pageNumbersLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE))))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
        .addComponent(highlightsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(selectPdfFileLabel)
          .addComponent(selectPdfFileTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(browsePdfFileButton))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(pdfSkipPagesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(matchThressholdLabel)
          .addComponent(matchThressholdSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(percentLabel)
          .addComponent(pdfSkipPagesLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(proceedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statusLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statusScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
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
  // End of variables declaration//GEN-END:variables

}
