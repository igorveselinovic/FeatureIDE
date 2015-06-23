/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2015  FeatureIDE team, University of Magdeburg, Germany
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
package de.ovgu.featureide.featurehouse.refactoring.matcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import de.ovgu.featureide.core.signature.ProjectSignatures;
import de.ovgu.featureide.core.signature.base.AbstractClassSignature;
import de.ovgu.featureide.core.signature.base.AbstractSignature;
import de.ovgu.featureide.featurehouse.refactoring.RefactoringUtil;
import de.ovgu.featureide.featurehouse.signature.fuji.FujiMethodSignature;

/**
 * TODO description
 * 
 * @author steffen
 */
public class MethodSignatureMatcher extends SignatureMatcher {

	public MethodSignatureMatcher(ProjectSignatures signatures, IMember selectedElement, String newName) {
		super(signatures, selectedElement, newName);
	}
	
	protected void addInvolvedClasses(final Set<AbstractClassSignature> involvedClasses) {
		Set<AbstractClassSignature> involvedSuperClasses = new HashSet<>();
		
		if (oldMatchedSignatures.size() > 1) {
			addSuperClasses(involvedSuperClasses, selectedSignature.getParent());
			filterSuperClasses(involvedSuperClasses, oldMatchedSignatures);
			involvedClasses.addAll(involvedSuperClasses); 
			if (!selectedSignature.isPrivate()) {
				addSubClasses(involvedClasses, new HashSet<>(involvedClasses));
			}
		}
		
		filterMatchedSignatures(involvedClasses, oldMatchedSignatures);
	}
	
	private void filterMatchedSignatures(final Set<AbstractClassSignature> involvedClasses, final Set<AbstractSignature> matchedSignatures) {
		for (AbstractSignature matchedSignature : new HashSet<>(matchedSignatures)) {
			if (!(involvedClasses.contains(matchedSignature.getParent())))
				matchedSignatures.remove(matchedSignature);
		}
	}
	
	protected void addSubClasses(final Set<AbstractClassSignature> involvedClasses, final Set<AbstractClassSignature> matchedClasses) {
		for (AbstractClassSignature abstractClassSignature : matchedClasses) {
			addSubClasses(involvedClasses, abstractClassSignature);
		}
	}
	
	protected void addSubClasses(final Set<AbstractClassSignature> result, final AbstractClassSignature classSignature) {
		if (classSignature == null)
			return;

		addSubClassesForNames(result, classSignature.getImplementList());
		addSubClassesForNames(result, classSignature.getSubClassesList());
	}
	
	protected void addSubClassesForNames(final Set<AbstractClassSignature> result, final Set<String> names) {
		for (String className : names) {
			if (!classes.containsKey(className))
				continue;

			final AbstractClassSignature classSignature = classes.get(className);
			if (classSignature == null)
				return;

			if (!result.contains(classSignature)) {
				result.add(classSignature);
				addSubClasses(result, classSignature);
			}
		}
	}
	
	protected void addSuperClassesForNames(final Set<AbstractClassSignature> result, final Set<String> names) {
		for (String className : names) {
			if (!classes.containsKey(className))
				continue;

			final AbstractClassSignature classSignature = classes.get(className);
			if (classSignature == null)
				return;

			if (!result.contains(classSignature)) {
				result.add(classSignature);
				addSuperClasses(result, classSignature);
			}
		}
	}
	
	private void addSuperClasses(final Set<AbstractClassSignature> result, final AbstractClassSignature classSignature) {
		if (classSignature == null)
			return;

		addSuperClassesForNames(result, classSignature.getImplementList());
		addSuperClassesForNames(result, classSignature.getExtendList());
	}


	@Override
	protected boolean hasSameType(AbstractSignature signature) {
		return (signature instanceof FujiMethodSignature);
	}


	@Override
	protected boolean checkSignature(AbstractSignature signature) {
		return hasSameType(signature) && RefactoringUtil.hasSameName(signature, selectedElement)
				&& hasSameParameters((FujiMethodSignature) signature) && hasSameReturnType((FujiMethodSignature) signature);
	}
	
	

	private void filterSuperClasses(final Set<AbstractClassSignature> involvedClasses, final Set<AbstractSignature> matchedSignatures) {
		for (AbstractClassSignature involvedClass : new HashSet<>(involvedClasses)) {
			AbstractSignature matchedSignature = getMatchedSignature(involvedClass, matchedSignatures);
			if ((matchedSignature == null) || ((matchedSignature != null) && matchedSignature.isPrivate())) {
				involvedClasses.remove(involvedClass);
			}
		}
	}
	
	private AbstractSignature getMatchedSignature(final AbstractClassSignature signature, final Set<AbstractSignature> matchedSignatures) {
		for (AbstractSignature match : matchedSignatures) {
			if (checkSignature(match) && match.getParent().equals(signature)) {
				return match;
			}
		}
		return null;
	}
	
	private boolean hasSameParameters(final FujiMethodSignature signature) {
		List<String> parameterTypes = signature.getParameterTypes();

		final IMethod method = (IMethod) selectedElement;
		final int myParamsLength = method.getParameterTypes().length;
		final String[] simpleNames = new String[myParamsLength];
		for (int i = 0; i < myParamsLength; i++) {
			String erasure = Signature.getTypeErasure(method.getParameterTypes()[i]);
			simpleNames[i] = Signature.getSimpleName(Signature.toString(erasure));
		}

		return Arrays.equals(simpleNames, parameterTypes.toArray(new String[parameterTypes.size()]));
	}

	private boolean hasSameReturnType(final FujiMethodSignature signature) {
		try {
			return signature.getReturnType().equals(Signature.toString(((IMethod) selectedElement).getReturnType()));
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public Set<AbstractSignature> getMatchedSignatures()
	{
		return oldMatchedSignatures;
	}
	
}