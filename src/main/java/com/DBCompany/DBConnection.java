/**
 * @author  Vincenzo Morrone
 * @version 1.0
 * @since   2020-06-01
 *
 * PREMESSA: La maggior parte dei metodi lavora su classi passate come parametro. Alcuni metodi
 * lavorano direttamente su valori passati come parametro al fine di velocizzare la procedura.
 * Per alcuni metodi, la classe passata non necessita di essere compilata interamente
 * proprio perchè i metodi cercano di ricavare le informazioni complete dal DB dove l'utente non può fornirle.
 * I metodi che lavorano sulle istanze di classe DB_Utenti devono contenere più informazioni possibili,
 * quindi deve essere presente sempre la Matricola(ID) (a meno che il metodo non ricavi proprio questa informazione).
 * Questo al fine di ridurre le ambiguità.
 * ATTENZIONE: Assicurarsi di aver importato già il file SQL per la creazione del database. I metodi funzionano logicamente
 * se eseguiti in ordine logica: Per esempio è errato creare un evento quando la sua aula ancora non esiste.
 *
 * L'ordine corretto è specificato nel file test.java, dove si simulano le azioni base.
 * Questa classe è a disposizione del front-end, mentre la classe MQTTLogic non deve essere utilizzata dal front-end.
 * MQTTLogic può funzionare liberamente su dispositivo a se stante.
 * Per simulare questa classe utilizzare il file test.java, per verificare il funzionamento della connessione MQTT,
 * è a disposizione la classe MQTTLogic, che utilizza il Broker dell'università.
 *
 *
 */
package com.DBCompany;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;
import java.lang.Math;

import java.util.Date;

import static java.lang.Math.round;

/**
 * Contiene i metodi che gestiscono l'interazione con il database.
 */
public class DBConnection {

    private static Connection myConnection;

    public DBConnection() throws SQLException, MqttException {
        myConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/DB", "root", "123456789");
    }


    /**
     * Permette di avere il profilo completo dell'utente possedendo solo la matricola.
     *
     * @param matricola Stringa corrispondente alla matricola.
     * @return Oggetto di tipo DB_Utente
     * @throws SQLException
     */
    public DB_Utente getUserByMatricola(String matricola) throws SQLException {
        Statement st = myConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM users WHERE id="+ matricola);
        DB_Utente utente = null;
        if (rs.next()) {
            utente = new DB_Utente(rs.getString("nome"), rs.getString("cognome"), rs.getString("data_nascita"), rs.getString("email"));
            utente.setMatricola(rs.getString("id"));
            utente.setPrivilegio(rs.getInt("privilegio"));
            utente.setAccesso_consentito(rs.getBoolean("accesso_consentito"));

        }
        st.close();
        return utente;
    }

    /**
     * Permette l'inserimento nel DB dell'utente. L'utente deve essere realizzato precedentemente nella classe
     * DB_Utente e poi quest'ultima passata come parametro a questo metodo. Tutti i campi della classe DB_Utente
     * devono essere compilati proprio per garantire l'integrità dell'informazione.
     *
     * @param DB_user Oggetto di tipo DB_User
     * @return Ritorna oggetto di tipo DB_User
     * @throws SQLException
     */
    public DB_Utente addUtente(DB_Utente DB_user) throws SQLException {
        Statement st = myConnection.createStatement();
        st.executeUpdate("INSERT INTO users (nome,cognome,data_nascita,email,password) VALUES ('" + DB_user.getNome() + "','" + DB_user.getCognome() + "','" + DB_user.getData_nascita() + "','" + DB_user.getEmail() + "','test')");
        ResultSet rs = st.executeQuery("SELECT * FROM users WHERE id=(SELECT max(id) FROM users)");
        DB_Utente DBUtente = null;
        if (rs.next()) {
            DBUtente = new DB_Utente(rs.getString("nome"), rs.getString("cognome"), rs.getString("data_nascita"), rs.getString("email"));
        }
        st.close();
        return DBUtente;
    }

    /**
     * Permette di impostare il livello di privilegio dell'utente, questo permette di differenziare gli utenti
     * tra Studenti, Responsabili eventi, Segreteria, ecc.. . Il livello di privilegio è un intero da 0 a N.
     *
     * @param user           di tipo DB_User
     * @param privilegeLevel Intero che indica il privilegio (admin,user,superadmin...)
     * @return True per successo, false per fallimento.
     * @throws SQLException
     */
    public boolean setUserPrivilege(DB_Utente user, int privilegeLevel) throws SQLException {
        Statement st = myConnection.createStatement();
        st.executeUpdate("UPDATE users SET privilegio='" + privilegeLevel + "' WHERE id=" + user.getMatricola());
        return true;
    }

