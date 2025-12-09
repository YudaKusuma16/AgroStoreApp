<?php
require_once 'config.php';

echo "<h1>Test Null Image URL Handling</h1>";

// Test 1: Add product with null image_url
echo "<h2>Test 1: Add Product with Null Image URL</h2>";

$productWithNullImage = [
    'name' => 'Product with Null Image',
    'category' => 'Test',
    'price' => 10000.0,
    'description' => 'This product has null image_url',
    'stock' => 10,
    'imageUrl' => null,
    'sellerId' => 'seller1'
];

$ch = curl_init('http://localhost/agrostore/api/products/index.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($productWithNullImage));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
curl_close($ch);

echo "<h3>Request:</h3>";
echo "<pre>" . json_encode($productWithNullImage, JSON_PRETTY_PRINT) . "</pre>";

echo "<h3>Response:</h3>";
echo "<pre>" . $response . "</pre>";

// Test 2: Add product with empty image_url
echo "<h2>Test 2: Add Product with Empty Image URL</h2>";

$productWithEmptyImage = [
    'name' => 'Product with Empty Image',
    'category' => 'Test',
    'price' => 20000.0,
    'description' => 'This product has empty image_url',
    'stock' => 20,
    'imageUrl' => '',
    'sellerId' => 'seller1'
];

$ch = curl_init('http://localhost/agrostore/api/products/index.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($productWithEmptyImage));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$response = curl_exec($ch);
curl_close($ch);

echo "<h3>Request:</h3>";
echo "<pre>" . json_encode($productWithEmptyImage, JSON_PRETTY_PRINT) . "</pre>";

echo "<h3>Response:</h3>";
echo "<pre>" . $response . "</pre>";

// Test 3: Get all products and check image_url field
echo "<h2>Test 3: Get All Products</h2>";

$ch = curl_init('http://localhost/agrostore/api/products/index.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($ch);
curl_close($ch);

$products = json_decode($response, true);

echo "<h3>All Products (checking image_url field):</h3>";
echo "<table border='1' cellpadding='5'>";
echo "<tr><th>ID</th><th>Name</th><th>Image URL</th><th>Image URL Type</th></tr>";

foreach ($products as $product) {
    $imageUrl = $product['image_url'];
    $type = gettype($imageUrl);
    $isNull = is_null($imageUrl) ? 'NULL' : 'NOT NULL';
    $isEmpty = $imageUrl === '' ? 'EMPTY' : 'NOT EMPTY';

    echo "<tr>";
    echo "<td>{$product['id']}</td>";
    echo "<td>{$product['name']}</td>";
    echo "<td>" . (is_null($imageUrl) ? '<span style="color:red;">NULL</span>' : htmlspecialchars($imageUrl)) . "</td>";
    echo "<td>$type / $isNull / $isEmpty</td>";
    echo "</tr>";
}
echo "</table>";

// Test 4: Check database directly
echo "<h2>Test 4: Check Database Directly</h2>";

try {
    $sql = "SELECT id, name, image_url FROM products ORDER BY id DESC LIMIT 5";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $dbProducts = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>ID</th><th>Name</th><th>Image URL (DB)</th><th>Is NULL?</th></tr>";

    foreach ($dbProducts as $product) {
        $isNull = $product['image_url'] === null ? 'YES' : 'NO';
        echo "<tr>";
        echo "<td>{$product['id']}</td>";
        echo "<td>{$product['name']}</td>";
        echo "<td>" . ($product['image_url'] === null ? '<span style="color:red;">NULL</span>' : htmlspecialchars($product['image_url'])) . "</td>";
        echo "<td>$isNull</td>";
        echo "</tr>";
    }
    echo "</table>";

} catch (Exception $e) {
    echo "<p style='color: red;'>Error: " . $e->getMessage() . "</p>";
}

echo "<h2>Summary</h2>";
echo "<p>The API should handle null image_url gracefully and convert it to empty string in the response.</p>";
echo "<p>Android app should use empty string as default value for imageUrl field.</p>";
?>