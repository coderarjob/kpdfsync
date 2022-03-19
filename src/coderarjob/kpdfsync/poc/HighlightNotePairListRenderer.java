package coderarjob.kpdfsync.poc;

import javax.swing.*;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Color;

public class HighlightNotePairListRenderer extends JPanel implements ListCellRenderer<HighlightNotePair>
{

  private JLabel highlightLabel;
  private JLabel pairedNoteLabel;
  private ImageIcon highlightIcon;

  public HighlightNotePairListRenderer()
  {
    this.setLayout (new BorderLayout());

    highlightIcon = new ImageIcon ("src/coderarjob/kpdfsync/poc/res/highlighter.png");

    highlightLabel = new JLabel();
    highlightLabel.setIcon (highlightIcon);
    highlightLabel.setForeground (Color.BLUE);
    highlightLabel.setOpaque (false);
    this.add (highlightLabel, BorderLayout.PAGE_START);

    pairedNoteLabel = new JLabel();
    pairedNoteLabel.setBackground (Color.DARK_GRAY);
    pairedNoteLabel.setOpaque (false);
    this.add (pairedNoteLabel, BorderLayout.PAGE_END);
  }

  public Component getListCellRendererComponent(JList<? extends HighlightNotePair> list,
                                                HighlightNotePair value, int index,
                                                boolean isSelected, boolean cellHasFocus)
  {
    highlightLabel.setText (value.getHighlightText());
    pairedNoteLabel.setText (value.getNoteText());

    if (isSelected)
      this.setBackground (list.getSelectionBackground());
    else
      this.setBackground (list.getBackground());

    return this;
  }
}
