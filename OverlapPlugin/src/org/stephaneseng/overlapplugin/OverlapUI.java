package org.stephaneseng.overlapplugin;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implements StatisticsUI. Returns the UI to configure the metric calculation.
 */
@ServiceProvider(service = StatisticsUI.class)
public class OverlapUI implements StatisticsUI {

  private OverlapJPanel overlapJPanel;
  private Overlap overlap;

  @Override
  public String getDisplayName() {
    return "Overlap";
  }

  @Override
  public String getShortDescription() {
    return "Overlap";
  }

  @Override
  public String getCategory() {
    return StatisticsUI.CATEGORY_NODE_OVERVIEW;
  }

  @Override
  public int getPosition() {
    return 1000;
  }

  @Override
  public Class<? extends Statistics> getStatisticsClass() {
    return Overlap.class;
  }

  @Override
  public JPanel getSettingsPanel() {
    this.overlapJPanel = new OverlapJPanel();
    return overlapJPanel;
  }

  @Override
  public void setup(Statistics statistics) {
    this.overlap = (Overlap) statistics;
  }

  @Override
  public void unsetup() {
    this.overlapJPanel = null;
  }

  @Override
  public String getValue() {
    return overlap.toString();
  }

}
