package coderarjob.kpdfsync.poc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.List;

/**
 *
 * @author coder
 */
public class NoteAssociationDialog extends javax.swing.JDialog
{
  public enum NoteAssociationDialogOptions
  {
    DELETE, CREATE, CANCEL
  }

  private NoteAssociationDialogOptions mSelectedDialogOption;
  private String mSelectedNoteText;
  private DefaultListModel<String> mNotesListModel;

  /**
   * Creates new form NoteAssociationDialog
   */
  public NoteAssociationDialog(Frame parent)
  {
    super(parent, true);
    mNotesListModel = new DefaultListModel<>();
    initComponents();
  }

  private void reset()
  {
    mSelectedDialogOption = NoteAssociationDialogOptions.CANCEL;
    mSelectedNoteText = null;
    mNotesListModel.clear();
    notesList.setEnabled (true);
    createMappingButton.setEnabled (true);
  }

  private void populateUI (PageResource res)
  {
    pageNumberLabel.setText (String.valueOf(res.getPageNumber()));
    List<String> notesTextList = res.getNoteList();

    if (notesTextList.size() == 0) {
      // There are no notes in this page. Show a pop-up and close.
      mNotesListModel.addElement ("No unpaired note on this page.");
      notesList.setEnabled (false);
      createMappingButton.setEnabled (false);
      return;
    }

    // Display notes on the list.
    for (String note : notesTextList)
      mNotesListModel.addElement (note);
  }

  public String getSelectedNoteText()
  {
    return mSelectedNoteText;
  }

  public NoteAssociationDialogOptions showDialog(Frame parent, PageResource pageResource)
  {
    this.reset();
    this.populateUI (pageResource);
    this.setVisible(true);

    return this.mSelectedDialogOption;
  }

  private void closeButtonActionPerformed(ActionEvent evt)
  {
    this.setVisible (false);
  }

  private void createMappingButtonActionPerformed(ActionEvent evt)
  {
    mSelectedNoteText = notesList.getSelectedValue();
    mSelectedDialogOption = NoteAssociationDialogOptions.CREATE;
    this.setVisible (false);
  }

  private void deleteMappingButtonActionPerformed(ActionEvent evt)
  {
    mSelectedDialogOption = NoteAssociationDialogOptions.DELETE;
    this.setVisible (false);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    jScrollPane1 = new javax.swing.JScrollPane();
    notesList = new javax.swing.JList<>();
    jPanel1 = new javax.swing.JPanel();
    createMappingButton = new javax.swing.JButton();
    closeButton = new javax.swing.JButton();
    deleteMappingButton = new javax.swing.JButton();
    jLabel4 = new javax.swing.JLabel();
    pageNumberLabel = new javax.swing.JLabel();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Note association dialog");
    setAlwaysOnTop(true);
    setName("noteAssociationDialog"); // NOI18N
    setResizable(false);
    setType(java.awt.Window.Type.POPUP);

    jLabel1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
    jLabel1.setIcon(new javax.swing.ImageIcon("/home/coder/Work/Java/Projects/kpdfsync/src/coderarjob/kpdfsync/poc/res/sticky-note.png")); // NOI18N
    jLabel1.setText("Select the note associated with the highlight");

    jLabel2.setText("Open the book to the page, and check which note goes with the highlight.");

    notesList.setModel(mNotesListModel);
    notesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    jScrollPane1.setViewportView(notesList);

    createMappingButton.setText("Create mapping");
    createMappingButton.setToolTipText("");
    createMappingButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        createMappingButtonActionPerformed(evt);
      }
    });

    closeButton.setText("Close");
    closeButton.setToolTipText("");
    closeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        closeButtonActionPerformed(evt);
      }
    });

    deleteMappingButton.setText("Delete mapping");
    deleteMappingButton.setToolTipText("");
    deleteMappingButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        deleteMappingButtonActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(deleteMappingButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(createMappingButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(closeButton)
        .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(createMappingButton)
          .addComponent(closeButton)
          .addComponent(deleteMappingButton))
        .addContainerGap())
    );

    jLabel4.setText("Page number:");

    pageNumberLabel.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
    pageNumberLabel.setText("<Page number>");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel1)
              .addComponent(jLabel2)
              .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pageNumberLabel)))
            .addGap(0, 81, Short.MAX_VALUE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jLabel1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel2)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel4)
          .addComponent(pageNumberLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton closeButton;
  private javax.swing.JButton createMappingButton;
  private javax.swing.JButton deleteMappingButton;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JList<String> notesList;
  private javax.swing.JLabel pageNumberLabel;
  // End of variables declaration//GEN-END:variables
}
