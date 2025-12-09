<?php
require_once 'config.php';

echo "<h1>Test Products API</h1>";

// Test get all products
echo "<h2>Get All Products</h2>";
$ch = curl_init('http://localhost/agrostore/api/products/index.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($ch);
curl_close($ch);

$products = json_decode($response, true);
echo "<pre>" . json_encode($products, JSON_PRETTY_PRINT) . "</pre>";
echo "<p>Found " . count($products) . " products</p>";

// Test get products by seller
echo "<h2>Get Products by Seller ID</h2>";
$sellers = ['seller1', 'seller2', 'seller3'];

foreach ($sellers as $sellerId) {
    echo "<h3>Seller: $sellerId</h3>";
    $ch = curl_init("http://localhost/agrostore/api/products/index.php?seller_id=$sellerId");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    curl_close($ch);

    $sellerProducts = json_decode($response, true);
    echo "<p>Found " . count($sellerProducts) . " products</p>";

    // Show product IDs
    $productIds = array_map(function($p) { return $p['id']; }, $sellerProducts);
    echo "<p>Product IDs: " . implode(', ', $productIds) . "</p>";
}

// Test direct database query
echo "<h2>Direct Database Query</h2>";
try {
    $sql = "SELECT id, name, seller_id FROM products ORDER BY seller_id, name";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $dbProducts = $stmt->fetchAll();

    // Group by seller
    $grouped = [];
    foreach ($dbProducts as $product) {
        $grouped[$product['seller_id']][] = $product;
    }

    foreach ($grouped as $sellerId => $products) {
        echo "<h3>Seller $sellerId (" . count($products) . " products)</h3>";
        echo "<ul>";
        foreach ($products as $product) {
            echo "<li>{$product['id']} - {$product['name']}</li>";
        }
        echo "</ul>";
    }

} catch (Exception $e) {
    echo "<p style='color: red;'>Error: " . $e->getMessage() . "</p>";
}

// Check if there are any products with different seller IDs
echo "<h2>Check Seller IDs</h2>";
try {
    $sql = "SELECT DISTINCT seller_id, COUNT(*) as product_count FROM products GROUP BY seller_id";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $sellers = $stmt->fetchAll();

    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>Seller ID</th><th>Product Count</th></tr>";
    foreach ($sellers as $seller) {
        echo "<tr><td>{$seller['seller_id']}</td><td>{$seller['product_count']}</td></tr>";
    }
    echo "</table>";

} catch (Exception $e) {
    echo "<p style='color: red;'>Error: " . $e->getMessage() . "</p>";
}
?>