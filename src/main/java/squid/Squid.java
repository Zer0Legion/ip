package squid;

import java.util.Objects;
import java.util.Scanner;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import squid.constants.CorrectUsage;
import squid.constants.Exceptions;
import squid.constants.Messages;
import squid.constants.Regex;
import squid.exceptions.DuplicateTaskNameException;
import squid.exceptions.IncorrectIndexException;
import squid.exceptions.IncorrectInputException;
import squid.exceptions.NotEnoughDatesException;
import squid.exceptions.NotEnoughInputsException;
import squid.exceptions.ParseFailException;
import squid.exceptions.SquidDateException;
import squid.exceptions.SquidException;
import squid.tasks.DateTime;
import squid.tasks.Deadline;
import squid.tasks.Event;
import squid.tasks.Task;
import squid.tasks.Tasks;
import squid.tasks.Todo;
import squid.ui.DialogBox;

/**
 * The main class of Squid.
 */
public class Squid {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;
    private Scene scene;
    private Image imageUser = new Image(
            Objects.requireNonNull(this.getClass().getResourceAsStream("/images/DaUser.png")));
    private Image imageDuke = new Image(
            Objects.requireNonNull(this.getClass().getResourceAsStream("/images/DaDuke.png")));

    public Squid() {
        new Tasks();
    }

    /**
     * List all existing tasks.
     *
     * @throws NotEnoughInputsException If there are not enough parameters.
     * @throws IncorrectIndexException Should never happen.
     */
    private static String list() throws NotEnoughInputsException, IncorrectIndexException {
        StringBuilder res = new StringBuilder();
        res.append(echo(Messages.LIST));
        res.append(Tasks.list());
        return res.toString();
    }

    /**
     * Initializes Squid.
     */
    private static String hello() {
        try {
            Tasks.read();
        } catch (ParseFailException | DuplicateTaskNameException | SquidDateException e) {
            echo(e.toString());
        }
        return Messages.LINE_BREAK + "\n" + Messages.HELLO + "\n" + Messages.LINE_BREAK;
    }

    /**
     * Terminates the program.
     *
     * @throws NotEnoughInputsException Should never happen, unless constant MESSAGES.BYE is blank.
     */
    private static String bye() throws NotEnoughInputsException {
        Tasks.save();
        return Messages.BYE;
    }

    /**
     * Overloaded method to include this as a valid command.
     *
     * @param message The raw input from the user, or message to be printed.
     * @param isFromUser Whether to further process message.
     * @throws NotEnoughInputsException If there are not enough parameters.
     */
    private static String echo(String message, boolean isFromUser) throws NotEnoughInputsException {
        if (!isFromUser) {
            return echo(message);
        }
        String[] params = message.split(" ", 2);
        if (params.length <= 1) {
            throw new NotEnoughInputsException(
                    String.format(
                            Exceptions.NOT_ENOUGH_INPUTS, "echo", CorrectUsage.ECHO));
        }
        return echo(params[1]);
    }

    /**
     * Prints a message with a custom header.
     *
     * @param message The message to be printed.
     */
    private static String echo(String message) {
        return Messages.ECHO + message;
    }

    /**
     * Handles the creation of a todo task.
     *
     * @param message The user's raw input.
     * @throws NotEnoughInputsException If there are not enough parameters.
     * @throws DuplicateTaskNameException If there is an existing task with the same name.
     */
    private static String todo(String message) throws NotEnoughInputsException, DuplicateTaskNameException {
        String[] params = message.split(" ", 2);
        if (params.length <= 1) {
            throw new NotEnoughInputsException(
                    String.format(
                            Exceptions.NOT_ENOUGH_INPUTS, "todo", CorrectUsage.TODO));

        }

        Task t = new Todo(params[1]);
        Tasks.add(t);
        return echo(String.format(Messages.TODO, t));
    }

    /**
     * Handles the creation of a deadline task.
     *
     * @param message The user's raw input.
     * @throws NotEnoughInputsException If there are not enough parameters.
     * @throws DuplicateTaskNameException If there is an existing task with the same name.
     * @throws SquidDateException If the date given is unable to be parsed.
     */
    private static String deadline(String message) throws
            NotEnoughInputsException,
            DuplicateTaskNameException,
            SquidDateException {
        String[] params = message.split(" ", 2);
        if (params.length <= 1) {
            throw new NotEnoughInputsException(
                    String.format(
                            Exceptions.NOT_ENOUGH_INPUTS,
                            "deadline",
                            CorrectUsage.DEADLINE));
        }
        String[] arguments = params[1].split(Regex.DEADLINE);

        if (arguments.length == 1) {
            throw new NotEnoughDatesException(
                    String.format(
                            Exceptions.NOT_ENOUGH_DATES,
                            1,
                            "deadline",
                            CorrectUsage.DEADLINE));
        }
        String task = arguments[0];
        DateTime dateTime = new DateTime(arguments[1]);
        Task t = new Deadline(task, dateTime);
        Tasks.add(t);
        return echo(String.format(Messages.DEADLINE, t));
    }

