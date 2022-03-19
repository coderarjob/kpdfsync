package coderarjob.kpdfsync.poc;

import javax.swing.*;
import java.awt.Component;
import java.awt.FlowLayout;

public class PageNumberListRenderer extends JLabel implements ListCellRenderer<PageResource>
{

  private ImageIcon okayIcon;
  private ImageIcon warningIcon;
  private ImageIcon errorIcon;

  public PageNumberListRenderer()
  {
    okayIcon = new ImageIcon ("src/coderarjob/kpdfsync/poc/res/check-mark.png");
    warningIcon = new ImageIcon ("src/coderarjob/kpdfsync/poc/res/attention.png");
    errorIcon = new ImageIcon ("src/coderarjob/kpdfsync/poc/res/problems.png");

    this.setOpaque (true);
  }

  public Component getListCellRendererComponent(JList<? extends PageResource> list,
      PageResource value, int index, boolean isSelected,
      boolean cellHasFocus)
  {
    this.setText (String.valueOf (value.getPageNumber()));
    String status;
    try {
      this.setIcon (value.isPairsComplete() ? okayIcon : warningIcon);
    } catch (Exception ex) {
      this.setIcon (errorIcon);
    }

    if (isSelected)
      this.setBackground (list.getSelectionBackground());
    else
      this.setBackground (list.getBackground());

    return this;
  }

}
