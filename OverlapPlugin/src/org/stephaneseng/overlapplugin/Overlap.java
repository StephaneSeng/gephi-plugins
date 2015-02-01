package org.stephaneseng.overlapplugin;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 * Implements Statistics. Contains the algorithm of the metric.
 */
public class Overlap implements Statistics, LongTask {

  private String report = "";
  private boolean cancel = false;
  private ProgressTicket progressTicket;

  @Override
  public void execute(GraphModel graphModel, AttributeModel attributeModel) {
    // Initialize the Overlap attribute.
    AttributeTable nodeTable = attributeModel.getNodeTable();
    AttributeColumn overlapColumn = nodeTable.getColumn("overlapColumn");
    if (overlapColumn == null) {
      overlapColumn = nodeTable.addColumn("overlapColumn", "Overlap", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
    }

    Graph graph = graphModel.getGraph();
    graph.readLock();
    try {
      Progress.start(this.progressTicket, graph.getNodeCount());

      for (Node node : graph.getNodes()) {
        AttributeRow nodeRow = (AttributeRow) node.getNodeData().getAttributes();
        nodeRow.setValue(overlapColumn, 1);

        Progress.progress(progressTicket);
        if (this.cancel) {
          break;
        }
      }
      graph.readUnlockAll();
    } catch (Exception e) {
      e.printStackTrace();
      graph.readUnlockAll();
    }
  }

  @Override
  public String getReport() {
    return this.report;
  }

  @Override
  public boolean cancel() {
    this.cancel = true;
    return true;
  }

  @Override
  public void setProgressTicket(ProgressTicket progressTicket) {
    this.progressTicket = progressTicket;
  }

}
