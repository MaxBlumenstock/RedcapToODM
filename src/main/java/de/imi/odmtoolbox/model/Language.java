package de.imi.odmtoolbox.model;

/**
 * An enum and an parse method to represent Language (not Country)-Codes. 
 * Used by the translator classes.
 * @author Philipp Neuhaus <Philipp.Neuhaus@uni-muenster.de>
 */
public final class Language{
    public static enum language {
        de, en, nl, cs, da, et, fi, fr, el, ht, ca, bg, ar, zh, he, hi, hu, id, it, ja, ko, lv, lt, ms, no, fa, pl, pt, ro, ru, sk, sl, es, sv, th, tr, uk, ur, vi, UNDEFINED
    }
   
    public static language parseLanguage (String languageString){
        try {
            return language.valueOf(languageString.toLowerCase());
        } catch ( IllegalArgumentException e) {
            //OK, entspricht nicht direkt einem language-Enum. Also Weiter.
        }
        
        if (languageString.contains("-")) {
            try {
                return language.valueOf(languageString.split("-")[0].toLowerCase());
            } catch ( IllegalArgumentException e) {
             // Immer noch nicht?
            }
        }
        
        return language.UNDEFINED;
    }
    
    public static String getWrittenName (Language.language lang) {
        switch (lang) {
            case en:
                return "english";
  
            case de:
                return "german";
            case nl:
                return "dutch";
            case cs:
                return "czech";
            case da:
                return "danish";
            case et:
                return "estonian";
            case fi:
                return "finnish";
            case fr:
                return "french";
            case el:
                return "greek";
            case ht:
                return "haitian_creole";
            case ca:
                return "catalan";
            case bg:
                return "bulgarian";
            case ar:
                return "arabic";
            case zh:
                return "chinese_traditional";
            case he:
                return "hebrew";
            case hi:
                return "hindi";
            case hu:
                return "hungarian";
            case id:
                return "indonesian";
            case it:
                return "italian";
            case ja:
                return "japanese";
            case ko:
                return "korean";
            case lv:
                return "latvian";
            case lt:
                return "lithuanian";
            case ms:
                return "malay";
            case no:
                return "norwegian";
            case fa:
                return "persian";
            case pl:
                return "polish";
            case pt:
                return "portoguese";
            case ro:
                return "romanian";
            case ru:
                return "russian";
            case sk:
                return "slovak";
            case sl:
                return "slovenian";
            case es:
                return "spanish";
            case sv:
                return "swedish";
            case th:
                return "thai";
            case tr:
                return "turkish";
            case uk:
                return "ukrainian";
            case ur:
                return "urdu";
            case vi:
                return "vietnamese";                          
        }
        return "";
    }

}
