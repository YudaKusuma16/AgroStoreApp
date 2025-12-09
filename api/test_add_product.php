<?php
// Test file untuk menambah produk
// Buka di browser: http://localhost/agrostore/api/test_add_product.php

require_once 'config.php';

// Sample product data
$productData = [
    'id' => 'test_product_123',
    'name' => 'Test Product',
    'category' => 'Pupuk',
    'price' => 50000.0,
    'description' => 'Ini adalah produk test',
    'stock' => 100,
    'imageUrl' => '',
    'sellerId' => 'seller1'
];

echo "<h2>Testing Add Product API</h2>";
echo "<h3>Sending Data:</h3>";
echo "<pre>" . json_encode($productData, JSON_PRETTY_PRINT) . "</pre>";

// Convert to JSON
$jsonData = json_encode($productData);

// Use cURL to send POST request
$ch = curl_init('http://localhost/agrostore/api/products/index.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, $jsonData);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Content-Type: application/json',
    'Content-Length: ' . strlen($jsonData)
]);
curl_setopt($ch, CURLOPT_VERBOSE, true);
$verbose = fopen('php://temp', 'w+');
curl_setopt($ch, CURLOPT_STDERR, $verbose);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$curlError = curl_error($ch);
curl_close($ch);

// Get verbose info
rewind($verbose);
$verboseLog = stream_get_contents($verbose);

echo "<h3>cURL Debug Info:</h3>";
echo "<p>cURL Error: " . ($curlError ?: 'None') . "</p>";
echo "<pre>" . htmlspecialchars($verboseLog) . "</pre>";

echo "<h3>Response (HTTP Code: $httpCode):</h3>";
echo "<pre>" . $response . "</pre>";

// Decode response
$responseData = json_decode($response, true);

if (isset($responseData['error'])) {
    echo "<h3 style='color: red;'>Error: " . $responseData['error'] . "</h3>";
} elseif (isset($responseData['message'])) {
    echo "<h3 style='color: green;'>Success: " . $responseData['message'] . "</h3>";
}
?>