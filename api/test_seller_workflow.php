<?php
require_once 'config.php';

echo "<h1>Test Seller Workflow</h1>";

// Create a test seller if not exists
echo "<h2>Create Test Seller</h2>";

$testSeller = [
    'name' => 'Test Seller ' . date('YmdHis'),
    'email' => 'seller_' . time() . '@test.com',
    'password' => '123456',
    'role' => 'penjual'
];

// Check if seller exists
$checkSql = "SELECT id FROM users WHERE email = ?";
$checkStmt = $pdo->prepare($checkSql);
$checkStmt->execute([$testSeller['email']]);
$existingSeller = $checkStmt->fetch();

if (!$existingSeller) {
    // Create new seller
    $ch = curl_init('http://localhost/agrostore/api/auth/register.php');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($testSeller));
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    $response = curl_exec($ch);
    curl_close($ch);

    $result = json_decode($response, true);
    if (isset($result['user'])) {
        $sellerId = $result['user']['id'];
        echo "<p style='color: green;'>✓ Created seller: $sellerId ({$testSeller['email']})</p>";
    } else {
        echo "<p style='color: red;'>✗ Failed to create seller: $response</p>";
        exit;
    }
} else {
    $sellerId = $existingSeller['id'];
    echo "<p style='color: blue;'>ℹ Using existing seller: $sellerId</p>";
}

// Login as seller
echo "<h2>Login as Seller</h2>";
$loginData = [
    'email' => $testSeller['email'],
    'password' => '123456'
];

$ch = curl_init('http://localhost/agrostore/api/auth/login.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($loginData));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
curl_close($ch);

$loginResult = json_decode($response, true);
if (isset($loginResult['user'])) {
    echo "<p style='color: green;'>✓ Login successful: {$loginResult['user']['id']} - {$loginResult['user']['name']}</p>";
    $loggedInSellerId = $loginResult['user']['id'];
} else {
    echo "<p style='color: red;'>✗ Login failed: $response</p>";
    exit;
}

// Add product as this seller
echo "<h2>Add Product as Seller</h2>";
$productData = [
    'name' => 'Test Product by ' . $loggedInSellerId,
    'category' => 'Test Category',
    'price' => 100000.0,
    'description' => 'Product added by ' . $loggedInSellerId,
    'stock' => 50,
    'imageUrl' => '',
    'sellerId' => $loggedInSellerId
];

$ch = curl_init('http://localhost/agrostore/api/products/index.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($productData));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
curl_close($ch);

$productResult = json_decode($response, true);
if (isset($productResult['product_id'])) {
    $productId = $productResult['product_id'];
    echo "<p style='color: green;'>✓ Product added: $productId</p>";
} else {
    echo "<p style='color: red;'>✗ Failed to add product: $response</p>";
}

// Verify product belongs to seller
echo "<h2>Verify Product Ownership</h2>";
try {
    $sql = "SELECT id, name, seller_id FROM products WHERE seller_id = ? ORDER BY id DESC LIMIT 5";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$loggedInSellerId]);
    $products = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo "<h3>Products for Seller $loggedInSellerId:</h3>";
    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>ID</th><th>Name</th><th>Seller ID</th></tr>";
    foreach ($products as $product) {
        echo "<tr><td>{$product['id']}</td><td>{$product['name']}</td><td>{$product['seller_id']}</td></tr>";
    }
    echo "</table>";

} catch (Exception $e) {
    echo "<p style='color: red;'>Error: " . $e->getMessage() . "</p>";
}

// Test API call to get products for this seller
echo "<h2>Test API - Get Products for Seller</h2>";
$ch = curl_init("http://localhost/agrostore/api/products/index.php?seller_id=$loggedInSellerId");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "<p>HTTP Code: $httpCode</p>";
$apiProducts = json_decode($response, true);

if ($httpCode == 200 && is_array($apiProducts)) {
    echo "<h3>API Response - Products for Seller $loggedInSellerId:</h3>";
    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>ID</th><th>Name</th><th>Category</th><th>Price</th><th>Seller ID</th></tr>";
    foreach ($apiProducts as $product) {
        echo "<tr>";
        echo "<td>{$product['id']}</td>";
        echo "<td>{$product['name']}</td>";
        echo "<td>{$product['category']}</td>";
        echo "<td>" . number_format($product['price'], 0, ',', '.') . "</td>";
        echo "<td>{$product['seller_id']}</td>";
        echo "</tr>";
    }
    echo "</table>";
} else {
    echo "<p style='color: red;'>Error in API response: $response</p>";
}

// Summary
echo "<h2>Summary</h2>";
echo "<p>Seller ID: $loggedInSellerId</p>";
echo "<p>Seller Email: {$testSeller['email']}</p>";
echo "<p>Password: 123456</p>";
echo "<p><strong>Use this account to test in Android app!</strong></p>";
?>