package org.stephaneseng.overlapplugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
    AttributeColumn xAttributeColumn = nodeTable.getColumn("xAttributeColumn");
    if (xAttributeColumn == null) {
      xAttributeColumn = nodeTable.addColumn("xAttributeColumn", "X", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
    }
    AttributeColumn yAttributeColumn = nodeTable.getColumn("yAttributeColumn");
    if (yAttributeColumn == null) {
      yAttributeColumn = nodeTable.addColumn("yAttributeColumn", "Y", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
    }
    AttributeColumn sizeAttributeColumn = nodeTable.getColumn("sizeAttributeColumn");
    if (sizeAttributeColumn == null) {
      sizeAttributeColumn = nodeTable.addColumn("sizeAttributeColumn", "SIZE", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
    }
    AttributeColumn overlapAttributeColumn = nodeTable.getColumn("overlapAttributeColumn");
    if (overlapAttributeColumn == null) {
      overlapAttributeColumn = nodeTable.addColumn("overlapAttributeColumn", "OVERLAP", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
    }

    // Initialize the Overlap attribute algorithm.
    Map<String, Integer> overlapHashMap = new HashMap<>();
    Map<Integer, Set<Node>> overlapTreeMap = new TreeMap<>();

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
        AttributeRow attributeRow = (AttributeRow) node.getNodeData().getAttributes();
        attributeRow.setValue(xAttributeColumn, x);
        attributeRow.setValue(yAttributeColumn, y);
        attributeRow.setValue(sizeAttributeColumn, size);

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

      // Second graph iteration: Evaluate the Overlap attribute.
      for (Node node : graph.getNodes()) {
        AttributeRow attributeRow = (AttributeRow) node.getNodeData().getAttributes();
        int x = (int) attributeRow.getValue(xAttributeColumn);
        int y = (int) attributeRow.getValue(yAttributeColumn);
        int size = (int) attributeRow.getValue(sizeAttributeColumn);

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
        attributeRow.setValue(overlapAttributeColumn, overlap);

        Progress.progress(progressTicket);
        if (this.cancel) {
          break;
        }
      }

      // "Hack": Third graph iteration: Change the order of the nodes so that the more Overlapped nodes are shown in the foreground.
      for (Node node : graph.getNodes()) {
        AttributeRow attributeRow = (AttributeRow) node.getNodeData().getAttributes();
        int overlap = (int) attributeRow.getValue(overlapAttributeColumn);

        if (overlapTreeMap.get(overlap) == null) {
          overlapTreeMap.put(overlap, new HashSet<Node>());
        }
        Set<Node> overlapTreeMapSet = overlapTreeMap.get(overlap);
        overlapTreeMapSet.add(node);
        overlapTreeMap.put(overlap, overlapTreeMapSet);
      }

      graph.readUnlockAll();
    } catch (Exception e) {
      e.printStackTrace();
      graph.readUnlockAll();
    }

    graph.writeLock();
    try {
      // "Hack": "Fourth" graph iteration.
      Collection<Set<Node>> overlapTreeMapSets = overlapTreeMap.values();
      for (Set<Node> overlapTreeMapSet : overlapTreeMapSets) {
        for (Node node : overlapTreeMapSet) {
          graph.removeNode(node);
          graph.addNode(node);
        }
      }
      graph.writeUnlock();
    } catch (Exception e) {
      graph.writeUnlock();
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
