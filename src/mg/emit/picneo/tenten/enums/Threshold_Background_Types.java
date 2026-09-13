package mg.emit.picneo.tenten.enums;

public enum Threshold_Background_Types {

	TRUE(true),
	FALSE(false);
	
	private final Boolean displayBackgroundName;
	
	Threshold_Background_Types(Boolean displayBackgroundName){
		this.displayBackgroundName = displayBackgroundName;
	}
	
	public Boolean getDisplayBackgroundName() {
		return displayBackgroundName;
	}
}
