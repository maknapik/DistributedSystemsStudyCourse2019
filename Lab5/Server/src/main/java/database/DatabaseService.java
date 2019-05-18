package database;

import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseService {

    private static final String FILENAME_1 = "/database/database1.mk";
    private static final String FILENAME_2 = "/database/database2.mk";
    private static final String ORDERS_FILENAME = "/database/orders.mk";
    private static final String DATABASE_POSITIONS_PREFIX = "/database/position/";

    private Lock lock = new ReentrantLock();

    private double price = -1.0;

    public double findPosition(String title) {

        price = -1.0;

        DatabaseReader databaseReader1 = new DatabaseReader(FILENAME_1, title);
        DatabaseReader databaseReader2 = new DatabaseReader(FILENAME_2, title);

        databaseReader1.run();
        databaseReader2.run();

        return price;
    }

    public boolean saveOrder(String title) {
        new OrderWriter(title).run();

        return true;
    }

    public List<String> readPosition(String title) throws InterruptedException {
        List<String> content = new LinkedList<>();

        PositionReader positionReader = new PositionReader(title, content);
        Thread thread = new Thread(positionReader);

        thread.start();
        thread.join();

        return content;
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
                Scanner scanner = new Scanner(new File(String.valueOf(DatabaseService.class.getResource(filename)
                        .getFile())));

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

    private class OrderWriter implements Runnable {

        private String title;

        OrderWriter(String title) {
            this.title = title;
        }

        @Override
        public void run() {
            lock.lock();

            File file = new File(String.valueOf(DatabaseService.class.getResource(ORDERS_FILENAME).getFile()));
            try (FileWriter fileWriter = new FileWriter(file, true)) {
                fileWriter.write(title + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

    }

    private class PositionReader implements Runnable {

        private String title;
        private List<String> content;

        PositionReader(String title, List<String> content) {
            this.title = title;
            this.content = content;
        }

        @Override
        public void run() {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(getFilePath(title)));

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    content.add(line);
                }

                bufferedReader.close();
            } catch (IOException e) {
                content.add("Position not found");
            }
        }

        private String getFilePath(String title) throws FileNotFoundException {
            return String.valueOf(Optional.ofNullable(
                    DatabaseService.class.getResource(DATABASE_POSITIONS_PREFIX + title + ".mkp"))
                    .map(URL::getFile)
                    .orElseThrow(FileNotFoundException::new));
        }

    }

}
