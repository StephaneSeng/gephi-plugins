package org.stephaneseng.overlapplugin;

import java.util.HashMap;
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
 *
 * @author StephaneSeng
 */
public class Overlap implements Statistics, LongTask {

  private String report = "";
  private boolean cancel = false;
  private ProgressTicket progressTicket;

  private Double scale = 1000.0;

  @Override
  public void execute(GraphModel graphModel, AttributeModel attributeModel) {
    // Initialize the Overlap attributes in the AttributeModel.
    AttributeTable nodeTable = attributeModel.getNodeTable();
    AttributeColumn xColumn = nodeTable.getColumn("xColumn");
    if (xColumn == null) {
      xColumn = nodeTable.addColumn("xColumn", "X", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
    }
    AttributeColumn yColumn = nodeTable.getColumn("yColumn");
    if (yColumn == null) {
      yColumn = nodeTable.addColumn("yColumn", "Y", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
    }
    AttributeColumn sizeColumn = nodeTable.getColumn("sizeColumn");
    if (sizeColumn == null) {
      sizeColumn = nodeTable.addColumn("sizeColumn", "SIZE", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
    }
    AttributeColumn overlapColumn = nodeTable.getColumn("overlapColumn");
    if (overlapColumn == null) {
      overlapColumn = nodeTable.addColumn("overlapColumn", "OVERLAP", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
    }

    // Initialize the Overlap attribute algorithm.
    HashMap<String, Integer> overlapHashMap = new HashMap<>();

    // For all the Graph nodes, evaluate the Overlap attribute.
    Graph graph = graphModel.getGraph();
    graph.readLock();
    try {
      Progress.start(this.progressTicket, graph.getNodeCount() * 2);

      // First graph iteration: Evaluate the numbers of nodes on the same x() and y() positions.
      for (Node node : graph.getNodes()) {
        int x = Math.round(node.getNodeData().x());
        int y = Math.round(node.getNodeData().y());
        int size = Math.round(node.getNodeData().getSize());

        // Save the node position.
        AttributeRow nodeRow = (AttributeRow) node.getNodeData().getAttributes();
        nodeRow.setValue(xColumn, x);
        nodeRow.setValue(yColumn, y);
        nodeRow.setValue(sizeColumn, size);

        // Increment the number of nodes at the same position.
        for (int i = 0; i < size; i++) {
          for (int j = 0; j < size; j++) {
            int X = x + i;
            int Y = y + i;
            String key = X + " " + Y;

            if (!overlapHashMap.containsKey(key)) {
              overlapHashMap.put(key, 0);
            }
            overlapHashMap.put(key, overlapHashMap.get(key) + 1);
          }
        }

        Progress.progress(progressTicket);
        if (this.cancel) {
          break;
        }
      }

      for (Node node : graph.getNodes()) {
        AttributeRow nodeRow = (AttributeRow) node.getNodeData().getAttributes();
        int x = (int) nodeRow.getValue(xColumn);
        int y = (int) nodeRow.getValue(yColumn);
        int size = (int) nodeRow.getValue(sizeColumn);

        int overlapTotal = 0;
        int overlapCount = 0;

        for (int i = 0; i < size; i++) {
          for (int j = 0; j < size; j++) {
            int X = x + i;
            int Y = y + i;
            String key = X + " " + Y;

            overlapTotal += overlapHashMap.get(key);
            overlapCount++;
          }
        }

        int overlap = 0;
        if (overlapCount != 0) {
          overlap = Math.round((float) (overlapTotal / overlapCount));
        }
        nodeRow.setValue(overlapColumn, overlap);

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

  public Double getScale() {
    return scale;
  }

  public void setScale(Double scale) {
    this.scale = scale;
  }

}
