package com.creditsuisse.graphics.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands;
import org.kordamp.ikonli.swing.FontIcon;

import com.creditsuisse.graphics.ImageUtil;

public class JImage extends JLabel {
	private static final long serialVersionUID = 1L;

	private PreparedRenderable renderable;
	private PreparedRenderable disabledRenderable;
	private PreparedRenderable hoveredRenderable;
	private PreparedRenderable clickedRenderable;
	
	private boolean enabled = true;
	private boolean hovered;
	private boolean clicked;

	private final List<ActionListener> actionListeners = new ArrayList<ActionListener>();

	private boolean isCursorSet = false;
	private boolean mouseListenersInitialized = false;
	
	private JImage() {
		addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				repaint();
			}

			@Override
			public void focusGained(FocusEvent e) {
				repaint();
			}
		});
		addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (!actionListeners.isEmpty()) {
						doClick();
					}
				}
			}
		});
	}

	public JImage(Image image) {
		this();
		this.renderable = new PreparedImage(image);
	}

	public JImage(Icon icon) {
		this(ImageUtil.toImage(icon));
	}

	public JImage(Ikon icon) {
		this(icon, Color.BLACK);
	}

	public JImage(Ikon icon, Color color) {
		this();
		setIcon(icon, color);
	}

	private void initMouseListeners() {
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				clicked = false;
				repaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				clicked = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				hovered = false;
				repaint();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				hovered = true;
				repaint();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				doClick(e.getButton());
			}
		});

		mouseListenersInitialized = true;
	}

	public void doClick() {
		doClick(MouseEvent.BUTTON1);
	}

	public void doClick(int mouseButton) {
		if (isEnabled() && mouseButton == MouseEvent.BUTTON1) {
			if (!hasFocus()) {
				requestFocus();
			}
			for (ActionListener actionListener : getActionListeners())
				actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "mouseClicked"));
		}
	}

	@Override
	public Dimension getPreferredSize() {
		if (super.isPreferredSizeSet()) {
			return super.getPreferredSize();
		} else if (getImage() != null) {
			int height = getFontMetrics(getFont()).getHeight();
			double imageRatio = (double) getImage().getWidth(null) / getImage().getHeight(null);
			int width = (int) (height * imageRatio);
			return new Dimension(width, height);
		} else return new Dimension(0, 0);
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.setCursor(getCursor());
		super.paintComponent(g);

		PreparedRenderable image;
		if (!isEnabled() && disabledRenderable != null) image = disabledRenderable;
		else if (clicked && clickedRenderable != null) image = clickedRenderable;
		else if ((hovered || isFocusOwner()) && hoveredRenderable != null) image = hoveredRenderable;
		else image = this.renderable;

		if (image != null) {
			Insets insets;
			if (getBorder() == null) {
				insets = new Insets(0, 0, 0, 0);
			} else {
				insets = getBorder().getBorderInsets(this);
			}
			
			int width = getWidth() - insets.right - insets.left;
			int height = getHeight() - insets.bottom - insets.top;
			if (width <= 0 || height <= 0) {
				return;
			}
			
			double imageRatio = (double) image.getWidth() / image.getHeight();
			double containerRatio = (double) width / height;
			if (imageRatio > containerRatio) {
				height = (int) (width / imageRatio);
			} else {
				width = (int) (height * imageRatio);
			}
			int x = (getWidth() - width) / 2;
			int y = (getHeight() - height) / 2;

			Image scaled = image.getScaledImage(width, height);
			g.drawImage(scaled, x, y, null);
		}
	}

	public void scalePreferredSize(float scale) {
		Dimension prefSize = getPreferredSize();
		prefSize.width *= scale;
		prefSize.height *= scale;
		setPreferredSize(prefSize);
	}

	/**
	 * Convenience method to use JImages as Buttons <br/>
	 * Adds a click listener. <br/>
	 * Also changes the cursor to a hand cursor when on top of this component. <br/>
	 * To revert this, call {@link #setCursor(java.awt.Cursor)}
	 * 
	 * @param actionListener
	 *            the listener to be called on click
	 */
	public void addActionListener(final ActionListener actionListener) {
		actionListeners.add(actionListener);
		if (!mouseListenersInitialized) initMouseListeners();
		setFocusable(true);
	}

	protected List<ActionListener> getActionListeners() {
		return actionListeners;
	}

	public void generateStateImages() {
		generateStateImages(true, true, true);
	}

	public void generateStateImages(boolean disabled, boolean hovered, boolean clicked) {
		if (renderable == null) throw new IllegalStateException("Cannot generate state images while default image is null.");
		if (disabled) {
			disabledRenderable = new AlteredRenderable(renderable, (image) -> ImageUtil.grayScale(image));
		}
		if (hovered) {
			hoveredRenderable = new AlteredRenderable(renderable, (image) -> ImageUtil.changeBrightness(image, 1.2f));
		}
		if (clicked) {
			clickedRenderable = new AlteredRenderable(renderable, (image) -> ImageUtil.changeBrightness(image, 0.8f));
		}
		if (!mouseListenersInitialized) initMouseListeners();
	}
	
	public void generateStateImages(Color disabled, Color hovered, Color clicked) {
		if (renderable == null) throw new IllegalStateException("Cannot generate state images while default image is null.");
		if (disabled != null) {
			if (renderable instanceof PreparedIkon) {
				disabledRenderable = new PreparedIkon(((PreparedIkon) renderable).getIkon(), disabled);
			} else {
				disabledRenderable = new AlteredRenderable(renderable, (image) -> ImageUtil.color(image, disabled));
			}
		}
		if (hovered != null) {
			if (renderable instanceof PreparedIkon) {
				disabledRenderable = new PreparedIkon(((PreparedIkon) renderable).getIkon(), hovered);
			} else {
				disabledRenderable = new AlteredRenderable(renderable, (image) -> ImageUtil.color(image, hovered));
			}
		}
		if (clicked != null) {
			if (renderable instanceof PreparedIkon) {
				disabledRenderable = new PreparedIkon(((PreparedIkon) renderable).getIkon(), clicked);
			} else {
				disabledRenderable = new AlteredRenderable(renderable, (image) -> ImageUtil.color(image, clicked));
			}
		}
		if (!mouseListenersInitialized) initMouseListeners();
	}

	public Image getImage() {
		return renderable.getOriginalImage();
	}

	public void setImage(Image image) {
		renderable = new PreparedImage(image);
		repaint();
	}

	public void setIcon(Ikon icon) {
		setIcon(icon, Color.BLACK);
	}

	public void setIcon(Ikon icon, Color color) {
		renderable = new PreparedIkon(icon, color);
	}

	public Image getDisabledImage() {
		return disabledRenderable.getOriginalImage();
	}
	
	public Ikon getDisabledIkon() {
		return disabledRenderable instanceof PreparedIkon ? ((PreparedIkon) disabledRenderable).getIkon() : null;
	}

	public void setDisabledImage(Image disabledImage) {
		setDisabledRenderable(new PreparedImage(disabledImage));
	}

	public void setDisabledIkon(Ikon ikon, Color color) {
		setDisabledRenderable(new PreparedIkon(ikon, color));
	}

	public void setDisabledRenderable(PreparedRenderable disabledRenderable) {
		this.disabledRenderable = disabledRenderable;
		if (!mouseListenersInitialized && disabledRenderable != null) initMouseListeners();
		repaint();
	}

	public Image getHoveredImage() {
		return hoveredRenderable.getOriginalImage();
	}
	
	public Ikon getHoveredIkon() {
		return hoveredRenderable instanceof PreparedIkon ? ((PreparedIkon) hoveredRenderable).getIkon() : null;
	}

	public void setHoveredImage(Image hoveredImage) {
		setHoveredRenderable(new PreparedImage(hoveredImage));
	}

	public void setHoveredIkon(Ikon ikon, Color color) {
		setHoveredRenderable(new PreparedIkon(ikon, color));
	}

	public void setHoveredRenderable(PreparedRenderable hoveredRenderable) {
		this.hoveredRenderable = hoveredRenderable;
		if (!mouseListenersInitialized && hoveredRenderable != null) initMouseListeners();
		repaint();
	}

	public Image getClickedImage() {
		return clickedRenderable.getOriginalImage();
	}
	
	public Ikon getClickedIkon() {
		return clickedRenderable instanceof PreparedIkon ? ((PreparedIkon) clickedRenderable).getIkon() : null;
	}

	public void setClickedImage(Image clickedImage) {
		setClickedRenderable(new PreparedImage(clickedImage));
	}

	public void setClickedIkon(Ikon ikon, Color color) {
		setClickedRenderable(new PreparedIkon(ikon, color));
	}

	public void setClickedRenderable(PreparedRenderable clickedRenderable) {
		this.clickedRenderable = clickedRenderable;
		if (!mouseListenersInitialized && clickedRenderable != null) initMouseListeners();
		repaint();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		repaint();
	}

	public boolean isHovered() {
		return hovered;
	}

	public boolean isClicked() {
		return clicked;
	}

	@Override
	public Cursor getCursor() {
		if (!isCursorSet && !actionListeners.isEmpty() && isEnabled()) return Cursor.getPredefinedCursor(isEnabled() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR);
		else return super.getCursor();
	}

	@Override
	public void setCursor(Cursor cursor) {
		super.setCursor(cursor);
		isCursorSet = true;
	}

	public JImage createDelegate() {
		final JImage thisImage = this;
		JImage delegate = new JImage(getImage()) {

			public boolean isEnabled() {
				return thisImage.isEnabled();
			}

			public List<ActionListener> getActionListeners() {
				List<ActionListener> actionListeners = new ArrayList<ActionListener>(thisImage.getActionListeners());
				actionListeners.addAll(super.getActionListeners());
				return actionListeners;
			}

			public Image getImage() {
				return thisImage.getImage();
			}

			public Image getDisabledImage() {
				return thisImage.getDisabledImage();
			}

			public Image getHoveredImage() {
				return thisImage.getHoveredImage();
			}

			public Image getClickedImage() {
				return thisImage.getClickedImage();
			}
		};

		return delegate;
	}
	
	
	private interface PreparedRenderable {
		public Image getScaledImage(int width, int height);
		public Image getOriginalImage();
		public int getHeight();
		public int getWidth();
	}
	
	
	private class PreparedImage implements PreparedRenderable {
		private Image original;
		private Image scaled;
		
		public PreparedImage(Image image) {
			original = image;
			scaled = image;
		}
		
		@Override
		public Image getScaledImage(int width, int height) {
			if (scaled.getWidth(null) != width || scaled.getHeight(null) != height) {
				scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			}
			return scaled;
		}

		@Override
		public Image getOriginalImage() {
			return original;
		}

		@Override
		public int getHeight() {
			return original.getHeight(null);
		}

		@Override
		public int getWidth() {
			return original.getWidth(null);
		}
	}
	
	private class PreparedIkon implements PreparedRenderable {
		private FontIcon fontIcon;
		private BufferedImage scaled;
		
		public PreparedIkon(Ikon ikon, Color color) {
			fontIcon = FontIcon.of(ikon, color);
		}

		@Override
		public Image getScaledImage(int width, int height) {
			if (scaled != null && scaled.getWidth() == width && scaled.getHeight() == height) {
				return scaled;
			}
			
			// increase font size until icon size exceeds given size
			while (fontIcon.getIconWidth() < width && fontIcon.getIconHeight() < height) {
				fontIcon.setIconSize(fontIcon.getIconSize() + 1);
			}
			
			// decrease font size until icon width and height are just below the given width and height
			while (fontIcon.getIconWidth() > width || fontIcon.getIconHeight() > height) {
				fontIcon.setIconSize(fontIcon.getIconSize() - 1);
			}
			
			// get image from icon and center it
			BufferedImage base = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			BufferedImage iconImage = fontIcon.toImage();
			scaled = ImageUtil.merge(base, iconImage, new Dimension((base.getWidth() - iconImage.getWidth()) / 2, (base.getHeight() - iconImage.getHeight()) / 2));
			return scaled;
		}

		@Override
		public Image getOriginalImage() {
			return fontIcon.toImage();
		}

		@Override
		public int getHeight() {
			return fontIcon.getIconHeight();
		}

		@Override
		public int getWidth() {
			return fontIcon.getIconWidth();
		}
		
		public Ikon getIkon() {
			return fontIcon.getIkon();
		}
	}
	
	private interface ImageAlteration {
		public Image alter(Image image);
	}

	
	private class AlteredRenderable implements PreparedRenderable {
		
		private PreparedRenderable base;
		private ImageAlteration alteration;
		
		private Image alteredImage;
		
		public AlteredRenderable(PreparedRenderable base, ImageAlteration alteration) {
			this.base = base;
			this.alteration = alteration;
		}

		@Override
		public Image getScaledImage(int width, int height) {
			if (alteredImage == null || alteredImage.getWidth(null) != width || alteredImage.getHeight(null) != height) {
				alteredImage = alteration.alter(base.getScaledImage(width, height));
			}
			return alteredImage;
		}

		@Override
		public Image getOriginalImage() {
			return alteration.alter(base.getOriginalImage());
		}

		@Override
		public int getHeight() {
			return base.getHeight();
		}

		@Override
		public int getWidth() {
			return base.getWidth();
		}
		
	}

}
