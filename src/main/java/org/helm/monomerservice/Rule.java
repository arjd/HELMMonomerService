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

public class Rule {
	
	private Integer id;
	private String category, name, description, script, author, _id;
	
	/**
	 * Just for testing
	 * Creates a new rule with some random initialized attributes
	 */
	public Rule() {
		_id = null;
		id = 0;
		category = "atomatic";
		name = "TestIt";
		description = "automatically generated";
		script = "no script given";
		author = "Nobody";
	}
	
	public Rule(Integer id, String category, String name, String description, String script, String author) {
		this.id = id;
		this.category = category;
		this.name = name;
		this.description = description;
		this.script = script;
		this.author = author;
		this._id = null;
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getScript() {
		return script;
	}
	
	public Integer getId() {
		return id;
	}
	
	public String get_id() {
		return _id;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public void set_id(String _id) {
		this._id = _id;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setScript(String script) {
		this.script = script;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
}
