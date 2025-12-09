<?php
// Directly test the orders API
$_SERVER['REQUEST_METHOD'] = 'POST';

// Simulate POST data
$json_data = json_encode([
    'user_id' => 'user1',
    'items' => [
        [
            'product_id' => 'prod16',
            'quantity' => 6,
            'price' => 50000.0
        ]
    ],
    'total' => 300000.0,
    'shipping_address' => 'Test Address',
    'payment_method' => 'Transfer Bank'
]);

// Override php://input
file_put_contents('php://memory', $json_data);

// Include the orders API
include 'orders/index.php';
?>