package com.stocksearch;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewsActivity extends Activity{
	
	ArrayAdapter<String> adapter;
	ArrayList<String> listItems=new ArrayList<String>();
	ArrayList<NewsInfo> news = new ArrayList<NewsInfo>();
	static ListView listview;
	static TextView errorinfo;
	final static String[] choices= new String[] { "View", "Cancel"};
	static int pos;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_layout);
		listview = (ListView) findViewById(R.id.list);
		errorinfo = (TextView) findViewById(R.id.newsNotAvailable);
		setErroShow(false);
		setListShow(false);
		
		news = (ArrayList<NewsInfo>) getIntent().getSerializableExtra("NEWS");
		
		if(news==null){
			
			setErroShow(true);
			setListShow(false);
			
		}else{		
			
			Toast.makeText(NewsActivity.this, "Showing "+news.size()+" headlines",
					Toast.LENGTH_SHORT).show();
			setErroShow(false);
			listItems.clear();
			
			for(int i=0;i<news.size();i++){
				listItems.add(news.get(i).headline);
			}
			
			adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,listItems);
			listview.setAdapter(adapter);
	        setListShow(true);
		}
		
		listview.setOnItemClickListener(new OnItemClickListener() {
			
			@Override 
		    public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
		    { 
				pos = position;
//				Toast.makeText(NewsActivity.this, "You selected "+position,
//						Toast.LENGTH_SHORT).show();
				Builder builder = new AlertDialog.Builder(NewsActivity.this);
				builder.setTitle("View News");					
				builder.setItems(choices, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						switch(which){
						case 0:
//							Toast.makeText(NewsActivity.this, "You selected View",
//							Toast.LENGTH_SHORT).show();
							Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.get(pos).urlstr));
							startActivity(browserIntent);
							break;
						case 1:
//							Toast.makeText(NewsActivity.this, "You selected Cancel",
//							Toast.LENGTH_SHORT).show();
							break;
						}
					
					}
				});

				builder.create().show();
				
		    }
		});
	}
	
	public static void setErroShow(boolean is){
		if(is){
			errorinfo.setVisibility(0);
		}else{
			errorinfo.setVisibility(8);
		}
	}
	
	public static void setListShow(boolean is){
		if(is){
			listview.setVisibility(0);
		}else{
			listview.setVisibility(8);
		}
	}
}
