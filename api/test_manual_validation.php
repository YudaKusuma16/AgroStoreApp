<?php
// Set base path
$basePath = __DIR__;
define('BASE_PATH', $basePath);

// Include config directly instead of requiring id_generator which has path issues
$host = 'localhost';
$dbname = 'agrostore';
$username = 'root';
$password = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch(PDOException $e) {
    die("Database connection failed: " . $e->getMessage());
}

// First create a test product
$productId = 'test123';
$stmt = $pdo->prepare("INSERT INTO products (id, name, category, price, description, stock, seller_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
$stmt->execute([
    $productId,
    'Test Product',
    'Test Category',
    50.0,
    'Test Description',
    5,
    'seller1'
]);

echo "Created test product with 5 stock\n\n";

// Manually test the validation logic
try {
    $pdo->beginTransaction();

    // Test data
    $quantity = 6;

    echo "Checking stock for product: $productId\n";

    // Check stock availability (same as in orders API)
    $outOfStockItems = [];
    $productSql = "SELECT id, name, stock FROM products WHERE id = ? FOR UPDATE";
    $productStmt = $pdo->prepare($productSql);
    $productStmt->execute([$productId]);
    $product = $productStmt->fetch();

    echo "Product found: " . ($product ? "Yes" : "No") . "\n";

    if ($product) {
        echo "Product name: {$product['name']}\n";
        echo "Product stock: {$product['stock']}\n";
        echo "Requested quantity: $quantity\n";

        if (!$product) {
            $outOfStockItems[] = "Product ID {$productId} not found";
        } elseif ($product['stock'] < $quantity) {
            $outOfStockItems[] = "{$product['name']}: Requested {$quantity}, Available {$product['stock']}";
            echo "STOCK INSUFFICIENT!\n";
        } else {
            echo "Stock is sufficient\n";
        }
    }

    $pdo->rollBack();

    if (!empty($outOfStockItems)) {
        echo "\nValidation failed: " . implode(", ", $outOfStockItems) . "\n";
    } else {
        echo "\nValidation passed\n";
    }

} catch(Exception $e) {
    $pdo->rollBack();
    echo "Error: " . $e->getMessage() . "\n";
}
?>