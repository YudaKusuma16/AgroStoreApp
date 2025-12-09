<?php
require_once 'config.php';
require_once 'helpers/id_generator.php';

echo "<h1>Test ID Generation</h1>";

// Test user ID generation
echo "<h2>User ID Generation</h2>";
echo "<h3>Generate Pembeli ID:</h3>";
for ($i = 1; $i <= 3; $i++) {
    $userId = generateUserId('pembeli');
    echo "<p>Generated: " . $userId . "</p>";
}

echo "<h3>Generate Penjual ID:</h3>";
for ($i = 1; $i <= 3; $i++) {
    $userId = generateUserId('penjual');
    echo "<p>Generated: " . $userId . "</p>";
}

// Test product ID generation
echo "<h2>Product ID Generation</h2>";
for ($i = 1; $i <= 5; $i++) {
    $productId = generateProductId();
    echo "<p>Generated: " . $productId . "</p>";
}

// Test order ID generation
echo "<h2>Order ID Generation</h2>";
for ($i = 1; $i <= 5; $i++) {
    $orderId = generateOrderId();
    echo "<p>Generated: " . $orderId . "</p>";
}

// Test review ID generation
echo "<h2>Review ID Generation</h2>";
for ($i = 1; $i <= 5; $i++) {
    $reviewId = generateReviewId();
    echo "<p>Generated: " . $reviewId . "</p>";
}

// Test with actual data
echo "<h2>Test with Registration</h2>";

// Test registration
$testUser = [
    'name' => 'Test User',
    'email' => 'test' . time() . '@example.com',
    'password' => '123456',
    'role' => 'pembeli'
];

$ch = curl_init('http://localhost/agrostore/api/auth/register.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($testUser));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
curl_close($ch);

echo "<h3>Register New User:</h3>";
echo "<p>Request: " . json_encode($testUser) . "</p>";
echo "<p>Response: " . $response . "</p>";

// Test add product
$testProduct = [
    'name' => 'Test Product ' . time(),
    'category' => 'Test Category',
    'price' => 10000.0,
    'description' => 'Test product description',
    'stock' => 50,
    'imageUrl' => '',
    'sellerId' => 'seller1'
];

$ch = curl_init('http://localhost/agrostore/api/products/index.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($testProduct));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
curl_close($ch);

echo "<h3>Add New Product:</h3>";
echo "<p>Request: " . json_encode($testProduct) . "</p>";
echo "<p>Response: " . $response . "</p>";

// Check current IDs in database
echo "<h2>Current IDs in Database</h2>";

try {
    // Users
    echo "<h3>Users:</h3>";
    $stmt = $pdo->query("SELECT id, role FROM users ORDER BY id");
    while ($user = $stmt->fetch()) {
        echo "<p>{$user['id']} - {$user['role']}</p>";
    }

    // Products
    echo "<h3>Products:</h3>";
    $stmt = $pdo->query("SELECT id, name FROM products ORDER BY id LIMIT 10");
    while ($product = $stmt->fetch()) {
        echo "<p>{$product['id']} - {$product['name']}</p>";
    }

} catch (Exception $e) {
    echo "<p style='color: red;'>Error: " . $e->getMessage() . "</p>";
}
?>