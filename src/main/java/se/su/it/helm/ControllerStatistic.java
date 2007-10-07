package se.su.it.helm;

class ControllerStatistic implements java.io.Serializable {
	public static final long serialVersionUID = -43910120687871968L;
	private String name;
	private String value;
	private String type;
	ControllerStatistic(String name, String value, String type) {
		this.name = name;
		this.value = value;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * 
	 */
	
	public int hashCode() {
		return name.hashCode();
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ControllerStatistic)) {
			return false;
		}
		ControllerStatistic other = (ControllerStatistic)object;
		if (!name.equals(other.name)) {
			return false;
		}
		if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

};

