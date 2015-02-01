package org.stephaneseng.overlapplugin;

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * JPanel used to configure the Overlap metric calculation.
 *
 * @author StephaneSeng
 */
public class OverlapJPanel extends JPanel {

  private final GridLayout gridLayout;

  private Double scale;
  private final JLabel scaleJLabel;
  private final JTextField scaleJTextField;

  public OverlapJPanel() {
    this.gridLayout = new GridLayout(1, 2);
    this.setLayout(gridLayout);

    scaleJLabel = new JLabel("Scale:");
    this.add(scaleJLabel);

    scaleJTextField = new JTextField();
    this.add(scaleJTextField);
  }

  public Double getScale() {
    return Double.valueOf(scaleJTextField.getText());
  }

  public void setScale(double scale) {
    this.scale = scale;
    scaleJTextField.setText(Double.toString(this.scale));
  }

}
