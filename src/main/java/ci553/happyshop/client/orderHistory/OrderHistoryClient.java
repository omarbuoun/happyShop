package ci553.happyshop.client.orderHistory;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Standalone Order History Client that can be run independently.
 * Displays order history, statistics, and provides export functionality.
 */
public class OrderHistoryClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage window) {
        OrderHistoryView view = new OrderHistoryView();
        view.start(window);
    }
}



