package com.DBCompany;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttSubscribe;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Clase interna da non richiamare, utilizzata per i metodi della comunicazione MQTT.
 */
public class SubscribeCallBack implements MqttCallback {

    @Override
    public void connectionLost(Throwable cause) {

    }



    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if(topic.equals("GRUPPO3VC/INGRESSI")){
             DBConnection db = new DBConnection();
             System.out.println("Messaggio Ingresso ricevuto: "+message.toString());
            db.MQTTaddToClassroom(message.toString());

                MqttClient client;
                String broker_url = "tcp://localhost:1883";
                String clientId = MqttClient.generateClientId();
                client = new MqttClient(broker_url,clientId);
                client.connect();
                MqttTopic signal = client.getTopic("GRUPPO3VC/SIGNALS");
                MqttMessage mex = new MqttMessage(db.MQTTcapienzaEdAula(message.toString()).getBytes());
                mex.setQos(2);
                signal.publish(new MqttMessage(db.MQTTcapienzaEdAula(message.toString()).getBytes()));
                client.disconnect();

        }
        if(topic.equals("GRUPPO3VC/USCITE")){
            DBConnection db = new DBConnection();
            System.out.println("Messaggio Uscita ricevuto: "+message.toString());
            db.MQTTremFromClassroom(message.toString());

            MqttClient client;
            String broker_url = "tcp://localhost:1883";
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(broker_url,clientId);
            client.connect();
            MqttTopic signal = client.getTopic("GRUPPO3VC/SIGNALS");
            MqttMessage mex = new MqttMessage(db.MQTTcapienzaEdAula(message.toString()).getBytes());
            mex.setQos(2);
            signal.publish(new MqttMessage(db.MQTTcapienzaEdAula(message.toString()).getBytes()));
            client.disconnect();
        }
        if(topic.equals("GRUPPO3VCLASTWILL")){
            System.out.println("CADUTA CONNESSIONE");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


}
