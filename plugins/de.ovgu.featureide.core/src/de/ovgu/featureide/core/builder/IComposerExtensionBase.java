/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2013  FeatureIDE team, University of Magdeburg, Germany
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
 * See http://www.fosd.de/featureide/ for further information.
 */
package de.ovgu.featureide.core.builder;

import de.ovgu.featureide.core.IExtension;

/**
 * A FeatureIDE extension to compose source files.
 * 
 * @author Tom Brosch
 */
public interface IComposerExtensionBase extends IExtension {
	
	public static String extensionPointID = "composers";
	
	public static String extensionID = "composer";
	
	String getName();
	
	String getDescription();
	
	/**
	 * @return <code>true</code> if the composer has a folder for each features.
	 */
	boolean hasFeatureFolder();

	/**
	 * @return <code>false</code> if a source folder should not be created. Default: <code>true</code>
	 */
	boolean hasSourceFolder();
	
	/**
	 * @return <code>true</code> if the composition tool supports contract composition.
	 */
	boolean hasContractComposition();
	
	/**
	 * @return <code>true</code> if the composition tool supports meta product generation.
	 */
	boolean hasMetaProductGeneration();
	
	/**
	 * @return <code>true</code> if the composition tool supports different composition tools
	 */
	boolean hasCompositionMechanisms();
	
	/**
	 * @return <code>true</code> if the composition tool should create a folder for each feature 
	 */
	boolean createFolderForFeatures();
}