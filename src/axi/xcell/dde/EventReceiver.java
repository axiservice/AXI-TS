package axi.xcell.dde;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.DDEMLException;
import com.pretty_tools.dde.client.DDEClientConversation;
import com.pretty_tools.dde.client.DDEClientEventListener;

import axi.apis.GoogleSheetApiManager;
import axi.xcell.dde.scheduler.GoogleSheetUpdateTask;
import axi.xcell.dde.scheduler.PersistDataTask;
import axi.xcell.model.DDEItems;
import axi.xcell.model.DDEPriceRate;
import axi.xcell.model.Datastorage;
import axi.xcell.model.ConfigItemsListnerList.ConfigItemModel;
import axi.xcell.model.Datastorage.Item;
import axi.xcell.model.ConfigItemsListnerList;

public class EventReceiver {
	private static final String CONFIG_PATH = "conf";
	private static final String CONFIG_FILE = CONFIG_PATH+File.separator+"configParam.json";
	
	private static final String PERSIST_DATA_STORAGE_PATH = "DATA-STORAGE";
	private static final int PERSIST_DATA_TASK_START_DELAY = 5000;
	private static final int PERSIST_DATA_TASK_PERIOD = 1*60*1000;  // 3 Minuti
	
	private static final int GOOGLE_SHEET_UPADATE_TASK_START_DELAY = 5000;
	private static final int GOOGLE_SHEET_UPADATE_TASK_PERIOD = 3*1000;  // 3 Secondi
	
	private static final String DDE_SERVICE = "FDF";
	private static final String DDE_TOPIC = "Q";
	
	private static final Gson gson = new Gson();
	
	private static GoogleSheetApiManager googleSheetApiManager = null;
	private static String GOOGLE_SPREADSHEET_ID = "1SLFRaLVjwZAlTiPwElSB1W3346Cxqo3dcPkMkCqJKtk";
	private static String GOOGLE_SHEET_OUTPUT_RANGE = "Class Data!B2:C";
	private static String GOOGLE_SHEET_TOPIC_INPUT_RANGE = "Class Data!A2:B";
	
	private static ConfigItemsListnerList configItemsListnerList = new ConfigItemsListnerList();
	DDEItems itemsStorage = new DDEItems();
	Datastorage datastorage = new Datastorage();
	TimerTask task;

	/**
	 * 
	 */
	public EventReceiver() {
		super();
		
		initGoogleSheetAPIs(GOOGLE_SPREADSHEET_ID);

		initScheduler();
		initEventListner();


	}
	
