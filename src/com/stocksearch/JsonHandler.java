package com.stocksearch;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.*;
public class JsonHandler {
	private static String jsonstr;
	static JSONObject obj_result=null;
	
	public JsonHandler(String str){
		jsonstr = str;
	}
	
	public boolean parseJson(){
		JSONObject obj;
		try {
			obj = new JSONObject(jsonstr);
			obj_result = obj.getJSONObject("result");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}
	
	public boolean parseJsonAuto(){
		JSONObject obj;
		try {
			obj = new JSONObject(jsonstr);
			obj_result = obj.getJSONObject("ResultSet");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}
	
	public QuoteInfo getQuote() throws JSONException, MalformedURLException{
		if(this.parseJson()){
			QuoteInfo quote = new QuoteInfo();
			JSONObject obj_quote = (JSONObject) obj_result.get("Quote");
			quote.lastprice = obj_quote.get("LastTradePriceOnly").toString();
			quote.changetype = obj_quote.get("ChangeType").toString();
			quote.name = obj_result.get("Name").toString();
			quote.symbol = obj_result.get("Symbol").toString();
			quote.marketcap = obj_quote.get("MarketCapitalization").toString();
			quote.Ask = obj_quote.get("Ask").toString();
			quote.bid = obj_quote.get("Bid").toString();
			quote.open = obj_quote.get("Open").toString();
			quote.target = obj_quote.get("OneYearTargetPrice").toString();
			quote.volume = obj_quote.get("Volume").toString();
			quote.prevclose = obj_quote.get("PreviousClose").toString();
			quote.change = obj_quote.get("Change").toString();
			quote.avgvol = obj_quote.get("AverageDailyVolume").toString();
			quote.percent = "("+obj_quote.get("ChangeInPercent").toString()+")";
			quote.dayrange = obj_quote.get("DaysLow").toString() +"-"+obj_quote.get("DaysHigh").toString();
			quote.wkrange = obj_quote.get("YearLow").toString() +"-"+obj_quote.get("YearHigh").toString();		
			quote.chart = obj_result.get("StockChartImageURL").toString();
			return quote;
		}else{
			System.out.println("Stock Information not available");
			return null;
		}
	}
	
	public ArrayList<NewsInfo> getNews() throws MalformedURLException, JSONException{
		ArrayList<NewsInfo> newslist = new ArrayList<NewsInfo>();
		if(this.parseJson()){
			
			JSONObject News;
			try {
				News = obj_result.getJSONObject("News");
				JSONArray Items=News.getJSONArray("Item");
				for(int i=0;i<Items.length();i++){
					NewsInfo news = new NewsInfo();
					news.urlstr=((JSONObject)Items.get(i)).get("Link").toString();
					news.link = new URL(((JSONObject)Items.get(i)).get("Link").toString());
					news.headline = ((JSONObject)Items.get(i)).get("Title").toString();	
					
					newslist.add(news);
				}
				return newslist;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				System.out.println("Finance News Not Available");
				return newslist;
			}		
		}else{
			return newslist;
		}
	}
	
	public ArrayList<AutoInfo> getAutoInfo() throws JSONException{
		ArrayList<AutoInfo> auto = new ArrayList<AutoInfo>();
		this.parseJsonAuto();
		JSONArray Results = obj_result.getJSONArray("Result");
		for(int i=0;i<Results.length();i++){
			AutoInfo data = new AutoInfo();
			data.symbol = ((JSONObject)Results.get(i)).get("symbol").toString();
			data.name = ((JSONObject)Results.get(i)).get("name").toString();
			data.code = "("+((JSONObject)Results.get(i)).get("exch").toString()+")";
			auto.add(data);
		}
		return auto;		
	}
	
}
