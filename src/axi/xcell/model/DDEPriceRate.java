package axi.xcell.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DDEPriceRate {
	List<PriceRate> lista = new ArrayList<PriceRate>();
	
	public DDEPriceRate() {
	}
	
	public DDEPriceRate(Number pLast) {
		addPriceRateLast(pLast);
	}

	public List<PriceRate> getLista() {
		return lista;
	}
	
	public void setLista(List<PriceRate> lista) {
		this.lista = lista;
	}
	
	public PriceRate addPriceRateLast(Number pLast) {
		PriceRate pr = new PriceRate();
		pr.setpLast(pLast);
		pr.setTimestamp(new Timestamp(System.currentTimeMillis()));
		this.lista.add(pr);
		return pr;
	}

	/**
	 * 
	 */
	public class PriceRate {
		Timestamp timestamp;
		Number pLast;

		public Number getpLast() {
			return pLast;
		}
		public void setpLast(Number pLast) {
			this.pLast = pLast;
		}
		
		public Timestamp getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(Timestamp timestamp) {
			this.timestamp = timestamp;
		}

		
		public String toString() {
			return "Timestamp: "+timestamp+" - Last: "+pLast;
		}
	}
}
