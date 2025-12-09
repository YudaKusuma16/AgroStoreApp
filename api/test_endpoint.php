<?php
// This script tests if the API endpoint is accessible via HTTP

$url = 'http://localhost/agrostore/api/orders/index.php';

// First, create a test product via cURL
$testProduct = [
    'name' => 'Test Product with 3 Stock',
    'category' => 'Test Category',
    'price' => 100.0,
    'description' => 'Test product with limited stock',
    'stock' => 3,
    'imageUrl' => '',
    'sellerId' => 'seller1'
];

$ch = curl_init('http://localhost/agrostore/api/products/index.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($testProduct));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "Product Creation Response:\n";
echo "HTTP Code: $httpCode\n";
echo "Response: $response\n\n";

$result = json_decode($response, true);
if (isset($result['product_id'])) {
    $productId = $result['product_id'];
    echo "Created product: $productId\n\n";

    // Now test order creation
    $orderData = [
        'user_id' => 'user1',
        'items' => [
            [
                'product_id' => $productId,
                'quantity' => 5, // More than available
                'price' => 100.0
            ]
        ],
        'total' => 500.0,
        'shipping_address' => 'Test Address',
        'payment_method' => 'Transfer Bank'
    ];

    echo "Attempting to create order with quantity 5 (stock is 3)...\n\n";

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($orderData));
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true); // Follow redirects
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $error = curl_error($ch);
    curl_close($ch);

    echo "Order Creation Response:\n";
    echo "HTTP Code: $httpCode\n";
    if ($error) {
        echo "CURL Error: $error\n";
    }
    echo "Response: $response\n\n";

    // Check if order was created (which it shouldn't be)
    $orderResult = json_decode($response, true);
    if ($httpCode == 200 && isset($orderResult['order_id'])) {
        echo "❌ FAILED: Order was created despite insufficient stock!\n";
    } else {
        echo "✅ SUCCESS: Order was properly blocked!\n";
    }

    // Clean up
    $ch = curl_init("http://localhost/agrostore/api/products/show.php?id=$productId&seller_id=seller1");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'DELETE');
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    curl_exec($ch);
    curl_close($ch);
} else {
    echo "❌ Failed to create test product\n";
}
?>