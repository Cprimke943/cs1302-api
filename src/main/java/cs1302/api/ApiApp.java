package cs1302.api;

import javafx.geometry.Pos;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.net.http.HttpClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Functions as a Pokemon Pokedex using the PokeAPI and the PokemonTCG API.
 */
public class ApiApp extends Application {

    /**
     * Class for Json response.
     */
    private class PokeResponse {
        Integer id;
        String name;
        PokemonSprites sprites;
        PokemonType[] types;
        int height;
        int weight;
    } // PokeResponse

    /**
     * Class for Json response.
     */
    private class PokeSpecies {
        @SerializedName("gender_rate") int genderRate;
    } // PokeResponse

    /**
     * Class for Json response.
     */
    private class PokemonType {
        Types type;
    } // PokemonTypes

    /**
     * Class for Json response.
     */
    private class Types {
        String name;
    } // Types

    /**
     * Class for Json response.
     */
    private class PokemonSprites {
        @SerializedName("front_default") String frontDefault;
    } // PokemonSprites

    /**
     * Class for Json response.
     */
    private class PokeCards {
        Card[] data;
    } // PokeResponse

    /**
     * Class for Json response.
     */
    private class Card {
        Pic images;
    } // PokeResponse

    /**
     * Class for Json response.
     */
    private class Pic {
        String small;
    } // PokeResponse


    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    private static final String POKE_API = "https://pokeapi.co/api/v2/pokemon";
    private static final String POKE_CARD_API = "https://api.pokemontcg.io/v2/cards?q=name:";

    Stage stage;
    Scene scene;
    VBox root;

    HBox alayer;
    Button search;
    Label searchLabel;
    TextField pokename;

    HBox blayer;
    ImageView banner;
    VBox info;
    Label title;
    Label type;
    Label gender;
    Label heightLabel;
    Label weightLabel;
    ImageView cardViewer;

    HBox clayer;
    Label idLabel;
    Button left;
    Button right;

    Alert alert;
    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */

