package de.imi.odmtoolbox.library;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * This class connects to a webservice which can return additional information
 * for UMLS codes like descirption or preferred name.
 *
 */
@Service
public class UMLSCodeService {

    // The base URL of the webservice
    private final String baseURL = "http://umls.uni-muenster.de/index.php?CUI=";

    public UMLSCodeService() {

    }

    /**
     * Returns the preferred name for a given UMLS code.
     *
     * @param code The UMLS code for which the preferred name should be
     * searched.
     * @return The preferred name for the given UMLS Code
     */
    public String getPreferredName(String code) throws Exception {
        return this.doRequest(code).getString("STR");
    }

    private String getCurrentUMLSVersion() throws Exception {
        return this.doRequest("2014AB").getString("VERSION");
    }

    /**
     * Returns the detailed information for a given UMLS Code like description
     * and preferred name.
     *
     * @param CUI The UMLS code for which the additional information should be
     * searched.
     * @return The information for the given UMLS code.
     */
    private JSONObject doRequest(String CUI) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(baseURL + URLEncoder.encode(CUI.trim(), "UTF-8"));

        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        String responseString = "";
        String tmpString = "";
        while ((tmpString = rd.readLine()) != null) {
            responseString += tmpString;
        }

        JSONObject jsonObject = new JSONObject(responseString);

        return jsonObject;
    }

    /**
     * Returns the HTML representation for the additional information of a given
     * UMLS Code.
     *
     * @param UMLSCode The UMLS code for which the additional information should
     * be displayed as HTML.
     * @return The HTML representation of the additional information for the
     * given UMLS code.
     */
    public String getHTMLPresentation(String UMLSCode) {

        JSONObject umlsCodeInformation;
        String result = "";
        try {
            umlsCodeInformation = doRequest(UMLSCode);
            String preferredName = umlsCodeInformation.getString("STR").replaceAll("\\<[^>]*>", "");
            preferredName = preferredName.substring(1, preferredName.length() - 1);
            if (preferredName.isEmpty() == false) {
                preferredName = preferredName.substring(1, preferredName.length() - 1);
            }
            String description = umlsCodeInformation.getString("DEF").replaceAll("\\<[^>]*>", "");
            description = description.substring(1, description.length() - 1);
            if (description.isEmpty() == false) {
                description = description.substring(1, description.length() - 1);
            }
            if (description.isEmpty() == true && preferredName.isEmpty() == true) {
                result = "No description available for this UMLS code";
            } else if (description.isEmpty() == true) {
                result = preferredName;
            } else {
                result = preferredName + ": " + description;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }
}
