package de.ovgu.featureide.ui.projectExplorer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import de.ovgu.featureide.core.CorePlugin;
import de.ovgu.featureide.core.IFeatureProject;
import de.ovgu.featureide.core.builder.IComposerExtensionClass;
import de.ovgu.featureide.core.fstmodel.FSTClass;
import de.ovgu.featureide.core.fstmodel.FSTModel;
import de.ovgu.featureide.core.fstmodel.FSTRole;
import de.ovgu.featureide.fm.core.Feature;
import de.ovgu.featureide.fm.core.color.FeatureColorManager;
import de.ovgu.featureide.ui.projectExplorer.DrawImageForProjectExplorer.ExplorerObject;

/**
 * Labelprovider for projectExplorer - sets an image and a text before the files, folders and packages
 * 
 * @author Jonas Weigt
 */
@SuppressWarnings("restriction")
public class ProjectExplorerLabelProvider implements ILabelProvider {

	/*
	 * constant to create space for the image 
	 */
	private static final String SPACE_STRING = "             ";

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 * sets custom colored image instead of package, files or folders 
	 */
	@Override
	public Image getImage(Object element) {
		Set<Integer> elementColors = new HashSet<Integer>();
		//returns the image for packages
		if (element instanceof PackageFragment) {
			PackageFragment frag = (PackageFragment) element;
			IFolder folder = (IFolder) frag.getResource();
			IResource res = frag.getParent().getResource();
			if (res == null) {
				return null;
			}
			IFeatureProject featureProject = CorePlugin.getFeatureProject(res);
			if (featureProject == null) {
				return null;
			}
			FSTModel model = featureProject.getFSTModel();
			if (model.getClasses().isEmpty()) {
				featureProject.getComposer().buildFSTModel();
				model = featureProject.getFSTModel();
			}
			IComposerExtensionClass composer = featureProject.getComposer();
			getPackageColors(folder, elementColors, model, !composer.hasFeatureFolder() && !composer.hasSourceFolder());
			return DrawImageForProjectExplorer.drawExplorerImage(ExplorerObject.PACKAGE, new ArrayList<Integer>(elementColors));

		}

		// returns the image for folders and preprocessor files
		if (element instanceof IResource) {
			IFeatureProject featureProject = CorePlugin.getFeatureProject((IResource) element);
			if (featureProject == null) {
				return null;
			}
			IComposerExtensionClass composer = featureProject.getComposer();
			FSTModel model = featureProject.getFSTModel();
			if (model == null || model.getClasses().isEmpty()) {
				featureProject.getComposer().buildFSTModel();
				model = featureProject.getFSTModel();
			}

			if (composer.hasFeatureFolder()) {
				if (element instanceof IFolder) {
					IFolder folder = (IFolder) element;
					//folder inSourceFolder but not SourceFolder itself
					if (folder.getParent().equals(featureProject.getSourceFolder())) {
						getFeatureFolderColors(folder, elementColors, featureProject);
						return DrawImageForProjectExplorer.getFOPModuleImage(new ArrayList<Integer>(elementColors));
					} else if (isInSourceFolder(folder)) {
						return DrawImageForProjectExplorer.getPackageImage();
					}
				}
			}

			if (composer.hasSourceFolder() && !composer.hasFeatureFolder()) {
				if (element instanceof IFolder) {
					IFolder folder = (IFolder) element;
					if (isInSourceFolder(folder) && !folder.equals(featureProject.getSourceFolder())) {
						getPackageColors(folder, elementColors, model, true);
						return DrawImageForProjectExplorer.drawExplorerImage(ExplorerObject.PACKAGE, new ArrayList<Integer>(elementColors));
					}
				}
				if (element instanceof IFile) {
					IFile file = (IFile) element;
					IContainer folder = file.getParent();
					if (folder instanceof IFolder) {
						if (isInSourceFolder(file)) {
							getPackageColors((IFolder) folder, elementColors, model, true);
							return DrawImageForProjectExplorer.drawExplorerImage(isJavaFile(file) ? ExplorerObject.JAVA_FILE : ExplorerObject.FILE, new ArrayList<Integer>(elementColors));
						}
					}
				}
			}
		}

		// returns the image for composed files
		if (element instanceof org.eclipse.jdt.internal.core.CompilationUnit) {

			CompilationUnit cu = (CompilationUnit) element;

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath path = cu.getPath();
			IFile myfile = root.getFile(path);
			IFeatureProject featureProject = CorePlugin.getFeatureProject(myfile);
			FSTModel model = featureProject.getFSTModel();
			IComposerExtensionClass composer = featureProject.getComposer();
			if (model.getClasses().isEmpty()) {
				composer.buildFSTModel();
				model = featureProject.getFSTModel();
			}
			getColors(elementColors, myfile, model, !composer.hasFeatureFolder() && !composer.hasSourceFolder());
			return DrawImageForProjectExplorer.drawExplorerImage(ExplorerObject.JAVA_FILE, new ArrayList<Integer>(elementColors));
		}

		return null;
	}

