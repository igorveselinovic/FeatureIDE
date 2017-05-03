/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2016  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 * 
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.fm.core.io.xpath;

import de.ovgu.featureide.fm.core.PluginID;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureStructure;
import de.ovgu.featureide.fm.core.io.IFeatureModelFormat;
import de.ovgu.featureide.fm.core.io.IPersistentFormat;
import de.ovgu.featureide.fm.core.io.ProblemList;

/**
 * Prints feature models in their XPath representation
 * 
 * @author Igor Veselinovic
 */
public class XPathFormat implements IFeatureModelFormat {
	
	private final String NL = System.lineSeparator();
	
	public static final String ID = PluginID.PLUGIN_ID + ".format.fm." + XPathFormat.class.getSimpleName();

	@Override
	public ProblemList read(IFeatureModel object, CharSequence source) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String write(IFeatureModel object) {
		return processModel(object.getStructure().getRoot(), new StringBuffer(), "").toString();
	}
	
	/**
	 * Process a feature model and convert it to its equivalent XPath representation
	 * 
	 * @param node The current feature node being processed
	 * @param fullOutput The full XPath representation of all features
	 * @param line The current line (one XPath query)
	 * @return The full XPath representation of all features
	 */
	private StringBuffer processModel (IFeatureStructure node, StringBuffer fullOutput, String line) {
		line += "/" + node.getFeature();
		
		// Optional features need predicate to specify whether they are selected
		if (!node.isMandatory()) {
			fullOutput.append(line + "[@select=\"0\"]" + NL);
			line += "[@select=\"1\"]";
		}
		
		// If no children, then the end of the current query has been reached
		if (node.getChildrenCount() == 0)
			fullOutput.append(line + NL);
		
		// Process all the children (if any) of the current feature
		for (int i = 0; i < node.getChildrenCount(); i++)
			processModel(node.getChildren().get(i), fullOutput, line);
		
		return fullOutput;
	}

	@Override
	public String getSuffix() {
		return "xpath";
	}

	@Override
	public IPersistentFormat<IFeatureModel> getInstance() {
		return this;
	}

	@Override
	public boolean supportsRead() {
		return false;
	}

	@Override
	public boolean supportsWrite() {
		return true;
	}

	@Override
	public String getId() {
		return ID;
	}
	
}
