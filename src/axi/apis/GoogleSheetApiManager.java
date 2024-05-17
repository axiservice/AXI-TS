package axi.apis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import axi.xcell.model.ConfigItemsListnerList;
import axi.xcell.model.ConfigItemsListnerList.ConfigItemModel;

public class GoogleSheetApiManager {
	private static final String APPLICATION_NAME = "Google Sheets API TS";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final String GOOGLE_API_CREDENTIALS_FILE_PATH = "/resources/GoogleApiCredentials.json";
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	String spreadsheetId = null;
	Sheets service = null;
	
	/**
	 * Global instance of the scopes required by this quickstart.
	 * If modifying these scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES =
			Collections.singletonList(SheetsScopes.SPREADSHEETS);

	/**
	 * 
	 * @param spreadsheetId
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public GoogleSheetApiManager(String spreadsheetId) throws IOException, GeneralSecurityException {
		super();
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		this.spreadsheetId = spreadsheetId;
		this.service =
				new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME)
				.build();
	}

	/**
	 * Creates an authorized Credential object.
	 *
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
			throws IOException {
		// Load client secrets.
		InputStream in = GoogleSheetApiManager.class.getResourceAsStream(GOOGLE_API_CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + GOOGLE_API_CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets =
				GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	/**
	 * 
	 * @param range
	 * @param newValues
	 * @throws IOException
	 */
	public void writeRangeCells(String range, List<List<Object>> newValues) throws IOException {
		ValueRange body = new ValueRange().setValues(newValues);  
		UpdateValuesResponse result = service.spreadsheets()
				.values()
				.update(spreadsheetId, range, body)
				.setValueInputOption("USER_ENTERED")
				.execute();

		System.out.println(result);
	}
	
	/**
	 * 
	 * @param range
	 * @return
	 * @throws IOException
	 */
	public ConfigItemsListnerList readTopicList(String range) throws IOException {
		ConfigItemsListnerList res = new ConfigItemsListnerList();
		ValueRange response = service.
				spreadsheets().
				values()
				.get(spreadsheetId, range)
				.execute();


		List<List<Object>> values = response.getValues();
		if (values == null || values.isEmpty()) {
			System.out.println("No data found.");
		} else {
			System.out.println("Name, Major");
			for (List row : values) {
				res.add(res.new ConfigItemModel(
						row.size()>0?(String)row.get(0):"",           	// itemKey;
						row.size()>1?(String)row.get(1):"1",           	// ticValue;
						row.size()>2?(String)row.get(2):"1",           	// moltiplicatore;
						row.size()>3?(String)row.get(3):"-",           	// descrizione;
						"*"                           					// sheetCellLocation;
						));
				
				// Print columns A and E, which correspond to indices 0 and 4.
//				if(row!=null&&row.size()>0)
//					System.out.printf("%s\n", row.get(0));
//				else 
//					System.out.printf("%s\n", "----");

			}
		}
		
		return res;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String spreadsheetId = "1SLFRaLVjwZAlTiPwElSB1W3346Cxqo3dcPkMkCqJKtk";
		
		try {
			GoogleSheetApiManager gapi = new GoogleSheetApiManager(spreadsheetId);
			
			String range = "A1:C2";
		    List<List<Object>> newValues = Arrays.asList(
		            Arrays.asList("FEDE", "MIAO"),
		            Arrays.asList("C", "D","XXXXXXXXXXx"));
		    
			gapi.writeRangeCells(range, newValues);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}
	
	
}
