package mg.emit.picneo.tenten.enums;

public enum Threshold_Background_Types {

	TRUE("Fond sombre", true),
	FALSE("Fond clair", false);
	
	private final String displayBackgroundLabel;
	private final Boolean displayBackgroundName;
	
	Threshold_Background_Types(String displayBackgroundLabel, Boolean displayBackgroundName){
		this.displayBackgroundLabel = displayBackgroundLabel;
		this.displayBackgroundName = displayBackgroundName;
	}
	
	public Boolean getDisplayBackgroundName() {
		return displayBackgroundName;
	}

	@Override
	public String toString() {
		return displayBackgroundLabel;
	}
}
