//agarg2
//Arpit Garg
//agarg2@andrew.cmu.edu
package task1.project4task1;
/*
 * @author Arpit Garg
 *
 * This file gets a string request
 * to be converted to a JSON request
 */

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class AudioView {
    protected String toJSON(String result, long apiTime){
        // Convert it to JsonObject
        result = result.replaceAll("&quot;", "\"");
        JsonObject responseJson = new Gson().fromJson(result.split(";")[1].trim(), JsonObject.class);

        String text = responseJson.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray("alternatives").get(0).getAsJsonObject().get("transcript").toString();
        System.out.println("Text: " + text);
        result = "{ \"Text\" : " + text;
        result += ",\"ApiTime\" : \"" + apiTime + "\" }";

        return result;
    }
}
