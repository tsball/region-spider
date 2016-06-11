package com.paul.model;

public class Region {

	private String name;
	private String firstPinyin;
	private String code; 
	private String parentCode;
	private String type;
	private String childrenUrl;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFirstPinyin() {
		return firstPinyin;
	}
	public void setFirstPinyin(String firstPinyin) {
		this.firstPinyin = firstPinyin;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getParentCode() {
		return parentCode;
	}
	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getChildrenUrl() {
		return childrenUrl;
	}
	public void setChildrenUrl(String childrenUrl) {
		this.childrenUrl = childrenUrl;
	}
	@Override
	public String toString() {
		return "Region [name=" + name + ", firstPinyin=" + firstPinyin
				+ ", code=" + code + ", parentCode=" + parentCode + ", type="
				+ type + "]";
	}
	
}
