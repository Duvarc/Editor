package editor;

import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.Group;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.ArrayList;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.shape.Rectangle;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseDragEvent;


public class Editor extends Application {

    private int WINDOW_WIDTH = 500;
    private int WINDOW_HEIGHT = 500;
    private FastLinkedList characters = new FastLinkedList();
    private FastLinkedList wordWrap = new FastLinkedList();
    private ArrayList<Node> lines = new ArrayList<Node>();
    private Rectangle cursor = new Rectangle(0, 0);
    private HistoryStack history = new HistoryStack();
    private ScrollBar scrollBar = new ScrollBar();

    private int currentY = 0;
    private int currentX = 5;
    private static boolean printYes = true;
    private static String print;
    int fontSize = 12;
    private String fontName = "Verdana";
    private int textHeight = Math.round((float) characters.getCurrentNode().getEntry().getLayoutBounds().getHeight());
    Group root;
    Group textRoot;
    private static String inputFilename = "";


    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
        /** A Text object that will be used to print the current mouse position. */
        Text positionText;

        MouseClickEventHandler(Group root) {

        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            // Because we registered this EventHandler using setOnMouseClicked, it will only called
            // with mouse events of type MouseEvent.MOUSE_CLICKED.  A mouse clicked event is
            // generated anytime the mouse is pressed and released on the same JavaFX node.
            double mousePressedX = mouseEvent.getX();
            double mousePressedY = mouseEvent.getY();
            Text sample = new Text("A");
            sample.setFont(Font.font(fontName, fontSize));
            textHeight = Math.round((float) sample.getLayoutBounds().getHeight());

            int index = Math.round((float) mousePressedY / textHeight);
            if (index < lines.size()) {
                Node x1 = lines.get(index);
                Node x2 = x1.next;

                while (x2.getEntry().getX() + x2.getEntry().getLayoutBounds().getWidth() < mousePressedX) {
                    x1 = x1.next;
                    x2 = x2.next;
                }

                if (mousePressedX - x1.getEntry().getX() <= mousePressedX - x2.getEntry().getX()) {
                    characters.setCurrentNode(x1);
                }
                else {
                    characters.setCurrentNode(x2);
                }

                updateCursor();
            }
        }
    }

    private class KeyEventHandler implements EventHandler<KeyEvent> {
        int textCenterX;
        int textCenterY;

        private static final int STARTING_FONT_SIZE = 12;
        private static final int STARTING_TEXT_POSITION_X = 0;
        private static final int STARTING_TEXT_POSITION_Y = 0;

        /**
         * The Text to display on the screen.
         */
        //private Text displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");

        String fontName = "Verdana";

        KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            textCenterX = windowWidth / 2;
            textCenterY = windowHeight / 2;

            // // Initialize some empty text and add it to root so that it will be displayed.
            // displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");
            // // Always set the text origin to be VPos.TOP! Setting the origin to be VPos.TOP means
            // // that when the text is assigned a y-position, that position corresponds to the
            // // highest position across all letters (for example, the top of a letter like "I", as
            // // opposed to the top of a letter like "e"), which makes calculating positions much
            // // simpler!
            // displayText.setTextOrigin(VPos.TOP);
            // displayText.setFont(Font.font(fontName, fontSize));

            // // All new Nodes need to be added to the root in order to be displayed.
            // root.getChildren().add(displayText);
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            KeyCode code = keyEvent.getCode();
            if (keyEvent.isShortcutDown()) {
                if (code == KeyCode.Z) {
                    undo();
                } else if (code == KeyCode.Y) {
                    redo();
                } else if (code == KeyCode.S) {
                    write();
                } else if (code == KeyCode.EQUALS) {
                    setFont(fontName, fontSize + 4);
                    render();
                } else if (code == KeyCode.MINUS) {
                    setFont(fontName, fontSize - 4);
                    render();
                } else if (code == KeyCode.P) {
                    System.out.println((int) cursor.getX() + ", " + (int) cursor.getY());
                }
            }
            else {
                if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                    // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                    // the KEY_TYPED event, javafx handles the "Shift" key and associated
                    // capitalization.
                    String characterTyped = keyEvent.getCharacter();
                    if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                        // Ignore control keys, which have non-zero length, as well as the backspace
                        // key, which is represented as a character of value = 8 on Windows.

                        if (characterTyped.charAt(0) == 13 || (int) characterTyped.charAt(0) == 10) {
                            int enterX = characters.getCurrentNodeX();
                            int enterY = characters.getCurrentNodeY();
                            Text enter = new Text("\n");
                            int enterWidth = Math.round((float) enter.getLayoutBounds().getWidth());
                            Text newEnter = new Text(5 - enterWidth, enterY + textHeight, "\n");
                            characters.addCurrent(newEnter);
                            history.put(characters.getCurrentNode());
                        }
                        else {
                            Text charX = new Text(characterTyped);
                            characters.addCurrent(charX);
                            history.put(characters.getCurrentNode());
                            textRoot.getChildren().add(charX);
                        }
                        render();
                        updateCursor();
                        history.clearRedoStack();
                        keyEvent.consume();
                    }

                    //centerText();
                } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                    // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                    // events have a code that we can check (KEY_TYPED events don't have an associated
                    // KeyCode).
                    if (code == KeyCode.UP) {
                        characters.goUp();
                        updateCursor();
                    } else if (code == KeyCode.DOWN) {
                        characters.goDown();
                        updateCursor();
                    } else if (code == KeyCode.LEFT) {
                        characters.goLeft();
                        updateCursor();
                    } else if (code == KeyCode.RIGHT) {
                        characters.goRight();
                        updateCursor();
                    } else if (code == KeyCode.BACK_SPACE) {
                        textRoot.getChildren().remove(characters.deleteCurrent().getEntry());
                        render();
                        updateCursor();
                        //displayText.setText(characters.toString());
                    }
                }
            }
        }

        private void centerText() {
            // Figure out the size of the current text.
            //double textHeight = displayText.getLayoutBounds().getHeight();
            //double textWidth = displayText.getLayoutBounds().getWidth();

            // Calculate the position so that the text will be centered on the screen.
            //double textTop = textCenterY - textHeight / 2;
            //double textLeft = textCenterX - textWidth / 2;

            // Re-position the text.
            //displayText.setX(textLeft);
            //displayText.setY(textTop);

            // Make sure the text appears in front of any other objects you might add.
            //displayText.toFront();
        }

    }

    private class CursorBlinkEventHandler implements EventHandler<ActionEvent> {

            public CursorBlinkEventHandler() {
                changeColor();
            }

            private void changeColor() {
                if (cursor.getFill() == Color.BLACK) {
                    cursor.setFill(Color.WHITE);
                }
                else {
                    cursor.setFill(Color.BLACK);
                }
            }

            public void handle(ActionEvent event) {
                changeColor();
            }
    }

    public void updateCursor() {
        Text sample = new Text("A");
        sample.setFont(Font.font(fontName, fontSize));
        int textWidth = Math.round((float) characters.getCurrentNode().getEntry().getLayoutBounds().getWidth());
        int textHeight = Math.round((float) sample.getLayoutBounds().getHeight());
        cursor.setX(characters.getCurrentNodeX() + textWidth);
        cursor.setY(characters.getCurrentNodeY());
        cursor.setHeight(textHeight);
    }


    // public void cursorNewLine() {
    //     int textHeight = Math.round((float) characters.getCurrentNode().getEntry().getLayoutBounds().getHeight());
    //     cursor.setX(5);
    //     cursor.setY(characters.getCurrentNodeY() + 15);
    // }

    public void cursorBlink() {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        CursorBlinkEventHandler cursorChange = new CursorBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    public void renderOpen() {
        currentY = 0;
        currentX = 5;
        lines = new ArrayList<Node>();
        Text sample = new Text("A");
        sample.setFont(Font.font(fontName, fontSize));
        for (Node n : characters) {
            //double textHeight = Math.round(x.getLayoutBounds().getHeight());

            Text letter = n.getEntry();
            letter.setTextOrigin(VPos.TOP);
            letter.setFont(Font.font(fontName, fontSize));

            double textHeight = Math.round((float) sample.getLayoutBounds().getHeight());
            textRoot.getChildren().add(letter);

            char typed = letter.getText().charAt(0);

            if ((int) typed == 13 || (int) typed == 10) {
                currentX = 5;
                currentY += textHeight;
                letter.setY(currentY);
                letter.setX(currentX);
            }

            else {

                if (currentX == 5) {
                    lines.add(n);
                }

                if ((int) typed == 32) {
                    //System.out.println("xdxdxdxdxdxd");
                    wordWrap = new FastLinkedList();
                }
                else {
                    wordWrap.addLast(letter);
                }

                int textWidth = Math.round((float) letter.getLayoutBounds().getWidth());
                letter.setY(currentY);
                letter.setX(currentX);
                currentX += textWidth;

                if (currentX + textWidth + 5 >= WINDOW_WIDTH + scrollBar.getWidth()) {
                    if (wordWrap.size() == 0) {
                        currentX -= textWidth;

                    } else if (wordWrap.getFirstText().getX() == 5) {
                        currentX = 5;
                        currentY += textHeight;
                        wordWrap = new FastLinkedList();

                    } else {
                        int xVal = (int) (wordWrap.getFirstText().getX()) - 5;
                        currentY += textHeight;

                        lines.add(wordWrap.getFirst());

                        for (Node z : wordWrap) {
                            Text w = z.getEntry();
                            int xValNew = Math.round((float) w.getX()) - xVal;
                            w.setX(xValNew);
                            w.setY(currentY);
                            currentX = xValNew;
                        }

                        int wLastLetter = Math.round((float) wordWrap.getLastText().getLayoutBounds().getWidth());
                        currentX += wLastLetter;
                    }
                }
            }
        }

        characters.jumpToStart();
        updateCursor();
    }

    public void debug() {
        if (printYes == true) {
            for (Node n : characters) {
                System.out.print(n.getEntry());
            }
        }
    }

    public void undo() {
        if (history.getUndoStack().size() > 0) {
            history.undo();
            textRoot.getChildren().remove(characters.deleteCurrent().getEntry());
            render();
            updateCursor();
        }
    }

    public void redo() {
        if (history.getRedoStack().size() > 0) {
            textRoot.getChildren().add(characters.addCurrentNode(history.redo()).getEntry());
            render();
            updateCursor();
        }
    }

    public void setFont(String font, int size) {

        fontName = font;
        fontSize = size;
        render();
        updateCursor();
    }

    public void render() {
        currentY = 0;
        currentX = 5;
        lines = new ArrayList<Node>();
        Text sample = new Text("A");
        sample.setFont(Font.font(fontName, fontSize));
        for (Node n : characters) {

            Text letter = n.getEntry();
            letter.setFont(Font.font(fontName, fontSize));
            double textHeight = Math.round((float) sample.getLayoutBounds().getHeight());

            char typed = letter.getText().charAt(0);

            if (letter.getText() == "  " || (int) typed == 13 || (int) typed == 10) {
                letter.setFont(Font.font(fontName, fontSize));
                currentX = 5;
                currentY += textHeight;
                letter.setY(currentY);
                letter.setX(currentX);
            }

            else {

                if (currentX == 5) {
                    lines.add(n);
                }

                if ((int) typed == 32) {
                    //System.out.println("xdxdxdxdxdxd");
                    wordWrap = new FastLinkedList();
                }
                else {
                    wordWrap.addLast(letter);
                }

                int textWidth = Math.round((float) letter.getLayoutBounds().getWidth());
                letter.setY(currentY);
                letter.setX(currentX);
                letter.setTextOrigin(VPos.TOP);
                currentX += textWidth;

                if (currentX + textWidth + 5 >= WINDOW_WIDTH - scrollBar.getWidth()) {
                    if (wordWrap.size() == 0) {
                        currentX -= textWidth;

                    } else if (wordWrap.getFirstText().getX() == 5) {
                        currentX = 5;
                        currentY += textHeight;
                        wordWrap = new FastLinkedList();

                    } else {
                        int xVal = (int) (wordWrap.getFirstText().getX()) - 5;
                        currentY += textHeight;

                        lines.add(wordWrap.getFirst());

                        for (Node z : wordWrap) {
                            Text w = z.getEntry();
                            int xValNew = Math.round((float) w.getX()) - xVal;
                            w.setX(xValNew);
                            w.setY(currentY);
                            currentX = xValNew;
                        }

                        int wLastLetter = Math.round((float) wordWrap.getLastText().getLayoutBounds().getWidth());
                        currentX += wLastLetter;
                    }
                }
            }
        }
    }

    public void write() {

        try {
            FileWriter writer;
            if (inputFilename != "") {
                writer = new FileWriter(inputFilename);
            }
            else {
                writer = new FileWriter("untitled.txt");
            }

            for (Node n : characters) {
                writer.write(n.getEntry().getText());
            }

            writer.close();

        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File not found! Exception was: " + fileNotFoundException);
        } catch (IOException ioException) {
            System.out.println("Error when copying; exception was: " + ioException);
        }
    }

    public void read(String inputFilename) {
        try {
            File inputFile = new File(inputFilename);
            // Check to make sure that the input file exists!
            if (!inputFile.exists()) {
                System.out.println("Unable open because file with name " + inputFilename
                    + " does not exist");
                return;
            }

            FileReader reader = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(reader);

            int intRead = -1;
            // Keep reading from the file input read() returns -1, which means the end of the file
            // was reached.
            while ((intRead = bufferedReader.read()) != -1) {
                // The integer read can be cast to a char, because we're assuming ASCII.
                char charRead = (char) intRead;
                characters.addCurrent(new Text(Character.toString(charRead)));
            }

            bufferedReader.close();
            renderOpen();
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File not found! Exception was: " + fileNotFoundException);
        } catch (IOException ioException) {
            System.out.println("Error when copying; exception was: " + ioException);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Create a Node that will be the parent of all things displayed on the screen.
        root = new Group();
        textRoot = new Group();
        root.getChildren().add(textRoot);

        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);

        cursor.setFill(Color.BLACK);
        cursor.setHeight(textHeight);
        cursor.setWidth(1);
        cursor.setX(5);
        cursor.setY(0);
        textRoot.getChildren().add(cursor);
        cursorBlink();

        ScrollBar scrollBar = new ScrollBar();
        scrollBar.setMin(0);
        scrollBar.setMax(scene.getHeight());
        scrollBar.setValue(5);
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setPrefHeight(scene.getHeight());
        scrollBar.setLayoutX(scene.getWidth() - scrollBar.getWidth());
        scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ObservableValue,
                Number oldValue,
                Number newValue) {
            textRoot.setLayoutY(-1 * newValue.intValue());
            }
        });

        root.getChildren().add(scrollBar);

        scene.setOnMouseClicked(new MouseClickEventHandler(root));

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenWidth,
                    Number newScreenWidth) {
                WINDOW_WIDTH = newScreenWidth.intValue();
                scrollBar.setLayoutX(WINDOW_WIDTH - scrollBar.getWidth());
                render();
            }
        });

        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenHeight,
                    Number newScreenHeight) {
                WINDOW_HEIGHT = newScreenHeight.intValue();
                render();
            }
        });

        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);

        primaryStage.setTitle("Editor");
        // This is boilerplate, necessary to setup the window where things are displayed.
        if (inputFilename != "") {
            read(inputFilename);
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            inputFilename = args[0];
            if (args.length == 2) {
                print = args[1];
                if (print.equals("debug")) {
                    printYes = true;
                }
            }
        }
        launch(args);
    }
}
