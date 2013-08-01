package com.gat3way.airpirate;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.util.Log;
import com.actionbarsherlock.app.SherlockListFragment;
public class SettingsFragmentChannels extends SherlockListFragment
{
	private Band band;
    private String str_options[];
    private boolean str_options_enabled[];
    public boolean checkaction=true; 

    
    public void setSettings()
    {
    	for (int cnt=0;cnt<str_options.length;cnt++)
    	{
    		str_options_enabled[cnt]=checkaction;
    	}   
    	band.setChannelsEnabled(str_options_enabled);
    }    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
		band = Band.instance();
    	str_options = band.getChannels();
		str_options_enabled = band.getChannelsEnabled();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_list_item_multiple_choice, str_options);
		setListAdapter(adapter);
    	return super.onCreateView(inflater, container, savedInstanceState);
    }
    @Override
    public void onListItemClick (ListView l, View v, int position, long id)
    {
    	int cnt;
    	
    	for (cnt=0;cnt<str_options.length;cnt++)
    	{
    		str_options_enabled[cnt]=getListView().isItemChecked(cnt);
    	}
    	band.setChannelsEnabled(str_options_enabled);
    }
    
    @Override
    public void onStart() 
    {
        super.onStart();
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		for (int cnt=0;cnt<str_options.length;cnt++)
		{
			getListView().setItemChecked(cnt,str_options_enabled[cnt]);
		}        
    }
}