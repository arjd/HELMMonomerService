/*******************************************************************************
 * Copyright C 2017, The Pistoia Alliance
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/

package org.helm.monomerservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and reads the config-File
 *
 */

public final class LibraryManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(LibraryManager.class);

	private static final String CONFIG_FILE_PATH = LibraryManager.class.getResource("resources/config.txt").toString();

	private static final String RULES = "rules";

	private static final String MONOMERS = "monomers";

	@SuppressWarnings("unused")
	private static String rules;

	@SuppressWarnings("unused")
	private static String monomers;

	private IRuleLibrary rulesLibrary;

	private IMonomerLibrary monomerLibrary;

	private static LibraryManager _instance;

	private LibraryManager() throws Exception {
		refresh();
		readConfigFile();
	}

	private void readConfigFile() throws Exception {
		File configFile = new File(CONFIG_FILE_PATH);
		if (!configFile.exists()) {
			setDefaultSetting();
			LOG.info("Could not find config-file");	
		}

		try {
			PropertiesConfiguration conf = new PropertiesConfiguration(CONFIG_FILE_PATH);
			monomers = conf.getString(MONOMERS);
			rules = conf.getString(RULES);
		} catch (ConfigurationException e) {
			setDefaultSetting();
		}

		// set IConfigLoaderMonomerLibrary
		Class<?> clazz;
		try {
			clazz = Class.forName("org.helm.monomerservice.MonomerLibrarySQLite");
			monomerLibrary = (IMonomerLibrary) clazz.getConstructor().newInstance();
		} catch (Exception e) {
			throw e;
		}
		// set IConfigLoaderRulesLibrary
		try {
			clazz = Class.forName("org.helm.monomerservice.RuleLibrarySQLite");
			rulesLibrary = (IRuleLibrary) clazz.getConstructor().newInstance();
		} catch (Exception e) {
			throw e;
		}
	}

	private void setDefaultSetting() {
		rules = "org.helm.monomerservice.RuleLibrarySQLite";
		monomers = "org.helm.monomerserevise.MonomerLibrarySQLite";
		LOG.info("Default settings used");
	}

	public static LibraryManager getInstance() throws Exception {
		if (_instance == null) {
			_instance = new LibraryManager();
		}
		return _instance;
	}

	public IMonomerLibrary getMonomerLibrary() {
		return monomerLibrary;
	}

	public IRuleLibrary getRulesLibrary() {
		return rulesLibrary;
	}

	public void refresh() throws Exception {
		File configFile = new File(CONFIG_FILE_PATH);
		if (!configFile.exists()) {
			LOG.info("Could not find config-file");
			BufferedWriter writer = null;
			try {
				configFile.createNewFile();

				writer = new BufferedWriter(new FileWriter(configFile));

				writer.write("#LibrariesLoader");
				writer.write("loader.rules=org.helm.monomerservice.RuleLibrarySQLite");
				writer.write("loader.monomers=org.helm.monomerservice.MonomerLibrarySQLite");

			} catch (Exception e) {
				setDefaultSetting();
				LOG.info("Could not create config-file");
			} finally {
				try {
					if (writer != null) {
						writer.close();
					}
				} catch (IOException e) {
					throw e;
				}
			}
		}
	}
}
