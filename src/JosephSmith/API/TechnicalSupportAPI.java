package JosephSmith.API;

import JosephSmith.Database.Database;
import JosephSmith.model.Token;
import jakarta.xml.soap.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Properties;

public class TechnicalSupportAPI extends DellAPI {

    private static final String webCaseEndpoint = "https://apigtwb2cnp.us.dell.com/Sandbox/support/case/v3/WebCase";
    private static final String searchCaseLiteEndpoint = "https://apigtwb2cnp.us.dell.com/Sandbox/support/case/v3/searchcaselite";
    private static final String getCaseLiteEndpoint = "https://apigtwb2cnp.us.dell.com/Sandbox/External/V3/Case/Update/CommonSyncCaseManagementUpdate";
    private static Token token;

    //Register client
    //Registered under my credentials, can other techs still submit requests?? Ask Deepa
    public static void registerClient() throws IOException, SOAPException {
        //Check if the token has expired
        checkToken();

        //Namespace
        String namespace = "ser";

        //Create message
        SOAPMessage soapMessage = messageFactory.createMessage();

        //Add headers
        soapMessage.getMimeHeaders().addHeader("Authorization", "Bearer " + token.getBearerToken());
        soapMessage.getMimeHeaders().addHeader("Content-Type", "text/xml");

        //Envelope
        SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
        soapEnvelope.addNamespaceDeclaration(namespace, "http://ph.services.dell.com/Server/");

            //Body
            SOAPBody soapBody = soapEnvelope.getBody();

                SOAPElement alertRequest = soapBody.addChildElement("AlertRequest", namespace);

                    SOAPElement sourceHeader = alertRequest.addChildElement("SourceHeader");

                        SOAPElement clientId = sourceHeader.addChildElement("ClientId");
                        clientId.addTextNode("0");

                        SOAPElement clientType = sourceHeader.addChildElement("ClientType");
                        clientType.addTextNode("HelpDesk");

                        SOAPElement clientHostName = sourceHeader.addChildElement("ClientHostName");
                        clientHostName.addTextNode("DaveBreckPC.Dell.COM");

                        SOAPElement clientIPAddress = sourceHeader.addChildElement("ClientIPAddress");
                        clientIPAddress.addTextNode("192.192.192.192");

                        SOAPElement requestId = sourceHeader.addChildElement("RequestId");
                        requestId.addTextNode("12348");

                    SOAPElement customerHeader = alertRequest.addChildElement("CustomerHeader");

                        SOAPElement companyName = customerHeader.addChildElement("CompanyName");
                        companyName.addTextNode("Dell API Support");

                        SOAPElement countryCodeISO = customerHeader.addChildElement("CountryCodeISO");
                        countryCodeISO.addTextNode("USA");

                        SOAPElement emailOptIn = customerHeader.addChildElement("EmailOptIn");
                        emailOptIn.addTextNode("david_breck@dell.com");

                        SOAPElement primaryContact = customerHeader.addChildElement("PrimaryContact");


                            SOAPElement firstName = primaryContact.addChildElement("FirstName");
                            firstName.addTextNode("Dave");

                            SOAPElement lastName = primaryContact.addChildElement("LastName");
                            lastName.addTextNode("Breck");

                            SOAPElement country = primaryContact.addChildElement("Country");
                            country.addTextNode("United States of America");

                            SOAPElement timeZone = primaryContact.addChildElement("TimeZone");
                            timeZone.addTextNode("-0500");

                            SOAPElement phoneNumber1 = primaryContact.addChildElement("PhoneNumber1");
                            phoneNumber1.addTextNode("512-723-0789");

                            SOAPElement emailAddress = primaryContact.addChildElement("EmailAddress");
                            emailAddress.addTextNode("david_breck@dell.com");

                            SOAPElement preferContactMethod = primaryContact.addChildElement("PreferContactMethod");
                            preferContactMethod.addTextNode("Email");

                            SOAPElement preferLanguage = primaryContact.addChildElement("PreferLanguage");
                            preferLanguage.addTextNode("EN");

                    SOAPElement webCaseOperation = alertRequest.addChildElement("WebCaseOperation");

                        SOAPElement operation = webCaseOperation.addChildElement("Operation");
                        operation.addTextNode("REGISTER_CLIENT");

        //Save the message
        soapMessage.saveChanges();

        //Get response
        SOAPMessage response = getResponse(soapMessage, technicalSupportEndpoint);

        logSOAP(soapMessage, response, "Register Client");
    }

