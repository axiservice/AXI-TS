package axi.xcell.model;

import java.util.ArrayList;

public class ConfigItemsListnerList extends ArrayList<ConfigItemsListnerList.ConfigItemModel>{
	private static final long serialVersionUID = 1L;	
	public static final String[] STOCK_MACROITEMS_ARRAY= {
			"description","last","var_%","7",
			"bid_1","ask_1","low","high","volume",
			"market","timestamp","currency"
			};
	
	public static final String[] BOOK_MACROITEMS_ARRAY= {
			"Bid_Num_1","Bid_Qty_1","Bid_1","Ask_1","Ask_Qty_1","Ask_Num_1",
			"Bid_Num_2","Bid_Qty_2","Bid_2","Ask_2","Ask_Qty_2","Ask_Num_2",
			"Bid_Num_3","Bid_Qty_3","Bid_3","Ask_3","Ask_Qty_3","Ask_Num_3",
			"Bid_Num_4","Bid_Qty_4","Bid_4","Ask_4","Ask_Qty_4","Ask_Num_4",
			"Bid_Num_5","Bid_Qty_5","Bid_5","Ask_5","Ask_Qty_5","Ask_Num_5",
			};
	
	public class ConfigItemModel{
		String itemKey;
		String sheetCellLocation;
		
		public ConfigItemModel() {
			super();
			// TODO Auto-generated constructor stub
		}
		
		public ConfigItemModel(String itemKey, String sheetCellLocation) {
			super();
			this.itemKey = itemKey;
			this.sheetCellLocation = sheetCellLocation;
		}

		public String getItemKey() {
			return itemKey;
		}
		public void setItemKey(String itemKey) {
			this.itemKey = itemKey;
		}
		public String getSheetCellLocation() {
			return sheetCellLocation;
		}
		public void setSheetCellLocation(String sheetCellLocation) {
			this.sheetCellLocation = sheetCellLocation;
		}
		
	}
}
