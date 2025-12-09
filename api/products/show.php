<?php
require_once '../config.php';

// Get product by ID
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    if (!isset($_GET['id'])) {
        sendResponse(['error' => 'Product ID is required'], 400);
    }

    try {
        $sql = "SELECT * FROM products WHERE id = ?";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([$_GET['id']]);
        $product = $stmt->fetch();

        if ($product) {
            // Get seller information
            $sql = "SELECT id, name, email FROM users WHERE id = ?";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([$product['seller_id']]);
            $seller = $stmt->fetch();

            $product['seller'] = $seller;
            sendResponse($product);
        } else {
            sendResponse(['error' => 'Product not found'], 404);
        }

    } catch(PDOException $e) {
        sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
    }
}

// Update product
if ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    if (!isset($_GET['id'])) {
        sendResponse(['error' => 'Product ID is required'], 400);
    }

    try {
        $data = json_decode(file_get_contents('php://input'), true);

        // Validate required fields
        $required = ['name', 'category', 'price', 'description', 'stock', 'sellerId'];
        if (!validateRequired($data, $required)) {
            sendResponse(['error' => 'Missing required fields'], 400);
        }

        // Check if product exists and belongs to seller
        $checkSql = "SELECT seller_id FROM products WHERE id = ?";
        $checkStmt = $pdo->prepare($checkSql);
        $checkStmt->execute([$_GET['id']]);
        $product = $checkStmt->fetch();

        if (!$product) {
            sendResponse(['error' => 'Product not found'], 404);
        }

        if ($product['seller_id'] != $data['sellerId']) {
            sendResponse(['error' => 'Unauthorized to update this product'], 403);
        }

        $sql = "UPDATE products SET
                name = ?,
                category = ?,
                price = ?,
                description = ?,
                stock = ?,
                image_url = ?,
                updated_at = CURRENT_TIMESTAMP
                WHERE id = ?";

        $stmt = $pdo->prepare($sql);
        $result = $stmt->execute([
            $data['name'],
            $data['category'],
            $data['price'],
            $data['description'],
            $data['stock'],
            $data['imageUrl'] ?? '',
            $_GET['id']
        ]);

        if ($result) {
            sendResponse(['message' => 'Product updated successfully']);
        } else {
            sendResponse(['error' => 'Failed to update product'], 500);
        }

    } catch(PDOException $e) {
        sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
    }
}

// Delete product
if ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    if (!isset($_GET['id'])) {
        sendResponse(['error' => 'Product ID is required'], 400);
    }

    if (!isset($_GET['seller_id'])) {
        sendResponse(['error' => 'Seller ID is required'], 400);
    }

    try {
        // Check if product exists and belongs to seller
        $checkSql = "SELECT seller_id FROM products WHERE id = ?";
        $checkStmt = $pdo->prepare($checkSql);
        $checkStmt->execute([$_GET['id']]);
        $product = $checkStmt->fetch();

        if (!$product) {
            sendResponse(['error' => 'Product not found'], 404);
        }

        if ($product['seller_id'] != $_GET['seller_id']) {
            sendResponse(['error' => 'Unauthorized to delete this product'], 403);
        }

        $sql = "DELETE FROM products WHERE id = ?";
        $stmt = $pdo->prepare($sql);
        $result = $stmt->execute([$_GET['id']]);

        if ($result) {
            sendResponse(['message' => 'Product deleted successfully']);
        } else {
            sendResponse(['error' => 'Failed to delete product'], 500);
        }

    } catch(PDOException $e) {
        sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
    }
}
?>