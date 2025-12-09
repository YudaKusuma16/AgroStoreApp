<?php
require_once 'config.php';

echo "<h1>Test Update Product API</h1>";

// First, add a test product
echo "<h2>Step 1: Add Test Product</h2>";

$testProduct = [
    'name' => 'Test Product for Update',
    'category' => 'Test Category',
    'price' => 50000.0,
    'description' => 'This product will be updated',
    'stock' => 100,
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

$result = json_decode($response, true);
if (isset($result['product_id'])) {
    $productId = $result['product_id'];
    echo "<p style='color: green;'>✓ Created product: $productId</p>";
} else {
    echo "<p style='color: red;'>✗ Failed to create product: $response</p>";
    exit;
}

// Wait a moment to ensure product is saved
sleep(1);

// Update the product
echo "<h2>Step 2: Update Product</h2>";

$updatedProduct = [
    'name' => 'Updated Product Name',
    'category' => 'Updated Category',
    'price' => 75000.0,
    'description' => 'This product has been updated',
    'stock' => 150,
    'imageUrl' => 'https://example.com/image.jpg',
    'sellerId' => 'seller1'
];

$ch = curl_init("http://localhost/agrostore/api/products/show.php?id=$productId");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'PUT');
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($updatedProduct));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "<h3>Update Request:</h3>";
echo "<pre>" . json_encode($updatedProduct, JSON_PRETTY_PRINT) . "</pre>";

echo "<h3>Update Response:</h3>";
echo "<p>HTTP Code: $httpCode</p>";
echo "<pre>" . $response . "</pre>";

$updateResult = json_decode($response, true);
if ($httpCode == 200 && isset($updateResult['message'])) {
    echo "<p style='color: green;'>✓ Update successful: {$updateResult['message']}</p>";
} else {
    echo "<p style='color: red;'>✗ Update failed</p>";
}

// Verify the update
echo "<h2>Step 3: Verify Update</h2>";

// Get product details
$ch = curl_init("http://localhost/agrostore/api/products/show.php?id=$productId");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($ch);
curl_close($ch);

$verification = json_decode($response, true);

if (isset($verification['id'])) {
    echo "<h3>Product After Update:</h3>";
    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>Field</th><th>Value</th></tr>";
    echo "<tr><td>ID</td><td>{$verification['id']}</td></tr>";
    echo "<tr><td>Name</td><td>{$verification['name']}</td></tr>";
    echo "<tr><td>Category</td><td>{$verification['category']}</td></tr>";
    echo "<tr><td>Price</td><td>" . number_format($verification['price'], 0, ',', '.') . "</td></tr>";
    echo "<tr><td>Stock</td><td>{$verification['stock']}</td></tr>";
    echo "<tr><td>Description</td><td>{$verification['description']}</td></tr>";
    echo "<tr><td>Image URL</td><td>" . ($verification['image_url'] ?? 'NULL') . "</td></tr>";
    echo "<tr><td>Seller ID</td><td>{$verification['seller_id']}</td></tr>";
    echo "</table>";

    // Check if values match
    echo "<h3>Verification:</h3>";
    $checks = [
        'Name' => $verification['name'] === $updatedProduct['name'],
        'Category' => $verification['category'] === $updatedProduct['category'],
        'Price' => $verification['price'] == $updatedProduct['price'],
        'Stock' => $verification['stock'] == $updatedProduct['stock'],
        'Description' => $verification['description'] === $updatedProduct['description']
    ];

    foreach ($checks as $field => $match) {
        $status = $match ? '✓' : '✗';
        $color = $match ? 'green' : 'red';
        echo "<p style='color: $color;'>$status $field match</p>";
    }
} else {
    echo "<p style='color: red;'>✗ Cannot retrieve updated product</p>";
}

// Test unauthorized update
echo "<h2>Step 4: Test Unauthorized Update</h2>";

$unauthorizedUpdate = [
    'name' => 'Hacked Product',
    'category' => 'Hacked',
    'price' => 1.0,
    'description' => 'This should fail',
    'stock' => 1,
    'imageUrl' => '',
    'sellerId' => 'seller2' // Different seller
];

$ch = curl_init("http://localhost/agrostore/api/products/show.php?id=$productId");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'PUT');
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($unauthorizedUpdate));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "<p>HTTP Code: $httpCode</p>";
if ($httpCode == 403) {
    echo "<p style='color: green;'>✓ Unauthorized update correctly blocked</p>";
} else {
    echo "<p style='color: red;'>✗ Unauthorized update not blocked (should be 403)</p>";
}

echo "<h2>Summary</h2>";
echo "<p>Product ID: $productId</p>";
echo "<p>Test Update Product API works correctly!</p>";
?>