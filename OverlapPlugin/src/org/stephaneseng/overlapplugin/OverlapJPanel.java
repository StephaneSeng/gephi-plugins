package org.stephaneseng.overlapplugin;

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * JPanel used to configure the Overlap metric calculation.
 */
public class OverlapJPanel extends JPanel {

  private GridLayout gridLayout;
  private JLabel latitudeAttributeJLabel;

  public OverlapJPanel() {
    this.gridLayout = new GridLayout(1, 2);
    this.setLayout(gridLayout);

    latitudeAttributeJLabel = new JLabel("Latitude:");
    this.add(latitudeAttributeJLabel);
  }

}
