package de.imi.odmtoolbox.library;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

/**
 * This class implements {@link String} utilities, which are are not part of the
 * {@link String} implementataion in Java, but useful to simplify the source
 * code
 * 
 */
@Service
public class StringUtilities {

    Map<String, String> germanUmlautsReplacement = new HashMap<>();

    @PostConstruct
    public void init() {
        // Fill the map of characters to exchange for replaceGermanUmlauts(String germanText)
        germanUmlautsReplacement.put("Ä", "Ae");
        germanUmlautsReplacement.put("ä", "ae");
        germanUmlautsReplacement.put("Ö", "Oe");
        germanUmlautsReplacement.put("ö", "oe");
        germanUmlautsReplacement.put("Ü", "Ue");
        germanUmlautsReplacement.put("ü", "ue");
        germanUmlautsReplacement.put("ß", "ss");
    }

    /**
     * This method exchanges all german umlauts in a given german text to an
     * appropriate english spelling.
     *
     * @param germanText The german text, where the umlauts are changed to the
     * english spelling.
     * @return The german text without any german umlauts
     */
    public String replaceGermanUmlauts(String germanText) {
        for (String character : germanUmlautsReplacement.keySet()) {
            germanText = germanText.replaceAll(character, germanUmlautsReplacement.get(character));
        }
        return germanText;
    }
}
