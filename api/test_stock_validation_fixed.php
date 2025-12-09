<?php
require_once 'config.php';

echo "<h1>Test Stock Validation</h1>";

// Create test product with limited stock
echo "<h2>Step 1: Create Test Product with Limited Stock</h2>";

$testProduct = [
    'name' => 'Test Product with 5 Stock',
    'category' => 'Test Category',
    'price' => 50000.0,
    'description' => 'This product has only 5 units in stock',
    'stock' => 5,
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
    echo "<p style='color: green;'>✓ Created product: $productId with stock: {$testProduct['stock']}</p>";
} else {
    echo "<p style='color: red;'>✗ Failed to create product: $response</p>";
    exit;
}

// Wait a moment
sleep(1);

// Test 1: Try to add 6 items to cart (should fail)
echo "<h2>Test 1: Adding More Items Than Stock</h2>";
echo "<p>Creating order with 6 items (stock only 5)...</p>";

// Send as single item with quantity 6
$cartItems = [
    [
        'product_id' => $productId,
        'quantity' => 6,  // Requesting 6 units when only 5 available
        'price' => 50000.0
    ]
];

$cartRequest = [
    'user_id' => 'user1',
    'items' => $cartItems,
    'total' => 50000.0 * 6,
    'shipping_address' => 'Test Address',
    'payment_method' => 'Transfer Bank'
];

$ch = curl_init('http://localhost/agrostore/api/orders/index.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($cartRequest));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "<h3>Checkout Response:</h3>";
echo "<p>HTTP Code: $httpCode</p>";
echo "<pre>" . $response . "</pre>";

$orderResult = json_decode($response, true);
if ($httpCode == 200 && isset($orderResult['order_id'])) {
    echo "<p style='color: red;'>✗ Order created despite insufficient stock! (This should fail)</p>";
} else {
    echo "<p style='color: green;'>✓ Order correctly blocked due to insufficient stock</p>";
}

// Summary
echo "<h2>Summary</h2>";
echo "<ul>";
echo "<li>Stock validation should prevent checkout when cart quantity exceeds available stock</li>";
echo "<li>Android app should show error dialog with details about insufficient stock</li>";
echo "<li>After successful checkout, product stock should be automatically deducted</li>";
echo "<li>Cart should handle real-time stock updates</li>";
echo "</ul>";

// Clean up test product
echo "<h2>Cleanup</h2>";
$ch = curl_init("http://localhost/agrostore/api/products/show.php?id=$productId&seller_id=seller1");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'DELETE');
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
curl_close($ch);

echo "<p>Test product deleted: " . ($response ? "Success" : "Failed") . "</p>";
?>