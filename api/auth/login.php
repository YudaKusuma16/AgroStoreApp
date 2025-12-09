<?php
require_once '../config.php';

// Handle POST request for login
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $data = json_decode(file_get_contents('php://input'), true);

        // Validate required fields
        if (!validateRequired($data, ['email', 'password'])) {
            sendResponse(['error' => 'Email and password are required'], 400);
        }

        // Find user by email
        $sql = "SELECT * FROM users WHERE email = ?";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([$data['email']]);
        $user = $stmt->fetch();

        // Verify password - support both plaintext and hashed passwords
        $passwordValid = false;

        // Check if password is hashed (starts with $2a$ for bcrypt)
        if (substr($user['password'], 0, 4) === '$2a$' || substr($user['password'], 0, 4) === '$2y$') {
            // Password is hashed, use password_verify
            $passwordValid = password_verify($data['password'], $user['password']);
        } else {
            // Password might be plaintext, direct comparison
            $passwordValid = ($data['password'] === $user['password']);
        }

        if ($user && $passwordValid) {
            // Remove password from response
            unset($user['password']);

            sendResponse([
                'message' => 'Login successful',
                'user' => $user
            ]);
        } else {
            sendResponse(['error' => 'Invalid email or password'], 401);
        }

    } catch(PDOException $e) {
        sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
    }
}
?>