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

import java.util.List;

public interface IMonomerLibrary {

	/**
	   * method to delete monomer selected by polymerType and symbol
	   * 
	   * @param polymerType
	   * 			Type of polymer which should be deleted
	   * 
	   * @param symbol
	   * 			Symbol of monomer which should be deleted
	   */
	public int deleteMonomer(String polymerType, String symbol) throws Exception;
	
	
	/**
	   * method to show a list of monomers
	   * 
	   * @param polymerType
	   * 			Type of polmyer: RNA, PEPTIDE, CHEM, ALL
	   * @param monomerType
	   * 			Type of monomer: Backbone, Branch
	   * @param filter
	   * 			Filter criteria. Performs a "contains" search
	   * @param filterField
	   * 			Field to search: name, symbol. If empty, search in both fields.
	   * @param offset
	   * 			Offset of results for pagination.
	   * @param limit
	   * 			Limit of results for pagination.
	   */
	public List<LWMonomer>  showMonomerList(String polymerType, String monomerType, String filter, String filterField, int offset, int limit) throws Exception;
	
	
	/**
	   * method to show all monomers
	   */
	public List<LWMonomer> showAllMonomers() throws Exception;
	
	
	/**
	   * method to show details of one monomer selected by polymerType and symbol
	   * 
	   * @param polymerType
	   * 			Type of monomer which details should be shown
	   * 
	   * @param symbol
	   * 			Symbol of monomer which details should be shown
	   */
	public LWMonomer monomerDetail(String polymerType, String symbol) throws Exception;
	
	
	
	/**
	   *method to insert one monomer 
	   *or 
	   *methed to update one monomer selected by polymerType and symbol
	   * 
	   * @param polymerType
	   * 			Type of monomer which should be updated or inserted
	   * 
	   * @param symbol
	   * 			Symbol of monomer which should be updated or inserted
	   * 
	   * @param monomer
	   * 			monomer which should be inserted
	   */
	public LWMonomer insertOrUpdateMonomer(String polymerType, String symbol, LWMonomer monomer) throws Exception;
	
	
	/**
	   *method to insert one monomer 
	   *
	   * @param polymerType
	   * 			Type of monomer which should be inserted
	   * 
	   * @param symbol
	   * 			Symbol of monomer which should be inserted
	   * 
	   * @param monomer
	   * 			monomer which should be inserted
	   */
	public int insertMonomer( LWMonomer monomer) throws Exception;
	
	
	/**
	   *method to update one monomer selected by polymerType and symbol
	   * 
	   * @param polymerType
	   * 			Type of monomer which should be updated
	   * 
	   * @param symbol
	   * 			Symbol of monomer which should be updated
	   * 
	   * @param monomer
	   * 			monomer which should be updated
	   */
	public LWMonomer updateMonomer(String polymerType, String symbol, LWMonomer monomer) throws Exception;
	
	
	/**
	   *total count of monomers after calling show showMonomerList
	   * 
	   */
	public int getTotalCount() throws Exception;
}
