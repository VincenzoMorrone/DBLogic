package com.DBCompany;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Classe utilizzata come modello da passare come parametro
 */
public class DB_Prenotazione {
    private String nome_evento;
    private String descrizione_evento;
    private String aula;
    private LocalTime ora_inizio;
    private LocalTime ora_fine;
    private int limite_partecipanti;
    private String filtro = "100";
    private String edificio_evento;
    private String data_svolgimento;
    private int in_svolgimento;
    private int responsabile;

    public DB_Prenotazione(String nome_evento, String descrizione_evento, String aula, LocalTime ora_inizio, LocalTime ora_fine, int limite_partecipanti, String filtro, String edificio_evento, String data_svolgimento, int in_svolgimento, int responsabile) {
        this.nome_evento = nome_evento;
        this.descrizione_evento = descrizione_evento;
        this.aula = aula;
        this.ora_inizio = ora_inizio;
        this.ora_fine = ora_fine;
        this.limite_partecipanti = limite_partecipanti;
        this.filtro = filtro;
        this.edificio_evento = edificio_evento;
        this.data_svolgimento = data_svolgimento;
        this.in_svolgimento = in_svolgimento;
        this.responsabile = responsabile;
    }



    public int getIn_svolgimento() {
        return in_svolgimento;
    }

    public void setIn_svolgimento(int in_svolgimento) {
        this.in_svolgimento = in_svolgimento;
    }

    public int getResponsabile() {
        return responsabile;
    }

    public void setResponsabile(int ID) {
        this.responsabile = ID;
    }

    public String getData_svolgimento() {
        return data_svolgimento;
    }

    public void setData_svolgimento(String data_svolgimento) {
        this.data_svolgimento = data_svolgimento;
    }


    public String getEdificio_evento() {
        return edificio_evento;
    }

    public void setEdificio_evento(String edificio_evento) {
        this.edificio_evento = edificio_evento;
    }



    public String getNome_evento() {
        return nome_evento;
    }

    public void setNome_evento(String nome_evento) {
        this.nome_evento = nome_evento;
    }

    public String getDescrizione_evento() {
        return descrizione_evento;
    }

    public void setDescrizione_evento(String descrizione_evento) {
        this.descrizione_evento = descrizione_evento;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public LocalTime getOra_inizio() {
        return ora_inizio;
    }

    public void setOra_inizio(LocalTime ora_inizio) {
        this.ora_inizio = ora_inizio;
    }

    public LocalTime getOra_fine() {
        return ora_fine;
    }

    public void setOra_fine(LocalTime ora_fine) {
        this.ora_fine = ora_fine;
    }

    public int getLimite_partecipanti() {
        return limite_partecipanti;
    }

    public void setLimite_partecipanti(int limite_partecipanti) {
        this.limite_partecipanti = limite_partecipanti;
    }

    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
    }

    @Override
    public String toString(){
        return new StringBuilder()
                .append("Nome evento: "+this.getNome_evento()+ System.lineSeparator())
                .append("Descrizione Evento: "+this.getDescrizione_evento()+ System.lineSeparator())
                .append("Aula: "+this.getAula()+ System.lineSeparator())
                .append("Ora Inizio: "+this.getOra_inizio()+ System.lineSeparator())
                .append("Ora Fine: "+this.getOra_fine()+ System.lineSeparator())
                .append("Max Capacita: "+this.getLimite_partecipanti()+ System.lineSeparator())
                .append("Filtro: "+this.getFiltro()+ System.lineSeparator())
                .append("Edificio: "+this.getEdificio_evento()+ System.lineSeparator())
                .append("Data svolgimento: "+this.getData_svolgimento()+ System.lineSeparator())
                .append("In svolgimento?: " +this.getIn_svolgimento()+ System.lineSeparator())
                .append(System.lineSeparator())
                .toString();
    }

}
