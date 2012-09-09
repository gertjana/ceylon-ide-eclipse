package com.redhat.ceylon.eclipse.core.test;

import org.eclipse.jdt.internal.junit.ui.TestRunnerViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class CUnitPlugin extends AbstractUIPlugin {
	
	private static CUnitPlugin cUnitPlugin;

	public static final String PLUGIN_ID= "com.redhat.ceylon.eclipse.core.test"; //$NON-NLS-1$
	public static final String ID_EXTENSION_POINT_JUNIT_LAUNCHCONFIGS= PLUGIN_ID + "." + "cunitLaunchConfigs"; //$NON-NLS-1$ //$NON-NLS-2$

	
	public CUnitPlugin() {
		cUnitPlugin = this;
	}
	
	 /** @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		if (cUnitPlugin == null)
			return null;
		IWorkbench workBench= cUnitPlugin.getWorkbench();
		if (workBench == null)
			return null;
		return workBench.getActiveWorkbenchWindow();
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow activeWorkbenchWindow= getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null)
			return null;
		return activeWorkbenchWindow.getActivePage();
	}
	
	public static TestRunnerViewPart showTestRunnerViewPartInActivePage() {
		try {
			// Have to force the creation of view part contents
			// otherwise the UI will not be updated
			IWorkbenchPage page= CUnitPlugin.getActivePage();
			if (page == null)
				return null;
			TestRunnerViewPart view= (TestRunnerViewPart) page.findView(TestRunnerViewPart.NAME);
			if (view == null) {
				//	create and show the result view if it isn't created yet.
				return (TestRunnerViewPart) page.showView(TestRunnerViewPart.NAME, null, IWorkbenchPage.VIEW_VISIBLE);
			} else {
				return view;
			}
		} catch (PartInitException pie) {
			//JUnitPlugin.log(pie);
			return null;
		}
	}
	
}
