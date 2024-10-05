import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class QuizFileReader {

    private static final String USERS_FILE = "./src/main/resources/users.json";
    private static final String QUIZ_FILE = "./src/main/resources/quiz.json";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            JSONArray usersArray = readJSONFile(USERS_FILE);

            System.out.println("System:> Enter your username");
            String username = scanner.nextLine();
            System.out.println("System:> Enter password");
            String password = scanner.nextLine();

            JSONObject authenticatedUser = authenticateUser(usersArray, username, password);

            if (authenticatedUser != null) {
                String role = (String) authenticatedUser.get("role");

                if (role.equals("admin")) {
                    handleAdminActions(scanner);
                } else if (role.equals("student")) {
                    handleStudentQuiz(scanner);
                }
            } else {
                System.out.println("System:> Invalid username or password.");
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    public static JSONArray readJSONFile(String filePath) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader(filePath);
        return (JSONArray) jsonParser.parse(reader);
    }

    public static JSONObject authenticateUser(JSONArray usersArray, String username, String password) {
        for (Object userObj : usersArray) {
            JSONObject user = (JSONObject) userObj;
            String storedUsername = (String) user.get("username");
            String storedPassword = (String) user.get("password");

            if (storedUsername.equals(username) && storedPassword.equals(password)) {
                return user;
            }
        }
        return null;
    }

    public static void handleAdminActions(Scanner scanner) throws IOException, ParseException {
        JSONArray quizArray = readQuizFile();
        String choice = "s";

        while (!choice.equalsIgnoreCase("q")) {
            System.out.println("System:> Input your question");
            String question = scanner.nextLine();

            System.out.println("System: Input option 1:");
            String option1 = scanner.nextLine();

            System.out.println("System: Input option 2:");
            String option2 = scanner.nextLine();

            System.out.println("System: Input option 3:");
            String option3 = scanner.nextLine();

            System.out.println("System: Input option 4:");
            String option4 = scanner.nextLine();

            System.out.println("System: What is the answer key?");
            int answerKey = scanner.nextInt();
            scanner.nextLine();

            JSONObject newQuestion = new JSONObject();
            newQuestion.put("question", question);
            newQuestion.put("option 1", option1);
            newQuestion.put("option 2", option2);
            newQuestion.put("option 3", option3);
            newQuestion.put("option 4", option4);
            newQuestion.put("answerkey", answerKey);

            quizArray.add(newQuestion);

            saveQuizFile(quizArray);

            System.out.println("System:> Saved successfully! Do you want to add more questions? (press s for start and q for quit)");
            choice = scanner.nextLine();
        }
    }

    public static void handleStudentQuiz(Scanner scanner) throws IOException, ParseException {
        JSONArray quizArray = readQuizFile();
        if (quizArray.isEmpty()) {
            System.out.println("System:> Quiz bank is empty. Please ask an admin to add questions.");
            return;
        }

        System.out.println("System:> Welcome to the quiz! We will throw you 10 questions. Each MCQ mark is 1 and no negative marking.");
        System.out.println("Are you ready? Press 's' to start.");
        String startQuiz = scanner.nextLine();
        if (!startQuiz.equalsIgnoreCase("s")) {
            return;
        }

        int score = 0;
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            int randomIndex = random.nextInt(quizArray.size());
            JSONObject question = (JSONObject) quizArray.get(randomIndex);

            System.out.println("[Question " + (i + 1) + "] " + question.get("question"));
            System.out.println("1. " + question.get("option 1"));
            System.out.println("2. " + question.get("option 2"));
            System.out.println("3. " + question.get("option 3"));
            System.out.println("4. " + question.get("option 4"));

            System.out.print("Your answer: ");
            int userAnswer = scanner.nextInt();

            int correctAnswer = ((Long) question.get("answerkey")).intValue();
            if (userAnswer == correctAnswer) {
                score++;
            }
        }

        displayResult(score);

        scanner.nextLine();
        System.out.println("Would you like to start again? Press 's' for start or 'q' for quit.");
        String choice = scanner.nextLine();
        if (choice.equalsIgnoreCase("s")) {
            handleStudentQuiz(scanner);
        }
    }

    public static JSONArray readQuizFile() throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader(QUIZ_FILE);
        return (JSONArray) jsonParser.parse(reader);
    }

    public static void displayResult(int score) {
        System.out.println("You have got " + score + " out of 10.");
        if (score >= 8) {
            System.out.println("Excellent!");
        } else if (score >= 5) {
            System.out.println("Good.");
        } else if (score >= 2) {
            System.out.println("Very poor!");
        } else {
            System.out.println("Very sorry, you are failed.");
        }
    }

    public static void saveQuizFile(JSONArray quizArray) throws IOException {
        try (FileWriter file = new FileWriter(QUIZ_FILE)) {
            file.write(quizArray.toJSONString());
            file.flush();
        }
    }
}
