package mg.emit.picneo.tenten.enums;

import ij.plugin.filter.RankFilters;

public enum Process_Filter_Types {

	MEAN("Mean", RankFilters.MEAN), 
	MIN("Min", RankFilters.MIN), 
	MAX("Max", RankFilters.MAX),
	VARIANCE("Variance", RankFilters.VARIANCE),
	MEDIAN("Median", RankFilters.MEDIAN);
	
	private final String displayFilterName;
	private final Integer displayFilterValue;
	
	Process_Filter_Types(String displayFilterName, Integer displayFilterValue) {
		this.displayFilterName = displayFilterName;
		this.displayFilterValue = displayFilterValue;
	}

	public String getDisplayFilterName() {
		return displayFilterName;
	}

	public Integer getDisplayFilterValue() {
		return displayFilterValue;
	}
}
