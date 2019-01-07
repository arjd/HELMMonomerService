package org.helm.monomerservice;

import org.helm.notation2.Attachment;
import org.helm.notation2.tools.MonomerParser;
import org.helm.notation2.tools.SMILES;
import org.slf4j.Logger;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validation implements IValidation {
	Logger Log;

	public Validation() {
	}

	public Validation(Logger log) {
		Log = log;
	}

	@Override
	public int checkMonomer(LWMonomer monomer) {
		// chekc if Molfile exists
		if (monomer.getMolfile().isEmpty() || monomer.getMolfile() == null) {
			Log.info("Monomer has no Molfile");
			return -5100;
		}

		// generate Smiles from Molfile
		try {
			String smiles = SMILES.convertMolToSMILESWithAtomMapping(monomer.getMolfile(), monomer.getRgroups());

			smiles = smiles.replaceAll("\\s+", "");
			monomer.setSmiles(smiles);
			Log.info("Generate SMILES from Molfile: " + smiles);
		} catch (Exception e) {
			Log.info("SMILES could not be generated from Molfile because of " + e.getMessage(), e);
			return -5200;
		}

		// check if Smiles connected
		try {
			if (!SMILES.isConnected(monomer.getMolfile())) {
				Log.info("Monomer is not connected");
				return -5500;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// check R-group
		if (checkRGroup(monomer.getSmiles(), monomer.getRgroups()) != 0) {
			return checkRGroup(monomer.getSmiles(), monomer.getRgroups());
		}

		// Check Name
		if (monomer.getName().isEmpty() || monomer.getName() == null) {
			Log.info("Monomer has no Name");
			return -6000;
		}
		// Check PolymerType
		if (monomer.getPolymerType().isEmpty() || monomer.getPolymerType() == null) {
			Log.info("Monomer has no Polymertype");
			return -6100;
		}
		// Check Symbol
		if (monomer.getSymbol().isEmpty() || monomer.getSymbol() == null) {
			Log.info("Monomer has no Symbol");
			return -6200;
		}
		// Check Monomertype
		if (monomer.getMonomerType().isEmpty() || monomer.getMonomerType() == null) {
			Log.info("Monomer has no Monomertype");
			return -6300;
		}
		// Check Create Date
		if (monomer.getCreateDate().isEmpty() || monomer.getCreateDate() == null) {
			Date date = new Date();
			monomer.setCreateDate(date.toString());
			Log.info("Monomer has no CreateDate: current date is used");
		}
		// Check Author
		if (monomer.getAuthor().isEmpty() || monomer.getAuthor() == null) {
			Log.info("Monomer has no Author: Author is set to 'unknownAuthor'");
			monomer.setAuthor("unknownAuthor");
		}

		// Check if Monomertype is correct
		if (!(monomer.getMonomerType().equals("Branch") || monomer.getMonomerType().equals("Backbone")
				|| monomer.getMonomerType().equals("Undefined"))) {
			Log.info("Monomer has wrong Monomertype (must be 'Branch', 'Backbone' or 'Undefined')");
			return -6600;
		}
		// Check if Polymertype is correct
		if (!(monomer.getPolymerType().equals("PEPTIDE") || monomer.getPolymerType().equals("RNA")
				|| monomer.getPolymerType().equals("CHEM"))) {
			Log.info("Monomer has wrong Polymertype (must be 'PEPTIDE', 'RNA' or 'CHEM')");
			return -6700;
		}

		// Check if Monomertype and Polymertype go together
		if (monomer.getMonomerType().equals("Undefined")) {
			if (!monomer.getPolymerType().equals("CHEM")) {
				Log.info("Monomertype and Polymertype do not fit; Monomertype = Undefined");
				return -6500;
			}
		}
		if (monomer.getMonomerType().equals("Backbone")) {
			if (!monomer.getPolymerType().equals("PEPTIDE"))
				if (!monomer.getPolymerType().equals("RNA")) {
					Log.info("Monomertype and Polymertype do not fit; Monomertype = Backbone");
					return -6500;
				}
		}
		if (monomer.getMonomerType().equals("Branch")) {
			if (!monomer.getPolymerType().equals("PEPTIDE"))
				if (!monomer.getPolymerType().equals("RNA")) {
					Log.info("Monomertype and Polymertype do not fit; Monomertype = Branch");
					return -6500;
				}
		}

		// Check Natural Analog
		if (monomer.getPolymerType().equals("CHEM") && !monomer.getNaturalAnalog().equals("-")) {
			Log.info("Monomer has wrong Natural Analog; must be '-' if Monomer is CHEM");
			return -6800;
		}
		if (monomer.getNaturalAnalog().isEmpty() || monomer.getNaturalAnalog().equals("-")) {
			if (!monomer.getPolymerType().equals("CHEM")) {
				Log.info("Monomer has no Natural Analog");
				return -6400;
			}
		}

		MonomerParser paser = new MonomerParser();
		MonomerConverter converter = new MonomerConverter();
		boolean validationToolkit = false;
		try {
			validationToolkit = paser.validateMonomer(converter.convertLWMonomerToMonomer(monomer));
		} catch (Exception e) {
			Log.debug("Toolkit does not validate Monomer because of " + e.getMessage(), e);
		}
		if (validationToolkit) {
			Log.info("Toolkit validates Monomer");
			return 0;
		} else {
			Log.info("Toolkit does not validate Monomer");
			return 1;
		}
	}

	@Override
	public int checkRGroup(String smiles, List<Attachment> attachment) {
		if (attachment.isEmpty()) {
			Log.info("Has no RGroups");
			return -9000;
		}

		int numberGivenAttachment = attachment.size();
		int numberAttachmentInSmiles = 0;

		// search for R-Groups in Smiles
		Pattern pattern = Pattern.compile("\\[\\*:([1-9]\\d*)\\]|\\[\\w+:([1-9]\\d*)\\]");
		Matcher matcher = pattern.matcher(smiles);
		int[][] array = new int[2][numberGivenAttachment + 2];
		String[] labels = new String[numberGivenAttachment + 2];
		while (matcher.find()) {
			array[0][numberAttachmentInSmiles] = matcher.start();
			array[1][numberAttachmentInSmiles] = matcher.end();
			numberAttachmentInSmiles++;
		}

		// check if Numbers start by 1
		for (int i = 0; i < numberGivenAttachment; i++) {
			int num = Integer.valueOf(attachment.get(i).getLabel().substring(1, 2));
			if (num > numberGivenAttachment) {
				Log.info("Number of RGroups does not start by one or does not raise correctly");
				return -9100;
			}
		}

		// get Labels from Attachmentlist
		for (int i = 0; i < numberAttachmentInSmiles; i++) {
			int num = Integer.valueOf(attachment.get(i).getLabel().substring(1, 2));

			labels[num - 1] = attachment.get(i).getCapGroupName();
		}

		// check if number of R-Groups is equal
		if (numberAttachmentInSmiles != numberGivenAttachment) {
			Log.info("Number of RGroups in Attachmentlist is NOT equal to number of R-Groups in Smiles");
			return -9200;
		}

		for (int i = 0; i < numberAttachmentInSmiles; i++) {
			// get number of R-Group from smiles
			String rGroup = smiles.substring(array[0][i], array[1][i]);
			matcher = Pattern.compile(":\\d+").matcher(rGroup);
			matcher.find();
			int rGroupNumber = Integer.parseInt(rGroup.substring(matcher.start() + 1, matcher.end()));

			// get Label of R-Group from smiles
			matcher = Pattern.compile("\\w+").matcher(rGroup);
			matcher.find();
			String rGroupNameString = rGroup.substring(matcher.start(), matcher.end());

			// Check if Labels are equal
			if (!rGroupNameString.equals(labels[rGroupNumber - 1])) {
				Log.info("Labels of RGroups do not match");
				return -9300;
			}

		}

		return 0;
	}

	@Override
	public int checkRule(Rule rule) {
		if (rule.getScript() == null || rule.getScript().isEmpty()) {
			Log.info("Rule has no Script");
			return -1000;
		}

		if (rule.getScript() == null || rule.getCategory().isEmpty()) {
			Log.info("Rule has no category");
			return -2000;
		}

		if (rule.getId() == null || rule.getId().toString().isEmpty()) {
			Log.info("Rule has no ID");
			return -3000;
		}

		if (rule.getAuthor() == null || rule.getAuthor().isEmpty()) {
			rule.setAuthor("unknownAuthor");
			Log.info("Rule has no Author; Author is set to 'unkownAuthor'");
		}

		if (rule.getDescription() == null || rule.getDescription().isEmpty()) {
			rule.setDescription("no description");
			Log.info("Rule has no description; Description is set to 'no description'");
		}

		return 0;
	}
}
