import java.util.Scanner;

public class InstapayApp {
    private static final OTPService otpService = new OTPService();
    private static final BankAccountService bankAccountService = new BankAccountService();
    private static final InstapaySystem instapaySystem = new InstapaySystem(otpService, bankAccountService);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nWelcome to Instapay!");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int option = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (option) {
                case 1:
                    registerUser(scanner);
                    break;
                case 2:
                    loginUser(scanner);
                    break;
                case 3:
                    System.out.println("Exiting Instapay. Thank you for using our service!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option selected. Please try again.");
            }
        }
    }

    private static void registerUser(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine(); // Hash this password in a real application
        System.out.print("Enter mobile number: ");
        String mobileNumber = scanner.nextLine();
        System.out.print("Will you be registering with a bank account? (yes/no): ");
        boolean isBankUser = "yes".equalsIgnoreCase(scanner.nextLine());
        String bankAccountNumber = null;
        String walletProvider = null;

        if (isBankUser) {
            System.out.print("Enter bank account number: ");
            bankAccountNumber = scanner.nextLine();
            if (!bankAccountService.verifyBankAccount(bankAccountNumber)) {
                System.out.println("Bank account verification failed. Please check the number and try again.");
                return;
            }
        } else {
            System.out.print("Enter wallet provider name: ");
            walletProvider = scanner.nextLine();
        }

        int otp = otpService.generateOTP(mobileNumber);
        System.out.print("Enter the OTP sent to your mobile number: ");
        int enteredOtp = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (otpService.verifyOTP(mobileNumber, enteredOtp)) {
            boolean registered = instapaySystem.registerUser(username, password, mobileNumber, isBankUser, bankAccountNumber, walletProvider);
            if (registered) {
                System.out.println("Registration successful!");
            } else {
                System.out.println("Registration failed. Username may already be taken.");
            }
        } else {
            System.out.println("Incorrect OTP. Registration failed.");
        }
    }

    private static void loginUser(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = instapaySystem.login(username, password);
        if (user != null) {
            System.out.println("Login successful!");
            userActions(user, scanner);
        } else {
            System.out.println("Login failed. Incorrect username or password.");
        }
    }

    private static void userActions(User user, Scanner scanner) {
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("\nUser: " + user.getUsername());
            System.out.println("1. Show Balance");
            System.out.println("2. Transfer Funds");
            System.out.println("3. Pay Bills");
            System.out.println("4. Logout");
            System.out.print("Choose an action: ");

            int action = scanner.nextInt();
            scanner.nextLine(); // Consume the newline

            switch (action) {
                case 1:
                    user.displayBalance();
                    break;
                case 2:
                        performTransfer(scanner, user);
                    break;
                case 3:
                    payBills(scanner, user);
                    break;
                case 4:
                    isRunning = false;
                    System.out.println("You have been logged out.");
                    break;
                default:
                    System.out.println("Invalid action selected. Please try again.");
            }
        }
    }

    private static void performTransfer(Scanner scanner, User user) {
        System.out.println("Select transfer type:");
        System.out.println("1. Transfer to Wallet");
        System.out.println("2. Transfer to Another Bank Account");
        int transferType = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (transferType) {
            case 1: // Transfer to Wallet
                System.out.print("Enter the mobile number of the wallet: ");
                String mobileNumber = scanner.nextLine();
                System.out.print("Enter the amount to transfer to the wallet: ");
                double walletAmount = scanner.nextDouble();
                scanner.nextLine(); // Consume newline
                if (instapaySystem.transferToWallet(user.getUsername(), mobileNumber, walletAmount)) {
                    System.out.println("Transfer to wallet successful.");
                } else {
                    System.out.println("Transfer to wallet failed.");
                }
                break;
            case 2: // Transfer to Another Instapay Account
                System.out.print("Enter the username of the Instapay account: ");
                String toUsername = scanner.nextLine();
                System.out.print("Enter the amount to transfer to the Instapay account: ");
                double accountAmount = scanner.nextDouble();
                scanner.nextLine(); // Consume newline
                if (user.isBankUser()) {
                    if (instapaySystem.transferToInstapayAccount(user.getUsername(), toUsername, accountAmount)) {
                        System.out.println("Transfer to Instapay account successful.");
                    } else {
                        System.out.println("Transfer to Instapay account failed.");
                    }
                } else {
                    System.out.println("Only bank users can transfer to bank accounts.");
                }
                break;
            default:
                System.out.println("Invalid transfer type selected.");
                break;
        }
    }

    private static void payBills(Scanner scanner, User user) {
        System.out.println("Which bill would you like to pay?");
        System.out.println("1. Gas");
        System.out.println("2. Electricity");
        System.out.println("3. Water");
        System.out.println("4. Telecom & Internet");
        System.out.print("Choose the bill type: ");
        int billType = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter the bill amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        String billTypeName;
        switch (billType) {
            case 1:
                billTypeName = "Gas";
                break;
            case 2:
                billTypeName = "Electricity";
                break;
            case 3:
                billTypeName = "Water";
                break;
            case 4:
                billTypeName = "Telecom & Internet";
                break;
            default:
                System.out.println("Invalid bill type selected.");
                return;
        }

        Bill bill = new Bill(billTypeName, amount);
        boolean billPaid = instapaySystem.processBillPayment(user.getUsername(), bill);
        if (billPaid) {
            System.out.println("Your " + billTypeName + " bill has been paid.");
            user.displayBalance();
        } else {
            System.out.println("Failed to pay bill. Check your balance and try again.");
        }
    }
}
