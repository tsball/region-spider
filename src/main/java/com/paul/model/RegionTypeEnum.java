package com.paul.model;

public enum RegionTypeEnum {
	
	PROVINCE("province"),   
	CITY("city"),   
	DISTRICT("district"),
	TOWN("town");
	
	public final String value;
	
	RegionTypeEnum(String value){   
		this.value = value;
	}
	
	@Override
    public String toString(){
        return this.value;
    }
	
}
