package com.paul;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.google.common.collect.Lists;
import com.paul.helper.CsvFileWriterHelper;
import com.paul.model.Region;
import com.paul.model.RegionTypeEnum;

public class App {
	
	private static int index = 0;

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
		// setting
		String filePath = "d:\\area.csv";
		String provinceCatalogUrl = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2014/";
		
		// Collect
        List<Region> regionList = Lists.newArrayList();
        
        RegionSpider regionSpider = new RegionSpider();
        // 1. get provinces
        List<Region> provinceList = regionSpider.getProvinces(provinceCatalogUrl);
        for (Region province : provinceList) {
        	regionList.add(getRegionWithFirstPinyin(province));
        	println(province);
        	
        	// 2. get cities
        	List<Region> cityList = regionSpider.getCities(province);
        	for (Region city : cityList) {
        		regionList.add(getRegionWithFirstPinyin(city));
        		println(city);
        		
        		// 3. get districts
        		List<Region> districtList = regionSpider.getDistricts(city);
        		// For the cities which do not have districts but have towns, we need to set a default districts
        		if (districtList.size() == 0) {
                	Region region = new Region();
                    region.setCode(city.getCode());
                    region.setName(city.getName());
                    region.setParentCode(city.getCode());
                    region.setType(RegionTypeEnum.DISTRICT.toString());
                    region.setChildrenUrl(city.getChildrenUrl());
                    districtList.add(region);
                }
        		
        		for (Region district : districtList) {
        			regionList.add(getRegionWithFirstPinyin(district));	
        			println(district);
        			
        			// 4. get towns
        			List<Region> townList = regionSpider.getTowns(district);
                    for (Region town : townList) {
                    	regionList.add(getRegionWithFirstPinyin(town));
                    	println(town);
                    }
        		}
        	}
        }
        
        //write to csv file
      	System.out.println("Writing CSV file:");
      	Object[] columnNames = {"Name","First Pinyin","Code","Parent Code","Type"};
      	String[] columnFields = {"name","firstPinyin","code","parentCode","type"};
        CsvFileWriterHelper.writeCsvFile(filePath, columnNames, columnFields, regionList);
        System.out.println("Writing CSV Done!");

    }
	
	private static Region getRegionWithFirstPinyin(Region region) {
		final String firstStrOfName = region.getName().substring(0, 1); 
		region.setFirstPinyin(PinyinHelper.getShortPinyin(firstStrOfName).substring(0, 1).toUpperCase());
		return region;
	}
	
	private static void println(Region region) {
		System.out.println(String.format("%d [%s] %s : %s", ++index, region.getType(), region.getCode(), region.getName()));
	}
	
}
