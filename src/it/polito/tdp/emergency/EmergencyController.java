package it.polito.tdp.emergency;
	
	import java.net.URL;
	import java.time.Duration;
	import java.util.ResourceBundle;
	
	import it.polito.tdp.emergency.model.Simulatore;
	import javafx.event.ActionEvent;
	import javafx.fxml.FXML;
	import javafx.scene.control.TextArea;
	import javafx.scene.control.TextField;
	
	public class EmergencyController {
	
		private Simulatore sim;
	
	
		@FXML
		private ResourceBundle resources;
	
		@FXML
		private URL location;
	
		@FXML
		private TextField txtNS;
	
		@FXML
		private TextField txtNP;
	
		@FXML
		private TextField txtInterval;
	
		@FXML
		private TextArea txtResult;
	
	
		@FXML
		void handleSimula(ActionEvent event) {
	
			int NP = Integer.parseInt(txtNP.getText()) ;
			int NS = Integer.parseInt(txtNS.getText()) ;
			int intervallo = Integer.parseInt(txtInterval.getText()) ;
	
			sim.setNP(NP);
			sim.setNS(NS);
			sim.setT_ARRIVAL(Duration.ofMinutes(intervallo));
	
			sim.init();
			sim.run();
	
			txtResult.appendText(String.format("Pazienti dimessi    : %d\n", sim.getNumDimessi()));
			txtResult.appendText(String.format("Pazienti abbandonati: %d\n", sim.getNumAbbandoni()));
			txtResult.appendText(String.format("Pazienti morti      : %d\n", sim.getNumMorti()));
			txtResult.appendText("\n");
		}
	
		@FXML
		void initialize() {
			assert txtNS != null : "fx:id=\"txtNS\" was not injected: check your FXML file 'Emergency.fxml'.";
			assert txtNP != null : "fx:id=\"txtNP\" was not injected: check your FXML file 'Emergency.fxml'.";
			assert txtInterval != null : "fx:id=\"txtInterval\" was not injected: check your FXML file 'Emergency.fxml'.";
			assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Emergency.fxml'.";
	
		}
	
		public void setSim(Simulatore sim) {
			this.sim = sim;
	
			txtNP.setText(Integer.toString(sim.getNP()));
			txtNS.setText(Integer.toString(sim.getNS()));
			txtInterval.setText(Long.toString(sim.getT_ARRIVAL().getSeconds()/60)) ;
		}
	}