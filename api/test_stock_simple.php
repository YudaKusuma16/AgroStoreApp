<?php
require_once 'config.php';

// First, let's directly check if product with limited stock exists
$stmt = $pdo->prepare("SELECT id, name, stock FROM products WHERE id = ?");
$stmt->execute(['prod16']);
$product = $stmt->fetch();

if ($product) {
    echo "Found product: {$product['name']} with stock: {$product['stock']}\n";

    // Simulate the order creation data
    $data = [
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
    ];

    echo "\nSimulating stock check...\n";

    // Check stock
    $productSql = "SELECT id, name, stock FROM products WHERE id = ? FOR UPDATE";
    $productStmt = $pdo->prepare($productSql);
    $productStmt->execute([$data['items'][0]['product_id']]);
    $productCheck = $productStmt->fetch();

    if (!$productCheck) {
        echo "Product not found\n";
    } elseif ($productCheck['stock'] < $data['items'][0]['quantity']) {
        echo "STOCK INSUFFICIENT: Available {$productCheck['stock']}, Requested {$data['items'][0]['quantity']}\n";
    } else {
        echo "Stock OK: Available {$productCheck['stock']}, Requested {$data['items'][0]['quantity']}\n";
    }
} else {
    echo "Product prod16 not found\n";
}
?>