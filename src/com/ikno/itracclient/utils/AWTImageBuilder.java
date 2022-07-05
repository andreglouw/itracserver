package com.ikno.itracclient.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.ikno.dao.IImage;
import com.ikno.dao.IImageBuilder;
import com.ikno.dao.business.Asset;
import com.ikno.dao.business.AssetImage;
import com.ikno.dao.business.BlobImage;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Logging;

public class AWTImageBuilder implements IImageBuilder{
	private static final Logger logger = Logging.getLogger(AWTImageBuilder.class.getName());
	public int width = -1;
	public int height = -1;

	public AWTImageBuilder() {
		this.width = -1;
		this.height = -1;
	}
	public AWTImageBuilder(int width, int height) {
		this.width = width;
		this.height = height;
	}
	public BufferedImage buildImage(IImage reference) {
		if (width != -1 && height != -1)
			return buildImage(reference, width, height);
		BufferedImage result = null;
		ByteArrayInputStream bis = new ByteArrayInputStream(reference.getImageByteArray());
		try {
			result = ImageIO.read(bis);
		} catch (Exception e) {
			logger.severe("Error reading Image from Image Byte Array as returned from reference: "+e);
		} finally {
			try {
				bis.close();
			} catch (Exception e) {
				logger.severe("Error closing Byte Array input stream: "+e);
			}
		}
		return result;
	}

	public BufferedImage buildImage(IImage reference, int width, int height) {
		ByteArrayInputStream bis = new ByteArrayInputStream(reference.getImageByteArray());
		try {
			return this.buildImage(ImageIO.read(bis), width, height);
		} catch (Exception e) {
			logger.severe("Error reading Image from Image Byte Array as returned from reference: "+e);
		} finally {
			try {
				bis.close();
			} catch (Exception e) {
				logger.severe("Error closing Byte Array input stream: "+e);
			}
		}
		return null;
	}

	public BufferedImage buildImage(BufferedImage image, int width, int height) throws Exception {
		BufferedImage resized = null;
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		// Resizing method 'http://www.webmaster-talk.com/coding-forum/63227-image-resizing-in-java.html'
		int thumbWidth = width;
		int thumbHeight = height;
		double thumbRatio = (double)thumbWidth/(double)thumbHeight;
		double imageRatio = (double)imageWidth/(double)imageHeight;
		if (thumbRatio < imageRatio) {
			thumbHeight = (int)(thumbWidth/imageRatio);
		} else {
			thumbWidth = (int)(thumbHeight*imageRatio);
		}
		resized = new BufferedImage(thumbWidth,thumbHeight,BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = resized.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

		/*
		float softenFactor = 0.05f;
        float[] softenArray = {0, softenFactor, 0, softenFactor, 1-(softenFactor*4), softenFactor, 0, softenFactor, 0};
        Kernel kernel = new Kernel(3, 3, softenArray);
        ConvolveOp cOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        resized = cOp.filter(resized, null);
        */

		return resized;
	}

	public BufferedImage buildImage(BufferedImage image) throws Exception {
		if (width != -1 && height != -1)
			return buildImage(image,width,height);
		return image;
	}
	public static void main(String[] args) {
		try {
			FileInputStream is = new FileInputStream("c:/temp/bullet_triangle_red_0.png");
			BufferedImage bi = ImageIO.read(is);
			int w = bi.getWidth();
			int h = bi.getHeight();
			Color origColor = Color.red;
			Color newColor = new Color(88,171,249);
			ColorModel cm = bi.getColorModel();
			WritableRaster wr = cm.createCompatibleWritableRaster(w, h);
			BufferedImage bout = new BufferedImage(cm,wr,cm.isAlphaPremultiplied(),null);
			int pixel;
			int newPixel = newColor.getRGB();
			for (int x=0;x<w;x++) {
				for (int y=0;y<h;y++) {
					pixel = bi.getRGB(x, y);
					if ((pixel & 0xff000000) != 0x00) {
						int ratio = (origColor.getRGB()-pixel)/origColor.getRGB();
						newPixel = -(ratio*newColor.getRGB())+newColor.getRGB();
						bout.setRGB(x, y, newPixel);
					}
				}
			}
			ImageIO.write(bout, "png", new File("c:/temp/bullet_triangle_blue_0.png"));
			
			/*
			DAO.localDAO().beginTransaction();
			Asset asset = DAO.localDAO().getAssetById(4225022);
			AssetImage image = asset.getAssetImage();
			if (image != null) {
				asset.setAssetImage(null);
				DAO.localDAO().delete(image);
			}
			image = new AssetImage(new BlobImage("c:/Developer/iTrac/com.ikno.itracclient/images/man_0.png"),
					new BlobImage("c:/Developer/iTrac/com.ikno.itracclient/images/man_1.png"),
					new BlobImage("c:/Developer/iTrac/com.ikno.itracclient/images/man_2.png"),
					new BlobImage("c:/Developer/iTrac/com.ikno.itracclient/images/man_3.png"),
					new BlobImage("c:/Developer/iTrac/com.ikno.itracclient/images/man_4.png"),
					new BlobImage("c:/Developer/iTrac/com.ikno.itracclient/images/man_5.png"),
					new BlobImage("c:/Developer/iTrac/com.ikno.itracclient/images/man_6.png"),
					new BlobImage("c:/Developer/iTrac/com.ikno.itracclient/images/man_7.png"));
			asset.setAssetImage(image);
			DAO.localDAO().saveOrUpdate(asset);
			DAO.localDAO().commitTransaction();
			*/
		} catch (Exception e) {
			DAO.localDAO().rollbackTransaction();
		}
	}
}
