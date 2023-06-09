package com.creditsuisse.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.imageio.ImageIO;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ImageUtil {

	/**
	 * Converts the given image to the matching gray scaled image.
	 * @param image the image to convert (will no modify original image)
	 * @return a new image instance that is now gray
	 */
	public static Image grayScale(Image image){
		ImageFilter filter = new GrayFilter(true, 50);  
		ImageProducer producer = new FilteredImageSource(image.getSource(), filter);  
		return Toolkit.getDefaultToolkit().createImage(producer);  
	}
	
	/**
	 * Scale an image to the given width, keeping its aspect ratio
	 * @param original the original image
	 * @param width the target with
	 * @return a scaled image
	 */
	public static Image getWidthScaledImage(Image original, int width){
		double scale = (double) width / original.getWidth(null);
		return original.getScaledInstance(width, (int)(original.getHeight(null) * scale), Image.SCALE_DEFAULT);
	}

	/**
	 * Scale an image to the given height, keeping its aspect ratio
	 * @param original the original image
	 * @param height the target height
	 * @return a scaled image
	 */
	public static Image getHeightScaledImage(Image original, int height){
		double scale = (double) height / original.getHeight(null);
		return original.getScaledInstance((int)(original.getWidth(null) * scale), height, Image.SCALE_DEFAULT);
	}
	
	/**
	 * Converts the given base64 image string to a buffered image
	 * @param base64 the base64 string representing an image
	 * @return the converted image or null if it failed
	 */
	public static BufferedImage fromBase64(String base64){
		BufferedImage image = null;
        byte[] imageByte;
        try {
        	Decoder decoder = Base64.getDecoder();
            imageByte = decoder.decode(base64);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
	}

	/**
	 * Converts an image to a base64 string representation of it, using png as image type.
	 * @param image the image to convert
	 * @return the base64 string or null if it failed
	 */
	public static String toBase64(BufferedImage image){
		return toBase64(image, "png");
	}
	
	/**
	 * Converts an image to a base64 string representation of it.
	 * @param image the image to convert
	 * @param type the image type (like png, or jpg)
	 * @return the base64 string or null if it failed
	 */
	public static String toBase64(BufferedImage image, String type){
		String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
 
        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();
 
            Encoder encoder = Base64.getEncoder();
            imageString = new String(encoder.encode(imageBytes));
 
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
	}

	/**
	 * Changes the brightness of given image.
	 * @param image the image to change the brightness on (this instance will stay untouched)
	 * @param mult the multiplier for the color of each pixel. R, G and B will not be over 255 or under 0.
	 * @return a new image with higher or lower brightness.
	 */
	public static Image changeBrightness(Image image, float mult) {
		BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		newImage.getGraphics().drawImage(image, 0, 0, null);
		
		WritableRaster wr = newImage.getRaster();
        int[] pixel = new int[4];
        int[] overflows = new int[3];
        
        for(int i = 0; i < wr.getWidth(); i++){
            for(int j = 0; j < wr.getHeight(); j++){
                wr.getPixel(i, j, pixel);
                pixel[0] = (int) (pixel[0] * mult);
                pixel[1] = (int) (pixel[1] * mult);
                pixel[2] = (int) (pixel[2] * mult);
                
                // if a value exceeds 255, the other color values will instead be raised to bleach the image
                overflows[0] = 0;
                overflows[1] = 0;
                overflows[2] = 0;
                if(pixel[0] > 255){
                	overflows[1] += pixel[0] - 255;
                	overflows[2] += pixel[0] - 255;
                	pixel[0] = 255;
                }
                if(pixel[1] > 255){
                	pixel[0] += pixel[1] - 255;
                	pixel[2] += pixel[1] - 255;
                	pixel[1] = 255;
                }
                if(pixel[2] > 255){
                	pixel[0] += pixel[2] - 255;
                	pixel[1] += pixel[2] - 255;
                	pixel[2] = 255;
                }
                pixel[0] = Math.min(255, pixel[0] + overflows[0]);
                pixel[1] = Math.min(255, pixel[1] + overflows[1]);
                pixel[2] = Math.min(255, pixel[2] + overflows[2]);
                
                wr.setPixel(i, j, pixel);
            }
        }
        
        return newImage;
	}
	
	/**
	 * Converts the given icon to an image.
	 * @param icon the icon to convert
	 * @return an image displaying the same content as the icon
	 */
	public static Image toImage(Icon icon){
		if (icon instanceof ImageIcon) {
			return ((ImageIcon) icon).getImage();
		} else {
			int w = icon.getIconWidth();
			int h = icon.getIconHeight();
			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.getGraphics();
			icon.paintIcon(null, g, 0, 0);
			g.dispose();
			return image;
		}
	}
	
	public static BufferedImage toBufferedImage(Image image){
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(image, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}
	
	private static GraphicsConfiguration getGraphicsConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

	private static BufferedImage createCompatibleImage(int width, int height, int transparency) {
        BufferedImage image = getGraphicsConfiguration().createCompatibleImage(width, height, transparency);
        image.coerceData(true);
        return image;
    }

    private static void applyQualityRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
    
    /**
     * Merges the two images.
     * @param base the image underneath
     * @param overlay the image to put on top
     * @return a new image instance with the base image below and the overlay image above.
     */
    public static Image merge(Image base, Image overlay){
    	return merge(base, overlay, null);
    }
    

    /**
     * Merges the two images.
     * <br/>Paints the overlay with the given offset.
     * @param base the image underneath
     * @param overlay the image to put on top
     * @param overlayOffset the offset to position the overlay with
     * @return a new image instance with the base image below and the overlay image above.
     */
    public static BufferedImage merge(Image base, Image overlay, Dimension overlayOffset){
    	if(overlayOffset == null) overlayOffset = new Dimension();
    	
    	// create the new image, canvas size is the max. of both image sizes
    	int w = Math.max(base.getWidth(null), overlay.getWidth(null));
    	int h = Math.max(base.getHeight(null), overlay.getHeight(null));
    	BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

    	// paint both images, preserving the alpha channels
    	Graphics g = combined.getGraphics();
    	g.drawImage(base, 0, 0, null);
    	g.drawImage(overlay, overlayOffset.width, overlayOffset.height, null);
    	return combined;
    }
    
    /**
     * Calls {@link #blend(Image, Image, Dimension, int, int)}
     * with 1/10th of the width as blendWidth and 1/10th of the height as blendHeight.
     * @param base the image to end up underneath
     * @param overlay the image to end up on top
     * @param overlayOffset the offset to position the overlay with (may be null for no offset)
     * @return the newly created image
     */
    public static Image blend(Image base, Image overlay, Dimension overlayOffset){
    	int blendWidth = overlay.getWidth(null) / 10;
    	int blendHeight = overlay.getHeight(null) / 10;
    	return blend(base, overlay, overlayOffset, blendWidth, blendHeight);
    }
    
    /**
     * Blend the given overlay image into the given base image.
     * <br/>Does the same as {@link #merge(Image, Image, Dimension)} but also blends in the edges of the overlay with the base image.
     * @param base the image to end up underneath
     * @param overlay the image to end up on top
     * @param overlayOffset the offset to position the overlay with (may be null for no offset)
     * @param blendWidth the width of the are the image is blended on the left and right sides
     * @param blendHeight the width of the are the image is blended on the top and bottom sides
     * @return the newly created image
     */
    public static Image blend(Image base, Image overlay, Dimension overlayOffset, int blendWidth, int blendHeight){
		BufferedImage blendedOverlay = toBufferedImage(overlay);
		int overlayWidth = blendedOverlay.getWidth();
		int overlayHeight = blendedOverlay.getHeight();
		
		float xBlendStep = 1f / (blendWidth + 1);
		float yBlendStep = 1f / (blendHeight + 1);
		
    	for (int y = 0; y < overlayHeight; y++) {
			for (int x = 0; x < overlayWidth; x++) {
				Color color = new Color(blendedOverlay.getRGB(x,y), true);
				float alphaScale = 1f;
				
				// calculate by x
				if(x < blendWidth){
					alphaScale = Math.min(alphaScale, (x + 1) * xBlendStep);
				}
				if(x >= overlayWidth - blendWidth){
					alphaScale = Math.min(alphaScale, (overlayWidth - x) * xBlendStep);
				}
				
				// calculate by y
				if(y < blendHeight){
					alphaScale = Math.min(alphaScale, (y + 1) * yBlendStep);
				}
				if(y >= overlayWidth - blendHeight){
					alphaScale = Math.min(alphaScale, (overlayHeight - y) * yBlendStep);
				}
				
				if(alphaScale > 1){
					// should not be possible
					alphaScale = 1f;
				}else if(alphaScale < 0){
					// should not be possible
					alphaScale = 0f;
				}
				
				int alpha = (int)(color.getAlpha() * alphaScale);
				
				blendedOverlay.setRGB(x, y, new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB());
			}
		}
    	
    	return merge(base, blendedOverlay, overlayOffset);
    }
	
    /**
     * Creates a mask of the given image in the given color.
     * <br/>A mask has the same shape as the given image (only the non-transparent pixels), but consists only of the given color.
     * @param image the image to create a mask of
     * @param color the color of the mask
     * @return a mask with the same shape as the given image and with the given color
     */
	public static Image mask(Image image, Color color){
		int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);

        BufferedImage imgMask = createCompatibleImage(imgWidth, imgHeight, Transparency.TRANSLUCENT);
        Graphics2D g2 = imgMask.createGraphics();
        applyQualityRenderingHints(g2);

        g2.drawImage(image, 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, (float) color.getAlpha() / 255));
        g2.setColor(color);

        g2.fillRect(0, 0, image.getWidth(null), image.getHeight(null));
        g2.dispose();

        return imgMask;
	}
	
	/**
	 * Tints the given image with the given color.
	 * <br/>Use the colors alpha to define the tint strength.
	 * @param image the image to tint
	 * @param color the color to tint with
	 * @return a new image instance which is the tinted version of the given image
	 */
	public static Image tint(Image image, Color color){
		int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);

        BufferedImage tinted = createCompatibleImage(imgWidth, imgHeight, Transparency.TRANSLUCENT);
        Graphics2D g2 = tinted.createGraphics();
        applyQualityRenderingHints(g2);
        g2.drawImage(image, 0, 0, null);
        g2.drawImage(mask(image, color), 0, 0, null);
        g2.dispose();

        return tinted;
	}
	
	/**
	 * Colors the given image with the given color.
	 * <br/>While {@link #tint(Image, Color)} combines the color of the pixels with the given color,
	 * this will only take the brightness of the pixel and then color that pixel with the given color in that brightness.
	 * <br/>Therefore this will result in pixels only in the given color and darker / brighter versions of it.
	 * @param image the image to color
	 * @param color the color to apply
	 * @param median defines what RGB values a pixel in the image must have to be converted to exactly the given color (with equal brightness).
	 * Default is 255 / 2, a higher value will result in a darker image (and a lower value in a brighter image).
	 * @return a new image in the given color
	 */
	public static Image color(Image image, Color color, int median){
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		if(width <= 0 || height <= 0) throw new IllegalArgumentException("given image must be fully loaded");

		BufferedImage base = toBufferedImage(image);
		float alphaScale = color.getAlpha() / 255f;
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		int redDistance = 255 - red;
		int greenDistance = 255 - green;
		int blueDistance = 255 - blue;
		
		int medianDistance = 255 - median;
		
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Color baseColor = new Color(base.getRGB(x,y), true);
				float distance = (baseColor.getRed() + baseColor.getGreen() + baseColor.getBlue()) / 3f - median;
				float baseStrength = distance > 0 ? distance / medianDistance : distance / median;
				int a = (int) (baseColor.getAlpha() * alphaScale);
				int r = (int) (red + (baseStrength > 0 ? redDistance * baseStrength : red * baseStrength));
				int g = (int) (green + (baseStrength > 0 ? greenDistance * baseStrength : green * baseStrength));
				int b = (int) (blue + (baseStrength > 0 ? blueDistance * baseStrength : blue * baseStrength));
				a = Math.min(255, a);
				r = Math.min(255, r);
				g = Math.min(255, g);
				b = Math.min(255, b);
				result.setRGB(x, y, new Color(r, g, b, a).getRGB());
			}
		}
		
		return result;
	}
	
	/**
	 * Colors the given image with the given color.
	 * <br/>While {@link #tint(Image, Color)} combines the color of the pixels with the given color,
	 * this will only take the brightness of the pixel and then color that pixel with the given color in that brightness.
	 * <br/>Therefore this will result in pixels only in the given color and darker / brighter versions of it.
	 * @param image the image to color
	 * @param color the color to apply
	 * @return a new image in the given color
	 */
	public static Image color(Image image, Color color){
		return color(image, color, 255 / 2);
	}
	
	/**
	 * Crops the given image to the given rectangle.
	 * @param image
	 * @param cropTo
	 * @return
	 * @throws IllegalArgumentException if the rectangle exceeds the images bounds
	 */
	public static Image crop(Image image, Rectangle cropTo) throws IllegalArgumentException{
		if(cropTo.x + cropTo.width > image.getWidth(null) || cropTo.y + cropTo.height > image.getHeight(null)){
			throw new IllegalArgumentException("Cannot crop to rectangle that exceeds the images bounds.");
		}
		BufferedImage base = toBufferedImage(image);
		return base.getSubimage(cropTo.x, cropTo.y, cropTo.width, cropTo.height);
	}
	
	/**
	 * Trims the given image to non transparent pixels.
	 * @param image the image to trim
	 * @param maxAlpha the maximum alpha (0 - 255) a pixel may have to still be considered transparent
	 * @return a new trimmed image
	 */
	public static Image trim(Image image, int maxAlpha){
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		int smallestX = width;
		int smallestY = height;
		int largestX = 0;
		int largestY = 0;
		BufferedImage base = toBufferedImage(image);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Color baseColor = new Color(base.getRGB(x,y), true);
				if(baseColor.getAlpha() > maxAlpha){
					if(x < smallestX) smallestX = x;
					if(x > largestX) largestX = x;
					if(y < smallestY) smallestY = y;
					if(y > largestY) largestY = y;
				}
			}
		}
		return crop(image, new Rectangle(smallestX, smallestY, largestX - smallestX + 1, largestY - smallestY + 1));
	}
	
	/**
	 * Puts the given image into a superscript position.
	 * @param image the image to convert
	 * @return a new image with the same height but smaller width, containing the given image in the top right corner
	 */
	public static Image toSuperscript(Image image){
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		int newWidth = width / 2;
		int newHeight = width / 2;
		int y = height / 10;
		
		BufferedImage result = new BufferedImage(newWidth, height, BufferedImage.TYPE_INT_ARGB);
		result.createGraphics().drawImage(image, 0, y, newWidth, newHeight, null);
		
		return result;
	}
}
