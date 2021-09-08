package de.imi.odmtoolbox.library;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.HashMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;


/**
 *
 * @author Philipp Neuhaus
 */
@Service
public class MultiMapperService {

    JSONObject lastResponseMapTable;

    
    public MultiMapperService() {

    }

    private JSONObject doRequest(String searchTerm) throws Exception{
        return this.doRequest(searchTerm, 0, "standard", true);
    }
    
    private JSONObject doRequest(String searchTerm, int threshold, String sorter, boolean useAdept) throws Exception{
        return this.doRequest(searchTerm, "", threshold, sorter, useAdept);
    }
    
    private JSONObject doRequest(String searchTerm, String additionalParameters, int threshold, String sorter, boolean useAdept) throws Exception{
        HttpClient client = HttpClientBuilder.create().build();
        
        switch (sorter) {
            
            case "standard":
            case "SimpleSorter":
            default:
                sorter = "SimpleSorter";
            break;
        }
        
        HttpGet request = new HttpGet("http://multimapper.uni-muenster.de/multimapper/map?searchTerm=" 
                + URLEncoder.encode(searchTerm, "UTF-8") 
                + "&useAdept=" + URLEncoder.encode(Boolean.toString(useAdept) , "UTF-8") 
                + "&sorter=" + URLEncoder.encode(sorter, "UTF-8") 
                + "&" + URLEncoder.encode(additionalParameters, "UTF-8"));
        
        HttpResponse response = client.execute(request); 
        BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));

        String responseString = "";
        String tmpString ="";
        while (( tmpString = rd.readLine()) != null){
            responseString += tmpString;
        }

        JSONObject jsonObject = new JSONObject(responseString);
        
        return jsonObject;
    }
    
    
    public String returnFirstHit(String searchTerm, int threshold, String sorter, boolean useAdept) throws Exception{
        JSONObject response = this.doRequest(searchTerm, threshold, sorter, useAdept);
        
        String originalSearchField = response.getString("originalSearchField");
        JSONObject mapTables = response.getJSONObject("mapTables");
        
        JSONObject codesOfOriginalSearchField = mapTables.getJSONObject(originalSearchField);
        mapTables.remove(originalSearchField);
        //Save additional Codes within the lastResponeMapTables (without the originalSearchField-codes)
        this.lastResponseMapTable = mapTables;
        
        JSONArray codes = codesOfOriginalSearchField.getJSONArray("mapEntrys");
        try{ 
            JSONObject firstHit = codes.getJSONObject(0);
            JSONObject probalities = firstHit.getJSONObject("probabilities");
            
            //Nur einen Wert zurück geben, wenn die Gleichheit über treshold liegt.
            Integer score = new Double (probalities.getDouble("max")*100).intValue();
            
            if ( score >= threshold) {
                return firstHit.getString("code");
            }
        } catch (JSONException e) {
            //Keine Antwort, also Mapping nicht gefunden!
            return null;
        }

        return null;
        
    }
    
    public HashMap<String, String> returnAdditionalHits() {
        HashMap<String, String> additionalHits = new HashMap<>();
        
        try {
            String[] fieldNames = JSONObject.getNames(this.lastResponseMapTable);

            for (String fieldName: fieldNames) {
                try{
                    JSONObject firstHit = this.lastResponseMapTable.getJSONObject(fieldName).getJSONArray("mapEntrys").getJSONObject(0);
                    additionalHits.put(fieldName, firstHit.getString("code"));
                } catch ( Exception e ) {
                    //Egal. Gibt sonst ein leeres HashMap zurück, auch nicht schlimm.
                }
            }
        } catch (NullPointerException e) {
            //Dann gab's keine weiteren codes
            return additionalHits;
        }
        
        return additionalHits;
    }
}
