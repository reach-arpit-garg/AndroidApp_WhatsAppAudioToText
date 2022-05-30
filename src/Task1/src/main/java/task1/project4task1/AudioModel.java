//agarg2
//Arpit Garg
//agarg2@andrew.cmu.edu
package task1.project4task1;
/*
 * @author Arpit Garg
 *
 * This file is the Model component of the MVC, and it models the business
 * logic for the web servlet.  In this case, the business logic involves
 * using Java library functions to create logs and convert to text
 * returned in order to be converted into different forms.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import com.ibm.watson.developer_cloud.service.exception.NotFoundException;
import com.ibm.watson.developer_cloud.service.exception.RequestTooLargeException;
import com.ibm.watson.developer_cloud.service.exception.ServiceResponseException;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import com.mongodb.client.model.Projections;
import org.bson.Document;

public class AudioModel {

    protected String getText(byte[] byteAudio){
        /*
         * Calls ibm watson api to convert audio to text
         */

        String response = "";
        try {
            String urlString = "https://api.us-east.speech-to-text.watson.cloud.ibm.com/instances/9ba8cc99-ec60-49e3-a839-06a5305befec/v1/recognize";
            URL url = new URL(urlString);

            // user and password
            String user = "USERNAME";
            String password = "PASSWORD";

            // open the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // https://stackoverflow.com/questions/12732422/adding-header-for-httpurlconnection
            String auth = user + ":" + password;
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(auth.getBytes()));

            connection.setRequestProperty ("Authorization", basicAuth);
            connection.setRequestProperty("Content-Type", "audio/ogg");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.getOutputStream().write(byteAudio);

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String str;
            // Read each line of "in" until done, adding each to "response"
            while ((str = in.readLine()) != null) {
                // str is one line of text readLine() strips newline characters
                response += str;
            }
            in.close();
    } catch (NotFoundException e) {
            return "Not Found HTTP/404;";
        } catch (RequestTooLargeException e) {
            return "Request Too Large HTTP/413;";
        } catch (ServiceResponseException | IOException e) {
            // Base class for all exceptions caused by error responses from the service
            System.out.println("Service returned status code "
                    + e.getLocalizedMessage() + ": " + e.getMessage());
            return e.getMessage();
        }
        return "OK HTTP/200;" + response;
    }

    protected String addToDB(String values){
        MongoDatabase database = connect();

        // https://www.tutorialspoint.com/how-to-insert-a-document-into-a-mongodb-collection-using-java
        //Preparing a document
        Document document = new Document();

        document.append("responseTime", values.split(":")[0]);
        document.append("apiTime", values.split(":")[1]);
        document.append("text", values.split(":")[2]);
        document.append("phoneModel", values.split(":")[3]);
        document.append("phoneBrand", values.split(":")[4]);
        document.append("phoneZone", values.split(":")[5]);
        document.append("phoneDate", values.split(":")[6]);

        //Inserting the document into the collection
        database.getCollection("Metrics").insertOne(document);

        return "OK HTTP/200 Documents inserted successfully.";
    }

    private String getFromDB(){
        String result = "";

        MongoDatabase database = connect();

        //https://stackoverflow.com/questions/49351629/mongodb-java-3-4-get-fields-from-embedded-document
        //Creating a collection object
        MongoCollection<Document> collection = database.getCollection("Metrics");
        //Retrieving the documents
        MongoCursor<Document> iterDoc = collection.find().projection(Projections.fields(Projections.excludeId())).iterator();

        // create a response
        result = "{ \"message\":[\n";
        while (iterDoc.hasNext()) {
            result += iterDoc.next().toJson() + ",";

        }
        result = result.substring(0, result.length()-1);
        result += " ],\n";


        collection = database.getCollection("Logs");
        iterDoc = collection.find().projection(Projections.fields(Projections.excludeId())).iterator();

        result += "\"logs\":[\n";
        while (iterDoc.hasNext()) {
            result += iterDoc.next().toJson() + ",";

        }
        result = result.substring(0, result.length()-1);
        result += " ]\n}";

        System.out.println(result);

        return result;
    }

    protected String getAnalyticsData(){
        // get data
        String data = getFromDB();

        return data;
    }

    protected void addLogsToDB(String values){
        MongoDatabase database = connect();

        // https://www.tutorialspoint.com/how-to-insert-a-document-into-a-mongodb-collection-using-java

        //Preparing a document
        Document document = new Document();

        document.append("time", new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        document.append("log", values);

        //Inserting the document into the collection
        database.getCollection("Logs").insertOne(document);

    }

    private MongoDatabase connect(){
        ConnectionString connectionString = new ConnectionString("mongodb+srv://arpitgarg:Arpit123@petfinder.dxoyu.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("Akshar");

        return database;
    }

    boolean checkAudioFormat(String audioFileName){
        System.out.println("check audio format ");

        if(!audioFileName.contains("opus") )
            return false;

        return true;
    }
}
