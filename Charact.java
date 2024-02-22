import lingologs.Script;
import lingologs.Texture;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Charact
{
    // Hinweis:
    // * Die Bibliothek 'LingoLibry' muss als JAR eingebunden werden
    // * Das JAR enthält sowohl die nutzbaren Packages/Klassen als auch
    //   deren Quelltexte (jeweils 2. Eintrag unter zwei gleichen)

    // Generische Palingramm-Erkennung (als Beispiel-Algorithmus für unten benötigt)
    public static <T, U> boolean isPalingram
    (T Chars, Function<T, Integer> Size,    // Funktion für Längen-Ermittlung
     BiFunction<T, Integer, U> At)          // Funktion für Zugriff per Index auf
    {                                       // String-char oder Script-Charact
        int X = 0, Y = Size.apply(Chars)-1;
        while (X < Y)
            if (At.apply(Chars, X) != At.apply(Chars, Y))
                return false;
            else { X++; Y--; }
        return true;
    }

    public static void main(String[] args)
    {
        // ### Charact ###
        {
            /*
            Konzept der Charact-Klasse:
            * einheitliche Darstellung eines Unicode-Kodepunkts/Characters als UTF32
              (Vermeidung von UTF16-Surrogat-Paaren und entspr. Verarbeitungsfehlern)
              Charact: ☺/😀 (U+263A/U+1F600 BPM/SMP) vs. Character/char: ☺ (nur BMP)
            * UTF16 (wie UTF8) sind im Gegensatz zu UTF32 eigentlich komprimierte
              Datenformate, die zur korrekten und effizienten Verarbeitung vorher
              dekomprimiert werden müssten (z.B. U+D83D/U+DE00 => U+1F600)
            * im Gegensatz zur Character-Klasse von Java (als Wrapper-Klasse für
              den primitiven Datentyp char) können Characts direkt als Objekte
              genutzt werden (z.B. C.toString(), C.isLetter() usw.)
            * Charact dient als Komponente für Script, das aus einer Kette von
              (gepoolten) Characts besteht
            */

            System.out.println();
            System.out.println("###############");
            System.out.println("### Charact ###");
            System.out.println("###############");
            System.out.println();

            // Erzeugung (selten direkt, meist indirekt über Script-Erzeugung)
            lingologs.Charact
                C1 = new lingologs.Charact('A'),
                C2 = new lingologs.Charact("\uD83D\uDE00"),   // 😀
                C3 = new lingologs.Charact(0x1F600),          // 😀
                C4 = lingologs.Charact.of('A'),
                C5 = lingologs.Charact.of("\uD83E\uDD21"),    // 🤡
                C6 = lingologs.Charact.of(0x1F921);           // 🤡
            /*
            Charact
                D1 = new Charact('\uD83E'),             // ungepaarte bzw.
                D2 = new Charact('\uDE00'),             // falsch gepaarte
                D3 = new Charact('\uDE00', '\uD83E');   // Surrogat-Zeichen
            */

            @SuppressWarnings("all")    // (wg. Identitätsvergleich per == und !=)
            boolean E = C1.equals(C4) && C1 != C4 &&    // verschiedene Objekte (gepoolt/ungepoolt)
                        C2.equals(C3) && C2 != C3 &&    // verschiedene Objekte (ungepoolt)
                        C5.equals(C6) && C5 == C6;      // identische Objekte (gepoolt)

            System.out.println("Erzeugung: "+E);
        }

        // ### Script ###
        {
            /*
            Konzept der Script-Klasse:
            * einheitliche Darstellung einer Zeichenkette aus Characts (UTF32),
              somit auch keine fehlerhaften Surrogat-Kombinationen (s. Bsp. u.)
            * Zugriff auf Script-Characts/Kodepunkte in konstanter Zeit vs.
              Zugriff auf String-Characters/Kodepunkte in linearer Zeit (s. u.)
            * unveränderliche (immutable) Datenstruktur analog zu String, d.h.
              Veränderungen erzeugen neue Scripts/Strings (vs. Script.Builder)
            * effizientere parallele/nebenläufige Multithreading-Verarbeitung
              aufgrund der Immutabilität (keine Synchronisierung notwendig)
            * Scripts können analog Characts per Script.of(…) zur Laufzeit
              gepoolt werden, ähnlich wie Strings per intern()
            * Hinweis: Script ist zwar kompatibel mit dem Interface CharSequence
              (analog String/StringBuilder), aber bitte nicht Stringfunktionen
              length(), charAt() oder subSequence() benutzen, da nur UTF16
              (stattdessen UTF32-Pendants extent(), at() und part())!
            */

            System.out.println();
            System.out.println("##############");
            System.out.println("### Script ###");
            System.out.println("##############");
            System.out.println();

            // (In)Korrekte Zeichendaten-Verarbeitung bei String vs. Script
            // (z.T. Grundlagen-Beispiele aus JavaDoc zu Script-Klasse)
            {
                // Erzeugung String vs. Script
                {
                    String
                        R = "\uD83D\uDE00",                 // 😀 (U+1F600) korrekt
                        R1 = "\uDE00\uD83D",
                        R2 = "\uD83D", R3 = "\uDE00";       // R1 bis R3 alle inkorrekt
                    Script
                        S1 = new Script(R),                 // R1 bis R3 würden hier
                        S2 = new Script(0x1F600);      // zu Exception führen

                    boolean E = S1.equals(S2) && S1.toString().equals(R); // true

                    System.out.println("Erzeugung: "+E+"\r\n");
                }

                // Zugriff/Verarbeitung String vs. Script
                {
                    String R = "W\uD83D\uDE00W";    // UTF16-Kodiereinheiten ("W😀W")
                    Script S = new Script(R);       // UTF32-Kodepunkte (Characts)

                    // Zugriff
                    int L = R.length(),                             // 4 UTF16-chars vs.
                        M = R.codePointCount(0, L), N = S.extent(); // 3 UTF32-Kodepunkte

                    boolean E1 = (L == 4 && M == 3 && N == 3);      // true

                    char C1 = R.charAt(1), C2 = R.charAt(2);        // ungültige Surrogate 0xD83D / 0xDE00
                    lingologs.Charact D1 = S.at(1), D2 = S.at(2);        // Kodepunkte 0x1F600 ('😀') / 0x0057 ('W')

                    boolean
                        E2 = (C1 == 0xD83D && C2 == 0xDE00) &&
                             (D1.toCode() == 0x1F600 && D2.toCode() == 0x0057); // true

                    System.out.println("Zugriff: "+E1+", "+E2);

                    // Verarbeitung
                    String
                        R1 = R.substring(0, 2),     // ungültiges 'W'+0xD83D
                        R2 = R.substring(1, 3);     // unerwartetes 0xD83D+0xDE00
                    Script
                        S1 = S.part(0, 2),      // gültiges "W😀" (1. Bigramm)
                        S2 = S.part(1, 3);      // gültiges "😀W" (2. Bigramm)

                    boolean
                        E3 = R1.equals("W\uD83D") && R2.equals("\uD83D\uDE00") &&
                             S1.equals(S.intro(2)) && S2.equals(S.extro(2)); // true

                    System.out.println("Verarbeitung: "+E3+"\r\n");
                }

                // Ersetzung String vs. Script
                {
                    String
                        R1 = "W\uD83D\uDE00W",
                        R2 = R1.replaceAll("", "_");   // UTF16
                    Script
                        S1 = new Script(R1),
                        S2 = S1.replace("", "_");           // UTF32

                    boolean
                        E = R2.equals("_W_\uD83D_\uDE00_W_") &&        // äußerst unerwartet!
                            S2.equals(new Script("_W_\uD83D\uDE00_W_")); // true

                    System.out.println("Ersetzung: "+E+"\r\n");
                }

                // Palingramm-Erkennung für String vs. Script:
                // Korrekter Algorithmus, aber falsche Daten-Repräsentation (!)
                // String R mit 4 UTF16-chars vs. Script S mit 3 UTF32-Kodepunkten
                {
                    String R = "W😀W";          // W😀W intern "W\uD83D\uDE00W"
                    Script S = new Script(R);   // W😀W intern [0x0057, 0x1F600, 0x0057]

                    boolean
                        IsPalinString = isPalingram(R, String::length, String::charAt),
                        IsPalinScript = isPalingram(S, Script::extent, Script::at);

                    boolean E = !IsPalinString &&   // R wird nicht als Palingramm erkannt,
                                IsPalinScript;      // obwohl es eines ist!

                    System.out.println("Palingramm: "+E+"\r\n");
                }

                // Achtung: Man sollte nicht glauben, andere Programmiersprachen hätten
                // hier keine Ungereimtheiten/Fehler - diese können sehr diffizil und
                // kaum zu erkennen sein (zumal UTF16-Darstellung bei Textdaten fast
                // immer der Standard ist)

                // Todo Fragen:
                // * Welche String-Algorithmen sind evtl. noch gefährdet?
                // * Welche String-Operationen sind evtl. noch betroffen?
            }

            // (In)Effiziente Zeichendaten-Verarbeitung bei String vs. Script
            {
                String R = "W\uD83D\uDE00W";
                Script S = new Script(R);

                int X = 2, I = R.offsetByCodePoints(0, X);  // UTF32-Index X = 2 vs. UTF16-Index I = 3!
                char CR = R.charAt(I);  // Zugriff in linearer Zeit (ineffizienter)
                lingologs.Charact CS = S.at(X);   // Zugriff in konstanter Zeit (effizienter)

                System.out.println("Index UTF16: "+I+"\tChar UTF16: "+CR);
                System.out.println("Index UTF32: "+X+"\tCharact UTF32: "+CS+"\r\n");

                // Todo Fragen:
                // * Wie müsste man die Zugriffsindexe X/Y für R.substring(X, Y) ermitteln?
                // * Was entspricht R.codePointCount(0, R.length()) bei Script?
            }

            // Arithmetische Grundoperationen (analog Zahlenalgebra)
            {
                Script
                    S0 = Script.NIX,
                    S1 = new Script("knock"),
                    S2 = S1.add(S1), S3 = S2.mul(2),
                    S44 = S3.div(4), S42 = S3.div(2),
                    S41 = S3.div(1), S40 = S3.div(8, S0),
                    S51 = S3.sub(5, S1).sub(S1).sub(S1),
                    S50 = S2.sub(1, S1, S0);
                int N1 = S3.div(S1), N2 = S3.div(S2), N3 = S3.div(S3);

                boolean
                    E = S2.equals(Script.join(S0, S1, S1)) &&
                        S3.equals(Script.join(S0, S2, S2)) &&
                        S44.equals(S1) && S42.equals(S2) &&
                        S41.equals(S3) && S40.equals(S0) &&
                        S51.equals(S1) && S50.isEmpty() &&
                        N1 == 4 && N2 == 2 && N3 == 1;  // true
                System.out.println("Arithmetik: "+E+"\r\n");

                // Todo Fragen:
                // * Was ist letztlich der Unterschied zwischen Zahlen- und
                //   Zeichenketten-Arithmetik?
                // * Könnte man weitere arithmetische Operatoren definieren?
            }

            // Grundlegende Zugriffsfunktionen
            {
                Script
                    S = new Script("EINSTEIN"), R = new Script("ONE"),
                    S1 = S.intro(3), S2 = S.part(3, 5), S3 = S.extro(5);
                Script
                    R1 = S1.add(S2, Script.SP, S1, Script.SP, S3),
                    R2 = S.intro(3, R).extro(3, R),
                    R3 = S.part(1, 5, new Script("LFENB"));

                boolean
                    E1 = R1.equals(new Script("EINST EIN STEIN")) &&
                         R2.equals(new Script("ONESTONE")) &&
                         R3.equals(new Script("ELFENBEIN"));

                Script
                    T1 = S.intro(3, Script.NIX),                // delete
                    T2 = S.extro(3, Script.NIX),                // delete
                    T3 = S.extro(0, new Script("IUM")),    // insert
                    T4 = S.part(3, 3, Script.SP),             // insert
                    T5 = S.at(6, lingologs.Charact.R);                    // replace

                boolean
                    E2 = T1.equals(S3) && T2.equals(S3.rotate(3)) &&
                         T3.equals(new Script("EINSTEINIUM")) &&
                         T4.equals(new Script("EIN STEIN")) &&
                         T5.equals(new Script("EINSTERN"));

                System.out.println("Zugriffe: "+E1+", "+E2+"\r\n");

                // Todo Aufgaben:
                // * Sortieren Sie S ("EINSTEIN") vor-/rückwärts nach den Zeichen!
                //   Rotieren Sie S um drei Stellen vor-/rückwärts!
                //   Vertauschen Sie in S die ersten mit den letzten vier Zeichen!
                // * Zerschneiden Sie S in vier bzw. zwei gleich große Teile!
                //   Zerlegen Sie S nach dem Trenner "ST"!
                // * Lassen Sie von S eine Zählung ('Strichliste') aller vorkommenden
                //   Zeichen mit ihren zugehörigen Häufigkeiten erzeugen!
                //   Lassen Sie von S eine Tabelle aller auftretenden Zeichen(typen)
                //   mit einer Liste ihrer Indexpositionen in S erzeugen!
            }

            // Streaming-Funktionen ('Fluent Interfaces')
            {
                // Acrogramm-Erzeugung
                Script
                    S = new Script("What you see is what you get");
                Texture<Script>
                    T = new Texture<>(S.split());

                Script
                    R = T.merge(new Script.Builder(),
                                (Script.Builder B, Script P) -> B.attach(P.at(0)))
                         .toScript().toUpper();
                boolean E1 = R.equals(new Script("WYSIWYG"));

                // Chronogramm-Erzeugung
                Script				        // Chronogramm für 'Great Fire of London' 1666
                    U = new Script("Lord Have Merci Vpon Vs");	// L+D+V+M+C+I+V+V
                Map<lingologs.Charact, Integer>
                    M = Map.of(lingologs.Charact.I, 1, lingologs.Charact.V, 5, lingologs.Charact.X, 10,
                               lingologs.Charact.L, 50, lingologs.Charact.C, 100,
                               lingologs.Charact.D, 500, lingologs.Charact.M, 1000);

                int N = U.map(lingologs.Charact::toUpper)
                         .filter(M::containsKey).map(M::get)
                         .merge(0, Integer::sum);
                boolean E2 = (N == 1666);	// dasselbe wie M+D+C+L+X+V+I (!)

                System.out.println("Streaming: "+E1+", "+E2+"\r\n");

                // Todo Aufgaben:
                // * Erzeugen Sie per 'make' ein Script von 'A' bis 'Z' (26 Characts)!
                // * Addieren Sie deren Unicode-Werte mittels eines Script-Streams zu
                //   einem Gesamtwert auf (d.h. Summe von 'A'=65 bis 'Z'=90 bilden)!
                // * Bilden Sie aus dem oben erzeugten Script die entspr. Variante mit
                //   so genannten FullWidth-Zeichen von 'Ａ'=0xFF21 bis 'Ｚ'=0xFF3A!
            }
        }
    }
}