    //Enroll devices
    //Enrolled under my credentials, can other techs still submit requests?? Ask Deepa
    public static void enrollDevices() throws IOException, SOAPException {
        //Check if the token has expired
        checkToken();

        //Namespace
        String namespace = "ser";

        //Create message
        SOAPMessage soapMessage = messageFactory.createMessage();

        //Add headers
        soapMessage.getMimeHeaders().addHeader("Authorization", "Bearer " + token.getBearerToken());
        soapMessage.getMimeHeaders().addHeader("Content-Type", "text/xml");

        //Envelope
        SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
        soapEnvelope.addNamespaceDeclaration(namespace, "http://ph.services.dell.com/Server/");

            //Body
            SOAPBody soapBody = soapEnvelope.getBody();

                SOAPElement deviceRequest = soapBody.addChildElement("DeviceRequest", namespace);

                    SOAPElement sourceHeader = deviceRequest.addChildElement("SourceHeader");

                        SOAPElement clientId = sourceHeader.addChildElement("ClientId");
                        clientId.addTextNode("46");

                        SOAPElement clientType = sourceHeader.addChildElement("ClientType");
                        clientType.addTextNode("HELPDESK");

                        SOAPElement clientHostName = sourceHeader.addChildElement("ClientHostName");
                        clientHostName.addTextNode("ABCDEF");

                        SOAPElement clientIPAddress = sourceHeader.addChildElement("ClientIPAddress");
                        clientIPAddress.addTextNode("192.1.0.0");

                        SOAPElement requestId = sourceHeader.addChildElement("RequestId");
                        requestId.addTextNode("1000");

                    SOAPElement clientInfo = deviceRequest.addChildElement("ClientInfo");

                        SOAPElement companyName = clientInfo.addChildElement("CompanyName", namespace);
                        companyName.addTextNode("Dell API Support");

                        SOAPElement clientIPAddress2 = clientInfo.addChildElement("ClientIPAddress", namespace);
                        clientIPAddress2.addTextNode("192.1.0.1");

                        SOAPElement primaryPhoneNumber = clientInfo.addChildElement("PrimaryPhoneNumber", namespace);
                        primaryPhoneNumber.addTextNode("1-111-111-1111");


                        SOAPElement primaryEmailAddress = clientInfo.addChildElement("PrimaryEmailAddress", namespace);
                        primaryEmailAddress.addTextNode("XXX_YYYY@dell.com");


                SOAPElement deviceInfo = deviceRequest.addChildElement("DeviceInfo");

                //Loop for multiple machines
                SOAPElement device = deviceInfo.addChildElement("Device");

                    SOAPElement serviceTag = device.addChildElement("ServiceTag");
                    serviceTag.addTextNode("123AB45");

                    SOAPElement deviceName = device.addChildElement("DeviceName");
                    deviceName.addTextNode("WebServer-Mac1");

                    SOAPElement deviceModel = device.addChildElement("DeviceModel");
                    deviceModel.addTextNode("E7240");

                    SOAPElement deviceOS = device.addChildElement("OS");
                    deviceOS.addTextNode("Windows 8");

        //Save the message
        soapMessage.saveChanges();

        //Get response
        SOAPMessage response = getResponse(soapMessage, technicalSupportEndpoint);

        logSOAP(soapMessage, response, "Enroll Devices");
    }

