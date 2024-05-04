package com.example.ccc151_proj.control;

import com.example.ccc151_proj.Main;
import com.example.ccc151_proj.model.ContributionProperties;
import com.example.ccc151_proj.model.DataManager;
import com.example.ccc151_proj.model.StudentPaymentInfo;
import com.example.ccc151_proj.model.UserData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ClassRepControl {
    @FXML private URL location;
    @FXML private ResourceBundle resources;
    @FXML private TextField user_position;
    @FXML private ImageView user_image;
    @FXML private TextField user_name;
    @FXML private TextField user_id;
    @FXML private TextField user_year_level;
    @FXML private TextField user_program;
    @FXML private TextField org_code;
    @FXML private TextField contribution_info;
    @FXML private TableView<ContributionProperties> contribution_data_table;
    @FXML private TableColumn<ContributionProperties,String> code_column;
    @FXML private TableColumn<ContributionProperties,String> sem_column;
    @FXML private TableColumn<ContributionProperties,Integer> amount_column;
    @FXML private TableView<StudentPaymentInfo> student_data_table;
    @FXML private TableColumn<StudentPaymentInfo,String> id_column;
    @FXML private TableColumn<StudentPaymentInfo,String> first_name_column;
    @FXML private TableColumn<StudentPaymentInfo,String> middle_name_column;
    @FXML private TableColumn<StudentPaymentInfo,String> last_name_column;
    @FXML private TableColumn<StudentPaymentInfo,String> first_sem_column;
    @FXML private TableColumn<StudentPaymentInfo,String> second_sem_column;
    @FXML private Button ec_button;
    @FXML private Button society_button;

    private UserData user;
    private ObservableList<ContributionProperties> details_contribution_ec;
    private ObservableList<ContributionProperties> details_contribution_society;

    public ClassRepControl(){}

    /**
     * Set the initial display. Along with the information of the class rep user.
     * @param user_id_number
     */
    public void initialize(String user_id_number) {
        //get the user details
        getUserData(user_id_number);

        // just use this default photo because we don't have enough time to implement changing of profile.
        File file = new File("src/src/default-user-image.jpg");
        Image image = new Image(file.toURI().toString());
        user_image.setImage(image);

        //set up the user details to be shown in the frame
        user_position.setText(user.getPosition());
        String user_full_name = user.getFirst_name() + " " + user.getMiddle_name() + " " + user.getLast_name();
        user_name.setText(user_full_name);
        user_id.setText(user.getId_number());
        user_year_level.setText(user.getYear_level());
        user_program.setText(user.getProgram_code());

        //display the society first
        setup_society();
    }

    /**
     * Get the information about the user from the database.
     * @param user_id_number
     */
    private void getUserData(String user_id_number){
        try {
            Connection connect = DataManager.getConnect();
            //set up the query
            String user_data_query = "SELECT `first_name`, `middle_name`, `last_name`, `user_id`, `year_level`, `program_code`, `position`, `organization_code` "
                    + "FROM `users` AS `u` LEFT JOIN `students` AS `s` ON `u`.`user_id` = `s`.`id_number` "
                    + "LEFT JOIN `officers` AS `o` ON `u`.`user_id` = `o`.`officer_id` "
                    + "WHERE `user_id` = \"" + user_id_number + "\";";
            //prepare then execute the query
            PreparedStatement setup_user_data = connect.prepareStatement(user_data_query);
            ResultSet result = setup_user_data.executeQuery();

            result.next();
            String first_name_data = result.getString("first_name");
            String middle_name_data = result.getString("middle_name");
            String last_name_data = result.getString("last_name");
            String user_id_data = result.getString("user_id");
            String year_level_data = result.getString("year_level");
            String program_code_data = result.getString("program_code");
            String position_data = result.getString("position");
            String organization_code_data1 = result.getString("organization_code");
            //since a classroom rep can collect up to 2 contribution from 2 different organizations, get the second one also.
            result.next();
            String organization_code_data2 = result.getString("organization_code");

            user = new UserData(first_name_data, middle_name_data, last_name_data, user_id_data,
                    year_level_data, program_code_data, position_data,
                    organization_code_data1, organization_code_data2);

            setup_user_data.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the contribution details of each organization during the current A.Y.
     * @param academic_year
     * @param org_code
     * @return
     */
    private ObservableList<ContributionProperties> get_contributions(String academic_year, String org_code){
        ObservableList<ContributionProperties> contributions = FXCollections.observableArrayList();
        try {
            Connection connect = DataManager.getConnect();
            String user_data_query = "SELECT `contribution_code`, `semester`,`amount` FROM `contributions` WHERE `academic_year` = \""
                    + academic_year + "\" AND `collecting_org_code` = \"" + org_code + "\";";
            PreparedStatement setup_user_data = connect.prepareStatement(user_data_query);
            ResultSet result = setup_user_data.executeQuery();
            while (result.next()){
                String contribution_code = result.getString("contribution_code");
                String semester = result.getString("semester");
                Integer amount = result.getInt("amount");
                contributions.add(new ContributionProperties(contribution_code, semester, amount));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return contributions;
    }

    /**
     * Set up the tables for EC.
     */
    private void setup_ec(){
        org_code.setText(user.getEc_code());
        contribution_info.setText("EC Contribution");

        code_column.setCellValueFactory(new PropertyValueFactory<>("contribution_code"));
        sem_column.setCellValueFactory(new PropertyValueFactory<>("contribution_sem"));
        amount_column.setCellValueFactory(new PropertyValueFactory<>("contribution_amount"));
        code_column.setStyle("-fx-alignment: CENTER;");
        sem_column.setStyle("-fx-alignment: CENTER;");
        amount_column.setStyle("-fx-alignment: CENTER;");

        details_contribution_ec = get_contributions("2023-2024", org_code.getText());
        contribution_data_table.setItems(details_contribution_ec);

        id_column.setCellValueFactory(new PropertyValueFactory<>("id_number"));
        first_name_column.setCellValueFactory(new PropertyValueFactory<>("first_name"));
        middle_name_column.setCellValueFactory(new PropertyValueFactory<>("middle_name"));
        last_name_column.setCellValueFactory(new PropertyValueFactory<>("last_name"));
        first_sem_column.setCellValueFactory(new PropertyValueFactory<>("first_sem_status"));
        second_sem_column.setCellValueFactory(new PropertyValueFactory<>("second_sem_status"));

        id_column.setStyle("-fx-alignment: CENTER;");
        first_name_column.setStyle("-fx-alignment: CENTER;");
        middle_name_column.setStyle("-fx-alignment: CENTER;");
        last_name_column.setStyle("-fx-alignment: CENTER;");
        first_sem_column.setStyle("-fx-alignment: CENTER;");
        second_sem_column.setStyle("-fx-alignment: CENTER;");

        //TODO: Change to accommodate MySql
        ObservableList<StudentPaymentInfo> details_students = FXCollections.observableArrayList();
        details_students.add(new StudentPaymentInfo("2022-0378", "Caine Ivan", "Recomata",
                "Bautista", "Not Paid", "Not Paid"));
        student_data_table.setItems(details_students);
    }

    /**
     * Set up the tables for Society.
     */
    private void setup_society(){
        org_code.setText(user.getSociety_code());
        contribution_info.setText("Society Contribution");

        code_column.setCellValueFactory(new PropertyValueFactory<>("contribution_code"));
        sem_column.setCellValueFactory(new PropertyValueFactory<>("contribution_sem"));
        amount_column.setCellValueFactory(new PropertyValueFactory<>("contribution_amount"));
        code_column.setStyle("-fx-alignment: CENTER;");
        sem_column.setStyle("-fx-alignment: CENTER;");
        amount_column.setStyle("-fx-alignment: CENTER;");

        details_contribution_society = get_contributions("2023-2024", org_code.getText());
        contribution_data_table.setItems(details_contribution_society);

        id_column.setCellValueFactory(new PropertyValueFactory<>("id_number"));
        first_name_column.setCellValueFactory(new PropertyValueFactory<>("first_name"));
        middle_name_column.setCellValueFactory(new PropertyValueFactory<>("middle_name"));
        last_name_column.setCellValueFactory(new PropertyValueFactory<>("last_name"));
        first_sem_column.setCellValueFactory(new PropertyValueFactory<>("first_sem_status"));
        second_sem_column.setCellValueFactory(new PropertyValueFactory<>("second_sem_status"));

        id_column.setStyle("-fx-alignment: CENTER;");
        first_name_column.setStyle("-fx-alignment: CENTER;");
        middle_name_column.setStyle("-fx-alignment: CENTER;");
        last_name_column.setStyle("-fx-alignment: CENTER;");
        first_sem_column.setStyle("-fx-alignment: CENTER;");
        second_sem_column.setStyle("-fx-alignment: CENTER;");

        //TODO: Change to accommodate MySql
        ObservableList<StudentPaymentInfo> details_students = FXCollections.observableArrayList();
        details_students.add(new StudentPaymentInfo("2022-3337", "Angel Claire", "Abrazado",
                "Cabilla", "Not Paid", "Not Paid"));
        student_data_table.setItems(details_students);
    }

    @FXML
    private void ec_button_clicked(ActionEvent event) {
        ec_button.setDisable(true);
        society_button.setDisable(false);
        setup_ec();
    }

    @FXML
    private void society_button_clicked(ActionEvent event) {
        ec_button.setDisable(false);
        society_button.setDisable(true);
        setup_society();
    }

    @FXML
    private void menu_first_sem() throws IOException {
        StudentPaymentInfo payer = student_data_table.getSelectionModel().getSelectedItem();
        if (payer != null){
            Stage transaction_stage = new Stage();
            transaction_stage.initModality(Modality.APPLICATION_MODAL);

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("transaction-form.fxml"));
            Parent transaction_parent = fxmlLoader.load();
            TransactionProcess transaction_process = fxmlLoader.getController();
            transaction_process.setPayer(payer);
            transaction_process.initialize(contribution_data_table.getItems().get(0).getContribution_code());

            Scene transaction_scene = new Scene(transaction_parent);
            transaction_stage.setTitle("Contribution Payment Transaction");
            transaction_stage.setScene(transaction_scene);
            transaction_stage.show();
        } else {
            Alert non_selected = new Alert(Alert.AlertType.ERROR);
            non_selected.setTitle("No Student Selected");
            non_selected.setHeaderText(null);
            non_selected.setContentText("Please Select a Student.");
            non_selected.showAndWait();
        }
    }

    @FXML
    private void menu_second_sem() throws IOException {
        StudentPaymentInfo payer = student_data_table.getSelectionModel().getSelectedItem();
        if (payer != null){
            Stage transaction_stage = new Stage();
            transaction_stage.initModality(Modality.APPLICATION_MODAL);

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("transaction-form.fxml"));
            Parent transaction_parent = fxmlLoader.load();
            TransactionProcess transaction_process = fxmlLoader.getController();
            transaction_process.setPayer(payer);
            transaction_process.initialize(contribution_data_table.getItems().get(1).getContribution_code());

            Scene transaction_scene = new Scene(transaction_parent);
            transaction_stage.setTitle("Contribution Payment Transaction");
            transaction_stage.setScene(transaction_scene);
            transaction_stage.show();
        } else {
            Alert non_selected = new Alert(Alert.AlertType.ERROR);
            non_selected.setTitle("No Student Selected");
            non_selected.setHeaderText(null);
            non_selected.setContentText("Please Select a Student.");
            non_selected.showAndWait();
        }

    }
}