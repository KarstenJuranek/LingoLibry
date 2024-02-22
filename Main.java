import lingolava.Legacy;
import lingolava.Tuple;
import lingologs.Charact;
import lingologs.Script;
import lingologs.Texture;

import static lingolava.Tuple.*;

import java.util.*;

public class Main
{
    // Hinweis:
    // * Die Bibliothek 'LingoLibry' muss als JAR eingebunden werden
    // * Das JAR enthält sowohl die nutzbaren Packages/Klassen als auch
    //   deren Quelltexte (jeweils 2. Eintrag unter zwei gleichen)

    public static void main(String[] args)
    {
        // ### Texture ###
        {
            /*
            Konzept der Texture-Klasse:
            * Listenartige Datenstruktur ähnlich (Array)List mit Typisierung
              der Komponenten per <T> (z.B. Texture<Integer> oder Texture<Script>)
            * Texture ist die allgemeine Oberklasse zu Script bzw. Script ist eine
              Unterklasse von Texture mit dem alleinigen Komponententyp Charact:
              d.h. Klasse Script erweitert Texture<Charact>
            * Die typ-technische Nähe von Texture und Script erlaubt es, einen
              Großteil der Operationen/Methoden zu teilen: d.h. alles von Texture
              existiert auch in Script (wobei Script v.a. um zeichen-spezifische
              Operationen erweitert ist)
            * Texture kann auch wie eine (Multi-)Menge verarbeitet werden, d.h. es
              sind verallgemeinerte Mengenoperationen möglich (Schnittmenge etc.)
            * Verarbeitungstechnisch wird eine Texture analog Script genau wie ein
              String/Stream als funktional behandelt: Eine Texture-Operation liefert
              wieder eine Texture als Ergebnis (vs. List erzeugt keine neue List!),
              so dass Operationen/Funktionen per '.' beliebig kaskadierbar sind
            * Texture ist wie Script (und String) immutabel: Die Verknüpfung zweier
              Textures ergibt eine neue veränderte Texture (d.h. die Original-Textures
              werden niemals verändert)
            * daher ist wiederum effizientere parallele/nebenläufige Multithreading-
              Verarbeitung aufgrund der Immutabilität möglich (d.h. read-only/many,
              write/create-once)
            */

            System.out.println();
            System.out.println("###############");
            System.out.println("### Texture ###");
            System.out.println("###############");
            System.out.println();

            // Erzeugung
            {
                Texture<Integer>
                    T1 = new Texture<>(1, 2, 3, 4, 5, 6),
                    T2 = Texture.make(1, X -> X <= 6, X -> X+1),
                    T3 = Texture.join(0, List.of(T1, T2)),  // T1+T2 mit Separator 0
                    T4 = Texture.join(0, T1, 2),         // zwei Mal T1 mit Separator 0
                    T5 = Texture.mul(0, 6);             // sechs Mal 0
                Texture<Charact>
                    S1 = new Script("ABCDEF"),
                    S2 = Script.make(Charact.A, C -> C.compareTo(Charact.F) <= 0, Charact::next);

                boolean E = T1.equals(T2) && T3.equals(T4) &&
                            T5.extent() == 6 && S1.equals(S2);

                System.out.println("Erzeugung: "+E+"\r\n");
            }

            // Grundoperationen (Zugriff)
            {
                Texture<String>
                    T1 = new Texture<>("to", "do", "or", "not", "to", "do"),
                    T2 = T1.intro(2), T3 = T1.extro(2), T4 = T2.add(T3),  // Anfang/Ende
                    T5 = T1.part(2, 4), T6 = T1.sub(2, T5);             // Mitte/Ränder
                String
                    R1 = T1.at(0), R2 = T1.at(4);       // jeweils "to"

                boolean E1 = T2.equals(T3) && T4.equals(T6) && R1.equals(R2);

                System.out.println("Zugriff lesend: "+E1);

                String S = "\uD83D\uDE00";      // 😀
                Texture<String>
                    U = new Texture<>(S),
                    U1 = T1.at(1, "be").at(5, "be"),
                    U2 = T1.replace("do", "be"),
                    U3 = T1.intro(2, U).extro(2, U),
                    U4 = U3.part(1, 3, new Texture<>("is"));

                boolean E2 = U1.equals(U2) &&
                             U3.equals(new Texture<>(S, "or", "not", S)) &&
                             U4.equals(new Texture<>(S, "is", S));

                System.out.println("Zugriff schreibend: "+E2+"\r\n");
            }

            // Grundoperationen (Arithmetik)
            {
                Script
                    S = new Script("knock");
                Texture<Script>
                    T0 = Texture.empty(),
                    T1 = new Texture<Script>(S),
                    T2 = T1.add(T1).mul(2),     // vier Mal
                    T3 = T2.sub(T1).sub(T1).div(2);

                boolean E1 = T0.isEmpty() && T3.equals(T1);

                System.out.println("Operationen 1: "+E1);

                Texture<Object>
                    U = new Texture<>(null, 1, '2', "3"),
                    U1 = U.add(U), U2 = U.add((Object)U);   // Achtung Unterschied: flach verkettet vs. verschachtelt
                int HC1 = U1.hashCode(), HC2 = U2.hashCode();

                boolean E2 = U1.equals(U.mul(2)) &&
                             U2.equals(new Texture<>(null, 1, '2', "3", U)) &&
                             HC1 != HC2;                    // U in sich selbst einfügbar

                System.out.println("Operationen 2: "+E2+"\r\n");

                List<Object> L = U.toList();    // selbe Elemente wie oben in U
                L.add(L);                       // L als Element in sich selbst einfügbar
                //int HC = L.hashCode();        // ergibt aber Endlos-Rekursion!
            }

            // Vergleiche
            {
                Texture<Character>
                    T1 = new Texture<>('A', 'B'),
                    T2 = new Texture<>('A', 'B', 'C');

                boolean E = T1.equals(T2);
                int C = T1.compareTo(T2);
                double
                    D1 = T1.similares(T2, Legacy.Similitude.Cosine),
                    D2 = T1.similares(T2, Legacy.Similitude.Levenshtein);

                System.out.println("Gleichheit: "+E);       // = oder !=
                System.out.println("Vergleichung: "+C);     // <, >, ≤, ≥, == oder !=
                System.out.println("Ähnlichkeit: "+D1+", "+D2); // ==, != oder ≈

                Texture<Script>
                    T3 = new Texture<>(new Script("hannah sees a racecar").split()),
                    T4 = new Texture<>(new Script("discounter introduces reductions").split());

                boolean
                    F1 = T3.verifyAll(Texture::isReverse),  // testet alle Scripts per Prädikat einzeln auf Palingramm
                    F2 = T4.map(Script::sort).tally().size() == 1;  // testet alle Scripts auf Anagramme untereinander

                System.out.println("Palingramm/Anagramm: "+F1+", "+F2+"\r\n");

                // Todo Fragen:
                // * Wie hängen Gleichheit, Vergleichung (Komparation) und
                //   Ähnlichkeit zusammen?
                // * Wie funktioniert die Anagramm-Berechnung für T4?
            }

            // Statistik
            {
                Texture<Double>
                    T1 = new Texture<>(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
                Texture<Character>
                    T2 = new Texture<>('B', 'A', 'N', 'A', 'N', 'A');

                double
                    Min = T1.min(), Max = T1.max(),
                    Mode = T1.mode(), Medi = T1.median(),
                    Medi1 = T1.median(false), Medi2 = T1.median(true),
                    Quant1 = T1.quantile(0.25), Quant2 = T1.quantile(0.75),
                    Mean = T1.mean(), Iqrange = T1.iqrange(), Vari = T1.variance();

                boolean E1 = Min == 1.0 && Max == 6.0 && T1.contains(Mode) &&
                             Medi == 3.5 && Medi1 == 3.0 && Medi2 == 4.0 &&
                             Quant1 == 2.0 && Quant2 == 5.0 &&
                             Mean == 3.5 && Iqrange == 3.0 &&
                             Vari == 2.9166666666666665;

                System.out.println("Statistik 1: "+E1);

                int N1 = T1.count(1.0), N2 = T2.count('A'),
                    N3 = T1.count(T1.intro(2)), N4 = T2.count(T2.extro(2));
                Map<?, Integer>
                    MT1 = T1.tally(), MT2 = T2.tally();
                SortedMap<Integer, List<Double>>
                    MC1 = T1.chart(false);  // Chart ist sortierte invertierte Tally
                SortedMap<Integer, List<Character>>
                    MC2 = T2.chart(true);   // absteigend (häufig nach selten)
                Map<?, List<Integer>>
                    MX1 = T1.index(), MX2 = T2.index();

                boolean E2 = N1 == 1 && N2 == 3 && N3 == 1 && N4 == 2 &&
                             MT1.equals(Map.of(1.0, 1, 2.0, 1, 3.0, 1,
                                              4.0, 1, 5.0, 1, 6.0, 1)) &&
                             MT2.equals(Map.of('B', 1, 'N', 2, 'A', 3)) &&
                             MC1.equals(Map.of(1, List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0))) &&
                             MC2.equals(Map.of(3, List.of('A'), 2, List.of('N'), 1, List.of('B'))) &&
                             MX1.equals(Map.of(1.0, List.of(0), 2.0, List.of(1), 3.0, List.of(2),
                                               4.0, List.of(3), 5.0, List.of(4), 6.0, List.of(5))) &&
                             MX2.equals(Map.of('A', List.of(1, 3, 5), 'N', List.of(2, 4), 'B', List.of(0)));

                System.out.println("Statistik 2: "+E2+"\r\n");
            }

            // Multimenge
            {
                Texture<Character>
                    T1 = new Texture<>('A', 'B', 'C'),
                    T2 = new Texture<>('B', 'B', 'C', 'C', 'C', 'D', 'D', 'D', 'D'),
                    TI = T1.intersect(T2), TU = T1.unite(T2),
                    TD = T2.differ(T1), TS = T2.sum(T1);
                int Cardi = T2.extent(),        // Kardinalität einer Menge
                    Multi = T2.count('D');  // Multiplizität eines Elements

                boolean E1 = TI.isShuffled(new Texture<>('B', 'C')) &&    // Anordnung undefiniert, daher wie
                             TU.isShuffled(new Texture<>('A').add(T2)) &&   // Anagramme auf Gleichheit testen
                             TD.isShuffled(new Texture<>('B', 'C', 'C', 'D', 'D', 'D', 'D')) &&
                             TS.isShuffled(T1.add(T2)) &&
                             Cardi == 9 && Multi == 4;

                System.out.println("Multimenge: "+E1);

                Texture<Character>          // Anagramme
                    T3 = new Texture<>(new Script("a gentleman")
                                            .filter(Charact::isLetter).toCharList()),
                    T4 = new Texture<>(new Script("elegant man")
                                           .filter(Charact::isLetter).toCharList()),
                    T5 = new Texture<>(new Script("eurovision song contest")
                                           .filter(Charact::isLetter).toCharList()),
                    T6 = new Texture<>(new Script("i vote on cretinous songs")
                                           .filter(Charact::isLetter).toCharList());

                boolean E2 = T3.isShuffled(T4) && T5.isShuffled(T6);    // Vergleich ohne Reihenfolge mit Elementanzahl

                System.out.println("Anagramme: "+E2+"\r\n");

                // Todo Aufgaben:
                // * Welche arithmetischen/statistischen Operationen auf den Element-Anzahlen
                //   verbergen sich hinter Multimengen-Intersektion/Union/Differenz/Summe?
                // * Stellen Sie fest, ob "TOKYO" und "KYOTO" Rotationen voneinander sind
                //   (Sonderform von Anagramm, aber nicht per isShuffled testen)
            }

            // Ableitung einer eigenen Texture-Klasse
            {
                class Data extends Texture<Number>  // nummerische Datenklasse mit
                {                                   // Typsicherheit bzgl. Elementen
                    public Data(Number... Numbs)    // Konstruktoren nicht erbbar und immer
                    { super(Numbs); }               // klassenspezifisch neu zu definieren

                    // hier beliebig neue Methoden gegenüber Texture hinzufügbar (analog zu Script)
                }

                Data
                    D1 = new Data(1, 2, 3, 4),
                    D2 = new Data(Math.PI, Math.E);

                Number      // alle Datenverarbeitungsmethoden von Texture verfügbar (analog zu Script)
                    Medi = D1.median(), Mean = D1.mean(),   // insbesondere gelingen alle nummerischen
                    IQRange = D2.iqrange(), Vari = D2.variance();   // Berechnungen, da Elemente immer Zahlen

                boolean E = Medi.doubleValue() == 2.5 &&
                            Mean.doubleValue() == 2.5 &&
                            IQRange.doubleValue() == 0.423310825130748 &&   // PI-E
                            Vari.doubleValue() == 0.044798013668218686;

                System.out.println("Datenklasse: "+E+"\r\n");
            }
        }

        // ### Tupel ###
        {
            /*
            Konzept der Tupel-Klasse(n):
            * Tupel sind (mathematische) Datenklassen zur Gruppierung zweier oder
              mehrerer Dateneinheiten verschiedener Datentypen (hier 2 bis 4 Elemente)
            * Tupel sind ähnlich wie Java-Records oder -Klassen verwendbar, müssen
              jedoch nicht vorab definiert werden (Records/Klassen sind letztlich
              ebenfalls Datentupel mit zugehörigen Verarbeitungsmethoden)
            * Da Tupel nicht definiert werden müssen, besitzen sie generische
              Zugriffsmethoden auf die gespeicherten Daten: it0() bzw. key() bis
              it3() bzw. val()/val3()
            * 2-Tupel sind Datenpaare, 3-Tupel Datentripel, 4-Tupel Datenquadrupel:
              Das 0. Element kann auch als Key, die restlichen Elemente als Values
              interpretiert werden (die Interpretation ist nicht vorgegeben)
            * Tupel sind für jede Dateneinheit per <T> typisierbar und können zu
              einem spezifischen Tupel-Untertyp abgeleitet werden (im Gegensatz
              zu Java-Records, die eigentlich auch als Datenklassen gedacht sind)
            * Tupel sind zur besseren Gruppierung beliebig ineinander schachtelbar
              und zur besseren Parallelisierbarkeit wiederum immutabel
            * Sinnvoll einsetzbar sind Tupel v.a. dann, wenn aus einer Funktion
              mehrere zusammengehörige Datenelemente zurückgegeben werden sollen
            */

            Couple<String, Character>
                Coup1 = new Couple<>("Sagittarius", '♐'),	// U+2650
                Coup2 = Tuple.of("Sagittarius", '♐');		// Kurzform
            Triple<String, Character, Integer>
                Trip1 = new Triple<>("Sagittarius", '♐', 0x2650),
                Trip2 = Tuple.of(Trip1);
            Quaple<String, Character, Integer, Boolean>     // (Polarität positiv/true)
                Quap1 = new Quaple<>("Sagittarius", '♐', 0x2650, true),
                Quap2 = Tuple.of(Quap1);

            boolean E1 = Coup1.equals(Coup2) && Trip1.equals(Trip2) &&
                         Coup1.key().equals(Coup1.it0()) &&
                         Coup1.val().equals(Coup1.it1()) &&
                         Quap1.it0().equals(Quap2.it0()) &&
                         Quap1.it1().equals(Quap2.it1()) &&
                         Quap1.it2().equals(Quap2.it2()) &&
                         Quap1.it3() && Quap2.it3();

            System.out.println("Tupel 1: "+E1);

            var Coup = Map.entry("Sagittarius", '♐');   // kompatibel mit Map.Entry
            var Quap = new Quaple<>("Sagittarius", '♐', 0x2650, true);  // Typinferenz

            Couple<Couple<String, Character>, Couple<Integer, Boolean>>  // gleiche Daten wie oben
                Cpls = new Couple<>(new Couple<>("Sagittarius", '♐'),   // als verschachtelte/
                                    new Couple<>(0x2650, true));         // gruppierte Datenpaare

            class Pixel extends Couple<Integer, Integer>     // spezifische Ableitung
            { public Pixel(int X, int Y) { super(X, Y); } }  // mit fixierten Typen
            Pixel Pixl = new Pixel(0, 0);               // automatisch Integer-Paar

            boolean E2 = Coup.equals(Coup1) && Quap.equals(Quap1) &&
                         Cpls.it0().equals(new Couple<>(Quap1.it0(), Quap1.it1())) &&
                         Cpls.it1().equals(new Couple<>(Quap1.it2(), Quap1.it3())) &&
                         Pixl.it0() == 0 && Pixl.it1() == 0;

            System.out.println("Tupel 2: "+E2+"\r\n");

            // Todo Fragen/Aufgaben:
            // * Was haben Tupel mit SQL-Tabellen gemeinsam?
            // * Wie sieht eine Texture aus, die z.B. nur Couples enthalten soll?
            // * Erzeugen Sie eine verschachtelte Tupel-Struktur für einen Adress-
            //   Datensatz aus Vor-/Nachname, Straße/Hausnummer, PLZ/Ort (6 Einheiten):
            //   Welche Daten sind wie zu gruppieren? Welche Tupel ergeben sich?
        }
    }
}
