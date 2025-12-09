<?php
echo "<h1>PHP Configuration Check</h1>";

echo "<h2>General Info</h2>";
echo "<p>PHP Version: " . phpversion() . "</p>";
echo "<p>Server Software: " . $_SERVER['SERVER_SOFTWARE'] . "</p>";

echo "<h2>JSON Support</h2>";
echo "<p>JSON extension: " . (extension_loaded('json') ? '✓ Enabled' : '✗ Disabled') . "</p>";
if (function_exists('json_decode')) {
    echo "<p>json_decode function: ✓ Available</p>";
} else {
    echo "<p>json_decode function: ✗ Not available</p>";
}

echo "<h2>Request Method Check</h2>";
echo "<p>Current request method: " . $_SERVER['REQUEST_METHOD'] . "</p>";

echo "<h2>POST Test</h2>";
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    echo "<p>✓ POST request detected</p>";

    echo "<h3>Raw POST data:</h3>";
    $postData = file_get_contents('php://input');
    echo "<pre>" . htmlspecialchars($postData) . "</pre>";

    if ($postData) {
        echo "<h3>Decoded JSON:</h3>";
        $decoded = json_decode($postData, true);
        if ($decoded) {
            echo "<pre>" . json_encode($decoded, JSON_PRETTY_PRINT) . "</pre>";
            echo "<p style='color: green;'>✓ JSON is valid</p>";
        } else {
            echo "<p style='color: red;'>✗ Invalid JSON</p>";
            echo "<p>Error: " . json_last_error_msg() . "</p>";
        }
    } else {
        echo "<p>No POST data received</p>";
    }
} else {
    echo "<p>Not a POST request</p>";
}

echo "<h2>Test Form</h2>";
echo "<form method='POST'>";
echo "<input type='hidden' name='test' value='testValue'>";
echo "<input type='submit' value='Submit POST Request'>";
echo "</form>";

if (isset($_POST['test'])) {
    echo "<p>POST parameter received: " . $_POST['test'] . "</p>";
}

echo "<h2>cURL Support</h2>";
echo "<p>cURL extension: " . (extension_loaded('curl') ? '✓ Enabled' : '✗ Disabled') . "</p>";

echo "<h2>Error Reporting</h2>";
echo "<p>Display errors: " . (ini_get('display_errors') ? 'On' : 'Off') . "</p>";
echo "<p>Error reporting level: " . ini_get('error_reporting') . "</p>";

// Turn on error reporting temporarily
ini_set('display_errors', 1);
ini_set('error_reporting', E_ALL);
echo "<p>Turned on error reporting for testing</p>";

echo "<h2>All HTTP Headers</h2>";
$headers = getallheaders();
foreach ($headers as $name => $value) {
    echo "<p>$name: $value</p>";
}
?>