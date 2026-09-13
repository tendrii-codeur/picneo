package mg.emit.picneo.tenten.enums;

import ij.ImagePlus;

public enum Image_Types {

	GRAY8("Gray 8-bit", ImagePlus.GRAY8),
	GRAY16("Gray 16-bit", ImagePlus.GRAY16),
	GRAY32("Gray 32-bit", ImagePlus.GRAY32),
	COLOR256("Color 256-bit", ImagePlus.COLOR_256),
	RGB("RGB", ImagePlus.COLOR_RGB);
	
	private final String displayImageTypeName;
	private final Integer displayImageTypeValue;
	
	Image_Types(String displayImageTypeName, Integer displayImageTypeValue) {
		this.displayImageTypeName = displayImageTypeName;
		this.displayImageTypeValue = displayImageTypeValue;
	}

	public String getDisplayImageTypeName() {
		return displayImageTypeName;
	}

	public Integer getDisplayImageTypeValue() {
		return displayImageTypeValue;
	}
}
