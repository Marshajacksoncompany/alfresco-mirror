
/*
 * 
 */

package org.alfresco.repo.cmis.ws;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

/**
 * This class was generated by Apache CXF 2.1.2
 * Fri Oct 23 11:53:52 BST 2009
 * Generated source version: 2.1.2
 * 
 */


@WebServiceClient(name = "ObjectService", 
                  wsdlLocation = "file:CMISWS-Service.wsdl",
                  targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/") 
public class ObjectService extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "ObjectService");
    public final static QName ObjectServicePort = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "ObjectServicePort");
    static {
        URL url = null;
        try {
            url = new URL("file:CMISWS-Service.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:CMISWS-Service.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public ObjectService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public ObjectService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ObjectService() {
        super(WSDL_LOCATION, SERVICE);
    }

    /**
     * 
     * @return
     *     returns ObjectServicePort
     */
    @WebEndpoint(name = "ObjectServicePort")
    public ObjectServicePort getObjectServicePort() {
        return super.getPort(ObjectServicePort, ObjectServicePort.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ObjectServicePort
     */
    @WebEndpoint(name = "ObjectServicePort")
    public ObjectServicePort getObjectServicePort(WebServiceFeature... features) {
        return super.getPort(ObjectServicePort, ObjectServicePort.class, features);
    }

}
