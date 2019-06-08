package it.polito.tdp.emergency.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import it.polito.tdp.emergency.model.Evento.TipoEvento;
import it.polito.tdp.emergency.model.Paziente.StatoPaziente;

public class Simulatore {

	// Coda degli eventi
	private PriorityQueue<Evento> queue = new PriorityQueue<>() ;
	
	// Modello del Mondo
	private List<Paziente> pazienti;
	private PriorityQueue<Paziente> salaAttesa;
	private int studiLiberi;
	
	// Parametri di simulazione
	private int NS = 3; // numero di studi medici
	private int NP = 50; // numero di pazienti in arrivo
	private Duration T_ARRIVAL = Duration.ofMinutes(15); // intervallo di tempo tra i pazienti

	private LocalTime T_inizio = LocalTime.of(8, 0);
	private LocalTime T_fine = LocalTime.of(20, 0);

	private int DURATION_TRIAGE = 5;
	private int DURATION_WHITE = 10;
	private int DURATION_YELLOW = 15;
	private int DURATION_RED = 30;
	private int TIMEOUT_WHITE = 120;
	private int TIMEOUT_YELLOW = 60;
	private int TIMEOUT_RED = 90;

	
	// Statistiche da calcolare
	private int numDimessi;
	private int numAbbandoni;
	private int numMorti;
	
	// Variabili interne
	private StatoPaziente nuovoStatoPaziente;
	private Duration intervalloPolling = Duration.ofMinutes(5) ;
	
	// Nel costruttore del simulatore si devono mettere dei parametri che non variano
	// Ad esempio può essere utile per leggere dei dati dal DB che non variano in base ai 
	//paramentri della simulazione 
	public Simulatore() {
		this.pazienti = new ArrayList<Paziente>();
		
	}
	// Questo metodo serve per simulare lo stessp scenario con parametri diversi
	// e può essere usato per leggere dei dati dal DB che varianon in base ai 
	// parametri della simuazione
	
	//PER CREARE UNA NUOVA SIMULAZIONE SI DEVONO FARE 3 COSE:
	public void init() {
		//1-creare i pazienti 
		LocalTime oraArrivo = T_inizio;
		
		pazienti.clear(); //butto via i pazienti vecchi e ne costruisco di nuovi
		for(int i=0; i<NP; i++) {
			Paziente p = new Paziente(i+1, oraArrivo);
			pazienti.add(p); //lista dei nuovi pazienti
			
			oraArrivo = oraArrivo.plus(T_ARRIVAL);
		}
		//inizializzo la sala d'attesa vuota (la classe PrioritaPaziente mi dice come funziona la sala d'attesa)
		this.salaAttesa = new PriorityQueue<>(new PrioritaPaziente()); 
		
		//1-creare gli studi medici 
		studiLiberi = NS;
		
		nuovoStatoPaziente = nuovoStatoPaziente.WAITING_WHITE;
		
		//2-creare gli eventi iniziali
		queue.clear();
		for(Paziente p : pazienti) {
			queue.add(new Evento(p.getOraArrivo(), TipoEvento.ARRIVO, p));
		}
		// lancia l'osservatore in polling
		queue.add(new Evento(T_inizio.plus(intervalloPolling), TipoEvento.POLLING, null));
		
		//3-resettare le statistiche
		numDimessi = 0 ; 
		numAbbandoni = 0;
		numMorti = 0 ; 
	}
	
