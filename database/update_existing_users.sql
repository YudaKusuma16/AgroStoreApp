-- Update passwords for existing users
-- Password untuk semua user adalah: 123456

-- Update passwords untuk pembeli
UPDATE users SET password = '$2y$10$rOKd7WzcxRNZB7AWo.7RweUeMZsxqnYIr1tkw0JkJhyxuNUEW.3my' WHERE email = 'petani@agro.com';
UPDATE users SET password = '$2y$10$rOKd7WzcxRNZB7AWo.7RweUeMZsxqnYIr1tkw0JkJhyxuNUEW.3my' WHERE email = 'siti@agro.com';

-- Update passwords untuk penjual
UPDATE users SET password = '$2y$10$rOKd7WzcxRNZB7AWo.7RweUeMZsxqnYIr1tkw0JkJhyxuNUEW.3my' WHERE email = 'toko@agro.com';
UPDATE users SET password = '$2y$10$rOKd7WzcxRNZB7AWo.7RweUeMZsxqnYIr1tkw0JkJhyxuNUEW.3my' WHERE email = 'benih@agro.com';
UPDATE users SET password = '$2y$10$rOKd7WzcxRNZB7AWo.7RweUeMZsxqnYIr1tkw0JkJhyxuNUEW.3my' WHERE email = 'alat@agro.com';

-- Note: $2y$10$rOKd7WzcxRNZB7AWo.7RweUeMZsxqnYIr1tkw0JkJhyxuNUEW.3my adalah hash dari "123456" menggunakan PASSWORD_DEFAULT