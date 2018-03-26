package org.helm.monomerservice;

import org.helm.notation2.Attachment;
import org.helm.notation2.tools.SMILES;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Validation implements IValidation {
    Logger Log;

    public Validation(Logger log) {
        Log = log;
    }


    @Override
    public int checkMonomer(LWMonomer monomer) {
        //chekc if Molfile exists
        if(monomer.getMolfile().isEmpty() || monomer.getMolfile() == null) {
            Log.info("Monomer has no Molfile");
            return -5100;
        }
        //check if SMILES exists
		/*if(monomer.getSmiles().isEmpty()) {
			LOG.info("Monomer has no SMILES");
			try {
				 String smiles = SMILES.convertMolToSMILESWithAtomMapping(monomer.getMolfile(), monomer.getRgroups());
				 monomer.setSmiles(smiles);
				 LOG.info("Generate SMILES form Molfile");
			} catch (Exception e) {
				LOG.info("SMILES could not be generated from Molfile");
				return -5200;
			}
		}*/

        //generate Smiles from Molfile
        try {
            String smiles = SMILES.convertMolToSMILESWithAtomMapping(monomer.getMolfile(), monomer.getRgroups());
            //smiles = Chemistry.getInstance().getManipulator().convertExtendedSmiles(smiles);
            smiles = smiles.replaceAll("\\s+", "");
            monomer.setSmiles(smiles);
            Log.info("Generate SMILES form Molfile: " + smiles);
        } catch (Exception e) {
            Log.info("SMILES could not be generated from Molfile");
            return -5200;
        }

        //check R-group
        if(!checkRGroup(monomer.getSmiles(), monomer.getRgroups())) {
            Log.info("Monomer has wrong R-Groups");
            return -5300;
        }

        //check if Molfile and SMILES match
		/*try {
			String smilesMolfile = SMILES.convertMolToSMILESWithAtomMapping(monomer.getMolfile(), monomer.getRgroups());
			String smilesMolfileUnique = Chemistry.getInstance().getManipulator().canonicalize(smilesMolfile);
			String smilesUnique = Chemistry.getInstance().getManipulator().canonicalize(monomer.getSmiles());
			if(!smilesMolfileUnique.equals(smilesUnique)) {
				LOG.info("Smiles and Molfile do not equal");
				LOG.info(SMILES.convertMolToSMILESWithAtomMapping(monomer.getMolfile(), monomer.getRgroups()));
				LOG.info(monomer.getSmiles());
				return -5400;
			}
		} catch (Exception e) {
			LOG.info("SMILES could not be generated from Molfile");
			return -5200;
		}*/

        if(monomer.getName().isEmpty() || monomer.getName() == null) {
            Log.info("Monomer has no Name");
            return -6000;
        }
        if(monomer.getPolymerType().isEmpty() || monomer.getPolymerType() == null) {
            Log.info("Monomer has no Polymertype");
            return -6100;
        }
        if(monomer.getSymbol().isEmpty() || monomer.getSymbol() == null) {
            Log.info("Monomer has no Symbol");
            return -6200;
        }
        if(monomer.getMonomerType().isEmpty() || monomer.getMonomerType() == null) {
            Log.info("Monomer has no Monomertype");
            return -6300;
        }
        if(monomer.getNaturalAnalog().isEmpty() || monomer.getNaturalAnalog() == null) {
            Log.info("Monomer has no Natural Analog");
            return -6400;
        }
        if(monomer.getCreateDate().isEmpty() || monomer.getCreateDate() == null) {
            Date date = new Date();
            monomer.setCreateDate(date.toString());
            Log.info("Monomer has no CreateDate: current date is used");
        }
        if(monomer.getAuthor().isEmpty() || monomer.getAuthor() == null) {
            Log.info("Monomer has no Author: Author is set to 'unknownAuthor'");
            monomer.setAuthor("unknownAuthor");
        }
        return 0;
    }

    @Override
    public boolean checkRGroup(String smiles, List<Attachment> attachment) {
        int numberGivenAttachment = attachment.size();
        int numberAttachmentInSmiles = 0;

        Pattern pattern = Pattern.compile("\\[\\*:([1-9]\\d*)\\]|\\[\\w+:([1-9]\\d*)\\]");
        Matcher matcher = pattern.matcher(smiles);

        while(matcher.find()) {
            numberAttachmentInSmiles++;
        }

        if(numberAttachmentInSmiles == numberGivenAttachment)
            return true;
        else
            return false;
    }

    @Override
    public int checkRule(Rule rule) {
        if(rule.getScript() == null || rule.getScript().isEmpty()) {
            Log.info("Rule has no Script");
            return -1000;
        }

        if(rule.getScript() == null || rule.getCategory().isEmpty()) {
            Log.info("Rule has no category");
            return -2000;
        }

        if(rule.getId() == null || rule.getId().toString().isEmpty()) {
            Log.info("Rule has no ID");
            return -3000;
        }

        if(rule.getAuthor() == null || rule.getAuthor().isEmpty()) {
            rule.setAuthor("unknownAuthor");
            Log.info("Rule has no Author; Author is set to 'unkownAuthor'");
        }

        if(rule.getDescription() == null || rule.getDescription().isEmpty()) {
            rule.setDescription("no description");
            Log.info("Rule has no description; Description is set to 'no description'");
        }

        return 0;
    }
}
