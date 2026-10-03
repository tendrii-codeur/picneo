package mg.emit.picneo.tenten.enums;

public enum Threshold_Method_Types {

	DEFAULT("Par défaut", "Default"), 
	HUANG("Huang", "Huang"),
	INTERMODES("Intermodes", "Intermodes"),
	ISODATA("IsoData", "IsoData"), 
	IJ_ISODATA("IJ_IsoData", "IJ_IsoData"), 
	LI("Li", "Li"), 
	MAX_ENTROPY("MaxEntropy", "MaxEntropy"), 
	MEAN("Mean", "Mean"), 
	MIN_ERROR("MinError", "MinError"), 
	MINIMUM("Minimum", "Minimum"),
	MOMENTS("Moments", "Moments"),
	OTSU("Otsu", "Otsu"),
	PERCENTILE("Percentile", "Percentile"),
	RENYI_ENTROPY("RenyiEntropy", "RenyiEntropy"),
	SHANBHAG("Shanbhag", "Shanbhag"),
	TRIANGLE("Triangle", "Triangle"),
	YEN("Yen", "Yen");
	
	private final String displayMethodName;
	private final String apiMethodName;
	
	Threshold_Method_Types(String displayMethodName, String apiMethodName){
		this.displayMethodName = displayMethodName;
		this.apiMethodName = apiMethodName;
	}
	
	public String getDisplayMethodName() {
		return displayMethodName;
	}

	/** Nom anglais exigé par ImageProcessor.setAutoThreshold. */
	public String getApiMethodName() {
		return apiMethodName;
	}

	@Override
	public String toString() {
		return displayMethodName;
	}
}
