package axi.xcell.model;

import java.util.HashMap;
import java.util.Map;

public class DDEItems {
	Map<String, DDEPriceRate> lista = new HashMap<String, DDEPriceRate>();

	public Map<String, DDEPriceRate> getLista() {
		return lista;
	}

	public void setLista(Map<String, DDEPriceRate> lista) {
		this.lista = lista;
	}

	public void PRINT_UTILS(){
		System.out.println("--------------------------------------------------------------------------------------");
		lista.forEach(
				(key, value1) -> {
					System.out.println("- Key : " + key +" - SIZE: "+value1.getLista().size());
					value1.getLista().forEach((value2) -> {
						//synchronized(value2) {
						  System.out.println("  " + value2.toString());
						//}
					});
				});
	}
}
