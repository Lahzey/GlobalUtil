package com.creditsuisse.graphics.swing;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;


/**
 * A JTextField that only allows integer input.
 * @author A469627
 *
 */
public class NumericTextField extends JTextField{
	
	public boolean allowFloats = false;
	

	/**
     * Constructs a new <code>TextField</code>.  A default model is created,
     * the initial string is <code>null</code>,
     * and the number of columns is set to 0.
     */
	public NumericTextField() {
		super();
		init();
	}

	/**
     * Constructs a new <code>JTextField</code> that uses the given text
     * storage model and the given number of columns.
     * This is the constructor through which the other constructors feed.
     * If the document is <code>null</code>, a default model is created.
     *
     * @param doc  the text storage to use; if this is <code>null</code>,
     *          a default will be provided by calling the
     *          <code>createDefaultModel</code> method
     * @param text  the initial string to display, or <code>null</code>
     * @param columns  the number of columns to use to calculate
     *   the preferred width &gt;= 0; if <code>columns</code>
     *   is set to zero, the preferred width will be whatever
     *   naturally results from the component implementation
     * @exception IllegalArgumentException if <code>columns</code> &lt; 0
     */
	public NumericTextField(Document doc, String text, int columns) {
		super(doc, text, columns);
		init();
	}

	/**
     * Constructs a new empty <code>TextField</code> with the specified
     * number of columns.
     * A default model is created and the initial string is set to
     * <code>null</code>.
     *
     * @param columns  the number of columns to use to calculate
     *   the preferred width; if columns is set to zero, the
     *   preferred width will be whatever naturally results from
     *   the component implementation
     */
	public NumericTextField(int columns) {
		super(columns);
		init();
	}

	/**
     * Constructs a new <code>TextField</code> initialized with the
     * specified text and columns.  A default model is created.
     *
     * @param text the text to be displayed, or <code>null</code>
     * @param columns  the number of columns to use to calculate
     *   the preferred width; if columns is set to zero, the
     *   preferred width will be whatever naturally results from
     *   the component implementation
     */
	public NumericTextField(String text, int columns) {
		super(text, columns);
		init();
	}
	
	/**
     * Constructs a new <code>TextField</code> initialized with the
     * specified text. A default model is created and the number of
     * columns is 0.
     *
     * @param text the text to be displayed, or <code>null</code>
     */
	public NumericTextField(String text) {
		super(text);
		init();
	}
	
	public String getTextOrZero() {
		String text = getText();
		if (text.isEmpty()) return "0";
		else return text;
	}

	/**
	 * @return The input in this field as integer.
	 * <br/>Equals <code>Integer.parseInt(getText())</code>
	 */
	public int getInput(){
		return Integer.parseInt(getTextOrZero());
	}
	
	public double getFloatInput(){
		return Float.parseFloat(getTextOrZero());
	}
	
	private void init(){
		((PlainDocument) getDocument()).setDocumentFilter(new MyIntFilter());
	}

	
	/**
	 * @author https://stackoverflow.com/a/11093360/5784265
	 *
	 */
	private class MyIntFilter extends DocumentFilter {
		   @Override
		   public void insertString(FilterBypass fb, int offset, String string,
		         AttributeSet attr) throws BadLocationException {

		      Document doc = fb.getDocument();
		      StringBuilder sb = new StringBuilder();
		      sb.append(doc.getText(0, doc.getLength()));
		      sb.insert(offset, string);

		      if (test(sb.toString())) {
		         super.insertString(fb, offset, string, attr);
		      } else {
		         // warn the user and don't allow the insert
		      }
		   }

		   private boolean test(String text) {
			   if (text.isEmpty()) return true; // allow for empty inputs which will be treated as 0 by getInput
			   
		      try {
		    	  if(allowFloats){
		    		  Float.parseFloat(text);
		    	  }else{
		    		  Integer.parseInt(text);
		    	  }
		    	  return true;
		      } catch (NumberFormatException e) {
		         return false;
		      }
		   }

		   @Override
		   public void replace(FilterBypass fb, int offset, int length, String text,
		         AttributeSet attrs) throws BadLocationException {

		      Document doc = fb.getDocument();
		      StringBuilder sb = new StringBuilder();
		      sb.append(doc.getText(0, doc.getLength()));
		      sb.replace(offset, offset + length, text);

		      if (test(sb.toString())) {
		         super.replace(fb, offset, length, text, attrs);
		      } else {
		         // warn the user and don't allow the insert
		      }

		   }

		   @Override
		   public void remove(FilterBypass fb, int offset, int length)
		         throws BadLocationException {
		      Document doc = fb.getDocument();
		      StringBuilder sb = new StringBuilder();
		      sb.append(doc.getText(0, doc.getLength()));
		      sb.delete(offset, offset + length);

		      if (test(sb.toString())) {
		         super.remove(fb, offset, length);
		      } else {
		         // warn the user and don't allow the insert
		      }

		   }
		}
}

