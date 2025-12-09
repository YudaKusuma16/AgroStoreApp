<?php
require_once 'config.php';

// Test adding product directly without cURL

echo "<h2>Direct Test Add Product</h2>";

// Simulate POST data
$_SERVER['REQUEST_METHOD'] = 'POST';

// Test data
$productData = [
    'name' => 'Test Product Direct',
    'category' => 'Pupuk',
    'price' => 50000.0,
    'description' => 'Test description',
    'stock' => 100,
    'imageUrl' => '',
    'sellerId' => 'seller1'
];

// Convert to JSON
$jsonData = json_encode($productData);

echo "<h3>Test Data:</h3>";
echo "<pre>" . $jsonData . "</pre>";

// Set php://input
stream_wrapper_register('phpinput', 'PhpInputStream');
file_put_contents('php://input', $jsonData);

class PhpInputStream {
    private $data = '';
    private $position = 0;

    public function stream_open($path, $mode, $options, &$opened_path) {
        return true;
    }

    public function stream_read($count) {
        $result = substr($this->data, $this->position, $count);
        $this->position += strlen($result);
        return $result;
    }

    public function stream_eof() {
        return $this->position >= strlen($this->data);
    }

    public function stream_stat() {
        return [];
    }

    public function url_stat($path, $flags) {
        return [];
    }
}

// Now include the actual API file
ob_start();
include 'products/index.php';
$output = ob_get_clean();

echo "<h3>API Response:</h3>";
echo "<pre>" . $output . "</pre>";

// Also test by calling directly
echo "<h2>Direct Function Test</h2>";

try {
    $data = json_decode($jsonData, true);

    // Validate required fields
    $required = ['name', 'category', 'price', 'description', 'stock'];
    $missingFields = [];

    foreach ($required as $field) {
        if (!isset($data[$field]) || (is_string($data[$field]) && trim($data[$field]) === '')) {
            $missingFields[] = $field;
        }
    }

    if (!empty($missingFields)) {
        echo "<p style='color: red;'>Missing fields: " . implode(', ', $missingFields) . "</p>";
    } else {
        echo "<p style='color: green;'>All required fields present!</p>";

        // Test database insert
        $sellerId = $data['sellerId'] ?? 'seller1';
        $id = 'test_' . uniqid();

        $sql = "INSERT INTO products (id, name, category, price, description, stock, image_url, seller_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        echo "<p>SQL: " . $sql . "</p>";
        echo "<p>Values: " . implode(', ', [
            $id,
            $data['name'],
            $data['category'],
            $data['price'],
            $data['description'],
            $data['stock'],
            $data['imageUrl'] ?? '',
            $sellerId
        ]) . "</p>";

        $stmt = $pdo->prepare($sql);
        $result = $stmt->execute([
            $id,
            $data['name'],
            $data['category'],
            $data['price'],
            $data['description'],
            $data['stock'],
            $data['imageUrl'] ?? '',
            $sellerId
        ]);

        if ($result) {
            echo "<p style='color: green;'>Product inserted successfully!</p>";
        } else {
            echo "<p style='color: red;'>Failed to insert product</p>";
        }
    }

} catch (Exception $e) {
    echo "<p style='color: red;'>Error: " . $e->getMessage() . "</p>";
}
?>