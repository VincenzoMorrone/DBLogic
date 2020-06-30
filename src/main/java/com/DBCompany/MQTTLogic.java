package com.DBCompany;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.sql.Connection;

/**
 * Classe che si occupa della comunicazione MQTT, si occupa della sottoscrizione ai vari topic.
 * MQTTLogic ha un funzionamento a se stante dalle altre classi, questa classe funziona sia da Subscriber che da
 * Publisher. Questo perchè la classe è in ascolto delle richieste inviate dal dispositivo di rilevamento
 * presenze in aula (su topic specifico) ed, allo stesso tempo è publisher dell'informazione sul grado di
 *  capienza dell'aula.
 *  ATTENZIONE: Per simulare l'entrata ed uscita da una stanza è possibile usare il progetto pubblicato su github,
 *  completamente scollegato da questo che simula l'entrata di un utente e l'uscita dopo pochi secondi. Da precisare
 *  che il software che effettua questa azione esegue solo quell'operazione, nient'altro, proprio per dimostrare
 *  l'indipendenza delle parti. La comunicazione si basa su messaggi con QoS impostato a 2. Questa classe fornisce
 *  quindi anche il dato per il sistema IoT, quindi sarà compito del modulo IoT catturare l'informazione e mostrarla.
 */
public class MQTTLogic {
    private static Connection myConnection;
    private MqttClient client;
    public MQTTLogic() throws MqttException {
        String broker_url = "tcp://iotlabgw.edu-al.unipmn.it";
        String clientId = MqttClient.generateClientId();
        client = new MqttClient(broker_url,clientId);
    }


    public void start() throws MqttException {
        client.setCallback(new SubscribeCallBack());
        client.connect();
        final String Ingressi = "GRUPPO3VC/INGRESSI";
        final String Uscite = "GRUPPO3VC/USCITE";
        client.subscribe(Ingressi);
        client.subscribe(Uscite);
        System.out.println("Sottoscritto ai topic "+Ingressi+Uscite);
    }

    public static void main(String args[]) throws MqttException {
        MQTTLogic subscriber = new MQTTLogic();
        subscriber.start();

    }
}
