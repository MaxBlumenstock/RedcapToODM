1. Repository klonen und branch "shared" auschecken.
2. Von Shared aus eigenen Branch erstellen namens "[Konvertername]-master"
3. Dateien anpassen:
    1. pom.xml : <name> und <artifactId> ändern
    1. pom.xml: Zeile 311 "Path" ändern
    1. pom.xml: Zeile 331: finalName ändern
    2. context.xml: Pfad des Kontextes anpassen
    3. tiles-def.xml: Eventuell CSS/jQuery konfigurieren (und auch ins Projekt kopieren).
    4. entityManagerDefinition.xml (Falls mySQL-Conenction benötigt wird)

4. Eigene Routinen ins Projekt kopieren und benötigte Abhängigkeiten hinzufügen.
5. Die Webseiten unter Umständen aus dem Unterordner heraus nehmen und nach Web-Inf packen ohen Unterordner (die War wird ja jetzt nicht mehr ins Root, sondern in den Unterordner deployed).
6. Im Controller analog zu 5 die Pfade der RequestMappings und returns anpassen.