	/**
	 * Init Google SHEET APIs
	 */
	private void initGoogleSheetAPIs(String spreadsheetId) {
		try {
			googleSheetApiManager = new GoogleSheetApiManager(spreadsheetId);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void initScheduler() {
		// [CONFIG] - Load Items List
		///loadConfigFileItemsList();
		loadGoogleSheetTopicList();
		
		// [CONFIG] - Process Macro Items
		processMacro();
		
		//new Timer().schedule(new PersistDataTask(DATA_STORAGE_PATH, itemsStorage), PERSIST_DATA_TASK_START_DELAY, PERSIST_DATA_TASK_PERIOD);
		new Timer().schedule(new PersistDataTask(PERSIST_DATA_STORAGE_PATH, datastorage), PERSIST_DATA_TASK_START_DELAY, PERSIST_DATA_TASK_PERIOD);
		new Timer().schedule(new GoogleSheetUpdateTask(googleSheetApiManager, configItemsListnerList, datastorage, GOOGLE_SHEET_OUTPUT_RANGE),GOOGLE_SHEET_UPADATE_TASK_START_DELAY,GOOGLE_SHEET_UPADATE_TASK_PERIOD);

	}

	/**
	 * 
	 */
	private void initEventListner() {
		try {
			
			// [CONFIG] - Save Items List to config file
			//saveItemsList();

			// event to wait disconnection
			final CountDownLatch eventDisconnect = new CountDownLatch(1);

			// DDE client
			final DDEClientConversation conversation = new DDEClientConversation();
			// We can use UNICODE format if server prefers it
			//conversation.setTextFormat(ClipboardFormat.CF_UNICODETEXT);

			conversation.setEventListener(new DDEClientEventListener(){
				public void onDisconnect(){
					System.out.println("onDisconnect()");
					eventDisconnect.countDown();
				}

				public void onItemChanged(String topic, String item, String data){
					//                	try {
					//						persistData(fileName, data);
					//					} catch (IOException e1) {
					//						e1.printStackTrace();
					//					}
					String key = topic+"-"+item;
					persistItemChange(key, data);
					
					dataStorage(key, data);
					
					//Debug
					datastorage.PRINT_UTILS();

					System.out.println("onItemChanged(" + topic + "," + item + "," + data + ")");
					try{
						if ("stop".equalsIgnoreCase(data)) {
							conversation.stopAdvice(item);
							System.out.println("server stop signal (" + topic + "," + item + "," + data + ")");
						}
					}
					catch (DDEException e){
						System.out.println("Exception: " + e);
						e.printStackTrace();
					}
				}
			});

			System.out.println("Connecting...");
			conversation.connect(DDE_SERVICE, DDE_TOPIC);
			for(ConfigItemsListnerList.ConfigItemModel it : configItemsListnerList) {
				conversation.startAdvice(it.getItemKey());
			}
			//            conversation.startAdvice(item);
			//            conversation.startAdvice(item2);

			System.out.println("Waiting event...");
			eventDisconnect.await();
			System.out.println("Disconnecting...");
			conversation.disconnect();
			System.out.println("Exit from thread");
		}
		catch (DDEMLException e){
			System.out.println("DDEMLException: 0x" + Integer.toHexString(e.getErrorCode()) + " " + e.getMessage());
		}
		catch (DDEException e){
			System.out.println("DDEClientException: " + e.getMessage());
		}
		catch (Exception e){
			e.printStackTrace();
			//System.out.println("Exception: " + e);
		} finally {

		}
	}

	/**
	 * 
	 * @param key
	 * @param data
	 */
	private void dataStorage(String key, String data) {
		if(datastorage.containsKey(key)) {
			datastorage.get(key).add(datastorage.addItemHelper(data));
		} else {
			datastorage.put(key, datastorage.addRecordHelper(data));
		}
	}
	
	/**
	 * 
	 * @param key
	 * @param data
	 */
	private void persistItemChange(String key, String data) {	
		Map<String, DDEPriceRate> lpr = itemsStorage.getLista();
		Number pData = 0;
		NumberFormat nf = new DecimalFormat ("##########.#####");
		try {
			pData = nf.parse (data);
			//float pData = Float.parseFloat(data);

			if(lpr.containsKey(key)) {
				//synchronized (lpr.get(key)) {
					lpr.get(key).addPriceRateLast(pData);
				//} 
			} else {
				//synchronized (lpr.get(key)) {
					lpr.put(key, new DDEPriceRate(pData));
				//}
			}
		} catch (ParseException e) {
			System.out.println("ERRORE key: "+key+" - data: "+data);
			e.printStackTrace();
		}

	}

	private void loadConfigFileItemsList() {
		try {
			JsonReader reader = new JsonReader(new FileReader(CONFIG_FILE));
			configItemsListnerList = gson.fromJson(reader, ConfigItemsListnerList.class);
			configItemsListnerList.remove(null);
			System.out.println("loadConfigFileItemsList Tot.: "+configItemsListnerList.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void loadGoogleSheetTopicList() {
		try {
			configItemsListnerList = googleSheetApiManager.readTopicList(GOOGLE_SHEET_TOPIC_INPUT_RANGE);
			configItemsListnerList.remove(null);
			System.out.println("loadGoogleSheetTopicList Tot.: "+configItemsListnerList.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void processMacro() {
		final String MACRO_ITEM_STOCK = "#STOCK#";
		final String MACRO_ITEM_BOOK = "#BOOK#";
		ConfigItemsListnerList processedList = new ConfigItemsListnerList();

		configItemsListnerList.forEach(
				(value) -> {
					String[] valueSplit = value.getItemKey().split(";");
					if(valueSplit.length>1) {
						if(valueSplit[1].equalsIgnoreCase(MACRO_ITEM_STOCK)){
							for(String subItem : ConfigItemsListnerList.STOCK_MACROITEMS_ARRAY) {
								processedList.add(configItemsListnerList.new ConfigItemModel(valueSplit[0]+";"+subItem,"*"));
							}
						} else if(valueSplit[1].equalsIgnoreCase(MACRO_ITEM_BOOK)){ 
							for(String subItem : ConfigItemsListnerList.BOOK_MACROITEMS_ARRAY) {
								processedList.add(configItemsListnerList.new ConfigItemModel(valueSplit[0]+";"+subItem,"*"));
							}
						} else {
							processedList.add(value);
						}
					}
				});
		
		configItemsListnerList = processedList;
		System.out.println("processMacro Tot.: "+configItemsListnerList.size());
		configItemsListnerList.forEach((v)->{System.out.println(" - "+v.getItemKey()+" - "+v.getSheetCellLocation());});
	}
	
	/**
	 * 
	 */
	private void saveItemsList() {
		System.out.println("saveItemsList");
		try {
			Files.createDirectories(Paths.get(CONFIG_PATH));
			String fileWriter = CONFIG_FILE;
			try (Writer writer = new FileWriter(fileWriter)) {
				gson.toJson(configItemsListnerList, writer);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Init DDE event receiver
		new EventReceiver();

	}

	/**
	 * 
	 */
	static {
		//Eq.: JVM PARAM:  -Djava.library.path=".\lib"
		try {
			System.setProperty("java.library.path",".\\lib");

			//set sys_paths to null so that java.library.path will be reevalueted next time it is needed
			final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);

			sysPathsField.set(null, null);
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
