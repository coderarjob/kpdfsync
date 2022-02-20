package coderarjob.kpdfsync.poc;

import javax.swing.JFileChooser;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;
import java.awt.event.ActionEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;

import coderarjob.kpdfsync.lib.clipparser.*;
import coderarjob.kpdfsync.lib.clipparser.ParserResult.AnnotationType;
import coderarjob.kpdfsync.lib.clipparser.ParserResult.PageNumberType;
import coderarjob.kpdfsync.lib.*;
import coderarjob.kpdfsync.lib.pm.*;
import coderarjob.kpdfsync.lib.annotator.*;

public class MainFrame extends javax.swing.JFrame
{
  private enum ApplicationStatus
  {
    NOT_STARTED, CLIPPINGS_FILE_SELECTED, PDF_SELECTED
  }

  /* Private fields */
  AbstractParser mParser;
  KindleClippingsFile mClippingsFile;
  AbstractMatcher mMatcher;

  // Other Swing variable declarations
  DefaultListModel<String> statusListModel;

  /* Constructor and other methods*/
  public MainFrame()
  {
    statusListModel = new DefaultListModel<> ();

	initComponents();
    initParser ();

    setStatus (ApplicationStatus.NOT_STARTED);
  }

  /* Kindle Parser event handler*/
  private void parserErrorHander (String fileName, long offset, String error, ParserResult result)
  {
    statusListModel.addElement (String.format("Parser error: '%s' at %d", error, offset));
  }

  /* Matcher event handler */
  private void matchCompletedEventHandler (Match match)
  {
    if (match.matchPercent() > 80.0f)
    {
      String pattern = match.pattern();
      String shortPattern = pattern.substring (0
                                        , (pattern.length() > 30) ? 30 : pattern.length() - 1);

      statusListModel.addElement (String.format ("%s... %.0f%% matched on page %d."
                                                , shortPattern
                                                , match.matchPercent()
                                                , match.beginFrom().lineNumber()));
    }
  }

