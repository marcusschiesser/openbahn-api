package de.marcusschiesser.dbpendler.common.vo;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class StationVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4162834891520852561L;

	private String value;
	private String typeStr;
	private String type;
	private String weight;
	
	public StationVO() {}
	
	public StationVO(String value) {
		super();
		this.value = value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StationVO other = (StationVO) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public String getValue() {
		return value;
	}

	public String getTypeStr() {
		return typeStr;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setTypeStr(String typeStr) {
		this.typeStr = typeStr;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getWeight() {
		return weight;
	}

	@Override
	public String toString() {
		return "StationVO [value=" + value + "]";
	}
	
	
}
