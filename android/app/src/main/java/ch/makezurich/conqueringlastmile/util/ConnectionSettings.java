package ch.makezurich.conqueringlastmile.util;

import java.security.cert.Certificate;
import java.util.List;

import ch.makezurich.ttnandroidapi.mqtt.api.AndroidTTNClient;

public class ConnectionSettings {
    private boolean tlsEnabled;
    private String protocol;
    private List<Certificate> clientCertificates;

    public ConnectionSettings(boolean tlsEnabled, AndroidTTNClient client) {
        this.tlsEnabled = tlsEnabled;
        if (tlsEnabled) {
            protocol = "TLS";
        } else {
            protocol = "TCP";
        }

        clientCertificates = client.getClientCertificates();
    }

    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    public String getProtocol() {
        return protocol;
    }

    public List<Certificate> getClientCertificates() {
        return clientCertificates;
    }
}
