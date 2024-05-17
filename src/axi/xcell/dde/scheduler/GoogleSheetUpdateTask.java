package axi.xcell.dde.scheduler;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import axi.apis.GoogleSheetApiManager;
import axi.xcell.model.ConfigItemsListnerList;
import axi.xcell.model.Datastorage;
import axi.xcell.model.Datastorage.Item;

public class GoogleSheetUpdateTask extends TimerTask {

	NumberFormat NUMBER_FORMAT = new DecimalFormat ("##########.#####");
	GoogleSheetApiManager googleSheetApiManager = null;
	ConfigItemsListnerList configItemsListnerList = null;
	Datastorage datastorage = null;
	int hashCodeOfValues = 0;
	String sheetOutputRange = null;
	int x_DEBUG= 0;
	
	public GoogleSheetUpdateTask(
			GoogleSheetApiManager googleSheetApiManager,
			ConfigItemsListnerList configItemsListnerList,
			Datastorage datastorage,
			String sheetOutputRange) {
		super();
		this.googleSheetApiManager = googleSheetApiManager;
		this.configItemsListnerList = configItemsListnerList;
		this.datastorage = datastorage;
		this.sheetOutputRange = sheetOutputRange;
		
	}

	@Override
	public void run() {
//		List<List<Object>> newValues = new ArrayList<List<Object>>();
//		List<Object> val = new ArrayList<Object>();
//		for(int i=2;i<7;i++) {
//			val.add("B"+i);
//		}
//		newValues.add(val);

		List<List<Object>> newValues2 = new ArrayList<List<Object>>();
//	    List<List<Object>> newValues2 = Arrays.asList(
//	            Arrays.asList("B2"),
//	            Arrays.asList("B3"),
//	            Arrays.asList(x_DEBUG<3?x_DEBUG++:x_DEBUG),
//	            Arrays.asList("B5"),
//	            Arrays.asList("B6"),
//	            Arrays.asList("B7")
//	            );
	    
		
		// DEBUG
//		String key = "ESM4.CME;last";
//		if(datastorage.containsKey(key)) {
//			datastorage.get(key).add(datastorage.addItemHelper(""+x_DEBUG++));
//		} else {
//			datastorage.put(key, datastorage.addRecordHelper(""+x_DEBUG++));
//		}
		
	    configItemsListnerList.forEach((i)->{
	    	synchronized(datastorage){
	    		LinkedList<Item> ilist = datastorage.get("Q-"+i.getItemKey());
	    		if(ilist!=null && ilist.size()>0){

	    			String 
	    			itemKey = i.getItemKey(),
	    			pdata = (""+ilist.getLast().getValue()).replace(",", "."),
	    			ticValue = i.getTicValue(),
	    			moltiplicatore = i.getMoltiplicatore(),
	    			descrizione = i.getDescrizione(),
	    			sheetCellLocation = i.getSheetCellLocation();

	    			newValues2.add(Arrays.asList(
	    					itemKey,			// B
	    					pdata,				// C
	    	    			ticValue,			// D
	    	    			moltiplicatore, 	// E
	    	    			descrizione,		// F
	    	    			sheetCellLocation	// G
	    					));

	    		}
	    	}
	    });
	    
		try {
			if(hashCodeOfValues != newValues2.hashCode()) {
				hashCodeOfValues = newValues2.hashCode();
				googleSheetApiManager.writeRangeCells(sheetOutputRange, newValues2);
				System.out.println("GoogleSheetUpdateTask : update data - "+hashCodeOfValues);
			} else {
				System.out.println("GoogleSheetUpdateTask : Unchanged data - bypass udpate! ");
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
