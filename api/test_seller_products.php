<?php
require_once 'config.php';

echo "<h1>Test Seller Products API</h1>";

// Test all products
echo "<h2>All Products</h2>";
$ch = curl_init('http://localhost/agrostore/api/products/index.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($ch);
curl_close($ch);

$allProducts = json_decode($response, true);
echo "<p>Total Products: " . count($allProducts) . "</p>";

// Group by seller
$groupedProducts = [];
foreach ($allProducts as $product) {
    $sellerId = $product['seller_id'];
    if (!isset($groupedProducts[$sellerId])) {
        $groupedProducts[$sellerId] = [];
    }
    $groupedProducts[$sellerId][] = $product;
}

echo "<h3>Products by Seller:</h3>";
foreach ($groupedProducts as $sellerId => $products) {
    echo "<h4>Seller: $sellerId (" . count($products) . " products)</h4>";
    echo "<ul>";
    foreach ($products as $product) {
        echo "<li>{$product['id']} - {$product['name']} (Rp " . number_format($product['price'], 0, ',', '.') . ")</li>";
    }
    echo "</ul>";
}

// Test specific seller
echo "<h2>Test Specific Seller APIs</h2>";
$sellers = ['seller1', 'seller2', 'seller3'];

foreach ($sellers as $sellerId) {
    echo "<h3>Seller: $sellerId</h3>";

    // Test API with seller_id filter
    $ch = curl_init("http://localhost/agrostore/api/products/index.php?seller_id=$sellerId");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    $sellerProducts = json_decode($response, true);

    echo "<p>HTTP Code: $httpCode</p>";
    echo "<p>Products count: " . count($sellerProducts) . "</p>";

    if ($httpCode != 200) {
        echo "<p style='color: red;'>Error: $response</p>";
    } else {
        echo "<table border='1' cellpadding='5'>";
        echo "<tr><th>ID</th><th>Name</th><th>Category</th><th>Price</th><th>Stock</th></tr>";
        foreach ($sellerProducts as $product) {
            echo "<tr>";
            echo "<td>{$product['id']}</td>";
            echo "<td>{$product['name']}</td>";
            echo "<td>{$product['category']}</td>";
            echo "<td>" . number_format($product['price'], 0, ',', '.') . "</td>";
            echo "<td>{$product['stock']}</td>";
            echo "</tr>";
        }
        echo "</table>";
    }
}

// Test direct database query
echo "<h2>Direct Database Query</h2>";
try {
    $sql = "SELECT id, name, seller_id FROM products ORDER BY seller_id, id";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $dbProducts = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo "<p>Database says there are " . count($dbProducts) . " products</p>";

    $dbGrouped = [];
    foreach ($dbProducts as $product) {
        $dbGrouped[$product['seller_id']][] = $product;
    }

    foreach ($dbGrouped as $sellerId => $products) {
        echo "<h3>Seller: $sellerId</h3>";
        echo "<ul>";
        foreach ($products as $product) {
            echo "<li>{$product['id']} - {$product['name']}</li>";
        }
        echo "</ul>";
    }
} catch (Exception $e) {
    echo "<p style='color: red;'>Database error: " . $e->getMessage() . "</p>";
}

// Test recently added product
echo "<h2>Recently Added Product</h2>";
try {
    $sql = "SELECT * FROM products ORDER BY created_at DESC LIMIT 1";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $latestProduct = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($latestProduct) {
        echo "<h3>Latest Product:</h3>";
        echo "<pre>" . json_encode($latestProduct, JSON_PRETTY_PRINT) . "</pre>";
        echo "<p>Seller ID: {$latestProduct['seller_id']}</p>";
        echo "<p>Created: {$latestProduct['created_at']}</p>";
    }
} catch (Exception $e) {
    echo "<p style='color: red;'>Error: " . $e->getMessage() . "</p>";
}
?>