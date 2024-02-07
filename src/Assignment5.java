/**
 * Assignment #5
 * Author: Young Sang Kwon, 000847777
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Assignment5 {

    /**
     * Represents a customer in the store with a specific number of items and calculated service time.
     */
    static class Customer {
        private final int itemCount;
        private final int serviceTime;

        /**
         * Constructs a new Customer with a specified number of items.
         * @param itemCount The number of items the customer has.
         */
        Customer(int itemCount) {
            this.itemCount = itemCount;
            this.serviceTime = calculateServiceTime(itemCount);
        }

        /**
         * Calculates the service time based on the number of items.
         * @param items The number of items.
         * @return The calculated service time.
         */
        private int calculateServiceTime(int items) {
            return 45 + 5 * items; // Service time formula: t = 45 + 5 * ni
        }

        public int getItemCount() {
            return itemCount;
        }

        public int getServiceTime() {
            return serviceTime;
        }
    }

    /**
     * Represents a checkout line in the store, which can be either normal or express.
     */
    static class CheckoutLine {
        private LinkedQueue<Customer> customers = new LinkedQueue<>();
        private final int maxItemsAllowed;

        /**
         * Constructs a new CheckoutLine with a maximum item limit.
         * @param maxItemsAllowed The maximum number of items allowed in this line.
         */
        CheckoutLine(int maxItemsAllowed) {
            this.maxItemsAllowed = maxItemsAllowed;
        }

        /**
         * Adds a customer to the line if the customer's item count is within the allowed limit.
         * @param customer The customer to add to the line.
         */
        public void addCustomer(Customer customer) {
            if (maxItemsAllowed == -1 || customer.getItemCount() <= maxItemsAllowed) {
                customers.enqueue(customer);
            }
        }

        /**
         * Generates a string representation of all customers in the checkout line.
         * This method temporarily dequeues customers to build the string, and then re-enqueues them to preserve the original order.
         * @return A string representing the customers in the line, including their item count and service time.
         */
        public String getCustomersString() {
            String result = "";
            LinkedQueue<Customer> tempQueue = new LinkedQueue<>();
            while (!customers.isEmpty()) {
                Customer customer = customers.dequeue();
                tempQueue.enqueue(customer);
                result += "[" + customer.getItemCount() + "(" + customer.getServiceTime() + " s)],";
            }
            // Restore customers to the original queue
            while (!tempQueue.isEmpty()) {
                customers.enqueue(tempQueue.dequeue());
            }
            if (!result.isEmpty()) {
                result = result.substring(0, result.length() - 1); // Remove trailing comma
            }
            return result;
        }

        /**
         * Calculates the total service time for all customers currently in the checkout line.
         * This method temporarily dequeues customers to calculate the total time, and then re-enqueues them to maintain the original order.
         * @return The total service time for all customers in the line.
         */
        public int getTotalServiceTime() {
            int totalServiceTime = 0;
            LinkedQueue<Customer> tempQueue = new LinkedQueue<>();
            while (!customers.isEmpty()) {
                Customer customer = customers.dequeue();
                tempQueue.enqueue(customer);
                totalServiceTime += customer.getServiceTime();
            }
            // Restore customers to the original queue
            while (!tempQueue.isEmpty()) {
                customers.enqueue(tempQueue.dequeue());
            }
            return totalServiceTime;
        }
    }

    public static void main(String[] args) {
        List<CheckoutLine> checkoutLines = new ArrayList<>();
        int threshold = 0;

        try (BufferedReader br = new BufferedReader(new FileReader("CustomerData.txt"))) {
            String setup = br.readLine();
            String[] config = setup.split("\\s+");
            int f = Integer.parseInt(config[0]); // Number of express checkout lines
            int n = Integer.parseInt(config[1]); // Number of normal checkout lines
            threshold = Integer.parseInt(config[2]); // Threshold for express checkout

            for (int i = 0; i < f + n; i++) {
                checkoutLines.add(new CheckoutLine(i < f ? threshold : -1));
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    int items = Integer.parseInt(line.trim());
                    Customer customer = new Customer(items);

                    // Assign customer to the appropriate line
                    CheckoutLine bestLine = null;
                    int bestTime = Integer.MAX_VALUE;

                    for (CheckoutLine checkoutLine : checkoutLines) {
                        int lineTime = checkoutLine.getTotalServiceTime();

                        if (items <= threshold || checkoutLine.maxItemsAllowed == -1) {
                            if (lineTime < bestTime) {
                                bestTime = lineTime;
                                bestLine = checkoutLine;
                            }
                        }
                    }

                    if (bestLine != null) {
                        bestLine.addCustomer(customer);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Part A Output
        System.out.println("PART A - Checkout lines and time estimates for each line");
        int lineNumber = 1;
        for (CheckoutLine line : checkoutLines) {
            String checkoutType = line.maxItemsAllowed != -1 ? "Express" : "Normal";
            System.out.println("CheckOut(" + checkoutType + ") # " + lineNumber + " (Est Time = " + line.getTotalServiceTime() + " s) = [" + line.getCustomersString() + "]");
            lineNumber++;
        }

        // Calculate and print the total time to clear the store of all customers
        int maxTimeToClearStore = checkoutLines.stream().mapToInt(CheckoutLine::getTotalServiceTime).max().orElse(0);
        System.out.println("Time to clear store of all customers = " + maxTimeToClearStore + " s");

        // Part B Output
        System.out.println("\nPART B - Number of customers in line after each minute (60s)");
        maxTimeToClearStore = checkoutLines.stream().mapToInt(CheckoutLine::getTotalServiceTime).max().orElse(0);

        System.out.print("t(s)");
        for (CheckoutLine line : checkoutLines) {
            System.out.print("\tLine" + (checkoutLines.indexOf(line) + 1));
        }
        System.out.println();

        for (int currentTime = 60; currentTime <= maxTimeToClearStore; currentTime += 60) {
            System.out.print(String.format("%4d", currentTime));
            for (CheckoutLine line : checkoutLines) {
                int count = 0;
                LinkedQueue<Customer> tempQueue = new LinkedQueue<>();
                while (!line.customers.isEmpty()) {
                    Customer customer = line.customers.dequeue();
                    tempQueue.enqueue(customer);
                    if (customer.getServiceTime() > currentTime) {
                        count++;
                    }
                }
                // Restore customers to the original queue
                while (!tempQueue.isEmpty()) {
                    line.customers.enqueue(tempQueue.dequeue());
                }
                System.out.print("\t" + String.format("%4d", count));
            }
            System.out.println();
        }
    }
}