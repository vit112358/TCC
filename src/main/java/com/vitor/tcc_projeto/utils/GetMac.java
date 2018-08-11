package com.vitor.tcc_projeto.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetMac {

    private String macAddress;
    private static final Logger l = Logger.getLogger(GetMac.class.getName());

    public GetMac() {
        this.macAddress="";
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String returnMac(){
        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            byte[] mac = ni.getHardwareAddress();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                builder.append(String.format("%02X-", mac[i]));
            }
            macAddress=builder.toString();
            l.log(Level.FINE, macAddress.substring(0, macAddress.length()-1));
        } catch (UnknownHostException | SocketException e) {
            l.severe("Erro ao Obter Mac: "+e.getMessage());
        }


        return macAddress;
    }
}
