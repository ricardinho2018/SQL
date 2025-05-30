import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class DatabaseSetup {

private static final String DB_USER = "root";
private static final String DB_PASSWORD = "123456";
private static final String DB_URL = "jdbc:mysql://localhost:3306/user_men?serverTimezone=UTC";


     // Certifique-se de que está usando a senha correta aqui
    private static final String FILE_PATH = "users.txt";


    public static Connection connectToDb() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            System.exit(1);
            return null;
        }
    }

    public static boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.matches(regex, email);
    }

    public static void addUser(String name, String email) {
        if (!isValidEmail(email)) {
            System.out.println("Email inválido. Tente novamente.");
            return;
        }
        
        try (Connection conn = connectToDb();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (name, email) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                saveUserToFile(id, name, email);
            }
            System.out.println("Usuário adicionado com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao adicionar usuário: " + e.getMessage());
        }
    }

    private static void saveUserToFile(int id, String name, String email) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(id + ";" + name + ";" + email);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Erro ao salvar usuário no arquivo: " + e.getMessage());
        }
    }

    public static void listUsers() {
        try (Connection conn = connectToDb();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {

            System.out.println("\nLista de Usuários:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                System.out.printf("ID: %d, Nome: %s, Email: %s%n", id, name, email);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar usuários: " + e.getMessage());
        }
    }

    public static void deleteUser(int userId) {
        try (Connection conn = connectToDb();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Usuário deletado com sucesso!");
                updateFile();
            } else {
                System.out.println("Usuário não encontrado!");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao deletar usuário: " + e.getMessage());
        }
    }
    
    private static void updateFile() {
        try (Connection conn = connectToDb();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users");
             BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                writer.write(id + ";" + name + ";" + email);
                writer.newLine();
            }
        } catch (SQLException | IOException e) {
            System.out.println("Erro ao atualizar o arquivo: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Adicionar Usuário");
            System.out.println("2. Listar Usuários");
            System.out.println("3. Deletar Usuário");
            System.out.println("4. Sair");
            System.out.print("Escolha uma opção: ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    System.out.print("Digite o nome: ");
                    String name = scanner.nextLine();
                    System.out.print("Digite o email: ");
                    String email = scanner.nextLine();
                    addUser(name, email);
                    break;
                case "2":
                    listUsers();
                    break;
                case "3":
                    System.out.print("Digite o ID do usuário para deletar: ");
                    try {
                        int userId = Integer.parseInt(scanner.nextLine());
                        deleteUser(userId);
                    } catch (NumberFormatException e) {
                        System.out.println("ID inválido. Deve ser um número inteiro.");
                    }
                    break;
                case "4":
                    System.out.println("Saindo do programa.");
                    scanner.close();
                    return;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }
}