    //Create new service request
    public static void createSupportRequest() throws IOException, SOAPException {
        //Check if the token has expired
        checkToken();

        //Namespace
        String namespace = "ser";

        //Create message
        SOAPMessage soapMessage = messageFactory.createMessage();

        //Add headers
        soapMessage.getMimeHeaders().addHeader("Authorization", "Bearer " + token.getBearerToken());
        soapMessage.getMimeHeaders().addHeader("Content-Type", "text/xml");

        //Envelope
        SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
        soapEnvelope.addNamespaceDeclaration(namespace, "http://ph.services.dell.com/Server/");

            //Body
            SOAPBody soapBody = soapEnvelope.getBody();

                SOAPElement alertRequest = soapBody.addChildElement("AlertRequest", namespace);

                    SOAPElement sourceHeader = alertRequest.addChildElement("SourceHeader");

                        SOAPElement clientId = sourceHeader.addChildElement("ClientId");
                        clientId.addTextNode("730634");

                        SOAPElement clientType = sourceHeader.addChildElement("ClientType");
                        clientType.addTextNode("helpdesk");

                        SOAPElement clientHostName = sourceHeader.addChildElement("ClientHostName");
                        clientHostName.addTextNode("ABC.Dell.COM");

                        SOAPElement clientIPAddress = sourceHeader.addChildElement("ClientIPAddress");
                        clientIPAddress.addTextNode("192.192.192.192");

                        SOAPElement requestId = sourceHeader.addChildElement("RequestId");
                        requestId.addTextNode("12348");

                    SOAPElement customerHeader = alertRequest.addChildElement("CustomerHeader");

                        SOAPElement companyName = customerHeader.addChildElement("CompanyName");
                        companyName.addTextNode("API Test, Inc.");

                        SOAPElement countryCodeISO = customerHeader.addChildElement("CountryCodeISO");
                        countryCodeISO.addTextNode("USA");

                        SOAPElement emailOptIn = customerHeader.addChildElement("EmailOptIn");
                        emailOptIn.addTextNode("true");

                        SOAPElement primaryContact = customerHeader.addChildElement("PrimaryContact");


                            SOAPElement firstName = primaryContact.addChildElement("FirstName");
                            firstName.addTextNode("Deepa");

                            SOAPElement lastName = primaryContact.addChildElement("LastName");
                            lastName.addTextNode("Chandra");

                            SOAPElement country = primaryContact.addChildElement("Country");
                            country.addTextNode("United States of America");

                            SOAPElement timeZone = primaryContact.addChildElement("TimeZone");
                            timeZone.addTextNode("-0500");

                            SOAPElement phoneNumber1 = primaryContact.addChildElement("PhoneNumber1");
                            phoneNumber1.addTextNode("1-111-111-1111");

                            SOAPElement emailAddress = primaryContact.addChildElement("EmailAddress");
                            emailAddress.addTextNode("ABC@Dell.com");

                            SOAPElement preferContactMethod = primaryContact.addChildElement("PreferContactMethod");
                            preferContactMethod.addTextNode("Email");

                            SOAPElement preferLanguage = primaryContact.addChildElement("PreferLanguage");
                            preferLanguage.addTextNode("EN");

                    SOAPElement alertData = alertRequest.addChildElement("AlertData");


                        SOAPElement eventId = alertData.addChildElement("EventId");
                        eventId.addTextNode("2");

                        SOAPElement trapId = alertData.addChildElement("TrapId");
                        trapId.addTextNode("0");

                        SOAPElement eventSource = alertData.addChildElement("EventSource");
                        eventSource.addTextNode("Server");

                        SOAPElement severity = alertData.addChildElement("Severity");
                        severity.addTextNode("3");

                        SOAPElement caseSeverity = alertData.addChildElement("CaseSeverity");
                        caseSeverity.addTextNode("Medium");

                        SOAPElement message = alertData.addChildElement("Message");
                        message.addTextNode("Test Case. Please ignore.");

                        SOAPElement timestamp = alertData.addChildElement("Timestamp");
                        timestamp.addTextNode("2019-03-11T014:13:02-0500");

                        SOAPElement serviceTag = alertData.addChildElement("ServiceTag");
                        serviceTag.addTextNode("123AB45");

                        SOAPElement deviceName = alertData.addChildElement("DeviceName");
                        deviceName.addTextNode("Test Case");

                        SOAPElement deviceIP = alertData.addChildElement("DeviceIP");
                        deviceIP.addTextNode("1.1.1.1");

                        SOAPElement deviceModel = alertData.addChildElement("DeviceModel");
                        deviceModel.addTextNode("Latitude 5480");

                        SOAPElement deviceType = alertData.addChildElement("DeviceType");
                        deviceType.addTextNode("Server");

                        SOAPElement deviceOS = alertData.addChildElement("OS");
                        deviceOS.addTextNode("Windows 10 Enterprise");

                        SOAPElement diagnosticsOptIn = alertData.addChildElement("DiagnosticsOptIn");
                        diagnosticsOptIn.addTextNode("false");

                    SOAPElement webCaseOperation = alertRequest.addChildElement("WebCaseOperation");

                        SOAPElement operation = webCaseOperation.addChildElement("Operation");
                        operation.addTextNode("ALERTS");


        //Save the message
        soapMessage.saveChanges();

        //Get response
        SOAPMessage response = getResponse(soapMessage, technicalSupportEndpoint);

        logSOAP(soapMessage, response, "Create Support Request");
    }

    //Append notes to open service request
    public static void addNotesToRequest(){}

    //Returns service request number and status of request
    public static void queryCaseHeader(){}

    //Returns detailed information of request
    public static void queryCaseDetails(){}

    //Returns a list of all service requests associated with a service tag
    public static void queryCaseHistory(){}

    /*
    Handle Token
     */
    /**
     * Check if the token has expired and generate new token if so
     * @throws IOException
     */
    private static void checkToken() throws IOException {

        //Get the token from the database
        token = Database.getToken("Technical Support API");

        //Check if the token is expired
        if (token.getTokenExpiration().isBefore(LocalDateTime.now())){

            //Get new token
            getToken();
        }
    }

    /**
     * Get the token from Http request
     * @throws IOException
     */
    public static void getToken() throws IOException {

        //Create properties and load file
        Properties properties = new Properties();
        FileInputStream config = new FileInputStream("src/JosephSmith/Resources/config.properties");
        properties.load(config);

        //Create the http client
        try (CloseableHttpClient client = HttpClients.createDefault()){
            //Create the http request
            HttpUriRequest request = RequestBuilder.create("POST")
                    .setUri(techSupportSandboxTokenEndpoint)
                    //Create the request body
                    .setEntity(new StringEntity("grant_type=client_credentials&" +
                            "client_id=" + properties.get("technicalSupportClientID") +
                            "&client_secret=" + properties.get("technicalSupportClientSecret")))
                    //Add header
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            //Get the http response
            HttpResponse response = client.execute(request);

            //Get the content
            InputStream inputStream = response.getEntity().getContent();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            //Get the bearer token
            bufferedReader.lines().forEach(line -> {
                if(line.contains("access_token")){
                    //Update the bearer token
                    token.setBearerToken(line.substring(18, 54));
                    //Update the expiration time
                    token.setTokenExpiration(LocalDateTime.now().plusSeconds(3600));
                    //Update the token in the database
                    Database.updateToken(token, "Technical Support API");
                }
            });
        }
    }
}
