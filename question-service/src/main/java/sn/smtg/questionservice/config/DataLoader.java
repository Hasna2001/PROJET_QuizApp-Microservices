package sn.smtg.questionservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sn.smtg.questionservice.entity.Question;
import sn.smtg.questionservice.repository.QuestionRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Charge des questions de démonstration au premier démarrage (table vide). */
@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner seedQuestions(QuestionRepo repo) {
        return args -> {
            List<Question> javaQuestions = List.of(
                q("Quelle valeur affiche ce code : System.out.println(2 + 3 * 4) ?", "20", "14", "24", "9", "14", "facile", "Java"),
                q("Quelle méthode compare le contenu de deux chaînes de caractères ?", "equals()", "==", "compare()", "same()", "equals()", "facile", "Java"),
                q("Quelle annotation identifie la classe principale d'une application Spring Boot ?", "@Configuration", "@SpringBootApplication", "@RestController", "@Component", "@SpringBootApplication", "facile", "Java"),
                q("Quel type primitif permet de stocker vrai ou faux ?", "boolean", "byte", "char", "int", "boolean", "facile", "Java"),
                q("Quel mot-clé permet de créer un objet à partir d'une classe ?", "this", "new", "class", "static", "new", "facile", "Java"),
                q("Quelle interface de collection conserve l'ordre et accepte les doublons ?", "Set", "List", "Map", "Queue", "List", "moyen", "Java"),
                q("Quel est le résultat de 10 / 3 lorsque les deux nombres sont des int ?", "3,33", "3", "4", "2", "3", "moyen", "Java"),
                q("Quelle classe Java est immuable ?", "StringBuilder", "String", "ArrayList", "HashMap", "String", "moyen", "Java"),
                q("Quelle annotation JPA indique la clé primaire d'une entité ?", "@Key", "@Id", "@Primary", "@ColumnId", "@Id", "facile", "Java"),
                q("Quel outil de construction de projet utilise le fichier pom.xml ?", "Gradle", "Maven", "Ant", "Make", "Maven", "facile", "Java"),
                q("Quel bloc permet de traiter une exception levée dans un bloc try ?", "throw", "throws", "catch", "finally", "catch", "moyen", "Java"),
                q("De quelle classe toutes les classes Java héritent-elles directement ou indirectement ?", "Class", "Base", "Object", "Root", "Object", "facile", "Java")
            );
            List<Question> questions = new ArrayList<>(javaQuestions);
            questions.addAll(List.of(
                q("Quel mot-clé définit une fonction en Python ?", "func", "def", "function", "lambda", "def", "facile", "Python"),
                q("Quelle structure est immuable en Python ?", "list", "dict", "tuple", "set", "tuple", "moyen", "Python"),
                q("Que renvoie len('abc') ?", "2", "3", "4", "abc", "3", "facile", "Python"),
                q("Quel symbole commente une ligne en Python ?", "//", "#", "--", "/*", "#", "facile", "Python"),
                q("Quel module permet de manipuler du JSON ?", "json", "jsn", "simplejs", "parse", "json", "facile", "Python"),
                q("Quel mot-clé gère les exceptions ?", "catch", "except", "rescue", "handle", "except", "moyen", "Python"),
                q("Que vaut 2 ** 3 en Python ?", "6", "8", "9", "5", "8", "facile", "Python"),
                q("Quel gestionnaire de paquets est standard ?", "npm", "pip", "gem", "cargo", "pip", "facile", "Python")
            ));

            if (repo.count() == 0) {
                repo.saveAll(questions);
                System.out.println(">>> " + repo.count() + " questions de démonstration insérées");
                return;
            }

            Map<String, Question> questionsJavaPrecedentes = Map.ofEntries(
                Map.entry("Quel mot-clé Java empêche l'héritage d'une classe ?", javaQuestions.get(0)),
                Map.entry("Quelle interface représente une liste ordonnée ?", javaQuestions.get(1)),
                Map.entry("Quelle annotation marque un point d'entrée Spring Boot ?", javaQuestions.get(2)),
                Map.entry("Quelle est la taille d'un int en Java ?", javaQuestions.get(3)),
                Map.entry("Quel mot-clé crée une nouvelle instance ?", javaQuestions.get(4)),
                Map.entry("Quelle collection n'accepte pas de doublons ?", javaQuestions.get(5)),
                Map.entry("Que renvoie 10 / 3 en Java (int) ?", javaQuestions.get(6)),
                Map.entry("Quelle classe est immuable ?", javaQuestions.get(7)),
                Map.entry("Quelle annotation JPA définit la clé primaire ?", javaQuestions.get(8)),
                Map.entry("Quel outil de build utilise un pom.xml ?", javaQuestions.get(9)),
                Map.entry("Quel mot-clé lève une exception ?", javaQuestions.get(10)),
                Map.entry("Quel type est le parent de toutes les classes ?", javaQuestions.get(11))
            );

            List<Question> questionsModifiees = repo.findByCategorie("Java").stream()
                .filter(question -> questionsJavaPrecedentes.containsKey(question.getQuestionTitle()))
                .peek(question -> copierContenu(questionsJavaPrecedentes.get(question.getQuestionTitle()), question))
                .toList();
            if (!questionsModifiees.isEmpty()) {
                repo.saveAll(questionsModifiees);
                System.out.println(">>> " + questionsModifiees.size() + " questions Java de démonstration mises à jour");
            }
        };
    }

    private static void copierContenu(Question source, Question destination) {
        destination.setQuestionTitle(source.getQuestionTitle());
        destination.setOption1(source.getOption1());
        destination.setOption2(source.getOption2());
        destination.setOption3(source.getOption3());
        destination.setOption4(source.getOption4());
        destination.setReponseCorrecte(source.getReponseCorrecte());
        destination.setDifficulte(source.getDifficulte());
        destination.setCategorie(source.getCategorie());
    }

    private static Question q(String titre, String o1, String o2, String o3, String o4,
                              String bonne, String difficulte, String categorie) {
        return new Question(null, titre, o1, o2, o3, o4, bonne, difficulte, categorie);
    }
}