    /**
     * Handles the creation of an event task.
     *
     * @param message The user's raw input.
     * @throws NotEnoughInputsException If there are not enough parameters.
     * @throws DuplicateTaskNameException If there is an existing task with the same name.
     * @throws SquidDateException If the date given is unable to be parsed.
     */
    private static String event(String message) throws
            NotEnoughInputsException,
            DuplicateTaskNameException,
            SquidDateException {
        String[] params = message.split(" ", 2);
        if (params.length <= 1) {
            throw new NotEnoughInputsException(
                    String.format(
                            Exceptions.NOT_ENOUGH_INPUTS,
                            "event",
                            CorrectUsage.EVENT));
        }
        params = params[1].split(Regex.EVENT_FROM);
        if (params.length != 2 || params[1].split(Regex.EVENT_TO).length != 2) {
            throw new NotEnoughDatesException(
                    String.format(
                            Exceptions.NOT_ENOUGH_DATES,
                            2,
                            "event",
                            CorrectUsage.EVENT));
        }
        String[] dates = params[1].split(Regex.EVENT_TO);
        DateTime from = new DateTime(dates[0]);
        DateTime to = new DateTime(dates[1]);
        Task t = new Event(params[0], from, to);
        Tasks.add(t);
        return echo(String.format(Messages.EVENT, t));
    }


    /**
     * Mark a task as either complete or incomplete.
     *
     * @param input The user's raw input.
     * @param isCompleted Whether to mark it completed or incomplete.
     * @throws NotEnoughInputsException If there are not enough parameters.
     * @throws IncorrectIndexException If the index does not refer to a valid task.
     */
    private static String mark(String input, boolean isCompleted) throws
            NotEnoughInputsException,
            IncorrectIndexException {
        String[] params = input.split(" ", 2);
        if (params.length <= 1) {
            throw new NotEnoughInputsException(
                    String.format(
                            Exceptions.NOT_ENOUGH_INPUTS,
                            isCompleted ? "mark" : "unmark",
                            CorrectUsage.mark(isCompleted)));
        }
        String task = params[1];
        // Find the task entry.
        Task found = null;
        for (int i = 0; i < Tasks.size(); i++) {
            if (Tasks.get(i).getTaskName().equals(task)) {
                found = Tasks.get(i);
            }
        }
        if (found != null) {
            if (found.isCompleted() == isCompleted) {
                return echo(String.format(Messages.MARK_REPEAT, isCompleted ? "" : "un"));
            } else {
                found.setCompleted(isCompleted);
                return echo(String.format(isCompleted
                        ? Messages.MARK_COMPLETE
                        : Messages.MARK_INCOMPLETE, found));
            }


        } else {
            return echo(Messages.MARK_NOT_FOUND);
        }
    }

    /**
     * Delete an existing task based on its index.
     *
     * @param input The user's raw input.
     * @throws NotEnoughInputsException If there are not enough parameters.
     * @throws IncorrectIndexException If the index does not refer to a valid task.
     */
    private static String delete(String input) throws NotEnoughInputsException, IncorrectIndexException {
        String[] params = input.split(" ", 2);
        if (params.length == 1) {
            throw new NotEnoughInputsException(
                    String.format(Exceptions.NOT_ENOUGH_INPUTS,
                            "delete",
                            CorrectUsage.DELETE));
        }
        Task deleted = Tasks.delete(params[1]);
        return echo(String.format(Messages.DELETE, params[1], deleted)) + "\n" + Tasks.list();
    }



    private static String find(String input) throws NotEnoughInputsException {
        String[] params = input.split(" ", 2);
        if (params.length == 1) {
            throw new NotEnoughInputsException(
                    String.format(Exceptions.NOT_ENOUGH_INPUTS, "find", CorrectUsage.FIND));
        }
        String keyword = params[1];
        return echo(Messages.FIND) + "\n" + Tasks.find(keyword);
    }

