package database;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class DatabaseService {

    private static final String FILENAME_1 = "/database/database1.mk";
    private static final String FILENAME_2 = "/database/database2.mk";

    private double price = -1.0;

    public double findPosition(String title) {

        price = -1.0;

        DatabaseReader databaseReader1 = new DatabaseReader(FILENAME_1, title);
        DatabaseReader databaseReader2 = new DatabaseReader(FILENAME_2, title);

        databaseReader1.run();
        databaseReader2.run();

        return price;
    }

    private class DatabaseReader implements Runnable {

        private String filename;
        private String title;

        DatabaseReader(String filename, String title) {
            this.filename = filename;
            this.title = title;
        }

        @Override
        public void run() {
            try {
                Scanner scanner = new Scanner(new File(String.valueOf(DatabaseService.class.getResource(filename).getFile())));

                while (scanner.hasNext() && price < 0.0) {
                    String line = scanner.nextLine().toLowerCase();
                    String[] parameters = line.split(" ; ");
                    String lineTitle = parameters[0];

                    if (lineTitle.equals(title)) {
                        price = Double.parseDouble(parameters[1]);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

}
