package squid;

import java.util.Objects;
import java.util.Scanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
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

/**
 * The main class of Squid.
 */
public class Squid extends Application {

    private ScrollPane scrollPane;
    private VBox dialogContainer;
    private TextField userInput;
    private Button sendButton;
    private Scene scene;
    private Image user = new Image(
            Objects.requireNonNull(this.getClass().getResourceAsStream("/images/DaUser.png")));
    private Image duke = new Image(
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
    private static void list() throws NotEnoughInputsException, IncorrectIndexException {
        echo(Messages.LIST);
        Tasks.list();
    }

    /**
     * Initializes Squid.
     */
    private static void hello() {
        try {
            Tasks.read();
        } catch (ParseFailException | DuplicateTaskNameException | SquidDateException e) {
            echo(e.toString());
        }
        System.out.println(Messages.LINE_BREAK);
        echo(Messages.HELLO);
        System.out.println(Messages.LINE_BREAK);
    }

    /**
     * Terminates the program.
     *
     * @throws NotEnoughInputsException Should never happen, unless constant MESSAGES.BYE is blank.
     */
    private static void bye() throws NotEnoughInputsException {
        echo(Messages.BYE);
        Tasks.save();
    }

    /**
     * Overloaded method to include this as a valid command.
     *
     * @param message The raw input from the user, or message to be printed.
     * @param isFromUser Whether to further process message.
     * @throws NotEnoughInputsException If there are not enough parameters.
     */
    private static void echo(String message, boolean isFromUser) throws NotEnoughInputsException {
        if (!isFromUser) {
            echo(message);
            return;
        }
        String[] params = message.split(" ", 2);
        if (params.length <= 1) {
            throw new NotEnoughInputsException(
                    String.format(
                            Exceptions.NOT_ENOUGH_INPUTS, "echo", CorrectUsage.ECHO));
        }
        echo(params[1]);
    }

    /**
     * Prints a message with a custom header.
     *
     * @param message The message to be printed.
     */
    private static void echo(String message) {
        System.out.println(Messages.ECHO + message);
    }

    /**
     * Handles the creation of a todo task.
     *
     * @param message The user's raw input.
     * @throws NotEnoughInputsException If there are not enough parameters.
     * @throws DuplicateTaskNameException If there is an existing task with the same name.
     */
    private static void todo(String message) throws NotEnoughInputsException, DuplicateTaskNameException {
        String[] params = message.split(" ", 2);
        if (params.length <= 1) {
            throw new NotEnoughInputsException(
                    String.format(
                            Exceptions.NOT_ENOUGH_INPUTS, "todo", CorrectUsage.TODO));

        }

        Task t = new Todo(params[1]);
        Tasks.add(t);
        echo(String.format(Messages.TODO, t));
    }

    /**
     * Handles the creation of a deadline task.
     *
     * @param message The user's raw input.
     * @throws NotEnoughInputsException If there are not enough parameters.
     * @throws DuplicateTaskNameException If there is an existing task with the same name.
     * @throws SquidDateException If the date given is unable to be parsed.
     */
    private static void deadline(String message) throws
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
        echo(String.format(Messages.DEADLINE, t));
    }

