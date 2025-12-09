<?php
require_once 'config.php';

// First create a test product
$productId = 'testapiproduct';
$stmt = $pdo->prepare("INSERT INTO products (id, name, category, price, description, stock, seller_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
$stmt->execute([
    $productId,
    'Test API Product',
    'Test Category',
    50000.0,
    'Test Description',
    5,
    'seller1'
]);

echo "Created test product with 5 stock\n";

// Test the order creation locally without cURL
$_SERVER['REQUEST_METHOD'] = 'POST';

// Create a temporary file with the JSON data
$jsonData = json_encode([
    'user_id' => 'user1',
    'items' => [
        [
            'product_id' => $productId,
            'quantity' => 6,
            'price' => 50000.0
        ]
    ],
    'total' => 300000.0,
    'shipping_address' => 'Test Address',
    'payment_method' => 'Transfer Bank'
]);

// Save to temp file and read it as php://input
file_put_contents('temp_input.json', $jsonData);

// Override php://input stream
$stream = fopen('php://memory', 'r+');
fwrite($stream, $jsonData);
rewind($stream);

// Now include the orders API
$backup = $_SERVER['REQUEST_METHOD'];
include 'orders/index.php';

// Clean up
unlink('temp_input.json');

// Check if product still exists
$stmt = $pdo->prepare("SELECT stock FROM products WHERE id = ?");
$stmt->execute([$productId]);
$remainingStock = $stmt->fetchColumn();

echo "\nRemaining stock after order attempt: $remainingStock\n";
?>