    /**
     * Adapted from <a href="https://se-education.org/guides/tutorials/javaFxPart1.html">SE-EDU</a>
     * @param stage the primary stage for this application, onto which the application scene can be set.
     */
//    @Override
//    public void start(Stage stage) {
//        //Step 1. Setting up required components
//        stage.setX(600);
//        stage.setY(50);
//
//        //The container for the content of the chat to scroll.
//        scrollPane = new ScrollPane();
//        dialogContainer = new VBox();
//        scrollPane.setContent(dialogContainer);
//
//        userInput = new TextField();
//        sendButton = new Button("Send");
//
//        AnchorPane mainLayout = new AnchorPane();
//        mainLayout.getChildren().addAll(scrollPane, userInput, sendButton);
//
//        scene = new Scene(mainLayout);
//
//        stage.setScene(scene);
//        stage.show();
//
//        //Step 2. Formatting the window to look as expected
//        stage.setTitle("Squid");
//        stage.setResizable(false);
//        stage.setMinHeight(900);
//        stage.setMinWidth(600);
//
//        float width = 600;
//        float sendButtonWidth = 100;
//
//        mainLayout.setPrefSize(width, 900);
//
//        scrollPane.setPrefSize(width - scrollPane.getWidth(), stage.getHeight() - 100);
//        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
//
//        scrollPane.setVvalue(1.0);
//        scrollPane.setFitToWidth(true);
//
//        //You will need to import `javafx.scene.layout.Region` for this.
//        dialogContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
//
//        userInput.setPrefWidth(width - sendButtonWidth - 25);
//
//        sendButton.setPrefWidth(sendButtonWidth);
//
//        AnchorPane.setTopAnchor(scrollPane, 1.0);
//
//        AnchorPane.setBottomAnchor(sendButton, 1.0);
//        AnchorPane.setRightAnchor(sendButton, 1.0);
//
//        AnchorPane.setLeftAnchor(userInput , 1.0);
//        AnchorPane.setBottomAnchor(userInput, 1.0);
//
//        //Step 3. Add functionality to handle user input.
//
//        dialogContainer.getChildren().addAll(DialogBox.getDukeDialog(new Label(hello()), new ImageView(imageDuke)));
//
//        sendButton.setOnMouseClicked((event) -> {
//            try {
//                handleUserInput();
//            } catch (InterruptedException e) {
//                e = e;
//            }
//        });
//
//        userInput.setOnAction((event) -> {
//            try {
//                handleUserInput();
//            } catch (InterruptedException e) {
//                e = e;
//            }
//        });
//
//        //Scroll down to the end every time dialogContainer's height changes.
//        dialogContainer.heightProperty().addListener((observable) -> scrollPane.setVvalue(1.0));
//    }

    /**
     * Iteration 1:
     * Creates a label with the specified text and adds it to the dialog container.
     * Adapted from <a href="https://se-education.org/guides/tutorials/javaFxPart3.html">SE-EDU</a>
     * @param text String containing text to add
     * @return a label with the specified text that has word wrap enabled.
     */
    private Label getDialogLabel(String text) {
        // You will need to import `javafx.scene.control.Label`.
        Label textToAdd = new Label(text);
        textToAdd.setWrapText(true);

        return textToAdd;
    }

    /**
     * Iteration 2:
     * Creates two dialog boxes, one echoing user input and the other containing Duke's reply and then appends them to
     * the dialog container. Clears the user input after processing.
    */
//    private void handleUserInput() throws InterruptedException {
//
//        String input = userInput.getText();
//        Response response = parseInput(true, input);
//        Label userText = new Label(input);
//        Label dukeText = new Label(response.getResponse());
//        dialogContainer.getChildren().addAll(
//                DialogBox.getUserDialog(userText, new ImageView(imageUser)),
//                DialogBox.getDukeDialog(dukeText, new ImageView(imageDuke))
//        );
//        Tasks.save();
//        userInput.clear();
//    }

    /**
     * Parse the user's input and assigns them to separate helper functions depending on command
     *
     * @param isLoop Condition whether to terminate loop.
     * @param input The user's input.
     * @return Whether the loop should continue (Usually true unless "bye" command is given).
     * @throws SquidException General exception thrown by Squid.
     */
    public static Response parseInput(boolean isLoop, String input) {
        System.out.println(Messages.LINE_BREAK);
        String[] messages = input.split(" ", 2);
        String command = messages[0];
        String res = "";

        try {
            switch (command) {
            case ("bye"):
                isLoop = false;
                res = bye();
                break;
            case ("echo"):
                res = echo(input, true);
                break;
            case ("list"):
                res = list();
                break;
            case ("mark"):
                res = mark(input, true);
                break;
            case ("unmark"):
                res = mark(input, false);
                break;
            case ("todo"):
                res = todo(input);
                break;
            case ("deadline"):
                res = deadline(input);
                break;
            case ("event"):
                res = event(input);
                break;
            case ("delete"):
                res = delete(input);
                break;
            case ("save"):
                Tasks.save();
                res = echo(Messages.SAVE);
                break;
            case ("find"):
                res = find(input);
                break;
            default:
                throw new IncorrectInputException(Exceptions.INCORRECT_INPUT);
            }
        } catch (SquidException e) {
            res = echo(e.getMessage());
        }
        res += "\n" + Messages.LINE_BREAK;
        res = "\n" + Messages.LINE_BREAK + res;
        return new Response(isLoop, res);
    }

    public static void main(String[] args) {
        new Squid();

        Scanner scanner = new Scanner(System.in);
        boolean isLoop = true;

        while (isLoop) {
            String input = scanner.nextLine().strip();
            Response response = parseInput(isLoop, input);
            isLoop = response.getIsLoop();
            Tasks.save();
        }
    }


}