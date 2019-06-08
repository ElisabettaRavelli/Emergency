package it.polito.tdp.emergency.model;

import java.util.Comparator;

import it.polito.tdp.emergency.model.Paziente.StatoPaziente;

public class PrioritaPaziente implements Comparator<Paziente>{

	@Override
	// se p1 ha precedenza rispetto a p2 restituisce un valore negativo 
	// se p2 ha precedenza rispetto a p1 restituisce un valore positivo
	public int compare(Paziente p1, Paziente p2) {
		
		if(p1.getStato()==StatoPaziente.WAITING_RED && p2.getStato()!= StatoPaziente.WAITING_RED)
			return -1;
		if(p1.getStato()==StatoPaziente.WAITING_RED && p2.getStato()== StatoPaziente.WAITING_RED)
			return +1;
		
		if(p1.getStato()==StatoPaziente.WAITING_YELLOW && p2.getStato()!= StatoPaziente.WAITING_YELLOW)
			return -1;
		if(p1.getStato()==StatoPaziente.WAITING_YELLOW && p2.getStato()== StatoPaziente.WAITING_YELLOW)
			return +1;
		
		//nel caso in cui i colori di p1 e p2 fossero uguali si devono confrontare i tempi di arrivo di p1 e p2
		return p1.getOraArrivo().compareTo(p2.getOraArrivo());
	}

}
