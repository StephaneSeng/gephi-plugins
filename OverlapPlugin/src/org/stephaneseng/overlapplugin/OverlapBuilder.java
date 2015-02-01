package org.stephaneseng.overlapplugin;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implements StatisticsBuilder. Returns metadata about the implemented metric.
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class OverlapBuilder implements StatisticsBuilder {

  @Override
  public String getName() {
    return "Overlap";
  }

  @Override
  public Statistics getStatistics() {
    return new Overlap();
  }

  @Override
  public Class<? extends Statistics> getStatisticsClass() {
    return Overlap.class;
  }

}
