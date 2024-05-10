package axi.xcell.dde;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

import axi.xcell.model.DDEItems;
import axi.xcell.model.InterfaceModel;

public class DataPersistManager {
	static Gson gson = new Gson();

	static public void persistItems(String storagePath, DDEItems ddeItems) {

		System.out.println("DataPersistManager::persistItems");

		// Converts Java object to JSON string
		//String json = gson.toJson(ddeItems);

		try {
			Files.createDirectories(Paths.get(storagePath));
			String fileWriter = storagePath+File.separator+"DDE-"+System.currentTimeMillis()+"-DATA.json";

			// Converts Java object to File
			try (Writer writer = new FileWriter(fileWriter)) {
				synchronized(ddeItems) {
				    gson.toJson(ddeItems, writer);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	
	public static void saveToFile(String storagePath, InterfaceModel iModel) {
		try {
			Files.createDirectories(Paths.get(storagePath));
			String fileWriter = storagePath+File.separator+"DT-"+System.currentTimeMillis()+"-DATA.json";

			// Converts Java object to File
			try (Writer writer = new FileWriter(fileWriter)) {
				synchronized(iModel) {
				    gson.toJson(iModel, writer);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
