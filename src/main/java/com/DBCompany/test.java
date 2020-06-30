package com.DBCompany;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.time.LocalDate;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Attenzione: Il funzionamento di questa classe è garantito soltanto per il primo avvio del software.
 * I secondi tentativi non andranno a buon fine proprio perchè il database non può contenere informazioni duplicate di alcune
 * informazioni importanti, come l'inserimento di una seconda aula con lo stesso nome, stesso edificio ecc...
 * Deselezionando l'invocazione dei metodi è possibile quindi verificare il funzionamento dei singoli metodi.
 * Si ricorda che l'ordine d'invocazione dei metodi è fondamentale al fine di far lavorare le funzioni su dati integri ed esistenti.
 */

public class test {
    public static DBConnection db;

    public static void main(String[] args) throws SQLException, InterruptedException, MqttException {

        //Istanziamo la connessione con il database.
        db = new DBConnection();

        //Compilare una classe modello come questa da utilizzare per interagire con il DB.
        DB_Utente user = new DB_Utente("Vincenzo","Morrone","1998-12-12","vincenzom@live.it");
        //Aggiungere l'utente creato al Database.
        db.addUtente(user);

       //Test finta autenticazione

        System.out.println("Autenticazione: "+db.fakeAuth("vincenzom@live.it","test"));

       /*IMPORTANTE: La matricola è generata dal DB automaticamente. Le matricole partono da 20020000
        E' quindi importante ricavarla e poi settarla nella nostra classe user  */
        user.setMatricola( db.getIDUtente(user));

        //Compilare una classe modello come questa da utilizzare per interagire con il DB.
        DB_Aula A1 = new DB_Aula("1","Ospedaletto",1,10,1);
        A1.setServizi("Proiettore,Lavagna TouchScreen,Audio Perfetto");
        //Confermiamo il caricamento dell'aula sul DB.
        db.addAula(A1);
        System.out.println("Capacità massima consentita dall'aula: "+db.maxClassroomCapacity("1"));

        //Compilare una classe modello come questa da utilizzare per interagire con il DB.
        DB_Prenotazione Analisi = new DB_Prenotazione("Analisi 2", "Corso di Analisi 1", "1", LocalTime.of(13, 00, 00), LocalTime.of(LocalTime.now().getHour() - 3 , 00, 00), 12, "100", "Ospedaletto","2020-06-30", 0, 20020000);
        //Confermiamo il caricamento della prenotazione nel DB
        db.addPrenotazione(Analisi);

        //Dichiariamo l'inizio dell'evento.
        db.startEvent(Analisi);

        //Otteniamo la lista di prenotazioni in ordine cronologico.
        ArrayList<DB_Prenotazione> po = db.chronologicalOrderPrenotazioni();
        System.out.println("Prenotazioni con data odierna in ordine cronologico :"+po.toString());

        //Stampiamo il numero di prenotazioni presenti nel DB.
        System.out.println("Numero prenotazioni nel DB: "+db.howManyPrenotazioni());

        //Aggiungiamo un utente ad una classe.
        db.addUserToClassroom(A1,user);

        //Ricaviamo la posizione dell'utente nel caso questo sia presente in un aula.
        System.out.println("L'utente è nell'aula: "+db.whereIsThisUser(user));


        //Applica un filtro di ricerca basandosi sugli argomenti attivi, gli argomenti non attivi devono essere impostati a null
        System.out.println(db.prenotazioniFilterBy(null, LocalDate.parse("2020-07-05"), "12:00", "22:00", null, "20020000", null));

        //Per verificare l'inizio di un evento utilizzare la seguente funzione
        db.isEventStarted(Analisi);


    }
}
