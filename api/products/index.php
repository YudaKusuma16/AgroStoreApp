<?php
require_once '../config.php';
require_once '../helpers/id_generator.php';

// Get all products
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    try {
        // Optional category filter
        $category = isset($_GET['category']) ? $_GET['category'] : null;
        // Optional search query
        $search = isset($_GET['search']) ? $_GET['search'] : null;
        // Optional seller filter
        $sellerId = isset($_GET['seller_id']) ? $_GET['seller_id'] : null;

        $sql = "SELECT * FROM products WHERE 1=1";
        $params = [];

        if ($category) {
            $sql .= " AND category = ?";
            $params[] = $category;
        }

        if ($sellerId) {
            $sql .= " AND seller_id = ?";
            $params[] = $sellerId;
        }

        if ($search) {
            $sql .= " AND (name LIKE ? OR description LIKE ? OR category LIKE ?)";
            $searchTerm = "%$search%";
            $params[] = $searchTerm;
            $params[] = $searchTerm;
            $params[] = $searchTerm;
        }

        $sql .= " ORDER BY created_at DESC";

        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);
        $products = $stmt->fetchAll();

        sendResponse($products);

    } catch(PDOException $e) {
        sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
    }
}

// Add new product (seller only)
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $data = json_decode(file_get_contents('php://input'), true);

        // Debug: Log received data
      error_log("Received data: " . json_encode($data));
      error_log("Raw input: " . file_get_contents('php://input'));

      // For debugging, accept with or without sellerId
      $sellerId = $data['sellerId'] ?? $data['seller_id'] ?? 'seller1';

      // Validate required fields
      $required = ['name', 'category', 'price', 'description', 'stock'];
      $missingFields = [];

      foreach ($required as $field) {
          if (!isset($data[$field]) || (is_string($data[$field]) && trim($data[$field]) === '')) {
              $missingFields[] = $field;
          }
      }

      if (!empty($missingFields)) {
          error_log("Missing fields: " . implode(', ', $missingFields));
          error_log("All received keys: " . implode(', ', array_keys($data)));
          error_log("Data content: " . print_r($data, true));
          sendResponse([
              'error' => 'Missing required fields: ' . implode(', ', $missingFields),
              'missing_fields' => $missingFields,
              'received_keys' => array_keys($data)
          ], 400);
      }

      // Generate readable product ID
      $id = generateProductId();

      // Map camelCase to snake_case
      $productData = [
          'name' => $data['name'],
          'category' => $data['category'],
          'price' => $data['price'],
          'description' => $data['description'],
          'stock' => $data['stock'],
          'image_url' => $data['imageUrl'] ?? $data['image_url'] ?? '',
          'seller_id' => $sellerId
      ];

      $sql = "INSERT INTO products (id, name, category, price, description, stock, image_url, seller_id)
              VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

      $stmt = $pdo->prepare($sql);
      $result = $stmt->execute([
          $id,
          $productData['name'],
          $productData['category'],
          $productData['price'],
          $productData['description'],
          $productData['stock'],
          $productData['image_url'],
          $productData['seller_id']
      ]);

        if ($result) {
            sendResponse(['message' => 'Product added successfully', 'product_id' => $id]);
        } else {
            sendResponse(['error' => 'Failed to add product'], 500);
        }

    } catch(PDOException $e) {
        sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
    }
}
?>