package ui;

import io.ConsoleWriter;
import parsers.tgm.ModelDataLoader;
import ui.graphics.SimulationFrame;
import ui.io.AssertionCheckReader;
import ui.utils.*;
import ui.utils.uiWithWorker.UIWithWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anton Chepurov
 */
public class BusinessLogicAssertionChecker implements Lockable {

	private final ApplicationForm applicationForm;
	private final ConsoleWriter consoleWriter;

	/* Private fields */
	private File hlddFile = null;
	private File tgmFile;
	private File simulationFile = null;
	volatile private AssertionCheckReader simulationReader = null;

	private final SimpleLock simpleLock = new SimpleLock();

	public BusinessLogicAssertionChecker(ApplicationForm applicationForm, ConsoleWriter consoleWriter) {
		this.applicationForm = applicationForm;
		this.consoleWriter = consoleWriter;
	}

	public File getSimulationFile() {
		return simulationFile;
	}

	public void setSimulationFile(File simulationFile) {
		this.simulationFile = simulationFile;
	}

	public void loadChkFile() {
		/* Reset simulationReader so that Draw button and SIM/CHK file loader
		won't take the old one while the new is being calculated*/
		simulationReader = null;
		/* Read simulation file */
		UIWithWorker.runUIWithWorker(
				new AssertionLoadingUI(applicationForm.getFrame()),
				new AssertionLoadingWorker(simulationFile, this)
		);
	}

	public boolean isChkFileLoaded() {
		return simulationReader != null;
	}

	public void processDraw() {
		UIWithWorker.runUIWithWorker(
				new AssertionLoadingWaiterUI(applicationForm.getFrame(), false),
				new AssertionLoadingWaiter(this));
		/* drawWaveform() is invoked in AssertionLoadingWaiter after it has completed its work */
	}

	public void drawWaveform() throws ExtendedException {
		/* Check .CHK file to be selected */
		if (simulationFile == null) {
			throw new ExtendedException(".CHK file is missing", ExtendedException.MISSING_FILE_TEXT);
		}

		int drawPatternCount = applicationForm.getDrawPatternCount();

		SimulationFrame simulationFrame = new SimulationFrame(simulationReader.getVariablePatterns(),
				simulationReader.getAssertionPatterns(),
				new ModelDataLoader(simulationFile).getVariableNames(),
				simulationReader.getBooleanIndices(), drawPatternCount, simulationFile.getName(), applicationForm.getFrame());
		applicationForm.addSimulation(simulationFile.getName(), simulationFile.getAbsolutePath(), simulationFrame.getMainPanel());

		unlock();
	}

	public boolean isLocked() {
		return simpleLock.isLocked();
	}

	public void lock() {
		simpleLock.lock();
	}

	public void unlock() {
		simpleLock.unlock();
	}

	public String getProposedFileName() {
		return hlddFile != null ? hlddFile.getAbsolutePath() : null;
	}

	public void setHlddFile(File hlddFile) {
		this.hlddFile = hlddFile;
	}

	public void setTgmFile(File tgmFile) {
		this.tgmFile = tgmFile;
	}

	public void processCheck() throws ExtendedException {
		boolean isRandom = applicationForm.isRandomAssert();
		int patternCount = applicationForm.getPatternCountForAssert();
		boolean doCheckAssertion = applicationForm.isDoCheckAssertion();

		/* Check design and assertions to be selected */
		if (hlddFile == null) {
			throw new ExtendedException("HLDD model file is missing", ExtendedException.MISSING_FILE_TEXT);
		}
		if (tgmFile == null && doCheckAssertion) {
			throw new ExtendedException("TGM file is missing", ExtendedException.MISSING_FILE_TEXT);
		}

		/* Collect execution string */
		List<String> commandList = new ArrayList<String>(5);
		commandList.add(ApplicationForm.LIB_DIR + "assert");
		if (isRandom) {
			commandList.add("-random");
			commandList.add("" + patternCount);
		}
		if (doCheckAssertion) {
			commandList.add("-check");
		}
		commandList.add(hlddFile.getAbsolutePath().replace(".agm", ""));

		/* Execute command */
		UIWithWorker.runUIWithWorker(
				new AssertionCheckingUI(applicationForm.getFrame()),
				new AssertionCheckingWorker(
						commandList,
						System.err,
						this,
						hlddFile.getAbsolutePath().replace(".agm", ".chk"),
						consoleWriter
				)
		);
	}

	public ApplicationForm getApplicationForm() {
		return applicationForm;
	}

	public void setSimulationReader(AssertionCheckReader simulationReader) {
		this.simulationReader = simulationReader;
	}

}