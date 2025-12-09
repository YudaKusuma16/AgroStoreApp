<?php
require_once '../config.php';
require_once '../helpers/id_generator.php';

// Add new review
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $data = json_decode(file_get_contents('php://input'), true);

        // Validate required fields
        $required = ['product_id', 'user_id', 'rating', 'comment'];
        if (!validateRequired($data, $required)) {
            sendResponse(['error' => 'Missing required fields'], 400);
        }

        // Validate rating
        if ($data['rating'] < 1 || $data['rating'] > 5) {
            sendResponse(['error' => 'Rating must be between 1 and 5'], 400);
        }

        // Check if user already reviewed this product
        $checkSql = "SELECT id FROM reviews WHERE product_id = ? AND user_id = ?";
        $checkStmt = $pdo->prepare($checkSql);
        $checkStmt->execute([$data['product_id'], $data['user_id']]);

        if ($checkStmt->fetch()) {
            // Update existing review
            $sql = "UPDATE reviews SET rating = ?, comment = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE product_id = ? AND user_id = ?";
            $stmt = $pdo->prepare($sql);
            $result = $stmt->execute([
                $data['rating'],
                $data['comment'],
                $data['product_id'],
                $data['user_id']
            ]);

            if ($result) {
                sendResponse(['message' => 'Review updated successfully']);
            } else {
                sendResponse(['error' => 'Failed to update review'], 500);
            }
        } else {
            // Insert new review
            $reviewId = generateReviewId();
            $sql = "INSERT INTO reviews (id, product_id, user_id, rating, comment)
                    VALUES (?, ?, ?, ?, ?)";
            $stmt = $pdo->prepare($sql);
            $result = $stmt->execute([
                $reviewId,
                $data['product_id'],
                $data['user_id'],
                $data['rating'],
                $data['comment']
            ]);

            if ($result) {
                sendResponse([
                    'message' => 'Review added successfully',
                    'review_id' => $reviewId
                ]);
            } else {
                sendResponse(['error' => 'Failed to add review'], 500);
            }
        }

    } catch(PDOException $e) {
        sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
    }
}

// Get reviews for a product
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    if (!isset($_GET['product_id'])) {
        sendResponse(['error' => 'Product ID is required'], 400);
    }

    try {
        $sql = "SELECT r.*, u.name as user_name
                FROM reviews r
                JOIN users u ON r.user_id = u.id
                WHERE r.product_id = ?
                ORDER BY r.created_at DESC";

        $stmt = $pdo->prepare($sql);
        $stmt->execute([$_GET['product_id']]);
        $reviews = $stmt->fetchAll();

        sendResponse($reviews);

    } catch(PDOException $e) {
        sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
    }
}
?>