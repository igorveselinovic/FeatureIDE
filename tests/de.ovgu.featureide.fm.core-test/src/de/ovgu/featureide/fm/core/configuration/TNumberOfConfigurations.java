/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2017  FeatureIDE team, University of Magdeburg, Germany
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
package de.ovgu.featureide.fm.core.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.ovgu.featureide.fm.core.base.IFeatureModel;

/**
 * Tests about the calculation of the number of valid configurations.
 * 
 * @author Thomas Thuem
 * @author Fabian Benduhn
 */
public class TNumberOfConfigurations extends AbstractConfigurationTest {

	@Override
	IFeatureModel loadModel() {
		return null;
	}
			
	@Test
	public void testOnlyRoot() {
		IFeatureModel fm = loadXML("<feature mandatory=\"true\" name=\"S\"/>");
		Configuration c = new Configuration(fm);
		assertEquals(1, c.number());
	}
	
	@Test
	public void testVoidOnlyRoot() {
		IFeatureModel fm = loadXML("<feature mandatory=\"true\" name=\"S\"/>",
				"<rule><not><var>S</var></not></rule>");
		Configuration c = new Configuration(fm);
		assertEquals(0, c.number());
	}

	@Test
	public void testVoidModel() {
		IFeatureModel fm = loadXML(
				"<and mandatory=\"true\" name=\"S\"><feature mandatory=\"true\" name=\"a\"/></and>",
				"<rule><not><var>a</var></not></rule>");
		Configuration c = new Configuration(fm);
		assertEquals(0, c.number());
	}


	@Test
	public void testOnlyMandatory() {
			IFeatureModel fm = loadXML(
			"	<and mandatory=\"true\" name=\"S\">	<feature mandatory=\"true\" name=\"A\"/></and>");
	Configuration c = new Configuration(fm);
	assertEquals(1, c.number());
	}

	@Test
	public void testOnlyOptional() {
		IFeatureModel fm = loadXML(
				"	<and mandatory=\"true\" name=\"S\">	<feature mandatory=\"false\" name=\"A\"/></and>");
		Configuration c = new Configuration(fm);
		assertEquals(2, c.number());
	}

	@Test
	public void testAndGroup() {
		IFeatureModel fm = loadXML(
				"<and mandatory=\"true\" name=\"S\"><feature mandatory=\"true\" name=\"A\"/><feature name=\"B\"/></and>");
		Configuration c = new Configuration(fm);
		assertEquals(2, c.number());
	}

	@Test
	public void testOnlyOrGroup() {
		IFeatureModel fm = loadXML(
				"<or mandatory=\"true\" name=\"S\"><feature mandatory=\"true\" name=\"A\"/><feature mandatory=\"true\" name=\"B\"/></or>");
		Configuration c = new Configuration(fm);
		assertEquals(3, c.number());
	}

	
	//mandatory true/false shouldnt matter in OR group
	@Test
	public void testOnlyOrGroup2() {
		IFeatureModel fm = loadXML(
				"<or mandatory=\"true\" name=\"S\"><feature mandatory=\"false\" name=\"A\"/><feature mandatory=\"false\" name=\"B\"/></or>");
		Configuration c = new Configuration(fm);
		assertEquals(3, c.number());
	}
	
	@Test
	public void testAlternativeGroup() {
		IFeatureModel fm = loadXML(
				"<alt mandatory=\"true\" name=\"S\"><feature mandatory=\"true\" name=\"A\"/><feature mandatory=\"true\" name=\"B\"/></alt>");
		Configuration c = new Configuration(fm);
		assertEquals(2, c.number());
	}
	
	@Test
	public void testAlternativeGroup2() {
		IFeatureModel fm = loadXML(
				"<alt mandatory=\"true\" name=\"S\"><feature mandatory=\"true\" name=\"A\"/><feature mandatory=\"true\" name=\"B\"/><feature mandatory=\"true\" name=\"C\"/></alt>");
		Configuration c = new Configuration(fm);
		assertEquals(3, c.number());
	}
	
	@Test
	public void testAbstract() {
		IFeatureModel fm = loadXML(
				"<and mandatory=\"true\" name=\"S\"><feature abstract=\"true\" name=\"C\"/></and>");
		Configuration c = new Configuration(fm,true,true);
		assertEquals(2, c.number());
	}
	
	@Test
	public void testAbstract2() {
		IFeatureModel fm = loadXML(
				"<and mandatory=\"true\" name=\"S\"><feature abstract=\"true\" name=\"C\"/></and>");
		Configuration c = new Configuration(fm,true,false);
		assertEquals(1, c.number());
	}
	
	
	@Test
	public void testAbstract3() {
		IFeatureModel fm = loadXML(
				"<and mandatory=\"true\" name=\"S\"><feature abstract=\"true\" name=\"A\"/><feature name=\"B\"/><feature name=\"C\"/></and>");
		Configuration c = new Configuration(fm,true,false);
		assertEquals(4, c.number());
	}
		
