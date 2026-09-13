package mg.emit.picneo.tenten.enums;

import ij.process.ImageProcessor;

public enum Threshold_Lut_Types {

	RED_LUT("Red Lut", ImageProcessor.RED_LUT), 
	BLACK_AND_WHITE_LUT("Black and White Luti", ImageProcessor.BLACK_AND_WHITE_LUT), 
	NO_LUT_UPDATE("No Lut update", ImageProcessor.NO_LUT_UPDATE),
	OVER_UNDER_LUT("Over under Lut", ImageProcessor.OVER_UNDER_LUT);
	
	private final String displayLutName;
	private final Integer displayLutValue;
	
	Threshold_Lut_Types(String displayLutName, Integer displayLutValue) {
		this.displayLutName = displayLutName;
		this.displayLutValue = displayLutValue;
	}

	public String getDisplayLutName() {
		return displayLutName;
	}

	public Integer getDisplayLutValue() {
		return displayLutValue;
	}

	
}
