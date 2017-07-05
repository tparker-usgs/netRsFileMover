package gov.usgs.volcanoes.netRSFileMover;

public enum ReceiverType {

	NETRS("NetRS"), 
	NETR9("NetR9");

	private String typeString;

	private ReceiverType(String typeString) {
		this.typeString = typeString;
	}

	public static ReceiverType parse(String typeString) {
		for (ReceiverType r : ReceiverType.values())
			if (r.typeString.equals(typeString))
				return r;

		return null;
	}

	public String getTypeString() {
		return typeString;
	}

	public String toString() {
		return typeString;
	}
	
}
