package axi.xcell.dde.scheduler;

import java.util.TimerTask;

import axi.xcell.dde.DataPersistManager;
import axi.xcell.model.DDEItems;
import axi.xcell.model.InterfaceModel;

public class PersistDataTask extends TimerTask {
	DDEItems itemsStorage;
	InterfaceModel iModel;
	String storagePath;
	
	public PersistDataTask(DDEItems itemsStorage) {
		super();
		this.itemsStorage = itemsStorage;
	}
	
	public PersistDataTask(String storagePath, InterfaceModel iModel) {
		super();
		this.storagePath = storagePath;
		this.iModel = iModel;
	}

	@Override
	public void run() {
		//DataPersistManager.persistItems(storagePath, itemsStorage);
		DataPersistManager.saveToFile(storagePath, iModel);
		

		// DEBUG
		//itemsStorage.PRINT_UTILS();
		
		
	}

}
