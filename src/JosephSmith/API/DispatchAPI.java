package JosephSmith.API;

import JosephSmith.Database.Database;
import JosephSmith.model.DispatchMachine;
import JosephSmith.model.Token;
import jakarta.xml.soap.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.NodeList;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatchAPI extends DellAPI {


    private static final String dispatchAPIName = "Dispatch API";
    private static Token dispatchToken;


    /*
Dispatch Methods
 */
    /*
    Doc:

    Request:
    *Login Contains the technician user ID for the inquiry
    *Password Contains the password of the technician performing the inquiry\

    Sample Request:

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
     xmlns:api="http://api.dell.com">
        <soapenv:Header/>
            <soapenv:Body>
                <api:CheckUser>
                    <api:CheckUserRequest>
                        <api:Login>apiuser_us_tech01@uatmail.com</api:Login>
                        <api:Password>TechDirectAPI#1</api:Password>
                    </api:CheckUserRequest>
                </api:CheckUser>
            </soapenv:Body>
        </soapenv:Envelope>

    Response:
    -FullName Returns the technician’s full name in the format of Lastname, Firstname
    -Inactive Returns true/false based on whether the user account is active
    -Locked Returns true/false based on whether the user account is locked
    -Role Returns the role associated with the user account

    Sample Response:

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        <soap:Body>
            <CheckUserResponse xmlns="http://api.dell.com">
                <CheckUserResult>
                    <LoginResult>
                        <FullName>One, Technician</FullName>
                        <Role>CompanyUser</Role>
                        <Inactive>false</Inactive>
                        <Locked>false</Locked>
                    </LoginResult>
                </CheckUserResult>
            </CheckUserResponse>
        </soap:Body>
    </soap:Envelope>
     */
    public static void checkUser(String technicianEmail, String technicianPassword) throws SOAPException, IOException {

        //Check if the token has expired
        checkToken();

        //Namespace
        String namespace = "api";

        //Create message
        SOAPMessage soapMessage = messageFactory.createMessage();

        //Add headers
        soapMessage.getMimeHeaders().addHeader("Authorization", "Bearer " + dispatchToken.getBearerToken());
        soapMessage.getMimeHeaders().addHeader("SOAPAction", "http://api.dell.com/IDispatchService/CheckUser");

        //Envelope
        SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
        soapEnvelope.addNamespaceDeclaration(namespace, "http://api.dell.com");

        //Body
        SOAPBody soapBody = soapEnvelope.getBody();

        //Add the checkUser element to the body
        SOAPElement checkUser = soapBody.addChildElement("CheckUser", namespace);

        //Add the checkUserRequest element to the checkUser element
        SOAPElement checkUserRequest = checkUser.addChildElement("CheckUserRequest", namespace);

        //Add login element to the checkUserRequest element
        SOAPElement login = checkUserRequest.addChildElement("Login", namespace);

        //technicianEmail
        login.addTextNode(technicianEmail);

        //Add password element to checkUserRequest
        SOAPElement password = checkUserRequest.addChildElement("Password", namespace);

        //technicianPassword
        password.addTextNode(technicianPassword);

        //Save the message
        soapMessage.saveChanges();

        //Get response
        SOAPMessage response = getResponse(soapMessage, dispatchEndpoint);

    }

    /*
    Doc:

    Request:
    *Login Contains the technician user ID for the inquiry
    *Password Contains the password of the technician performing the inquiry

    Sample Request:

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:api="http://api.dell.com">
        <soapenv:Header/>
            <soapenv:Body>
                <api:CheckLogin>
                    <api:CheckLoginRequest>
                        <api:Login>apiuser_us_tech01@uatmail.com</api:Login>
                        <api:Password>TechDirectAPI#1</api:Password>
                    </api:CheckLoginRequest>
                </api:CheckLogin>
            </soapenv:Body>
        </soapenv:Envelope>

    Response:
    -FullName Returns the technician’s full name in the format of Lastname, Firstname
    -Role Returns the access role associated with the login (for example LogisticsTechnician, Technician, SPOC)
    -HomeBranch Returns the Home (default) branch associated with the technician
    -Relationships Returns the relationships established between the service
        provider and one or more end user customers. This information
        includes:
            -BranchName – The name associated with a dispatching branch
            -CustomerName – the name associated with the end user
                customer
            -Track – The type of dispatching relationship (ie: Tier 1, Tier 2, etc)
    -Certificates Returns a collection of information about the technician’s
        certifications including
        -Certificate - The type of certification
        -ExpirationDate – The expiration date associated with the certification

     Sample Response:

     <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        <soap:Body>
            <CheckLoginResponse xmlns="http://api.dell.com">
                <CheckLoginResult>
                    <LoginResult>
                        <FullName>One, Technician</FullName>
                        <Role>CompanyUser</Role>
                        <HomeBranch>TechDirect API Customer Account</HomeBranch>
                        <Relationships>
                            <RelationshipInfo>
                                <BranchName>US Group</BranchName>
                                <CustomerName>Atlanta customer</CustomerName>
                                <Track>Tier 1</Track>
                            </RelationshipInfo>
                            <RelationshipInfo>
                                <BranchName>US Group</BranchName>
                                <CustomerName>Round Rock Customer</CustomerName>
                                <Track>Tier 1</Track>
                            </RelationshipInfo>
                        </Relationships>
                        <Certificates>
                            <CertificateInfo>
                                <Certificate>Certified</Certificate>
                                <ExpirationDate>2021-01-24T00:00:00-05:00</ExpirationDate>
                            </CertificateInfo>
                        </Certificates>
                        <DSP>false</DSP>
                    </LoginResult>
                </CheckLoginResult>
            </CheckLoginResponse>
        </soap:Body>
    </soap:Envelope>
     */
    public static boolean checkLogin(String technicianEmail, String technicianPassword) throws SOAPException, IOException {

        //Check if the token has expired
        checkToken();

        //Namespace
        String namespace = "api";

        //Create message factory
        MessageFactory messageFactory = jakarta.xml.soap.MessageFactory.newInstance();

        //Create message
        SOAPMessage soapMessage = messageFactory.createMessage();

        //Add headers
        soapMessage.getMimeHeaders().addHeader("Authorization", "Bearer " + dispatchToken.getBearerToken());
        soapMessage.getMimeHeaders().addHeader("SOAPAction", "http://api.dell.com/IDispatchService/CheckLogin");

        //Get soap part
        SOAPPart soapPart = soapMessage.getSOAPPart();

        //Envelope
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        SOAPHeader header = soapEnvelope.getHeader();
        soapEnvelope.addNamespaceDeclaration(namespace, "http://api.dell.com");


        //Body
        SOAPBody soapBody = soapEnvelope.getBody();

        //Add the checkLogin element to the body
        SOAPElement checkLogin = soapBody.addChildElement("CheckLogin", namespace);

        //Add the checkLoginRequest element to the checkLogin element
        SOAPElement checkLoginRequest = checkLogin.addChildElement("CheckLoginRequest", namespace);

        //Add login element to the checkUserRequest element
        SOAPElement login = checkLoginRequest.addChildElement("Login", namespace);
        //technicianEmail
        login.addTextNode(technicianEmail);

        //Add password element to checkUserRequest
        SOAPElement password = checkLoginRequest.addChildElement("Password", namespace);
        //technicianPassword
        password.addTextNode(technicianPassword);

        //Save the message
        soapMessage.saveChanges();
        SOAPMessage response = getResponse(soapMessage, dispatchEndpoint);


        //log the soap request and response
        logSOAP(soapMessage, response, "Check Login");

        //Return the result of the check
        return !getAttributeValue("faultstring", response).contains("Invalid Login/Password");

    }

    /*
    Doc:

    Request:
    *Login Contains the technician user ID for the inquiry Yes
    *Password Contains the technicianPassword of the technician performing the inquiry
    *TechEmail Contains the technicianEmail of the technician responsible for the dispatch. In the case of a
        logistics user this may be different from the login technicianEmail
    *Branch Contains the branch associated with the dispatch. Note that the technician must be
        authorized to dispatch parts from this branch
    *Customer Contains the customer associated with the dispatch. Note that the customer must have a
        valid relationship with the branch in order to successfully dispatch
    *Track Contains the track associated with the Branch to Customer relationship (this can be obtained
        via the CheckLogin results)
    *ServiceTag Contains the service tag associated with the dispatch
    *PrimaryContactName Contains the primary contact associated with the dispatch.
    *PrimaryContactPhone Contains the primary contact phone number for the dispatch
    *PrimaryContactEmail Contains the primary contact technicianEmail associated with the dispatch
    -AlternativeContactName Contains an additional alternate contact name No
    -AlternativeContactPhone Contains an additional alternate contact number
    -AddressBookName Contains the name of a personal or company address book entry. If this is specified, the
        detail address parameters are not allowed(CountryISOCode thru TimeZone)
    -CountryISOCode Contains the ISO country code for the ship to address
    -City Contains the ship to city
    -State Contains the ship to state
    -ZipPostalCode Contains the ship to zip or postal code
    -AddressLine1 Contains the first ship to address line
    -AddressLine2 Contains the second ship to address line
    -Addressline3 Contains the third ship to address line
    *TimeZone Valid TimeZone format (eg. US/Central for Central America).
        Please refer Section 9 for list of valid TimeZones.
    *RequestCompleteCare A true/false parameter indicating if accidental damage applies to this dispatch
    *RequestReturnToDepot A true/false parameter indicating if return to depot applies to this dispatch
    *RequestOnSiteTechnician A true/false parameter indicating if an onsite technician has been requested
    -ReferencePONumber An optional purchase order or internal reference number
    *TroubleshootingNote Contains troubleshooting notes, limited to 1000 characters
    *Parts A collection of part information associated with the dispatch. This is limited to a
        maximum of 4 parts per dispatch
    *PartNumber Contains a valid DOSD Commodity part
    -PPID Contains a PPID associated with the part being replaced. This is required for Monitors,
        Batteries and Port Replicators and optional for other parts
    *Quantity Contains the quantity of parts requested
    -AttachmentInfo A collection of attachments associated with the dispatch.
        -Description Description of the attachment
        -FileName File name of the attachment
        -MIMEType Mime type associated with the attachment
        -Data Base 64 encoded attachment.

    Sample Request:

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:tem="http://api.dell.com">
        <soapenv:Header/>
            <soapenv:Body>
                <tem:CreateDispatch>
                    <tem:CreateDispatchRequest>
                        <tem:Login>apiuser_apj_tech01@uatmail.com</tem:Login>
                        <tem:Password>TechDirectAPI#1</tem:Password>
                        <tem:Dispatch>
                            <tem:TechEmail>apiuser_apj_tech01@uatmail.com</tem:TechEmail>
                            <tem:Branch>APJ Group</tem:Branch>
                            <tem:Customer>Tokyo Customer</tem:Customer>
                            <tem:Track>Tier 1</tem:Track>
                            <tem:ServiceTag>837CFEA</tem:ServiceTag>
                            <tem:PrimaryContactName>Round Rock Customer</tem:PrimaryContactName>
                            <tem:PrimaryContactPhone>9650619692</tem:PrimaryContactPhone>
                            <tem:PrimaryContactEmail>apiuser_apj_tech01@uatmail.com</tem:PrimaryContactEmail>
                            <tem:ShipToAddress>
                                <tem:AddressBookName/>
                                <tem:CountryISOCode>US</tem:CountryISOCode>
                                <tem:City>Atlanta</tem:City>
                                <tem:State>Ga</tem:State>
                                <tem:ZipPostalCode>30328</tem:ZipPostalCode>
                                <tem:AddressLine1>5871 Glenridge Drive</tem:AddressLine1>
                                <tem:AddressLine2>LorSPOC ABU</tem:AddressLine2>
                                <tem:AddressLine3/>
                                <tem:TimeZone>US/Central</tem:TimeZone>
                            </tem:ShipToAddress>
                            <tem:RequestCompleteCare>false</tem:RequestCompleteCare>
                            <tem:RequestReturnToDepot>false</tem:RequestReturnToDepot>
                            <tem:RequestOnSiteTechnician>false</tem:RequestOnSiteTechnician>
                            <tem:ReferencePONumber/>
                            <tem:TroubleshootingNote>Test do not dispatch - API Test
                            Requestxcdcsdfhsdgfhdsgfhgdfdhgfhsdfvdsfvdshfsdfdsfvdsvfsdfvdvfdsdvfdsvfdvfsdvfhvdhf
                            vdfvsdfvsdfvsdvfjdhfvdsjfjsdvfhdfvsjdhvhsvdhsvvjh</tem:TroubleshootingNote>
                            <tem:OverrideDPSType/>
                            <tem:Parts>
                                <tem:PartInfo>
                                    <tem:PartNumber>MBD</tem:PartNumber>
                                    <tem:PPID/>
                                    <tem:Quanitity>1</tem:Quanitity>
                                </tem:PartInfo>
                            </tem:Parts>
                            <tem:Attachments>
                                <tem:AttachmentInfo>
                                    <tem:Description>Sample Attachment</tem:Description>
                                    <tem:FileName>testimage.jpg</tem:FileName>
                                    <tem:MIMEType></tem:MIMEType>
                                    <tem:Data></tem:Data>
                                </tem:AttachmentInfo>
                            </tem:Attachments>
                        </tem:Dispatch>
                    </tem:CreateDispatchRequest>
                <tem:CreateDispatch/>
            </tem:CreateDispatch>
        </soapenv:Body>
    </soapenv:Envelope>

    Response:
    -Result Contains the result of the create request. Success indicates that a
        Work Order was successfully created, ValidationError indicates a
        problem with one or more business rules which is expanded in the notes
    -DispatchID Contains the globally unique identifier of the dispatch, only
        returned on a successful request
    -DispatchCode Contains the work order of the dispatch, only returned on a
        successful request
    -Status Contains the status of the work order, only returned on a
        successful request
    -Notes Contains informational messages relating to the dispatch, in the
        case of an unsuccessful request the notes contain the reason for the failure

    Sample Response:

    <?xml version="1.0" encoding="UTF-8" ?>
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        <soap:Body>
            <CreateDispatchResponse xmlns="http://api.dell.com">
                <CreateDispatchResult>
                    <DispatchCreateResult>
                        <DispatchID>ABCD0B64DA2465CA1337D960BD2BD69</DispatchID>
                        <DispatchCode>SR1111111111</DispatchCode>
                        <Status>Parts Review</Status>
                        <RA>false</RA>
                        <SanBao xsi:nil="true"/>
                        <Notes/>
                    </DispatchCreateResult>
                </CreateDispatchResult>
            </CreateDispatchResponse>
        </soap:Body>
    </soap:Envelope>

     */
    public static String createDispatch(ArrayList<DispatchMachine> machineList, String technicianEmail, String technicianPassword) throws SOAPException, IOException {

        //Check if the token has expired
        checkToken();

        //Namespace
        String namespace = "tem";

        //Create message
        SOAPMessage soapMessage = messageFactory.createMessage();

        //Add headers
        soapMessage.getMimeHeaders().addHeader("Authorization", "Bearer " + dispatchToken.getBearerToken());
        soapMessage.getMimeHeaders().addHeader("SOAPAction", "http://api.dell.com/IDispatchService/CreateDispatch");

        //Envelope
        SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
        soapEnvelope.addNamespaceDeclaration(namespace, "http://api.dell.com");


        //Body
        SOAPBody soapBody = soapEnvelope.getBody();

        //Add the createDispatch element to the body
        SOAPElement createDispatch = soapBody.addChildElement("CreateDispatch", namespace);

        //Add the createDispatchRequest element to the createDispatch element
        SOAPElement createDispatchRequest = createDispatch.addChildElement("CreateDispatchRequest", namespace);

        //Add the login and technicianPassword elements to the createDispatchRequest element
        SOAPElement login = createDispatchRequest.addChildElement("Login", namespace);
        login.addTextNode(technicianEmail);

        SOAPElement passwordNode = createDispatchRequest.addChildElement("Password", namespace);
        passwordNode.addTextNode(technicianPassword);

        //Add dispatch element to createDispatchRequest
        SOAPElement dispatch = createDispatchRequest.addChildElement("Dispatch", namespace);

        //Dispatch request elements
        //Add tech technicianEmail to dispatch
        SOAPElement techEmail = dispatch.addChildElement("TechEmail", namespace);
        techEmail.addTextNode(technicianEmail);

        //Add Branch (Group?? United States Depot)
        SOAPElement branch = dispatch.addChildElement("Branch", namespace);
        branch.addTextNode("United States Depot");

        //Add customer ("Cummins inc.")
        SOAPElement customer = dispatch.addChildElement("Customer", namespace);
        customer.addTextNode("Cummins inc.");

        //Add track ("(Tier 1)")
        SOAPElement track = dispatch.addChildElement("Track", namespace);
        track.addTextNode("Tier 1");

        //Add service tag
        SOAPElement serviceTag = dispatch.addChildElement("ServiceTag", namespace);
        serviceTag.addTextNode(machineList.get(0).getServiceTag());

        //Primary contact name
        SOAPElement primaryContactName = dispatch.addChildElement("PrimaryContactName", namespace);
        primaryContactName.addTextNode("Joseph Smith");

        //Primary contact phone
        SOAPElement primaryContactPhone = dispatch.addChildElement("PrimaryContactPhone", namespace);
        primaryContactPhone.addTextNode("8129003452");

        //Primary contact technicianEmail
        SOAPElement primaryContactEmail = dispatch.addChildElement("PrimaryContactEmail", namespace);
        primaryContactEmail.addTextNode("rv355@cummins.com");

        //Ship to address
        SOAPElement shipToAddress = dispatch.addChildElement("ShipToAddress", namespace);

        //Add shipping information to shipToAddress
        //Add country
        SOAPElement countryISOCode = shipToAddress.addChildElement("CountryISOCode", namespace);
        countryISOCode.addTextNode("US");

        //Add city
        SOAPElement city = shipToAddress.addChildElement("City", namespace);
        city.addTextNode("Columbus");

        //Add state
        SOAPElement state = shipToAddress.addChildElement("State", namespace);
        state.addTextNode("In");

        //Add zip code
        SOAPElement zipPostalCode = shipToAddress.addChildElement("ZipPostalCode", namespace);
        zipPostalCode.addTextNode("47201");

        //Add address line 1
        SOAPElement addressLine1 = shipToAddress.addChildElement("AddressLine1", namespace);
        addressLine1.addTextNode("500 Jackson Street");

        //Add address line 2 ("Mail Code: 60001")
        SOAPElement addressLine2 = shipToAddress.addChildElement("AddressLine2", namespace);
        addressLine2.addTextNode("Mail Code: 60001");

        //Add address line 3
        //SOAPElement addressLine3 = shipToAddress.addChildElement("AddressLine3", namespace);
        //addressLine3.addTextNode("");

        //Add timezone (Get timezone programatically)
        SOAPElement timezone = shipToAddress.addChildElement("TimeZone", namespace);
        timezone.addTextNode("US/Eastern");
        //End shipping information

        SOAPElement requestCompleteCare = dispatch.addChildElement("RequestCompleteCare", namespace);
        requestCompleteCare.addTextNode("false");

        SOAPElement requestReturnToDepot = dispatch.addChildElement("RequestReturnToDepot", namespace);
        requestReturnToDepot.addTextNode("false");

        SOAPElement requestOnSiteTechnician = dispatch.addChildElement("RequestOnSiteTechnician", namespace);
        requestOnSiteTechnician.addTextNode("false");

        /*
        Handle multiple parts
         */
        //Troubleshooting steps
        SOAPElement troubleshootingSteps = dispatch.addChildElement("TroubleshootingNote", namespace);

        //Build string for troubleshooting notes
        StringBuilder builder = new StringBuilder();


        SOAPElement parts = dispatch.addChildElement("Parts", namespace);

        /*
        Start multiple parts loop here
         */

        for (DispatchMachine machine : machineList) {

            builder.append(machine.getMachineIssue());
            builder.append(" : ");
            builder.append(machine.getTroubleshootingSteps());
            builder.append("\n");

            //Part Info (Add to partsList)
            SOAPElement partInfo = parts.addChildElement("PartInfo", namespace);

        /*
        Doc: Contains a valid DOSD Commodity part
         */
            SOAPElement partNumber = partInfo.addChildElement("PartNumber", namespace);
            partNumber.addTextNode(machine.getPartCode());

        /*
        Doc: Contains a PPID associated with the part being
             replaced. This is required for Monitors,
             Batteries and Port Replicators and optional for
             other parts
         */
            SOAPElement ppid = partInfo.addChildElement("PPID", namespace);
            //ppid.addTextNode(machine.getSerialNumber());
            ppid.addTextNode(machine.getSerialNumber());

            if (machine.getSerialNumber().equals("")){
                //Quantity (add to part info)
                SOAPElement quantity = partInfo.addChildElement("Quantity", namespace);
                quantity.addTextNode("1");
            } else {
                //Quantity (add to part info)
                SOAPElement quantity = partInfo.addChildElement("Quanitity", namespace);
                quantity.addTextNode("1");
            }

        }

        troubleshootingSteps.addTextNode(builder.toString());
        /*
        End multiple parts loop here
         */
        //End createDispatch

        //Save the message
        soapMessage.saveChanges();

        //Get response
        SOAPMessage response = getResponse(soapMessage, dispatchEndpoint);

        //Log request and response
        logSOAP(soapMessage, response, "Create Dispatch");

        //Return the dispatch code
        return getAttributeValue("DispatchCode", response);

    }

    /*
    Doc:

    Request:
    *Login Contains the technician user ID for the inquiry
    *Password Contains the password of the technician performing the inquiry
    *DispatchCode Contains the Work Order number of the inquiry

    Sample Request:

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:api="http://api.dell.com">
        <soapenv:Header/>
            <soapenv:Body>
                <api:GetDispatchStatus>
                    <api:GetDispatchStatusRequest>
                        <api:Login>apiuser_apj_tech01@uatmail.com</api:Login>
                        <api:Password>TechDirectAPI#1</api:Password>
                        <api:DispatchCode>SR999830477</api:DispatchCode>
                    </api:GetDispatchStatusRequest>
                </api:GetDispatchStatus>
            </soapenv:Body>
        </soapenv:Envelope>

    Response:
    -Result Contains the result of the inquiry request.
    -Status Contains the status of the work order
    -DPSNumber Contains the DPS number associated with the work order
    -DispatchCode Contains the Work Order number of the request
    -OrderDeniedReason If the Work Order was denied this will contain the denial reason
        assigned by the processor
    -Waybill Contains the waybill number of the dispatch once it is approved

    Sample Response:

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        <soap:Body>
            <GetDispatchStatusResponse xmlns="http://api.dell.com">
                <GetDispatchStatusResult>
                    <DispatchStatusResult>
                        <Result>Success</Result>
                        <Status>Parts Review</Status>
                        <DPSNumber/>
                        <DispatchCode>SR999830477</DispatchCode>
                        <OrderDeniedReason/>
                        <Waybill/>
                    </DispatchStatusResult>
                </GetDispatchStatusResult>
            </GetDispatchStatusResponse>
        </soap:Body>
    </soap:Envelope>
     */
    public static void getDispatchStatus(String technicianEmail, String technicianPassword, String dispatchCode) throws IOException, SOAPException {

        //Check if the token has expired
        checkToken();

        //Namespace
        String namespace = "api";

        //Create message factory
        MessageFactory messageFactory = jakarta.xml.soap.MessageFactory.newInstance();

        //Create message
        SOAPMessage soapMessage = messageFactory.createMessage();

        //Add headers
        soapMessage.getMimeHeaders().addHeader("Authorization", "Bearer " + dispatchToken.getBearerToken());
        soapMessage.getMimeHeaders().addHeader("SOAPAction", "http://api.dell.com/IDispatchService/GetDispatchStatus");

        //Get soap part
        SOAPPart soapPart = soapMessage.getSOAPPart();

        //Envelope
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        soapEnvelope.addNamespaceDeclaration(namespace, "http://api.dell.com");

        //Body
        SOAPBody soapBody = soapEnvelope.getBody();

        //Add the getDispatchStatus element to the body
        SOAPElement getDispatchStatus = soapBody.addChildElement("GetDispatchStatus", namespace);

        //Add the getDispatchStatusRequest element to the checkLogin element
        SOAPElement getDispatchStatusRequest = getDispatchStatus.addChildElement("GetDispatchStatusRequest", namespace);

        //Add the login and password elements to the getDispatchStatusRequest element
        SOAPElement login = getDispatchStatusRequest.addChildElement("Login", namespace);
        //technicianEmail
        login.addTextNode("apiuser_us_tech01@uatmail.com");

        SOAPElement password = getDispatchStatusRequest.addChildElement("Password", namespace);
        //technicianPassword
        password.addTextNode("TechDirectAPI#1");

        //Add the dispatch code element (Request Number) to the getDispatchStatusRequest element
        SOAPElement dispatchCodeElement = getDispatchStatusRequest.addChildElement("DispatchCode", namespace);
        //dispatchCode
        dispatchCodeElement.addTextNode("SR999830477");

        //Save the message
        soapMessage.saveChanges();

        SOAPMessage response = getResponse(soapMessage, dispatchEndpoint);
        response.writeTo(System.out);
    }

    /*
    Doc:

    Request:
    *Login Contains the technician user ID for the inquiry
    *Password Contains password of the technician performing the inquiry
    *CreatedFromDate Contains a date constraint to limit the result set. Work
        Orders will only be returned if they were created after this
        date. The format is YYYY-MM-DDThh:mm:ss-zzzzz as
        defined by the following:
             YYYY is a four-digit year MM is a two-digit numeral that represents the month
             DD is a two-digit numeral that represents the day
             T is a separator that indicating time of day follows
             hh is a two-digit numeral that represents the hour in 24-hour format
             mm is a two-digit numeral that represents the minute
             ss is a two-digit numeral that represents the second
             - is a separator indicating the time zone offset follows
             zzzzz represents the time zone as a GMT offset
                a valid sample timestamp is 2011-07-01T00:00:00-05:00
    -InStatuses Contains a list of statuses used to limit the results returned
    *Scope Available for future expansion, in current version default “All”

    Sample Request:

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:api="http://api.dell.com">
        <soapenv:Header/>
            <soapenv:Body>
                <api:BulkDispatchesInquiry>
                    <api:BulkDispatchesInquiryRequest>
                        <api:Login>apiuser_apj_tech01@uatmail.com</api:Login>
                        <api:Password>TechDirectAPI#1</api:Password>
                        <api:CreatedFromDate>2019-05-07</api:CreatedFromDate>
                        <api:InStatuses>
                        </api:InStatuses>
                        <api:Scope>All</api:Scope>
                        <api:AdditinalFields>
                            <!--Zero or more repetitions:-->
                            <api:string></api:string>
                        </api:AdditinalFields>
                    </api:BulkDispatchesInquiryRequest>
                </api:BulkDispatchesInquiry>
            </soapenv:Body>
        </soapenv:Envelope>

    Response:
    -Code Contains the DOSD Work Order number
    -DellDispatchNumber Contains the Dell DPS number associated with the work order
    -Status Contains the status of the

    Sample Response:

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        <soap:Body>
            <BulkDispatchesInquiryResponse xmlns="http://api.dell.com">
                <BulkDispatchesInquiryResult>
                    <DispatchInquiryResult>
                        <DispatchInquiryResult>
                            <Code>WO020220</Code>
                            <DellDispatchNumber/>
                            <Status>SBD4</Status>
                        </DispatchInquiryResult>
                        <DispatchInquiryResult>
                            <Code>WO020221</Code>
                            <DellDispatchNumber/>
                            <Status>Pending Request</Status>
                        </DispatchInquiryResult>
                        <DispatchInquiryResult>
                            <Code>SR999830465</Code>
                            <DellDispatchNumber/>
                            <Status>Parts Review</Status>
                        </DispatchInquiryResult>
                    </DispatchInquiryResult>
                </BulkDispatchesInquiryResult>
            </BulkDispatchesInquiryResponse>
        </soap:Body>
    </soap:Envelope>
     */
    public static void bulkDispatchesInquiry(String createdFromDate) throws IOException, SOAPException {

        //Check if the token has expired
        checkToken();

        //Namespace
        String namespace = "api";

        //Create message factory
        MessageFactory messageFactory = jakarta.xml.soap.MessageFactory.newInstance();

        //Create message
        SOAPMessage soapMessage = messageFactory.createMessage();

        //Add headers
        soapMessage.getMimeHeaders().addHeader("Authorization", "Bearer " + dispatchToken.getBearerToken());
        soapMessage.getMimeHeaders().addHeader("SOAPAction", "http://api.dell.com/IDispatchService/BulkDispatchesInquiry");

        //Get soap part
        SOAPPart soapPart = soapMessage.getSOAPPart();

        //Envelope
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        soapEnvelope.addNamespaceDeclaration(namespace, "http://api.dell.com");

        //Body
        SOAPBody soapBody = soapEnvelope.getBody();

        //Add the bulkDispatchesInquiry element to the body
        SOAPElement bulkDispatchesInquiry = soapBody.addChildElement("BulkDispatchesInquiry", namespace);

        //Add the bulkDispatchesInquiryRequest element to the bulkDispatchesInquiry element
        SOAPElement bulkDispatchesInquiryRequest = bulkDispatchesInquiry.addChildElement("BulkDispatchesInquiryRequest", namespace);

        //Add the login and password elements to the bulkDispatchesInquiryRequest element
        SOAPElement login = bulkDispatchesInquiryRequest.addChildElement("Login", namespace);
        login.addTextNode("rv355@cummins.com");

        SOAPElement password = bulkDispatchesInquiryRequest.addChildElement("Password", namespace);
        password.addTextNode("Kayla0626!$");

        //Created from date element added to bulkDispatchesInquiryRequest
        SOAPElement createdFromDateNode = bulkDispatchesInquiryRequest.addChildElement("CreatedFromDate", namespace);
        createdFromDateNode.addTextNode(createdFromDate);

        /*
        Doc: Contains a list of statuses used to limit the results returned
         */
        SOAPElement inStatuses = bulkDispatchesInquiryRequest.addChildElement("InStatuses", namespace);
        //SOAPElement status = inStatuses.addChildElement("Status", namespace);
        inStatuses.addTextNode("Pending Request");

        //Add the scope element to the bulkDispatchesInquiryRequest
        SOAPElement scope = bulkDispatchesInquiryRequest.addChildElement("Scope", namespace);
        scope.addTextNode("All");

        //Save the message
        soapMessage.saveChanges();

        SOAPMessage response = getResponse(soapMessage, dispatchEndpoint);

        NodeList list = response.getSOAPBody().getChildNodes();

        logSOAP(soapMessage, response, "Bulk Dispatch Inquiry");

    }

    /*
    Doc:

    Request:
    *Login Contains the technician user ID for the inquiry
    *Password Contains the password of the technician performing the inquiry
    *DispatchCode Contains the dispatch code that was denied
    *TroubleshootingNote Contains troubleshooting notes, limited to 1000 characters
    *Parts A collection of part information associated with the dispatch. This is limited to a
        maximum of 4 parts per dispatch
    -PartNumber Contains a valid DOSD Commodity part PPID Contains a PPID associated with the part being
        replaced. This is required for Monitors, Batteries and Port Replicators and optional for other parts
    *Quantity Contains the quantity of parts requested

    Sample Request:

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:api="http://api.dell.com">
        <soapenv:Header/>
            <soapenv:Body>
                <api:ResubmitDispatch>
                    <api:ResubmitDispatchRequest>
                        <api:Login>apiuser_us_tech02@uatmail.com</api:Login>
                        <api:Password>TechDirectAPI#1</api:Password>
                        <api:Dispatch>
                            <api:DispatchCode>SR999903711</api:DispatchCode>
                            <api:TroubleshootingNote>Testing
                            ResubmitDispatch</api:TroubleshootingNote>
                            <api:Parts>
                                <!--Zero or more repetitions:-->
                                <api:PartInfo>
                                    <api:PartNumber>KBD</api:PartNumber>
                                    <api:PPID></api:PPID>
                                    <api:Quanitity>1</api:Quanitity>
                                </api:PartInfo>
                            </api:Parts>
                        </api:Dispatch>
                    </api:ResubmitDispatchRequest>
                </api:ResubmitDispatch>
            </soapenv:Body>
        </soapenv:Envelope>

    Response:
    -DispatchCode Contains the work order of the dispatch, only returned on a successful request
    -Status Contains the status of the work order, only returned on a successful request
    -Notes Contains informational messages relating to the dispatch, in the
        case of an unsuccessful request the notes contain the reason for the failure

    Sample Response:
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        <soap:Body>
            <ResubmitDispatchResponse xmlns="http://api.dell.com">
                <ResubmitDispatchResult>
                    <DispatchCreateResult>
                        <DispatchCode>SR999903711</DispatchCode>
                        <Status>Parts Review</Status>
                        <RA>false</RA>
                        <SanBao xsi:nil="true"/>
                        <Notes/>
                    </DispatchCreateResult>
                </ResubmitDispatchResult>
            </ResubmitDispatchResponse>
        </soap:Body>
    </soap:Envelope>
     */
    public static void ResubmitDispatch() throws IOException, SOAPException  {

        //Check if the token has expired
        checkToken();

        //Namespace
        String namespace = "api";

        //Create message factory
        MessageFactory messageFactory = jakarta.xml.soap.MessageFactory.newInstance();

        //Create message
        SOAPMessage soapMessage = messageFactory.createMessage();

        //Add headers
        soapMessage.getMimeHeaders().addHeader("Authorization", "Bearer " + dispatchToken.getBearerToken());
        soapMessage.getMimeHeaders().addHeader("SOAPAction", "http://api.dell.com/IDispatchService/ResubmitDispatch");

        //Get soap part
        SOAPPart soapPart = soapMessage.getSOAPPart();

        //Envelope
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        soapEnvelope.addNamespaceDeclaration(namespace, "http://api.dell.com");

        //Body
        SOAPBody soapBody = soapEnvelope.getBody();

        //Add the resubmitDispatch element to the body
        SOAPElement resubmitDispatch = soapBody.addChildElement("ResubmitDispatch", namespace);

        //Add the resubmitDispatchRequest element to the resubmitDispatch element
        SOAPElement resubmitDispatchRequest = resubmitDispatch.addChildElement("ResubmitDispatchRequest", namespace);

        //Add the login and password elements to the resubmitDispatchRequest element
        SOAPElement login = resubmitDispatchRequest.addChildElement("Login", namespace);
        login.addTextNode("apiuser_us_tech01@uatmail.com");

        SOAPElement password = resubmitDispatchRequest.addChildElement("Password", namespace);
        password.addTextNode("TechDirectAPI#1");

        //Dispatch element added to resubmitDispatchRequest
        SOAPElement dispatch = resubmitDispatchRequest.addChildElement("Dispatch", namespace);

        //DispatchCode element added to dispatch
        SOAPElement dispatchCode = dispatch.addChildElement("DispatchCode", namespace);
        dispatchCode.addTextNode("SR999903711");

        //TroubleshootingNote element added to dispatch
        SOAPElement troubleshootingNote = dispatch.addChildElement("TroubleshootingNote", namespace);
        troubleshootingNote.addTextNode("Testing ResubmitDispatch");

        //Parts
        SOAPElement parts = dispatch.addChildElement("Parts", namespace);

        //Part Info (Add to partsList)
        SOAPElement partInfo = parts.addChildElement("PartInfo", namespace);

        //Part Number
        SOAPElement partNumber = partInfo.addChildElement("PartNumber", namespace);
        partNumber.addTextNode("KBD");

        //SOAPElement ppid = partInfo.addChildElement("PPID", namespace);
        //ppid.addTextNode("");

        //Quantity (add to part info)
        SOAPElement quantity = partInfo.addChildElement("Quantity", namespace);
        quantity.addTextNode("1");

        //Save the message
        soapMessage.saveChanges();

        SOAPMessage response = getResponse(soapMessage, dispatchEndpoint);
        response.writeTo(System.out);
    }

    /*
    Doc:

    Request:
    *Login Contains the technician user ID for the inquiry
    *Password Contains the password of the technician performing the inquiry
    *ServiceTag Contains the service tag for which parts information required

    Sample Request:

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:api="http://api.dell.com">
        <soapenv:Header/>
            <soapenv:Body>
                <api:GetPartsbyServiceTag>
                    <api:GetPartsbyServiceTag>
                        <api:Login>apiuser_apj_tech01@uatmail.com</api:Login>
                        <api:Password>TechDirectAPI#1</api:Password>
                        <api:ServiceTag>FGVBN01</api:ServiceTag>
                    </api:GetPartsbyServiceTag>
                </api:GetPartsbyServiceTag>
            </soapenv:Body>
        </soapenv:Envelope>

    Response:
    -Model Contains Model of the asset
    -ModelDescription Contains Model description
    -PartTypeCode Contains a valid DOSD commodity part type
    -PartNumber Contains a valid DOSD Commodity part. Use this value for “PartNumber” in CreateDispatch.
    -PartDescription Contains Part description

    Sample Response:
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        <soap:Body>
            <GetPartsbyServiceTagResponse xmlns="http://api.dell.com">
                <GetPartsbyServiceTagResult>
                    <PartsbyTagResult>
                        <Model>;E066</Model>
                        <ModelDescription>DELL 27 MONITOR S2715H</ModelDescription>
                        <Parts>
                            <PartInformation>
                                <PartTypeCode>Hard Drives, Accessories</PartTypeCode>
                                <PartNumber>HRD</PartNumber>
                                <PartDescription>Storage, Hard Drive</PartDescription>
                                <Serializable>false</Serializable>
                            </PartInformation>
                        </Parts>
                        <ServiceTag>837CFEA</ServiceTag>
                    </PartsbyTagResult>
                </GetPartsbyServiceTagResult>
            </GetPartsbyServiceTagResponse>
        </soap:Body>
    </soap:Envelope>
     */
    public static String getPartsByServiceTag(String serviceTag) throws SOAPException, IOException {

        //Check if the token has expired
        checkToken();

        //Namespace
        String namespace = "api";

        //Create message factory
        MessageFactory messageFactory = jakarta.xml.soap.MessageFactory.newInstance();

        //Create message
        SOAPMessage soapMessage = messageFactory.createMessage();

        //Add headers
        soapMessage.getMimeHeaders().addHeader("Authorization", "Bearer " + dispatchToken.getBearerToken());
        soapMessage.getMimeHeaders().addHeader("SOAPAction", "http://api.dell.com/IDispatchService/GetPartsbyServiceTag");

        //Get soap part
        SOAPPart soapPart = soapMessage.getSOAPPart();

        //Envelope
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        SOAPHeader header = soapEnvelope.getHeader();
        soapEnvelope.addNamespaceDeclaration(namespace, "http://api.dell.com");


        //Body
        SOAPBody soapBody = soapEnvelope.getBody();

        //Add the getPartsByServiceTag element to the body
        SOAPElement getPartsByServiceTag = soapBody.addChildElement("GetPartsbyServiceTag", namespace);

        //Add the getPartsByServiceTagNested element to the getPartsByServiceTag element
        SOAPElement getPartsByServiceTagNested = getPartsByServiceTag.addChildElement("GetPartsbyServiceTag", namespace);

        //Add the login and password elements to the getPartsByServiceTagNested element
        SOAPElement login = getPartsByServiceTagNested.addChildElement("Login", namespace);
        login.addTextNode("rv355@cummins.com");
        SOAPElement password = getPartsByServiceTagNested.addChildElement("Password", namespace);
        password.addTextNode("Kayla0626!$");

        //Add Service tag to the getPartsByServiceTagNested element
        SOAPElement serviceTagNode = getPartsByServiceTagNested.addChildElement("ServiceTag", namespace);
        serviceTagNode.addTextNode(serviceTag);

        //Save the message
        soapMessage.saveChanges();

        //Get response
        SOAPMessage response = getResponse(soapMessage, dispatchEndpoint);


        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        response.writeTo(stream);

        logSOAP(soapMessage, response, "Get Parts By Service Tag");

        //Return response as a string
        return stream.toString();
    }

    /*
    Doc:

    Request:
    *Login Contains the technician user ID for the inquiry
    *Password Contains the password of the technician performing the inquiry
    *ModelCode Contains the type of device. E.g. Desktops, Servers.

    Sample Request:

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:api="http://api.dell.com">
        <soapenv:Header/>
            <soapenv:Body>
                <api:GetPartsbyModel>
                    <api:GetPartsbyModelRequest>
                        <api:Login>apiuser_apj_tech01@uatmail.com</api:Login>
                        <api:Password>TechDirectAPI#1</api:Password>
                        <api:ModelCode>Desktops</api:ModelCode>
                    </api:GetPartsbyModelRequest>
                </api:GetPartsbyModel>
            </soapenv:Body>
        </soapenv:Envelope>

    Response:
    -Model Contains Model of the asset
    -ModelDescription Contains Model description
    -PartTypeCode Contains a valid DOSD commodity part type
    -PartNumber Contains a valid DOSD Commodity part. Use this value for “PartNumber” in CreateDispatch request.
    -PartDescription Contains Part description

    Sample Response:

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        <soap:Body>
            <GetPartsbyModelResponse xmlns="http://api.dell.com">
                <GetPartsbyModelResult>
                    <PartsbyModelResult>
                        <Model>Desktops</Model>
                        <ModelDescription>Desktops</ModelDescription>
                            <Parts>
                                <PartInformation>
                                    <PartTypeCode>Hard Drives, Accessories</PartTypeCode>
                                    <PartNumber>HRD</PartNumber>
                                    <PartDescription>Storage, Hard Drive</PartDescription>
                                    <Serializable>false</Serializable>
                                </PartInformation>
                            </Parts>
                    </PartsbyModelResult>
                </GetPartsbyModelResult>
            </GetPartsbyModelResponse>
        </soap:Body>
    </soap:Envelope>
     */
    public static void getPartsByModel() throws IOException, SOAPException {

        //Check if the token has expired
        checkToken();

        //Namespace
        String namespace = "api";

        //Create message factory
        MessageFactory messageFactory = jakarta.xml.soap.MessageFactory.newInstance();

        //Create message
        SOAPMessage soapMessage = messageFactory.createMessage();

        //Add headers
        soapMessage.getMimeHeaders().addHeader("Authorization", "Bearer " + dispatchToken.getBearerToken());
        soapMessage.getMimeHeaders().addHeader("SOAPAction", "http://api.dell.com/IDispatchService/GetPartsbyModel");

        //Get soap part
        SOAPPart soapPart = soapMessage.getSOAPPart();

        //Envelope
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        SOAPHeader header = soapEnvelope.getHeader();
        soapEnvelope.addNamespaceDeclaration(namespace, "http://api.dell.com");


        //Body
        SOAPBody soapBody = soapEnvelope.getBody();

        //Add the getPartsByModel element to the body
        SOAPElement getPartsByModel = soapBody.addChildElement("GetPartsbyModel", namespace);

        //Add the getPartsByModelRequest element to the getPartsByModel element
        SOAPElement getPartsByModelRequest = getPartsByModel.addChildElement("GetPartsbyModelRequest", namespace);

        //Add the login and password elements to the getPartsByModelRequest element
        SOAPElement login = getPartsByModelRequest.addChildElement("Login", namespace);
        login.addTextNode("apiuser_us_tech01@uatmail.com");
        SOAPElement password = getPartsByModelRequest.addChildElement("Password", namespace);
        password.addTextNode("TechDirectAPI#1");

        //Add Model Code to the getPartsByModelRequest element
        SOAPElement modelCode = getPartsByModelRequest.addChildElement("ModelCode", namespace);
        modelCode.addTextNode("Desktops");

        //Save the message
        soapMessage.saveChanges();

        //Get response
        SOAPMessage response = getResponse(soapMessage, dispatchEndpoint);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        response.writeTo(stream);
        String message = Arrays.toString(stream.toByteArray()).replace("/>", "\n");
    }

        /*
    Dispatch Token Methods
     */
    /**
     * Check if the token has expired and generate new token if so
     * @throws IOException
     */
    private static void checkToken() throws IOException {

        //Get the token from the database
        dispatchToken = Database.getToken(dispatchAPIName);

        //Check if the token is expired
        if (dispatchToken.getTokenExpiration().isBefore(LocalDateTime.now())){

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
                    .setUri(tokenEndpoint)
                    //Create the request body
                    .setEntity(new StringEntity("grant_type=client_credentials&" +
                            "client_id=" + properties.get("dispatchClientID") +
                            "&client_secret=" + properties.get("dispatchClientSecret")))
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
                    dispatchToken.setBearerToken(line.substring(18, 54));
                    //Update the expiration time
                    dispatchToken.setTokenExpiration(LocalDateTime.now().plusSeconds(3600));
                    //Update the token in the database
                    Database.updateToken(dispatchToken, dispatchAPIName);
                }
            });
        }
    }

    /*Get Model From Service Tag*/
    public static String getMachineModel(String serviceTag) throws IOException, SOAPException {
        String response = getPartsByServiceTag(serviceTag);

        //regex to retrieve dispatch
        String openTagPattern = "<ModelDescription>";
        String closeTagPattern = "</ModelDescription>";

        Pattern pattern = Pattern.compile(Pattern.quote(openTagPattern) + "(.*?)" + Pattern.quote(closeTagPattern));
        Matcher matcher = pattern.matcher(response);

        String modelDescription = null;
        while (matcher.find()){
            modelDescription = matcher.group(1);
            if (modelDescription.toLowerCase().contains("latitude 7490")){
                modelDescription = modelDescription.charAt(0) + modelDescription.substring(1, 13).toLowerCase();
            } else if (modelDescription.toLowerCase().contains("precision 5530") || modelDescription.toLowerCase().contains("precision 7730")){
                modelDescription = modelDescription.charAt(0) + modelDescription.substring(1,14).toLowerCase();
            } else if (modelDescription.toLowerCase().contains("latitude 5290 2-in-1")){
                modelDescription = modelDescription.charAt(0) + modelDescription.substring(1,20).toLowerCase();
            }
        }
        return modelDescription;
    }
}
