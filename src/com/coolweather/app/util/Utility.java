package com.coolweather.app.util;

import android.text.TextUtils;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

public class Utility {
	
	/**
	 * 解析服务器返回的省级数据并且存入数据库
	 */
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,
			String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("|");//判断array数组是否为空？
					Province province = new Province();
					province.setProvinceName(array[1]);
					province.setPrivinceCode(array[0]);
					
					coolWeatherDB.saveProvice(province);
				}
			}
			return true;
		}
		
		return false;

	}
	
	
	/**
	 * 解析服务器返回的市级数据并且存入数据库
	 */
	public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,
			String response, int provinceId){
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String p : allCities) {
					String[] array = p.split("|");//判断array数组是否为空？
					City city = new City();
					city.setCityName(array[1]);
					city.setCityCode(array[0]);
					city.setProvinceId(provinceId);
					
					coolWeatherDB.saveCity(city);
				}
			}
			return true;
		}
		
		return false;

	}
	
	
	/**
	 * 解析服务器返回的县级数据并且存入数据库
	 */
	
	public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,
			String response, int cityId){
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String p : allCounties) {
					String[] array = p.split("|");//判断array数组是否为空？
					County county = new County();
					county.setCountyName(array[1]);
					county.setCountyCode(array[0]);
					county.setCityId(cityId);
					
					coolWeatherDB.saveCounty(county);
				}
			}
			return true;
		}
		
		return false;
	}
		

}
