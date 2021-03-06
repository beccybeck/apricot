package ee.ttu.pld.apricot.verifier;

import org.junit.Test;
import ui.ConverterSettings;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anton Chepurov
 */
public class DesignTest {

	@Test
	public void initNewDesignFile() throws ConverterSettings.ConverterSettingsParseException {
		File hlddFile = new File("test/designs/ITC99/orig/b00/b00_M_FU.agm");
		File newDesignFile = new File("test/designs/ITC99/orig/b00/verif/b00_M_FU.agm");
		Design design = new Design(hlddFile, ConverterSettings.parse(hlddFile.getAbsolutePath()));

		assertEquals("Design(): should create newDesignFile in \'" + Design.NEW_DESIGN_DIR + "\' folder.",
				newDesignFile, design.getNewDesignFile());
	}

	@Test
	public void createNewDesignDir() throws IOException, ConverterSettings.ConverterSettingsParseException {
		File hlddFile = new File("test/designs/ITC99/orig/b00/b00_M_FU.agm");
		Design design = new Design(hlddFile, ConverterSettings.parse(hlddFile.getAbsolutePath()));

		design.createNewDesignDir();

		File newDesignDir = design.getNewDesignFile().getParentFile();
		newDesignDir.deleteOnExit();

		assertTrue("Design.createNewDesignDir(): should create new directory \'" + Design.NEW_DESIGN_DIR + "\'.",
				newDesignDir.exists() || newDesignDir.isDirectory());
	}

	@Test
	public void initNewMapFile() throws ConverterSettings.ConverterSettingsParseException {
		File hlddFile = new File("test/designs/ITC99/orig/b00/b00_M_FU.agm");
		File newMapFile = new File("test/designs/ITC99/orig/b00/verif/b00_M_FU.map");
		Design design = new Design(hlddFile, ConverterSettings.parse(hlddFile.getAbsolutePath()));

		assertEquals("Design(): should create newMapFile in \'" + Design.NEW_DESIGN_DIR + "\' folder.",
				newMapFile, design.getNewMapFile());
	}
}
