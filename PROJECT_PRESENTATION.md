# Projektvorstellung

Daily Habits ist eine Webanwendung, die Nutzer bei kleinen positiven Gewohnheiten im Alltag unterstuetzt. Die App stellt Challenges bereit, also kurze Aufgaben aus Kategorien wie Fitness, Lernen, Gesundheit, Alltag oder Sozial. Sie wurde entwickelt, um alltaegliche Aktivitaeten abwechslungsreicher zu machen und Nutzer zu motivieren, kleine Aufgaben bewusst umzusetzen. Nutzer koennen gespeicherte Challenges anzeigen, neue Challenges erstellen, Challenges als erledigt markieren und zufaellige Vorschlaege generieren lassen. Ein Vorschlag wird nicht automatisch gespeichert, sondern erst dann dauerhaft uebernommen, wenn das Frontend ihn an die bestehende POST-Route sendet. Das Backend basiert auf Java 21, Spring Boot 4.0.5, Spring Web MVC und Spring Data JPA. PostgreSQL speichert die Challenge-Daten dauerhaft, waehrend H2 fuer Backend-Tests verwendet wird. Das Frontend ruft die REST-Endpunkte des Backends auf, das Backend liest oder schreibt Challenge-Daten ueber JPA, und PostgreSQL sorgt dafuer, dass gespeicherte Challenges nach einem Neuladen oder Neustart erhalten bleiben.

# Umgesetzte Use-Cases

## Use-Case 1

### Fuer Nutzer erklaert

Nutzer sehen eine Liste ihrer Challenges. Die Liste bleibt auch nach einem Neuladen erhalten, solange die Challenges gespeichert wurden.

### Technisch erklaert

Das Frontend ruft `GET /api/v1/challenges` auf. Das Backend liest alle gespeicherten Challenges aus PostgreSQL und gibt sie als JSON-Liste zurueck. Es werden Daten gelesen, aber nicht veraendert.

## Use-Case 2

### Fuer Nutzer erklaert

Nutzer koennen sich eine zufaellige neue Challenge vorschlagen lassen. Dieser Vorschlag ist zunaechst nur ein Vorschlag und wird nicht automatisch gespeichert.

### Technisch erklaert

Das Frontend ruft `GET /api/v1/challenges/suggestions/random` auf. Das Backend waehlt zufaellig einen Eintrag aus einem festen Vorschlags-Pool und gibt `title`, `category` und `done=false` zurueck. PostgreSQL wird dafuer nicht verwendet.

## Use-Case 3

### Fuer Nutzer erklaert

Nutzer koennen einen vorgeschlagenen Eintrag uebernehmen. Danach wird daraus eine gespeicherte Challenge.

### Technisch erklaert

Das Frontend sendet den Vorschlag mit `POST /api/v1/challenges` an das Backend. Das Backend speichert daraus eine neue `Challenge` in PostgreSQL. Die gespeicherte Challenge bekommt eine Datenbank-ID und bleibt erhalten.

## Use-Case 4

### Fuer Nutzer erklaert

Nutzer koennen eigene Challenges erstellen. Diese Challenges erscheinen anschliessend in der Liste.

### Technisch erklaert

Das Frontend sendet `title`, `category` und `done` per `POST /api/v1/challenges`. Das Backend setzt die ID vor dem Speichern auf `null`, damit PostgreSQL eine neue ID erzeugt. Die Challenge wird dauerhaft in PostgreSQL gespeichert.

## Use-Case 5

### Fuer Nutzer erklaert

Nutzer koennen eine Challenge als erledigt markieren und auch wieder zuruecksetzen. Der Status bleibt gespeichert.

### Technisch erklaert

Das Frontend ruft `PATCH /api/v1/challenges/{id}/toggle` auf. Das Backend sucht die Challenge anhand der ID, dreht den Wert `done` um und speichert die aktualisierte Challenge. PostgreSQL wird gelesen und anschliessend aktualisiert.

## Use-Case 6

### Fuer Nutzer erklaert

Nutzer koennen sehen, wie viele Challenges erledigt sind. Der Fortschritt basiert auf den gespeicherten Erledigt-Markierungen.

### Technisch erklaert

Das Frontend kann `GET /api/v1/challenges` verwenden und aus den gelieferten `done`-Werten den Fortschritt berechnen. Es gibt keinen eigenen Progress-Endpunkt und keine eigene Progress-Tabelle. PostgreSQL speichert die Grundlage des Fortschritts ueber die Challenges und ihren `done`-Status.

## Use-Case 7

### Fuer Nutzer erklaert

Nutzer koennen Challenges nach Kategorien wie Fitness oder Lernen filtern. Dadurch sehen sie nur passende Aufgaben.

### Technisch erklaert

Das Frontend ruft `GET /api/v1/challenges?category=...` auf. Das Backend verwendet eine Repository-Methode, die nach Kategorie filtert, und gibt nur passende gespeicherte Challenges zurueck. PostgreSQL wird gelesen, aber nicht veraendert.

# Persistente Daten

Dauerhaft in PostgreSQL gespeichert werden Challenges mit den Feldern `id`, `title`, `category` und `done`. Nach einem Neuladen bleiben erstellte Challenges, uebernommene Vorschlaege und der erledigt/nicht-erledigt Status erhalten. Die Persistenz entsteht ueber Spring Data JPA und das `ChallengeRepository`.

# Nicht persistente Funktionen

Zufaellige Challenge-Vorschlaege aus `GET /api/v1/challenges/suggestions/random` existieren zunaechst nur als Antwort des Backends und werden nicht automatisch gespeichert. Das ist sinnvoll, weil Nutzer erst entscheiden sollen, ob sie den Vorschlag uebernehmen moechten. Informationen wie ein eigener Fortschrittswert oder eine Streak-Anzeige werden aktuell nicht als separate Daten gespeichert. Eine Streak-Anzeige waere deshalb in der aktuellen Implementierung nicht persistent, weil es keine Streak-Entity, keine Streak-Tabelle und keinen Streak-Endpunkt gibt.

# Kurze Zusammenfassung fuer die Praesentation

Daily Habits ist eine App fuer kleine Challenges im Alltag. Nutzer koennen Challenges anzeigen, eigene Aufgaben erstellen und erledigte Aufgaben markieren. Das Backend speichert Challenges dauerhaft in PostgreSQL. Zufaellige Challenge-Vorschlaege werden aus einem festen Pool generiert und erst gespeichert, wenn Nutzer sie uebernehmen. Die App unterstuetzt Kategorien, sodass Challenges gefiltert angezeigt werden koennen. Der Fortschritt kann aus den gespeicherten `done`-Werten berechnet werden. Technisch nutzt das Backend Java 21, Spring Boot 4.0.5, Spring Web MVC, Spring Data JPA und PostgreSQL. Tests laufen mit H2 und MockMvc. GitHub Actions fuehrt die Backend-Tests automatisch bei Pushes und Pull Requests auf `main` aus.

# Quellen der Dokumentation

Aus dem Code abgeleitet wurden REST-Endpunkte, Entity-Felder, Persistenzverhalten, Testabdeckung, Java-Version, Spring-Boot-Version und GitHub-Actions-Verhalten.

Aus der README uebernommen wurden die Projektidee, die Motivation der App und die genannten Funktionsbereiche wie zufaellige Challenges, Erledigt-Markierung, Fortschritt, Kategorien und eigene Challenges.
