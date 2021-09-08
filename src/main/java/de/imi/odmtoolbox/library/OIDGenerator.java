
package de.imi.odmtoolbox.library;

/*
 * Generiert OIDs für die Verwendung z.B. als FileOID
 * Siehe auch https://imiwiki.uni-muenster.de/dokuwiki/doku.php/organisation:oids
 *
 * @author Philipp Neuhaus <Philipp.Neuhaus@uni-muenster.de>
 */
public class OIDGenerator {
    private static OIDGenerator instance;
    
    public Long currentBase;

    private OIDGenerator(){
        Long currentTime = (System.currentTimeMillis() / 1000L);
        this.currentBase = currentTime;
    }
    
    public String getOID(){
        this.currentBase+= 1;
        return "1.3.6.1.4.1.13158.101.7." + this.currentBase.toString();
    }
    
    public static OIDGenerator getInstance(){
        if (OIDGenerator.instance == null) {
            OIDGenerator.instance = new OIDGenerator();
        }
        return OIDGenerator.instance;
    }
}
