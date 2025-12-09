<?php
require_once 'config.php';

echo "<h1>Simple Database Test</h1>";

// Test database connection
if ($pdo) {
    echo "<p style='color: green;'>✓ Database connected successfully</p>";
} else {
    echo "<p style='color: red;'>✗ Database connection failed</p>";
    exit;
}

// Test adding product directly
echo "<h2>Direct SQL Insert Test</h2>";

try {
    $sql = "INSERT INTO products (id, name, category, price, description, stock, image_url, seller_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    $stmt = $pdo->prepare($sql);
    $result = $stmt->execute([
        'test_' . uniqid(),
        'Simple Test Product',
        'Test Category',
        100.00,
        'Test description from simple test',
        50,
        '',
        'seller1'
    ]);

    if ($result) {
        echo "<p style='color: green;'>✓ Product inserted successfully!</p>";
    } else {
        echo "<p style='color: red;'>✗ Failed to insert product</p>";
    }

    // Verify insertion
    $checkSql = "SELECT * FROM products WHERE name LIKE 'Simple Test Product%'";
    $checkStmt = $pdo->prepare($checkSql);
    $checkStmt->execute();
    $products = $checkStmt->fetchAll();

    echo "<h3>Found " . count($products) . " test products</h3>";
    foreach ($products as $product) {
        echo "<pre>" . json_encode($product, JSON_PRETTY_PRINT) . "</pre>";
    }

} catch (Exception $e) {
    echo "<p style='color: red;'>Error: " . $e->getMessage() . "</p>";
}

echo "<h2>JSON Decode Test</h2>";

$jsonString = '{"name":"Test","category":"Pupuk","price":50000,"description":"Test","stock":100,"imageUrl":"","sellerId":"seller1"}';
$data = json_decode($jsonString, true);

if ($data) {
    echo "<p style='color: green;'>✓ JSON decoded successfully</p>";
    echo "<pre>" . json_encode($data, JSON_PRETTY_PRINT) . "</pre>";

    $required = ['name', 'category', 'price', 'description', 'stock'];
    $missingFields = [];

    foreach ($required as $field) {
        if (!isset($data[$field])) {
            $missingFields[] = $field;
        }
    }

    if (empty($missingFields)) {
        echo "<p style='color: green;'>✓ All required fields present</p>";
    } else {
        echo "<p style='color: red;'>✗ Missing: " . implode(', ', $missingFields) . "</p>";
    }
} else {
    echo "<p style='color: red;'>✗ JSON decode failed</p>";
}

echo "<h2>php://input Test</h2>";

// Simulate php://input
$testInput = '{"name":"Test Input","category":"Pupuk","price":50000,"description":"Test description","stock":100,"imageUrl":"","sellerId":"seller1"}';
echo "<p>Simulated input: " . $testInput . "</p>";

$simulatedData = json_decode($testInput, true);
if ($simulatedData) {
    echo "<p style='color: green;'>✓ Simulated input decoded successfully</p>";

    // Test the validation logic
    $required = ['name', 'category', 'price', 'description', 'stock'];
    $allPresent = true;

    foreach ($required as $field) {
        if (!isset($simulatedData[$field]) || $simulatedData[$field] === '') {
            echo "<p style='color: red;'>✗ Missing field: $field</p>";
            $allPresent = false;
        }
    }

    if ($allPresent) {
        echo "<p style='color: green;'>✓ All validations passed!</p>";
    }
} else {
    echo "<p style='color: red;'>✗ Failed to decode simulated input</p>";
}
?>