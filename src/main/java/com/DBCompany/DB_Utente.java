package com.DBCompany;

import java.time.LocalDate;
/**
 * Classe utilizzata come modello da passare come parametro
 * ATTENZIONE: E' importante specificare che seppur esiste il metodo setMatricola, questo non viene mai utilizzato,
 * proprio perchè il numero della matricola è generato dal database mediante ID auto-incrementale. Questo ID parte da 20020000
 * e tale valore iniziale è settato dal file SQL di configurazione del database.
 */
public class DB_Utente {

    String matricola;
    String nome;
    String cognome;
    String data_nascita;
    int privilegio=-1;
    boolean accesso_consentito = true;
    String password;
    String email;
    public DB_Utente(String nome, String cognome,String data_nascita, String email){
        this.nome = nome;
        this.cognome = cognome;
        this.data_nascita= data_nascita;
        this.email = email;
    }

    public void setPassword(String psswd) { this.password = psswd;}

    public String getPassword(){return this.password;}

    public String getMatricola() {
        return matricola;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getData_nascita() {
        return data_nascita;
    }

    public int getPrivilegio() {
        return privilegio;
    }

    public boolean isAccesso_consentito() {
        return accesso_consentito;
    }

    public String getEmail() {
        return email;
    }

    public void setMatricola(String matricola) {
        this.matricola = matricola;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public void setData_nascita(String data_nascita) {
        this.data_nascita = data_nascita;
    }

    public void setPrivilegio(int privilegio) {
        this.privilegio = privilegio;
    }

    public void setAccesso_consentito(boolean accesso_consentito) {
        this.accesso_consentito = accesso_consentito;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    @Override
    public String toString(){
        return new StringBuilder()
                .append(this.getMatricola())
                .append(this.getNome())
                .append(this.getCognome())
                .append(this.getEmail())
                .append(this.getData_nascita())
                .append(this.getPrivilegio())
                .append(System.lineSeparator())
                .toString();
    }




}
