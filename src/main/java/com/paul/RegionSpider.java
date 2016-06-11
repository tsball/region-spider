package com.paul;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Lists;
import com.paul.model.Region;
import com.paul.model.RegionTypeEnum;

public class RegionSpider {
	
    private static final String CODE_RULE = "^-?[1-9]\\d*$";
    
    private Document url2Doc(String url) throws IOException {
    	//For <= 2013
        // return Jsoup.connect(url).timeout(600 * 1000)
        // .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36").get();
    	//return Jsoup.connect(url).timeout(600 * 1000).get();
    	
    	//For = 2014
    	return Jsoup.parse(new URL(url).openStream(), "gbk", url);
    }

    /**
     * Get province list
     */
    public List<Region> getProvinces(String provinceCatalogUrl) throws IOException {
        List<Region> provinceList = Lists.newArrayList();
        Document doc = url2Doc(provinceCatalogUrl);
        Elements provinceRowElems = doc.getElementsByAttributeValue("class", "provincetr");
        for (Element provinceRowElem : provinceRowElems) {
            Elements tds = provinceRowElem.select("a[href]");
            for (Element element : tds) {
            	String childrenUri = element.attr("href");
            	String childrenUrl = element.attr("abs:href");
                Region region = new Region();
                region.setCode(childrenUri.substring(0, 2) + "0000000000");
                region.setName(element.text().replaceAll("<br />", ""));
                region.setType(RegionTypeEnum.PROVINCE.toString());
                region.setParentCode("");
                region.setChildrenUrl(childrenUrl);
                
                provinceList.add(region);
            }
        }
        return provinceList;
    }
    
    /**
     * Get cities under specified province
     */
    public List<Region> getCities(Region province) throws IOException {
        List<Region> cityList = Lists.newArrayList();
        Document doc = url2Doc(province.getChildrenUrl());
        Elements trElems = doc.select(".citytr");
        for (Element trElem : trElems) {
            Elements tdElems = trElem.select("td");
            Element codeTdElement = tdElems.get(0);
            Element nameTdElement = tdElems.get(1);
            
            Region region = new Region();
            region.setCode(codeTdElement.text());
            region.setName(nameTdElement.text());
            region.setParentCode(province.getCode());
            region.setType(RegionTypeEnum.CITY.toString());
            Element hrefElem = nameTdElement.select("a").first();
            if (hrefElem != null) {
            	region.setChildrenUrl(hrefElem.attr("abs:href"));
            }
            cityList.add(region);
        }
        return cityList;
    }

    /**
     * Get the districts under specified city
     * Note: dongguan,zhongshan... 5 cities do not have districts, but they have the towns. so for this region, need to set a default district.
     */
    public List<Region> getDistricts(Region city) throws IOException {
        List<Region> districtList = Lists.newArrayList();
        
        if (city.getChildrenUrl() == null) {
        	return districtList;
        }
        
        Document doc = url2Doc(city.getChildrenUrl());
        Elements districtRowElems = doc.select(".countytr");
        for (Element districtRowElem : districtRowElems) {
            Elements tds = districtRowElem.select("a[href]");
            if (tds.size() == 0) continue; // For 市辖区 
            Element codeTdElement = tds.get(0);
            Element nameTdElement = tds.get(1);
            
            Region region = new Region();
            region.setCode(codeTdElement.text());
            region.setName(nameTdElement.text());
            region.setParentCode(city.getCode());
            region.setType(RegionTypeEnum.DISTRICT.toString());
            
            Element hrefElem = nameTdElement.select("a").first();
            if (hrefElem != null) {
            	region.setChildrenUrl(hrefElem.attr("abs:href"));
            }
            districtList.add(region);
        }
        
        return districtList;
    }

    /**
     * Get towns under specified district
     * 
     * Note: some districts may have no towns, for example "市辖区" is district without any town.
     */
    public List<Region> getTowns(Region district) throws IOException {
        List<Region> townList = Lists.newArrayList();
        
        if (district.getChildrenUrl() == null) {
        	return townList;
        }
        
        Document doc = url2Doc(district.getChildrenUrl());
        Elements townRowElems = doc.select(".towntr");
        for (Element townRowElem : townRowElems) {
            Elements tds = townRowElem.select("a[href]");
            if (tds.size() == 0) continue; // For part of the town does not have any village
            Element codeTdElement = tds.get(0);
            Element nameTdElement = tds.get(1);

            Region region = new Region();
            region.setCode(codeTdElement.text());
            region.setName(nameTdElement.text());
            region.setParentCode(district.getCode());
            region.setType("town");
            
            Element hrefElem = nameTdElement.select("a").first();
            if (hrefElem != null) {
            	region.setChildrenUrl(hrefElem.attr("abs:href"));
            }
            
            townList.add(region);
        }
        return townList;
    }

    /**
     * Get the villages under specified town
     */
    @Deprecated
    public List<Region> getVillages(Region town) throws IOException {
        List<Region> list = Lists.newArrayList();
        Document doc = url2Doc(town.getChildrenUrl());
        Elements villageRowElems = doc.getElementsByAttributeValue("class", "villagetr");
        for (Element villageRowElem : villageRowElems) {
            Elements trs = villageRowElem.select("tr");
            for (Element element : trs) {
                Elements tds = element.select("td");
                Region region = new Region();
                for (Element element2 : tds) {
                    String value = element2.text();
                    if (isMatchedRegionCode(value) && value.length() == 3) {
                        region.setType(element2.text());
                    }
                    if (isMatchedRegionCode(value) && value.length() > 3) {
                        region.setCode(value);
                        region.setParentCode(value.substring(0, 9));
                    } else {
                        region.setName(value);
                    }

                }
                list.add(region);

            }
        }
        return list;
    }
    
    private boolean isMatchedRegionCode(String value) {
        return match(CODE_RULE, value);
    }

    private boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    
}