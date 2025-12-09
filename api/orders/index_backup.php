<?php
require_once '../config.php';
require_once '../helpers/id_generator.php';

// Get user's orders
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    if (!isset($_GET['user_id'])) {
        sendResponse(['error' => 'User ID is required'], 400);
    }

    try {
        $sql = "SELECT o.*, oi.product_id, oi.quantity, oi.price as item_price,
                       p.name as product_name, p.image_url as product_image
                FROM orders o
                LEFT JOIN order_items oi ON o.id = oi.order_id
                LEFT JOIN products p ON oi.product_id = p.id
                WHERE o.user_id = ?
                ORDER BY o.created_at DESC";

        $stmt = $pdo->prepare($sql);
        $stmt->execute([$_GET['user_id']]);
        $results = $stmt->fetchAll();

        // Group order items
        $orders = [];
        foreach ($results as $row) {
            $orderId = $row['id'];

            if (!isset($orders[$orderId])) {
                $orders[$orderId] = [
                    'id' => $row['id'],
                    'total' => $row['total'],
                    'status' => $row['status'],
                    'shipping_address' => $row['shipping_address'],
                    'payment_method' => $row['payment_method'],
                    'created_at' => $row['created_at'],
                    'items' => []
                ];
            }

            if ($row['product_id']) {
                $orders[$orderId]['items'][] = [
                    'product_id' => $row['product_id'],
                    'product_name' => $row['product_name'],
                    'product_image' => $row['product_image'],
                    'quantity' => $row['quantity'],
                    'price' => $row['item_price']
                ];
            }
        }

        sendResponse(array_values($orders));

    } catch(PDOException $e) {
        sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
    }
}

// Create new order
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $data = json_decode(file_get_contents('php://input'), true);

        // Validate required fields
        if (!validateRequired($data, ['user_id', 'items', 'total', 'shipping_address', 'payment_method'])) {
            sendResponse(['error' => 'Missing required fields'], 400);
        }

        // Start transaction
        $pdo->beginTransaction();

        // Check stock availability for all items
        $outOfStockItems = [];
        error_log("VALIDATION CHECK - Checking stock for " . count($data['items']) . " items");
        file_put_contents('debug.log', "VALIDATION CHECK - " . json_encode($data) . "\n", FILE_APPEND);
        foreach ($data['items'] as $item) {
            error_log("Checking product {$item['product_id']}, quantity: {$item['quantity']}");
            $productSql = "SELECT id, name, stock FROM products WHERE id = ? FOR UPDATE";
            $productStmt = $pdo->prepare($productSql);
            $productStmt->execute([$item['product_id']]);
            $product = $productStmt->fetch();

            if (!$product) {
                $outOfStockItems[] = "Product ID {$item['product_id']} not found";
                error_log("Product not found: {$item['product_id']}");
            } elseif ($product['stock'] < $item['quantity']) {
                $outOfStockItems[] = "{$product['name']}: Requested {$item['quantity']}, Available {$product['stock']}";
                error_log("Insufficient stock for {$product['name']}: {$product['stock']} < {$item['quantity']}");
            } else {
                error_log("Stock OK for {$product['name']}: {$product['stock']} >= {$item['quantity']}");
            }
        }

        if (!empty($outOfStockItems)) {
            error_log("Stock validation failed: " . implode(", ", $outOfStockItems));
            $pdo->rollBack();
            sendResponse([
                'error' => 'Stock tidak mencukupi',
                'details' => $outOfStockItems
            ], 400);
        }

        error_log("Stock validation passed, proceeding with order creation");

        // Generate readable order ID
        $orderId = generateOrderId();

        // Insert order
        $orderSql = "INSERT INTO orders (id, user_id, total, status, shipping_address, payment_method)
                     VALUES (?, ?, ?, 'Dibayar', ?, ?)";

        $orderStmt = $pdo->prepare($orderSql);
        $orderResult = $orderStmt->execute([
            $orderId,
            $data['user_id'],
            $data['total'],
            $data['shipping_address'],
            $data['payment_method']
        ]);

        if (!$orderResult) {
            throw new Exception('Failed to create order');
        }

        // Insert order items
        $itemSql = "INSERT INTO order_items (order_id, product_id, quantity, price)
                    VALUES (?, ?, ?, ?)";
        $itemStmt = $pdo->prepare($itemSql);

        foreach ($data['items'] as $item) {
            $itemResult = $itemStmt->execute([
                $orderId,
                $item['product_id'],
                $item['quantity'],
                $item['price']
            ]);

            if (!$itemResult) {
                throw new Exception('Failed to add order item');
            }

            // Update product stock
            $updateStockSql = "UPDATE products SET stock = stock - ? WHERE id = ?";
            $updateStockStmt = $pdo->prepare($updateStockSql);
            $updateStockStmt->execute([$item['quantity'], $item['product_id']]);
        }

        // Commit transaction
        $pdo->commit();

        sendResponse([
            'message' => 'Order created successfully',
            'order_id' => $orderId
        ]);

    } catch(Exception $e) {
        // Rollback transaction
        $pdo->rollback();
        sendResponse(['error' => $e->getMessage()], 500);
    } catch(PDOException $e) {
        $pdo->rollback();
        sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
    }
}
?>