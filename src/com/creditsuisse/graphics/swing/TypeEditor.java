package com.creditsuisse.graphics.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands;
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import com.creditsuisse.graphics.ImageUtil;
import com.creditsuisse.graphics.swing.ColorChooserButton.ColorChangedListener;
import com.creditsuisse.graphics.swing.SelectionPanel.SelectionDialog;
import com.creditsuisse.util.ColorUtil;

import net.miginfocom.swing.MigLayout;

public abstract class TypeEditor<T> extends JPanel implements FocusListener {
	
	private List<ActionListener> submitListeners = new ArrayList<ActionListener>();

	public abstract boolean isInputValid();
	public abstract T getInput();
	public abstract void setInput(T input);
	public abstract void setEditable(boolean editable);
	
	@SuppressWarnings("unchecked")
	public boolean trySetInput(Object input){
		try{
			setInput((T) input);
			return true;
		}catch(Throwable e){
			return false;
		}
	}
	
	protected void onSubmit(ActionEvent e){
		for(ActionListener listener : submitListeners) listener.actionPerformed(e);
	}
	
	public void addSubmitListener(ActionListener listener){
		submitListeners.add(listener);
	}
	
	public void removeSubmitListener(ActionListener listener){
		submitListeners.remove(listener);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> TypeEditor<T> getEditor(Class<T> forType){
		if(forType.isAssignableFrom(Integer.class) || forType.isAssignableFrom(int.class)){
			// Integer
			return (TypeEditor<T>) new NumberEditor.IntegerEditor();
		}else if(forType.isAssignableFrom(Long.class) || forType.isAssignableFrom(long.class)){
			// Long
			return (TypeEditor<T>) new NumberEditor.LongEditor();
		}else if(forType.isAssignableFrom(Short.class) || forType.isAssignableFrom(short.class)){
			// Short
			return (TypeEditor<T>) new NumberEditor.ShortEditor();
		}else if(forType.isAssignableFrom(Float.class) || forType.isAssignableFrom(float.class)){
			// Float
			return (TypeEditor<T>) new NumberEditor.FloatEditor();
		}else if(forType.isAssignableFrom(Double.class) || forType.isAssignableFrom(double.class)){
			// Double
			return (TypeEditor<T>) new NumberEditor.DoubleEditor();
		}else if(forType.isAssignableFrom(Boolean.class) || forType.isAssignableFrom(boolean.class)){
			// Boolean
			return (TypeEditor<T>) new BooleanEditor();
		}else if(forType.isAssignableFrom(String.class)){
			// String (or any CharSequence)
			return (TypeEditor<T>) new StringEditor();
		}else if(forType.isAssignableFrom(Color.class)){
			// Color
			return (TypeEditor<T>) new ColorEditor();
		}else if(forType.isAssignableFrom(Dimension.class)){
			// Dimension
			return (TypeEditor<T>) new DimensionEditor();
		}else if(forType.isAssignableFrom(Point.class)){
			// Point
			return (TypeEditor<T>) new PointEditor();
		}else if(forType.isAssignableFrom(Rectangle.class)){
			// Rectangle
			return (TypeEditor<T>) new RectangleEditor();
		}else if(forType.isAssignableFrom(Cursor.class)){
			// Cursor
			return (TypeEditor<T>) new CursorEditor();
		}else if(forType.isAssignableFrom(Image.class)){
			// Image
			return (TypeEditor<T>) new ImageEditor();
		}else if(forType.isAssignableFrom(Icon.class)){
			// Image
			return (TypeEditor<T>) new IconEditor();
		}else{
			// No Editor for given type
			return null;
		}
	}

	@Override
	public void focusGained(FocusEvent e) {}

	@Override
	public void focusLost(FocusEvent e) {
		onSubmit(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, "focusLost"));
	}
	
	
	public static class IconEditor extends TypeEditor<Icon> {
		
		private ImageEditor editor = new ImageEditor();
		
