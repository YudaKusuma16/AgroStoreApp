<?php
header('Content-Type: application/json');
echo json_encode(['test' => 'This is a new file', 'timestamp' => time()]);
?>