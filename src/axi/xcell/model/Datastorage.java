package axi.xcell.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;

import org.w3c.dom.views.AbstractView;

public class Datastorage extends HashMap<String, LinkedList<Datastorage.Item> > implements InterfaceModel{
	private static final long serialVersionUID = 1L;
	private static final NumberFormat NUMBER_FORMAT = new DecimalFormat ("##########.#####");

	public class Item{
		Number value;
		long timestamp;
		public Item() {
			super();
			// TODO Auto-generated constructor stub
		}
		public Item(Number value, long timestamp) {
			super();
			this.value = value;
			this.timestamp = timestamp;
		}
		public Number getValue() {
			return value;
		}
		public void setValue(Number value) {
			this.value = value;
		}
		public long getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		} 
	}

	public axi.xcell.model.Datastorage.Item addItemHelper(String data) {
		Number pdata = null;
		try {
			pdata = NUMBER_FORMAT.parse(data);
		} catch (ParseException e) {
			System.out.println("ERRORE FORMAT NUMBER: "+data);
			e.printStackTrace();
		}
		return new Item(pdata, System.currentTimeMillis());
	}
	
	public LinkedList<Datastorage.Item> addRecordHelper(String data) {
		LinkedList<Datastorage.Item> rec =new LinkedList<Datastorage.Item>();
		rec.add(addItemHelper(data));
		return rec;
	}
	
	public void PRINT_UTILS(){
		System.out.println("--------------------------------------------------------------------------------------");
		this.forEach(
				(key, value1) -> {
					System.out.println("- Key : " + key +" - SIZE: "+value1.size());
					value1.forEach((value2) -> {
						//synchronized(value2) {
						  System.out.println("  " + value2.value+" - Timestamp: "+value2.timestamp);
						//}
					});
				});
	}
}
