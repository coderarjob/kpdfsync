package coderarjob.kpdfsync.poc;

import javax.swing.*;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Color;

public class HighlightNotePairListRenderer extends JPanel implements ListCellRenderer<HighlightNotePair>
{

  private JLabel highlightLabel;
  private JLabel pairedNoteLabel;
  private Color alternateColor;

  public HighlightNotePairListRenderer()
  {
    this.setLayout (new BorderLayout());

    highlightLabel = new JLabel();
    String iconResourceName = "/coderarjob/kpdfsync/poc/res/highlighter.png";
    highlightLabel.setIcon (new ImageIcon (getClass().getResource (iconResourceName)));
    highlightLabel.setForeground (Color.BLACK);
    highlightLabel.setOpaque (false);
    this.add (highlightLabel, BorderLayout.PAGE_START);

    pairedNoteLabel = new JLabel();
    pairedNoteLabel.setForeground (Color.DARK_GRAY);
    pairedNoteLabel.setOpaque (false);
    this.add (pairedNoteLabel, BorderLayout.PAGE_END);

    alternateColor = new Color (237, 244, 249);
    this.setBorder (BorderFactory.createEmptyBorder (5, 2, 5, 0));
  }

  public Component getListCellRendererComponent(JList<? extends HighlightNotePair> list,
                                                HighlightNotePair value, int index,
                                                boolean isSelected, boolean cellHasFocus)
  {
    highlightLabel.setText (value.getHighlightText());
    pairedNoteLabel.setText (value.getNoteText());

    Color normalBackgroundColor = (index % 2 == 0) ? alternateColor : list.getBackground();

    if (isSelected)
      this.setBackground (list.getSelectionBackground());
    else
      this.setBackground (normalBackgroundColor);

    return this;
  }
}
