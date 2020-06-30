package com.DBCompany;

import org.omg.CORBA.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe utilizzata come modello da passare come parametro
 */
public class DB_Aula {
    String nome;
    String edificio;
    int disponibilita;
    int capacita;
    int id_segnaleout;
    String servizi;

    public DB_Aula(String nome, String edificio, int disponibilita, int capacita, int id_segnaleout){
        this.nome=nome;
        this.edificio=edificio;
        this.disponibilita=disponibilita;
        this.capacita=capacita;
        this.id_segnaleout=id_segnaleout;

    }

    public void setServizi(String listaServizi){
        servizi=listaServizi;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEdificio() {
        return edificio;
    }

    public void setEdificio(String edificio) {
        this.edificio = edificio;
    }

    public int getDisponibilita() {
        return disponibilita;
    }

    public void setDisponibilita(int disponibilita) {
        this.disponibilita = disponibilita;
    }

    public int getCapacita() {
        return capacita;
    }

    public void setCapacita(int capacita) {
        this.capacita = capacita;
    }

    public int getId_segnaleout() {
        return id_segnaleout;
    }

    public void setId_segnaleout(int id_segnaleout) {
        this.id_segnaleout = id_segnaleout;
    }

    public String getServizi(){
        return servizi;
    }

    @Override
    public String toString(){
        return new StringBuilder()
                .append("Nome aula:"+this.getNome()+ System.lineSeparator())
                .append(" Nome edificio:"+this.getEdificio()+ System.lineSeparator())
                .append(" Disponibile:"+this.getDisponibilita()+ System.lineSeparator())
                .append(" Capacita:"+this.getDisponibilita()+ System.lineSeparator())
                .append(" ID Segnale esterno:"+this.getId_segnaleout()+ System.lineSeparator()).toString();

    }

}