	public void run() {
		
		while(!queue.isEmpty()) { //finchè ci sono eventi nella coda prioritaria
			Evento ev = queue.poll();// estraggo un evento alla volta
//			System.out.println(ev);
			
			Paziente p = ev.getPaziente();

			
			/* se la simulazione dovesse terminare alle 20:00
			if(ev.getOra().isAfter(T_fine))
				break;
			*/
			
			switch(ev.getTipo()) {
			
			/*
			 * Si deve sempre rispondere a tre domande:
			 * 1-Aggiornare la coda degli eventi?
			 * 2-Aggiornare lo stato del mondo? 
			 * 3-Aggiornare le statistiche?
			 */
			
			case ARRIVO:
				//tra 5 min verrà assegnato un codice colore
				queue.add(new Evento(ev.getOra().plusMinutes(DURATION_TRIAGE),
						TipoEvento.TRIAGE, ev.getPaziente() ));
				break;
				
			case TRIAGE:
				
				p.setStato(nuovoStatoPaziente);
				
				if(p.getStato() == StatoPaziente.WAITING_WHITE)
					queue.add(new Evento(ev.getOra().plusMinutes(TIMEOUT_WHITE),
						TipoEvento.TIMEOUT, p));
				else if(p.getStato() == StatoPaziente.WAITING_YELLOW)
					queue.add(new Evento(ev.getOra().plusMinutes(TIMEOUT_YELLOW),
							TipoEvento.TIMEOUT, p));
				else if(p.getStato() == StatoPaziente.WAITING_RED)
					queue.add(new Evento(ev.getOra().plusMinutes(TIMEOUT_RED),
							TipoEvento.TIMEOUT, p));
				//aggiungo il paziente in sala d'attesa e si posizionerà in base alla sua priorità
				salaAttesa.add(p);
				
				ruotaNuovoStatoPaziente();//aggiorna lo stato del paziente
				
				break;
				
			case VISITA:
				//determina il paziente con max priorità
				Paziente pazChiamato = salaAttesa.poll(); //prendo un paziente dalla sala d'attesa 
				
				if(pazChiamato == null) {
					break;
				}
				
				//paziente entra in uno studio
				StatoPaziente vecchioStato = pazChiamato.getStato();
				pazChiamato.setStato(StatoPaziente.TREATING); //il paziente chiamato viene trattato
				
				//studio diventa occupato
				studiLiberi --;
				
				//schedula l'uscita (CURATO) del paziente
				if(vecchioStato == StatoPaziente.WAITING_RED) {
					queue.add(new Evento(ev.getOra().plusMinutes(DURATION_RED), TipoEvento.CURATO, pazChiamato));
					
				}else if(vecchioStato == StatoPaziente.WAITING_YELLOW) {
					queue.add(new Evento(ev.getOra().plusMinutes(DURATION_YELLOW), TipoEvento.CURATO, pazChiamato));
					
				}else if(vecchioStato == StatoPaziente.WAITING_WHITE) {
					queue.add(new Evento(ev.getOra().plusMinutes(DURATION_WHITE), TipoEvento.CURATO, pazChiamato));
				}
				
				break;
			
			case CURATO:
				//paziente è fuori 
				p.setStato(StatoPaziente.OUT);
				
				//aggiorna numDimessi
				numDimessi ++;
				
				//schedula evento VISITA "adesso"
				studiLiberi ++;
				queue.add(new Evento(ev.getOra(), TipoEvento.VISITA, null));
				
				break;
				
			case TIMEOUT:
				// rimuovi dalla lista d'attesa
				salaAttesa.remove(p) ;

				if(p.getStato()==StatoPaziente.WAITING_WHITE) {
					p.setStato(StatoPaziente.OUT);
					numAbbandoni ++;
					
				} else if(p.getStato()==StatoPaziente.WAITING_YELLOW) {
					p.setStato(StatoPaziente.WAITING_RED);
					queue.add(new Evento(ev.getOra().plusMinutes(TIMEOUT_RED), TipoEvento.TIMEOUT, p));
					salaAttesa.add(p) ;
					
				} else if(p.getStato()==StatoPaziente.WAITING_RED) {
					p.setStato(StatoPaziente.BLACK);
					numMorti++;
					
				} else {
					System.out.println("Timeout anomalo nello stato"+ p.getStato());
				}
				
				break;
				
			case POLLING:
				//verifica se ci sono pazienti in attesa con studi liberi
				if(!salaAttesa.isEmpty() && studiLiberi > 0) {
					queue.add(new Evento(ev.getOra(), TipoEvento.VISITA, null));
				}
				//rischedula se stesso 
				if(ev.getOra().isBefore(T_fine)) {
				queue.add(new Evento(ev.getOra().plus(intervalloPolling), TipoEvento.POLLING, null));
				
				}
				break;
			
			}
			
		}
	}

	//Metodo che aggiorna lo stato del paiente (bianco-> giallo-> rosso-> bianco)
	private void ruotaNuovoStatoPaziente() {
		if(nuovoStatoPaziente==StatoPaziente.WAITING_WHITE)
			nuovoStatoPaziente = StatoPaziente.WAITING_YELLOW;
		else if(nuovoStatoPaziente==StatoPaziente.WAITING_YELLOW)
			nuovoStatoPaziente = StatoPaziente.WAITING_RED;
		else if(nuovoStatoPaziente==StatoPaziente.WAITING_RED)
			nuovoStatoPaziente = StatoPaziente.WAITING_WHITE;
		
	}

	public int getNS() {
		return NS;
	}

	public void setNS(int nS) {
		NS = nS;
	}

	public int getNP() {
		return NP;
	}

	public void setNP(int nP) {
		NP = nP;
	}

	public Duration getT_ARRIVAL() {
		return T_ARRIVAL;
	}

	public void setT_ARRIVAL(Duration t_ARRIVAL) {
		T_ARRIVAL = t_ARRIVAL;
	}

	public LocalTime getT_inizio() {
		return T_inizio;
	}

	public void setT_inizio(LocalTime t_inizio) {
		T_inizio = t_inizio;
	}

	public LocalTime getT_fine() {
		return T_fine;
	}

	public void setT_fine(LocalTime t_fine) {
		T_fine = t_fine;
	}

	public int getDURATION_TRIAGE() {
		return DURATION_TRIAGE;
	}

	public void setDURATION_TRIAGE(int dURATION_TRIAGE) {
		DURATION_TRIAGE = dURATION_TRIAGE;
	}

	public int getDURATION_WHITE() {
		return DURATION_WHITE;
	}

	public void setDURATION_WHITE(int dURATION_WHITE) {
		DURATION_WHITE = dURATION_WHITE;
	}

	public int getDURATION_YELLOW() {
		return DURATION_YELLOW;
	}

	public void setDURATION_YELLOW(int dURATION_YELLOW) {
		DURATION_YELLOW = dURATION_YELLOW;
	}

	public int getDURATION_RED() {
		return DURATION_RED;
	}

	public void setDURATION_RED(int dURATION_RED) {
		DURATION_RED = dURATION_RED;
	}

	public int getTIMEOUT_WHITE() {
		return TIMEOUT_WHITE;
	}

	public void setTIMEOUT_WHITE(int tIMEOUT_WHITE) {
		TIMEOUT_WHITE = tIMEOUT_WHITE;
	}

	public int getTIMEOUT_YELLOW() {
		return TIMEOUT_YELLOW;
	}

	public void setTIMEOUT_YELLOW(int tIMEOUT_YELLOW) {
		TIMEOUT_YELLOW = tIMEOUT_YELLOW;
	}

	public int getTIMEOUT_RED() {
		return TIMEOUT_RED;
	}

	public void setTIMEOUT_RED(int tIMEOUT_RED) {
		TIMEOUT_RED = tIMEOUT_RED;
	}

	public int getNumDimessi() {
		return numDimessi;
	}

	public int getNumAbbandoni() {
		return numAbbandoni;
	}

	public int getNumMorti() {
		return numMorti;
	}
}
