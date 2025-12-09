<?php
require_once 'config.php';

echo "<h1>Fix Product IDs</h1>";

try {
    // Show current products
    echo "<h2>Current Products:</h2>";
    $sql = "SELECT id, name FROM products ORDER BY id";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $products = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>Current ID</th><th>Name</th></tr>";
    foreach ($products as $product) {
        echo "<tr><td>{$product['id']}</td><td>{$product['name']}</td></tr>";
    }
    echo "</table>";

    // Fix IDs
    echo "<h2>Fixing Product IDs...</h2>";

    // Get all products that don't have prodXXX format
    $sql = "SELECT id, name, seller_id, category, price, description, stock, image_url, created_at
            FROM products
            WHERE id NOT REGEXP '^prod[0-9]+$'
            ORDER BY id";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $productsToFix = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo "<p>Found " . count($productsToFix) . " products to fix</p>";

    foreach ($productsToFix as $index => $product) {
        // Generate new ID
        $sql = "SELECT MAX(CAST(SUBSTRING(id, 5) AS UNSIGNED)) as max_seq
                FROM products
                WHERE id REGEXP '^prod[0-9]+$'";
        $stmt = $pdo->prepare($sql);
        $stmt->execute();
        $result = $stmt->fetch();
        $newSeq = ($result['max_seq'] ?? 0) + $index + 1;
        $newId = 'prod' . $newSeq;

        echo "<p>Updating: {$product['id']} -> $newId ({$product['name']})</p>";

        // Update the product
        $updateSql = "UPDATE products SET id = ? WHERE id = ?";
        $updateStmt = $pdo->prepare($updateSql);
        $updateStmt->execute([$newId, $product['id']]);

        // Also update order_items if they reference this product
        $updateItemsSql = "UPDATE order_items SET product_id = ? WHERE product_id = ?";
        $updateItemsStmt = $pdo->prepare($updateItemsSql);
        $updateItemsStmt->execute([$newId, $product['id']]);

        // Also update cart if they reference this product
        $updateCartSql = "UPDATE cart SET product_id = ? WHERE product_id = ?";
        $updateCartStmt = $pdo->prepare($updateCartSql);
        $updateCartStmt->execute([$newId, $product['id']]);
    }

    echo "<p style='color: green;'>✓ Product IDs fixed successfully!</p>";

    // Show fixed products
    echo "<h2>Products After Fix:</h2>";
    $sql = "SELECT id, name FROM products ORDER BY id";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $products = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>ID</th><th>Name</th></tr>";
    foreach ($products as $product) {
        echo "<tr><td>{$product['id']}</td><td>{$product['name']}</td></tr>";
    }
    echo "</table>";

} catch (Exception $e) {
    echo "<p style='color: red;'>Error: " . $e->getMessage() . "</p>";
}

echo "<h2>Test Adding New Product</h2>";

// Test adding a new product
$testProduct = [
    'name' => 'Test Product ' . date('Y-m-d H:i:s'),
    'category' => 'Test',
    'price' => 100.00,
    'description' => 'Test description',
    'stock' => 10,
    'imageUrl' => '',
    'sellerId' => 'seller1'
];

// Use cURL to test add product
$ch = curl_init('http://localhost/agrostore/api/products/index.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($testProduct));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "<p>HTTP Code: $httpCode</p>";
echo "<p>Response: $response</p>";

$responseData = json_decode($response, true);
if (isset($responseData['product_id'])) {
    echo "<p style='color: green;'>✓ New product added with ID: {$responseData['product_id']}</p>";
}
?>