    public ApiApp() {
        root = new VBox();

        alayer = new HBox(8);
        search = new Button("Find");
        searchLabel = new Label("Enter the Name or #Id of a Pokemon:");
        pokename = new TextField("Bulbasaur");

        blayer = new HBox();
        banner = new ImageView();
        info = new VBox(5);
        title = new Label("Name: ");
        type = new Label("Type: ");
        gender = new Label("Gender: ");
        heightLabel = new Label("Height: ");
        weightLabel = new Label("Weight: ");
        cardViewer = new ImageView();

        clayer = new HBox(8);
        left = new Button("<--");
        right = new Button("-->");
        idLabel = new Label();

        alert = new Alert(AlertType.NONE);
    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void init() {
        System.out.println("init called");
        // Image ratio
        banner.setFitHeight(150);
        banner.setPreserveRatio(true);
        // Base state
        getPoke(pokename.getText());
        getGender(pokename.getText());
        //alayer
        alayer.setBackground(new Background(new BackgroundFill(Color.rgb(226,60,52), null, null)));
        searchLabel.setTextFill(Color.WHITE);
        searchLabel.setStyle("-fx-font-weight: bold");
        searchLabel.setFont(new Font("Arial", 15));
        // blayer
        blayer.setHgrow(info, Priority.ALWAYS);
        info.setBackground(new Background(new BackgroundFill(Color.LIGHTGREY, null, null)));
        title.setFont(new Font("Arial", 15));
        title.setStyle("-fx-font-weight: bold");
        type.setFont(new Font("Arial", 15));
        type.setStyle("-fx-font-weight: bold");
        gender.setFont(new Font("Arial", 15));
        gender.setStyle("-fx-font-weight: bold");
        heightLabel.setFont(new Font("Arial", 15));
        heightLabel.setStyle("-fx-font-weight: bold");
        weightLabel.setFont(new Font("Arial", 15));
        weightLabel.setStyle("-fx-font-weight: bold");
        cardViewer.setFitHeight(150);
        cardViewer.setPreserveRatio(true);
        // Buttons
        Runnable handler = () -> {
            getPoke(pokename.getText());
            getGender(pokename.getText());
        }; search.setOnAction(event -> runOnNewThread(handler));
        Runnable handler2 = () -> {
            Integer holder = Integer.valueOf(idLabel.getText().substring(4)) - 1;
            getPoke(holder.toString());
            getGender(holder.toString());
        }; left.setOnAction(event -> runOnNewThread(handler2));
        Runnable handler3 = () -> {
            Integer holder = Integer.valueOf(idLabel.getText().substring(4)) + 1;
            getPoke(holder.toString());
            getGender(holder.toString());
        }; right.setOnAction(event -> runOnNewThread(handler3));
        // clayer
        left.setPrefWidth(50);
        right.setPrefWidth(50);
        clayer.setBackground(new Background(new BackgroundFill(Color.rgb(226,60,52), null, null)));
        idLabel.setTextFill(Color.WHITE);
        idLabel.setStyle("-fx-font-weight: bold");
        idLabel.setFont(new Font("Arial", 15));
        left.setDisable(true);
        // Children
        clayer.setAlignment(Pos.CENTER);
        alayer.getChildren().addAll(searchLabel, pokename, search);
        blayer.getChildren().addAll(banner, info, cardViewer);
        info.getChildren().addAll(title, type, gender, heightLabel, weightLabel);
        clayer.getChildren().addAll(left, idLabel, right);
        root.getChildren().addAll(alayer, blayer, clayer);
    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;

        scene = new Scene(root);

        // setup stage
        stage.setTitle("PokeDex App");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();
    } // start

    /**
     * Retrieves most of the information about the pokemon from PokeAPI.
     * Info including name, id, weight, and height.
     *
     * @param name Name or id of pokemon.
     */
    public void getPoke(String name) {
        name = name.toLowerCase().trim();
        String uri = POKE_API + "/" + name;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
            // send request / receive response in the form of a String
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());
            // ensure the request is okay
            if (response.statusCode() != 200) {
                throw new IOException(response.toString());
            } // if
            // get request body (the content we requested)
            String jsonString = response.body();
            jsonString.trim();
            // parse the JSON-formatted string using GSON
            PokeResponse pokeresponse = GSON
                .fromJson(jsonString, PokeResponse.class);
            String urlImage = pokeresponse.sprites.frontDefault;
            banner.setImage(new Image(urlImage));
            Platform.runLater(() -> title.setText("Name: " +
                pokeresponse.name.substring(0,1).toUpperCase() + pokeresponse.name.substring(1)));
            Platform.runLater(() -> type.setText("Type: " + pokeresponse.types[0].type
                .name.substring(0,1).toUpperCase() + pokeresponse.types[0].type.name.substring(1)));
            if (pokeresponse.types.length > 1) {
                Platform.runLater(() -> type.setText("Type: " + pokeresponse.types[0].type
                    .name.substring(0,1).toUpperCase() + pokeresponse.types[0].type.name
                    .substring(1) + "/" + pokeresponse.types[1].type.name.substring(0,1)
                    .toUpperCase() + pokeresponse.types[1].type.name.substring(1)));
            } // if
            Platform.runLater(() -> idLabel.setText("Id: " + pokeresponse.id.toString()));
            DecimalFormat df = new DecimalFormat("0.00");
            double holdheight = pokeresponse.height / 3.048;
            Platform.runLater(() -> heightLabel.setText("Height: "
                + df.format(holdheight) + " ft"));
            double holdweight = pokeresponse.weight / 4.538;
            Platform.runLater(() -> weightLabel.setText("Weight: "
                + df.format(holdweight) + " lbs"));
            right.setDisable(false);
            left.setDisable(false);
            if (pokeresponse.id == 1) {
                left.setDisable(true);
            }
            if (pokeresponse.id == 1008) {
                right.setDisable(true);
            }
            getCard(pokeresponse.name);
        } catch (IOException | InterruptedException e) {
            String holder = "created just so it doesnt break";
        } // try - catch
    } // getPoke

    /**
     * Retrieves the gender of the pokemon from PokeAPI.
     *
     * @param name Name or id of pokemon.
     */
    public void getGender(String name) {
        name = name.toLowerCase().trim();
        String uri = POKE_API + "-species/" + name;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
            // send request / receive response in the form of a String
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());
            // ensure the request is okay
            if (response.statusCode() != 200) {
                throw new IOException(response.toString());
            } // if
            // get request body (the content we requested)
            String jsonString = response.body();
            jsonString.trim();
            // parse the JSON-formatted string using GSON
            PokeSpecies pokespecies = GSON
                .fromJson(jsonString, PokeSpecies.class);
            if (pokespecies.genderRate == -1) {
                Platform.runLater(() -> gender.setText("Gender: Genderless"));
            } else if (pokespecies.genderRate == 8) {
                Platform.runLater(() -> gender.setText("Gender: Female"));
            } else if (pokespecies.genderRate == 0) {
                Platform.runLater(() -> gender.setText("Gender: Male"));
            } else {
                Platform.runLater(() -> gender.setText("Gender: Male/Female"));
            } // else
        } catch (IOException | InterruptedException e) {
            alert.setAlertType(AlertType.ERROR);
            alert.setContentText("Name or Id of Pokemon does not exist, Please try again."
                + "\n\n" + e.toString());
            alert.setHeight(300);
            alert.setWidth(400);
            Platform.runLater(() -> alert.show());
        } // try - catch
    } // getGender

    /**
     * Retrieves a pokemon card from PokemonTCG API.
     * Card is first one out of given list of cards with Pokemon's name.
     *
     * @param name Name or id of pokemon.
     */
    public void getCard(String name) {
        name = name.toLowerCase().trim();
        String uri = POKE_CARD_API + name;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
            // send request / receive response in the form of a String
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());
            // ensure the request is okay
            if (response.statusCode() != 200) {
                throw new IOException(response.toString());
            } // if
            // get request body (the content we requested)
            String jsonString = response.body();
            jsonString.trim();
            PokeCards pokecards = GSON
                .fromJson(jsonString, PokeCards.class);
            if (pokecards.data.length == 0) {
                throw new IOException(response.toString());
            } else {
                Platform.runLater(() -> cardViewer.setImage
                    (new Image(pokecards.data[0].images.small)));
            } // else
        } catch (IOException | InterruptedException e) {
            alert.setAlertType(AlertType.ERROR);
            alert.setContentText("Problem finding card for pokemon or Card does not exist. "
                + "Showing last used card."  + "\n\n" + e.toString());
            alert.setHeight(300);
            alert.setWidth(400);
            Platform.runLater(() -> alert.show());
        } // try - catch
    } // getCard

    /**
     * New Thread.
     *
     * @param target
     */
    public static void runOnNewThread(Runnable target) {
        Thread t = new Thread(target);
        t.setDaemon(true);
        t.start();
    } // runOnNewThread

} // ApiApp
