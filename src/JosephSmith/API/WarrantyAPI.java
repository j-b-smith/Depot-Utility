package JosephSmith.API;

import JosephSmith.Database.Database;
import JosephSmith.model.Token;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Properties;

public class WarrantyAPI extends DellAPI {

    //Warranty Variables
    private static Token warrantyToken;
    private static final String warrantyAPIName = "Warranty API";

        /*
    Warranty API Methods
     */
    /**
     * Check if the token has expired and generate new token if so
     */
    private static void checkWarrantyToken()  {

        //Get the token from the database
        warrantyToken = Database.getToken(warrantyAPIName);

        //Check if the token is expired
        if (warrantyToken.getTokenExpiration().isBefore(LocalDateTime.now())){

            //Get new token
            try {
                getWarrantyToken();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the token from Http request
     * @throws IOException
     */
    public static void getWarrantyToken() throws IOException{

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
                            "client_id=" + properties.get("warrantyClientID") +
                            "&client_secret=" + properties.get("warrantyClientSecret")))
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
                    warrantyToken.setBearerToken(line.substring(18, 54));
                    //Update the expiration time
                    warrantyToken.setTokenExpiration(LocalDateTime.now().plusSeconds(3600));
                    //Update the token in the database
                    Database.updateToken(warrantyToken, warrantyAPIName);
                }
            });
        }

    }

/*    public static HashMap<String, String> getAssetInfo(ArrayList<String> serviceTags, String requestType) {
        StringBuilder builder = new StringBuilder();
        HashMap<String, String> infoMap = new HashMap<>();

        //Store url to use
        String url = null;

        switch (requestType) {
            case "Asset Information":
                url = "https://apigtwb2c.us.dell.com/PROD/sbil/eapi/v5/assets";
                break;
            case "Asset Entitlement":
                url = "https://apigtwb2c.us.dell.com/PROD/sbil/eapi/v5/asset-entitlements";
                break;
            case "Asset Details":
                url = "https://apigtwb2c.us.dell.com/PROD/sbil/eapi/v5/asset-components";
                break;
            case "Asset Summary":
                url = "https://apigtwb2c.us.dell.com/PROD/sbil/eapi/v5/asset-entitlement-components";
                break;
        }

        checkWarrantyToken();

        StringBuilder queryString = new StringBuilder();
        for (String serviceTag : serviceTags) {
            queryString.append(serviceTag);
            if (serviceTags.size() > 1) {
                queryString.append(",");
            }
        }

        //Create the http client
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            //Create the http request
            HttpUriRequest request = RequestBuilder.create("GET")
                    .setUri(url + "?servicetags=" + queryString)
                    //Add header
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + warrantyToken.getBearerToken())
                    .build();

            //Get the http response
            HttpResponse response = client.execute(request);

            //Get the content
            InputStream inputStream = response.getEntity().getContent();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            //Get the bearer token
            bufferedReader.lines().forEach(str -> {
                builder.append(str);
                System.out.println(str);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


        return infoMap;
    }*/
}
