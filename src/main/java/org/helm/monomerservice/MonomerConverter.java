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

import org.helm.notation2.Monomer;

/**
 * Can convert LWMonomer to monomer and the other way around
 */

public class MonomerConverter {

	public static Monomer convertLWMonomerToMonomer(LWMonomer lwMonomer) {
		
		Monomer monomer = new Monomer(lwMonomer.getPolymerType(), lwMonomer.getMonomerType(), lwMonomer.getNaturalAnalog(), lwMonomer.getSymbol());
		monomer.setId(lwMonomer.getId());
		monomer.setMolfile(lwMonomer.getMolfile());
		monomer.setName(lwMonomer.getName());
		monomer.setCanSMILES(lwMonomer.getSmiles());
		
		monomer.setAttachmentList(lwMonomer.getRgroups());
		
		return monomer;
	}

	
	/**
	 * 'Symbol' at LWMonomer = 'name' at Monomer
	 */
	
	
	public static LWMonomer convertMonomerToLWMonomer(Monomer monomer) {

		LWMonomer lwMonomer = new LWMonomer("unknownAuthor" , monomer.getAlternateId(), monomer.getPolymerType(), monomer.getName(),
				monomer.getNaturalAnalog(), monomer.getMolfile(),
				monomer.getCanSMILES(), monomer.getMonomerType(), monomer.getAttachmentList());

		lwMonomer.setId(monomer.getId());

		return lwMonomer;
	}
}
