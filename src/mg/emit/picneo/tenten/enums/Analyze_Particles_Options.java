package mg.emit.picneo.tenten.enums;

import ij.plugin.filter.ParticleAnalyzer;

public enum Analyze_Particles_Options {

	ROI_MASKS("Masques ROI", ParticleAnalyzer.SHOW_ROI_MASKS),
	OUTLINES("Contours", ParticleAnalyzer.SHOW_OUTLINES),
	MASKS("Masques", ParticleAnalyzer.SHOW_MASKS);
	
	private final String displayOptionName;
	private final Integer displayOptionValue;
	
	private Analyze_Particles_Options(String displayOptionName, Integer displayOptionValue) {
		this.displayOptionName = displayOptionName;
		this.displayOptionValue = displayOptionValue;
	}
	
	public String getDisplayOptionName() {
		return displayOptionName;
	}
	public Integer getDisplayOptionValue() {
		return displayOptionValue;
	}
	
	
}
