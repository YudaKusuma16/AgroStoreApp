<?php
// Script untuk update password user yang sudah ada
// Jalankan sekali untuk mengupdate password menjadi hash yang benar

$host = 'localhost';
$dbname = 'agrostore';
$username = 'root';
$password = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Update password untuk semua user menjadi 123456 (plaintext untuk testing)
    // atau gunakan hash yang benar
    $plainPassword = '123456';
    $hashedPassword = password_hash($plainPassword, PASSWORD_DEFAULT);

    $sql = "UPDATE users SET password = ? WHERE email IN (
        'petani@agro.com',
        'siti@agro.com',
        'toko@agro.com',
        'benih@agro.com',
        'alat@agro.com'
    )";

    $stmt = $pdo->prepare($sql);
    $stmt->execute([$hashedPassword]);

    echo "Passwords updated successfully!\n";
    echo "All users can now login with password: 123456\n";

} catch(PDOException $e) {
    die("Error: " . $e->getMessage());
}
?>