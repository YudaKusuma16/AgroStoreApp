<?php
require_once '../config.php';

/**
 * Generate user ID based on role
 * Format: [role][sequence_number]
 * Examples: user1, user2, seller1, seller2
 */
function generateUserId($role) {
    global $pdo;

    $prefix = $role === 'penjual' ? 'seller' : 'user';

    // Get the maximum sequence number for the role
    $sql = "SELECT
                CASE
                    WHEN id REGEXP '^{$prefix}[0-9]+$' THEN CAST(SUBSTRING(id, " . (strlen($prefix) + 1) . ") AS UNSIGNED)
                    ELSE 0
                END as sequence_num
            FROM users
            WHERE id REGEXP '^{$prefix}[0-9]+$'
            ORDER BY sequence_num DESC
            LIMIT 1";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $result = $stmt->fetch();

    if ($result && $result['sequence_num'] > 0) {
        $newSequence = $result['sequence_num'] + 1;
    } else {
        $newSequence = 1;
    }

    $newId = $prefix . $newSequence;

    // Double check if ID already exists (prevent race conditions)
    $checkSql = "SELECT id FROM users WHERE id = ?";
    $checkStmt = $pdo->prepare($checkSql);
    $checkStmt->execute([$newId]);

    if ($checkStmt->fetch()) {
        // If still exists, try with a higher sequence
        return generateUserId($role); // Recursive call
    }

    return $newId;
}

/**
 * Generate product ID
 * Format: prod[sequence_number]
 * Examples: prod1, prod2, prod3
 */
function generateProductId() {
    global $pdo;

    // Get the maximum sequence number
    $sql = "SELECT
                CASE
                    WHEN id REGEXP '^prod[0-9]+$' THEN CAST(SUBSTRING(id, 5) AS UNSIGNED)
                    ELSE 0
                END as sequence_num
            FROM products
            WHERE id REGEXP '^prod[0-9]+$'
            ORDER BY sequence_num DESC
            LIMIT 1";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $result = $stmt->fetch();

    if ($result && $result['sequence_num'] > 0) {
        $newSequence = $result['sequence_num'] + 1;
    } else {
        $newSequence = 1;
    }

    $newId = 'prod' . $newSequence;

    // Double check if ID already exists (prevent race conditions)
    $checkSql = "SELECT id FROM products WHERE id = ?";
    $checkStmt = $pdo->prepare($checkSql);
    $checkStmt->execute([$newId]);

    if ($checkStmt->fetch()) {
        // If still exists, try with a higher sequence
        return generateProductId(); // Recursive call with new sequence
    }

    return $newId;
}

/**
 * Generate order ID
 * Format: order[sequence_number]
 * Examples: order1, order2, order3
 */
function generateOrderId() {
    global $pdo;

    // Get the maximum order sequence
    $sql = "SELECT
                CASE
                    WHEN id REGEXP '^order[0-9]+$' THEN CAST(SUBSTRING(id, 6) AS UNSIGNED)
                    ELSE 0
                END as sequence_num
            FROM orders
            WHERE id REGEXP '^order[0-9]+$'
            ORDER BY sequence_num DESC
            LIMIT 1";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $result = $stmt->fetch();

    if ($result && $result['sequence_num'] > 0) {
        $newSequence = $result['sequence_num'] + 1;
    } else {
        $newSequence = 1;
    }

    $newId = 'order' . $newSequence;

    // Double check if ID already exists
    $checkSql = "SELECT id FROM orders WHERE id = ?";
    $checkStmt = $pdo->prepare($checkSql);
    $checkStmt->execute([$newId]);

    if ($checkStmt->fetch()) {
        return generateOrderId(); // Recursive call
    }

    return $newId;
}

/**
 * Generate review ID
 * Format: rev[sequence_number]
 * Examples: rev1, rev2, rev3
 */
function generateReviewId() {
    global $pdo;

    // Get the maximum review sequence
    $sql = "SELECT
                CASE
                    WHEN id REGEXP '^rev[0-9]+$' THEN CAST(SUBSTRING(id, 4) AS UNSIGNED)
                    ELSE 0
                END as sequence_num
            FROM reviews
            WHERE id REGEXP '^rev[0-9]+$'
            ORDER BY sequence_num DESC
            LIMIT 1";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $result = $stmt->fetch();

    if ($result && $result['sequence_num'] > 0) {
        $newSequence = $result['sequence_num'] + 1;
    } else {
        $newSequence = 1;
    }

    $newId = 'rev' . $newSequence;

    // Double check if ID already exists
    $checkSql = "SELECT id FROM reviews WHERE id = ?";
    $checkStmt = $pdo->prepare($checkSql);
    $checkStmt->execute([$newId]);

    if ($checkStmt->fetch()) {
        return generateReviewId(); // Recursive call
    }

    return $newId;
}
?>