	private boolean isJavaFile(final IFile file) {
		final String fileExtension = file.getFileExtension();
		return fileExtension.equals("java") || fileExtension.equals("jak");
	}

	/**
	 * @param folder
	 * @param featureProject
	 * @return color for featureFolders
	 */
	private void getFeatureFolderColors(IFolder folder, Set<Integer> myColors, IFeatureProject featureProject) {
		Feature feature = featureProject.getFeatureModel().getFeature(folder.getName());
		myColors.add(FeatureColorManager.getColor(feature).getValue());
	}

	/**
	 * @param myColors
	 * @param myfile
	 * @param model
	 * @param colorUnselectedFeature
	 * @return colors for files
	 */
	private void getColors(Set<Integer> myColors, IFile myfile, FSTModel model, boolean colorUnselectedFeature) {
		FSTClass clazz = model.getClass(model.getAbsoluteClassName(myfile));
		if (clazz == null) {
			return;
		}
		for (FSTRole r : clazz.getRoles()) {
			if (colorUnselectedFeature || r.getFeature().isSelected()) {
				if (r.getFeature().getColor() != -1) {
					myColors.add(r.getFeature().getColor());
				}
			}
		}
	}

	/**
	 * @param folder
	 * @param colorUnselectedFeature
	 * @return colors for packages
	 */
	private void getPackageColors(IFolder folder, Set<Integer> myColors, FSTModel model, boolean colorUnselectedFeature) {
		try {
			for (IResource member : folder.members()) {
				if (member instanceof IFile) {
					IFile file = (IFile) member;
					getColors(myColors, file, model, colorUnselectedFeature);
				}
				if (member instanceof IFolder) {
					getPackageColors((IFolder) member, myColors, model, colorUnselectedFeature);
				}
			}
		} catch (CoreException e) {
			CorePlugin.getDefault().logError(e);
		}

	}

	/**
	 * @param folder
	 * @return if the Folder is in the Source Folder of the project
	 */
	private boolean isInSourceFolder(IResource res) {
		return isInFolder(res, CorePlugin.getFeatureProject(res).getSourceFolder());
	}

	/**
	 * @param res
	 * @return if the Folder is in the build folder of the project
	 */
	private boolean isInBuildFolder(IResource res) {
		return isInFolder(res, CorePlugin.getFeatureProject(res).getBuildFolder());
	}

	private boolean isInFolder(IResource folder, IFolder parentFolder) {
		IContainer parent = folder.getParent();
		if (parent.equals(parentFolder)) {
			return true;
		}
		if (parent instanceof IFolder) {
			return isInFolder((IFolder) parent, parentFolder);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 * sets customized text to have spacing for our image
	 */
	@Override
	public String getText(Object element) {

		//text for Packages
		if (element instanceof PackageFragment) {
			PackageFragment frag = (PackageFragment) element;
			IResource resource = frag.getParent().getResource();
			if (resource == null) {
				return null;
			}
			IFeatureProject featureProject = CorePlugin.getFeatureProject(resource);
			if (featureProject == null) {
				return null;
			}
			String mytest = frag.getElementName();
			if (mytest.isEmpty()) {
				return SPACE_STRING + "(default package)";
			}
			return SPACE_STRING + mytest;
		}

		//text for Folders
		if (element instanceof IResource) {
			IFeatureProject featureProject = CorePlugin.getFeatureProject((IResource) element);
			if (featureProject != null) {
				IComposerExtensionClass composer = featureProject.getComposer();
				if (composer.hasFeatureFolder()) {
					if (element instanceof IFolder) {
						IFolder folder = (IFolder) element;
						//folder inSourceFolder but not SourceFolder itself
						if (isInSourceFolder(folder) && !folder.equals(featureProject.getSourceFolder())) {
							return "  " + folder.getName();
						}
					}
				} else {
					if (element instanceof IResource) {
						IResource res = (IResource) element;
						if (isInBuildFolder(res) || isInSourceFolder(res)) {
							return SPACE_STRING + res.getName();
						}
					}
				}
			}

		}

		//text for composed files
		if (element instanceof org.eclipse.jdt.internal.core.CompilationUnit) {

			CompilationUnit cu = (CompilationUnit) element;

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath path = cu.getPath();
			IFile myfile = root.getFile(path);
			return SPACE_STRING + myfile.getName();

		}

		return null;
	}

}