    /**
     * Rimuove completamente l'utente dal database.
     *
     * @param user Oggetto di tipo DB_Utente
     * @throws SQLException
     */
    public void deleteUser(DB_Utente user) throws SQLException {
        Statement st = myConnection.createStatement();
        st.execute("DELETE FROM users WHERE id=" + user.getMatricola());
        st.close();
    }

    /**
     * Ritorna una lista di tutti gli utenti iscritti alla piattaforma.
     *
     * @return Ritorna ArrayList con tutti gli utenti iscritti.
     * @throws SQLException
     */
    public ArrayList getAllUsers() throws SQLException {
        ArrayList _users = new ArrayList();
        Statement st = myConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM users");
        while (rs.next()) {
            _users.add(new DB_Utente(rs.getString("nome"), rs.getString("cognome"), rs.getString("data_nascita"), rs.getString("email")));
        }
        st.close();
        return _users;
    }

    /**
     * Questo metodo permette di ricevere un profilo utente non avendo a dispozione la matricola, si basa sulla ricerca
     * in base all'email e su una ricerca congiunta di nome, cognome e data di nascita. Si assume che la probabilità
     * di avere due utenti con stesso nome, cognome e data di nascita sia impossibile.
     *
     * @param user Oggetto di tipo DB_Utente
     * @return Oggetto di tipo DB_Utente
     * @throws SQLException
     */
    public DB_Utente tryToGetUser(DB_Utente user) throws SQLException {
        ResultSet rs = null;
        Statement st = null;
        DB_Utente nuovoUtente = null;
        String matricola;
        if (user.getEmail() != null) {
            st = myConnection.createStatement();
            rs = st.executeQuery("SELECT * FROM users WHERE email='" + user.getEmail() + "'");
        }
        if (user.getNome() != null && user.getCognome() != null && user.getData_nascita() != null) {
            st = myConnection.createStatement();
            rs = st.executeQuery("SELECT * FROM users WHERE nome='" + user.getNome() + "' AND cognome='" + user.getCognome() + "' AND data_nascita='" + user.getData_nascita() + "'");
        }
        if (user.getNome() != null && user.getCognome() != null && user.getPrivilegio() != -1) {
            st = myConnection.createStatement();
            rs = st.executeQuery("SELECT * FROM users WHERE nome='" + user.getNome() + "' AND cognome='" + user.getCognome() + "' AND privilegio='" + user.getPrivilegio() + "'");
        }
        if (rs.next()) {
            nuovoUtente = new DB_Utente(rs.getString("nome"), rs.getString("cognome"), rs.getString("data_nascita"), rs.getString("email"));
            nuovoUtente.setMatricola(rs.getString("id"));
            nuovoUtente.setCognome(rs.getString("accesso_consentito"));
            nuovoUtente.setPrivilegio(rs.getInt("privilegio"));
        }
        st.close();
        return nuovoUtente;

    }

    /**
     * Ritorna la matricola dell'utente passato come parametro. Quindi bisognerà passare la classe DB_Utente con il campo matricola non settato.
     *
     * @param user Oggetto di tipo DB_Utente.
     * @return Stringa contenente matricola utente.
     * @throws SQLException
     */
    public String getIDUtente(DB_Utente user) throws SQLException {
        DB_Utente utente = tryToGetUser(user);
        return utente.getMatricola();
    }

    /**
     * Permette di impostare un bit collegato ad ogni singolo utente che può essere usato per bloccare
     * il login.
     *
     * @param user   Oggetto di tipo DB_Utente.
     * @param valore Intero (1 per accesso consentito, -1 per accesso bloccato)
     * @return True se la modifica è avvenuta.
     * @throws SQLException
     */
    public Boolean BanOrNotUtente(DB_Utente user, int valore) throws SQLException {
        DB_Utente utente = tryToGetUser(user);
        if (utente != null) {
            Statement st = myConnection.createStatement();
            st.executeUpdate("UPDATE users SET accesso_consentito='" + valore + "' WHERE id=" + utente.getMatricola());

            return true;
        } else {
            return false;
        }
    }

