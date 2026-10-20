package sn.smtg.questionservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sn.smtg.questionservice.entity.Question;
import sn.smtg.questionservice.repository.QuestionRepo;

import java.util.List;

/** Charge des questions de démonstration au premier démarrage (table vide). */
@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner seedQuestions(QuestionRepo repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.saveAll(List.of(
                q("Quel mot-clé Java empêche l'héritage d'une classe ?", "final", "static", "abstract", "enum", "final", "facile", "Java"),
                q("Quelle interface représente une liste ordonnée ?", "Set", "List", "Map", "Queue", "List", "facile", "Java"),
                q("Quelle annotation marque un point d'entrée Spring Boot ?", "@Service", "@Entity", "@SpringBootApplication", "@Bean", "@SpringBootApplication", "facile", "Java"),
                q("Quelle est la taille d'un int en Java ?", "16 bits", "32 bits", "64 bits", "8 bits", "32 bits", "facile", "Java"),
                q("Quel mot-clé crée une nouvelle instance ?", "this", "new", "class", "make", "new", "facile", "Java"),
                q("Quelle collection n'accepte pas de doublons ?", "List", "ArrayList", "Set", "LinkedList", "Set", "moyen", "Java"),
                q("Que renvoie 10 / 3 en Java (int) ?", "3.33", "3", "4", "2", "3", "moyen", "Java"),
                q("Quelle classe est immuable ?", "StringBuilder", "String", "ArrayList", "Date", "String", "moyen", "Java"),
                q("Quelle annotation JPA définit la clé primaire ?", "@Key", "@Id", "@Primary", "@PK", "@Id", "facile", "Java"),
                q("Quel outil de build utilise un pom.xml ?", "Gradle", "Maven", "Ant", "Make", "Maven", "facile", "Java"),
                q("Quel mot-clé lève une exception ?", "throws", "throw", "raise", "catch", "throw", "moyen", "Java"),
                q("Quel type est le parent de toutes les classes ?", "Class", "Base", "Object", "Root", "Object", "facile", "Java"),
                q("Quel mot-clé définit une fonction en Python ?", "func", "def", "function", "lambda", "def", "facile", "Python"),
                q("Quelle structure est immuable en Python ?", "list", "dict", "tuple", "set", "tuple", "moyen", "Python"),
                q("Que renvoie len('abc') ?", "2", "3", "4", "abc", "3", "facile", "Python"),
                q("Quel symbole commente une ligne en Python ?", "//", "#", "--", "/*", "#", "facile", "Python"),
                q("Quel module permet de manipuler du JSON ?", "json", "jsn", "simplejs", "parse", "json", "facile", "Python"),
                q("Quel mot-clé gère les exceptions ?", "catch", "except", "rescue", "handle", "except", "moyen", "Python"),
                q("Que vaut 2 ** 3 en Python ?", "6", "8", "9", "5", "8", "facile", "Python"),
                q("Quel gestionnaire de paquets est standard ?", "npm", "pip", "gem", "cargo", "pip", "facile", "Python")
            ));
            System.out.println(">>> " + repo.count() + " questions de démonstration insérées");
        };
    }

    private static Question q(String titre, String o1, String o2, String o3, String o4,
                              String bonne, String difficulte, String categorie) {
        return new Question(null, titre, o1, o2, o3, o4, bonne, difficulte, categorie);
    }
}
