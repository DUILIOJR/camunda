package com.redeempresarial.custom.connector;

import connectjar.org.apache.http.config.Registry;
import connectjar.org.apache.http.config.RegistryBuilder;
import connectjar.org.apache.http.conn.socket.ConnectionSocketFactory;
import connectjar.org.apache.http.conn.socket.PlainConnectionSocketFactory;
import connectjar.org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import connectjar.org.apache.http.conn.ssl.SSLContexts;
import connectjar.org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import connectjar.org.apache.http.impl.client.CloseableHttpClient;
import connectjar.org.apache.http.impl.client.HttpClientBuilder;
import connectjar.org.apache.http.impl.client.HttpClients;
import connectjar.org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.camunda.connect.httpclient.HttpConnector;
import org.camunda.connect.spi.ConnectorConfigurator;
import org.camunda.connect.httpclient.impl.AbstractHttpConnector;

public class HttpConnectorConfigurator implements ConnectorConfigurator<HttpConnector> {

    @Override
    public Class<HttpConnector> getConnectorClass() {
        return HttpConnector.class;
    }

    @Override
    public void configure(HttpConnector connector) {

        try {

            System.setProperty("jsse.enableSNIExtension", "false");

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream instream = new FileInputStream(new File("/usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts"));
            //FileInputStream instream = new FileInputStream(new File("C:\\Program Files\\Java\\jdk1.8.0_161\\jre\\lib\\security\\cacerts"));
            trustStore.load(instream, "changeit".toCharArray());

            SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                    .build();

            instream.close();

            SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", sslConnectionFactory)
                    .register("http", new PlainConnectionSocketFactory())
                    .build();

            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(registry);
            connManager.setMaxTotal(3000);

            HttpClientBuilder builder = HttpClients.custom().setConnectionManager(connManager);

            builder.setSSLSocketFactory(sslConnectionFactory);

            CloseableHttpClient client = builder.build();

            ((AbstractHttpConnector) connector).setHttpClient(client);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
