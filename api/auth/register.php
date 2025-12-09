<?php
require_once '../config.php';
require_once '../helpers/id_generator.php';

// Handle POST request for registration
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $data = json_decode(file_get_contents('php://input'), true);

        // Validate required fields
        if (!validateRequired($data, ['name', 'email', 'password', 'role'])) {
            sendResponse(['error' => 'All fields are required'], 400);
        }

        // Validate role
        if (!in_array($data['role'], ['pembeli', 'penjual'])) {
            sendResponse(['error' => 'Invalid role'], 400);
        }

        // Check if email already exists
        $checkSql = "SELECT id FROM users WHERE email = ?";
        $checkStmt = $pdo->prepare($checkSql);
        $checkStmt->execute([$data['email']]);

        if ($checkStmt->fetch()) {
            sendResponse(['error' => 'Email already registered'], 409);
        }

        // Hash password
        $hashedPassword = password_hash($data['password'], PASSWORD_DEFAULT);

        // Generate readable ID based on role
        $id = generateUserId($data['role']);

        // Insert new user
        $sql = "INSERT INTO users (id, name, email, password, role)
                VALUES (?, ?, ?, ?, ?)";

        $stmt = $pdo->prepare($sql);
        $result = $stmt->execute([
            $id,
            $data['name'],
            $data['email'],
            $hashedPassword,
            $data['role']
        ]);

        if ($result) {
            sendResponse([
                'message' => 'Registration successful',
                'user' => [
                    'id' => $id,
                    'name' => $data['name'],
                    'email' => $data['email'],
                    'role' => $data['role']
                ]
            ]);
        } else {
            sendResponse(['error' => 'Failed to register user'], 500);
        }

    } catch(PDOException $e) {
        sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
    }
}
?>