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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.helm.notation2.Attachment;

public class LWMonomer {
	
	/**
	 * 'symbol' of LWMonomer = 'name' of Monomer
	 */

	private String symbol, polymertype, name, naturalanalog, molfile, canSmiles, monomertype, createdate, author;
	private int id;
	private List<Attachment> rgroups;
	
	
	/**
	 * Just for testing
	 * Creates a new LWMonomer with some random initialized attributes
	 */
	public LWMonomer() {
		this.symbol = System.currentTimeMillis() + "";
		this.polymertype = "RNA";
		this.name = "testIt";
		this.naturalanalog = "A";
		this.molfile = System.currentTimeMillis() + "";
		this.canSmiles = "[H:1]N1CC[C@H]1C([OH:2])=O";
		this.monomertype = "Undefined";
		this.author  = "UnknownAuthor";
		Date date = new Date();
		createdate = date.toString();
		rgroups = new ArrayList<Attachment>();
		rgroups.add(new Attachment("R3", "X"));
		
	}
	
	public LWMonomer(String author, String symbol, String polymerType, String name, String naturalAnalog, String molfile, String canSmiles, String monomerType, List<Attachment> rgroups){
		this.symbol = symbol;
		this.polymertype = polymerType;
		this.name = name;
		this.naturalanalog = naturalAnalog;
		this.molfile = molfile;
		this.canSmiles = canSmiles;
		this.monomertype = monomerType;
		this.createdate = new Date().toString();
		this.author = author;
		this.rgroups = rgroups;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public String getPolymerType() {
		return polymertype;
	}
	
	public String getName() {
		return name;
	}
	
	public String getNaturalAnalog() {
		return naturalanalog;
	}
	
	public String getMolfile() {
		return molfile;
	}
	
	public int getId() {
		return id;
	}
	
	public String getSmiles() {
		return canSmiles;
	}
	
	public String getMonomerType() {
		return monomertype;
	}
	
	public List<Attachment> getRgroups() {
		return rgroups;
	}
	
	public String getCreateDate() {
		return createdate;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public void setMonomerType(String monomerType) {
		this.monomertype = monomerType;
	}
	
	public void setSmiles(String smiles) {
		this.canSmiles = smiles;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	public void setPolymerType(String polymertype) {
		this.polymertype = polymertype;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setNaturalAnalog(String naturalanalog) {
		this.naturalanalog = naturalanalog;
	}
	
	public void setMolfile(String molfile) {
		this.molfile = molfile;
	}
	
	public void setCreateDate(String createdate) {
		this.createdate = createdate;
	}
	
	public void addAttachment(Attachment attachment) {
		rgroups.add(attachment);
	}
	
	public void setRgroups(List<Attachment> rgroups) {
		this.rgroups = rgroups;
	}
}
