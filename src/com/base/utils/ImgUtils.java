package com.base.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImgUtils {

	public static byte[] getThumbnail(InputStream ins, int w, int h)
			throws IOException {
		BufferedImage in = ImageIO.read(ins);
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		out.getGraphics().drawImage(in, 0, 0, w, h, null);
		// ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		// ColorConvertOp op = new ColorConvertOp(cs, null);
		// out = op.filter(out, null);
		ImageIO.write(out, "jpg", baos);
		byte[] buf = baos.toByteArray();
		baos.close();
		return buf;
	}

}