		public IconEditor(){
			setLayout(new BorderLayout());
			add(editor);
			
			editor.addSubmitListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					onSubmit(e);
				}
			});
		}

		@Override
		public boolean isInputValid() {
			return editor.isInputValid();
		}

		@Override
		public Icon getInput() {
			return new ImageIcon(editor.getInput());
		}

		@Override
		public void setInput(Icon input) {
			editor.setInput(ImageUtil.toImage(input));
		}

		@Override
		public void setEditable(boolean editable) {
			editor.setEditable(editable);
		}
		
	}
	
	public static class ImageEditor extends TypeEditor<Image> {
		
		private static final Color FONT_AWESOME_COLOR = new Color(51, 154, 240);
		private static final Ikon[] FONT_AWESOME_OPTIONS;
		
		static{
			List<Ikon> ikonList = new ArrayList<Ikon>();
			ikonList.addAll(Arrays.asList(FontAwesomeRegular.values()));
			ikonList.addAll(Arrays.asList(FontAwesomeSolid.values()));
			ikonList.addAll(Arrays.asList(FontAwesomeBrands.values()));
			
			Collections.sort(ikonList, new Comparator<Ikon>() {

				@Override
				public int compare(Ikon o1, Ikon o2) {
					return o1.getDescription().compareTo(o2.getDescription());
				}
				
			});
			
			FONT_AWESOME_OPTIONS = ikonList.toArray(new Ikon[ikonList.size()]);
		}
		
		private JImage image = new JImage((Image) null);
		private JButton browseButton = new JButton("...");
		private JImage fontAwesomeButton = new JImage(FontAwesomeBrands.FONT_AWESOME_ALT, FONT_AWESOME_COLOR);
		
		public ImageEditor(){
			setLayout(new MigLayout("insets 0, hidemode 3", "[grow, fill]0px[]2px[]", "[grow, fill]"));
			add(image, "hmax 50");
			add(browseButton, "hmax 25, hmin 25");
			add(fontAwesomeButton);
			
			browseButton.setToolTipText("browse for image files");
			
			fontAwesomeButton.setHoveredImage(FontIcon.of(FontAwesomeBrands.FONT_AWESOME, 25, FONT_AWESOME_COLOR).toImage());
			fontAwesomeButton.setPreferredSize(new Dimension(25, 25));
			fontAwesomeButton.setToolTipText("select a FontAwesome icon");
			
			browseButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					new Thread() {
						@Override
						public void run() {
							JFileChooser fileChooser = new JFileChooser();
							fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "bmp", "gif", "jpg", "jpeg"));
							int selection = fileChooser.showOpenDialog(ImageEditor.this.getTopLevelAncestor());
							if(selection == JFileChooser.APPROVE_OPTION){
								setInput(new ImageIcon(fileChooser.getSelectedFile().getAbsolutePath()).getImage());
								onSubmit(new ActionEvent(browseButton, ActionEvent.ACTION_PERFORMED, "imageChanged"));
							}
						}
					}.start();
				}
			});
			
			fontAwesomeButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					new Thread() {
						
						@Override
						public void run() {
							final Map<Ikon, Integer> sizes = new HashMap<Ikon, Integer>();
							final Map<Ikon, Color> colors = new HashMap<Ikon, Color>();
							for(Ikon ikon : FONT_AWESOME_OPTIONS){
								sizes.put(ikon, 16);
								colors.put(ikon, Color.BLACK);
							}
							
							SelectionDialog<Ikon> dialog = new SelectionDialog<Ikon>(ImageEditor.this, FONT_AWESOME_OPTIONS) {
								
								@Override
								public boolean matchesFilter(Ikon element, String filter) {
									return element.getDescription().toLowerCase().contains(filter.toLowerCase());
								}
								
								@Override
								public Component createComponent(final Ikon element) {
									final JPanel component = new JPanel(new MigLayout("wrap 2", "[grow, fill][grow, fill]", "[grow, fill][][][]"));
									Backgrounds.set(component, ColorUtil.INFO_BACKGROUND_COLOR, ColorUtil.mix(ColorUtil.INFO_BACKGROUND_COLOR, ColorUtil.INFO_BACKGROUND_COLOR, Color.WHITE), null);
									component.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, ColorUtil.INFO_BORDER_COLOR, ColorUtil.INFO_BORDER_COLOR.darker()));
									component.setMaximumSize(new Dimension(150, 150));
									
									Color color = colors.get(element);
									int size = sizes.get(element);
									
									final JImage image = new JImage(FontIcon.of(element, size, color).toImage());
									image.setPreferredSize(new Dimension(size, size));
									component.add(image, "span 2");
									
									JLabel descriptionLabel = new JLabel(element.getDescription());
									descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(10));
									descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
									component.add(descriptionLabel, "span 2");
									
									final JSpinner sizeEditor = new JSpinner(new SpinnerNumberModel(size, 1, 9999, 1));
									sizeEditor.addChangeListener(new ChangeListener() {
										
										@Override
										public void stateChanged(ChangeEvent e) {
											try{
												sizeEditor.commitEdit();
											}catch(ParseException ex) {
												sizeEditor.setValue(sizeEditor.getValue());
											}
											int size = (Integer) sizeEditor.getValue();
											sizes.put(element, size);
											image.setImage(FontIcon.of(element, size, colors.get(element)).toImage());
											image.setPreferredSize(new Dimension(size, size));
											component.revalidate();
											
										}
									});
									component.add(sizeEditor);
									
									final ColorEditor colorEditor = new ColorEditor();
									colorEditor.setInput(color);
									colorEditor.addSubmitListener(new ActionListener() {
										
										@Override
										public void actionPerformed(ActionEvent e) {
											colors.put(element, colorEditor.getInput());
											image.setImage(FontIcon.of(element, sizes.get(element), colors.get(element)).toImage());
										}
									});
									component.add(colorEditor);
									
									return component;
								}
							};
							dialog.setIconImage(FontIcon.of(FontAwesomeBrands.FONT_AWESOME, 50, FONT_AWESOME_COLOR).toImage());
							dialog.setTitle("Select FontAwesome Icon");
							Ikon ikon = dialog.open();
							if (ikon != null) {
								setInput(FontIcon.of(ikon, sizes.get(ikon), colors.get(ikon)).toImage());
								onSubmit(new ActionEvent(ImageEditor.this, ActionEvent.ACTION_PERFORMED, "imageChanged"));
							}
						}
					}.start();
				}
			});
		}

		@Override
		public boolean isInputValid() {
			return getInput() != null;
		}

		@Override
		public Image getInput() {
			return image.getImage();
		}

		@Override
		public void setInput(Image input) {
			image.setImage(input);
			image.setPreferredSize(new Dimension(input.getWidth(null), input.getHeight(null)));
		}

		@Override
		public void setEditable(boolean editable) {
			browseButton.setVisible(editable);
			fontAwesomeButton.setVisible(editable);
		}
		
	}
	
	public static class CursorEditor extends TypeEditor<Cursor> {
		
		private static Cursor[] OPTIONS = {
				Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR),
				Cursor.getPredefinedCursor(Cursor.HAND_CURSOR),
				Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR),
				Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR),
				Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR),
				Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR),
				Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR),
				Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR),
				Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR),
				Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR),
				Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR),
				Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR),
				Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR),
				Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR)
		};
		
		private Cursor currentCursor = null;
		private JButton cursorButton = new JButton();
		
		public CursorEditor(){
			setLayout(new BorderLayout());
			add(cursorButton);
			setInput(currentCursor);
			
			cursorButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					new Thread() {
						
						@Override
						public void run() {
							SelectionDialog<Cursor> dialog = new SelectionDialog<Cursor>(CursorEditor.this, OPTIONS) {
								
								@Override
								public boolean matchesFilter(Cursor element, String filter) {
									return element.getName().toLowerCase().contains(filter.toLowerCase());
								}
								
								@Override
								public Component createComponent(Cursor element) {
									JPanel component = new JPanel(new MigLayout("wrap 1", "[grow, fill]", "[grow, fill][]"));
									component.setCursor(element);
									Backgrounds.set(component, ColorUtil.INFO_BACKGROUND_COLOR, ColorUtil.mix(ColorUtil.INFO_BACKGROUND_COLOR, ColorUtil.INFO_BACKGROUND_COLOR, Color.WHITE), null);
									component.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, ColorUtil.INFO_BORDER_COLOR, ColorUtil.INFO_BORDER_COLOR.darker()));
									component.setMinimumSize(new Dimension(100, 100));
									
									JLabel nameLabel = new JLabel(element.getName());
									nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 10));
									nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
									nameLabel.setVerticalAlignment(SwingConstants.CENTER);
									component.add(nameLabel);
									
									JLabel noticeLabel = new JLabel("(hover to view)");
									noticeLabel.setFont(noticeLabel.getFont().deriveFont(Font.ITALIC, 8));
									noticeLabel.setHorizontalAlignment(SwingConstants.CENTER);
									noticeLabel.setForeground(Color.GRAY);
									component.add(noticeLabel);
									
									return component;
								}
							};
							dialog.setIconImage(FontIcon.of(FontAwesomeSolid.MOUSE_POINTER, 50, ColorUtil.INFO_FOREGROUND_COLOR).toImage());
							dialog.setTitle("Select Cursor");
							setInput(dialog.open());
							onSubmit(new ActionEvent(CursorEditor.this, ActionEvent.ACTION_PERFORMED, "cursorChanged"));
						}
					}.start();
				}
			});
		}

		@Override
		public boolean isInputValid() {
			return true;
		}

		@Override
		public Cursor getInput() {
			return currentCursor;
		}

		@Override
		public void setInput(Cursor input) {
			currentCursor = input;
			cursorButton.setText(input == null ? "no cursor set" : input.getName());
		}

		@Override
		public void setEditable(boolean editable) {
			cursorButton.setEnabled(editable);
		}
		
	}
	
	public static class RectangleEditor extends TypeEditor<Rectangle> implements ActionListener {
		
		private PointEditor locationInput = new PointEditor();
		private DimensionEditor sizeInput = new DimensionEditor();
		
		public RectangleEditor(){
			setLayout(new MigLayout("insets 0, wrap 1", "[grow, fill]", ""));
			add(locationInput);
			add(sizeInput);
			
			locationInput.addSubmitListener(this);
			sizeInput.addSubmitListener(this);
		}

		@Override
		public boolean isInputValid() {
			return getInput() != null;
		}

		@Override
		public Rectangle getInput() {
			Point location = locationInput.getInput();
			Dimension size = sizeInput.getInput();
			if(location == null || size == null) return null;
			else return new Rectangle(location, size);
		}

		@Override
		public void setInput(Rectangle input) {
			locationInput.setInput(input.getLocation());
			sizeInput.setInput(input.getSize());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			onSubmit(e);
		}

		@Override
		public void setEditable(boolean editable) {
			locationInput.setEditable(editable);
			sizeInput.setEditable(editable);
		}
		
	}
	
	public static class PointEditor extends TypeEditor<Point> {
		
		private JTextField xInputField = new JTextField();
		private JTextField yInputField = new JTextField();
		
		public PointEditor(){
			setLayout(new MigLayout("insets 0", "[]2px[grow, fill]10px[]2px[grow, fill]", ""));
			add(new JLabel("x:"));
			add(xInputField);
			add(new JLabel("y:"));
			add(yInputField);
			
			xInputField.addFocusListener(this);
			yInputField.addFocusListener(this);
		}

		@Override
		public boolean isInputValid() {
			return getInput() != null;
		}

		@Override
		public Point getInput() {
			try{
				int x = Integer.parseInt(xInputField.getText());
				int y = Integer.parseInt(yInputField.getText());
				return new Point(x, y);
			} catch(NumberFormatException e) {
				return null;
			}
		}

		@Override
		public void setInput(Point input) {
			xInputField.setText(input.x + "");
			yInputField.setText(input.y + "");
		}

		@Override
		public void setEditable(boolean editable) {
			xInputField.setEditable(editable);
			yInputField.setEditable(editable);
		}
		
	}
	
	public static class DimensionEditor extends TypeEditor<Dimension> {
		
		private JTextField widthInputField = new JTextField();
		private JTextField heightInputField = new JTextField();
		
		public DimensionEditor(){
			setLayout(new MigLayout("insets 0", "[]2px[grow, fill]10px[]2px[grow, fill]", ""));
			add(new JLabel("width:"));
			add(widthInputField);
			add(new JLabel("height:"));
			add(heightInputField);
			
			widthInputField.addFocusListener(this);
			heightInputField.addFocusListener(this);
		}

		@Override
		public boolean isInputValid() {
			return getInput() != null;
		}

		@Override
		public Dimension getInput() {
			try{
				int width = Integer.parseInt(widthInputField.getText());
				int height = Integer.parseInt(heightInputField.getText());
				return new Dimension(width, height);
			} catch(NumberFormatException e) {
				return null;
			}
		}

		@Override
		public void setInput(Dimension input) {
			widthInputField.setText(input.width + "");
			heightInputField.setText(input.height + "");
		}

		@Override
		public void setEditable(boolean editable) {
			widthInputField.setEditable(editable);
			heightInputField.setEditable(editable);
		}
		
	}
	
	public static class ColorEditor extends TypeEditor<Color> {
		
		private ColorChooserButton colorChooser = new ColorChooserButton(Color.BLACK);
		
		public ColorEditor(){
			setLayout(new BorderLayout());
			colorChooser.addColorChangedListener(new ColorChangedListener() {
				
				@Override
				public void colorChanged(Color newColor) {
					onSubmit(new ActionEvent(colorChooser, ActionEvent.ACTION_PERFORMED, "colorChanged"));
				}
			});
			add(colorChooser);
		}

		@Override
		public boolean isInputValid() {
			return true;
		}

		@Override
		public Color getInput() {
			return colorChooser.getSelectedColor();
		}

		@Override
		public void setInput(Color input) {
			colorChooser.setSelectedColor(input);
		}

		@Override
		public void setEditable(boolean editable) {
			colorChooser.setEnabled(editable);
		}
		
	}
	
	public static class StringEditor extends TypeEditor<String> {
		
		protected JTextField inputField = new JTextField();
		
		public StringEditor(){
			setLayout(new BorderLayout());
			
			inputField.addFocusListener(this);
			inputField.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					onSubmit(e);
				}
			});
			add(inputField);
		}

		@Override
		public boolean isInputValid() {
			return true;
		}

		@Override
		public String getInput() {
			return inputField.getText();
		}

		@Override
		public void setInput(String input) {
			inputField.setText(input);
		}

		@Override
		public void setEditable(boolean editable) {
			inputField.setEditable(editable);
		}
		
	}
	
	public static class BooleanEditor extends TypeEditor<Boolean> {
		
		protected JCheckBox checkBox = new JCheckBox();
		
		public BooleanEditor(){
			setLayout(new BorderLayout());
			
			checkBox.setHorizontalAlignment(SwingConstants.CENTER);
			checkBox.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					onSubmit(e);
				}
			});
			
			add(checkBox);
		}

		@Override
		public boolean isInputValid() {
			return true;
		}

		@Override
		public Boolean getInput() {
			return checkBox.isSelected();
		}

		@Override
		public void setInput(Boolean input) {
			checkBox.setSelected(input);
		}

		@Override
		public void setEditable(boolean editable) {
			checkBox.setEnabled(editable);
		}
	}
	
	
	public static abstract class NumberEditor<T extends Number> extends TypeEditor<T> {
		
		protected JTextField inputField = new JTextField();
		
		public NumberEditor(){
			setLayout(new BorderLayout());
			
			inputField.addFocusListener(this);
			inputField.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					onSubmit(e);
				}
			});
			add(inputField);
		}
		
		public void setInput(T input){
			inputField.setText(input.toString());
		}

		@Override
		public void setEditable(boolean editable) {
			inputField.setEditable(editable);
		}
		
		public static class IntegerEditor extends NumberEditor<Integer> {

			@Override
			public boolean isInputValid() {
				return getInput() != null;
			}

			@Override
			public Integer getInput() {
				try{
					return Integer.parseInt(inputField.getText());
				} catch(NumberFormatException e) {
					return null;
				}
			}
			
		}
		
		public static class LongEditor extends NumberEditor<Long> {

			@Override
			public boolean isInputValid() {
				return getInput() != null;
			}

			@Override
			public Long getInput() {
				try{
					return Long.parseLong(inputField.getText());
				} catch(NumberFormatException e) {
					return null;
				}
			}
			
		}
		
		public static class ShortEditor extends NumberEditor<Short> {

			@Override
			public boolean isInputValid() {
				return getInput() != null;
			}

			@Override
			public Short getInput() {
				try{
					return Short.parseShort(inputField.getText());
				} catch(NumberFormatException e) {
					return null;
				}
			}
			
		}
		
		public static class FloatEditor extends NumberEditor<Float> {

			@Override
			public boolean isInputValid() {
				return getInput() != null;
			}

			@Override
			public Float getInput() {
				try{
					return Float.parseFloat(inputField.getText());
				} catch(NumberFormatException e) {
					return null;
				}
			}
			
		}
		
		public static class DoubleEditor extends NumberEditor<Double> {

			@Override
			public boolean isInputValid() {
				return getInput() != null;
			}

			@Override
			public Double getInput() {
				try{
					return Double.parseDouble(inputField.getText());
				} catch(NumberFormatException e) {
					return null;
				}
			}
			
		}
	}
	
}
