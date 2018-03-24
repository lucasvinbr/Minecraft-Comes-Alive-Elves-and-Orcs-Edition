/*******************************************************************************
 * SkinLoader.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import mca.enums.EnumProfessionSkinGroup;
import mca.enums.EnumRace;
import radixcore.core.RadixCore;
import org.apache.logging.log4j.LogManager;

/**
 * Handles loading of MCA's skins.
 */
public final class SkinLoader {
	public static void loadSkins() {
		try {
			final File modFile = findModDataFile();

			if (modFile.isFile()) {
				loadSkinsFromFile(modFile);
			} else {
				LogManager.getLogger(SkinLoader.class).fatal(new FileNotFoundException("Unable to locate MCA assets!"));
			}
		} catch (final IOException e) {
			LogManager.getLogger(SkinLoader.class).fatal(e);
		} catch (final NullPointerException e) {
			LogManager.getLogger(SkinLoader.class).fatal(e);
		}
	}

	private static File findModDataFile() throws ZipException, IOException {
		File modData = findModAsArchive();

		if (modData == null) {
			LogManager.getLogger(SkinLoader.class).fatal(new FileNotFoundException(
					"Unable to locate MCA assets! This may be due to an issue with your launcher (if made by a third party), or your MCA installation. Try reinstalling the mod, or try a different launcher."));
		}

		return modData;
	}

	private static File findModAsArchive() throws ZipException, IOException {
		final File modsFolder = new File(RadixCore.getRunningDirectory() + "/mods");
		for (final File fileInMods : modsFolder.listFiles()) {
			if (fileInMods.isFile() && fileInMods.getName().contains(".zip") || fileInMods.getName().contains(".jar")) {
				if (fileContainsModData(fileInMods)) {
					return fileInMods;
				}
			}
			else if (fileInMods.isDirectory()) {
				final File modData = getModFileFromNestedFolder(fileInMods);

				if (modData != null) {
					return modData;
				}
			}
		}

		return null;
	}

	private static void loadSkinsFromFile(File modDataFile) throws ZipException, IOException {
		final ZipFile modArchive = new ZipFile(modDataFile);
		final Enumeration<?> enumerator = modArchive.entries();
		int counter = 0;

		while (enumerator.hasMoreElements()) {
			// Loop through each entry within the JAR until the MCA folder is hit.
			final ZipEntry file = (ZipEntry) enumerator.nextElement();
			String archiveFilePath = "/" + file.getName();

			if (archiveFilePath.contains("textures/skins") && !archiveFilePath.contains("/sleeping/")) {
				if(file.getName().toLowerCase().contains("orc")) {
					//Just load up the orc skins for any profession.
					for (EnumProfessionSkinGroup skinGroup : EnumProfessionSkinGroup.values()) {
						skinGroup.addSkin(archiveFilePath, EnumRace.Orc);
						counter++;
					}
				} else if(file.getName().toLowerCase().contains("elf")) {
					//Just load up the elf skins for any profession.
					for (EnumProfessionSkinGroup skinGroup : EnumProfessionSkinGroup.values()) {
						skinGroup.addSkin(archiveFilePath, EnumRace.Elf);
						counter++;
					}
				} else {
					for (EnumProfessionSkinGroup skinGroup : EnumProfessionSkinGroup.values()) {
						if (file.getName().toLowerCase().contains(skinGroup.toString().toLowerCase())) {
							skinGroup.addSkin(archiveFilePath, EnumRace.Villager);
							counter++;
						}
					}
				}
			}
		}

		modArchive.close();
		LogManager.getLogger(SkinLoader.class).info("MCA has successfully loaded " + counter + " skins.");
	}

	private static File getModFileFromNestedFolder(File nestedFolder) throws IOException {
		final File[] nestedFiles = nestedFolder.listFiles();
		if(nestedFiles != null) {
			for (final File file : nestedFiles) {
				if (file.isDirectory()) {
					getModFileFromNestedFolder(file);
				}
				else {
					if (fileContainsModData(file)) {
						return file;
					}
				}
			}
		}

		return null;
	}

	private static boolean fileContainsModData(File fileToTest) throws IOException {
		if (fileToTest.getName().contains(".zip") || fileToTest.getName().contains(".jar")) {
			try {
				final ZipFile archive = new ZipFile(fileToTest);
				final Enumeration<?> enumerator = archive.entries();
				ZipEntry entry;

				while (enumerator.hasMoreElements()) {
					entry = (ZipEntry) enumerator.nextElement();

					// Test for random files unique to MCA.
					if (entry.getName().contains("mca/core/MCA.class")
							|| entry.getName().contains("sleeping/ee1.png")) {
						archive.close();
						return true;
					}
				}

				archive.close();
			}
			catch (final ZipException e) {
				Logger.getGlobal().log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}

		return false;
	}
}
