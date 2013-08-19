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
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.cytoscape.view.layout.AbstractLayoutTask#doLayout(org.cytoscape
			 * .work.TaskMonitor)
			 */
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
				/*
				 * double newScale = networkView.getVisualProperty(
				 * BasicVisualLexicon.NETWORK_SCALE_FACTOR).doubleValue() * 0.5;
				 * networkView.setVisualProperty(
				 * BasicVisualLexicon.NETWORK_SCALE_FACTOR, newScale);
				 * networkView.updateView();
				 */
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

				// Set visual property.
				double maxX = 0.0d, minX = 0.0d, maxY = 0.0d, minY = 0.0d;
				for (final View<CyNode> nView : nodesToLayOut) {

					if (nView.getVisualProperty(xLoc) > maxX) {
						maxX = nView.getVisualProperty(xLoc);
					}
					if (nView.getVisualProperty(xLoc) < minX) {
						minX = nView.getVisualProperty(xLoc);
					}
					if (nView.getVisualProperty(yLoc) < minY) {
						minY = nView.getVisualProperty(yLoc);
					}
					if (nView.getVisualProperty(yLoc) > maxY) {
						maxY = nView.getVisualProperty(yLoc);
					}

				}

				for (final View<CyNode> nView : nodesToLayOut) {

					// boolean left = nView.getVisualProperty(xLoc) < focusX;
					boolean focus = (nView.getVisualProperty(xLoc) == focusX);
					boolean focus1 = (nView.getVisualProperty(yLoc) == focusY);
					// System.out.println(focus&&focus1);
					if (focus && focus1) {
						nView.setVisualProperty(
								BasicVisualLexicon.NODE_HEIGHT,
								1.5 * nView
										.getVisualProperty(BasicVisualLexicon.NODE_SIZE));
						nView.setVisualProperty(
								BasicVisualLexicon.NODE_WIDTH,
								1.5 * nView
										.getVisualProperty(BasicVisualLexicon.NODE_SIZE));
					} else {
						currX = fisheyeDistortion(
								nView.getVisualProperty(xLoc), focusX, 1.38,
								-minX, maxX
						/*
						 * networkView.getVisualProperty(
						 * BasicVisualLexicon.NETWORK_WIDTH) .doubleValue() / 2,
						 * networkView.getVisualProperty(
						 * BasicVisualLexicon.NETWORK_WIDTH) .doubleValue() / 2
						 */);
						nView.setVisualProperty(xLoc, currX);

						currY = fisheyeDistortion(
								nView.getVisualProperty(yLoc), focusY, 1.38,
								-minY, maxY
						/*
						 * networkView.getVisualProperty(
						 * BasicVisualLexicon.NETWORK_HEIGHT) .doubleValue() /
						 * 2, networkView.getVisualProperty(
						 * BasicVisualLexicon.NETWORK_HEIGHT) .doubleValue() / 2
						 */);
						nView.setVisualProperty(yLoc, currY);
						nView.setVisualProperty(BasicVisualLexicon.NODE_LABEL,
								"");
						networkView.updateView();
						double newSize = fisheyeDistortSize(
								nView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH),
								nView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT),
								nView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
								nView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION),
								focusX, focusY, minX, maxX, minY, maxY);
						nView.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT,
								newSize);
						nView.setVisualProperty(BasicVisualLexicon.NODE_WIDTH,
								newSize);
						/*
						 * double newSize = fisheyeDistortSize(focusX, focusY,
						 * currX, currY,
						 * nView.getVisualProperty(BasicVisualLexicon
						 * .NODE_SIZE));
						 * nView.setVisualProperty(BasicVisualLexicon
						 * .NODE_HEIGHT, newSize);
						 * nView.setVisualProperty(BasicVisualLexicon
						 * .NODE_WIDTH, newSize);
						 */
					}
				}
				networkView.updateView();

				/*
				 * for (final View<CyNode> nView : nodesToLayOut) {
				 * 
				 * boolean focus = (nView.getVisualProperty(xLoc) == focusX);
				 * boolean focus1 = (nView.getVisualProperty(yLoc) == focusY);
				 * if (!focus || !focus1) { double newSize = fisheyeDistortSize(
				 * nView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH),
				 * nView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT),
				 * nView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
				 * nView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION),
				 * focusX, focusY, minX, maxX, minY, maxY);
				 * networkView.getVisualProperty(
				 * BasicVisualLexicon.NETWORK_WIDTH) .doubleValue() / 2,
				 * networkView.getVisualProperty(
				 * BasicVisualLexicon.NETWORK_HEIGHT) .doubleValue() / 2);
				 * System.out.println(nView.getModel().getSUID() + "->" +
				 * newSize);
				 * nView.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT,
				 * newSize); nView.setLockedValue(BasicVisualLexicon.NODE_WIDTH,
				 * newSize);
				 * 
				 * networkView.updateView(); } }
				 */

			}
		};
		return new TaskIterator(task);
	}

	public double fisheyeDistortSize(double focusX, double focusY,
			double nodeX, double nodeY, double size) {

		double distance = Math.sqrt(Math.pow(focusX - nodeX, 2)
				+ Math.pow(focusY - nodeY, 2));
		return 1.5 * Math.pow(size, 2) / distance;
	}

	/**
	 * Returns the x or y coordinate of the node after applying fisheye
	 * distortion to either the x or y coordinate of the node.
	 * 
	 * @param currNodePosition
	 *            Node position in the normal view i.e. the x or the y
	 *            coordinate of the node depending on whether you want to apply
	 *            the distortion in the x direction or the y direction
	 * @param focusCoordinate
	 *            x or y coordinate of the focus about which you want to apply
	 *            fisheye distortion
	 * @param d distortion factor
	 * @param min
	 *            Minimum Coordinate of the network
	 * @param max
	 *            Maximum Coordinate of the network
	 * @return currNodePosition Node Coordinate after applying the fisheye
	 *         distortion
	 */
	public double fisheyeDistortion(double currNodePosition,
			double focusCoordinate, double d, double min, double max) {
		if (d != 0) {
			boolean left = currNodePosition < focusCoordinate;
			double m = left ? focusCoordinate + min : max - focusCoordinate;
			if (m == 0) {
				m = max - min;
			}
			double v = Math.abs((currNodePosition - focusCoordinate) / m);
			v = (d + 1) / (d + 1 / (v));
			return (left ? -1 : 1) * m * v + focusCoordinate;
		} else
			return currNodePosition;
	}

	/**
	 * Returns the size of a node after applying the fisheye distortion. This method should
	 * be called after the node position has been distorted by calling the fisheyeDistortion 
	 * method.
	 * 
	 * @param nodeSizeW Node width
	 * @param nodeSizeH Node Height
	 * @param nodeX Node x - coordinate
	 * @param nodeY Node y - coordinate
	 * @param focusX Focus x - coordinate
	 * @param focusY Focus y - coordinate
	 * @param boundMinX Minimum x - coordinate of a node in the network
	 * @param boundMaxX Maximum x - coordinate of a node in the network
	 * @param boundMinY Minimum y - coordinate of a node in the network
	 * @param boundMaxY Maximum y - coordinate of a node in the network
	 * @return distortedNodeSize
	 */
	public double fisheyeDistortSize(double nodeSizeW, double nodeSizeH,
			double nodeX, double nodeY, double focusX, double focusY,
			double boundMinX, double boundMaxX, double boundMinY,
			double boundMaxY) {

		double fx = 1, fy = 1;
		double minX, maxX;

		minX = nodeX - nodeSizeW / 2;
		maxX = nodeX + nodeSizeW / 2;

		double xx = (Math.abs(minX - focusX) > Math.abs(maxX - focusX) ? minX
				: maxX);

		if (xx < boundMinX || xx > boundMaxX)
			xx = (xx == minX ? maxX : minX);
		fx = fisheyeDistortion(xx, focusX, 1.38, -boundMinX, boundMaxX);
		fx = Math.abs(nodeX - fx) / nodeSizeW;

		double minY, maxY;

		minY = nodeY + nodeSizeH / 2;
		maxY = nodeY - nodeSizeH / 2;

		double yy = (Math.abs(minY - focusY) > Math.abs(maxY - focusY) ? minY
				: maxY);

		if (yy < boundMinY || yy > boundMaxY)
			yy = (yy == minY ? maxY : minY);
		fy = fisheyeDistortion(yy, focusY, 1.38, -boundMinY, boundMaxY);
		fy = Math.abs(nodeY - fy) / nodeSizeH;

		double sf = Math.min(fx, fy);
		if (Double.isInfinite(sf) || Double.isNaN(sf)) {
			return 1.0;
		} else {

			return 25.0 * sf;
		}
	}
}
