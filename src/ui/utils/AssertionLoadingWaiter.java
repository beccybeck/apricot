package ui.utils;

import ui.BusinessLogicAssertionChecker;
import ui.ExtendedException;
import ui.utils.uiWithWorker.TaskSwingWorker;

/**
 * @author Anton Chepurov
 */
public class AssertionLoadingWaiter extends TaskSwingWorker {

	public AssertionLoadingWaiter(final BusinessLogicAssertionChecker businessLogicAC) {
		executableRunnable = new Runnable() {
			public void run() {
				/* Wait until simulationReader is initiated by AssertionLoadingWorker */
				while (!Thread.interrupted()) {
					try {
						if (businessLogicAC.isChkFileLoaded()) {
							/* Wait until CHK file is loaded */
							break;
						}
						if (businessLogicAC.getSimulationFile() == null) {
							/* If loading of CHK file is cancelled, stop waiting and terminate the thread*/
							Thread.currentThread().interrupt();
						}
						Thread.sleep(200);
					} catch (InterruptedException e) {
						return;
					}
				}

				try {
					uiHolder.setVisible(true);
					businessLogicAC.drawWaveform();
				} catch (ExtendedException e) {
					occurredException = e;
				}

			}
		};
	}

	@Override
	public void stopWorker() {
		Thread.currentThread().interrupt();
		super.stopWorker();
	}
}
