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
echo "<p>Creating cart with 6 items (stock only 5)...</p>";

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

// Test 2: Update product stock
echo "<h2>Test 2: Update Product Stock</h2>";

// Update product to have 10 units
$updateStock = [
    'id' => $productId,
    'name' => 'Test Product Updated',
    'category' => 'Test Category',
    'price' => 50000.0,
    'description' => 'Updated product with more stock',
    'stock' => 10,
    'imageUrl' => 'https://example.com/image.jpg',
    'sellerId' => 'seller1'
];

$ch = curl_init("http://localhost/agrostore/api/products/show.php?id=$productId");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'PUT');
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($updateStock));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "<p>HTTP Code: $httpCode</p>";
echo "<pre>" . $response . "</pre>";

if ($httpCode == 200) {
    echo "<p style='color: green;'>✓ Stock updated to 10 units</p>";
} else {
    echo "<p style='color: red;'>✗ Failed to update stock</p>";
}

// Test 3: Check updated product
echo "<h2>Test 3: Verify Updated Product</h2>";

$ch = curl_init("http://localhost/agrostore/api/products/show.php?id=$productId");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($ch);
curl_close($ch);

$updatedProduct = json_decode($response, true);
if (isset($updatedProduct['stock'])) {
    echo "<p>Current stock: {$updatedProduct['stock']}</p>";
}

// Test 4: Try checkout with adequate stock
echo "<h2>Test 4: Checkout with Adequate Stock</h2>";

// Send as single item with quantity 8 (stock is now 10)
$cartItems = [
    [
        'product_id' => $productId,
        'quantity' => 8,
        'price' => 50000.0
    ]
];

$cartRequest = [
    'user_id' => 'user1',
    'items' => $cartItems,
    'total' => 50000.0 * 8,
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
    echo "<p style='color: green;'>✓ Order created successfully with ID: {$orderResult['order_id']}</p>";

    // Update product stock after checkout
    echo "<h3>Stock After Checkout:</h3>";
    $ch = curl_init("http://localhost/agrostore/api/products/show.php?id=$productId");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    curl_close($ch);

    $finalProduct = json_decode($response, true);
    if (isset($finalProduct['stock'])) {
        echo "<p>Remaining stock: {$finalProduct['stock']}</p>";
        $expectedStock = 10 - 8; // 10 initial - 8 purchased
        if ($finalProduct['stock'] == $expectedStock) {
            echo "<p style='color: green;'>✓ Stock correctly deducted</p>";
        } else {
            echo "<p style='color: orange;'>⚠ Stock mismatch. Expected: $expectedStock, Actual: {$finalProduct['stock']}</p>";
        }
    }
} else {
    echo "<p style='color: red;'>✗ Failed to create order</p>";
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