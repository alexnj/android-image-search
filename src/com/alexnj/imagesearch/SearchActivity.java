package com.alexnj.imagesearch;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class SearchActivity extends Activity {
	EditText etSearchQuery;
	GridView gvResults;
	Button btnSearch;
	ArrayList<ImageResult> irResults = new ArrayList<ImageResult>();
	ImageResultArrayAdapter imageAdapter;
	EndlessScrollListener scrollListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
						
		setupViews();
		imageAdapter = new ImageResultArrayAdapter( this, irResults );
		gvResults.setAdapter( imageAdapter );
		
		gvResults.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View parent, int position, long arg3) {
				Intent i = new Intent(getApplicationContext(), SingleImageActivity.class );
				
				ImageResult imageResult = irResults.get( position );
				i.putExtra ( "image" , imageResult );
				startActivity( i );
			}
			
		});
		
		this.scrollListener = new EndlessScrollListener( ) {
			@Override
		    public void onLoadMore( int start ) {
	        	googleImageSearch( gvResults.getCount() ); 
		    }
        };
        
		gvResults.setOnScrollListener( this.scrollListener );
	}
	
	public void googleImageSearch( int start ) {		
		String query = etSearchQuery.getText().toString();
		String prefSize = PreferenceManager.getDefaultSharedPreferences( this ).getString("size","");
		String prefColor = PreferenceManager.getDefaultSharedPreferences( this ).getString("color","");
		String prefSite = PreferenceManager.getDefaultSharedPreferences( this ).getString("site","");
		String prefType = PreferenceManager.getDefaultSharedPreferences( this ).getString("type","");
		
		if( prefSize != "" ) {
			prefSize = "&imgsz=" + prefSize;
		}
		if( prefType != "" ) {
			prefType = "&imgtype=" + prefType;
		}
		if( prefColor != "" ) {
			prefColor = "&imgcolor=" + prefColor;
		}
		if( prefSite != "" ) {
			prefSite = "&as_sitesearch=" + Uri.encode(prefSite);
		}
		
		AsyncHttpClient client = new AsyncHttpClient();

		client.get("http://ajax.googleapis.com/ajax/services/search/images?rsz=8&start=" + start 
				+ "&v=1.0&q=" + Uri.encode( query ) + prefSize + prefType + prefColor + prefSite, 
			new JsonHttpResponseHandler() {
				@Override
				public void onSuccess( JSONObject response ) {
					JSONArray imageJsonResults = null;
					try {
						int total = response.getJSONObject("responseData").getJSONObject("cursor").getInt("estimatedResultCount");
						scrollListener.setTotal( total );
						imageJsonResults= response.getJSONObject( "responseData" ).getJSONArray("results");
						imageAdapter.addAll(ImageResult.fromJSONArray(imageJsonResults));
					} catch( JSONException e ) {
						e.printStackTrace();
					}
				}
			}
		);
	}

	public void onFilterAction(MenuItem mi) {
		Intent i = new Intent( getApplicationContext(), SettingsActivity.class );
		startActivity(i);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}
	
	public void setupViews() {
		etSearchQuery = (EditText) findViewById(R.id.etSearchQuery);
		gvResults = (GridView) findViewById(R.id.gvResults);
		btnSearch = (Button) findViewById(R.id.btnSearch);
	}
	
	public void onImageSearch(View v) {
		irResults.clear();
		googleImageSearch( 0 );		
	}

}
