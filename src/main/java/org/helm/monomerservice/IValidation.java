package org.helm.monomerservice;

import org.helm.notation2.Attachment;
import java.util.List;

public interface IValidation {

    int checkRule(Rule rule);

    int checkMonomer(LWMonomer monomer);

    boolean checkRGroup(String smiles, List<Attachment> attachment);
}