  /* Button and other UI event handlers*/
  private void browseClippingsFileButtonActionPerformed(ActionEvent evt)
  {
    if (fileChooser.showOpenDialog (this) == JFileChooser.APPROVE_OPTION)
    {
      setStatus (ApplicationStatus.NOT_STARTED);

      File file= fileChooser.getSelectedFile ();
      clippingsFileTextBox.setText (file.getAbsolutePath());

      try {
        mParser.openClippingsFile (file.getAbsolutePath());
        mClippingsFile = new KindleClippingsFile(mParser);
        ArrayList<String> titles = Collections.list(mClippingsFile.getBookTitles());

        for (String title : titles)
          selectBookNameComboBox.addItem (title);

      } catch (Exception ex)
      {
        printExceptionStackTrace (ex);
      }

      setStatus (ApplicationStatus.CLIPPINGS_FILE_SELECTED);
    }
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

  private void updateMapButtonActionPerformed(ActionEvent evt)
  {
  }

  private void cancelMapButtonActionPerformed(ActionEvent evt)
  {
  }

  private void proceedButtonActionPerformed(ActionEvent evt)
  {
    String bookTitle = (String)selectBookNameComboBox.getSelectedItem();
    String pdfFileName = selectPdfFileTextBox.getText();
    int numberOfPagesToSkip = (Integer)(pdfSkipPagesSpinner.getValue());

    if (fileChooser.showSaveDialog (this) != JFileChooser.APPROVE_OPTION)
      return;

    String destinationPdfFileName = fileChooser.getSelectedFile().getAbsolutePath();

    try {
      AbstractAnnotator ann = new PdfAnnotatorV1 (mMatcher, pdfFileName, numberOfPagesToSkip);
      ann.open ();

      ArrayList<ParserResult> entries = Collections.list (mClippingsFile.getBookAnnotations (bookTitle));
      for (ParserResult entry : entries)
      {
        if (entry.annotationType() != AnnotationType.HIGHLIGHT)
          continue;

        if (entry.pageNumberType() != PageNumberType.PAGE_NUMBER)
          continue;

        ann.highlight (entry.pageOrLocationNumber(), entry.text(), "Test highlight");

        // Save pdf to a new file.
        ann.save (destinationPdfFileName);
      }
    } catch (Exception ex)
    {
      printExceptionStackTrace (ex);
    }
  }

  private void optionsButtonActionPerformed(ActionEvent evt)
  {
  }

  private void exitButtonActionPerformed(ActionEvent evt)
  {
    // TODO: Possibly add confirmation before closing.
    System.exit (0);
  }

  /* Other private class methods*/
  private void initParser ()
  {
    mParser = new KindleParserV1 ();
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

  private void printExceptionStackTrace (Exception ex)
  {
      System.err.println (ex.getMessage());
      for (StackTraceElement s: ex.getStackTrace())
        System.err.println (s.toString());
  }

  private void setStatus (ApplicationStatus status)
  {
    selectBookNameComboBox.setEnabled (false);
    selectPdfFileTextBox.setEnabled (false);
    browsePdfFileButton.setEnabled (false);
    updateMapButton.setEnabled (false);
    cancelMapButton.setEnabled (false);
    proceedButton.setEnabled (false);
    pdfSkipPagesSpinner.setEnabled (false);

    switch (status)
    {
      case NOT_STARTED:
        statusListModel.clear ();
        selectBookNameComboBox.removeAllItems ();
        break;
      case PDF_SELECTED:
        updateMapButton.setEnabled (true);
        cancelMapButton.setEnabled (true);
        proceedButton.setEnabled (true);
        pdfSkipPagesSpinner.setEnabled (true);
      case CLIPPINGS_FILE_SELECTED:
        selectBookNameComboBox.setEnabled (true);
        selectPdfFileTextBox.setEnabled (true);
        browsePdfFileButton.setEnabled (true);
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
    selectNoteLabel = new javax.swing.JLabel();
    notesScrollPane = new javax.swing.JScrollPane();
    notesList = new javax.swing.JList<>();
    proceedPanel = new javax.swing.JPanel();
    proceedButton = new javax.swing.JButton();
    jProgressBar1 = new javax.swing.JProgressBar();
    statusScrollPane = new javax.swing.JScrollPane();
    statusList = new javax.swing.JList<>();
    statusLabel = new javax.swing.JLabel();
    pageNumbersScrollPane1 = new javax.swing.JScrollPane();
    pageNumbersList1 = new javax.swing.JList<>();
    pdfSkipPagesLabel = new javax.swing.JLabel();
    pdfSkipPagesSpinner = new javax.swing.JSpinner();
    updateMapButton = new javax.swing.JButton();
    cancelMapButton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("mk-float-kpdfsync-gui");

    headerPanel.setBackground(new java.awt.Color(25, 66, 97));

    logoLabel.setIcon(new javax.swing.ImageIcon("/home/coder/NetBeansProjects/GuiHelloWorld/Resources/LogoHori.png")); // NOI18N
    logoLabel.setToolTipText("");

    exitButton.setText("Exit");
    exitButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exitButtonActionPerformed(evt);
      }
    });

    optionsButton.setText("Options");
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

    pageNumbersScrollPane.setViewportView(pageNumbersList);

    pageNumbersLabel.setText("Page numbers:");

    selectHighlightLabel.setText("Select a highlight from the selected page:");

    highlightsScrollPane.setViewportView(highlightsList);

    selectNoteLabel.setText("Select the note from the selected page, that corresponds with the above highlight:");

    notesScrollPane.setViewportView(notesList);

    proceedButton.setText("Proceed");
    proceedButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        proceedButtonActionPerformed(evt);
      }
    });

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
      .addGroup(proceedPanelLayout.createSequentialGroup()
        .addGap(0, 12, Short.MAX_VALUE)
        .addGroup(proceedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(proceedButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );

    statusList.setModel(this.statusListModel);
    statusScrollPane.setViewportView(statusList);

    statusLabel.setText("Status:");

    pageNumbersScrollPane1.setViewportView(pageNumbersList1);

    pdfSkipPagesLabel.setLabelFor(pdfSkipPagesSpinner);
    pdfSkipPagesLabel.setText("Number of pages to skip:");

    pdfSkipPagesSpinner.setModel(new javax.swing.SpinnerNumberModel());

    updateMapButton.setText("Update map");
    updateMapButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        updateMapButtonActionPerformed(evt);
      }
    });

    cancelMapButton.setText("Cancel map");
    cancelMapButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cancelMapButtonActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addComponent(proceedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(statusScrollPane)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(selectBookNameLabel)
                .addComponent(clippingsFileLabel)
                .addComponent(pageNumbersLabel)
                .addComponent(pageNumbersScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                .addComponent(statusLabel)
                .addComponent(pdfSkipPagesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pageNumbersScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addComponent(selectPdfFileLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(cancelMapButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(updateMapButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(clippingsFileTextBox)
                  .addComponent(selectBookNameComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseClippingsFileButton))
              .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addGroup(layout.createSequentialGroup()
                    .addComponent(selectHighlightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE))
                  .addComponent(selectNoteLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE))
                .addGap(190, 190, 190))
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(selectPdfFileTextBox)
                  .addGroup(layout.createSequentialGroup()
                    .addComponent(pdfSkipPagesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browsePdfFileButton))
              .addComponent(highlightsScrollPane)
              .addComponent(notesScrollPane))))
        .addContainerGap())
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
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(selectPdfFileLabel)
          .addComponent(selectPdfFileTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(browsePdfFileButton))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(pdfSkipPagesLabel)
          .addComponent(pdfSkipPagesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(pageNumbersLabel)
          .addComponent(selectHighlightLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(highlightsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
          .addComponent(pageNumbersScrollPane))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(updateMapButton)
          .addComponent(selectNoteLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addGroup(layout.createSequentialGroup()
            .addComponent(cancelMapButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(pageNumbersScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE))
          .addComponent(notesScrollPane))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(proceedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statusLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statusScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton browseClippingsFileButton;
  private javax.swing.JButton browsePdfFileButton;
  private javax.swing.JButton cancelMapButton;
  private javax.swing.JLabel clippingsFileLabel;
  private javax.swing.JTextField clippingsFileTextBox;
  private javax.swing.JButton exitButton;
  private javax.swing.JFileChooser fileChooser;
  private javax.swing.JPanel headerPanel;
  private javax.swing.JList<String> highlightsList;
  private javax.swing.JScrollPane highlightsScrollPane;
  private javax.swing.JProgressBar jProgressBar1;
  private javax.swing.JLabel logoLabel;
  private javax.swing.JList<String> notesList;
  private javax.swing.JScrollPane notesScrollPane;
  private javax.swing.JButton optionsButton;
  private javax.swing.JLabel pageNumbersLabel;
  private javax.swing.JList<String> pageNumbersList;
  private javax.swing.JList<String> pageNumbersList1;
  private javax.swing.JScrollPane pageNumbersScrollPane;
  private javax.swing.JScrollPane pageNumbersScrollPane1;
  private javax.swing.JLabel pdfSkipPagesLabel;
  private javax.swing.JSpinner pdfSkipPagesSpinner;
  private javax.swing.JButton proceedButton;
  private javax.swing.JPanel proceedPanel;
  private javax.swing.JComboBox<String> selectBookNameComboBox;
  private javax.swing.JLabel selectBookNameLabel;
  private javax.swing.JLabel selectHighlightLabel;
  private javax.swing.JLabel selectNoteLabel;
  private javax.swing.JLabel selectPdfFileLabel;
  private javax.swing.JTextField selectPdfFileTextBox;
  private javax.swing.JLabel statusLabel;
  private javax.swing.JList<String> statusList;
  private javax.swing.JScrollPane statusScrollPane;
  private javax.swing.JButton updateMapButton;
  // End of variables declaration//GEN-END:variables

}
