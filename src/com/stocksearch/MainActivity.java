package com.stocksearch;
/**
* @author Kang Wang
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;

import org.json.JSONException;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.model.*;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;

public class MainActivity extends ActionBarActivity {
	
	static AutoCompleteTextView autocomplete;
	static Button facebook, news, search;
	static TextView notavailable;
	static TextView PrevClose, Open, Bid, Ask, Target, DayRange, wkRange, Volume, AvgVol, MarketCap;
	static TextView title, lastprice, qoute_change;
	static ImageView updown, StockChart;
	static TableLayout tb1, tb2;
	static String JsonString="";
	static ArrayList<AutoInfo> autolist = new ArrayList<AutoInfo>();
	static Bitmap bm;
	static ArrayList<NewsInfo> NewsList = new ArrayList<NewsInfo>();
	private static final String URL_PATTERN = "http://cs-server.usc.edu:25560/examples/servlet/finance_search?symbol=";
	private static final String AUTO_LINK_PART1 = "http://autoc.finance.yahoo.com/autoc?query=";
	private static final String AUTO_LINK_PART2 ="&callback=YAHOO.Finance.SymbolSuggest.ssCallback";
	private static final String YHOO_FINANCE_URL = "http://finance.yahoo.com/q;_ylt=Ap13wEp4dL4nrAMb40E7m_yiuYdG;"
			+ "_ylu=X3oDMTBxdGVyNzJxBHNlYwNVSCAzIERlc2t0b3AgU2VhcmNoIDEx;_ylg=X3oDMTBsdWsyY2FpBGxhbmcDZW4tVVMEcHQDMgR0ZXN0Aw--;"
			+ "_ylv=3?uhb=uhb2&fr=uh3_finance_vert_gs&type=2button&s=";
	private static final String DEBUG_TAG = "HttpExample";
	private static Bundle savedState=null;
	private static boolean isFaceBook_Button_Clicked = false;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private static String feedname, feedsymbol, feedlink, feedprice, feedchange, feedpercent;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.fragment_main);
		
		savedState = savedInstanceState;
		
		//Get View Components by ID		
		notavailable = (TextView) findViewById(R.id.NotAvailbale);
		
		tb1 = (TableLayout) findViewById(R.id.tableLayout1);
		tb2 = (TableLayout) findViewById(R.id.tableLayout2);
		title = (TextView) findViewById(R.id.company_title);
		lastprice = (TextView) findViewById(R.id.qoute_LastTradePriceOnly);
		qoute_change = (TextView) findViewById(R.id.qoute_change);
		
		autocomplete = (AutoCompleteTextView) findViewById(R.id.searchbox);
		search = (Button) findViewById(R.id.search_button);
		facebook = (Button) findViewById(R.id.facebook_button);
		news = (Button) findViewById(R.id.news_button);
		updown = (ImageView) findViewById(R.id.arrow);
		StockChart = (ImageView) findViewById(R.id.stockchart);
		PrevClose = (TextView) findViewById(R.id.row1_col2);
		Open = (TextView) findViewById(R.id.row2_col2);
		Bid = (TextView) findViewById(R.id.row3_col2);
		Ask = (TextView) findViewById(R.id.row4_col2);
		Target = (TextView) findViewById(R.id.row5_col2);
		DayRange = (TextView) findViewById(R.id.row6_col2);
		wkRange = (TextView) findViewById(R.id.row7_col2);
		Volume = (TextView) findViewById(R.id.row8_col2);
		AvgVol = (TextView) findViewById(R.id.row9_col2);
		MarketCap = (TextView) findViewById(R.id.row10_col2);

		autocomplete.setThreshold(1);
		autocomplete.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				System.out.println(s);
				new RetrieveAutoList().execute(AUTO_LINK_PART1+s.toString()+AUTO_LINK_PART2);
			}
			
		});
		
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedState != null) {
                session = Session.restoreSession(MainActivity.this, null, statusCallback, savedState);
            }
            if (session == null) {
                session = new Session(MainActivity.this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(MainActivity.this).setCallback(statusCallback));
            }
        }
		
		setShowPanelInvisiable(false);
		setErrorShow(false);
				
	}
	
	public void newsOnClick(View v){
//		Toast.makeText(MainActivity.this, "This is News Headlines Button",
//				Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(MainActivity.this, NewsActivity.class);	
		Bundle NewsData = new Bundle();
		NewsData.putSerializable("NEWS", NewsList);
//		intent.putExtra("NEWS", NewsList);
		intent.putExtras(NewsData);
		intent.setClass(MainActivity.this, NewsActivity.class);
		startActivity(intent);
	}
	
	public void fbOnClick(View v){
		isFaceBook_Button_Clicked = true;
		Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(MainActivity.this).setCallback(statusCallback));
        } else {
            Session.openActiveSession(MainActivity.this, true, statusCallback);
        }
	}
		
	
    @Override
    public void onStart() {
        super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

	
	
	public void searchOnClick(View v){
		ConnectivityManager connMgr = (ConnectivityManager) 
		        getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		    
		    if (networkInfo != null && networkInfo.isConnected()) {			    	
		    	setShowPanelInvisiable(false);
				setErrorShow(false);
				if(autocomplete.getText().toString().equals("")){
					Toast.makeText(MainActivity.this, "Company Symbol Can not be Null!",
							Toast.LENGTH_SHORT).show();	
				}else{
					// fetch data
			    	new AjaxRequests().execute(URL_PATTERN+autocomplete.getText().toString());
//			    	new RetrieveAutoList().execute(AUTO_LINK_PART1+autocomplete.getText().toString()+AUTO_LINK_PART2);
				}
		    	
		    } else {
		    	Toast.makeText(MainActivity.this, "Error: Network is not available now.",
						Toast.LENGTH_SHORT).show();
		    }
	}
	
	
	private void publishFeedDialog() {
	    Bundle params = new Bundle();
	    params.putString("name", feedname);
	    params.putString("caption", "Stock Information of "+feedname+"("+feedsymbol+")");
	    params.putString("description", "Last Trade Price: "+feedprice+","+"Change:"+feedchange+feedpercent);
	    params.putString("link", YHOO_FINANCE_URL+feedsymbol);
	    params.putString("picture", feedlink);

	    WebDialog feedDialog = (
	        new WebDialog.FeedDialogBuilder(MainActivity.this,
	            Session.getActiveSession(),
	            params))
	        .setOnCompleteListener(new OnCompleteListener() {

	            @Override
	            public void onComplete(Bundle values,
	                FacebookException error) {
	                if (error == null) {
	                    // When the story is posted, echo the success
	                    // and the post Id.
	                    final String postId = values.getString("post_id");
	                    if (postId != null) {
	                        Toast.makeText(MainActivity.this,
	                            "Posted story, id: "+postId,
	                            Toast.LENGTH_SHORT).show();
	                    } else {
	                        // User clicked the Cancel button
	                        Toast.makeText(MainActivity.this.getApplicationContext(), 
	                            "Publish cancelled", 
	                            Toast.LENGTH_SHORT).show();
	                    }
	                } else if (error instanceof FacebookOperationCanceledException) {
	                    // User clicked the "x" button
	                    Toast.makeText(MainActivity.this.getApplicationContext(), 
	                        "Publish cancelled", 
	                        Toast.LENGTH_SHORT).show();
	                } else {
	                    // Generic, ex: network error
	                    Toast.makeText(MainActivity.this.getApplicationContext(), 
	                        "Error posting story", 
	                        Toast.LENGTH_SHORT).show();
	                }
	            }

	        })
	        .build();
	    feedDialog.show();
	}
	
	public Context getActivity(){
		return MainActivity.this;
	}
	
	public static void setQuote(QuoteInfo quote) throws MalformedURLException, JSONException{
		title.setText(quote.name+"("+quote.symbol+")");
		feedname = quote.name;
		feedsymbol = quote.symbol;
		feedchange = quote.change;
		feedprice = quote.lastprice;
		feedpercent = quote.percent;
		feedlink = quote.chart;
		lastprice.setText(quote.lastprice);
		if(quote.changetype.equals("+")){
			qoute_change.setTextColor(Color.GREEN);
			updown.setImageResource(R.drawable.green_up_arrow);
		}else if(quote.changetype.equals("-")){
			qoute_change.setTextColor(Color.RED);
			updown.setImageResource(R.drawable.red_down_arrow);
		}
		qoute_change.setText(quote.change+quote.percent);
		//PrevClose, Open, Bid, Ask, Target, DayRange, wkRange, Volume, AvgVol, MarketCap;
		PrevClose.setText(quote.prevclose);
		Open.setText(quote.open);
		Bid.setText(quote.bid);
		Ask.setText(quote.Ask);
		Target.setText(quote.target);
		DayRange.setText(quote.dayrange);
		wkRange.setText(quote.wkrange);
		Volume.setText(quote.volume);
		AvgVol.setText(quote.avgvol);
		MarketCap.setText(quote.marketcap);	
		
		setErrorShow(false);
		setShowPanelInvisiable(true);
		
//		news.setVisibility(0);
		if(NewsList.size()==0||NewsList==null){
			news.setVisibility(4);
		}
	}
	
	public static void setErrorShow(boolean visibility){
		if(visibility){
			notavailable.setVisibility(0);
		}else{
			notavailable.setVisibility(8);
		}
	}
	
	public static void setShowPanelInvisiable(boolean visibility){
		if(visibility){
			tb1.setVisibility(0);
			tb2.setVisibility(0);
			title.setVisibility(0);
			lastprice.setVisibility(0);
			facebook.setVisibility(0);
			news.setVisibility(0);
			StockChart.setVisibility(0);
		}else{
			tb1.setVisibility(8);
			tb2.setVisibility(8);
			title.setVisibility(8);
			lastprice.setVisibility(8);
			facebook.setVisibility(8);
			news.setVisibility(8);
			StockChart.setVisibility(8);
		}
	}
	
	public static String getCallbackJson(String callback){
		return callback.substring(39, callback.length()-1);
	} 
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	private class AjaxRequests extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
              
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
//           System.out.println(result);
        	if(result!=null&&result!=""){
        		JsonString = result;
        		try {
					QuoteInfo quote = this.getJsonHandler().getQuote();
					if(quote==null){
						setErrorShow(true);
						setShowPanelInvisiable(false);
						System.out.println("Stock Information not available");
					}else{
						new RetrieveImage().execute(quote.chart);
						NewsList.clear();
						NewsList = this.getJsonHandler().getNews();
						setQuote(quote);
					}
				} catch (MalformedURLException | JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}else{
        		System.out.println("Null Json String");
        	}
       }
        
        public JsonHandler getJsonHandler(){
        	JsonHandler jsonhandler = new JsonHandler(JsonString);
        	return jsonhandler;
        }
    }
	
	private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is);
            return contentAsString;
            
        // Makes sure that the InputStream is closed after the app is
        // finished using it.
        } finally {
            if (is != null) {
                is.close();
            } 
        }
    }
	
	public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
    	BufferedReader in = new BufferedReader(new InputStreamReader(stream));
    	String inputLine, XMLinput = "";
    	while ((inputLine = in.readLine()) != null){
              XMLinput += inputLine;
            }
    	stream.close();
    	return XMLinput; 
    }
	
	private class RetrieveImage extends AsyncTask<String, Void, Bitmap>{
		protected Bitmap doInBackground(String... urls) {
            Bitmap bmp = null;
            try {
                bmp = this.loadImageFromURL(urls[0]);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return bmp;
        }
        
        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            StockChart.setImageBitmap(result);
        	}
        
        private Bitmap loadImageFromURL(String imageUrl) throws IOException {
            URL url = new URL(imageUrl);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setDoInput(true);
            con.connect();
            InputStream inputStream = con.getInputStream();
            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
            return bmp;
        }
        
	}
	
   private class RetrieveAutoList extends AsyncTask<String, Void, String>{
	   String JsonStr="";
	   @Override
       protected String doInBackground(String... urls) {
             
           // params comes from the execute() call: params[0] is the url.
           try {
               return downloadUrl(urls[0]);
           } catch (IOException e) {
               return "Unable to retrieve web page. URL may be invalid.";
           }
       }
       // onPostExecute displays the results of the AsyncTask.
       @Override
       protected void onPostExecute(String result) {
    	   //Handling AutoComplete List Data here
    	   
    	   autolist.clear();
    	   this.JsonStr = getCallbackJson(result);
	   try {
    		   
			autolist = this.getJsonHandler().getAutoInfo();
			String[] items = this.getStringArray(autolist);
			this.setAutoAdapter(items);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//    	   System.out.println(getCallbackJson(result));
      }
       
       public JsonHandler getJsonHandler(){
	       	JsonHandler jsonhandler = new JsonHandler(this.JsonStr);
	       	return jsonhandler;
       }
              
       public String[] getStringArray(ArrayList<AutoInfo> auto){
    	   String[] items = new String[autolist.size()];
    	   for(int i=0;i<auto.size();i++){
				AutoInfo cur = auto.get(i);
				items[i]=cur.symbol+","+cur.name+","+cur.code;
			}
    	   return items;
       }
       
       public void setAutoAdapter(final String[] items){
    	   ArrayAdapter<String> newadapter = new ArrayAdapter<String>(MainActivity.this, 
    			   android.R.layout.simple_dropdown_item_1line, items);
    	   newadapter.notifyDataSetChanged();
    	   autocomplete.setThreshold(1);
    	   autocomplete.setAdapter(newadapter);
    	   autocomplete.setThreshold(1);
    	   newadapter.notifyDataSetChanged();
    	   autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				String[] data = items[pos].split(",");
				autocomplete.setText(data[0]);
				searchOnClick(search);
			}   		   
    	   });
       }

   }
   
   private class SessionStatusCallback implements Session.StatusCallback {
	   @Override
	    public void call(final Session session, final SessionState state, final Exception exception) {
		   
	        if (state.isOpened()&&isFaceBook_Button_Clicked) {
	            String facebookToken = session.getAccessToken();
	            Log.i("MainActivityFaceBook", facebookToken);
	            Request.newMeRequest(session, new Request.GraphUserCallback() {

	                @Override
	                public void onCompleted(GraphUser user,
	                        com.facebook.Response response) {
	                    publishFeedDialog();
	                }
	            }).executeAsync();
	            isFaceBook_Button_Clicked = false;
	        }else{
	        	System.out.println("Session is not open!");
	        }
	    }
   }

}
