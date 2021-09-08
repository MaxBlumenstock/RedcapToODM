/**
 *  This function converts a language code to the language and country name.
 *  For exsample  "en-GB" or "en" would be converted to "English (Great Britain)" or "English".
 * @param {type} langCode The language code that will be converted.
 * @return {String} The language name as String.
 */
function getLanguageName(langCode) {
    var langSplit = langCode.split("-");
    var langName;
    var countryName;
    switch (langSplit[0].toLowerCase()) {
        case "hi":
            langName = "Hindi";
            break;
        case "ps":
            langName = "Pushto";
            break;
        case "pt":
            langName = "Portuguese";
            break;
        case "ho":
            langName = "Hiri Motu";
            break;
        case "hr":
            langName = "Croatian";
            break;
        case "ht":
            langName = "Haitian";
            break;
        case "hu":
            langName = "Hungarian";
            break;
        case "yi":
            langName = "Yiddish";
            break;
        case "hy":
            langName = "Armenian";
            break;
        case "hz":
            langName = "Herero";
            break;
        case "yo":
            langName = "Yoruba";
            break;
        case "ia":
            langName = "Interlingua";
            break;
        case "id":
            langName = "Indonesian";
            break;
        case "ie":
            langName = "Interlingue ";
            break;
        case "ig":
            langName = "Igbo";
            break;
        case "aa":
            langName = "Afar";
            break;
        case "ii":
            langName = "Sichuan";
            break;
        case "ab":
            langName = "Abkhazian";
            break;
        case "ik":
            langName = "Inupiaq";
            break;
        case "ae":
            langName = "Avestan";
            break;
        case "qu":
            langName = "Quechua";
            break;
        case "af":
            langName = "Afrikaans";
            break;
        case "io":
            langName = "Ido";
            break;
        case "za":
            langName = "Zhuang";
            break;
        case "ak":
            langName = "Akan";
            break;
        case "is":
            langName = "Icelandic";
            break;
        case "it":
            langName = "Italian";
            break;
        case "am":
            langName = "Amharic";
            break;
        case "iu":
            langName = "Inuktitut";
            break;
        case "an":
            langName = "Aragonese";
            break;
        case "zh":
            langName = "Chinese";
            break;
        case "ar":
            langName = "Arabic";
            break;
        case "as":
            langName = "Assamese";
            break;
        case "av":
            langName = "Avaric";
            break;
        case "ja":
            langName = "Japanese";
            break;
        case "ay":
            langName = "Aymara";
            break;
        case "az":
            langName = "Azerbaijani";
            break;
        case "rm":
            langName = "Romansh";
            break;
        case "zu":
            langName = "Zulu";
            break;
        case "rn":
            langName = "Rundi";
            break;
        case "ro":
            langName = "Romanian";
            break;
        case "ba":
            langName = "Bashkir";
            break;
        case "be":
            langName = "Belarusian";
            break;
        case "ru":
            langName = "Russian";
            break;
        case "bg":
            langName = "Bulgarian";
            break;
        case "rw":
            langName = "Kinyarwanda";
            break;
        case "bh":
            langName = "Bihari languages";
            break;
        case "bi":
            langName = "Bislama";
            break;
        case "bm":
            langName = "Bambara";
            break;
        case "bn":
            langName = "Bengali";
            break;
        case "jv":
            langName = "Javanese";
            break;
        case "bo":
            langName = "Tibetan";
            break;
        case "sa":
            langName = "Sanskrit";
            break;
        case "br":
            langName = "Breton";
            break;
        case "sc":
            langName = "Sardinian";
            break;
        case "bs":
            langName = "Bosnian";
            break;
        case "sd":
            langName = "Sindhi";
            break;
        case "se":
            langName = "Northern Sami";
            break;
        case "sg":
            langName = "Sango";
            break;
        case "ka":
            langName = "Georgian";
            break;
        case "si":
            langName = "Sinhala";
            break;
        case "sk":
            langName = "Slovak";
            break;
        case "sl":
            langName = "Slovenian";
            break;
        case "dari":
            langName = "Dari";
            break;
        case "sm":
            langName = "Samoan";
            break;
        case "sn":
            langName = "Shona";
            break;
        case "kg":
            langName = "Kongo";
            break;
        case "so":
            langName = "Somali";
            break;
        case "ca":
            langName = "Catalan";
            break;
        case "ki":
            langName = "Kikuyu ";
            break;
        case "sq":
            langName = "Albanian";
            break;
        case "kj":
            langName = "Kuanyama";
            break;
        case "sr":
            langName = "Serbian";
            break;
        case "kk":
            langName = "Kazakh";
            break;
        case "ss":
            langName = "Swati";
            break;
        case "kl":
            langName = "Kalaallisut";
            break;
        case "st":
            langName = "Sotho, Southern";
            break;
        case "ce":
            langName = "Chechen";
            break;
        case "km":
            langName = "Central Khmer";
            break;
        case "su":
            langName = "Sundanese";
            break;
        case "kn":
            langName = "Kannada";
            break;
        case "sv":
            langName = "Swedish";
            break;
        case "ko":
            langName = "Korean";
            break;
        case "sw":
            langName = "Swahili";
            break;
        case "ch":
            langName = "Chamorro";
            break;
        case "kr":
            langName = "Kanuri";
            break;
        case "ks":
            langName = "Kashmiri";
            break;
        case "ku":
            langName = "Kurdish";
            break;
        case "kv":
            langName = "Komi";
            break;
        case "co":
            langName = "Corsican";
            break;
        case "kw":
            langName = "Cornish";
            break;
        case "ta":
            langName = "Tamil";
            break;
        case "ky":
            langName = "Kirghiz";
            break;
        case "cr":
            langName = "Cree";
            break;
        case "cs":
            langName = "Czech";
            break;
        case "te":
            langName = "Telugu";
            break;
        case "cu":
            langName = "Church Slavic";
            break;
        case "cv":
            langName = "Chuvash";
            break;
        case "tg":
            langName = "Tajik";
            break;
        case "th":
            langName = "Thai";
            break;
        case "la":
            langName = "Latin";
            break;
        case "ti":
            langName = "Tigrinya";
            break;
        case "cy":
            langName = "Welsh";
            break;
        case "lb":
            langName = "Luxembourgish";
            break;
        case "tk":
            langName = "Turkmen";
            break;
        case "tl":
            langName = "Tagalog";
            break;
        case "tn":
            langName = "Tswana";
            break;
        case "lg":
            langName = "Ganda";
            break;
        case "to":
            langName = "Tonga";
            break;
        case "da":
            langName = "Danish";
            break;
        case "li":
            langName = "Limburgish";
            break;
        case "tr":
            langName = "Turkish";
            break;
        case "ts":
            langName = "Tsonga";
            break;
        case "tt":
            langName = "Tatar";
            break;
        case "de":
            langName = "German";
            break;
        case "ln":
            langName = "Lingala";
            break;
        case "lo":
            langName = "Lao";
            break;
        case "tw":
            langName = "Twi";
            break;
        case "ty":
            langName = "Tahitian";
            break;
        case "lt":
            langName = "Lithuanian";
            break;
        case "lu":
            langName = "Luba-Katanga";
            break;
        case "lv":
            langName = "Latvian";
            break;
        case "dv":
            langName = "Divehi";
            break;
        case "ug":
            langName = "Uighur";
            break;
        case "dz":
            langName = "Dzongkha";
            break;
        case "uk":
            langName = "Ukrainian";
            break;
        case "mg":
            langName = "Malagasy";
            break;
        case "mh":
            langName = "Marshallese";
            break;
        case "mi":
            langName = "Maori";
            break;
        case "ur":
            langName = "Urdu";
            break;
        case "mk":
            langName = "Macedonian";
            break;
        case "ml":
            langName = "Malayalam";
            break;
        case "ee":
            langName = "Ewe";
            break;
        case "mn":
            langName = "Mongolian";
            break;
        case "mr":
            langName = "Marathi";
            break;
        case "uz":
            langName = "Uzbek";
            break;
        case "ms":
            langName = "Malay";
            break;
        case "el":
            langName = "Greek, Modern";
            break;
        case "mt":
            langName = "Maltese";
            break;
        case "en":
            langName = "English";
            break;
        case "eo":
            langName = "Esperanto";
            break;
        case "my":
            langName = "Burmese";
            break;
        case "es":
            langName = "Spanish";
            break;
        case "et":
            langName = "Estonian";
            break;
        case "ve":
            langName = "Venda";
            break;
        case "eu":
            langName = "Basque";
            break;
        case "na":
            langName = "Nauru";
            break;
        case "vi":
            langName = "Vietnamese";
            break;
        case "nb":
            langName = "Bokm\u00e5l";
            break;
        case "nd":
            langName = "Simbabwe";
            break;
        case "ne":
            langName = "Nepali";
            break;
        case "ng":
            langName = "Ndonga";
            break;
        case "vo":
            langName = "Volap";
            break;
        case "fa":
            langName = "Persian";
            break;
        case "nl":
            langName = "Dutch";
            break;
        case "ff":
            langName = "Fulah";
            break;
        case "nn":
            langName = "Norwegian Nynorsk";
            break;
        case "no":
            langName = "Norwegian";
            break;
        case "fi":
            langName = "Finnish";
            break;
        case "fj":
            langName = "Fijian";
            break;
        case "nr":
            langName = "Transvaal";
            break;
        case "nv":
            langName = "Navajo";
            break;
        case "fo":
            langName = "Faroese";
            break;
        case "wa":
            langName = "Walloon";
            break;
        case "ny":
            langName = "Chichewa";
            break;
        case "fr":
            langName = "French";
            break;
        case "fy":
            langName = "Western Frisian";
            break;
        case "oc":
            langName = "Occitan";
            break;
        case "wo":
            langName = "Wolof";
            break;
        case "ga":
            langName = "Irish";
            break;
        case "oj":
            langName = "Ojibwa";
            break;
        case "gd":
            langName = "Gaelic";
            break;
        case "om":
            langName = "Oromo";
            break;
        case "or":
            langName = "Oriya";
            break;
        case "os":
            langName = "Ossetian";
            break;
        case "gl":
            langName = "Galician";
            break;
        case "gn":
            langName = "Guarani";
            break;
        case "gu":
            langName = "Gujarati";
            break;
        case "gv":
            langName = "Manx";
            break;
        case "xh":
            langName = "Xhosa";
            break;
        case "pa":
            langName = "Panjabi";
            break;
        case "ha":
            langName = "Hausa";
            break;
        case "pi":
            langName = "Pali";
            break;
        case "pl":
            langName = "Polish";
            break;
        case "he":
            langName = "Hebrew";
            break;
    }
    if (langSplit.length === 2) {
        switch (langSplit[1].toLowerCase()) {
            case "de":
                countryName = "Germany";
                break;
            case "pr":
                countryName = "Puerto Rico";
                break;
            case "hk":
                countryName = "Hong Kong, Special Administrative Region of China";
                break;
            case "tw":
                countryName = "Taiwan, Province of China";
                break;
            case "pt":
                countryName = "Portugal, Portuguese Republic";
                break;
            case "hn":
                countryName = "Honduras, Republic of";
                break;
            case "dk":
                countryName = "Denmark, Kingdom of";
                break;
            case "lt":
                countryName = "Lithuania";
                break;
            case "lu":
                countryName = "Luxembourg, Grand Duchy of";
                break;
            case "py":
                countryName = "Paraguay, Republic of";
                break;
            case "hr":
                countryName = "Hrvatska (Croatia)";
                break;
            case "lv":
                countryName = "Latvia";
                break;
            case "do":
                countryName = "Dominican Republic";
                break;
            case "ua":
                countryName = "Ukraine";
                break;
            case "ye":
                countryName = "Yemen";
                break;
            case "hu":
                countryName = "Hungary, Hungarian People's Republic";
                break;
            case "ly":
                countryName = "Libyan Arab Jamahiriya";
                break;
            case "qa":
                countryName = "Qatar, State of";
                break;
            case "ma":
                countryName = "Morocco, Kingdom of";
                break;
            case "dz":
                countryName = "Algeria, People's Democratic Republic of";
                break;
            case "me":
                countryName = "Montenegro";
                break;
            case "id":
                countryName = "Indonesia, Republic of";
                break;
            case "ie":
                countryName = "Ireland";
                break;
            case "ec":
                countryName = "Ecuador, Republic of";
                break;
            case "mk":
                countryName = "Macedonia, the former Yugoslav Republic of";
                break;
            case "us":
                countryName = "United States of America";
                break;
            case "ee":
                countryName = "Estonia";
                break;
            case "eg":
                countryName = "\u00c4gypten";
                break;
            case "il":
                countryName = "Israel, State of";
                break;
            case "ae":
                countryName = "United Arab Emirates (was Trucial States)";
                break;
            case "uy":
                countryName = "Uruguay, Eastern Republic of";
                break;
            case "in":
                countryName = "India, Republic of";
                break;
            case "mt":
                countryName = "Malta, Republic of";
                break;
            case "za":
                countryName = "South Africa, Republic of";
                break;
            case "iq":
                countryName = "Iraq, Republic of";
                break;
            case "ir":
                countryName = "Iran, Islamic Republic of";
                break;
            case "is":
                countryName = "Iceland, Republic of";
                break;
            case "al":
                countryName = "Albania, People's Socialist Republic of";
                break;
            case "it":
                countryName = "Italy, Italian Republic";
                break;
            case "mx":
                countryName = "Mexico, United Mexican States";
                break;
            case "my":
                countryName = "Malaysia";
                break;
            case "es":
                countryName = "Spain, Spanish State";
                break;
            case "ve":
                countryName = "Venezuela, Bolivarian Republic of";
                break;
            case "ar":
                countryName = "Argentina, Argentine Republic";
                break;
            case "at":
                countryName = "Austria, Republic of";
                break;
            case "au":
                countryName = "Australia, Commonwealth of";
                break;
            case "vn":
                countryName = "Viet Nam, Socialist Republic of (was Democratic Republic of & Republic of)";
                break;
            case "ni":
                countryName = "Nicaragua, Republic of";
                break;
            case "ro":
                countryName = "Romania, Socialist Republic of";
                break;
            case "nl":
                countryName = "Netherlands, Kingdom of the";
                break;
            case "ba":
                countryName = "Bosnia and Herzegovina";
                break;
            case "no":
                countryName = "Norway, Kingdom of";
                break;
            case "rs":
                countryName = "Serbia";
                break;
            case "be":
                countryName = "Belgium, Kingdom of";
                break;
            case "fi":
                countryName = "Finland, Republic of";
                break;
            case "ru":
                countryName = "Russian Federation";
                break;
            case "bg":
                countryName = "Bulgaria, People's Republic of";
                break;
            case "jo":
                countryName = "Jordan, Hashemite Kingdom of";
                break;
            case "bh":
                countryName = "Bahrain, Kingdom of";
                break;
            case "jp":
                countryName = "Japan";
                break;
            case "fr":
                countryName = "France, French Republic";
                break;
            case "nz":
                countryName = "Zealand";
                break;
            case "bo":
                countryName = "Bolivia, Republic of";
                break;
            case "sa":
                countryName = "Saudi Arabia, Kingdom of";
                break;
            case "br":
                countryName = "Brazil, Federative Republic of";
                break;
            case "sd":
                countryName = "Sudan, Democratic Republic of the";
                break;
            case "se":
                countryName = "Sweden, Kingdom of";
                break;
            case "sg":
                countryName = "Singapore, Republic of";
                break;
            case "si":
                countryName = "Slovenia";
                break;
            case "by":
                countryName = "Belarus";
                break;
            case "sk":
                countryName = "Slovakia (Slovak Republic)";
                break;
            case "gb":
                countryName = "Great Britain";
                break;
            case "ca":
                countryName = "Canada";
                break;
            case "om":
                countryName = "Oman, Sultanate of (was Muscat and Oman)";
                break;
            case "sv":
                countryName = "Salvador, Republic of";
                break;
            case "ch":
                countryName = "Switzerland, Swiss Confederation";
                break;
            case "sy":
                countryName = "Syrian Arab Republic";
                break;
            case "kr":
                countryName = "Korea, Republic of";
                break;
            case "cl":
                countryName = "Chile, Republic of";
                break;
            case "cn":
                countryName = "China, People's Republic of";
                break;
            case "gr":
                countryName = "Greece, Hellenic Republic";
                break;
            case "co":
                countryName = "Colombia, Republic of";
                break;
            case "kw":
                countryName = "Kuwait, State of";
                break;
            case "gt":
                countryName = "Guatemala, Republic of";
                break;
            case "cr":
                countryName = "Costa Rica, Republic of";
                break;
            case "cs":
                countryName = "Serbia and Montenegro";
                break;
            case "pa":
                countryName = "Panama, Republic of";
                break;
            case "th":
                countryName = "Thailand, Kingdom of";
                break;
            case "pe":
                countryName = "Peru, Republic of";
                break;
            case "cy":
                countryName = "Cyprus, Republic of";
                break;
            case "lb":
                countryName = "Lebanon, Lebanese Republic";
                break;
            case "cz":
                countryName = "Czech Republic";
                break;
            case "ph":
                countryName = "Philippines, Republic of the";
                break;
            case "tn":
                countryName = "Tunisia, Republic of";
                break;
            case "pl":
                countryName = "Poland, Polish People's Republic";
                break;
            case "tr":
                countryName = "Turkey, Republic of";
                break;
        }
        return langName + " (" + countryName + ")";
    } else {
        return langName;
    }
}