	@Test
	public void testAbstract4() {
		IFeatureModel fm = loadXML(
				"<and mandatory=\"true\" name=\"S\"><feature abstract=\"true\" name=\"A\"/><feature name=\"B\"/><feature name=\"C\"/></and>");
		Configuration c = new Configuration(fm);
		assertEquals(8, c.number());
	}
	
	
	//TODO: replace selection strategy for hidden features
	@Test
	public void testHidden() {
		IFeatureModel fm = loadXML(
				"<and mandatory=\"true\" name=\"S\"><feature mandatory=\"false\" name=\"A\"/><feature hidden=\"true\" name=\"B\"/></and>");
		Configuration c = new Configuration(fm);
		long x = c.number();
		assertEquals(2, x);
	}
	
	@Test
	public void testDependendHidden() {
		IFeatureModel fm = loadXML(
			"<and mandatory=\"true\" name=\"S\">" +
				"<feature name=\"A\"/>" +
				"<feature hidden=\"true\" name=\"B\"/>" +
			"</and>" ,
			"<rule>" +
				"<eq>" +
					"<var>A</var>" +
					"<var>B</var>" +
				"</eq>" +
			"</rule>");
		Configuration c = new Configuration(fm);
		assertEquals(2, c.number());
	}
	
	@Test
	public void testWithSimplePositiveConstraint() {
		IFeatureModel fm = loadXML(
				"<and mandatory=\"true\" name=\"S\"><feature name=\"A\"/></and>",
				"<rule><var>A</var></rule>");
		Configuration c = new Configuration(fm);
		assertEquals(1, c.number());
	}

	@Test
	public void testWithSimpleNegationConstraint() {
		IFeatureModel fm = loadXML(
				"<and mandatory=\"true\" name=\"S\"><feature name=\"A\"/></and>",
				"<rule><not><var>A</var></not></rule>");
		Configuration c = new Configuration(fm);
		assertEquals(1, c.number());

	}

	@Test
	public void testWithImplicationConstraint() {
		IFeatureModel fm = loadXML(
				"<and mandatory=\"true\" name=\"S\"><feature mandatory=\"true\" name=\"A\"/><feature name=\"B\"/></and>",
				"<rule><imp><var>A</var><var>B</var></imp></rule>");
		Configuration c = new Configuration(fm);
		assertEquals(1, c.number());

	}

	@Test
	public void testWithComplexConstraints() {
		IFeatureModel fm = loadXML(
				"<and mandatory=\"true\" name=\"S\"><feature name=\"A\"/><feature name=\"B\"/><feature name=\"C\"/><feature name=\"D\"/></and>",
			"<rule><disj><var>A</var><imp><var>B</var><eq><var>C</var><not><var>D</var></not></eq></imp></disj></rule>"	);
		Configuration c = new Configuration(fm);
		assertEquals(14, c.number());

	}

	@Test
	public void testCombination1() {
		IFeatureModel fm = loadXML("<and name=\"S\">" + "<feature name=\"A\"/>"
				+ "<feature mandatory=\"true\" name=\"B\"/>"
				+ "<feature name=\"C\"/>" + "</and>");
		Configuration c = new Configuration(fm);
		assertEquals(4, c.number());
	}

	@Test
	public void testCombination2() {
		IFeatureModel fm = loadXML("<or name=\"S\">" + "<feature name=\"A\"/>"
				+ "<feature name=\"B\"/>" + "<feature name=\"C\"/>" + "</or>");
		Configuration c = new Configuration(fm);
		assertEquals(7, c.number());
	}

	@Test
	public void testCombination3() {
		IFeatureModel fm = loadXML("<alt name=\"S\">" + "<feature name=\"A\"/>"
				+ "<feature name=\"B\"/>" + "<feature name=\"C\"/>" + "</alt>");
		Configuration c = new Configuration(fm);
		assertEquals(3, c.number());
	}

	@Test
	public void testCombination4() {
		IFeatureModel fm = loadXML("<and name=\"S\">" + "<feature name=\"A\"/>"
				+ "<feature mandatory=\"true\" name=\"B\"/>"
				+ "<feature abstract=\"true\" name=\"C\"/>" + "</and>");
		Configuration c = new Configuration(fm, true, false);
		assertEquals(2, c.number());
	}

	@Test
	public void testCombination5() {
		IFeatureModel fm = loadXML("<or name=\"S\">" + "<feature name=\"A\"/>"
				+ "<feature name=\"B\"/>"
				+ "<feature abstract=\"true\" name=\"C\"/>" + "</or>");
		Configuration c = new Configuration(fm, true, false);
		assertEquals(4, c.number());
	}

	@Test
	public void testCombination6() {
		IFeatureModel fm = loadXML("<alt name=\"S\">" + "<feature name=\"A\"/>"
				+ "<feature name=\"B\"/>"
				+ "<feature abstract=\"true\" name=\"C\"/>" + "</alt>");
		Configuration c = new Configuration(fm);
		assertEquals(3, c.number());
	}

}
