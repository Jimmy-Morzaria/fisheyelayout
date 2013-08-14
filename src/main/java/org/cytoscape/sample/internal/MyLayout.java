package org.cytoscape.sample.internal;

import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.layout.AbstractLayoutTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

public class MyLayout extends AbstractLayoutAlgorithm {
	/**
	 * Creates a new MyLayout object.
	 */
	public MyLayout(UndoSupport undo) {
		super("myLayout", "My Layout", undo);
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView,
			Object context, Set<View<CyNode>> nodesToLayOut, String attrName) {
		Task task = new AbstractLayoutTask(toString(), networkView,
				nodesToLayOut, attrName, undoSupport) {
			@Override
			protected void doLayout(TaskMonitor taskMonitor) {
				double currX = 0.0d;
				double currY = 0.0d;
				double focusX = 0.0d;
				double focusY = 0.0d;

				final VisualProperty<Double> xLoc = BasicVisualLexicon.NODE_X_LOCATION;
				final VisualProperty<Double> yLoc = BasicVisualLexicon.NODE_Y_LOCATION;
				List<CyNode> selectedNodes = CyTableUtil.getNodesInState(
						networkView.getModel(), "selected", true);
				double newScale = networkView.getVisualProperty(
						BasicVisualLexicon.NETWORK_SCALE_FACTOR).doubleValue() * 0.1;
				networkView.setVisualProperty(
						BasicVisualLexicon.NETWORK_SCALE_FACTOR, newScale);
				networkView.updateView();
				focusX = networkView.getNodeView(selectedNodes.get(0))
						.getVisualProperty(xLoc);
				focusY = networkView.getNodeView(selectedNodes.get(0))
						.getVisualProperty(yLoc);
				System.out
						.println(networkView
								.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH)
								+ "  "
								+ networkView
										.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT));
				// System.out.println(focusX+ " "+focusY);
				// Set visual property.
				for (final View<CyNode> nView : nodesToLayOut) {

					// boolean left = nView.getVisualProperty(xLoc) < focusX;
					boolean focus = (nView.getVisualProperty(xLoc) == focusX);
					boolean focus1 = (nView.getVisualProperty(yLoc) == focusY);
					// System.out.println(focus&&focus1);
					if (focus && focus1) {
						// System.out.println("true");
					} else {
						currX = fisheyeDistortion(
								nView.getVisualProperty(xLoc),
								focusX,
								4.38,
								networkView.getVisualProperty(
										BasicVisualLexicon.NETWORK_WIDTH)
										.doubleValue() / 2,
								networkView.getVisualProperty(
										BasicVisualLexicon.NETWORK_WIDTH)
										.doubleValue() / 2);
						nView.setVisualProperty(xLoc, currX);
						networkView.updateView();
						currY = fisheyeDistortion(
								nView.getVisualProperty(yLoc),
								focusY,
								4.38,
								networkView.getVisualProperty(
										BasicVisualLexicon.NETWORK_HEIGHT)
										.doubleValue() / 2,
								networkView.getVisualProperty(
										BasicVisualLexicon.NETWORK_HEIGHT)
										.doubleValue() / 2);
						nView.setVisualProperty(yLoc, currY);
						networkView.updateView();
					}
				}

				for (final View<CyNode> nView : nodesToLayOut) {
					double newSize = fisheyeDistortSize(
							nView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH),
							nView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT),
							nView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
							nView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION),
							focusX,
							focusY,
							networkView.getVisualProperty(
									BasicVisualLexicon.NETWORK_HEIGHT)
									.doubleValue() / 2,
							networkView.getVisualProperty(
									BasicVisualLexicon.NETWORK_HEIGHT)
									.doubleValue() / 2);
					System.out.println(newSize);
					nView.setVisualProperty(BasicVisualLexicon.NODE_SIZE,
							newSize);
					
					//networkView.updateView();
				}
				
			}
		};
		return new TaskIterator(task);
	}

	public double fisheyeDistortion(double currNodePosition,
			double focusCoordinate, double d, double min, double max) {
		if (d != 0) {
			boolean left = currNodePosition < focusCoordinate;
			double m = left ? focusCoordinate + min : max - focusCoordinate;
			if (m == 0) {
				m = max - min;
			}
			double v = Math.abs((currNodePosition - focusCoordinate) / m);
			v = d / (d + 1 / (v));
			return (left ? -1 : 1) * m * v + focusCoordinate;
		} else
			return currNodePosition;
	}

	public double fisheyeDistortSize(double nodeSizeW, double nodeSizeH,
			double nodeX, double nodeY, double focusX, double focusY,
			double width, double height) {

		double fx = 1, fy = 1;
		double minX, maxX;
		if (nodeX > 0) {
			minX = nodeX - nodeSizeW / 2;
			maxX = nodeX + nodeSizeW / 2;
		} else {
			minX = nodeX + nodeSizeW / 2;
			maxX = nodeX - nodeSizeW / 2;
		}

		double xx = (Math.abs(minX - focusX) > Math.abs(maxX - focusX) ? minX
				: maxX);

		if (xx < -width / 2 || xx > width / 2)
			xx = (xx == minX ? maxX : minX);
		fx = fisheyeDistortion(xx, focusX, 4.38, -width / 2, width / 2);
		fx = Math.abs(nodeX - fx) / nodeSizeW;

		double minY, maxY;
		if (nodeY > 0) {
			minY = nodeY - nodeSizeH / 2;
			maxY = nodeY + nodeSizeH / 2;
		} else {
			minY = nodeY + nodeSizeH / 2;
			maxY = nodeY - nodeSizeH / 2;
		}
		double yy = (Math.abs(minY - focusY) > Math.abs(maxX - focusX) ? minX
				: maxX);

		if (yy < -height / 2 || yy > height / 2)
			yy = (yy == minY ? maxY : minY);
		fy = fisheyeDistortion(yy, focusY, 4.38, -height / 2, height / 2);
		fy = Math.abs(nodeY - fy) / nodeSizeH;

		double sf = Math.min(fx, fy);
		if (Double.isInfinite(sf) || Double.isNaN(sf)) {
			return 1.;
		} else {
			return 3.0 * sf;
		}
	}
}