    /**
     * Handles the creation of an event task.
     *
     * @param message The user's raw input.
     * @throws NotEnoughInputsException If there are not enough parameters.
     * @throws DuplicateTaskNameException If there is an existing task with the same name.
     * @throws SquidDateException If the date given is unable to be parsed.
     */
    private static void event(String message) throws
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
        echo(String.format(Messages.EVENT, t));
    }


    /**
     * Mark a task as either complete or incomplete.
     *
     * @param input The user's raw input.
     * @param isCompleted Whether to mark it completed or incomplete.
     * @throws NotEnoughInputsException If there are not enough parameters.
     * @throws IncorrectIndexException If the index does not refer to a valid task.
     */
    private static void mark(String input, boolean isCompleted) throws
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
                echo(String.format(Messages.MARK_REPEAT, isCompleted ? "" : "un"));
            } else {
                found.setCompleted(isCompleted);
                echo(String.format(isCompleted
                        ? Messages.MARK_COMPLETE
                        : Messages.MARK_INCOMPLETE, found));
            }


        } else {
            echo(Messages.MARK_NOT_FOUND);
        }
    }

    /**
     * Delete an existing task based on its index.
     *
     * @param input The user's raw input.
     * @throws NotEnoughInputsException If there are not enough parameters.
     * @throws IncorrectIndexException If the index does not refer to a valid task.
     */
    private static void delete(String input) throws NotEnoughInputsException, IncorrectIndexException {
        String[] params = input.split(" ", 2);
        if (params.length == 1) {
            throw new NotEnoughInputsException(
                    String.format(Exceptions.NOT_ENOUGH_INPUTS,
                            "delete",
                            CorrectUsage.DELETE));
        }
        Task deleted = Tasks.delete(params[1]);
        echo(String.format(Messages.DELETE, params[1], deleted));
        Tasks.list();
    }



    private static void find(String input) throws NotEnoughInputsException {
        String[] params = input.split(" ", 2);
        if (params.length == 1) {
            throw new NotEnoughInputsException(
                    String.format(Exceptions.NOT_ENOUGH_INPUTS, "find", CorrectUsage.FIND));
        }
        String keyword = params[1];
        echo(Messages.FIND);
        Tasks.find(keyword);
    }

    /**
     * Adapted from <a href="https://se-education.org/guides/tutorials/javaFxPart1.html">SE-EDU</a>
     * @param stage the primary stage for this application, onto which the application scene can be set.
     */
    @Override
    public void start(Stage stage) {
        //Step 1. Setting up required components
        stage.setX(600);
        stage.setY(50);

        //The container for the content of the chat to scroll.
        scrollPane = new ScrollPane();
        dialogContainer = new VBox();
        scrollPane.setContent(dialogContainer);

        userInput = new TextField();
        sendButton = new Button("Send");

        AnchorPane mainLayout = new AnchorPane();
        mainLayout.getChildren().addAll(scrollPane, userInput, sendButton);

        scene = new Scene(mainLayout);

        stage.setScene(scene);
        stage.show();

        //Step 2. Formatting the window to look as expected
        stage.setTitle("Squid");
        stage.setResizable(false);
        stage.setMinHeight(900);
        stage.setMinWidth(600);

        float width = 600;
        float sendButtonWidth = 100;

        mainLayout.setPrefSize(width, 900);

        scrollPane.setPrefSize(width - scrollPane.getWidth(), stage.getHeight() - 100);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        scrollPane.setVvalue(1.0);
        scrollPane.setFitToWidth(true);

        //You will need to import `javafx.scene.layout.Region` for this.
        dialogContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);

        userInput.setPrefWidth(width - sendButtonWidth - 25);

        sendButton.setPrefWidth(sendButtonWidth);

        AnchorPane.setTopAnchor(scrollPane, 1.0);

        AnchorPane.setBottomAnchor(sendButton, 1.0);
        AnchorPane.setRightAnchor(sendButton, 1.0);

        AnchorPane.setLeftAnchor(userInput , 1.0);
        AnchorPane.setBottomAnchor(userInput, 1.0);

        //Step 3. Add functionality to handle user input.
        sendButton.setOnMouseClicked((event) -> {
            dialogContainer.getChildren().add(getDialogLabel(userInput.getText()));
            userInput.clear();
        });

        userInput.setOnAction((event) -> {
            dialogContainer.getChildren().add(getDialogLabel(userInput.getText()));
            userInput.clear();
        });

        //Scroll down to the end every time dialogContainer's height changes.
        dialogContainer.heightProperty().addListener((observable) -> scrollPane.setVvalue(1.0));
    }

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
     * Parse the user's input and assigns them to separate helper functions depending on command
     *
     * @param loop Condition whether to terminate loop.
     * @param input The user's input.
     * @return Whether the loop should continue (Usually true unless "bye" command is given).
     * @throws SquidException General exception thrown by Squid.
     */
    private static boolean parseInput(boolean loop, String input) throws SquidException {
        System.out.println(Messages.LINE_BREAK);
        String[] messages = input.split(" ", 2);
        String command = messages[0];
        String params = "";
        if (messages.length > 1) {
            params = messages[1];
        }

        try {
            switch (command) {
            case ("bye"):
                loop = false;
                bye();
                break;
            case ("echo"):
                echo(input, true);
                break;
            case ("list"):
                list();
                break;
            case ("mark"):
                mark(input, true);
                break;
            case ("unmark"):
                mark(input, false);
                break;
            case ("todo"):
                todo(input);
                break;
            case ("deadline"):
                deadline(input);
                break;
            case ("event"):
                event(input);
                break;
            case ("delete"):
                delete(input);
                break;
            case ("save"):
                Tasks.save();
                echo(Messages.SAVE);
                break;
            case ("find"):
                find(input);
                break;
            default:
                throw new IncorrectInputException(Exceptions.INCORRECT_INPUT);
            }
        } catch (SquidException e) {
            echo(e.getMessage());
        }
        return loop;
    }

    public static void main(String[] args) throws SquidException {
        new Squid();
        hello();

        Scanner scanner = new Scanner(System.in);
        boolean loop = true;

        while (loop) {
            String input = scanner.nextLine().strip();
            loop = parseInput(loop, input);
            System.out.println(Messages.LINE_BREAK);
            Tasks.save();
        }

    }


}
