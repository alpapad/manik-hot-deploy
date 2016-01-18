/*******************************************************************************
 *  Manik Hot Deploy
 *  Copyright (C) 2010 Ralph Soika  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Contributors:  
 *  	Ralph Soika ,Alexander
 * 
 *******************************************************************************/
package org.imixs.eclipse.manik;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The Builder Class for hot-deployment resource files
 * 
 * @author rsoika,Alexander
 * 
 */
public class HotdeployBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "org.imixs.eclipse.manik.hotdeployBuilder";

	private static String[] IGNORE_DIRECTORIES = { "/src/main/resources/", "/src/main/java/", "/src/test/resources/",
			"/src/test/java/", "/target/m2e-wtp/", "/target/maven-archiver/", "/META-INF/", "/target/application.xml",
			"/target/test-classes/", "/target/classes/", "/WEB-INF/classes/" };
	private static String[] IGNORE_SUBDIRECTORIES = { "/classes/", "/src/main/webapp/" };

	private String hotdeployTarget = "";
	// private String autodeployTarget = "";
	// private boolean hotDeployMode = true;
	// private boolean explodeArtifact = false;
	// private boolean wildflySupport = false;
	private String sourceFilePath = "";
	private String sourceFileName = "";

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		Console console = new Console();

		if (kind == FULL_BUILD) {
			// console.println("FULL_BUILD not supported");
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				console.println("MODE not supported");
			} else {
				incrementalBuild(delta, monitor);
			}
		}

		return null;
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new HotdeployDeltaVisitor());
	}

	/**
	 * This is the main method of the HotdeployBuilder to copy the resource into
	 * the target server.
	 * 
	 * The method distinguishes between two modes: In the case that the file
	 * resource ends in .ear or .war the file will be copied into the autodeploy
	 * folder. In all other cases the method tries to perform a hot-deployment
	 * into the hot-deployment folder. The method terminates if no deployment
	 * folder is defined.
	 * 
	 * In case of a hot-deployment the target of the file to be copied is
	 * computed by the helper method computeTarget()
	 * 
	 * The method did not compute any copy of a directory resource.
	 * 
	 * If .ear or .war file is autodeployed the method checks the maven /target
	 * folder pattern. In this case only root artifacts will be deployed!
	 * 
	 * @param resource
	 *            The SourceFile
	 * 
	 * @param iResourceDelta
	 *            indicates what happened to the resource
	 *            (IResourceDelta.ADDED,IResourceDelta
	 *            .REMOVED,IResourceDelta.CHANGED)
	 * 
	 * 
	 * @throws CoreException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	void deployResource(IResource resource, int iResourceDelta) throws CoreException {

		String targetFilePath = null;

		// open a new console..
		Console console = new Console();

		// do not deploy directory resources!
		if (!(resource instanceof IFile))
			return;

		IFile file = (IFile) resource;
		// console.println(action + " " + file.getFullPath());
		sourceFileName = file.getName();
		sourceFilePath = file.getFullPath().toString();
		//sourceFilePathAbsolute = file.getRawLocation().toString();

		// we do not deploy files from the source directories
		// skip source files like /src/main/java/*
		for (String value : IGNORE_DIRECTORIES) {
			if (sourceFilePath.contains(value)) {
				console.println("Skipping resource: " + sourceFilePath + " because it contains: " + value);
				return;
			}
		}
		Configuration c = TargetPropertyPage.load(this.getProject());

		if (c.getWildflyPath() == null || c.getWildflyPath().trim().equals("")) {
			return;
		}

		// Hotdepoyment mode!
		if (hotdeployTarget == null) {
			return;
		}

		// optimize path....
		if (!hotdeployTarget.endsWith("/")) {
			hotdeployTarget += "/";
		}

		// compute the target path....
		targetFilePath = computeTarget();


		// if the target file was not computed return....
		if (targetFilePath == null) {
			return;
		}

		if (iResourceDelta == IResourceDelta.REMOVED) {
			// remove file
			File f = new File(targetFilePath);
			f.delete();
			console.println("[DELETE]: " + targetFilePath);
			return;
		}

		// HOTDEPLOY MODE

		long lStart = System.currentTimeMillis();
		copySingelResource(file, targetFilePath, console);
		if (console != null) {
			long lTime = System.currentTimeMillis() - lStart;
			// log message..
			if (sourceFileName.endsWith(".ear") || sourceFileName.endsWith(".war"))
				console.println("[AUTODEPLOY]: " + sourceFilePath + " in " + lTime + "ms to " + targetFilePath);
			else
				console.println("[HOTDEPLOY]: " + sourceFilePath + " in " + lTime + "ms to " + targetFilePath);

		}

	}

	/**
	 * This method copies a file resource into the targetPath
	 * 
	 * 
	 * @param file
	 *            Source File
	 * @param targetFilePath
	 *            target Path
	 * @param console
	 * @throws CoreException
	 */
	private void copySingelResource(IFile file, String targetFilePath, Console console) throws CoreException {

		// now copy / delete the file....
		OutputStream out = null;
		InputStream is = null;
		try {
			// Copy the file....
			is = file.getContents();
			File fOutput = new File(targetFilePath);
			out = new FileOutputStream(fOutput);
			byte buf[] = new byte[1024];
			int len;
			while ((len = is.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

		} catch (IOException ex) {
			// unable to copy file
			// console.println("[ERROR]: "+ex.getMessage());
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				if (console != null)
					console.println("[ERROR]: closing stream: " + e.getMessage());
			}

		}

	}

	/**
	 * Copies a folder.
	 * 
	 * Thanks to mkyong
	 * 
	 * http://www.mkyong.com/java/how-to-copy-directory-in-java/
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	private static void copyFolder(File src, File dest) throws IOException {

		if (src.isDirectory()) {
			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}

		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
		}
	}

	/**
	 * This method did the magic of the manik-hot-deployer. The method computes
	 * the target goal inside an application target. There are three different
	 * cases:
	 * 
	 * case-1): .war or .ear files are simply deployed into the target root. No
	 * hierarchy is computed
	 * 
	 * case-2): the target is a web application. This is indicated by the
	 * existence of an /WEB-INF folder. in this case we check for two different
	 * source files
	 * 
	 * case-2-a): the source file contains the path /target/classes/ - so we
	 * copy the source into [target]/WEB-INF/classes
	 * 
	 * case-2-b): otherwise we copy into the target root strip any
	 * /target/main/webcontent präfixes
	 * 
	 * 
	 * case-3): the target is no web application. So we can copy the source into
	 * the target root strip any /target/main/src prefixes
	 * 
	 * 
	 * @param resource
	 * @throws CoreException
	 */
	private String computeTarget() {
		File folder = null;

		// hotdeplyoment mode
		// test if deployment is enabled
		if (hotdeployTarget == null) {
			return null;
		}

		/* case-2 case a and b included */
		// test if the sourcefile contains a source path which needs to be
		// removed ?
		for (String value : IGNORE_SUBDIRECTORIES) {
			if (sourceFilePath.contains(value)) {

				String path = sourceFilePath.substring(sourceFilePath.indexOf(value) + value.length() - 0);

				// now test if the target folder is a web application and the
				// sourcfile is a /classes/ file
				// - test for /WEB-INF/ folder
				if (sourceFilePath.contains("/classes/")) {
					folder = new File(hotdeployTarget + "/WEB-INF/");
					if (folder.exists()) {
						// target is web app - so we need to extend the
						// target....
						path = "/WEB-INF/classes/" + path;
						// console.println("Target is a web application changed
						// target path to: "
						// + path);
					}
				}

				if (path.indexOf('/') > -1) {
					folder = new File(hotdeployTarget + path.substring(0, path.lastIndexOf('/')));
					// test target folder - if not exists we did not create the
					// path and return null...
					if (!folder.exists()) {
						return null;
						// console.println("Target folder does not exist.
						// Creating: "
						// + folder.getAbsolutePath());
						// folder.mkdirs();
					}
				}
				// console.println("Target is: " + target + path);
				return hotdeployTarget + path;

			}
		}
		// console.println("Target is: " + target + sourceFilePath);
		return hotdeployTarget + sourceFilePath;

	}

	class HotdeployDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();

			// tell the method if the resource should be added removed ore
			// changed
			deployResource(resource, delta.getKind());

			// return true to continue visiting children.
			return true;
		}
	}

}