    /**
     * Verifica se l'utente richiesto esiste, in questo caso il risultato è True.
     *
     * @param user Oggetto di tipo DB_Utente
     * @return True se l'utente esiste.
     * @throws SQLException
     */
    public Boolean existUtente(DB_Utente user) throws SQLException {
        if (user != null) {
            if (tryToGetUser(user) == null) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Verifica se l'utente richiesto ha un livello di privlegio >=1 in questo caso è true.
     *
     * @param user Oggetto di tipo DB_Utente
     * @return True se il livello di privilegio >= 1
     * @throws SQLException
     */
    public Boolean isSuperuser(DB_Utente user) throws SQLException {
        int privilegio = 0;
        if (user != null) {
            Statement st = myConnection.createStatement();
            ResultSet rs = st.executeQuery("SELECT privilegio FROM users WHERE id=" + user.getMatricola());
            privilegio = rs.getInt("privilegio");
            st.close();

        }
        if (privilegio >= 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Aggiunge l'aula passata via parametro al database.
     *
     * @param aula Oggetto di tipo DB_Aula.
     * @return True se l'aula è stata aggiunta.
     * @throws SQLException
     */
    public boolean addAula(DB_Aula aula) throws SQLException {
        Statement st = myConnection.createStatement();
        st.executeUpdate("INSERT INTO aule (nome,edificio,servizi,disponibilita,capacita,id_segnaleout) VALUES ('" + aula.getNome() + "','" + aula.getEdificio() + "','" + aula.getServizi() + "','" + aula.getDisponibilita() + "','" + aula.getCapacita() + "','" + aula.getId_segnaleout() + "')");
        st.close();
        return true;
    }

    /**
     * Ritorna un ArrayList contenente tutti i servizi collegati ad una singola aula.
     *
     * @param aula Oggetto di tipo aula.
     * @return Oggetto di tipo lista contenente tutti i servizi collegati ad una singola aula.
     * @throws SQLException
     */
    public List<String> getAulaServizi(DB_Aula aula) throws SQLException {
        Statement st = myConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT servizi FROM aule WHERE nome='" + aula.getNome() + "'");
        if (rs.next()) {
            String servizi = rs.getString("servizi");
            List<String> lista = Arrays.asList(servizi.split(","));
            st.close();
            return lista;
        } else {
            return null;
        }

    }

    /**
     * Aggiunge ad un aula una lista di servizi, questa lista è semplicemente una stringa dove ogni
     * servizio è separato da virgola. Esempio: Priettore,Lavagna...
     *
     * @param aula    Oggetto di tipo DB_Aula
     * @param servizi Stringa di servizi, ogni servizio separato da virgola Esempio: proiettore,computer,lavagna...
     * @throws SQLException
     */
    public void addServizio(DB_Aula aula, String servizi) throws SQLException {
        Statement st = myConnection.createStatement();
        st.executeUpdate("UPDATE aule SET servizi='" + servizi + "'");
    }

    /**
     * Un aula può essere non disponibile per differenti motivi. Questo metodo ritorna true
     * se l'aula è accessibile, false se l'aula non è disponibile per qualsiasi motivo.
     *
     * @param aula Oggetto di tipo DB_Aula
     * @return True se l'aula è accessibile | False se l'aula non è accessibile.
     * @throws SQLException
     */
    public boolean isAccesible(DB_Aula aula) throws SQLException {
        Statement st = myConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT disponibilita FROM aule WHERE nome='" + aula.getNome() + "' AND edificio='" + aula.getEdificio() + "'");
        if (rs.next()) {
            if (rs.getInt("disponibilita") == 1) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Ritorna l'identificativo numero del sensore luminoso collegato ad una singola aula.
     *
     * @param aula Oggetto di tipo aula
     * @return Ritorna intero corrispondente all'ID del segnale luminoso.
     * @throws SQLException
     */
    public int getSignalIDfromClassroom(DB_Aula aula) throws SQLException {
        Statement st = myConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id_segnaleout FROM aule WHERE nome='" + aula.getNome() + "' AND edificio='" + aula.getEdificio() + "'");
        if (rs.next()) {
            return rs.getInt("id_segnaleout");
        } else {
            return -1;
        }
    }

    /**
     * Elimina completamente un aula.
     *
     * @param aula Oggetto di tipo DB_Aula.
     * @return True se l'operazione è andata a buon fine.
     * @throws SQLException
     */
    public boolean deleteAula(DB_Aula aula) throws SQLException {
        if (aula != null) {
            Statement st = myConnection.createStatement();
            st.executeUpdate("DELETE FROM aule WHERE nome='" + aula.getNome() + "' AND edificio='" + aula.getEdificio() + "'");
            st.close();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Ritorna un ArrayList di Utenti contenente tutti i singoli utenti presenti in una
     * specifica aula.
     *
     * @param aula Stringa con nome aula
     * @return ArrayList di Stringe contenente tutte le matricole dei presenti.
     * @throws SQLException
     */
    public ArrayList<String> getUserInClassroom(String aula) throws SQLException {
        ArrayList<String> userInClass = new ArrayList<String>();
        Statement st = myConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id_presenza_utenti FROM presenza_utenti WHERE nome_aula='" + aula + "' AND timestampuscita IS NULL");
        while (rs.next()) {
            userInClass.add(rs.getString("id_presenza_utenti"));
        }
        st.close();
        return userInClass;
    }



    /**
     * Aggiunge ad un aula un singolo utente soltanto se il numero di presenti nell'aula non è superiore alla
     * capacità massima espressa nel momento in cui si è creata la prenotazione.
     *
     * @param aula Oggetto di tipo DB_Aula, completamente compilato.
     * @param user Oggetto di tipo DB_Utente con matricola impostata.
     * @return True se l'utente è stato aggiunto alla classe, false in caso il numero di presenti in aula è superiore a quello consentito.
     * @throws SQLException
     */
    public boolean addUserToClassroom(DB_Aula aula, DB_Utente user) throws SQLException {
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int minuti = rightNow.get(Calendar.MINUTE);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        String today = dtf.format(now);
        String completeHour = (hour + ":" + minuti).toString();
        Statement st1 = myConnection.createStatement();
        ResultSet rs1 = st1.executeQuery("SELECT limite_partecipanti FROM prenotazioni WHERE ora_inizio < '" + completeHour +"' and ora_fine > '"+completeHour+"' AND data_svolgimento='" + today + "' AND aula='"+aula.getNome()+"'");
        if (rs1.next()) {
            int limite_partecipanti_inaula = rs1.getInt("limite_partecipanti");
                    if (getUserInClassroom(aula.getNome()).size() < limite_partecipanti_inaula) {
                        Statement st = myConnection.createStatement();
                        st.executeUpdate("INSERT INTO presenza_utenti VALUES ('" + user.getMatricola() + "','" + new Timestamp(System.currentTimeMillis()) + "',NULL,'" + aula.getNome() + "','" + aula.getEdificio() + "')");
                        st.close();
                        return true;
                    }else{
                        //Notifica amministratore
                        notifyResponsabile(aula,user);
                    }
        }
        return false;
    }

    public String notifyResponsabile(DB_Aula aula,DB_Utente user){
        String toReturn = "ATTENZIONE: Nell'aula "+aula.getNome()+" vi è una situazione di sovraffollamento."+System.lineSeparator()+"L'utente "+user.getMatricola()+" è l'ultimo entrato che ha causato questa situazione.";
        System.out.println(toReturn);
        return toReturn;
    }



    /**
     * Rimuove da un aula un singolo utente.
     *
     * @param aula Oggetto di tipo DB_Aula
     * @param user Oggetto di tipo DB_utente
     * @return True se l'utente è stato rimosso dalla classe.
     * @throws SQLException
     */
    public boolean remUserToClassroom(DB_Aula aula, DB_Utente user) throws SQLException {
        if (aula != null && user != null) {
            if (aula.getNome() != null && aula.getEdificio() != null && user.getMatricola() != null) {
                Statement st = myConnection.createStatement();
                st.executeUpdate("UPDATE presenza_utenti SET timestampuscita='" + new Timestamp(System.currentTimeMillis()) + "' WHERE id_presenza_utenti='" + user.getMatricola() + "' AND nome_aula='" + aula.getNome() + "' AND edificio='" + aula.getEdificio() + "'");
                st.close();
                return true;
            }
        }
        return false;
    }



    /**
     * Ricerca un utente in una qualsiasi aula, se quest'ultimo è presente in un aula allora
     * verrà ritornata l'istanza dell'aula in cui l'utente si trova.
     *
     * @param user Oggetto di tipo DB_Utente
     * @return Ritorna un oggetto di tipo DB_Aula
     * @throws SQLException
     */
    public DB_Aula whereIsThisUser(DB_Utente user) throws SQLException {
        if (user != null) {
            Statement st = myConnection.createStatement();
            ResultSet rs = st.executeQuery("SELECT nome_aula,edificio FROM presenza_utenti WHERE id_presenza_utenti='" + user.getMatricola() + "' AND timestampuscita IS NULL");
            if (rs.next()) {

                Statement st1 = myConnection.createStatement();
                ResultSet rs1 = st1.executeQuery("SELECT * FROM aule WHERE nome='" + rs.getString("nome_aula") + "' AND edificio='" + rs.getString("edificio") + "'");
                if (rs1.next()) {
                    DB_Aula toReturn = new DB_Aula(rs1.getString("nome"), rs1.getString("edificio"), rs1.getInt("disponibilita"), rs1.getInt("capacita"), rs1.getInt("id_segnaleout"));
                    st1.close();
                    st.close();
                    return toReturn;
                }
            }
        }
        return null;
    }

    /**
     * Questo metodo ritorna la percentuale di riempimento dell'aula, considera solo gli utente presenti
     * al momento della richiesta, non considera gli utenti che sono entrati ed usciti.
     *
     * @param aula Oggetto di tipo DB_Aula
     * @return Ritorna la percentuale del grado di riempimento dell'aula calcolata sulla capacità massima dell'aula.
     * @throws SQLException
     */
    public float fillingClassroomPercentage(DB_Aula aula) throws SQLException {
        float conteggio = 0;
        float capacita = 0;
        int limite_partecipanti_evento=0;
        if (aula != null) {

            Statement st = myConnection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id_presenza_utenti FROM presenza_utenti WHERE nome_aula='" + aula.getNome() + "' AND edificio='" + aula.getEdificio() + "' AND timestampuscita IS NULL");
            while (rs.next()) {
                conteggio++;
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            String hour = String.valueOf(LocalDateTime.now().getHour());
            String minutes = String.valueOf(LocalDateTime.now().getMinute());
            String ora_completa = hour+":"+minutes;
            Statement st2 = myConnection.createStatement();
            ResultSet rs2 = st2.executeQuery("SELECT limite_partecipanti FROM prenotazioni WHERE data_svolgimento='"+dtf.format(now)+"' AND aula='" + aula.getNome() + "' AND ora_inizio<='"+ora_completa+"'");
            if (rs2.next()) {
                limite_partecipanti_evento = rs2.getInt("limite_partecipanti");
                System.out.println(limite_partecipanti_evento);
            }


            return (conteggio / limite_partecipanti_evento) * 100;
        }
        return -1;
    }




    /**
     * Aggiunge al DB la prenotazione passata come parametro.
     *
     * @param prenotazione Oggetto di tipo DB_Prenotazione
     * @return True per successo.
     * @throws SQLException
     */
    public boolean addPrenotazione(DB_Prenotazione prenotazione) throws SQLException {
        if (prenotazione != null) {
            int filtro = Integer.parseInt(prenotazione.getFiltro());
            if(filtro!=100){
                float capacita = 0;
                Statement st1 = myConnection.createStatement();
                ResultSet rs1 = st1.executeQuery("SELECT capacita FROM aule WHERE nome='" + prenotazione.getAula() + "' AND edificio='" + prenotazione.getEdificio_evento() + "'");
                if (rs1.next()) {
                    capacita = rs1.getInt("capacita");
                }
                float utenti_conFiltro;
                utenti_conFiltro = (capacita * filtro)/100;
                prenotazione.setFiltro(String.valueOf(utenti_conFiltro));
                prenotazione.setLimite_partecipanti(round(utenti_conFiltro));
            }
            Statement st = myConnection.createStatement();
            st.executeUpdate("INSERT INTO prenotazioni VALUES ('" + prenotazione.getNome_evento() + "','" + prenotazione.getDescrizione_evento() + "','" + prenotazione.getAula() + "','" + prenotazione.getEdificio_evento() + "'," + prenotazione.getLimite_partecipanti() + ",'" + prenotazione.getOra_inizio() + "','" + prenotazione.getOra_fine() + "','" + prenotazione.getData_svolgimento() + "'," + prenotazione.getIn_svolgimento() + ",'" + prenotazione.getFiltro() + "','" + prenotazione.getResponsabile() + "')");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Rimuove la prenotazione passata come parametro.
     *
     * @param prenotazione
     * @return
     * @throws SQLException
     */
    public boolean remPrenotazione(DB_Prenotazione prenotazione) throws SQLException {
        if (prenotazione != null) {
            Statement st = myConnection.createStatement();
            st.executeUpdate("DELETE FROM prenotazioni WHERE nome_evento='" + prenotazione.getNome_evento() + "' AND aula='" + prenotazione.getAula() + "' AND edificio='" + prenotazione.getEdificio_evento() + "' AND ora_inizio='" + prenotazione.getOra_inizio() + "'  ");
            return true;
        }
        return false;
    }

    /**
     * Recupera la prenotazione avendo a disposizione i dati minimi per garantire l'unicità.
     * I dati necessari per recuperare in maniera univoca una prenotazione sono:
     * nome_evento | aula | edifico | ora_inizio (dell'evento)
     *
     * @param prenotazione Oggetto di tipo prenotazione compilato con le informazioni che si hanno.
     * @return Ritorna oggetto di tipo DB_Prenotazione compilato con tutti i dati ricavati dal DB, qualora la prenotazione esistesse. Altrimenti torna null.
     * @throws SQLException
     */
    public DB_Prenotazione tryToGetPrenotazione(DB_Prenotazione prenotazione) throws SQLException {
        if (prenotazione != null) {
            Statement st = myConnection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM prenotazioni WHERE nome_evento='" + prenotazione.getNome_evento() + "' AND aula='" + prenotazione.getAula() + "' AND edificio='" + prenotazione.getEdificio_evento() + "' AND ora_inizio='" + prenotazione.getOra_inizio().toString() + "'");
            if (rs.next()) {
                DB_Prenotazione toReturn = new DB_Prenotazione(rs.getString("nome_evento"), rs.getString("descrizione_evento"), rs.getString("aula"), LocalTime.parse(rs.getString("ora_inizio")), LocalTime.parse(rs.getString("ora_fine")), rs.getInt("limite_partecipanti"), rs.getString("filtro"), rs.getString("edificio"), rs.getString("data_svolgimento"), rs.getInt("in_svolgimento"), rs.getInt("responsabile"));
                st.close();
                return toReturn;
            }
        }
        return null;
    }

    /**
     * Recupera la matricola del responsabile di un evento.
     *
     * @param prenotazione
     * @return Ritorna la matricola del responsabile dell'evento.
     * @throws SQLException
     */
    public String getResponsabilePrenotazione(DB_Prenotazione prenotazione) throws SQLException {
        if (prenotazione != null) {
            Statement st = myConnection.createStatement();
            ResultSet rs = st.executeQuery("SELECT responsabile FROM prenotazioni WHERE nome_evento='" + prenotazione.getNome_evento() + "' AND aula='" + prenotazione.getAula() + "' AND edificio='" + prenotazione.getEdificio_evento() + "' AND ora_inizio='" + prenotazione.getOra_inizio().toString() + "'");
            st.close();
            if (rs.next()) {
                return rs.getString("responsabile");
            }

        }
        return "";
    }

    /**
     * Ritorna in maniera ordinata in ordine ascendente le prenotazioni inserite nel DB che hanno data si svolgimento
     * odierna, quindi prima le prenotazioni con un orario di inizio più prossimo.
     *
     * @return Ritorna un ArrayList in ordine cronologico di tutte le prenotazioni inserite.
     * @throws SQLException
     */
    public ArrayList<DB_Prenotazione> chronologicalOrderPrenotazioni() throws SQLException {
        ArrayList<DB_Prenotazione> arrayListToReturn = new ArrayList<DB_Prenotazione>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        Statement st = myConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM prenotazioni WHERE data_svolgimento='"+dtf.format(now)+"' ORDER BY ora_inizio ASC");
        while (rs.next()) {
            DB_Prenotazione toReturn = new DB_Prenotazione(rs.getString("nome_evento"), rs.getString("descrizione_evento"), rs.getString("aula"), LocalTime.parse(rs.getString("ora_inizio")), LocalTime.parse(rs.getString("ora_fine")), rs.getInt("limite_partecipanti"), rs.getString("filtro"), rs.getString("edificio"), rs.getString("data_svolgimento"), rs.getInt("in_svolgimento"), rs.getInt("responsabile"));
            arrayListToReturn.add(toReturn);
        }
        st.close();
        return arrayListToReturn;
    }

    /**
     * Funzione per il conteggio delle prenotazioni inserite.
     *
     * @return Intero corrispondente al numero di prenotazioni presenti nel DB.
     * @throws SQLException
     */
    public int howManyPrenotazioni() throws SQLException {
        ArrayList<DB_Prenotazione> test = chronologicalOrderPrenotazioni();
        return test.size();
    }

    /**
     * Segna l'inizio di un evento asserendo un semplice bit nella prenotazione.
     *
     * @param prenotazione
     * @throws SQLException
     */
    public void startEvent(DB_Prenotazione prenotazione) throws SQLException {
        Statement st = myConnection.createStatement();
        st.executeUpdate("UPDATE prenotazioni SET in_svolgimento=1 WHERE nome_evento='" + prenotazione.getNome_evento() + "' AND aula='" + prenotazione.getAula() + "' AND edificio='" + prenotazione.getEdificio_evento() + "' AND ora_inizio='" + prenotazione.getOra_inizio().toString() + "'");
        st.close();
    }

    /**
     * Segna la fine di un evento asserendo un semplice bit nella prenotazione.
     *
     * @param prenotazione
     * @throws SQLException
     */
    public void stopEvent(DB_Prenotazione prenotazione) throws SQLException {
        Statement st = myConnection.createStatement();
        st.executeUpdate("UPDATE prenotazioni SET in_svolgimento=0 WHERE nome_evento='" + prenotazione.getNome_evento() + "' AND aula='" + prenotazione.getAula() + "' AND edificio='" + prenotazione.getEdificio_evento() + "' AND ora_inizio='" + prenotazione.getOra_inizio().toString() + "'");
        st.close();
    }

    /**
     * Aggiunge la prenotazione con orario più basso allo storico prenotazioni basandosi sul nome dell'evento.
     * Praticamente avendo una lista di prenotazioni ordinata con orario crescente (15:00->15:30->16:40..)
     * è possibile aggiungere la prenotazione più recente (quella delle 15:00) allo storico prenotazioni.
     * Bisogna fornire il nome_evento perchè potrebbero esserci due eventi alle ore 15:00 e quindi decidere
     * quale dei due inserire nello storico prenotazioni.
     *
     * @throws SQLException
     */
    public void addLastPrenotazioneToStorico(String nome_evento) throws SQLException {
        Statement st = myConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM prenotazioni WHERE nome_evento='" + nome_evento + "' ORDER BY ora_inizio ASC LIMIT 1");
        if (rs.next()) {
            DB_Prenotazione prenotazione = new DB_Prenotazione(rs.getString("nome_evento"), rs.getString("descrizione_evento"), rs.getString("aula"), LocalTime.parse(rs.getString("ora_inizio")), LocalTime.parse(rs.getString("ora_inizio")), rs.getInt("limite_partecipanti"), rs.getString("filtro"), rs.getString("edificio"), rs.getString("data_svolgimento"), rs.getInt("in_svolgimento"), rs.getInt("responsabile"));
            addToStorico(prenotazione);
            remPrenotazione(prenotazione);
        }
    }

    /**
     * Inserisce una prenotazione passata come paramtro nello storico prenotazioni non tiene conto se la prenotazione esiste.
     * Può essere utilizzato per aggiungere eventi passati che sono stati svolti ma mai inseriti sulla piattaforma.
     * @param prenotazione - Oggetto di tipo DB_Prenotazione
     * @throws SQLException
     */
    public void addToStorico(DB_Prenotazione prenotazione) throws SQLException {
        Statement st = myConnection.createStatement();
        st.executeUpdate("INSERT INTO storico_prenotazioni VALUES ('" + prenotazione.getNome_evento() + "','" + prenotazione.getAula() + "','" + prenotazione.getOra_inizio() + "','" + prenotazione.getDescrizione_evento() + "','" + prenotazione.getLimite_partecipanti() + "','" + prenotazione.getOra_fine() + "','" + prenotazione.getIn_svolgimento() + "','" + prenotazione.getData_svolgimento() + "')");
        st.close();
    }


    /**
     * Analizza il messaggio MQTT inviato dal publisher, estrapola i dati degli utenti ed aggiunge
     * l'utente alla classe/aula. Questo metodo è utilizzato dalla classe stessa, non è pensato per l'uso esterno.
     * Si sconsiglia l'uso dato che il parametro ha una forma standard conosciuta solo dalla classe stessa.
     * @param message
     * @throws SQLException
     */
    public void MQTTaddToClassroom(String message) throws SQLException {
        String domain = message;
        String[] strArray = domain.split("\\|");
        DB_Aula aula = new DB_Aula(strArray[1],strArray[2],1,0,0);
        DB_Utente utente = new DB_Utente("","","","");
        utente.setMatricola(strArray[0]);
        addUserToClassroom(aula,utente);

    }

    /**
     * Analizza il messaggio MQTT inviato dal publisher, estrapola i dati degli utenti e rimuove
     * l'utente dalla classe/aula dove era entrato precedentemente. Questo metodo viene utilizzato implicitamente
     * non è consigliato l'uso diretto.
     * @param message - Il parametro ha una forma standard conosciuta solo dalla classe.
     * @throws SQLException
     */
    public void MQTTremFromClassroom(String message) throws SQLException {
        String domain = message;
        String[] strArray = domain.split("\\|");
        DB_Aula aula = new DB_Aula(strArray[1],strArray[2],1,0,0);
        DB_Utente utente = new DB_Utente("","","","");
        utente.setMatricola(strArray[0]);
        remUserToClassroom(aula,utente);

    }

    /**
     * Ritorna il numero di persone presenti in una classe/aula e l'aula. Viene utilizzata per pubblicare sul broker nel
     * topic GRUPPO3VC/SIGNALS il numero di persone presenti in una classe. Viene invocata ogni volta che l'utente
     * entra od esce da una classe, fornendo quindi il numero aggiornato di presenti ogni volta che lo stato di una classe
     * cambia. Non va usata direttamente.
     * @param message
     * @return
     * @throws SQLException
     */
    public String MQTTcapienzaEdAula(String message) throws SQLException{
        String domain = message;
        String[] strArray = domain.split("\\|");
        DB_Aula aula = new DB_Aula(strArray[1],strArray[2],1,0,0);
        Integer persone = getUserInClassroom(aula.getNome()).size();
        String toReturn = persone+"-"+fillingClassroomPercentage(aula)+"-"+strArray[1];
        return toReturn.toString();
    }

    /**
     * Metodo per falsa autenticazione, si realizza una classe di tipo DB_Utente e si utilizza il metodo di setter (setPassword)
     * per associare la password che sarà usata per l'autenticazione. Si passa al metodo la classe compilata con la mail
     * dell'utente e la password (impostati attraverso i setter).
     * @param email,password
     * @return Ritorna un oggetto di tipo DB_Utente contenente tutte le informazioni dell'utente che ha effettuato l'accesso, solo se l'autenticazione è andata a buon fine, altrimenti torna null.
     * @throws SQLException
     */
    public DB_Utente fakeAuth(String email, String password) throws SQLException {
        Statement st = myConnection.createStatement();
        DB_Utente toReturn = null;
        ResultSet rs = st.executeQuery("SELECT id FROM users WHERE password='"+password+"' AND email='"+email+"'");
        if (rs.next()) {
        toReturn = getUserByMatricola(rs.getString("id"));
        return toReturn;
        }else{
            return null;
        }
    }

    /**
     * Funzione che verifica l'inizio di un evento.
     * @param prenotazione
     * @return True se l'evento è iniziato, false in caso contrario
     * @throws SQLException
     */
    public boolean isEventStarted(DB_Prenotazione prenotazione) throws SQLException {
        Statement st = myConnection.createStatement();
        DB_Utente toReturn = null;
        ResultSet rs = st.executeQuery("SELECT in_svolgimento FROM prenotazioni WHERE nome_evento='"+prenotazione.getNome_evento()+"' AND aula='"+prenotazione.getAula()+"' AND edificio='"+prenotazione.getEdificio_evento()+"' AND ora_inizio='"+prenotazione.getOra_inizio()+"' AND ora_fine='"+prenotazione.getOra_fine()+"'");
        if (rs.next()) {
            if(rs.getInt("in_svolgimento")==1){
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    /**
     * Questo metodo ritorna la capacità massima consentita dall'aula.
     * @param ID_aula Stringa che rappresenta l'identificativo univoco dell'aula.
     * @return Intero che corrisponde al numero di posti occupabili nell'aula.
     * @throws SQLException
     */
    public int maxClassroomCapacity(String ID_aula) throws SQLException {
        Statement st = myConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT capacita FROM aule WHERE nome='"+ID_aula+"'");
        if (rs.next()) {
            return rs.getInt("capacita");
        }else {
            return -1;
        }
    }




    /**
     * Metodo che effettua una ricerca prenotazione basandosi sui filtri impostati. I filtri sono passati come parametro,
     * i filtri non attivi devono essere impostati a null.
     * @param data_inizio
     * @param data_fine
     * @param ora_inizio
     * @param ora_fine
     * @param nome_prenotazione
     * @param matricola_docente
     * @param nome_aula
     * @return Ritorna un arraylist di prenotazioni che rispecchiano i criteri di filtraggio.
     * @throws SQLException
     */
    public ArrayList<DB_Prenotazione> prenotazioniFilterBy(LocalDate data_inizio, LocalDate data_fine,String ora_inizio, String ora_fine, String nome_prenotazione, String matricola_docente, String nome_aula) throws SQLException {
        ArrayList<DB_Prenotazione> risultati = new ArrayList<>();
        if(ora_inizio!=null && ora_fine!=null) {
            LocalTime localTime = LocalTime.parse(ora_inizio, DateTimeFormatter.ofPattern("HH:mm"));
            int hour = localTime.get(ChronoField.CLOCK_HOUR_OF_DAY);
            int minute = localTime.get(ChronoField.MINUTE_OF_HOUR);
            String ora_inizio1 = String.valueOf(hour + minute);
        }
        String query = "SELECT * FROM prenotazioni WHERE ";
        if((data_inizio)!=null && (data_fine)!=null){
            query = query + "data_svolgimento>='"+data_inizio+"' AND data_svolgimento<='"+data_fine+"'";
        }else if(data_inizio==null & data_fine==null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            query = query + "data_svolgimento>='"+dtf.format(now)+"' AND data_svolgimento<='"+dtf.format(now.plusDays(6))+"'";
        }else if(data_inizio==null && data_fine!=null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            query = query + "data_svolgimento>='"+dtf.format(now)+"' AND data_svolgimento<='"+data_fine+"'";
        }else if(data_inizio!=null && data_fine==null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            query = query + "data_svolgimento>='"+data_inizio+"' AND data_svolgimento<='"+dtf.format(now.plusDays(6))+"'";
        }
        if((ora_inizio)!=null && (ora_fine)!=null){
            query = query + " AND ora_inizio>='"+ora_inizio+"' AND ora_fine<='"+ora_fine+"'";
        }
        if(nome_prenotazione!=null){
            query = query + " AND nome_evento='"+nome_prenotazione+"'";
        }
        if(matricola_docente!=null){
            query = query + " AND responsabile='"+matricola_docente+"'";
        }
        if(nome_aula!=null){
            query = query + " AND aula='"+nome_aula+"'";
        }

        Statement st = myConnection.createStatement();
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            risultati.add(new DB_Prenotazione(rs.getString("nome_evento"),rs.getString("descrizione_evento"),rs.getString("aula"),LocalTime.parse(rs.getString("ora_inizio")),LocalTime.parse(rs.getString("ora_fine")),rs.getInt("limite_partecipanti"),rs.getString("filtro"),rs.getString("edificio"),rs.getString("data_svolgimento"),rs.getInt("in_svolgimento"),rs.getInt("responsabile")));
        }
            return risultati;
    }






    }





