package com.coolweather.app;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ChooseAreaActivity extends Activity {
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private boolean isFromWeatherActivity;
	
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	
	private List<String> dataList = new ArrayList<String>();
	
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	
	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;
	
	private int currentLevel;
	
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
        
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        
        listView = (ListView)findViewById(R.id.list_view);
        titleText = (TextView)findViewById(R.id.title_text);
        
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        //coolWeatherDB.deleteDB(this);
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub 
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(arg2);
					queryCities(); 
				} else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(arg2);
					
					//Log.i("++++++++++++++++", String.valueOf(selectedCity.getId()));
					
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countyList.get(arg2).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
        	
		});
        
        queryProvinces();
        
    }
    
    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			Log.i("ChooseAreaActivity", String.valueOf(provinceList.size()));
			dataList.clear();
			for(Province province : provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");

		}
	}
    
    /**
     * 查询某省下的所有市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    
    private void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		
		if (cityList.size() > 0) {
			dataList.clear();
			for(City city : cityList){
				
				Log.i("++++++++++++++++", String.valueOf(city.getId()));
				Log.i("----------------", city.getCityName());
				dataList.add(city.getCityName());
				
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
    
    /**
     * 查询某市下的所有县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties() {
		countyList =  coolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for(County county : countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
    

    /**
     * 根据传入的代号和类型从服务器上查询省市县数据
     */
    
    private void queryFromServer(final String code, final String type) {
	   String address = null;
	   if (!TextUtils.isEmpty(code)) {
		   address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
	   } else {
		   address = "http://www.weather.com.cn/data/list3/city.xml";
	   }
	   showProgressDialog();
	   HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
		
		@Override
		public void onFinish(String response) {
			// TODO Auto-generated method stub
			Log.i("ChooseAreaActivity", "onFinish");
			boolean result =  false;
			if ("province".equals(type)) {
				result = Utility.handleProvincesResponse(coolWeatherDB, response);
			} else if ("city".equals(type)) {
				result = Utility.handleCitiesResponse(coolWeatherDB, 
						response, selectedProvince.getId());
			} else if ("county".equals(type)) {
				//有问题
				result = Utility.handleCountiesResponse(coolWeatherDB, 
						response, selectedCity.getId());
			} else {
				
			}
			
			if (result) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						if ("province".equals(type)) {
							queryProvinces();
						} else if ("city".equals(type)) {
							queryCities();
						} else if ("county".equals(type)) {
							queryCounties();
						} else {
							//
						}
						
					}
				});
			}else {
				Log.i("ChooseAreaActivity", "response is null");
			}
			
		}
		
		@Override
		public void onError(Exception e) {
			// TODO Auto-generated method stub
			Log.i("ChooseAreaActivity", "On Error...request error");
		}
	});
	
    } 
    

   private void showProgressDialog() {
	   
	   if (progressDialog == null) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("正在加载...");
		progressDialog.setCanceledOnTouchOutside(false);
	   }
	   progressDialog.show();
   }
    
   
   	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
    
 	
    @Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryCities();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
