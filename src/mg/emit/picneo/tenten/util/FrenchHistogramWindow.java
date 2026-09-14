package mg.emit.picneo.tenten.util;

import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.Field;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.HistogramWindow;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Histogramme ImageJ dont les libellés (Count, Mean, Intensity, etc.) sont affichés en français.
 */
public class FrenchHistogramWindow extends HistogramWindow {

	private static final int WIN_WIDTH = 300;
	private static final int WIN_HEIGHT = 240;
	private static final int HIST_WIDTH = 256;
	private static final int HIST_HEIGHT = 128;
	private static final int BAR_HEIGHT = 12;
	private static final int XMARGIN = 20;
	private static final int YMARGIN = 10;
	private static final int INTENSITY1 = 0;
	private static final int INTENSITY2 = 1;
	private static final int RGB = 2;
	private static final int RED = 3;
	private static final int GREEN = 4;
	private static final int BLUE = 5;

	public FrenchHistogramWindow(ImagePlus imp) {
		super(imp);
		relabelInFrench(imp);
	}

	private void relabelInFrench(ImagePlus source) {
		ImageProcessor ip = getImagePlus().getProcessor();
		int textTop = YMARGIN + HIST_HEIGHT + BAR_HEIGHT + 4;
		ip.setColor(Color.white);
		ip.setRoi(0, textTop, WIN_WIDTH, WIN_HEIGHT - textTop);
		ip.fill();
		ip.resetRoi();
		ip.setColor(Color.black);

		int type = source.getType();
		boolean fixedRange = type == ImagePlus.GRAY8
				|| type == ImagePlus.COLOR_256
				|| type == ImagePlus.COLOR_RGB;
		int y = YMARGIN + HIST_HEIGHT + 2 + BAR_HEIGHT + 15;
		drawFrenchText(ip, XMARGIN + 1, y, fixedRange);
		getImagePlus().updateAndDraw();
	}

	private void drawFrenchText(ImageProcessor ip, int x, int y, boolean fixedRange) {
		ip.setFont(new Font("SansSerif", Font.PLAIN, 12));
		ip.setAntialiasedText(true);

		double hmin = cal.getCValue(stats.histMin);
		double hmax = cal.getCValue(stats.histMax);
		double range = hmax - hmin;
		if (fixedRange && !cal.calibrated() && hmin == 0 && hmax == 255) {
			range = 256;
		}
		ip.drawString(d2s(hmin), x - 4, y);
		ip.drawString(d2s(hmax), x + HIST_WIDTH - ip.getStringWidth(d2s(hmax)) + 10, y);

		int rgbMode = getRgbMode();
		if (rgbMode >= INTENSITY1) {
			int titleX = x + HIST_WIDTH / 2;
			int titleY = y + 1;
			ip.setJustification(ImageProcessor.CENTER_JUSTIFY);
			boolean weighted = ip instanceof ColorProcessor && ((ColorProcessor) ip).weightedHistogram();
			switch (rgbMode) {
				case INTENSITY1:
					ip.drawString(weighted ? "Intensité (pondérée)" : "Intensité (non pondérée)", titleX, titleY);
					break;
				case INTENSITY2:
					ip.drawString(weighted ? "Intensité (non pondérée)" : "Intensité (pondérée)", titleX, titleY);
					break;
				case RGB:
					ip.drawString("R+V+B", titleX, titleY);
					break;
				case RED:
					ip.drawString("Rouge", titleX, titleY);
					break;
				case GREEN:
					ip.drawString("Vert", titleX, titleY);
					break;
				case BLUE:
					ip.drawString("Bleu", titleX, titleY);
					break;
				default:
					break;
			}
			ip.setJustification(ImageProcessor.LEFT_JUSTIFY);
		}

		double binWidth = Math.abs(range / stats.nBins);
		boolean showBins = binWidth != 1.0 || !fixedRange;
		int col1 = XMARGIN + 5;
		int col2 = XMARGIN + HIST_WIDTH / 2 + 12;
		int row1 = y + 25;
		if (showBins) {
			row1 -= 8;
		}
		int row2 = row1 + 15;
		int row3 = row2 + 15;
		int row4 = row3 + 15;
		long count = stats.longPixelCount > 0 ? stats.longPixelCount : stats.pixelCount;
		String modeCount = " (" + stats.maxCount + ")";
		if (modeCount.length() > 12) {
			modeCount = "";
		}

		ip.drawString("Nombre : " + count, col1, row1);
		ip.drawString("Moyenne : " + d2s(stats.mean), col1, row2);
		ip.drawString("Écart-type : " + d2s(stats.stdDev), col1, row3);
		ip.drawString("Mode : " + d2s(stats.dmode) + modeCount, col2, row3);
		ip.drawString("Min : " + d2s(stats.min), col2, row1);
		ip.drawString("Max : " + d2s(stats.max), col2, row2);

		if (showBins) {
			ip.drawString("Classes : " + d2s(stats.nBins), col1, row4);
			ip.drawString("Largeur : " + d2s(binWidth), col2, row4);
		}
	}

	private int getRgbMode() {
		try {
			Field field = HistogramWindow.class.getDeclaredField("rgbMode");
			field.setAccessible(true);
			return field.getInt(this);
		} catch (ReflectiveOperationException e) {
			return -1;
		}
	}

	private String d2s(double d) {
		if ((int) d == d) {
			return IJ.d2s(d, 0);
		}
		return IJ.d2s(d, 3, 8);
	}
}
