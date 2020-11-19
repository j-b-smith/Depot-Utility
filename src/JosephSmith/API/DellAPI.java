package JosephSmith.API;

import com.sun.xml.messaging.saaj.client.p2p.HttpSOAPConnectionFactory;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** Handles all SOAP API Messages*/
public abstract class DellAPI {

    //Dispatch Variables
    public static final String tokenEndpoint = "https://apigtwb2c.us.dell.com/auth/oauth/v2/token";
    public static final String dispatchEndpoint = "https://apigtwb2c.us.dell.com/PROD/support/dispatch/v3/service";
    public static final String technicalSupportEndpoint = "https://apigtwb2cnp.us.dell.com/Sandbox/support/case/v3/WebCase";
    public static final String techSupportSandboxTokenEndpoint = "https://apigtwb2cnp.us.dell.com/auth/oauth/v2/token";

    public static MessageFactory messageFactory;

    static {
        try {
            messageFactory = MessageFactory.newInstance();
        } catch (SOAPException e) {
            e.printStackTrace();
        }
    }

    /*
    Log Method
     */
    public static void logSOAP(SOAPMessage request, SOAPMessage response, String typeOfRequest) throws IOException, SOAPException {
        //Open log stream
        FileOutputStream logStream = new FileOutputStream("DellAPILog", true);

        //Write request to stream
        ByteArrayOutputStream requestStream = new ByteArrayOutputStream();
        request.writeTo(requestStream);
        requestStream.close();

        //Write response to stream
        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
        response.writeTo(responseStream);
        responseStream.close();

        //Create document builder and factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        try{
            //Create document builder
            builder = factory.newDocumentBuilder();

            //Create transformer factory
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            //Setup XML format
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            logStream.write("\n\n\n\n".getBytes());
            logStream.write("Request Type:\n".getBytes());
            logStream.write(typeOfRequest.getBytes());
            logStream.write("\nDate: \n".getBytes());
            logStream.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' HH:mm")).getBytes());
            logStream.write("\nRequest:\n".getBytes());

            //Create document
            Document requestDoc = builder.parse(new InputSource(new StringReader(requestStream.toString())));
            Document responseDoc = builder.parse(new InputSource(new StringReader(responseStream.toString())));

            //Write formatted XML to logStream
            transformer.transform(new DOMSource(requestDoc),
                    new StreamResult(new OutputStreamWriter(logStream, StandardCharsets.UTF_8)));

            logStream.write("\nResponse:\n".getBytes());

            transformer.transform(new DOMSource(responseDoc),
                    new StreamResult(new OutputStreamWriter(logStream, StandardCharsets.UTF_8)));

            //Close logStream
            logStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    Retrieve Value Methods
     */
    public static String getAttributeValue(String tagName, SOAPMessage response) throws IOException, SOAPException {
        //Store attribute value
        String attributeValue = "No Value Found";

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        response.writeTo(stream);

        //regex to retrieve value
        String openTagPattern = "<" + tagName + ">";
        String closeTagPattern = "</" + tagName + ">";
        Pattern pattern = Pattern.compile(Pattern.quote(openTagPattern) + "(.*?)" + Pattern.quote(closeTagPattern));
        Matcher matcher = pattern.matcher(stream.toString());

        //Find the value
        while (matcher.find()){
            attributeValue = matcher.group(1);
        }

        return attributeValue;
    }

    /**
     * Get the SOAP Message response from the API
     * @param message the message to send
     * @return the response message received
     * @throws SOAPException if the message fails
     */
    public static SOAPMessage getResponse(SOAPMessage message, String endpoint) throws SOAPException{

       //Create connection factory
       HttpSOAPConnectionFactory soapConnectionFactory = (HttpSOAPConnectionFactory) HttpSOAPConnectionFactory.newInstance();

       //Create connection
       SOAPConnection connection = soapConnectionFactory.createConnection();

       //Return response
        return connection.call(message, endpoint);

    }

}
